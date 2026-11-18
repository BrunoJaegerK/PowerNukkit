package cn.nukkit.network.protocol;

/**
 * Initial Bedrock handshake packet requesting the server's compression settings.
 */
public class RequestNetworkSettingsPacket extends DataPacket {

    public static final byte NETWORK_ID = ProtocolInfo.REQUEST_NETWORK_SETTINGS_PACKET;

    public int protocolVersion;

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.protocolVersion = this.getInt();
    }

    @Override
    public void encode() {
        throw new UnsupportedOperationException("RequestNetworkSettingsPacket is serverbound only");
    }
}
