package com.github.nickxgrom.prgcraft_s1_final_scene;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Слушатель событий для обработки убийства дракона
 */
public class FinalScene implements Listener {
    private final int PULL_HOLD_TIMER = 5;
    private final int BAN_HOLD_TIMER = 2;
    private final int SPHERE_LIVE_TIMER = 300;
    private final int SPHERE_RADIUS = 4;
    private final int PORTAL_DELAY = 5;
    private final Prgcraft_s1_final_scene plugin;
    private final ParticleUtils particleUtils;

    public FinalScene(Prgcraft_s1_final_scene plugin) {
        this.plugin = plugin;
        this.particleUtils = new ParticleUtils(plugin);
    }

    /**
     * Обработчик события убийства дракона
     */
    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        // Проверяем, что убитое существо - это Эндер Дракон
        if (event.getEntity().getType() == EntityType.ENDER_DRAGON) {
            Location dragonLocation = event.getEntity().getLocation();
            World endWorld = dragonLocation.getWorld();

            Location portalLocation = new Location(endWorld, 0, 64, 0);

            Location sphereCenter = portalLocation.clone().add(0, 15, 0);


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
                            for (Player player : endWorld.getPlayers()) {
                                particleUtils.pullPlayerToLocation(player, sphereCenter, 0.15, 2.0, BAN_HOLD_TIMER);
                            }
                        }

                    }.runTaskLater(plugin, PULL_HOLD_TIMER * 20);
                }
            }.runTaskLater(plugin, PORTAL_DELAY*20);
        }
    }
}
