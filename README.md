
<h1 align="center">
  Curios Creative Dimension Patch
</h1>
<p align="center">
  A small patch to the <a href="https://www.curseforge.com/minecraft/mc-mods/curios">Curios API</a> to add compatibility with <a href="https://modrinth.com/datapack/creative-dimension">Creative Dimension</a> datapack!
</p>
<br />

> [!WARNING]
> This mod does not directly modify any function inside the Creative Dimension datapack. You will need to do this yourself (see: [Modifying Datapack](#-modifying-datapack))

# ⚙️ Installation

### 🔨 Building

This project uses the Gradle build system from ModDevGradle for NeoForge.

e.g. `./gradlew build`

A full list of build tasks can be found in [build.gradle](build.gradle).

### 🧪 Modifying Datapack

> [!CAUTION]
> Always stop your server (_or minecraft instance_) before modifying any files.


> [!NOTE]
> Updated for version V.3.1.7 of Creative Dimension. May not accurately reflect newer versions.


1. Navigate to where the world where your datapack is installed.
   - for a multiplayer server this is usually in `world/datapacks` 
   - for a singleplayer world this will be under `.minecraft/saves/[YOUR WORLD]/datapacks` 
2. Unzip `PK_Creative_Dimension_V.3.1.2_MC_1.21.zip` into its own folder
3. Modify `PK_Creative_Dimension_V.3.1.2_MC_1.21/data/pk_cr_di/function/entities/player/creative_dimension/join.mcfunction` to be
```diff
#> pk_cr_di:entities/player/creative_dimension/join
#
# Triggers when a player joins the creative dimension in any way (from a changed_dimension trigger)
#
# @context a player at @s

# Mark player
tag @s add pk.cr_di.in_creative_dimension

+ # Save curios data
+ curios save @s

# Swap data
function pk_cr_di:entities/player/data/save/all {subpath:"regular_dimension"}
function pk_cr_di:entities/player/data/restore/all {subpath:"creative_dimension"}

# Clear potential remaning effects
effect clear @s slow_falling

# Force gamemode to creative
gamemode creative @s
```
3. Modify `PK_Creative_Dimension_V.3.1.2_MC_1.21/data/pk_cr_di/function/entities/player/creative_dimension/leave.mcfunction` to include
```diff
#> pk_cr_di:entities/player/creative_dimension/leave
#
# Triggers when a player leaves the creative dimension in any way (from a changed_dimension trigger)
#
# @context a player at @s

# Mark player
tag @s remove pk.cr_di.in_creative_dimension

# Clear effect
effect clear @s slow_falling

# Reset beds use warning
advancement revoke @s only pk_cr_di:events/inventory_changed/get_bed_in_creative_dimension

+ # Load curios data
+ curios load @s

# Swap data
function pk_cr_di:entities/player/data/save/all {subpath:"creative_dimension"}
function pk_cr_di:entities/player/data/restore/all {subpath:"regular_dimension"}
```
4. Delete the zipped archive and keep the modified folder. You may re-zip the folder or leave it unzipped.

### 🥳 Final Steps
This project requires Curios API (of course!) make sure to install it! The required version is `9.5.1+1.21.1`.

You're now good to go!

# 🧐 How it works
This mod listens for NeoForge's command event registry and hooks into Curios API's node attaching two new sub-nodes, `save` and `load`. As the names imply, using two methods added to Curios API in [$164](https://github.com/TheIllusiveC4/Curios/issues/164), the Player's curios inventory is saved (loaded) to (from) persistent data. The `save` command also clears a player's curios inventory when run.

These commands are accessible from the datapack and are run directly. Simple as, simple is.