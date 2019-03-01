package com.multiheaded.disbot;

import com.multiheaded.disbot.command.HelpCommand;
import com.multiheaded.disbot.settings.Constants;
import com.multiheaded.disbot.settings.Settings;
import com.multiheaded.disbot.settings.SettingsManager;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.UnsupportedEncodingException;


public class DisBot {
    private static final Logger logger = LoggerFactory.getLogger(DisBot.class);
    private static JDA api;

    private static void setupBot() {
        try {
            Settings settings = SettingsManager.getInstance().getSettings();

            JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT).setToken(settings.token);

            HelpCommand help = new HelpCommand();
            jdaBuilder.addEventListener(help.registerCommand(help));

            api = jdaBuilder.build();
        } catch (LoginException le) {
            logger.error("Invalid username and/or password.");
            System.exit(Constants.BAD_USERNAME_PASS_COMBO);
        }
    }

    public static void main(String[] args) {
        setupBot();
    }
}
