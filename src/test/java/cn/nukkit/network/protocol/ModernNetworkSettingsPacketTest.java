package cn.nukkit.network.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModernNetworkSettingsPacketTest {

    @Test
    void encodesCurrentNetworkSettingsFields() {
        NetworkSettingsPacket packet = new NetworkSettingsPacket();
        packet.protocol = ProtocolInfo.CURRENT_PROTOCOL;
        packet.compressionThreshold = 512;
        packet.compressionAlgorithm = 0;
        packet.clientThrottleEnabled = true;
        packet.clientThrottleThreshold = 7;
        packet.clientThrottleScalar = 0.5f;

        packet.encode();

        // threshold + algorithm + throttle flag + threshold byte + scalar
        assertEquals(10, packet.getBuffer().length);
    }

    @Test
    void preservesLegacyNetworkSettingsLayout() {
        NetworkSettingsPacket packet = new NetworkSettingsPacket();
        packet.protocol = ProtocolInfo.v1_19_20;
        packet.compressionThreshold = 512;
        packet.compressionAlgorithm = 0;

        packet.encode();

        assertEquals(2, packet.getBuffer().length);
    }
}
