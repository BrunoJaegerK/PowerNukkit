# Compatibility Audit

Audit date: 2026-11-18 (revalidated against Bedrock 1.26.65)

## Repository state

- PowerNukkit `master`: `240de897a88a35e12b67bcb5fac005f3493e6e7a`.
- PetteriM1Edition `release`: `65fe92b1835c68e341bccb239d65e1665c2f6f38`.
- Nukkit-MOT `master`: `427bb1b7ffe2f6d4cbbd247f95e9276c89c00fa6`.
- Work branch: `modernization/bedrock-latest`.

## Major compatibility gaps verified

### Java and build

- PowerNukkit `master` targets Java 8. The modernization branch updates the legacy GitHub Actions workflow to current checkout/setup-java actions and v4 cache/artifact actions; the branch's baseline Maven build and full test suite now pass on Java 8.
- PetteriM1Edition targets Java 17 and uses newer Netty, Log4j, NetherNet and data-channel dependencies.
- Nukkit-MOT targets Java 17 in Maven and its README instructs Java 21 or newer at runtime. Its current transport stack includes NetherNet/WebRTC as well as RakNet.
- The modernization branch now runs its baseline build on Java 8 because the existing source/annotation-processing toolchain is Java 8-era. A Java 17 trial reached compilation but failed inside the legacy Lombok/Javac integration (`JavacProcessingEnvironment.discoveredProcs` is not open). The Java 17 migration must upgrade the Lombok dependency and Delombok plugin/toolchain coherently before switching the required build runtime.

### Entity AI

- PowerNukkit's `EntityVillager.java` is a small `EntityCreature` implementation (about 1.3 KB) with health, dimensions and baby state.
- PetteriM1Edition's `EntityVillager.java` delegates to `EntityVillagerV1`; the advanced behavior is in the V1 implementation and supporting AI/POI/event classes.
- PowerNukkit's `EntityEnderman.java` is about 1 KB and lacks the PM1E behavior system; PM1E's implementation is about 19.8 KB and depends on walking-mob AI, target/path state, block pickup/place events, safe teleportation, and metadata.
- PowerNukkit's `EntityShulker.java` is under 1 KB; PM1E's implementation is about 12.5 KB and depends on movement/targeting, attachment metadata, projectiles, sounds, and relocation.
- Therefore, replacing individual entity files is unsafe. The migration must first identify and port the AI base classes, navigation/pathfinding contracts, entity tick lifecycle, metadata, and events, then port each mob with tests.

### Protocol and registries

- PowerNukkit has a protocol/packet implementation and runtime block/item mappings tied to its older Bedrock data set.
- PetteriM1Edition contains additional packet classes and resources such as newer block/item component data and palettes.
- Nukkit-MOT has a substantially newer packet/network layer, dedicated packet processors, NetherNet transport/signaling, and newer Bedrock resource data.
- Protocol IDs, packet serialization, palettes, item components, entity identifiers, recipes, creative inventory and biome definitions must be upgraded together. Mixing individual resource files from different protocol revisions is not safe.

## Immediate implementation order

1. Baseline CI now compiles and passes the existing Maven suite on Java 8.
2. Protocol constants now include the PM1E version table and 33 additional modern Bedrock packet IDs.
3. Initial transport handshake and PlayerAuthInput gameplay path are implemented and compile/test successfully.
4. Remaining protocol work: resource-pack negotiation, StartGame schema, modern chunk/subchunk packets, and real-client login/play validation.
5. Remaining data work: runtime block states, item states/components, biome definitions, entity identifiers, recipes, creative inventory and tags must be migrated as matched versioned resources.
6. Port shared entity AI infrastructure before PM1E's Enderman, Shulker, Dragon and Villager behavior.
7. Integrate natural spawning and the remaining vanilla mechanics, then run persistence and plugin-compatibility tests.

## Explicitly not marked complete

- The branch now targets Bedrock `1.26.65` / protocol `2225`, includes the uncompressed `RequestNetworkSettings` handshake, ZLIB `NetworkSettings` response and modern compression prefix, plus `PlayerAuthInputPacket` decoding. Movement, sprint/sneak, jump, glide, swim, spin attack, flight, block-break actions, and predicted boat movement are wired into PowerNukkit's existing player lifecycle. A real Bedrock client login/play test is still outstanding.
- No PM1E entity AI has been ported yet.
- No Nukkit-MOT transport or packet implementation has been transplanted yet.
- The existing Maven suite passed **11,427 tests** with zero failures/errors at commit `20d9e1619c37d67282f5f763458c627048dde058`; the new handshake-detection test is included. No real Bedrock client integration test has been run yet.
- The old `maven.yml` workflow initially failed before checkout because it used retired `actions/cache@v1` and `actions/upload-artifact@v1`; it now uses checkout/setup-java v5 and cache/upload-artifact v4. The Java 8 Maven build and artifact upload succeeded at commit `20d9e1619c37d67282f5f763458c627048dde058`.
- The first Java 17 modernization build failed during compilation due to the legacy Lombok/Javac integration, not because of a source compilation diagnostic. The modernization workflow has been moved to Java 8 temporarily to establish a valid baseline before upgrading the compiler toolchain.

### Protocol version verified from source

- PowerNukkit `master` declared protocol `662` and Minecraft network version `1.20.70`; the modernization branch now declares protocol `2225` and Minecraft version `1.26.65`.
- PetteriM1Edition and Nukkit-MOT source branches inspected previously declare Bedrock `1.26.60` / protocol enum `v1_26_60`; their wire protocol is `2225`, shared by Bedrock `1.26.65`.
- Mojang's protocol documentation records Bedrock `1.26.65` as released on 2026-11-16 with network protocol `2225`; the intervening `1.26.61`, `1.26.62`, and `1.26.65` entries keep protocol `2225` and show no packet schema changes. Therefore the migration target is now `1.26.65` / protocol `2225`, while upstream `1.26.60` remains a useful packet-schema baseline.
- This confirms the primary upgrade is not a small patch: it is a multi-generation jump from Bedrock 1.20.70 / protocol 662 to 1.26.65 / protocol 2225.
