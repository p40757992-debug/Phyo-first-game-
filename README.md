# Phyo first game — Android starter

A lightweight, landscape Android 2D cartoon arena prototype.

## Included in this starter
- Dark purple / black menu and landscape demo arena
- Player name field
- Offline practice demo with movement, jump/down, team toggle and harmless energy-orb visual effect
- Host / Join lobby screens for a future Wi-Fi hotspot multiplayer implementation
- 8-player target concept and local room instructions
- GitHub Actions workflow to build a debug APK

## Important current limitation
This is **not yet a working multiplayer build**. Host/Join are lobby UI placeholders; live player synchronization, room discovery, authoritative game state and reconnect handling still need implementation and testing on multiple Android devices. The room code shown is a sample label, not a live session credential.

For the first LAN networking milestone, connect all devices to the same trusted mobile hotspot/Wi-Fi network, then implement a host-authoritative local server with authenticated room membership and a strict player cap of 8. Avoid exposing the game server to the public internet.

## Build APK
1. Upload the project files to a GitHub repository.
2. Open Actions and run **Build Phyo First Game APK**.
3. Download the `PhyoFirstGame-debug` artifact from the completed workflow.
4. Extract the ZIP to get `app-debug.apk`.

No paid service is required for the GitHub Actions build, subject to GitHub account limits.
