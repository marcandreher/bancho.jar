> [!WARNING]  
> At the moment the server is not recommended to use in production

## Roadmap

- [x] Authentication
- [x] Leaderboards
- [x] Score Submission
    - [x] All Modes Supported
    - [x] RX & AP Support
    - [x] PP Calculation (Only Osu-Native supported rn)
- [x] Chat
    - [x] Disconnect and Join propagation
    - [x] Sending messages (global/private)
- [x] Player Handling
    - [x] Presence, Auto Disconnect
    - [x] User Stats (Also RX & AP)
    - [x] Friends
    - [ ] Silence Info
    - [x] Privileges
- [x] Bots
    - [x] Bot online Handling
    - [x] Commands
    - [x] Announce on #1
- [x] Multiplayer
- [x] Spectating
- [x] Restriction
- [x] Achievements
- [ ] Web Redirects
- [x] osu!Direct
- [ ] BSS
- [ ] IRC
- [ ] Tourney Client

### Backend

- [ ] API
- [ ] Action Notifications (Pubsub)
- [ ] Plugin Framework
- [x] Asset downloading
- [x] Configuration
    - [x] Welcome message and metadata
    - [x] Seasonal & Main Menu Icon
    - [x] .env for Secrets

### Key Directories

| Directory | Description |
|-----------|-------------|
| `commands` | Bancho command handlers |
| `handlers` | Web handlers |
| `models` | Data class files |
| `modules` | Utility classes |
| `packets` | Packet writers/handlers |
| `repos` | Database repository utilities |
| `server` | Application services |

## Acknowledgements

This project builds upon the work of several open-source projects and contributors. We'd like to thank:

- **[7mochi](https://github.com/7mochi)** for developing **[osu-native-jar](https://github.com/7mochi/osu-native-jar)** and **[osz2.jar](https://github.com/7mochi/osz2.jar)**.
- **[Lekuruu](https://github.com/Lekuruu)** for maintaining the excellent **[bancho-documentation](https://github.com/Lekuruu/bancho-documentation)**.
- **[osuAkatsuki](https://github.com/osuAkatsuki)** and the **[bancho.py](https://github.com/osuAkatsuki/bancho.py)** project, whose database schema and parts of the server logic served as a foundation for this project (heavily modified).
- **[ekgame](https://github.com/ekgame)** for creating **[bancho-api](https://github.com/ekgame/bancho-api)**, the first Java Bancho implementation, which inspired parts of this project.