### Simple Discord bot
Bot is written using JDA library and common sense.
#### Credits
- [JDA](https://github.com/DV8FromTheWorld/JDA) for flexible and intuitive Discord API Java wrapper
- [Yui-bot](https://github.com/DV8FromTheWorld/Yui) for basic idea of how discord bot should work
- [JMusic](https://github.com/jagrosh/MusicBot) (_Aug 31, 2021_) for wonderful and simple MusicBot, that has become a core of project
- [DiscordChatExporter](https://github.com/Tyrrrz/DiscordChatExporter/releases) for discord chat backup solution
- [Dyno Bot](https://dyno.gg/bot) for great ideas on how to improve and extend bot
- [JDA-Chewtils](https://github.com/Chew/JDA-Chewtils) for replacing original JDA-Utilities
#### Recommended launch options
- `-Dconsole.encoding=UTF-8` 
- `-Dfile.encoding=UTF-8` 
- `-Djenkins` (_for Jenkins specific logging layout_)
- `-Droot_log_level=INFO`
#### Docker
To ensure smooth Docker performance, make sure to expose `2375 port without TSL` in Docker Settings.

_Note_: Bot itself could be Dockerized, and it already has configurations for it. 
However, bot uses docker container `tyrrrz/discordchatexporter`, and full dockerization either requires socket connection between two
containers or integration of `tyrrrz/discordchatexporter` right into the bot. As for now, there is no dire need for Docker.

#### Development setup
Install `Lombok` plugin and enable `annotations processing` in the settings of your IDE

#### Database access
To edit database externally (for example in DataGrip) use TCP connection: `jdbc:h2:tcp://192.168.208.1:9092/nio:./app/database`

Also, it is possible to edit database in when bot is offline. For that access memory database directly (for example `jdbc:h2:~/app/database`)