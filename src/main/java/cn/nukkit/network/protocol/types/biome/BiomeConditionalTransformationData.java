package cn.nukkit.network.protocol.types.biome;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@RequiredArgsConstructor(onConstructor_ = { @Deprecated })
public class BiomeConditionalTransformationData {
    List<BiomeWeightedData> weightedBiomes;
    @Getter(AccessLevel.NONE)
    Indexable<String> conditionJson;
    long minPassingNeighbors;

    public BiomeConditionalTransformationData(List<BiomeWeightedData> weightedBiomes, String conditionJson,
                                              long minPassingNeighbors) {
        this.weightedBiomes = weightedBiomes;
        this.conditionJson = new Unindexed<>(conditionJson);
        this.minPassingNeighbors = minPassingNeighbors;
    }

    public String getConditionJson() {
        return conditionJson.get();
    }
}
