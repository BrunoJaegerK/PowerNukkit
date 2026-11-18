package cn.nukkit.network.protocol.types.biome;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Map;

@Value
@RequiredArgsConstructor(onConstructor_ = { @Deprecated })
public class BiomeDefinitions {

    Map<String, BiomeDefinitionData> definitions;



    public Map<String, BiomeDefinitionData> getDefinitions() {
        return definitions;
    }
}
