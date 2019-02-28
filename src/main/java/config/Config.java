package config;

public class Config {
    private String token;
    private String appName;

    public static class Builder {
        private String token;
        private String appName;

        public Builder() {}

        public Builder addToken(String token) {
            this.token = token;

            return this;
        }

        public Builder addAppName(String appName) {
            this.appName = appName;

            return this;
        }

        public Config build() {
            Config config = new Config();
            config.token = this.token;
            config.appName = this.appName;

            return config;
        }
    }

    private Config() {}

    public String getToken() {
        return token;
    }

    public String getAppName() {
        return appName;
    }
}
