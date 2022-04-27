#### ToDo List
0. Add commands
    1. `slots` - Spin the slots and win the jackpot! ...or lose everything. [Example](https://www.javacodegeeks.com/2014/08/programming-a-simple-slot-machine-game-using-java.html) - implement with memory for credits, etc
    1. `whois [user mention]` - Get user information
    1. `saypm` - Send a private message to the user
0. Rework news module:
    1. Gaming news
    1. Major world news
    1. Major local news
    1. Memes
    1. NSFW
    1. WHO notifications API (https://www.who.int/csr/don/en/)
    1. Word of the day
0. Extend reply core:
    1. Create word usage frequency and add replies accordingly
    1. Create history of replies and do not use same reply for a period
    1. Create a dictionary of forbidden words (to avoid replies to "yes" "okay" "etc")
    1. Look for random matched word from string. As for now, reply is made to the first matched word. It would be nice to reply to a random word from list of matched words.
    1. Create a dictionary of bot's names in order to create reply to @bot, Vlad, etc
    1. Limit bot's replying to specific channels or add blacklisted channels
    1. Use actual emoji, not ids (:emoji:)
    1. Ignore commands "~" in replies
0. Create a dynamic dictionary:
    1. Create different dictionaries for different trigger situations
    1. Dictionary should have specific ranking that changes
    1. Value of reply should be calculated according to amount of emoji reactions
    1. Mood scale for the bot. The better the mood - better and kinder replies
    1. Add functions /feed bot/
    1. Add stats for bot 
0. Add custom status to the bot (with emojis)
0. Fix MCServer timeonline command output
0. Add image processing and shrinking by some 'magik' library
0. Implement export for text backup command
0. Use https://github.com/Chew/JDA-Chewtils/tree/master/menu for menus
0. Add an author to the reaction item and use it for emote statistics calculation
0. Play with new buttons and popup windows
0. Redo settings. This time do it! It is difficult to update existing file or change type of setting