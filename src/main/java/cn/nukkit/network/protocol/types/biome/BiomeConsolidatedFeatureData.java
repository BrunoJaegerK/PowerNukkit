package cn.nukkit.network.protocol.types.biome;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(onConstructor_ = { @Deprecated })
public class BiomeConsolidatedFeatureData {
    BiomeScatterParamData scatter;
    String feature;
    String identifier;
    String pass;
    boolean internalUse;



    public String getFeature() {
        return feature;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getPass() {
        return pass;
    }
}