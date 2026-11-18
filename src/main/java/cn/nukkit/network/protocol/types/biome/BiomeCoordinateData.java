package cn.nukkit.network.protocol.types.biome;

import lombok.AccessLevel;
import lombok.Getter;
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

    public BiomeCoordinateData(ExpressionOp minValueType, String minValue, ExpressionOp maxValueType, String maxValue,
                               long gridOffset, long gridStepSize, RandomDistributionType distribution) {
        this.minValueType = minValueType;
        this.minValue = minValue;
        this.maxValueType = maxValueType;
        this.maxValue = maxValue;
        this.gridOffset = gridOffset;
        this.gridStepSize = gridStepSize;
        this.distribution = distribution;
    }

    public String getMinValue() {
        return minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }
}
