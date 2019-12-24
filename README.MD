### Simple Discord bot
Bot is written using JDA library and common sense.
#### Credits
- [JDA](https://github.com/DV8FromTheWorld/JDA) for flexible and intuitive Discord API Java wrapper
- [Yui-bot](https://github.com/DV8FromTheWorld/Yui) for basic idea of how discord bot should work
- [JMusic](https://github.com/jagrosh/MusicBot) (_10 of November 2019_) for wonderful and simple MusicBot, that has become a core of project
- [DiscordChatExporter](https://github.com/Tyrrrz/DiscordChatExporter/releases) for discord chat backup solution
#### ToDo List
1. Add commands (partially from DYNO.GG)
   1. `itunes [song name]` - Get info on a song
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
   26. `findpicture` - looks for google picture using given search string
   27. `fbi` - get list of most wanted criminals
7. Test lyrics command
8. Add [code analyzer](https://dzone.com/articles/intellij-idea-finds-bugs)
13. Shorten command names (like `findpicture` -> `fp`) and add aliases
14. Implement wordFrequencyAnalyzer
16. Separate backup files per guild
17. Separate media files per guild
18. Separate AutoModeration per guild
19. Separate status rotation per guild
20. Extend logger to every error\warning reply
23. Perform code analysis and fix issues (especially duplicates)
24. Embedded message couldn't be switched using emoji after certain time
27. Cyclomatic complexity
29. Understand how ownership of commands works
31. Remove IOException for certain types (like rule not found)
32. Fix encoding for initial emojis writing, or remove emojis from file (runtime only). Try -Dconsole.encoding=UTF-8 -Dfile.encoding=UTF-8
33. Fix Jenkins logging symbols
34. Automatically terminate task for coh and bot https://wiki.jenkins.io/display/JENKINS/Build-timeout+Plugin (or, probably, design bot proper launch)

Reply recommendations:
3) ƒобавить возможность процентно задать распределение между двум€ режимами ответа. Ќастраиваетс€ параметром извне.
4) —оздать историю последних ответов, так, что бы ответы запоминались на указанный срок и не повтор€лись. Ќастраиваетс€ параметром извне.
5) ƒобавить запретный словарик часто встречаемых междуимений и обращений, как "так, ага, угу, хаха". Ќастраиваетс€ параметром извне.
6) —ейчас, при ответе по вхождению, он ищет линейно от начала до конца фразы и если первое слово совпало, всегда использует его. ƒобавить выбор рандомной фразы дл€ вхождени€.  Ќастраиваетс€ параметром извне.
7) —амые частые обращение к боту, нужно добавить в запретный словарик, так как они будут чаще всего использовать при общении с ним. “акие как: "влад, владик".
8) –азделить разные словари, так что бы можно было задавать отдельные правила, дл€ отдельно подключаемых словарей.