package cn.nukkit.network.protocol.types.biome;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(onConstructor_ = { @Deprecated })
public class BiomeCoordinateData {
    ExpressionOp minValueType;
    @Getter(AccessLevel.NONE)
    Indexable<String> minValue;
    ExpressionOp maxValueType;
    @Getter(AccessLevel.NONE)
    Indexable<String> maxValue;
    long gridOffset;
    long gridStepSize;
    RandomDistributionType distribution;

    public BiomeCoordinateData(ExpressionOp minValueType, String minValue, ExpressionOp maxValueType, String maxValue,
                               long gridOffset, long gridStepSize, RandomDistributionType distribution) {
        this.minValueType = minValueType;
        this.minValue = new Unindexed<>(minValue);
        this.maxValueType = maxValueType;
        this.maxValue = new Unindexed<>(maxValue);
        this.gridOffset = gridOffset;
        this.gridStepSize = gridStepSize;
        this.distribution = distribution;
    }

    public String getMinValue() {
        return minValue.get();
    }

    public String getMaxValue() {
        return maxValue.get();
    }
}
