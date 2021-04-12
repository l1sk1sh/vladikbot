#### ToDo List
0. Create Base class for Daemons
0. Change usage of deprecated callback LogContainerResultCallback()
0. Create all backup using Name + IDs of channels
0. Automatic backup separation per date should be done into every separate channel. Current realization will remove channel backup, if it was removed from guild
0. Add commands
   0. `slots` - Spin the slots and win the jackpot! ...or lose everything. [Example](https://www.javacodegeeks.com/2014/08/programming-a-simple-slot-machine-game-using-java.html) - implement with memory for credits, etc
   0. `whois [user mention]` - Get user information
0. Add 'docker logs' command to log.debug if one of backups has failed
0. Separate auto reply per guild
0. Separate status rotation per guild
0. Extend logger to every error\warning reply
0. Perform code analysis and fix issues (especially duplicates)
0. An embedded message couldn't be switched using emoji after certain time
0. Cyclomatic complexity
0. Understand how ownership of commands works
0. Remove IOException for certain types (like rule not found)
0. Calculate auto backup time from last moment of backup. Not absolute time
0. Add RichPresence to game activity
0. Add news module:
    1. Gaming news
    3. Major world news
    4. Major local news
    5. Memes
    6. NSFW
    7. WHO notifications API (https://www.who.int/csr/don/en/)
    8. Word of the day
0. Deal with duplicate code
0. Extend reply core:
    0. Create word usage frequency and add replies accordingly
    0. Create history of replies and do not use same reply for period of time
    0. Create a dictionary of forbidden words (to avoid replies to "yes" "okay" "etc")
    0. Look for random matched word from string. As for now, reply is made to the first matched word. It would be nice to reply to a random word from list of matched words.
    0. Create a dictionary of bot's names in order to create reply to @bot, Vlad, etc
    0. Create different dictionaries
    0. Limit bot's replying to specific channels or add blacklisted channels
    0. Use actual emoji, not ids (:emoji:)
    0. Ignore commands "~" in replies
0. Rebuild settings file if some setting is missing from .json
0. Centralize thread running (bot.getThreadpool().execute(() ->)
0. Work with shards and move all files to the database (Vortex)
0. Migrate to database, instead of .json files (json handling becomes complex)
0. Migrate from GSON to Moshi