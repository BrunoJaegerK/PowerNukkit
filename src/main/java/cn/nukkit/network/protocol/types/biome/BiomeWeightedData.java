package cn.nukkit.network.protocol.types.biome;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(onConstructor_ = { @Deprecated })
public class BiomeWeightedData {
    String biome;
    int weight;



    public String getBiome() {
        return biome;
    }
}
