package dev.shuncha.efp;

import dev.shuncha.efp.gui.EfpGuiListener;
import org.bukkit.plugin.java.JavaPlugin;

public class EfpPlugin extends JavaPlugin {

    private LocationManager locationManager;

    @Override
    public void onEnable() {
        locationManager = new LocationManager(this);
        locationManager.load();

        EfpCommand command = new EfpCommand(this);
        getCommand("efp").setExecutor(command);
        getCommand("efp").setTabCompleter(command);
        getServer().getPluginManager().registerEvents(new EfpGuiListener(this), this);

        getLogger().info("Everyone's Favorite Place を有効化しました");
    }

    @Override
    public void onDisable() {
        if (locationManager != null) {
            locationManager.save();
        }
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }
}