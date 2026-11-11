# Pay Tracker (Fabric, Minecraft 1.21.11, client-side, made for DonutSMP)

## Getting the jar (no installs needed)
1. Make a free account at github.com, create a NEW repository.
2. Upload everything in this folder (drag and drop into the repo page,
   make sure the hidden .github folder is included).
3. Open the repo's "Actions" tab -> "Build mod" -> wait ~3 min for the green check.
4. Click the run, download the "paytracker-jar" artifact, unzip it -> paytracker-1.0.0.jar.
5. Put that jar in .minecraft/mods next to Fabric API for 1.21.11.

## Or build locally
Install JDK 21 and Gradle 9.2.1, then in this folder run:  gradle build
Jar: build/libs/paytracker-1.0.0.jar  (not the -sources one)

## Use
- Press J in-game (rebind: Controls -> Pay Tracker).
- Start/Stop Timer -> shows highest payment received + username.
- Debug button: shows chat lines containing "paid" that the mod could not read.
- Config: .minecraft/config/paytracker.json  (patterns, startingBalance)
