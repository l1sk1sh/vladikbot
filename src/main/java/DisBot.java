import config.Config;
import config.ConfigInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;


public class DisBot {
    private static final Logger logger = LoggerFactory.getLogger(DisBot.class);

    private DisBot() {
        Config config = new ConfigInitializer().init();
        DiscordClientBuilder builder = new DiscordClientBuilder(config.getToken());
        DiscordClient client = builder.build();

        client.getEventDispatcher().on(ReadyEvent.class)
                .subscribe(event -> {
                    User self = event.getSelf();
                    logger.info(String.format("Logged in as %s#%s", self.getUsername(), self.getDiscriminator()));
                });

        client.getEventDispatcher().on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
                .filter(message -> message.getContent().orElse("").equalsIgnoreCase("!ping"))
                .flatMap(Message::getChannel)
                .flatMap(channel -> channel.createMessage("Pong!"))
                .subscribe();

        client.login().block();
    }

    public static void main(String[] args) {
        new DisBot();
    }
}
