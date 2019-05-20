package net.yofuzzy3.BungeePortals.Tasks;

import org.bukkit.scheduler.BukkitRunnable;

import net.yofuzzy3.BungeePortals.BungeePortals;

public class SaveTask extends BukkitRunnable {

    private BungeePortals plugin;

    public SaveTask(BungeePortals plugin) {
        this.plugin = plugin;
    }

    public void run() {
        if (plugin.configFile.getBoolean("SaveTask.Enabled")) {
            plugin.savePortalsData();
        }
    }

}
