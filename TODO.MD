#### ToDo List
0. Add commands
   0. `slots` - Spin the slots and win the jackpot! ...or lose everything. [Example](https://www.javacodegeeks.com/2014/08/programming-a-simple-slot-machine-game-using-java.html) - implement with memory for credits, etc
   0. `whois [user mention]` - Get user information
0. Perform code analysis and fix issues (especially duplicates)
0. An embedded message couldn't be switched using emoji after certain time
0. Rework news module:
    1. Gaming news
    3. Major world news
    4. Major local news
    5. Memes
    6. NSFW
    7. WHO notifications API (https://www.who.int/csr/don/en/)
    8. Word of the day
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
0. Migrate to database, instead of .json files (json handling becomes complex)