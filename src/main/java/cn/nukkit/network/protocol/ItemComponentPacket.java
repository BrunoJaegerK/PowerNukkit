package cn.nukkit.network.protocol;

import cn.nukkit.api.PowerNukkitOnly;
import cn.nukkit.api.Since;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.MainLogger;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;

/**
 * @author GoodLucky777
 */
@ToString
@PowerNukkitOnly
@Since("1.4.0.0-PN")
@NoArgsConstructor(onConstructor = @__(@PowerNukkitOnly))
public class ItemComponentPacket extends DataPacket {
    @PowerNukkitOnly
    @Since("1.4.0.0-PN")
    public static final byte NETWORK_ID = ProtocolInfo.ITEM_COMPONENT_PACKET;
    
    @Since("1.4.0.0-PN")
    private Entry[] entries = Entry.EMPTY_ARRAY;
    private List<ItemDefinition> itemDefinitions;
    
    @PowerNukkitOnly
    @Since("1.4.0.0-PN")
    public void setEntries(Entry[] entries) {
        this.entries = entries == null? null : entries.length == 0? Entry.EMPTY_ARRAY : entries.clone();
    }
    
    @PowerNukkitOnly
    @Since("1.4.0.0-PN")
    public Entry[] getEntries() {
        return entries == null? null : entries.length == 0? Entry.EMPTY_ARRAY : entries.clone();
    }
    
    public void setItemDefinitions(List<ItemDefinition> definitions) {
        this.itemDefinitions = definitions == null ? Collections.<ItemDefinition>emptyList() : definitions;
    }

    private static final byte[] EMPTY_COMPOUND_TAG;

    static {
        try {
            EMPTY_COMPOUND_TAG = NBTIO.writeNetwork(new CompoundTag(""));
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static class ItemDefinition {
        private final String identifier;
        private final int runtimeId;
        private final boolean componentBased;
        private final int version;
        private final CompoundTag networkData;

        public ItemDefinition(String identifier, int runtimeId, boolean componentBased, int version, CompoundTag networkData) {
            this.identifier = identifier;
            this.runtimeId = runtimeId;
            this.componentBased = componentBased;
            this.version = version;
            this.networkData = networkData;
        }
    }

    @Override
    public byte pid() {
        return NETWORK_ID;
    }
    
    @Override
    public void decode() {
        
    }
    
    @Override
    public void encode() {
        this.reset();
        if (this.protocol >= ProtocolInfo.v1_21_60) {
            List<ItemDefinition> definitions = this.itemDefinitions == null
                    ? Collections.<ItemDefinition>emptyList() : this.itemDefinitions;
            this.putUnsignedVarInt(definitions.size());
            for (ItemDefinition definition : definitions) {
                this.putString(definition.identifier);
                this.putLShort(definition.runtimeId);
                this.putBoolean(definition.componentBased);
                this.putVarInt(definition.version);
                try {
                    if (definition.networkData == null) {
                        this.put(EMPTY_COMPOUND_TAG);
                    } else {
                        this.put(NBTIO.write(definition.networkData, ByteOrder.LITTLE_ENDIAN, true));
                    }
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to encode item component " + definition.identifier, e);
                }
            }
            return;
        }

        Entry[] legacyEntries = this.entries == null ? Entry.EMPTY_ARRAY : this.entries;
        this.putUnsignedVarInt(legacyEntries.length);
        try {
            for (Entry entry : legacyEntries) {
                this.putString(entry.getName());
                this.put(NBTIO.write(entry.getData(), ByteOrder.LITTLE_ENDIAN, true));
            }
        } catch (IOException e) {
            MainLogger.getLogger().error("Error while encoding NBT data of ItemComponentPacket", e);
        }
    }
    
    @ToString
    @PowerNukkitOnly
    @Since("1.4.0.0-PN")
    public static class Entry {
        @PowerNukkitOnly
        @Since("1.4.0.0-PN")
        public static final Entry[] EMPTY_ARRAY = new Entry[0];
        
        private final String name;
        private final CompoundTag data;
        
        @PowerNukkitOnly
        @Since("1.4.0.0-PN")
        public Entry(String name, CompoundTag data) {
            this.name = name;
            this.data = data;
        }
        
        @PowerNukkitOnly
        @Since("1.4.0.0-PN")
        public String getName() {
            return name;
        }
        
        @PowerNukkitOnly
        @Since("1.4.0.0-PN")
        public CompoundTag getData() {
            return data;
        }
    }
}
