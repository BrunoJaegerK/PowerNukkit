package cn.nukkit.network.protocol;

import cn.nukkit.level.GameRules;
import cn.nukkit.network.protocol.data.BedrockDataRegistry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ModernStartGamePacketTest {

    @Test
    void encodesModernStartGameWithBedrockBlockProperties() {
        StartGamePacket packet = new StartGamePacket();
        packet.worldName = "Protocol test";
        packet.gameRules = GameRules.getDefault();

        packet.encode();

        assertTrue(packet.getBuffer().length > 10000);
    }

    @Test
    void encodesRuntimeItemDefinitionsForModernClients() {
        BedrockDataRegistry registry = BedrockDataRegistry.getDefault();
        List<ItemComponentPacket.ItemDefinition> definitions = new ArrayList<>();
        for (BedrockDataRegistry.RuntimeItemState item : registry.getRuntimeItems()) {
            definitions.add(new ItemComponentPacket.ItemDefinition(
                    item.getName(), item.getId(), item.isComponentBased(), item.getVersion(), null));
        }

        ItemComponentPacket packet = new ItemComponentPacket();
        packet.setItemDefinitions(definitions);
        packet.encode();

        assertTrue(definitions.size() > 2000);
        assertTrue(packet.getBuffer().length > 10000);
    }
}
