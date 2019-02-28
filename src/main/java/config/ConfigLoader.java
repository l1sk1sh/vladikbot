package config;

import java.util.Properties;

public interface ConfigLoader {
    Properties getConfig(String file);
}
