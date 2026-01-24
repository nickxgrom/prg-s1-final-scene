package com.github.nickxgrom.prgcraft_s1_final_scene;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Слушатель событий для обработки убийства дракона
 */
public class FinalScene implements Listener {
    private final int PULL_HOLD_TIMER = 20;
    private final int BAN_HOLD_TIMER = 2;
    private final int SPHERE_LIVE_TIMER = 300;
    private final int SPHERE_RADIUS = 4;
    private final int PORTAL_DELAY = 40;
    private final Prgcraft_s1_final_scene plugin;
    private final ParticleUtils particleUtils;
    private boolean isDragonDead = false;

    public FinalScene(Prgcraft_s1_final_scene plugin) {
        this.plugin = plugin;
        this.particleUtils = new ParticleUtils(plugin);
    }

    /**
     * Замена блоков END_PORTAL на скалк в области центрального портала
     */
    private void replaceCentralPortalWithSculk(Location center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location loc = center.clone().add(x, y, z);
                    Block block = loc.getBlock();

                    // Заменяем только блоки центрального портала
                    if (block.getType() == Material.END_PORTAL) {
                        block.setType(Material.SCULK);
                    }
                }
            }
        }
    }

    /**
     * Замена конкретного END_GATEWAY на скалк
     */
    private void replaceGatewayWithSculk(World world, int x, int y, int z) {
        Location gatewayLoc = new Location(world, x, y, z);
        Block block = gatewayLoc.getBlock();

        if (block.getType() == Material.END_GATEWAY) {
            block.setType(Material.SCULK);
        }
    }

    /**
     * Обработчик события убийства дракона
     */
    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        // Проверяем, что убитое существо - это Эндер Дракон
        if (event.getEntity().getType() == EntityType.ENDER_DRAGON) {
            isDragonDead = true;

            Location dragonLocation = event.getEntity().getLocation();
            World endWorld = dragonLocation.getWorld();

            Location portalLocation = new Location(endWorld, 0, 64, 0);

            Location sphereCenter = portalLocation.clone().add(0, 30, 0);

            new BukkitRunnable() {
                @Override
                public void run() {
                    // Замена центрального портала на скалк
                    replaceCentralPortalWithSculk(portalLocation, 20);

                    // Замена первого портала на дальние острова (END_GATEWAY) по координатам x:96 y:75 z:0
                    replaceGatewayWithSculk(endWorld, 29, 75, -92);
                }
            }.runTaskTimer(plugin, 154 /* 7.7s according to wiki */, 1);

            new BukkitRunnable() {
                @Override
                public void run() {

                    sphereCenter.getWorld().playSound(
                            sphereCenter,
                            Sound.BLOCK_RESPAWN_ANCHOR_CHARGE,
                            5.0f,
                            0.8f
                    );

                    new BukkitRunnable() {
                        int ticks = 0;

                        @Override
                        public void run() {
                            if (ticks >= SPHERE_LIVE_TIMER * 20) {
                                cancel();
                                return;
                            }

                            particleUtils.spawnParticleSphere(
                                    sphereCenter.getWorld(),
                                    sphereCenter.getX() + 0.5,
                                    sphereCenter.getY() + 0.5,
                                    sphereCenter.getZ() + 0.5,
                                    SPHERE_RADIUS // Радиус сферы
                            );

                            if (ticks % 20 == 0) {
                                sphereCenter.getWorld().playSound(
                                        sphereCenter,
                                        Sound.AMBIENT_BASALT_DELTAS_MOOD,
                                        5.0f, // громкость
                                        0.8f  // тональность
                                );
                            }

                            ticks++;
                        }

                    }.runTaskTimer(plugin, 0, 1);

                    new BukkitRunnable() {
                        @Override
                        public void run() {

                            for (Player player : plugin.getServer().getOnlinePlayers()) {
                                player.playSound(
                                        player.getLocation(),
                                        Sound.ITEM_LODESTONE_COMPASS_LOCK,
                                        5.0f,
                                        0.5f
                                );
                                Component message1 = Component.text("Вы убили моего домашнего питомца...");
                                Component message2 = Component.text("Теперь вы станете моими питомцами!");

                                player.sendMessage(message1.color(NamedTextColor.DARK_PURPLE));
                                player.sendMessage(message2.color(NamedTextColor.DARK_PURPLE));
                            }

                            for (Player player : endWorld.getPlayers()) {
                                particleUtils.pullPlayerToLocation(player, sphereCenter, 0.15, 2.0, BAN_HOLD_TIMER);
                            }
                        }

                    }.runTaskLater(plugin, PULL_HOLD_TIMER * 20);
                }
            }.runTaskLater(plugin, PORTAL_DELAY*20);
        }
    }

    /**
     * Обработчик события телепортации через портал
     * Блокирует телепортацию через END_PORTAL и END_GATEWAY только если дракон мертв
     */
    @EventHandler
    public void onPortalTeleport(PlayerTeleportEvent event) {
        // Проверяем, что дракон мертв и телепортация происходит через портал Энда
        if (isDragonDead) {
            // Отменяем телепортацию
            event.setCancelled(true);
        }
    }
}
