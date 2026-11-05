# AnarchyMod — unofficial AnarchyMod fork

Unofficial Fabric mod derived from the MIT-licensed AnarchyMod sources. Not affiliated with Mojang, 6b6t, or 9b9t.

## Features

- Local exceptions for `9b9t.com`, `9b9t.org`, `2b2t.com` and original built-in domains, including their subdomains.
- **Privacy:** Removed all original 6b6t telemetry and analytics, remote domain fetching, and automatic 6b6t server-list insertion. The mod operates entirely offline and does not send usage data to 6b6t.
- **Minecraft 1.21.11:** a `Blacklist` button beside the bottom multiplayer controls opens a compact Blacklist Manager.
- Add/remove custom hostnames without restarting Minecraft, optionally allowing subdomains.
- Custom entries persist in `config/anarchymod.json`. Built-in exceptions remain enabled and separate from user entries.

Example config (generated automatically when you add an address):

```json
{
  "customDomains": ["*.example.org", "play.example.net"]
}
```

`*.example.org` allows `example.org` and its subdomains; `play.example.net` matches that hostname only. The setting affects the local client-side server blocklist check, **not** the Minecraft account, DNS, network connectivity or server-side bans.

## Install

Use the GitHub Actions artifact for your Minecraft version; unzip it and place the compiled JAR in the instance's `mods` folder. Remove the original AnarchyMod or previous fork JAR first (the Fabric mod ID is `anarchymod`).

- [Minecraft 1.21.11 build](../../actions/workflows/build-1.21.11.yml)
- [Minecraft 26.2 build](../../actions/workflows/build-26.2.yml)

The Blacklist Manager UI is currently implemented only for Minecraft 1.21.11. The 26.2 build retains the offline built-in exceptions but does not expose this editor.

## Build from source

Requires installed Gradle 9.7.1 and a compatible JDK; Minecraft 1.21.11 uses JDK 21:

```powershell
gradle :1.21.11:build
```

Output: `versions/1.21.11/build/libs/`. For a multi-version build, use `gradle chiseledBuild` with a suitable JDK (25+ for recent versions). This repository does not include an unverified Gradle wrapper JAR.

## Notes

Builds are checked by GitHub Actions, but the current UI changes still need in-game verification with other UI mods. A `java.net.SocketException: Connection reset` may have unrelated network or server-side causes.

Upstream source: https://6b6t.kianbrose.com/AnarchyMod/ · License: MIT, see [LICENSE](LICENSE).
