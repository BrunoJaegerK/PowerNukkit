package cn.nukkit.network.protocol.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteOrder;
import java.util.zip.GZIPInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only Bedrock 1.26.65 runtime metadata, kept separate from PowerNukkit's
 * legacy runtime mappings until packet and item conversion are migrated.
 */
public final class BedrockDataRegistry {

    private static final String ROOT = "bedrock/1.26.65/";

    private final Map<String, RuntimeItemState> itemsByName;
    private final Map<Integer, RuntimeItemState> itemsById;
    private final Map<String, JsonObject> blockProperties;
    private final Map<String, List<String>> itemTags;
    private final List<RuntimeItemState> runtimeItems;
    private final byte[] itemDataPalette;
    private final CompoundTag itemComponents;
    private static volatile BedrockDataRegistry defaultRegistry;

    private BedrockDataRegistry(Map<String, RuntimeItemState> itemsByName,
                                Map<Integer, RuntimeItemState> itemsById,
                                Map<String, JsonObject> blockProperties,
                                Map<String, List<String>> itemTags,
                                List<RuntimeItemState> runtimeItems,
                                byte[] itemDataPalette,
                                CompoundTag itemComponents) {
        this.itemsByName = Collections.unmodifiableMap(itemsByName);
        this.itemsById = Collections.unmodifiableMap(itemsById);
        this.blockProperties = Collections.unmodifiableMap(blockProperties);
        this.itemTags = Collections.unmodifiableMap(itemTags);
        this.runtimeItems = Collections.unmodifiableList(runtimeItems);
        this.itemDataPalette = itemDataPalette.clone();
        this.itemComponents = itemComponents;
    }

    public static BedrockDataRegistry loadDefault() throws IOException {
        ClassLoader loader = BedrockDataRegistry.class.getClassLoader();
        Map<String, RuntimeItemState> itemsByName = new LinkedHashMap<>();
        Map<Integer, RuntimeItemState> itemsById = new HashMap<>();
        Map<String, JsonObject> blockProperties = new LinkedHashMap<>();
        Map<String, List<String>> itemTags = new LinkedHashMap<>();
        List<RuntimeItemState> runtimeItems = new ArrayList<>();
        BinaryStream itemPalette = new BinaryStream();

        try (InputStream stream = requiredResource(loader, ROOT + "runtime_item_states_2225.json");
             InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonArray entries = new JsonParser().parse(reader).getAsJsonArray();
            itemPalette.putUnsignedVarInt(entries.size());
            for (JsonElement element : entries) {
                JsonObject entry = element.getAsJsonObject();
                String name = requiredString(entry, "name");
                int id = entry.get("id").getAsInt();
                int version = entry.get("version").getAsInt();
                boolean componentBased = entry.get("componentBased").getAsBoolean();
                RuntimeItemState state = new RuntimeItemState(name, id, version, componentBased);
                runtimeItems.add(state);
                itemPalette.putString(name.startsWith("minecraft:") ? name.substring("minecraft:".length()) : name);
                itemPalette.putLShort(id);
                itemPalette.putBoolean(componentBased);
                if (itemsByName.put(name, state) != null) {
                    throw new IllegalStateException("Duplicate Bedrock runtime item name: " + name);
                }
                if (itemsById.put(id, state) != null) {
                    throw new IllegalStateException("Duplicate Bedrock runtime item id: " + id);
                }
            }
        }

        try (InputStream stream = requiredResource(loader, ROOT + "block_properties_2193.json");
             InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonArray entries = new JsonParser().parse(reader).getAsJsonArray();
            for (JsonElement element : entries) {
                JsonObject entry = element.getAsJsonObject();
                String name = requiredString(entry, "name");
                JsonObject properties = entry.getAsJsonObject("properties");
                if (properties == null || blockProperties.put(name, properties) != null) {
                    throw new IllegalStateException("Invalid or duplicate Bedrock block properties: " + name);
                }
            }
        }

        try (InputStream stream = requiredResource(loader, ROOT + "item_tags_2225.json");
             InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonObject tags = new JsonParser().parse(reader).getAsJsonObject();
            for (Map.Entry<String, JsonElement> tag : tags.entrySet()) {
                JsonArray values = tag.getValue().getAsJsonArray();
                List<String> members = new ArrayList<>(values.size());
                for (JsonElement value : values) {
                    members.add(value.getAsString());
                }
                itemTags.put(tag.getKey(), Collections.unmodifiableList(members));
            }
        }

        CompoundTag itemComponents;
        try (InputStream stream = requiredResource(loader, ROOT + "item_components_2225.nbt");
             GZIPInputStream gzip = new GZIPInputStream(stream)) {
            itemComponents = NBTIO.read(gzip, ByteOrder.BIG_ENDIAN, false);
        }

        if (itemsByName.isEmpty() || blockProperties.isEmpty() || itemTags.isEmpty() || itemComponents == null) {
            throw new IllegalStateException("Bedrock 1.26.65 data registry is incomplete");
        }
        return new BedrockDataRegistry(itemsByName, itemsById, blockProperties, itemTags, runtimeItems, itemPalette.getBuffer(), itemComponents);
    }

