package cn.nukkit.network.protocol.types.biome;

import lombok.AccessLevel;
import lombok.Getter;
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

    public BiomeElementData(float noiseFrequencyScale, float noiseLowerBound, float noiseUpperBound,
                            ExpressionOp heightMinType, String heightMin, ExpressionOp heightMaxType,
                            String heightMax, BiomeSurfaceMaterialData adjustedMaterials) {
        this.noiseFrequencyScale = noiseFrequencyScale;
        this.noiseLowerBound = noiseLowerBound;
        this.noiseUpperBound = noiseUpperBound;
        this.heightMinType = heightMinType;
        this.heightMin = heightMin;
        this.heightMaxType = heightMaxType;
        this.heightMax = heightMax;
        this.adjustedMaterials = adjustedMaterials;
    }

    public String getHeightMin() {
        return heightMin;
    }

    public String getHeightMax() {
        return heightMax;
    }
}
