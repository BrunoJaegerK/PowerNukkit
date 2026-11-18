package cn.nukkit.network.protocol;

/**
 * Server response sent before packet compression is enabled.
 */
public class NetworkSettingsPacket extends DataPacket {

    public static final byte NETWORK_ID = ProtocolInfo.NETWORK_SETTINGS_PACKET;

    public int compressionThreshold;
    public int compressionAlgorithm;
    public boolean clientThrottleEnabled;
    public byte clientThrottleThreshold;
    public float clientThrottleScalar;

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    @Override
    public void encode() {
        this.reset();
        this.putLShort(this.compressionThreshold);
        if (this.protocol >= ProtocolInfo.v1_19_30) {
            this.putLShort(this.compressionAlgorithm);
            this.putBoolean(this.clientThrottleEnabled);
            this.putByte(this.clientThrottleThreshold);
            this.putLFloat(this.clientThrottleScalar);
        }
    }

    @Override
    public void decode() {
        throw new UnsupportedOperationException("NetworkSettingsPacket is clientbound only");
    }
}
