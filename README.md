<div align="center">

# Totem Pulse

**Know how many totems remain after a pop.**

`FABRIC` · `MINECRAFT 1.21.11` · `CLIENT-SIDE`

[Download](https://github.com/Vaileks/TotemPulse/releases/latest) · [Report an issue](https://github.com/Vaileks/TotemPulse/issues)

</div>

---

Totem Pulse is a client-side Fabric mod by **Vaileks**.

## Features

- Uses the server event for your own totem activation.
- Shows your remaining totem count for five seconds.
- Adjustable low-stock threshold and optional sound.

## Installation

1. Use **Minecraft Java 1.21.11**, **Fabric Loader 0.19.2+** and **Java 21+**.
2. Download `totem-pulse-1.0.0+1.21.11.jar` from [Releases](https://github.com/Vaileks/TotemPulse/releases/latest).
3. Put the JAR in your client `mods` folder and start the game.

No server installation or Fabric API is required. The shared runtime is already bundled in the JAR, so this mod can be installed on its own or alongside other mods in the series.

## Configuration

Press **F8** or use the mod's settings button in **Mod Menu**. Mod Menu is optional.

Adjust the available colors, thresholds and feature controls in the settings screen. HUD panels support scale, shadow, background, item names or icons, and mouse placement where applicable. Save changes with **Done / Готово**. The action key is unbound by default to avoid conflicts; assign one in **Functions / Функции**.

Settings are stored in `config/totem_pulse.json`.

## Русский

Totem Pulse после срабатывания вашего тотема на пять секунд показывает оставшийся запас. Поддерживает порог малого количества и звук.

Установка: Fabric **1.21.11**, Java **21+**. Поместите JAR в клиентскую папку `mods`. Настройки — **F8** или Mod Menu. Fabric API не требуется.

## Build

With JDK 21–25 and Python 3 installed:

```sh
./gradlew packageMods
```

Output: `dist/mods/totem-pulse-1.0.0+1.21.11.jar`.

## License

[MIT](LICENSE).
