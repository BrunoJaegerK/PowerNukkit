package cn.nukkit.network.protocol.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

    private BedrockDataRegistry(Map<String, RuntimeItemState> itemsByName,
                                Map<Integer, RuntimeItemState> itemsById,
                                Map<String, JsonObject> blockProperties,
                                Map<String, List<String>> itemTags) {
        this.itemsByName = Collections.unmodifiableMap(itemsByName);
        this.itemsById = Collections.unmodifiableMap(itemsById);
        this.blockProperties = Collections.unmodifiableMap(blockProperties);
        this.itemTags = Collections.unmodifiableMap(itemTags);
    }

    public static BedrockDataRegistry loadDefault() throws IOException {
        ClassLoader loader = BedrockDataRegistry.class.getClassLoader();
        Map<String, RuntimeItemState> itemsByName = new HashMap<>();
        Map<Integer, RuntimeItemState> itemsById = new HashMap<>();
        Map<String, JsonObject> blockProperties = new HashMap<>();
        Map<String, List<String>> itemTags = new HashMap<>();

        try (InputStream stream = requiredResource(loader, ROOT + "runtime_item_states_2225.json");
             InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonArray entries = new JsonParser().parse(reader).getAsJsonArray();
            for (JsonElement element : entries) {
                JsonObject entry = element.getAsJsonObject();
                String name = requiredString(entry, "name");
                int id = entry.get("id").getAsInt();
                int version = entry.get("version").getAsInt();
                boolean componentBased = entry.get("componentBased").getAsBoolean();
                RuntimeItemState state = new RuntimeItemState(name, id, version, componentBased);
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

        if (itemsByName.isEmpty() || blockProperties.isEmpty() || itemTags.isEmpty()) {
            throw new IllegalStateException("Bedrock 1.26.65 data registry is incomplete");
        }
        return new BedrockDataRegistry(itemsByName, itemsById, blockProperties, itemTags);
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
