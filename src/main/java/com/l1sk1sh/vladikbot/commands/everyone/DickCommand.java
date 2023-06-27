package com.l1sk1sh.vladikbot.commands.everyone;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.l1sk1sh.vladikbot.data.entity.Dick;
import com.l1sk1sh.vladikbot.services.dick.DickService;
import com.l1sk1sh.vladikbot.utils.CommandUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Comparator;

/**
 * @author l1sk1sh
 */
@Slf4j
@Service
public class DickCommand extends SlashCommand {

    private static final String EGGPLANT = "\uD83C\uDF46";
    private static final String DISAPPOINTED = "\uD83D\uDE1E";
    private static final String SMIRKING = "\uD83D\uDE0F";
    private static final String SCREAMING = "\uD83D\uDE31";
    private static final String UNAMUSED = "\uD83D\uDE12";

    @Autowired
    public DickCommand(DickService dickService) {
        this.name = "dick";
        this.help = "Хочеш побачити пісюна?";
        this.guildOnly = true;
        this.children = new SlashCommand[]{
                new Grow(dickService),
                new Register(dickService),
                new Unregister(dickService),
                new List(dickService)
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.reply(CommandUtils.getListOfChildCommands(event, children, name).toString()).setEphemeral(true).queue();
    }

    private static final class Register extends SlashCommand {

        private final DickService dickService;

        private Register(DickService dickService) {
            this.dickService = dickService;
            this.name = "register";
            this.help = "Приєднатися до культу";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Guild guild = event.getGuild();
            Member author = event.getMember();
            if (guild == null || author == null) {
                log.warn("Command has been called not from guild.");
                return;
            }

            boolean registered = dickService.register(author.getIdLong(), guild.getIdLong());
            if (registered) {
                event.replyFormat("У тебе з'явився пісюн! %1$s", EGGPLANT).queue();
                return;
            }

            event.replyFormat("У тебе уже є пісюн %1$s", event.getClient().getWarning()).setEphemeral(true).queue();
        }
    }

    private static final class Unregister extends SlashCommand {

        private final DickService dickService;

        private Unregister(DickService dickService) {
            this.dickService = dickService;
            this.name = "unregister";
            this.help = "Сепоку для пісюна";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Guild guild = event.getGuild();
            Member author = event.getMember();
            if (guild == null || author == null) {
                log.warn("Command has been called not from guild.");
                return;
            }

            boolean unregistered = dickService.unregister(author.getIdLong(), guild.getIdLong());
            if (unregistered) {
                event.replyFormat("%1$s Пісюн засох, прощавай", DISAPPOINTED).queue();
                return;
            }

            event.replyFormat("Немає чого відрізати %1$s", SMIRKING).setEphemeral(true).queue();
        }
    }

    private static final class List extends SlashCommand {

        private final DickService dickService;

        private List(DickService dickService) {
            this.dickService = dickService;
            this.name = "list";
            this.help = "Список пісюнів";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Guild guild = event.getGuild();
            if (guild == null) {
                log.warn("Command has been called not from guild.");
                return;
            }

            JDA jda = event.getJDA();

            java.util.List<Dick> dicks = dickService.getAllDicks(guild.getIdLong());
            if (dicks.isEmpty()) {
                event.replyFormat("%1$s Ні одного пісюна на горизонті", EGGPLANT).setEphemeral(true).queue();
                return;
            }

            dicks.sort(Comparator.comparingInt(Dick::getSize).reversed());

            StringBuilder dickList = new StringBuilder();

            int position = 1;
            for (Dick dick : dicks) {
                User user = jda.getUserById(dick.getAuthorId());
                if (user == null) {
                    continue;
                }

                dickList.append(position)
                        .append(". \t`")
                        .append(user.getEffectiveName())
                        .append("`\t");

                if (dick.getSize() == 0) {
                    dickList.append("без пісюньця ")
                            .append(SMIRKING)
                            .append("\r\n");
                } else {
                    dickList.append(" - ")
                            .append(dick.getSize())
                            .append("см")
                            .append("\r\n");
                }
            }

            MessageCreateBuilder builder = new MessageCreateBuilder();
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle(EGGPLANT + " Герої пісюнів")
                    .setColor(new Color(138, 0, 198))
                    .setDescription(dickList.toString());

            event.reply(builder.setEmbeds(embedBuilder.build()).build()).queue();
        }
    }

    private static final class Grow extends SlashCommand {

        private final DickService dickService;

        private Grow(DickService dickService) {
            this.dickService = dickService;
            this.name = "grow";
            this.help = "Трошки піднатужитись і ..!";
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            Guild guild = event.getGuild();
            Member author = event.getMember();
            if (guild == null || author == null) {
                log.warn("Command has been called not from guild.");
                return;
            }

            DickService.DickResults result = dickService.grow(author.getIdLong(), guild.getIdLong());

            switch (result) {
                case absent:
                    event.replyFormat("У тебе немає пісюна %1$s", event.getClient().getWarning()).queue();
                    break;
                case grow:
                    event.replyFormat("Твій пісюн виріс на **%1$sсм** і тепер він **%2$sсм**", result.getChange(), result.getSize()).queue();
                    break;
                case shrink:
                    if (result.getSize() == 0) {
                        event.replyFormat("У тебе немає пісюна %1$s", SMIRKING).queue();
                        break;
                    }

                    event.replyFormat("Твій пісюн підсох на **%1$sсм** і тепер він **%2$sсм**", result.getChange(), result.getSize()).queue();
                    break;
                case cut:
                    event.replyFormat("Твій пісюн відсох! Яка трагедія %1$s", SCREAMING).queue();
                    break;
                case nothing:
                    event.replyFormat("У тебе вяленький пісюнчик. %1$s Нічого не сталося...", UNAMUSED).queue();
                    break;
                case already:
                    event.replyFormat("Твій пісюнчик втомився за сьогодні. Грай через %1$s", result.getNextTime()).setEphemeral(true).queue();
                    break;
            }
        }
    }
}
