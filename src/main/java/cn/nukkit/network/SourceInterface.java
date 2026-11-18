package cn.nukkit.network;

import cn.nukkit.Player;
import cn.nukkit.api.PowerNukkitOnly;
import cn.nukkit.api.Since;
import cn.nukkit.network.protocol.DataPacket;


/**
 * @author MagicDroidX (Nukkit Project)
 */
public interface SourceInterface {

    Integer putPacket(Player player, DataPacket packet);

    Integer putPacket(Player player, DataPacket packet, boolean needACK);

    Integer putPacket(Player player, DataPacket packet, boolean needACK, boolean immediate);

    /** Sends a packet before Bedrock packet compression has been negotiated. */
    default void putPacketUncompressed(Player player, DataPacket packet) {
        putPacket(player, packet, false, true);
    }

    /** Enables the negotiated packet compression for this player's connection. */
    default void enableCompression(Player player) {
    }

    int getNetworkLatency(Player player);

    void close(Player player);

    void close(Player player, String reason);

    void setName(String name);

    boolean process();

    void shutdown();

    void emergencyShutdown();
    
    @PowerNukkitOnly
    @Since("1.5.2.0-PN")
    Integer putResourcePacket(Player player, DataPacket packet);
}
