package cn.nukkit.network.protocol;

import cn.nukkit.block.Block;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.network.protocol.types.biome.*;
import cn.nukkit.utils.Utils;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import lombok.ToString;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.Iterator;
import java.util.zip.Deflater;

@ToString()
public class BiomeDefinitionListPacket extends DataPacket {

    public static final byte NETWORK_ID = ProtocolInfo.BIOME_DEFINITION_LIST_PACKET;

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Color.class, new ColorTypeAdapter())
            .registerTypeAdapter(Block.class, new BlockSerializer()) //避免GSON错误的扫描所有涉及方块的其他类
            .create();

    private static final LinkedHashMap<String, BiomeDefinitionData> BIOME_DEFINITIONS;
    private static final BatchPacket CACHED_PACKET;
    private LinkedHashMap<String, BiomeDefinitionData> biomeDefinitions;

    static {
        try {
            Type type = new TypeToken<LinkedHashMap<String, BiomeDefinitionData>>() {}.getType();
            BIOME_DEFINITIONS = GSON.fromJson(
                    Utils.loadJsonResource("bedrock/1.26.65/stripped_biome_definitions_844.json"), type);
            BiomeDefinitionListPacket packet = new BiomeDefinitionListPacket();
            packet.protocol = ProtocolInfo.CURRENT_PROTOCOL;
            packet.biomeDefinitions = BIOME_DEFINITIONS;
            packet.tryEncode();
            CACHED_PACKET = packet.compress(Deflater.BEST_COMPRESSION);
        } catch (Exception e) {
            throw new AssertionError("Unable to load Bedrock 1.26.65 biome definitions", e);
        }
    }

    public BiomeDefinitionListPacket() {
        this.biomeDefinitions = BIOME_DEFINITIONS;
    }

    public static DataPacket getCachedPacket(int protocol) {
        if (protocol < ProtocolInfo.v1_21_80) {
            throw new UnsupportedOperationException("This modernization branch targets Bedrock 1.26.65 only");
        }
        return CACHED_PACKET;
    }

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.decodeUnsupported();
    }

    @Override
    public void encode() {
        this.reset();
        if (this.protocol < ProtocolInfo.v1_21_80) {
            throw new UnsupportedOperationException("Modern biome definitions require protocol 800 or newer");
        }
        if (this.biomeDefinitions == null) {
            this.biomeDefinitions = BIOME_DEFINITIONS;
        }
        SequencedHashSet<String> strings = new SequencedHashSet<>();
        this.putUnsignedVarInt(this.biomeDefinitions.size());
        for (Map.Entry<String, BiomeDefinitionData> entry : this.biomeDefinitions.entrySet()) {
            this.putLShort(strings.addAndGetIndex(entry.getKey()));
            this.putBiomeDefinitionData(entry.getValue(), strings);
        }
        this.putUnsignedVarInt(strings.size());
        for (String value : strings) {
            this.putString(value);
        }
    }

    protected <T> void putOptional(Predicate<T> predicate, T value, Consumer<T> writer) {
        boolean present = predicate.test(value);
        this.putBoolean(present);
        if (present) {
            writer.accept(value);
        }
    }

    protected <T> void putOptionalNull(T value, Consumer<T> writer) {
        this.putBoolean(value != null);
        if (value != null) {
            writer.accept(value);
        }
    }

    private static final class SequencedHashSet<E> implements Iterable<E> {
        private final LinkedHashMap<E, Integer> indices = new LinkedHashMap<>();

        int addAndGetIndex(E value) {
            Integer index = indices.get(value);
            if (index == null) {
                index = indices.size();
                indices.put(value, index);
            }
            return index;
        }

        int size() {
            return indices.size();
        }

        @Override
        public Iterator<E> iterator() {
            return indices.keySet().iterator();
        }
    }

    protected void putBiomeDefinitionData(BiomeDefinitionData definition, SequencedHashSet<String> strings) {
        if (protocol >= ProtocolInfo.v1_21_100) {
            if (definition.getId() == null) {
                this.putLShort(-1); // Vanilla biomes don't contain ID field
            } else {
                this.putLShort(strings.addAndGetIndex(definition.getId()));
            }
        } else {
            this.putOptional(Objects::nonNull, definition.getId(), id -> this.putLShort(strings.addAndGetIndex(id)));
        }
        this.putLFloat(definition.getTemperature());
        this.putLFloat(definition.getDownfall());
        if (protocol >= ProtocolInfo.v1_21_111) {
            this.putLFloat(definition.getFoliageSnow());
        } else {
            this.putLFloat(definition.getRedSporeDensity());
            this.putLFloat(definition.getBlueSporeDensity());
            this.putLFloat(definition.getAshDensity());
            this.putLFloat(definition.getWhiteAshDensity());
        }
        this.putLFloat(definition.getDepth());
        this.putLFloat(definition.getScale());
        this.putLInt(definition.getMapWaterColor().getRGB());
        this.putBoolean(definition.isRain());
        if (false && this.protocol >= ProtocolInfo.v1_21_93) {
            this.putLInt(definition.getDimension());
            this.putString(definition.getVanilla() == null ? "" : definition.getVanilla());
        }
        this.putOptionalNull(definition.getTags(), tags -> {
            this.putUnsignedVarInt(tags.size());
            for (String tag : tags) {
                this.putLShort(strings.addAndGetIndex(tag));
            }
        });
        this.putOptionalNull(definition.getChunkGenData(), definitionChunkGen -> this.putBiomeDefinitionChunkGenData(definitionChunkGen, strings));
    }

    protected void putBiomeDefinitionChunkGenData(BiomeDefinitionChunkGenData definitionChunkGen, SequencedHashSet<String> strings) {
        this.putOptionalNull(definitionChunkGen.getClimate(), this::putClimate);
        this.putOptionalNull(definitionChunkGen.getConsolidatedFeatures(), (consolidatedFeatures) -> this.putConsolidatedFeatures(consolidatedFeatures, strings));
        this.putOptionalNull(definitionChunkGen.getMountainParams(), this::putMountainParamsData);
        this.putOptionalNull(definitionChunkGen.getSurfaceMaterialAdjustment(),
                (surfaceMaterialAdjustment) -> this.putSurfaceMaterialAdjustment(surfaceMaterialAdjustment, strings));
        this.putOptionalNull(definitionChunkGen.getSurfaceMaterial(), this::putSurfaceMaterial);
        if (protocol >= ProtocolInfo.v1_21_111) {
            this.putBoolean(definitionChunkGen.isHasDefaultOverworldSurface());
        }
        this.putBoolean(definitionChunkGen.isHasSwampSurface());
        this.putBoolean(definitionChunkGen.isHasFrozenOceanSurface());
        this.putBoolean(definitionChunkGen.isHasTheEndSurface());
        this.putOptionalNull(definitionChunkGen.getMesaSurface(), this::putMesaSurface);
        this.putOptionalNull(definitionChunkGen.getCappedSurface(), this::putCappedSurface);
        this.putOptionalNull(definitionChunkGen.getOverworldGenRules(),
                (overworldGenRules) -> this.putOverworldGenRules(overworldGenRules, strings));
        this.putOptionalNull(definitionChunkGen.getMultinoiseGenRules(), this::putMultinoiseGenRules);
        this.putOptionalNull(definitionChunkGen.getLegacyWorldGenRules(),
                (legacyWorldGenRules) -> this.putLegacyWorldGenRules(legacyWorldGenRules, strings));
        if (protocol >= ProtocolInfo.v1_21_120) {
            this.putOptionalNull(definitionChunkGen.getBiomeReplacementData(), this::putBiomeReplacementData);
        }
        if (protocol >= ProtocolInfo.v1_26_0) {
            this.putOptionalNull(definitionChunkGen.getVillageType(), villageType -> this.putByte((byte) villageType.intValue()));
        }
    }

    protected void putBiomeReplacementData(BiomeReplacementData replacementData) {
        this.putLShort(replacementData.getBiome());
        this.putLShort(replacementData.getDimension());
        this.putArray(replacementData.getTargetBiomes(), buf -> this.putLShort(buf));
        this.putLFloat(replacementData.getAmount());
        this.putLFloat(replacementData.getNoiseFrequencyScale());
        this.putLInt(replacementData.getReplacementIndex());
    }

    protected void putClimate(BiomeClimateData climate) {
        this.putLFloat(climate.getTemperature());
        this.putLFloat(climate.getDownfall());
        if (protocol <= ProtocolInfo.v1_21_100) {
            this.putLFloat(climate.getRedSporeDensity());
            this.putLFloat(climate.getBlueSporeDensity());
            this.putLFloat(climate.getAshDensity());
            this.putLFloat(climate.getWhiteAshDensity());
        }
        this.putLFloat(climate.getSnowAccumulationMin());
        this.putLFloat(climate.getSnowAccumulationMax());
    }

    protected void putConsolidatedFeatures(List<BiomeConsolidatedFeatureData> consolidatedFeatures, SequencedHashSet<String> strings) {
        this.putArray(consolidatedFeatures, consolidatedFeature -> this.putConsolidatedFeature(consolidatedFeature, strings));
    }

    protected void putConsolidatedFeature(BiomeConsolidatedFeatureData consolidatedFeature, SequencedHashSet<String> strings) {
        this.putScatterParam(consolidatedFeature.getScatter(), strings);
        this.putLShort(strings.addAndGetIndex(consolidatedFeature.getFeature()));
        this.putLShort(strings.addAndGetIndex(consolidatedFeature.getIdentifier()));
        this.putLShort(strings.addAndGetIndex(consolidatedFeature.getPass()));
        this.putBoolean(consolidatedFeature.isInternalUse());
    }

    protected void putScatterParam(BiomeScatterParamData scatterParam, SequencedHashSet<String> strings) {
        this.putArray(scatterParam.getCoordinates(), (coordinate) -> this.putCoordinate(coordinate, strings));
        this.putVarInt(scatterParam.getEvalOrder().ordinal());
        this.putVarInt(scatterParam.getChancePercentType() == null ? -1 : scatterParam.getChancePercentType().ordinal());
        this.putLShort(strings.addAndGetIndex(scatterParam.getChancePercent()));
        this.putLInt(scatterParam.getChanceNumerator());
        this.putLInt(scatterParam.getChangeDenominator());
        this.putVarInt(scatterParam.getIterationsType() == null ? -1 : scatterParam.getIterationsType().ordinal());
        this.putLShort(strings.addAndGetIndex(scatterParam.getIterations()));
    }

    protected void putCoordinate(BiomeCoordinateData coordinate, SequencedHashSet<String> strings) {
        this.putExpressionOp(coordinate.getMinValueType());
        this.putLShort(strings.addAndGetIndex(coordinate.getMinValue()));
        this.putExpressionOp(coordinate.getMaxValueType());
        this.putLShort(strings.addAndGetIndex(coordinate.getMaxValue()));
        this.putLInt((int) coordinate.getGridOffset());
        this.putLInt((int) coordinate.getGridStepSize());
        this.putVarInt(coordinate.getDistribution().ordinal());
    }

    protected void putMountainParamsData(BiomeMountainParamsData mountainParams) {
        this.putBlockNetId(mountainParams.getSteepBlock());
        this.putBoolean(mountainParams.isNorthSlopes());
        this.putBoolean(mountainParams.isSouthSlopes());
        this.putBoolean(mountainParams.isWestSlopes());
        this.putBoolean(mountainParams.isEastSlopes());
        this.putBoolean(mountainParams.isTopSlideEnabled());
    }

    protected void putSurfaceMaterialAdjustment(BiomeSurfaceMaterialAdjustmentData surfaceMaterialAdjustment, SequencedHashSet<String> strings) {
        this.putArray(surfaceMaterialAdjustment.getBiomeElements(), (biomeElement) -> this.putBiomeElement(biomeElement, strings));
    }

    protected void putBiomeElement(BiomeElementData biomeElement, SequencedHashSet<String> strings) {
        this.putLFloat(biomeElement.getNoiseFrequencyScale());
        this.putLFloat(biomeElement.getNoiseLowerBound());
        this.putLFloat(biomeElement.getNoiseUpperBound());
        this.putExpressionOp(biomeElement.getHeightMinType());
        this.putLShort(strings.addAndGetIndex(biomeElement.getHeightMin()));
        this.putExpressionOp(biomeElement.getHeightMaxType());
        this.putLShort(strings.addAndGetIndex(biomeElement.getHeightMax()));
        this.putSurfaceMaterial(biomeElement.getAdjustedMaterials());
    }

    protected void putSurfaceMaterial(BiomeSurfaceMaterialData surfaceMaterial) {
        this.putBlockNetId(surfaceMaterial.getTopBlock());
        this.putBlockNetId(surfaceMaterial.getMidBlock());
        this.putBlockNetId(surfaceMaterial.getSeaFloorBlock());
        this.putBlockNetId(surfaceMaterial.getFoundationBlock());
        this.putBlockNetId(surfaceMaterial.getSeaBlock());
        this.putLInt(surfaceMaterial.getSeaFloorDepth());
    }

    protected void putMesaSurface(BiomeMesaSurfaceData mesaSurface) {
        this.putBlockNetId(mesaSurface.getClayMaterial());
        this.putBlockNetId(mesaSurface.getHardClayMaterial());
        this.putBoolean(mesaSurface.isBrycePillars());
        this.putBoolean(mesaSurface.isHasForest());
    }

    protected void putCappedSurface(BiomeCappedSurfaceData cappedSurface) {
        this.putArray(cappedSurface.getFloorBlocks(), this::putBlockNetId);
        this.putArray(cappedSurface.getCeilingBlocks(), this::putBlockNetId);
        this.putOptionalNull(cappedSurface.getSeaBlock(), this::putBlockNetId);
        this.putOptionalNull(cappedSurface.getFoundationBlock(), this::putBlockNetId);
        this.putOptionalNull(cappedSurface.getBeachBlock(), this::putBlockNetId);
    }

    protected void putOverworldGenRules(BiomeOverworldGenRulesData overworldGenRules, SequencedHashSet<String> strings) {
        Consumer<BiomeWeightedData> writeWeight = (data) -> this.putWeight(data, strings);
        this.putArray(overworldGenRules.getHillsTransformations(), writeWeight);
        this.putArray(overworldGenRules.getMutateTransformations(), writeWeight);
        this.putArray(overworldGenRules.getRiverTransformations(), writeWeight);
        this.putArray(overworldGenRules.getShoreTransformations(), writeWeight);
        Consumer<BiomeConditionalTransformationData> writeConditionalTransformation = (data) -> this.putConditionalTransformation(data, strings);
        this.putArray(overworldGenRules.getPreHillsEdgeTransformations(), writeConditionalTransformation);
        this.putArray(overworldGenRules.getPostShoreTransformations(), writeConditionalTransformation);
        this.putArray(overworldGenRules.getClimateTransformations(), this::putWeightedTemperature);
    }

    protected void putWeight(BiomeWeightedData weightedData, SequencedHashSet<String> strings) {
        this.putLShort(strings.addAndGetIndex(weightedData.getBiome()));
        this.putLInt(weightedData.getWeight());
    }

    protected void putConditionalTransformation(BiomeConditionalTransformationData conditionalTransformation, SequencedHashSet<String> strings) {
        this.putArray(conditionalTransformation.getWeightedBiomes(), (data) -> putWeight(data, strings));
        this.putLShort(strings.addAndGetIndex(conditionalTransformation.getConditionJson()));
        this.putLInt((int) conditionalTransformation.getMinPassingNeighbors());
    }

    protected void putWeightedTemperature(BiomeWeightedTemperatureData weightedTemperature) {
        this.putVarInt(weightedTemperature.getTemperature().ordinal());
        this.putLInt((int) weightedTemperature.getWeight());
    }

    protected void putMultinoiseGenRules(BiomeMultinoiseGenRulesData multinoiseGenRules) {
        this.putLFloat(multinoiseGenRules.getTemperature());
        this.putLFloat(multinoiseGenRules.getHumidity());
        this.putLFloat(multinoiseGenRules.getAltitude());
        this.putLFloat(multinoiseGenRules.getWeirdness());
        this.putLFloat(multinoiseGenRules.getWeight());
    }

    protected void putLegacyWorldGenRules(BiomeLegacyWorldGenRulesData legacyWorldGenRules, SequencedHashSet<String> strings) {
        this.putArray(legacyWorldGenRules.getLegacyPreHills(), (data) -> this.putConditionalTransformation(data, strings));
    }

    protected void putExpressionOp(ExpressionOp expressionOp) {
        if (expressionOp == null) {
            this.putVarInt(-1);
            return;
        }
        this.putVarInt(expressionOp.ordinal());
    }

    protected void putBlockNetId(Block block) {
        this.putLInt(GlobalBlockPalette.getOrCreateRuntimeId(block.getId(), block.getDamage()));
    }

    protected static class ColorTypeAdapter extends TypeAdapter<Color> {

        @Override
        public void write(JsonWriter out, Color color) {
        }

        @Override
        public Color read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }

            int r = 0, g = 0, b = 0, a = 255;
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "r": r = in.nextInt(); break;
                    case "g": g = in.nextInt(); break;
                    case "b": b = in.nextInt(); break;
                    case "a": a = in.nextInt(); break;
                    default: in.skipValue(); break;
                }
            }
            in.endObject();
            return new Color(r, g, b, a);
        }
    }

    protected static class BlockSerializer implements JsonSerializer<Block>, JsonDeserializer<Block> {

        @Override
        public JsonElement serialize(Block src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", src.getId());
            obj.addProperty("meta", src.getDamage());
            return obj;
        }

        @Override
        public Block deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            int id = obj.get("id").getAsInt();
            int meta = obj.get("meta").getAsInt();
            return Block.get(id, meta);
        }
    }
}
