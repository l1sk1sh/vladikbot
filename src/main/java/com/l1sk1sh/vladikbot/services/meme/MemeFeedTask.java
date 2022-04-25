package com.l1sk1sh.vladikbot.services.meme;

import com.l1sk1sh.vladikbot.VladikBot;
import com.l1sk1sh.vladikbot.data.entity.GuildSettings;
import com.l1sk1sh.vladikbot.data.entity.SentMeme;
import com.l1sk1sh.vladikbot.data.repository.GuildSettingsRepository;
import com.l1sk1sh.vladikbot.data.repository.SentMemeRepository;
import com.l1sk1sh.vladikbot.network.dto.Meme;
import com.l1sk1sh.vladikbot.services.notification.MemeNotificationService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

/**
 * @author l1sk1sh
 */
@RequiredArgsConstructor
class MemeFeedTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(MemeFeedTask.class);

    private final SentMemeRepository sentMemeRepository;
    private final MemeNotificationService memeNotificationService;
    private final GuildSettingsRepository guildSettingsRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void run() {
        log.debug("Running memes lookup...");

        Meme meme;
        try {
            meme = restTemplate.getForObject("http://meme-api.herokuapp.com/gimme", Meme.class);
        } catch (RestClientException e) {
            log.error("Failed to get meme.", e);
            return;
        }

        if (meme == null) {
            log.error("Response body is empty.");

            return;
        }

        List<SentMeme> lastSentMemes = sentMemeRepository.findTop30ByOrderByIdDesc();

        boolean memeAlreadySent = lastSentMemes.stream().anyMatch(sentMeme -> sentMeme.getMemeId().equals(meme.getPostLink()));
        if (memeAlreadySent) {
            return;
        }

        sentMemeRepository.save(new SentMeme(meme.getPostLink()));

        log.info("Sending '{}' article ({}).", meme.getTitle(), meme.getPostLink());
        for (GuildSettings guildSettings : guildSettingsRepository.getAllBySendMemesIsTrue()) {
            Guild guild = VladikBot.jda().getGuildById(guildSettings.getGuildId());
            log.info("Sending '{}' to {}.", meme.getTitle(), Objects.requireNonNull(guild).getName());

            memeNotificationService.sendMemesArticle(guild, meme);
        }
    }
}
