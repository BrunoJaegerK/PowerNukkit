# PowerNukkit Modernization — Bedrock Latest

## Objective

Modernize this repository while preserving PowerNukkit's existing behavior and plugin API wherever possible. The target is Bedrock 1.26.65 / network protocol 2225, verified against the current release documentation; support is not complete until real-client login and gameplay tests pass.

## Source baselines inspected

- PowerNukkit base: `master`, commit `240de897a88a35e12b67bcb5fac005f3493e6e7a`; Maven coordinates `org.powernukkit:powernukkit:1.6.0.1-PN-CUSTOM`; compiler source/target 8.
- NukkitPetteriM1Edition base: `release`, commit `65fe92b1835c68e341bccb239d65e1665c2f6f38`; Maven compiler source/target 17.
- Nukkit-MOT base: `master`, commit `427bb1b7ffe2f6d4cbbd247f95e9276c89c00fa6`; Maven compiler source/target 17. Its README documents Bedrock 1.26.60 support; this is a reference point, not an assumption that its protocol can be copied unchanged.

## Integration rules

1. Keep the work on `modernization/bedrock-latest`; do not rewrite `master` during the migration.
2. Preserve PowerNukkit public APIs and existing behavior unless a change is required for protocol or vanilla correctness. Prefer additive overloads, adapters, and compatibility shims.
3. Do not copy whole packages blindly. Compare class dependencies, registries, packet schemas, runtime IDs, NBT formats, and lifecycle hooks before porting.
4. Treat the PM1E fork as the source of truth for the user's requested mob-AI and vanilla behavior additions. Revalidate each behavior in code and port it with tests.
5. Use Nukkit-MOT as a reference for newer protocol/networking, Bedrock data, blocks/items, entities, world generation, and server systems. Respect its license and preserve required notices when adapting code.
6. Never advertise a protocol or feature as supported until packet negotiation, login, gameplay, persistence, and regression tests pass.


## Current implementation status (2026-11-18)

- Target: Bedrock 1.26.65, network protocol 2225.
- Implemented: initial network-settings handshake, ZLIB prefix negotiation, version-aware PlayerAuthInput decoding, movement and common player input states, block-breaking actions, predicted boat input, modern protocol version constants, and additional packet identifiers.
- Verified: Java 8 Maven build succeeded and all 11,427 tests passed at commit `20d9e1619c37d67282f5f763458c627048dde058`. The CI artifact was uploaded by the `PowerNukkit v1.X` workflow.
- Not yet verified: a real 1.26.65 client login and gameplay session. StartGame serialization and the modern block/item/biome/entity data set are still legacy and are the next critical compatibility gates.

## PM1E behavior inventory to port and verify

### Natural spawning
- Player-centered spawn search radius and consistent hostile-mob classification.
- Keep spawn classification and sleep-related hostile checks consistent.
- Explicitly audit biome, light, pack-size, dimension, structure, and per-entity spawn conditions; PM1E README does not claim all tables are fully vanilla.

### Entity AI and vanilla behavior
- Ender Dragon: target refresh, circling/strafing/charge phases, movement, collision, hitbox, damage/armor, AI-disabled synchronization.
- Enderman: Bedrock gaze checks, anger timing/persistence, safe teleports, environmental escape, Endermite targeting, supported block pickup/place.
- Shulker: attachment face/peek metadata, safe relocation, hitbox/armor/projectile behavior, bullet speed/damage/levitation, peaceful/game-mode handling.
- Villagers: POI navigation, daily work/meeting/rest, bed and workstation claims/releases, profession/skin metadata, farming, crop safety, inventory/food sharing, bread crafting, breeding, farm/trade persistence, threat avoidance, panic, rain shelter, door navigation, baby following.
- Zombie/husk villager infection: fatal-hit attribution, difficulty chances, NBT preservation, conversion location.
- Additional previously explored AI behaviors (Allay, Creeper, spiders, Warden, witches, golems, armadillos, polar bears, wolves, foxes, rabbits, hoglins, goats, bats, phantoms, ranged combat) are a separate verification queue. Do not treat them as present in PM1E solely because they were discussed.

### Plugin API
- Cancellable Enderman block-place, villager harvest, villager plant, and villager door-open events.
- Preserve existing plugin events and add compatibility tests for event dispatch/cancellation.

## Migration phases

1. **Baseline and build:** capture branch heads, build configuration, Java/toolchain requirements, test inventory, protocol constants, packet registry, and generated resources.
2. **Protocol and transport:** update RakNet/Netty/Bedrock protocol negotiation and packet codecs as a coherent unit; add login and packet round-trip tests.
3. **Registries and data:** update block/item/entity identifiers, runtime palettes, block states, creative contents, recipes, tags, metadata, sounds, and resource data.
4. **Gameplay and worlds:** port world generation, dimensions, block mechanics, items, effects, recipes, commands, and vanilla systems.
5. **Entities and AI:** port PM1E behavior with focused unit/integration tests; use MOT only for verified gaps.
6. **Compatibility:** preserve PowerNukkit plugin API, permissions, events, configuration, storage, and migration behavior.
7. **Validation and release:** run Maven tests, clean package, protocol/login tests, world persistence tests, plugin smoke tests, and publish a changelog plus reproducible CI artifacts.

## Acceptance criteria

- A clean build from this branch using the documented JDK; the current Java 8 baseline has passed, and Java 17 migration remains a separate toolchain task.
- Existing PowerNukkit regression tests pass or have explicitly documented intentional changes.
- New tests cover each imported gameplay behavior and relevant edge cases.
- Supported Bedrock client versions are stated from tested protocol constants, not inferred from upstream README text.
- No unverified feature is marked complete.
