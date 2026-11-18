package cn.nukkit.network.protocol.types.biome;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(onConstructor_ = { @Deprecated })
public class BiomeWeightedData {
    String biome;
    int weight;

    public BiomeWeightedData(String biome, int weight) {
        this.biome = biome;
        this.weight = weight;
    }

    public String getBiome() {
        return biome;
    }
}
