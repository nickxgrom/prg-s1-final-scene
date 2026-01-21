package com.github.nickxgrom.prgcraft_s1_final_scene;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.time.Instant;

/**
 * Утилитарный класс для работы с частицами и эффектами
 */
public class ParticleUtils {

    private final Prgcraft_s1_final_scene plugin;

    public ParticleUtils(Prgcraft_s1_final_scene plugin) {
        this.plugin = plugin;
    }

    /**
     * Создает сферу из частиц в заданных координатах
     * @param world Мир
     * @param x координата X
     * @param y координата Y
     * @param z координата Z
     * @param radius радиус сферы
     */
    public void spawnParticleSphere(World world, double x, double y, double z, double radius) {
        // Количество частиц на поверхности сферы
        int points = 300;

        for (int i = 0; i < points; i++) {
            // Генерируем случайные углы для равномерного распределения по сфере
            double theta = Math.random() * 2 * Math.PI; // угол по горизонтали
            double phi = Math.acos(1 - 2 * Math.random()); // угол по вертикали

            // Вычисляем координаты точки на сфере
            double particleX = x + radius * Math.sin(phi) * Math.cos(theta);
            double particleY = y + radius * Math.cos(phi);
            double particleZ = z + radius * Math.sin(phi) * Math.sin(theta);

            Location particleLocation = new Location(world, particleX, particleY, particleZ);

            // Создание частиц
            Particle.DUST_COLOR_TRANSITION.builder()
                    .location(particleLocation)
                    .count(1)
                    .colorTransition(Color.BLUE, Color.RED)
                    .offset(0.1, 0.1, 0.1)
                    .spawn();
        }
    }

    /**
     * Притягивает игрока к определенной точке, отключая гравитацию
     * @param player Игрок, которого нужно притянуть
     * @param targetLocation Точка, к которой притягивается игрок
     * @param pullStrength Сила притягивания (рекомендуется 0.1 - 0.5)
     * @param stopDistance Расстояние, на котором притягивание останавливается
     */
    public void pullPlayerToLocation(Player player, Location targetLocation, double pullStrength, double stopDistance, int banHoldTimer) {
        GameMode originalGameMode = player.getGameMode();

        if (player.isInsideVehicle()) {
            player.leaveVehicle();
        }

        player.setGravity(false);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {


                Location playerLocation = player.getLocation();
                double distance = playerLocation.distance(targetLocation);

                if (distance <= stopDistance) {

                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 9999, 3, false, false, false));
                    cancel();

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.ban("конец", (java.time.Duration) null, null);
                        }
                    }.runTaskLater(plugin, banHoldTimer * 20L);
                    return;
                }

                if (player.isInsideVehicle()) {
                    player.leaveVehicle();
                }

                Vector direction = targetLocation.toVector().subtract(playerLocation.toVector()).normalize();

                Vector velocity = direction.multiply(pullStrength);
                player.setVelocity(velocity);

                player.getWorld().spawnParticle(
                    Particle.PORTAL,
                    playerLocation,
                    10,
                    0.3, 0.3, 0.3,
                    0.1
                );

                ticks++;
            }

        }.runTaskTimer(plugin, 0, 1);
    }
}
