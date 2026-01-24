package com.github.nickxgrom.prgcraft_s1_final_scene;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Команда для бана всех игроков (онлайн и оффлайн)
 */
public class BanAllCommand implements CommandExecutor {

    private final Prgcraft_s1_final_scene plugin;

    public BanAllCommand(Prgcraft_s1_final_scene plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.isOp() && !sender.hasPermission("prgcraft.banall")) {
            sender.sendMessage(Component.text("У вас нет прав для выполнения этой команды!").color(NamedTextColor.RED));
            return true;
        }

        // Сообщение для кика и бана
        Component kickMessage = Component.text()
                .append(Component.text("\n\nСезон завершен\n\n").color(NamedTextColor.WHITE))
                .append(Component.text("Спасибо за игру!\n\n").color(NamedTextColor.WHITE))
                .append(Component.text("Увидимся в следующем сезоне!\n\n").color(NamedTextColor.GREEN))
                .append(Component.text("https://prgcraft.evenfine.name/\n\n").color(NamedTextColor.WHITE))
                .build();

        String banReason = "Сезон завершен. Спасибо за игру!";

        int onlineCount = 0;
        int offlineCount = 0;

        // Банить всех онлайн игроков
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kick(kickMessage);
            player.ban(banReason, (java.time.Duration) null, null);
            onlineCount++;
        }

        // Банить всех оффлайн игроков
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            // Пропускаем игроков без имени или которые никогда не играли
            if (offlinePlayer.getName() == null || !offlinePlayer.hasPlayedBefore()) {
                continue;
            }

            // Пропускаем игроков, которые уже забанены
            if (!offlinePlayer.isBanned()) {
                offlinePlayer.ban(banReason, (java.time.Duration) null, null);
                offlineCount++;
            }
        }

        sender.sendMessage(Component.text("Забанено игроков:").color(NamedTextColor.GREEN));
        sender.sendMessage(Component.text("- Онлайн: " + onlineCount).color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("- Оффлайн: " + offlineCount).color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("- Всего: " + (onlineCount + offlineCount)).color(NamedTextColor.GOLD));

        return true;
    }
}
