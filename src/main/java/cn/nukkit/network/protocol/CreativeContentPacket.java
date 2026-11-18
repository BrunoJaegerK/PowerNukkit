package cn.nukkit.network.protocol;

import cn.nukkit.api.Since;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.types.inventory.creative.CreativeItemCategory;
import cn.nukkit.network.protocol.types.inventory.creative.CreativeItemGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.ToString;

@Since("1.3.0.0-PN")
@ToString
public class CreativeContentPacket extends DataPacket {
    public static final byte NETWORK_ID = ProtocolInfo.CREATIVE_CONTENT_PACKET;

    @Since("1.3.0.0-PN") public Item[] entries = Item.EMPTY_ARRAY;
    public List<CreativeItemGroup> groups = new ArrayList<>();
    public Map<Item, CreativeItemGroup> itemGroups = new HashMap<>();

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
            List<CreativeItemGroup> creativeGroups = this.groups == null
                    ? java.util.Collections.<CreativeItemGroup>emptyList() : this.groups;
            Map<Item, CreativeItemGroup> creativeItems = this.itemGroups == null
                    ? java.util.Collections.<Item, CreativeItemGroup>emptyMap() : this.itemGroups;
            Map<CreativeItemGroup, Integer> groupIds = new HashMap<>();
            this.putUnsignedVarInt(creativeGroups.size());
            for (CreativeItemGroup group : creativeGroups) {
                groupIds.put(group, group.getGroupId());
                this.writeGroup(group);
            }
            Item[] creativeEntries = this.entries == null ? Item.EMPTY_ARRAY : this.entries;
            this.putUnsignedVarInt(creativeEntries.length);
            for (int i = 0; i < creativeEntries.length; i++) {
                Item entry = creativeEntries[i];
                this.putUnsignedVarInt(i + 1);
                this.putSlot(entry, true);
                CreativeItemGroup group = creativeItems.get(entry);
                this.putUnsignedVarInt(group == null ? 0 : groupIds.getOrDefault(group, 0));
            }
            return;
        }

        Item[] legacyEntries = this.entries == null ? Item.EMPTY_ARRAY : this.entries;
        this.putUnsignedVarInt(legacyEntries.length);
        for (int i = 0; i < legacyEntries.length; i++) {
            this.putUnsignedVarInt(i + 1);
            this.putSlot(legacyEntries[i], this.protocol >= ProtocolInfo.v1_16_220);
        }
    }
    private void writeGroup(CreativeItemGroup group) {
        if (this.protocol >= ProtocolInfo.v1_26_40) {
            this.putByte((byte) group.getCategory().ordinal());
        } else {
            this.putLInt(group.getCategory().ordinal());
        }
        this.putString(group.getName());
        this.putSlot(group.getIcon(), true);
    }

}
