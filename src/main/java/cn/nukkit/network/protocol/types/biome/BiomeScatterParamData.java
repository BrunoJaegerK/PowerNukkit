package cn.nukkit.network.protocol.types.biome;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@RequiredArgsConstructor(onConstructor_ = { @Deprecated })
public class BiomeScatterParamData {

    List<BiomeCoordinateData> coordinates;
    CoordinateEvaluationOrder evalOrder;
    ExpressionOp chancePercentType;
    String chancePercent;
    int chanceNumerator;
    int changeDenominator;
    ExpressionOp iterationsType;
    String iterations;

    public BiomeScatterParamData(List<BiomeCoordinateData> coordinates, CoordinateEvaluationOrder evalOrder,
                                 ExpressionOp chancePercentType, String chancePercent, int chanceNumerator,
                                 int changeDenominator, ExpressionOp iterationsType, String iterations) {
        this.coordinates = coordinates;
        this.evalOrder = evalOrder;
        this.chancePercentType = chancePercentType;
        this.chancePercent = chancePercent;
        this.chanceNumerator = chanceNumerator;
        this.changeDenominator = changeDenominator;
        this.iterationsType = iterationsType;
        this.iterations = iterations;
    }

    public String getChancePercent() {
        return chancePercent;
    }

    public String getIterations() {
        return iterations;
    }
}
