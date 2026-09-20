# Getting ascension-1.0.0.jar without local Gradle/JDK setup

This environment could not build the jar itself — see
`IMPLEMENTATION_STATUS.md` for the exact reasons (no Java compiler
installed, no Gradle, and its network allowlist excludes every host the
Fabric/Minecraft build needs: Maven Central, Fabric's Maven, Mojang's
libraries server, and Gradle's distribution server all returned `403`).

The fastest real path to the actual jar, if you don't want to install
Gradle/JDK locally right now, is GitHub Actions — its hosted runners have
normal internet access and can run the real build unmodified.

## Steps

1. Push this project to a GitHub repository (the `.github/workflows/build.yml`
   file is already included).
2. On GitHub, open the **Actions** tab for that repo.
3. If it didn't already run automatically on push, click **Build Ascension**
   → **Run workflow**.
4. Wait for the run to finish (a few minutes — it downloads Minecraft,
   Fabric Loader, Fabric API, and compiles).
5. Open the finished run, scroll to **Artifacts**, and download
   **ascension-jar**. Unzip it — inside is
   `build/libs/ascension-1.0.0.jar`, the real, Loom-remapped, installable
   mod jar.
6. Copy that one file into `%APPDATA%\.minecraft\mods\`.

If the run fails, the workflow uploads a `build-failure-logs` artifact
with the full Gradle output — that will show you the actual compile
error, the same as running `.\gradlew.bat build` locally would.

## Building locally instead

If you'd rather build on your own Windows machine directly:

```
cd ascension
gradle wrapper --gradle-version 8.12
.\gradlew.bat build
```

Output: `build\libs\ascension-1.0.0.jar`

This requires: JDK 21 installed, and normal internet access (no proxy/
firewall blocking `maven.fabricmc.net`, `libraries.minecraft.net`,
`repo.maven.apache.org`, or `services.gradle.org`).
