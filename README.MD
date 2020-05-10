### Simple Discord bot
Bot is written using JDA library and common sense.
#### Credits
- [JDA](https://github.com/DV8FromTheWorld/JDA) for flexible and intuitive Discord API Java wrapper
- [Yui-bot](https://github.com/DV8FromTheWorld/Yui) for basic idea of how discord bot should work
- [JMusic](https://github.com/jagrosh/MusicBot) (_Apr 9, 2020_) for wonderful and simple MusicBot, that has become a core of project
- [DiscordChatExporter](https://github.com/Tyrrrz/DiscordChatExporter/releases) for discord chat backup solution
- [Dyno Bot](https://dyno.gg/bot) for great ideas on how to improve and extend bot
#### Recommended launch options
- -Dconsole.encoding=UTF-8 
- -Dfile.encoding=UTF-8 
- -Djenkins
- -Droot_log_level=INFO
#### Docker
To ensure smooth Docker performance, make sure to expose 2375 port without TSL in Docker Settings.
#### ToDo List
0. Check back for discord backup CLI, as it doesn't work with migrated JDA
1. Add commands
   8. `slots` - Spin the slots and win the jackpot! ...or lose everything. [Example](https://www.javacodegeeks.com/2014/08/programming-a-simple-slot-machine-game-using-java.html) - implement with memory for credits, etc
   19. `remindme [reminder]` - Set a reminder
   21. `whois [user mention]` - Get user information
   22. `whoisip [ip]` - Get info from WHOISIP service (https://ip-api.com/docs/api:json)
   23. `distance  [coords] [coords]` - Get the distance between two coordinates
45. Add 'docker logs' command to log.debug if one of backups has failed
14. Implement wordFrequencyAnalyzer
18. Separate auto reply per guild
19. Separate status rotation per guild
20. Extend logger to every error\warning reply
23. Perform code analysis and fix issues (especially duplicates)
24. Embedded message couldn't be switched using emoji after certain time
27. Cyclomatic complexity
29. Understand how ownership of commands works
31. Remove IOException for certain types (like rule not found)
35. Calculate auto backup time from last moment of backup. Not absolute time
39. Add RichPresence to game activity
40. Add news module:
    1. Gaming news
    3. Major world news
    4. Major local news
    5. Memes
    6. NSFW
    7. WHO notifications API (https://www.who.int/csr/don/en/)
42. Deal with duplicate code
43. Extend reply core:
    2. Creat word usage frequency and add replies accordingly
    3. Добавить возможность процентно задать распределение между двумя режимами ответа. Настраивается параметром извне.
    4. Создать историю последних ответов, так, что бы ответы запоминались на указанный срок и не повторялись. Настраивается параметром извне.
    5. Добавить запретный словарик часто встречаемых междуимений и обращений, как "так, ага, угу, хаха". Настраивается параметром извне.
    6. Сейчас, при ответе по вхождению, он ищет линейно от начала до конца фразы и если первое слово совпало, всегда использует его. Добавить выбор рандомной фразы для вхождения.  Настраивается параметром извне.
    7. Самые частые обращение к боту, нужно добавить в запретный словарик, так как они будут чаще всего использовать при общении с ним. Такие как: "влад, владик".
    8. Научить бота отвечать на обращение (@bot)
    8. Разделить разные словари, так что бы можно было задавать отдельные правила, для отдельно подключаемых словарей.
    9. Добавити лімітацію на відповідь бота на певні канали (також, винести це в Guild Settings)
    10. При первинному зчитуванні списку reply заміняти смайли на усі доступні на сервері (або заміняти на етапі відправки повідомлення для зменшення кількості операцій ІО)
    11. Ігнорувати команди (ті які починаються з "~")
44. Rebuild settings file if some setting is missing from .json
45. Centralize thread running (bot.getThreadpool().execute(() ->)
46. Почати працювати з Шардами (багато гільдій) і перенести усі файли в базу (Vortex)