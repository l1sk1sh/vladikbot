#### ToDo List
0. Create Base class for Daemons
0. Change usage of deprecated callback LogContainerResultCallback()
0. Create all backup using Name + IDs of channels
0. Automatic backup separation per date should be done into every separate channel. Current realization will remove channel backup, if it was removed from guild
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
    8. Word of the day
42. Deal with duplicate code
43. Extend reply core:
    2. Creat word usage frequency and add replies accordingly
    3. �������� ����������� ��������� ������ ������������� ����� ����� �������� ������. ������������� ���������� �����.
    4. ������� ������� ��������� �������, ���, ��� �� ������ ������������ �� ��������� ���� � �� �����������. ������������� ���������� �����.
    5. �������� ��������� �������� ����� ����������� ����������� � ���������, ��� "���, ���, ���, ����". ������������� ���������� �����.
    6. ������, ��� ������ �� ���������, �� ���� ������� �� ������ �� ����� ����� � ���� ������ ����� �������, ������ ���������� ���. �������� ����� ��������� ����� ��� ���������.  ������������� ���������� �����.
    7. ����� ������ ��������� � ����, ����� �������� � ��������� ��������, ��� ��� ��� ����� ���� ����� ������������ ��� ������� � ���. ����� ���: "����, ������".
    8. ������� ���� �������� �� ��������� (@bot)
    8. ��������� ������ �������, ��� ��� �� ����� ���� �������� ��������� �������, ��� �������� ������������ ��������.
    9. �������� ������� �� ������� ���� �� ����� ������ (�����, ������� �� � Guild Settings)
    10. ��� ���������� ���������� ������ reply ������� ������ �� �� �������� �� ������ (��� ������� �� ���� �������� ����������� ��� ��������� ������� �������� ��)
    11. ���������� ������� (� �� ����������� � "~")
44. Rebuild settings file if some setting is missing from .json
45. Centralize thread running (bot.getThreadpool().execute(() ->)
46. ������ ��������� � ������� (������ �����) � ��������� �� ����� � ���� (Vortex)
47. Migrate to database, instead of .json files (json handlind becomes complex)