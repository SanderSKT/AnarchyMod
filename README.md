# AnarchyMod 1.5.0 — unofficial 9b9t local patch

Unofficial fork of the MIT-licensed AnarchyMod source archive provided by the repository owner. Not affiliated with 6b6t or 9b9t.

## Changes

- Adds the base domains and subdomains of `9b9t.com`, `9b9t.org`, and `2b2t.com` to the local blocked-server exception list.
- Preserves the 11 original built-in server patterns.
- Removes remote domain-list fetching, join analytics, and automatic insertion of 6b6t into the Minecraft server list.
- Keeps only the blocked-server-check Mixin.

## Build

The source is for Fabric. Build with Gradle and a compatible JDK (JDK 25+ for the full multi-version build):

```powershell
./gradlew.bat chiseledBuild
```

Look under `versions/<minecraft-version>/build/libs/` for outputs. Remove the original AnarchyMod JAR before installing this fork, since the Fabric mod ID is unchanged. The original Gradle wrapper binary is not included in this repository because its provenance was not verified; regenerate it using a trusted Gradle installation or use `gradle chiseledBuild` directly.

**Not tested in-game.** This patch only affects the client-side server blacklist. A `java.net.SocketException: Connection reset` can have unrelated network or server-side causes.

Upstream source: https://6b6t.kianbrose.com/AnarchyMod/

License: MIT, see [LICENSE](LICENSE).
