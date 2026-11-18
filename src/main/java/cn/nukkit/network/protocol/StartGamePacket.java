package cn.nukkit.network.protocol;

import cn.nukkit.api.Since;
import cn.nukkit.item.RuntimeItems;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.data.BedrockDataRegistry;
import cn.nukkit.network.protocol.data.JsonNbtConverter;
import com.google.gson.JsonObject;
import cn.nukkit.level.GameRules;
import lombok.ToString;

import java.io.IOException;
import java.util.Map;
import lombok.extern.log4j.Log4j2;

/**
 * @since 15-10-13
 */
@Log4j2
@ToString
public class StartGamePacket extends DataPacket {

    public static final byte NETWORK_ID = ProtocolInfo.START_GAME_PACKET;

    public static final int GAME_PUBLISH_SETTING_NO_MULTI_PLAY = 0;
    public static final int GAME_PUBLISH_SETTING_INVITE_ONLY = 1;
    public static final int GAME_PUBLISH_SETTING_FRIENDS_ONLY = 2;
    public static final int GAME_PUBLISH_SETTING_FRIENDS_OF_FRIENDS = 3;
    public static final int GAME_PUBLISH_SETTING_PUBLIC = 4;

    @Override
    public byte pid() {
        return NETWORK_ID;
    }

    public long entityUniqueId;
    public long entityRuntimeId;
    public int playerGamemode;
    public float x;
    public float y;
    public float z;
    public float yaw;
    public float pitch;
    public int seed;
    public byte dimension;
    public int generator = 1;
    public int worldGamemode;
    public int difficulty;
    public int spawnX;
    public int spawnY;
    public int spawnZ;
    public boolean hasAchievementsDisabled = true;
    public int dayCycleStopTime = -1; //-1 = not stopped, any positive value = stopped at that time
    public int eduEditionOffer = 0;
    public boolean hasEduFeaturesEnabled = false;
    public float rainLevel;
    public float lightningLevel;
    public boolean hasConfirmedPlatformLockedContent = false;
    public boolean multiplayerGame = true;
    public boolean broadcastToLAN = true;
    public int xblBroadcastIntent = GAME_PUBLISH_SETTING_PUBLIC;
    public int platformBroadcastIntent = GAME_PUBLISH_SETTING_PUBLIC;
    public boolean commandsEnabled;
    public boolean isTexturePacksRequired = false;
    public GameRules gameRules;
    public boolean bonusChest = false;
    public boolean hasStartWithMapEnabled = false;
    @Since("1.3.0.0-PN") public boolean trustingPlayers;
    public int permissionLevel = 1;
    public int serverChunkTickRange = 4;
    public boolean hasLockedBehaviorPack = false;
    public boolean hasLockedResourcePack = false;
    public boolean isFromLockedWorldTemplate = false;
    public boolean isUsingMsaGamertagsOnly = false;
    public boolean isFromWorldTemplate = false;
    public boolean isWorldTemplateOptionLocked = false;
    public boolean isOnlySpawningV1Villagers = false;
    public String vanillaVersion = ProtocolInfo.MINECRAFT_VERSION_NETWORK;
    public String levelId = ""; //base64 string, usually the same as world folder name in vanilla
    public String worldName;
    public String premiumWorldTemplateId = "";
    public boolean isTrial = false;
    public boolean isMovementServerAuthoritative;
    public boolean hardcore;
    public int editorWorldType;
    public boolean isDisablingPersonas;
    public boolean isDisablingCustomSkins;
    public boolean emoteChatMuted;
    public byte chatRestrictionLevel;
    public boolean disablePlayerInteractions;
    public boolean clientSideGenerationEnabled;
    public boolean blockIdsAreHashed;
    public boolean forceNoServerAuthBlockBreaking;
    @Since("1.3.0.0-PN") public boolean isInventoryServerAuthoritative;
    public long currentTick;

    public int enchantmentSeed;

    public String multiplayerCorrelationId = "";

    @Override
    public void decode() {

    }

