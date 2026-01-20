package com.github.nickxgrom.prgcraft_s1_final_scene;

import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;

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
                    if (ticks >= 10 * 20) { // Уменьшено время с 5 до 3 секунд
                        cancel();
                        return;
                    }

                    // Центрируем сферу по центру блока
                    spawnParticleSphere(
                            blockLocation.getWorld(),
                            blockLocation.getX() + 0.5, // Центр блока по X
                            blockLocation.getY() + 2.5, // Центр на высоте +2.5
                            blockLocation.getZ() + 0.5, // Центр блока по Z
                            4 // Уменьшен радиус для лучшей производительности
                    );

                    ticks++;
                }

            }.runTaskTimer(this, 0, 1); // Увеличен интервал с 10 до 15 тиков для меньших лагов
        }
    }
}
