package com.multiheaded.vladikbot.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Oliver Johnson
 */
class AbstractSettingsManager {
    final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger logger = LoggerFactory.getLogger(AbstractSettingsManager.class);

    void writeSettings(AbstractSettings settings, File configFile) {
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(configFile));
            writer.setIndent("  ");
            writer.setHtmlSafe(false);
            gson.toJson(settings, settings.getClass(), writer);
            writer.close();
        } catch (IOException e) {
            logger.error(String.format("Error while writing %s file.", configFile.getName()),
                    e.getLocalizedMessage(), e.getCause());
        }
    }
}
