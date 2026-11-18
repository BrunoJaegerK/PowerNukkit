package cn.nukkit.network.protocol.types.biome;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Map;

@Value
@RequiredArgsConstructor(onConstructor_ = { @Deprecated })
public class BiomeDefinitions {

    @Getter(AccessLevel.NONE)
    Indexable<Map<String, BiomeDefinitionData>> definitions;

    public BiomeDefinitions(Map<String, BiomeDefinitionData> definitions) {
        this.definitions = new UnindexedBiomes(definitions);
    }

    public Map<String, BiomeDefinitionData> getDefinitions() {
        return definitions.get();
    }
}
