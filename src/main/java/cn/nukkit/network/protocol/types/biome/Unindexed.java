package cn.nukkit.network.protocol.types.biome;

/** Inline implementation used when a biome value is present directly in JSON. */
public final class Unindexed<T> implements Indexable<T> {
    private final T value;

    public Unindexed(T value) {
        this.value = value;
    }

    @Override
    public T get() {
        return value;
    }
}
