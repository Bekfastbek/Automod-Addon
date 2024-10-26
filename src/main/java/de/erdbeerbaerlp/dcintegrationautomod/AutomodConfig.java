package de.erdbeerbaerlp.dcintegrationautomod;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

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
        loadConfig(); // This method remains unchanged
    }

    public void loadConfig() {
        File configFile = new File("config/AutoModConfig.toml");
        
        // Check if the config file exists
        if (!configFile.exists()) {
            createDefaultConfig(configFile);
        }

        // Load the config (this remains unchanged)
        Toml toml = new Toml().read(configFile);
        automod = toml.to(AutoModSettings.class);
    }

    private void createDefaultConfig(File configFile) {
        // Create a default configuration
        Map<String, Object> defaultConfig = new HashMap<>();
        defaultConfig.put("enabled", true);
        defaultConfig.put("adminRoleAutomod", false);
        defaultConfig.put("adminRoleIDs", new String[0]);
        defaultConfig.put("bannedWords", new String[0]);
        defaultConfig.put("logChannelID", "");
        defaultConfig.put("minecraftChannelID", ""); // Add this line for Minecraft channel ID

        // Write the default configuration to the file
        TomlWriter writer = new TomlWriter();
        try {
            // Create the parent directory if it doesn't exist
            configFile.getParentFile().mkdirs();
            // Write the default configuration
            FileWriter fileWriter = new FileWriter(configFile);
            writer.write(defaultConfig, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class AutoModSettings {
        public boolean enabled;
        public boolean adminRoleAutomod;
        public String[] adminRoleIDs;
        public String[] bannedWords;
        public String logChannelID;
        public String minecraftChannelID; // Add this line for Minecraft channel ID
    }
}
