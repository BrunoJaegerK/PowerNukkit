# Upstream attribution and data provenance

This modernization branch combines the original PowerNukkit codebase with protocol and gameplay references from the following projects:

- **PowerNukkit** — original server implementation; project license: GPL-3.0.
- **NukkitPetteriM1Edition** — Bedrock packet, gameplay and entity behavior reference; upstream project license: GPL-3.0. Adapted source files should retain their original copyright and license notices where present.
- **Nukkit-MOT** — protocol and versioned-data reference; upstream project license: LGPL-3.0. The following metadata files were copied from its `master` branch into `src/main/resources/bedrock/1.26.65/`:
  - `runtime_item_states_2225.json`
  - `block_properties_2193.json`
  - `item_tags_2225.json`

The Nukkit-MOT license text is preserved at `LICENSES/LGPL-3.0.txt`. The source project is available at https://github.com/MemoriesOfTime/Nukkit-MOT.

These files describe Minecraft Bedrock protocol/runtime data. The upstream repository license does not itself grant rights to Minecraft trademarks or proprietary game assets; redistribution and use must also comply with the applicable Minecraft/Mojang terms.
