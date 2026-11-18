package cn.nukkit.network.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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
}
