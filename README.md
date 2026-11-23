# TOGÜ Yemekhane

An Android app that shows the weekly cafeteria menu of Tokat Gaziosmanpaşa University. Rewritten from scratch in Kotlin and Jetpack Compose; not released yet.

> **Unofficial.** This is a personal project. It is not affiliated with, endorsed by or operated by the university. The menu comes from the university's [public menu page](https://sosyaltesisler.gop.edu.tr/yemekhane_menu.aspx); the page itself is always the authority.

## How the menu data works

There is no server and no user account. A small Kotlin program runs on a schedule in GitHub Actions, reads the menu page and publishes it as static JSON. The app downloads that JSON and keeps it on the device.

## What the app does

- **Today:** the main dish, the other dishes, the vegetarian option and the calories of the day.
- **Week:** the whole week; a day opens in Today.
- **Archive:** finished weeks and a search that shows when a dish was served. Works offline once downloaded.
- **Widget:** today's menu on the home screen, following the phone's light or dark theme.
- **Settings:** a local menu reminder that stays silent on days without a menu, theme, and language (English or Turkish).

A failed download never erases the menu on the device.

## How it is built

One module with `data`, `domain`, `ui` and `platform` packages. Room is the single source the screens read from; the network only writes into it. ViewModels expose one `StateFlow` per screen. Hilt wires the parts, Navigation Compose uses typed routes, and background work is plain `JobScheduler` and `AlarmManager`. No analytics, ads or Firebase.

```sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

## Credits

Built in collaboration with AI coding agents such as Claude and Codex.

## License

Copyright © 2026 Yusuf Can Mercan. All rights reserved.

The source code is published for viewing only. Copying, modifying, redistributing or using it for commercial purposes requires written permission. See [LICENSE](LICENSE).
