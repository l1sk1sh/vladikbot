#### ToDo List
1. Add age restricted token to lavaplayer
2. Add another queue type https://github.com/jagrosh/MusicBot/commit/2e9dd5df499f4c794f29d6f685af8fbb84ba1c0e
3. Code review
   1. Code analysis
   2. Bump dependencies
   3. Inspect all "unused" suppression
4. Redo settings. This time do it! It is difficult to update existing file or change type of settings
5. Add commands
    1. `slots` - Spin the slots and win the jackpot! ...or lose everything. [Example](https://www.javacodegeeks.com/2014/08/programming-a-simple-slot-machine-game-using-java.html) - implement with memory for credits, etc
    2. `whois [user mention]` - Get user information
    3. `saypm` - Send a private message to the user
    4. `image?` - Generate image using AI generator with input params
    5. `kill user` - Send message that brutally kills user
6. Rework news module:
    1. Gaming news
    2. Major world news
    3. Major local news
    4. Memes
    5. NSFW
    6. WHO notifications API (https://www.who.int/csr/don/en/)
    7. Word of the day
    8. Track user activity
    9. Add panorama news
    10. Add news limit by keywords for certain news researches
7. Extend reply core:
    1. Create word usage frequency and add replies accordingly
    2. Create history of replies and do not use same reply for a period
    3. Create a dictionary of forbidden words (to avoid replies to "yes" "okay" "etc")
    4. Look for random matched word from string. As for now, reply is made to the first matched word. It would be nice to reply to a random word from list of matched words.
    5. Create a dictionary of bot's names in order to create reply to @bot, Vlad, etc
    6. Limit bot's replying to specific channels or add blacklisted channels
    7. Use actual emoji, not ids (:emoji:)
    8. Ignore commands "~" in replies
8. Create a dynamic dictionary:
    1. Create different dictionaries for different trigger situations
    2. Dictionary should have specific ranking that changes
    3. Value of reply should be calculated according to amount of emoji reactions
    4. Mood scale for the bot. The better the mood - better and kinder replies
    5. Add functions /feed bot/
    6. Add stats for bot 
9. Add custom status to the bot (with emojis)
10. Update mechanism with 'dick' chances - add increasing probability for loosers
11. Add image processing and shrinking by some 'magik' library
12. Implement export for text backup command
13. Add an author to the reaction item and use it for emoji statistics calculation
14. Play with new buttons and popup windows
15. Review Nekotina bot and all other trash
16. Research what exactly are new 'menus' provided by the API
17. Rewrite all pagination and menus to new format of buttons
18. Play a bit more with context menus - it is really cool
19. Go ahead and play with https://jda.wiki/using-jda/interactions
20. Research https://github.com/lavalink-devs/Lavalink for better music
21. Add context menu with "try my commands"
22. Finish CSV export command and use CsvParsedDiscordMessage