package com.github.nickxgrom.prgcraft_s1_final_scene;

import org.bukkit.plugin.java.JavaPlugin;

public final class Prgcraft_s1_final_scene extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
