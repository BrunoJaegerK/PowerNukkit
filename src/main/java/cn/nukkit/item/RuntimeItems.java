package cn.nukkit.item;

import cn.nukkit.Server;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.data.BedrockDataRegistry;
import cn.nukkit.api.PowerNukkitOnly;
import cn.nukkit.api.Since;
import cn.nukkit.utils.BinaryStream;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;



@Since("1.4.0.0-PN")
@UtilityClass
@Log4j2
public class RuntimeItems {

    private static final RuntimeItemMapping itemPalette;

    static {
        log.debug("Loading Bedrock 1.26.65 runtime items...");
        BedrockDataRegistry registry = BedrockDataRegistry.getDefault();
        Map<String, Integer> legacyIds = loadLegacyIds();
        Map<String, LegacyMapping> legacyMappings = loadLegacyMappings();

        Int2IntMap legacyNetworkMap = new Int2IntOpenHashMap();
        Int2IntMap networkLegacyMap = new Int2IntOpenHashMap();
        legacyNetworkMap.defaultReturnValue(-1);
        networkLegacyMap.defaultReturnValue(-1);
        Map<String, Integer> namespaceNetworkMap = new LinkedHashMap<>();
        Int2ObjectMap<String> networkNamespaceMap = new Int2ObjectOpenHashMap<>();

        for (BedrockDataRegistry.RuntimeItemState entry : registry.getRuntimeItems()) {
            String identifier = entry.getName();
            int runtimeId = entry.getId();
            networkNamespaceMap.put(runtimeId, identifier);
            namespaceNetworkMap.put(identifier, runtimeId);

            List<LegacyMapping> variants = legacyMappings.get(identifier);
            boolean hasLegacyMapping = false;
            if (variants != null) {
                for (LegacyMapping mapping : variants) {
                    if (mapping.protocol > ProtocolInfo.CURRENT_PROTOCOL) {
                        continue;
                    }
                    Integer legacyId = legacyIds.get(mapping.legacyName);
                    if (legacyId == null) {
                        continue;
                    }

                    int fullId = getFullId(legacyId, mapping.damage);
                    int networkFullId = (runtimeId << 1) | 1;
                    // Keep every supported legacy alias for outbound stacks. Prefer
                    // flattened variants over generic names that share the same ID.
                    legacyNetworkMap.put(fullId, networkFullId);
                    // Decode a modern item to the first (canonical PowerNukkit) legacy
                    // variant while retaining older aliases for encoding.
                    if (!networkLegacyMap.containsKey(runtimeId)) {
                        networkLegacyMap.put(runtimeId, fullId | 1);
                    }
                    hasLegacyMapping = true;
                }
            }

            if (!hasLegacyMapping) {
                Integer legacyId = legacyIds.get(identifier);
                if (legacyId != null) {
                    int fullId = getFullId(legacyId, 0);
                    int networkFullId = runtimeId << 1;
                    if (!legacyNetworkMap.containsKey(fullId)) {
                        legacyNetworkMap.put(fullId, networkFullId);
                    }
                    if (!networkLegacyMap.containsKey(runtimeId)) {
                        networkLegacyMap.put(runtimeId, fullId);
                    }
                }
            }
        }
        itemPalette = new RuntimeItemMapping(
                registry.getItemDataPalette(),
                legacyNetworkMap,
                networkLegacyMap,
                namespaceNetworkMap,
                networkNamespaceMap
        );
        log.info("Loaded {} Bedrock runtime item states ({} legacy item mappings, {} component-based items)",
                registry.getRuntimeItemCount(), legacyNetworkMap.size(), registry.getComponentBasedItemCount());
    }

    private static Map<String, Integer> loadLegacyIds() {
        Map<String, Integer> legacyIds = new LinkedHashMap<>();
        try (InputStream stream = Server.class.getClassLoader().getResourceAsStream("legacy_item_ids.json")) {
            if (stream == null) {
                throw new AssertionError("Unable to load legacy_item_ids.json");
            }
            JsonObject json = new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                legacyIds.put(entry.getKey(), entry.getValue().getAsInt());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return legacyIds;
    }

    private static Map<String, List<LegacyMapping>> loadLegacyMappings() {
        Map<String, List<LegacyMapping>> mappings = new HashMap<>();
        try (InputStream stream = Server.class.getClassLoader().getResourceAsStream("item_mappings.json")) {
            if (stream == null) {
                throw new AssertionError("Unable to load item_mappings.json");
            }
            JsonObject json = new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> legacyEntry : json.entrySet()) {
                JsonObject variants = legacyEntry.getValue().getAsJsonObject();
                int protocol = variants.has("protocol") ? variants.get("protocol").getAsInt() : 0;
                for (Map.Entry<String, JsonElement> variant : variants.entrySet()) {
                    if ("protocol".equalsIgnoreCase(variant.getKey())) {
                        continue;
                    }
                    String identifier = variant.getValue().getAsString();
                    if (identifier.isEmpty()) {
                        continue;
                    }
                    int damage = Integer.parseInt(variant.getKey());
                    mappings.computeIfAbsent(identifier, key -> new ArrayList<>())
                            .add(new LegacyMapping(legacyEntry.getKey(), damage, protocol));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return mappings;
    }

    private static final class LegacyMapping {
        private final String legacyName;
        private final int damage;
        private final int protocol;

        private LegacyMapping(String legacyName, int damage, int protocol) {
            this.legacyName = legacyName;
            this.damage = damage;
            this.protocol = protocol;
        }
    }

    @PowerNukkitOnly
    @Since("1.4.0.0-PN")
    public static RuntimeItemMapping getRuntimeMapping() {
        return itemPalette;
    }

    @PowerNukkitOnly
    @Since("1.4.0.0-PN")
    public static int getId(int fullId) {
        return (short) (fullId >> 16);
    }

    @PowerNukkitOnly
    @Since("1.4.0.0-PN")
    public static int getData(int fullId) {
        return ((fullId >> 1) & 0x7fff);
    }

    @PowerNukkitOnly
    @Since("1.4.0.0-PN")
    public static int getFullId(int id, int data) {
        return (((short) id) << 16) | ((data & 0x7fff) << 1);
    }

    @PowerNukkitOnly
    @Since("1.4.0.0-PN")
    public static int getNetworkId(int networkFullId) {
        return networkFullId >> 1;
    }

    @PowerNukkitOnly
    @Since("1.4.0.0-PN")
    public static boolean hasData(int id) {
        return (id & 0x1) != 0;
    }

    @ToString
    @RequiredArgsConstructor
    static class Entry {
        String name;
        int id;
        Integer oldId;
        Integer oldData;
        @PowerNukkitOnly
        @Since("1.4.0.0-PN")
        Boolean deprecated;
    }
}
