package com.github.happyzleaf.pokexpmultiplier;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.File;
import java.io.IOException;

public class PokexpConfig {
    private static PokexpConfig instance = new PokexpConfig();

    public static PokexpConfig getInstance() {
        return instance;
    }

    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private CommentedConfigurationNode config;
    private File configFile;

    public void createConfig() {
        try {
            configFile.createNewFile();
            loadConfig();
            config.getNode("multiplier", "default").setValue(1.0f);
            config.getNode("message", "enable").setValue(true);
            config.getNode("message", "message").setComment("You can use #POKEMON, #PLAYER, #OLD_EXP, #NEW_EXP, #MULTIPLIER.").setValue("Your experience has been multiplied from #OLD_EXP to #NEW_EXP (multiplied by #MULTIPLIER)");
            saveConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setup(File configFile, ConfigurationLoader<CommentedConfigurationNode> configLoader) {
        this.configLoader = configLoader;
        this.configFile = configFile;
        if(!configFile.exists()) {
            createConfig();
        } else
            loadConfig();
    }

    public CommentedConfigurationNode getConfig() {
        return config;
    }

    public void loadConfig() {
        if(!configFile.exists()) {
            createConfig();
        } else
            try {
                config = configLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public void saveConfig() {
        try {
            configLoader.save(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveAndLoad() {
        saveConfig();
        loadConfig();
    }
}

