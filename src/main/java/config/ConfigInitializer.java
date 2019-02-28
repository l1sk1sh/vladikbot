package config;

import java.util.Properties;

public class ConfigInitializer extends FileConfigLoader {
    private static final String applicationConfigName   = "app.properties";
    private static final String securityConfigName      = "sec.properties";
    private static final String configFolder            = "cfg";

    public Config init() {
        Properties secProps = getConfig(securityConfigName);
        Properties appProps = getConfig(applicationConfigName);

        return new Config.Builder()
                .addAppName(appProps.getProperty("appName"))
                .addToken(secProps.getProperty("token"))
                .build();
    }

    @Override
    public Properties getConfig(String file) {
        return getConfigImpl(configFolder, file);
    }
}
