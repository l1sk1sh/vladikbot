package config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

public abstract class FileConfigLoader implements ConfigLoader {

    protected Properties getConfigImpl(String folder, String filename) {
        if (folder == null || folder.length() == 0) {
            return null;
        }

        File file = new File(folder, filename);

        if (!file.exists() || file.isDirectory()) {
            return null;
        }

        try {
            try (Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
                Properties properties = new Properties();
                properties.load(reader);
                return properties;
            }
        } catch (Throwable e) {
            throw new RuntimeException("Unable to read property file: " + file.getAbsolutePath(), e);
        }
    }

}
