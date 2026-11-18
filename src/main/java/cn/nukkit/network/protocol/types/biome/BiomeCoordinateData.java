package cn.nukkit.network.protocol.types.biome;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(onConstructor_ = { @Deprecated })
public class BiomeCoordinateData {
    ExpressionOp minValueType;
    String minValue;
    ExpressionOp maxValueType;
    String maxValue;
    long gridOffset;
    long gridStepSize;
    RandomDistributionType distribution;



    public String getMinValue() {
        return minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }
}
