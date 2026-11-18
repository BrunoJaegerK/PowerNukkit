package cn.nukkit.network.protocol.types.biome;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@RequiredArgsConstructor(onConstructor_ = { @Deprecated })
public class BiomeConditionalTransformationData {
    List<BiomeWeightedData> weightedBiomes;
    String conditionJson;
    long minPassingNeighbors;



    public String getConditionJson() {
        return conditionJson;
    }
}
