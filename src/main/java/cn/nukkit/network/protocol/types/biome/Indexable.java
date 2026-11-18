package cn.nukkit.network.protocol.types.biome;

/** A value that can be resolved from an indexed or inline Bedrock biome palette. */
public interface Indexable<T> {
    T get();
}
