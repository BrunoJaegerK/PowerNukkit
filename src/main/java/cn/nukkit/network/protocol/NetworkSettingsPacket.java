package cn.nukkit.network.protocol;

/**
 * Server response sent before packet compression is enabled.
 */
public class NetworkSettingsPacket extends DataPacket {

    public static final byte NETWORK_ID = ProtocolInfo.NETWORK_SETTINGS_PACKET;

    public int compressionThreshold;
    public int compressionAlgorithm;

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void encode() {
        this.reset();
        this.putLShort(this.compressionThreshold);
        this.putLShort(this.compressionAlgorithm);
    }

    @Override
    public void decode() {
        throw new UnsupportedOperationException("NetworkSettingsPacket is clientbound only");
    }
}
