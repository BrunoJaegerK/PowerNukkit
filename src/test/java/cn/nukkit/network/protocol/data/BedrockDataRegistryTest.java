package cn.nukkit.network.protocol.data;

import cn.nukkit.nbt.NBTIO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BedrockDataRegistryTest {

    @Test
    void loadsAndValidatesBedrock12665Metadata() throws Exception {
        BedrockDataRegistry registry = BedrockDataRegistry.loadDefault();

        assertEquals(2080, registry.getRuntimeItemCount());
        assertTrue(registry.getBlockPropertyCount() > 0);
        assertTrue(registry.getItemTagCount() > 0);
        assertEquals(382, registry.getItem("minecraft:acacia_boat").getId());
        assertEquals(-140, registry.getItem("minecraft:acacia_button").getId());
        assertEquals(75, registry.getComponentBasedItemCount());
        assertTrue(registry.getItemsForTag("minecraft:arrow").contains("minecraft:arrow"));
        assertTrue(registry.getItemDataPalette().length > 0);

        for (com.google.gson.JsonObject properties : registry.getBlockPropertiesEntries().values()) {
            assertTrue(NBTIO.writeNetwork(JsonNbtConverter.toCompoundTag(properties)).length > 0);
        }
    }
}
