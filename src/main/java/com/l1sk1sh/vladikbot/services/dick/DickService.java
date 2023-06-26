package com.l1sk1sh.vladikbot.services.dick;


import com.l1sk1sh.vladikbot.data.entity.Dick;
import com.l1sk1sh.vladikbot.data.repository.DickRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class DickService {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("h годин та mm хв.");

    private final DickRepository dickRepository;
    private final Random random = new Random();

    public DickService(DickRepository dickRepository) {
        this.dickRepository = dickRepository;
    }

    public boolean register(long authorId, long guildId) {
        Dick registeredDick = dickRepository.getDickByAuthorIdAndGuildId(authorId, guildId);

        if (registeredDick == null) {
            dickRepository.save(new Dick(authorId, guildId, 0, System.currentTimeMillis()));
            return true;
        }

        return false;
    }

    public boolean unregister(long authorId, long guildId) {
        Dick registeredDick = dickRepository.getDickByAuthorIdAndGuildId(authorId, guildId);

        if (registeredDick != null) {
            dickRepository.delete(registeredDick);
            return true;
        }

        return false;
    }

    public List<Dick> getAllDicks(long guildId) {
        return dickRepository.getDicksByGuildId(guildId);
    }

    private boolean grownToday(Dick dickInQuestion) {
        Date now = Calendar.getInstance().getTime();
        Date lastUsed = new Date(dickInQuestion.getModifiedTime());
        return DateUtils.isSameDay(now, lastUsed);
    }

    private String getNextTime() {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        Date tomorrow = DateUtils.addDays(calendar.getTime(), 1);

        DateUtils.setMinutes(tomorrow, 0);
        long difference = tomorrow.getTime() - now.getTime();
        return formatter.format(new Date(difference));
    }

    public DickResults grow(long authorId, long guildId) {
        Dick registeredDick = dickRepository.getDickByAuthorIdAndGuildId(authorId, guildId);

        if (registeredDick == null) {
            return DickResults.absent;
        }

        String nextTime = getNextTime();

        if (grownToday(registeredDick)) {
            DickResults result = DickResults.already;
            result.setNextTime(nextTime);
            return result;
        }

        long now = System.currentTimeMillis();
        int chance = random.nextInt(100_00);
        log.info("Updating {} with chance {}", registeredDick.toString(), chance);

        if (chance < 1) { // < 0.01%
            registeredDick.setSize(0);
            registeredDick.setModifiedTime(now);
            dickRepository.save(registeredDick);

            DickResults result = DickResults.cut;
            result.setNextTime(nextTime);
            return result;

        } else if (chance < 1_00) { // < 1%
            registeredDick.setModifiedTime(now);
            DickResults result = DickResults.nothing;
            result.setNextTime(nextTime);
            return result;

        } else if (chance < 44_00) { // < 44%
            int shrinkSize = random.nextInt(8) + 1;
            int currentSize = registeredDick.getSize();
            int newSize;
            if (currentSize < shrinkSize) {
                newSize = 0;
            } else {
                newSize = currentSize - shrinkSize;
            }
            registeredDick.setSize(newSize);
            registeredDick.setModifiedTime(now);
            dickRepository.save(registeredDick);

            DickResults result = DickResults.shrink;
            result.setSize(registeredDick.getSize());
            result.setChange(shrinkSize);
            result.setNextTime(nextTime);
            return result;

        } else {
            int growSize = random.nextInt(8) + 1;
            registeredDick.setSize(registeredDick.getSize() + growSize);
            registeredDick.setModifiedTime(now);
            dickRepository.save(registeredDick);

            DickResults result = DickResults.grow;
            result.setSize(registeredDick.getSize());
            result.setChange(growSize);
            result.setNextTime(nextTime);
            return result;
        }
    }

    public enum DickResults {
        absent,
        already,
        grow,
        shrink,
        cut,
        nothing;

        @Setter
        @Getter
        private long size;

        @Setter
        @Getter
        private long change;

        @Setter
        @Getter
        private String nextTime;
    }
}