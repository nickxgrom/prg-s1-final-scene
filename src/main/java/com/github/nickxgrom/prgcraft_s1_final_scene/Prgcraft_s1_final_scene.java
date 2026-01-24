package com.github.nickxgrom.prgcraft_s1_final_scene;

import org.bukkit.plugin.java.JavaPlugin;

public final class Prgcraft_s1_final_scene extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new FinalScene(this), this);

        // Регистрируем команду для бана всех игроков
        if (getCommand("banall") != null) {
            getCommand("banall").setExecutor(new BanAllCommand(this));
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
