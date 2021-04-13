#### ToDo List
0. Perform code analysis and fix issues (especially duplicates)
0. An embedded message couldn't be switched using emoji after certain time
0. Migrate to database, instead of .json files (json handling becomes complex)
0. Add commands
    1. `slots` - Spin the slots and win the jackpot! ...or lose everything. [Example](https://www.javacodegeeks.com/2014/08/programming-a-simple-slot-machine-game-using-java.html) - implement with memory for credits, etc
    1. `whois [user mention]` - Get user information
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
    1. Create history of replies and do not use same reply for period of time
    1. Create a dictionary of forbidden words (to avoid replies to "yes" "okay" "etc")
    1. Look for random matched word from string. As for now, reply is made to the first matched word. It would be nice to reply to a random word from list of matched words.
    1. Create a dictionary of bot's names in order to create reply to @bot, Vlad, etc
    1. Limit bot's replying to specific channels or add blacklisted channels
    1. Use actual emoji, not ids (:emoji:)
    1. Ignore commands "~" in replies
0. Create dynamic dictionary:
    1. Create different dictionaries for different trigger situations
    1. Dictionary should have specific ranking that changes
    1. Value of reply is calculated according to amount of emoji reactions
    1. Mood scale for bot. The better the mood - better and kinder replies
    1. Add functions /feed bot/
    2. Add stats for bot 
