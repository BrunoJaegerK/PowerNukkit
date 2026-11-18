package cn.nukkit.network.protocol;

import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.types.inventory.creative.CreativeItemGroup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.powernukkit.tests.junit.jupiter.PowerNukkitExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(PowerNukkitExtension.class)
class ModernCreativeContentPacketTest {

    @Test
    void sendsCreativeGroupsAndGroupedItems() {
        List<CreativeItemGroup> groups = Item.getCreativeItemGroups();
        Item[] items = Item.getCreativeItems().toArray(Item.EMPTY_ARRAY);
        Map<Item, CreativeItemGroup> itemGroups = Item.getCreativeItemGroupMap();

        assertTrue(groups.size() > 0);
        assertTrue(items.length > 0);

        CreativeContentPacket packet = new CreativeContentPacket();
        packet.protocol = ProtocolInfo.CURRENT_PROTOCOL;
        packet.entries = items;
        packet.groups = groups;
        packet.itemGroups = itemGroups;
        packet.encode();

        assertTrue(packet.getBuffer().length > 10000);
    }
}
