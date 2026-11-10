PayBack Plus (Fabric, Minecraft 1.21.11)

BUILD
1. Install JDK 21.
2. Copy the gradle wrapper (gradlew, gradlew.bat, gradle/ folder) from
   https://github.com/FabricMC/fabric-example-mod into this folder
   (or run `gradle wrapper` if you have Gradle installed).
3. Run:  ./gradlew build      (Windows: gradlew.bat build)
4. Your mod is build/libs/paybackplus-1.0.0.jar -> put it in .minecraft/mods
   together with Fabric Loader 0.16+ and Fabric API for 1.21.11.

USE
- Press K in game to open the menu.
- Toggle auto pay-back, set the multiplier, win chance, colours (hex),
  and background image (drop a PNG into .minecraft/config/paybackplus/ and type its file name).
- When chat shows "Name paid you $1.5K." the mod sends /pay Name <amount x multiplier>.
  If DonutSMP's message format differs, edit "regex" in config/paybackplus/config.json
  (group 1 = player name, group 2 = amount).

NOTE: Automated commands may break DonutSMP's rules. Check them first and use at your own risk.

EASIEST WAY TO GET THE .JAR (no installs)
1. Make a free account at github.com and create a new repository.
2. Upload ALL the files in this folder (including the hidden .github folder) to it.
3. Open the repo's "Actions" tab -> "Build mod jar" -> wait ~2 minutes.
4. Click the finished run -> download "paybackplus-jar" -> unzip -> use paybackplus-1.0.0.jar
   (ignore any file ending in -sources.jar).
