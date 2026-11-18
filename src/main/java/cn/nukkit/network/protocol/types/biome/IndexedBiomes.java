package cn.nukkit.network.protocol.types.biome;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Resolves an indexed biome palette without depending on an external protocol library. */
@SuppressWarnings("rawtypes")
public final class IndexedBiomes implements Indexable<Map<String, BiomeDefinitionData>> {
    private final List<Map.Entry<Integer, BiomeDefinitionData>> biomes;
    private final List<String> values;
    private Map<String, BiomeDefinitionData> cached;

    public IndexedBiomes(List<Map.Entry<Integer, BiomeDefinitionData>> biomes, List<String> values) {
        this.biomes = biomes;
        this.values = values;
    }

    @Override
    public Map<String, BiomeDefinitionData> get() {
        if (cached == null) {
            Map<String, BiomeDefinitionData> map = new LinkedHashMap<>();
            for (Map.Entry<Integer, BiomeDefinitionData> pair : biomes) {
                map.put(values.get(pair.getKey()), pair.getValue());
            }
            cached = map;
        }
        return cached;
    }
}
