import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class DisBot {
    private Config config;
    private final Gson gson = new Gson();

    private DisBot() {
        try {
            readConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }

        DiscordClientBuilder builder = new DiscordClientBuilder("TOKEN HERE");
        DiscordClient client = builder.build();
        client.login().block();
    }

    private void readConfig() throws IOException {
        File confFile = new File("config.json");
        if (!confFile.exists()) {
            this.config = new Config();
            JsonWriter writer = new JsonWriter(new FileWriter(confFile));
            writer.setIndent("  ");
            writer.setHtmlSafe(false);
            gson.toJson(config, Config.class, writer);
            writer.close();
            System.out.println("Created config.json");
            System.exit(0);
        } else {
            this.config = gson.fromJson(
                    Files.readAllLines(confFile.toPath()).stream()
                            .map(String::trim)
                            .filter(s -> !s.startsWith("#") && !s.isEmpty())
                            .reduce((a, b) -> a += b)
                            .orElse(""),
                    Config.class
            );
        }
    }

    public static void main(String[] args) {
        new DisBot();
    }
}
