package com.github.nickxgrom.prgcraft_s1_final_scene;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public final class Prgcraft_s1_final_scene extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
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
     * @param duration Длительность притягивания в тиках (20 тиков = 1 секунда)
     * @param pullStrength Сила притягивания (рекомендуется 0.1 - 0.5)
     * @param stopDistance Расстояние, на котором притягивание останавливается
     */
    public void pullPlayerToLocation(Player player, Location targetLocation, int duration, double pullStrength, double stopDistance) {
        // Выключаем гравитацию у игрока
        player.setGravity(false);

        // Применяем притяжение в течение заданной длительности
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                // Проверяем условия остановки
                if (ticks >= duration || !player.isOnline() || player.isDead()) {
                    // Включаем гравитацию обратно и останавливаем задачу
                    player.setGravity(true);
                    cancel();
                    return;
                }

                Location playerLocation = player.getLocation();
                double distance = playerLocation.distance(targetLocation);

                // Если игрок достиг целевой точки
                if (distance <= stopDistance) {
                    player.setGravity(true);
                    cancel();
                    return;
                }

                // Вычисляем направление к целевой точке
                Vector direction = targetLocation.toVector().subtract(playerLocation.toVector()).normalize();

                // Применяем силу притягивания
                Vector velocity = direction.multiply(pullStrength);
                player.setVelocity(velocity);

                // Создаем визуальный эффект частиц вокруг игрока
                player.getWorld().spawnParticle(
                    Particle.PORTAL,
                    playerLocation,
                    10,
                    0.3, 0.3, 0.3,
                    0.1
                );

                ticks++;
            }

        }.runTaskTimer(this, 0, 1);
    }

    /**
     * Обработчик события размещения блока
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // Проверяем, что поставлен блок березовой листвы
        if (event.getBlock().getType() == Material.BIRCH_LEAVES) {
            Location blockLocation = event.getBlock().getLocation();

            new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    if (ticks >= 20 * 20) {
                        cancel();
                        return;
                    }

                    // Центрируем сферу по центру блока
                    spawnParticleSphere(
                            blockLocation.getWorld(),
                            blockLocation.getX() + 0.5,
                            blockLocation.getY() + 2.5,
                            blockLocation.getZ() + 0.5,
                            4
                    );

                    ticks++;
                }

            }.runTaskTimer(this, 0, 1);

            new BukkitRunnable() {
                @Override
                public void run() {
                    Player player = event.getPlayer();
                    Location targetLocation = blockLocation.clone().add(0.5, 1, 0.5);

                    pullPlayerToLocation(player, targetLocation, 20 * 20, 0.2, 0.5);
                }

            }.runTaskLater(this, 5*20);
        }
    }
}

