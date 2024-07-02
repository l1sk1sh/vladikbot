#### ToDo List
1. Play with new buttons and popup windows and REDO all pagination to buttons
   1. https://github.com/ygimenez/Pagination-Utils
2. Research what exactly are new 'menus' provided by the API
3. Rewrite all pagination and menus to new format of buttons
4. Add commands
    1. `slots` - Spin the slots and win the jackpot! ...or lose everything. [Example](https://www.javacodegeeks.com/2014/08/programming-a-simple-slot-machine-game-using-java.html) - implement with memory for credits, etc
5. Extend reply core:
    1. Create word usage frequency and add replies accordingly
    2. Create history of replies and do not use same reply for a period
    3. Create a dictionary of forbidden words (to avoid replies to "yes" "okay" "etc")
    4. Look for random matched word from string. As for now, reply is made to the first matched word. It would be nice to reply to a random word from list of matched words.
    5. Create a dictionary of bot's names in order to create reply to @bot, Vlad, etc
    6. Limit bot's replying to specific channels or add blacklisted channels
    7. Use actual emoji, not ids (:emoji:)
    8. Ignore commands "~" in replies
6. Create a dynamic dictionary:
    1. Create different dictionaries for different trigger situations
    2. Dictionary should have specific ranking that changes
    3. Value of reply should be calculated according to amount of emoji reactions
    4. Mood scale for the bot. The better the mood - better and kinder replies
    5. Add functions /feed bot/
    6. Add stats for bot 
7. Update mechanism with 'dick' chances - add increasing probability for loosers
8. Implement export for text backup command
9. Add an author to the reaction item and use it for emoji statistics calculation
10. Play a bit more with context menus - it is really cool
11. Go ahead and play with https://jda.wiki/using-jda/interactions
12. Research https://github.com/lavalink-devs/Lavalink for better music
13. Add context menu with "try my commands"
14. Finish CSV export command and use CsvParsedDiscordMessage
15. Code review
    1. Code analysis
    2. Bump dependencies
    3. Inspect all "unused" suppression