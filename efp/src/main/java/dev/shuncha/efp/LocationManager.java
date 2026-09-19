package dev.shuncha.efp;

import dev.shuncha.efp.model.BaseLocation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class LocationManager {

    private final JavaPlugin plugin;
    private final File file;
    private final Map<UUID, BaseLocation> locations = new LinkedHashMap<>();

    public LocationManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "locations.yml");
    }

    public void load() {
        locations.clear();
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("locations");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            try {
                UUID id = UUID.fromString(key);
                UUID owner = UUID.fromString(section.getString(key + ".owner"));
                String ownerName = section.getString(key + ".ownerName", "unknown");
                String name = section.getString(key + ".name", "unnamed");
                String world = section.getString(key + ".world", "world");
                // 旧データ(environment未保存)はワールド名から推測する
                String environment = section.getString(key + ".environment", guessEnvironment(world));
                int x = section.getInt(key + ".x");
                int y = section.getInt(key + ".y");
                int z = section.getInt(key + ".z");
                boolean pub = section.getBoolean(key + ".public", false);
                locations.put(id, new BaseLocation(id, owner, ownerName, name, world, environment, x, y, z, pub));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.WARNING, "locations.ymlの読み込みに失敗しました: " + key, e);
            }
        }
    }

    private String guessEnvironment(String worldName) {
        if (worldName.endsWith("_nether")) {
            return "NETHER";
        }
        if (worldName.endsWith("_the_end")) {
            return "THE_END";
        }
        return "NORMAL";
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (BaseLocation loc : locations.values()) {
            String base = "locations." + loc.getId();
            config.set(base + ".owner", loc.getOwnerId().toString());
            config.set(base + ".ownerName", loc.getOwnerName());
            config.set(base + ".name", loc.getName());
            config.set(base + ".world", loc.getWorld());
            config.set(base + ".environment", loc.getEnvironment());
            config.set(base + ".x", loc.getX());
            config.set(base + ".y", loc.getY());
            config.set(base + ".z", loc.getZ());
            config.set(base + ".public", loc.isPublic());
        }
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "locations.ymlの保存に失敗しました", e);
        }
    }

    /** 同名(大文字小文字区別なし)の自分の拠点があれば更新、無ければ新規追加 */
    public BaseLocation addOrUpdate(UUID owner, String ownerName, String name,
                                     String world, String environment, int x, int y, int z, boolean pub) {
        BaseLocation existing = findByOwnerAndName(owner, name);
        if (existing != null) {
            locations.remove(existing.getId());
        }
        UUID id = UUID.randomUUID();
        BaseLocation loc = new BaseLocation(id, owner, ownerName, name, world, environment, x, y, z, pub);
        locations.put(id, loc);
        save();
        return loc;
    }

    public boolean remove(UUID owner, String name) {
        BaseLocation existing = findByOwnerAndName(owner, name);
        if (existing == null) {
            return false;
        }
        locations.remove(existing.getId());
        save();
        return true;
    }

    public boolean setPublic(UUID owner, String name, boolean pub) {
        BaseLocation existing = findByOwnerAndName(owner, name);
        if (existing == null) {
            return false;
        }
        existing.setPublic(pub);
        save();
        return true;
    }

    /**
     * 拠点名を変更する。
     * @return 成功時true。対象が存在しない、または新しい名前が既に使われている場合はfalse
     */
    public boolean rename(UUID owner, String oldName, String newName) {
        BaseLocation existing = findByOwnerAndName(owner, oldName);
        if (existing == null) {
            return false;
        }
        if (!oldName.equalsIgnoreCase(newName) && findByOwnerAndName(owner, newName) != null) {
            return false;
        }
        existing.setName(newName);
        save();
        return true;
    }

    public BaseLocation findByOwnerAndName(UUID owner, String name) {
        for (BaseLocation loc : locations.values()) {
            if (loc.getOwnerId().equals(owner) && loc.getName().equalsIgnoreCase(name)) {
                return loc;
            }
        }
        return null;
    }

    public List<BaseLocation> getByOwner(UUID owner) {
        List<BaseLocation> result = new ArrayList<>();
        for (BaseLocation loc : locations.values()) {
            if (loc.getOwnerId().equals(owner)) {
                result.add(loc);
            }
        }
        return result;
    }

    public List<BaseLocation> getPublicLocations() {
        List<BaseLocation> result = new ArrayList<>();
        for (BaseLocation loc : locations.values()) {
            if (loc.isPublic()) {
                result.add(loc);
            }
        }
        Collections.sort(result, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return result;
    }

    /** 指定した環境(NORMAL/NETHER/THE_END)の公開拠点のみを取得 */
    public List<BaseLocation> getPublicLocationsByEnvironment(String environment) {
        List<BaseLocation> result = new ArrayList<>();
        for (BaseLocation loc : locations.values()) {
            if (loc.isPublic() && loc.getEnvironment().equals(environment)) {
                result.add(loc);
            }
        }
        Collections.sort(result, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return result;
    }
}