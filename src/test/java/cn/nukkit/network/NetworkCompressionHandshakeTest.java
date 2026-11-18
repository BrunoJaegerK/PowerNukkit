package cn.nukkit.network;

import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.utils.BinaryStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetworkCompressionHandshakeTest {

    @Test
    void detectsUncompressedRequestNetworkSettingsBatch() {
        BinaryStream packet = new BinaryStream();
        packet.putUnsignedVarInt(ProtocolInfo.REQUEST_NETWORK_SETTINGS_PACKET & 0xff);
        packet.putLInt(ProtocolInfo.CURRENT_PROTOCOL);

        BinaryStream batch = new BinaryStream();
        batch.putUnsignedVarInt(packet.getBuffer().length);
        batch.put(packet.getBuffer());

        assertTrue(Network.isRequestNetworkSettingsBatch(batch.getBuffer()));
    }

    @Test
    void doesNotTreatCompressedPayloadAsHandshake() {
        assertFalse(Network.isRequestNetworkSettingsBatch(new byte[]{0x78, (byte) 0x9c, 0x00, 0x01}));
    }
}
