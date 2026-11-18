package cn.nukkit.network.protocol.types.biome;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(onConstructor_ = { @Deprecated })
public class BiomeElementData {
    float noiseFrequencyScale;
    float noiseLowerBound;
    float noiseUpperBound;
    ExpressionOp heightMinType;
    String heightMin;
    ExpressionOp heightMaxType;
    String heightMax;
    BiomeSurfaceMaterialData adjustedMaterials;



    public String getHeightMin() {
        return heightMin;
    }

    public String getHeightMax() {
        return heightMax;
    }
}
