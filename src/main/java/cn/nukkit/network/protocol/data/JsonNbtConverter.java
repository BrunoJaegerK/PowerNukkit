package cn.nukkit.network.protocol.data;

import cn.nukkit.nbt.tag.ByteTag;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.IntTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.LongTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.nbt.tag.Tag;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

/** Converts the versioned Bedrock JSON block-property data into network NBT. */
public final class JsonNbtConverter {

    private JsonNbtConverter() {
    }

    public static CompoundTag toCompoundTag(JsonObject object) {
        CompoundTag compound = new CompoundTag("");
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            Tag tag = toTag(entry.getKey(), entry.getValue());
            if (tag != null) {
                compound.put(entry.getKey(), tag);
            }
        }
        return compound;
    }

    private static Tag toTag(String name, JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonObject()) {
            return toCompoundTag(name, element.getAsJsonObject());
        }
        if (element.isJsonArray()) {
            return toListTag(name, element.getAsJsonArray());
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isBoolean()) {
            return new ByteTag(name, primitive.getAsBoolean() ? 1 : 0);
        }
        if (primitive.isString()) {
            return new StringTag(name, primitive.getAsString());
        }
        if (primitive.isNumber()) {
            return numberTag(name, primitive.getAsBigDecimal());
        }
        throw new IllegalArgumentException("Unsupported JSON value in Bedrock block properties: " + element);
    }

    private static CompoundTag toCompoundTag(String name, JsonObject object) {
        CompoundTag compound = new CompoundTag(name);
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            Tag tag = toTag(entry.getKey(), entry.getValue());
            if (tag != null) {
                compound.put(entry.getKey(), tag);
            }
        }
        return compound;
    }

    private static ListTag<Tag> toListTag(String name, JsonArray array) {
        ListTag<Tag> list = new ListTag<>(name);
        if (array.size() == 0) {
            list.type = 0;
            return list;
        }

        boolean allNumbers = true;
        boolean hasFraction = false;
        for (JsonElement element : array) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
                allNumbers = false;
                break;
            }
            if (element.getAsBigDecimal().stripTrailingZeros().scale() > 0) {
                hasFraction = true;
            }
        }

        byte expectedType = 0;
        for (JsonElement element : array) {
            Tag tag;
            if (allNumbers && hasFraction) {
                tag = new DoubleTag("", element.getAsDouble());
            } else {
                tag = toTag("", element);
            }
            if (expectedType == 0) {
                expectedType = tag.getId();
            } else if (expectedType != tag.getId()) {
                throw new IllegalArgumentException("Mixed NBT list element types in Bedrock block properties: " + name);
            }
            list.add(tag);
        }
        return list;
    }

    private static Tag numberTag(String name, BigDecimal value) {
        BigDecimal normalized = value.stripTrailingZeros();
        if (normalized.scale() <= 0) {
            BigInteger integer = normalized.toBigInteger();
            if (integer.bitLength() < 31) {
                return new IntTag(name, integer.intValue());
            }
            if (integer.bitLength() < 63) {
                return new LongTag(name, integer.longValue());
            }
        }
        return new DoubleTag(name, value.doubleValue());
    }
}