    @Override
    public void encode() {
        this.reset();
        this.putEntityUniqueId(this.entityUniqueId);
        this.putEntityRuntimeId(this.entityRuntimeId);
        this.putVarInt(this.playerGamemode);
        this.putVector3f(this.x, this.y, this.z);
        this.putLFloat(this.yaw);
        this.putLFloat(this.pitch);

        this.putLLong(this.seed);
        this.putLShort(0x00); // SpawnBiomeType - Default
        this.putString("plains"); // UserDefinedBiomeName
        this.putVarInt(this.dimension);
        this.putVarInt(this.generator);
        this.putVarInt(this.worldGamemode);
        if (this.protocol >= ProtocolInfo.v1_20_80) {
            this.putBoolean(this.hardcore);
        }
        this.putVarInt(this.difficulty);
        this.putBlockVector3(this.spawnX, this.spawnY, this.spawnZ);
        this.putBoolean(this.hasAchievementsDisabled);

        if (this.protocol >= ProtocolInfo.v1_19_10) {
            if (this.protocol >= ProtocolInfo.v1_21_100) {
                this.putVarInt(this.editorWorldType);
            } else {
                this.putBoolean(this.editorWorldType != 0);
            }
            if (this.protocol >= ProtocolInfo.v1_19_80) {
                this.putBoolean(false); // isCreatedInEditor
                this.putBoolean(false); // isExportedFromEditor
            }
        }
        if (this.protocol >= ProtocolInfo.v1_26_60_29) {
            this.putByte((byte) 0); // EditorLevelMigrationVersion
        }

        this.putVarInt(this.dayCycleStopTime);
        if (this.protocol >= ProtocolInfo.v1_26_40) {
            this.putUnsignedVarInt(this.eduEditionOffer);
        } else {
            this.putVarInt(this.eduEditionOffer);
        }
        this.putBoolean(this.hasEduFeaturesEnabled);
        this.putString(""); // Education Edition Product ID
        this.putLFloat(this.rainLevel);
        this.putLFloat(this.lightningLevel);
        this.putBoolean(this.hasConfirmedPlatformLockedContent);
        this.putBoolean(this.multiplayerGame);
        this.putBoolean(this.broadcastToLAN);
        this.putVarInt(this.xblBroadcastIntent);
        this.putVarInt(this.platformBroadcastIntent);
        this.putBoolean(this.commandsEnabled);
        this.putBoolean(this.isTexturePacksRequired);
        this.putGameRules(this.gameRules);
        this.putUnsignedVarInt(0); // Experiments
        this.putBoolean(false); // Were experiments previously toggled
        this.putBoolean(this.bonusChest);
        this.putBoolean(this.hasStartWithMapEnabled);
        if (this.protocol >= ProtocolInfo.v1_26_40) {
            this.putByte((byte) this.permissionLevel);
        } else {
            this.putVarInt(this.permissionLevel);
        }

        this.putLInt(this.serverChunkTickRange);
        this.putBoolean(this.hasLockedBehaviorPack);
        this.putBoolean(this.hasLockedResourcePack);
        this.putBoolean(this.isFromLockedWorldTemplate);
        this.putBoolean(this.isUsingMsaGamertagsOnly);
        this.putBoolean(this.isFromWorldTemplate);
        this.putBoolean(this.isWorldTemplateOptionLocked);
        this.putBoolean(this.isOnlySpawningV1Villagers);
        if (this.protocol >= ProtocolInfo.v1_19_20) {
            this.putBoolean(this.isDisablingPersonas);
            this.putBoolean(this.isDisablingCustomSkins);
            if (this.protocol >= ProtocolInfo.v1_19_60) {
                this.putBoolean(this.emoteChatMuted);
            }
        }
        this.putString(this.vanillaVersion);
        this.putLInt(16); // Limited world width
        this.putLInt(16); // Limited world height
        this.putBoolean(false); // Nether type
        this.putString(""); // EduSharedUriResource buttonName
        this.putString(""); // EduSharedUriResource linkUri
        this.putBoolean(false); // Experimental Gameplay
        if (this.protocol >= ProtocolInfo.v1_19_20) {
            this.putByte(this.chatRestrictionLevel);
            this.putBoolean(this.disablePlayerInteractions);
        }
        if (this.protocol >= ProtocolInfo.v1_26_30) {
            this.putVarInt(0); // ServerEditorConnectionPolicy
            this.putBoolean(false); // AllowAnonymousBlockDropsInEditorWorlds
        }

        this.putString(this.levelId);
        this.putString(this.worldName);
        this.putString(this.premiumWorldTemplateId);
        this.putBoolean(this.isTrial);

        if (this.protocol >= ProtocolInfo.v1_16_100) {
            if (this.protocol >= ProtocolInfo.v1_16_210) {
                if (this.protocol < ProtocolInfo.v1_21_90) {
                    this.putVarInt(this.isMovementServerAuthoritative ? 1 : 0);
                }
                this.putVarInt(0); // RewindHistorySize
                this.putBoolean(!this.forceNoServerAuthBlockBreaking && this.protocol >= ProtocolInfo.v1_17_0);
            } else {
                this.putVarInt(this.isMovementServerAuthoritative ? 1 : 0);
            }
        } else {
            this.putBoolean(this.isMovementServerAuthoritative);
        }

        this.putLLong(this.currentTick);
        this.putVarInt(this.enchantmentSeed);

        if (this.protocol >= ProtocolInfo.v1_26_50_27) {
            Map<String, JsonObject> blockProperties = BedrockDataRegistry.getDefault().getBlockPropertiesEntries();
            this.putUnsignedVarInt(blockProperties.size());
            for (Map.Entry<String, JsonObject> entry : blockProperties.entrySet()) {
                this.putString(entry.getKey());
                try {
                    this.put(NBTIO.writeNetwork(JsonNbtConverter.toCompoundTag(entry.getValue())));
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to encode Bedrock block properties for " + entry.getKey(), e);
                }
            }
        } else if (this.protocol >= ProtocolInfo.v1_16_100) {
            this.putUnsignedVarInt(0); // No custom blocks
        } else {
            this.put(GlobalBlockPalette.BLOCK_PALETTE);
        }

        if (this.protocol < ProtocolInfo.v1_21_60) {
            this.put(RuntimeItems.getRuntimeMapping().getItemDataPalette());
        }
        this.putString(this.multiplayerCorrelationId);

        if (this.protocol >= 407) {
            this.putBoolean(this.isInventoryServerAuthoritative);
            if (this.protocol >= ProtocolInfo.v1_16_230_50) {
                this.putString(""); // Server Engine
                if (this.protocol >= ProtocolInfo.v1_18_0_20) {
                    if (this.protocol < ProtocolInfo.v1_19_0_29) {
                        this.putLLong(0L); // BlockRegistryChecksum
                    } else {
                        try {
                            this.put(NBTIO.writeNetwork(new CompoundTag("")));
                        } catch (IOException e) {
                            throw new IllegalStateException("Unable to encode empty player property data", e);
                        }
                        this.putLLong(0L); // BlockRegistryChecksum
                        this.put(new byte[16]); // WorldTemplateId
                        if (this.protocol >= ProtocolInfo.v1_19_20) {
                            this.putBoolean(this.clientSideGenerationEnabled);
                            if (this.protocol >= ProtocolInfo.v1_19_80) {
                                this.putBoolean(this.blockIdsAreHashed);
                                if (this.protocol >= ProtocolInfo.v1_20_0_23) {
                                    if (this.protocol >= ProtocolInfo.v1_21_100 && this.protocol < ProtocolInfo.v1_21_130_28) {
                                        this.putBoolean(false); // mTickDeathSystemsEnabled
                                    }
                                    this.putBoolean(true); // NetworkPermissions.isServerAuthSounds
                                    if (this.protocol >= ProtocolInfo.v1_26_30 && this.protocol < ProtocolInfo.v1_26_40) {
                                        this.putBoolean(false); // LoggingChat
                                    }
                                    if (this.protocol >= ProtocolInfo.v1_26_0) {
                                        this.putBoolean(false); // No server join info
                                        this.putString(""); // Server telemetry data
                                        this.putString("");
                                        this.putString("");
                                        this.putString("");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
