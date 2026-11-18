package cn.nukkit.network.protocol;

import cn.nukkit.item.RuntimeItemMapping;
import cn.nukkit.item.RuntimeItems;
import cn.nukkit.network.protocol.data.BedrockDataRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModernBiomeDefinitionPacketTest {

    @Test
    void encodesCurrentBiomeDefinitions() {
        BiomeDefinitionListPacket packet = new BiomeDefinitionListPacket();
        packet.encode();

        assertTrue(packet.getBuffer().length > 0, "Biome definition packet must contain an encoded payload");
        assertNotNull(BiomeDefinitionListPacket.getCachedPacket(ProtocolInfo.CURRENT_PROTOCOL));
    }

    @Test
    void encodesCurrentEntityIdentifiers() {
        AvailableEntityIdentifiersPacket packet = new AvailableEntityIdentifiersPacket();
        packet.encode();

        assertTrue(packet.getBuffer().length > 1000);
    }

    @Test
    void loadsCurrentBedrockItemComponentsAndTags() {
        BedrockDataRegistry registry = BedrockDataRegistry.getDefault();

        assertTrue(registry.getRuntimeItemCount() > 1000);
        assertTrue(registry.getItemComponentCount() > 0);
        assertTrue(registry.getItemTagCount() > 0);
        assertTrue(registry.getComponentBasedItemCount() > 0);
    }

    @Test
    void mapsRuntimeItemsBidirectionally() {
        RuntimeItemMapping mapping = RuntimeItems.getRuntimeMapping();
        int stoneRuntimeId = mapping.getNetworkIdByNamespaceId("minecraft:stone").orElse(-1);

        assertNotEquals(-1, stoneRuntimeId);
        assertEquals("minecraft:stone", mapping.getNamespacedIdByNetworkId(stoneRuntimeId));
        assertTrue(RuntimeItems.getId(mapping.getLegacyFullId(stoneRuntimeId)) > 0);
    }
}
