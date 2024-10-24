package de.erdbeerbaerlp.dcintegrationautomod;

import java.io.File;

import com.moandjiezana.toml.Toml;

public class AutomodConfig {
    private static AutomodConfig instance;
    public AutoModSettings automod;

    public static AutomodConfig instance() {
        if (instance == null) {
            instance = new AutomodConfig();
        }
        return instance;
    }

    public AutomodConfig() {
        loadConfig();
    }

    public void loadConfig() {
        File configFile = new File("config/AutoModConfig.toml");
        Toml toml = new Toml().read(configFile);
        automod = toml.to(AutoModSettings.class);
    }

    public static class AutoModSettings {
        public boolean enabled;
        public boolean adminRoleAutomod;
        public String[] adminRoleIDs;
        public String[] bannedWords;
        public String logChannelID;
    }
}
