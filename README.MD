### Simple Discord bot
Bot is written using JDA library and common sense.
#### Credits
- [JDA](https://github.com/DV8FromTheWorld/JDA) for flexible and intuitive Discord API Java wrapper
- [Yui-bot](https://github.com/DV8FromTheWorld/Yui) for basic idea of how discord bot should work
- [JMusic](https://github.com/jagrosh/MusicBot) (_30 of July 2019_) for wonderful and simple MusicBot, that has become a core of project
- [DiscordChatExporter](https://github.com/Tyrrrz/DiscordChatExporter/releases) for discord chat backup solution
#### ToDo List
1. Add commands (partially from DYNO.GG)
   1. `itunes [song name]` - Get info on a song
   2. `quote` - Get a random quote
   3. `joke` - Get a joke
   4. `dogfact` - Get random Dog Facts with this command
   5. `dog` - Find some cute dog pictures
   6. `cat` - Find some cute cat pictures
   7. `roll [size] [number of dice]` - Roll the dice (support optional size: d4, d8, d10, d12, d20, d00)
   8. `slots` - Spin the slots and win the jackpot! ...or lose everything
   9. `define` - Define a word
   10. `rps [choice]` - Rock Paper Scissors with the bot
   11. `country [3 letter country code]` - Check the stats on a country
   12. `weather [location]` - Get the forecast information for a location
   13. `pug` - Find some cute pug pictures
   14. `steamstatus` - Check the status of Steam
   15. `flip` - Flip a coin
   16. `space` - Get info about the ISS
   17. `catfact` - Get random Cat Facts with this command
   18. `dadjoke` - Get a random Dad joke
   19. `remindme [reminder]` - Set a reminder
   20. `google` - Get search results from google
   21. `whois [user mention]` - Get user information
   22. `whoisip [ip]` - Get info from WHOISIP service
   23. `distance  [coords] [coords]` - Get the distance between two coordinates
   24. `permissions` - Check if bot has all necessary permissions (also, log that in the logger in case bot can't write into the discord)
5. Create auto-scheduler with notification to backup chats and media
6. Command to backup all chats that bot is currently in
7. Test lyrics command
8. Add code analyzer
9. Package the bot
10. Deploy the bot
13. Shorten command names (like `findpicture` -> `fp`)
14. Implement wordFrequencyAnalyzer