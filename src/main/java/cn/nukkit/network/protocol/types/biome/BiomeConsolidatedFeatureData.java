package cn.nukkit.network.protocol.types.biome;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(onConstructor_ = { @Deprecated })
public class BiomeConsolidatedFeatureData {
    BiomeScatterParamData scatter;
    String feature;
    String identifier;
    undefined
    boolean internalUse;

    public BiomeConsolidatedFeatureData(BiomeScatterParamData scatter, String feature, String identifier, String pass,
                                        boolean internalUse) {
        this.scatter = scatter;
        this.feature = feature;
        this.identifier = identifier;
        this.pass = pass;
        this.internalUse = internalUse;
    }

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