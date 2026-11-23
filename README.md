# TOGÜ Yemekhane

An Android app that shows the weekly cafeteria menu of Tokat Gaziosmanpaşa University. Being rewritten from scratch.

> **Unofficial.** This is a personal project. It is not affiliated with, endorsed by or operated by the university. The menu comes from the university's [public menu page](https://sosyaltesisler.gop.edu.tr/yemekhane_menu.aspx); the page itself is always the authority.

## How the menu data works

There is no server and no user account. A small Kotlin program runs on a schedule in GitHub Actions, reads the menu page and publishes it as static JSON. The app downloads that JSON and keeps it on the device.

## Credits

Built in collaboration with AI coding agents such as Claude and Codex.

## License

Copyright © 2026 Yusuf Can Mercan. All rights reserved.

The source code is published for viewing only. Copying, modifying, redistributing or using it for commercial purposes requires written permission. See [LICENSE](LICENSE).