    private static InputStream requiredResource(ClassLoader loader, String path) {
        InputStream stream = loader.getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException("Missing Bedrock data resource: " + path);
        }
        return stream;
    }

    private static String requiredString(JsonObject object, String key) {
        JsonElement value = object.get(key);
        if (value == null || value.isJsonNull()) {
            throw new IllegalStateException("Missing required JSON property: " + key);
        }
        String result = value.getAsString();
        if (result.isEmpty()) {
            throw new IllegalStateException("Empty required JSON property: " + key);
        }
        return result;
    }

    public static BedrockDataRegistry getDefault() {
        BedrockDataRegistry registry = defaultRegistry;
        if (registry == null) {
            synchronized (BedrockDataRegistry.class) {
                registry = defaultRegistry;
                if (registry == null) {
                    try {
                        registry = loadDefault();
                    } catch (IOException e) {
                        throw new IllegalStateException("Unable to load Bedrock 1.26.65 metadata", e);
                    }
                    defaultRegistry = registry;
                }
            }
        }
        return registry;
    }

    public CompoundTag getItemComponentData(String identifier) {
        CompoundTag component = itemComponents.getCompound(identifier);
        return component == null ? null : component.copy();
    }

    public int getItemComponentCount() {
        return itemComponents.getTags().size();
    }

    public byte[] getItemDataPalette() {
        return itemDataPalette.clone();
    }

    public List<RuntimeItemState> getRuntimeItems() {
        return runtimeItems;
    }

    public Map<String, JsonObject> getBlockPropertiesEntries() {
        return blockProperties;
    }

    public RuntimeItemState getItem(String identifier) {
        return itemsByName.get(identifier);
    }

    public RuntimeItemState getItem(int runtimeId) {
        return itemsById.get(runtimeId);
    }

    public JsonObject getBlockProperties(String identifier) {
        return blockProperties.get(identifier);
    }

    public List<String> getItemsForTag(String tag) {
        List<String> values = itemTags.get(tag);
        return values == null ? Collections.<String>emptyList() : values;
    }

    public int getRuntimeItemCount() {
        return itemsByName.size();
    }

    public int getBlockPropertyCount() {
        return blockProperties.size();
    }

    public int getItemTagCount() {
        return itemTags.size();
    }

    public int getComponentBasedItemCount() {
        int count = 0;
        for (RuntimeItemState item : itemsByName.values()) {
            if (item.isComponentBased()) {
                count++;
            }
        }
        return count;
    }

    public static final class RuntimeItemState {
        private final String name;
        private final int id;
        private final int version;
        private final boolean componentBased;

        private RuntimeItemState(String name, int id, int version, boolean componentBased) {
            this.name = name;
            this.id = id;
            this.version = version;
            this.componentBased = componentBased;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        public int getVersion() {
            return version;
        }

        public boolean isComponentBased() {
            return componentBased;
        }
    }
}
