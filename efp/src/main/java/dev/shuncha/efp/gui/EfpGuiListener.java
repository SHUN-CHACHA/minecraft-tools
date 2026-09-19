package dev.shuncha.efp.gui;

import dev.shuncha.efp.EfpPlugin;
import dev.shuncha.efp.model.BaseLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EfpGuiListener implements Listener {

    // グリッド定義: 9列 x 6行(0〜53、GUI全体をマスとして使用)。中央付近(列4,行2)がワールド座標(0,0)
    private static final int COLS = 9;
    private static final int ROWS = 6;
    private static final int CENTER_COL = 4;
    private static final int CENTER_ROW = 2;
    private static final int CENTER_SLOT = CENTER_ROW * COLS + CENTER_COL;
    private static final double MIN_SCALE = 16.0; // 1マスあたり最低16ブロック

    public EfpGuiListener(EfpPlugin plugin) {
        // 将来の拡張用にプラグイン参照を保持(現状未使用)
    }

    public static Inventory build(List<BaseLocation> sameDimensionLocations, String environment) {
        double scale = computeScale(sameDimensionLocations);

        Map<Integer, List<BaseLocation>> slotMap = new HashMap<>();
        for (BaseLocation loc : sameDimensionLocations) {
            int colOffset = clamp((int) Math.round(loc.getX() / scale), -CENTER_COL, COLS - 1 - CENTER_COL);
            int rowOffset = clamp((int) Math.round(loc.getZ() / scale), -CENTER_ROW, ROWS - 1 - CENTER_ROW);
            int slot = (CENTER_ROW + rowOffset) * COLS + (CENTER_COL + colOffset);
            // 中央スロットは原点マーカー(コンパス)専用のため、拠点はずらして表示する
            if (slot == CENTER_SLOT) {
                slot = nearestFreeSlotAroundCenter(slotMap);
            }
            slotMap.computeIfAbsent(slot, k -> new ArrayList<>()).add(loc);
        }

        String dimensionLabel = dimensionLabel(environment);
        EfpGuiHolder holder = new EfpGuiHolder(slotMap);
        Inventory inv = Bukkit.createInventory(holder, ROWS * COLS,
                Component.text("みんなの拠点マップ [" + dimensionLabel + "] (北が上)", NamedTextColor.DARK_AQUA));
        holder.setInventory(inv);

        for (Map.Entry<Integer, List<BaseLocation>> entry : slotMap.entrySet()) {
            inv.setItem(entry.getKey(), createItem(entry.getValue()));
        }

        // 原点マーカーは常に中央付近に固定
        inv.setItem(CENTER_SLOT, compassItem(scale, dimensionLabel));

        return inv;
    }

    private static String dimensionLabel(String environment) {
        if (environment == null) {
            return "不明";
        }
        return switch (environment) {
            case "NETHER" -> "ネザー";
            case "THE_END" -> "エンド";
            default -> "オーバーワールド";
        };
    }

    /** 中央スロットと重なった拠点を、隣接する空きスロットへずらして配置する */
    private static int nearestFreeSlotAroundCenter(Map<Integer, List<BaseLocation>> slotMap) {
        int[] candidateOffsets = { 1, -1, COLS, -COLS, COLS + 1, COLS - 1, -COLS + 1, -COLS - 1 };
        for (int offset : candidateOffsets) {
            int candidate = CENTER_SLOT + offset;
            int candidateCol = candidate % COLS;
            // 行をまたいでの折り返しを避ける
            if (Math.abs(candidateCol - CENTER_COL) > 1) {
                continue;
            }
            if (candidate < 0 || candidate >= ROWS * COLS) {
                continue;
            }
            if (!slotMap.containsKey(candidate)) {
                return candidate;
            }
        }
        // すべて埋まっている場合は中央スロットにまとめて表示(コンパスは上書きされる)
        return CENTER_SLOT;
    }

    private static double computeScale(List<BaseLocation> locations) {
        double maxAbsX = 0;
        double maxAbsZ = 0;
        for (BaseLocation loc : locations) {
            maxAbsX = Math.max(maxAbsX, Math.abs(loc.getX()));
            maxAbsZ = Math.max(maxAbsZ, Math.abs(loc.getZ()));
        }
        double scaleX = maxAbsX / CENTER_COL;
        double scaleZ = maxAbsZ / Math.max(CENTER_ROW, ROWS - 1 - CENTER_ROW);
        return Math.max(MIN_SCALE, Math.max(scaleX, scaleZ));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static ItemStack createItem(List<BaseLocation> group) {
        BaseLocation first = group.get(0);
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, Math.min(group.size(), 64));
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        OfflinePlayer owner = Bukkit.getOfflinePlayer(first.getOwnerId());
        meta.setOwningPlayer(owner);

        if (group.size() == 1) {
            meta.displayName(Component.text(first.getName(), NamedTextColor.GOLD));
        } else {
            meta.displayName(Component.text(group.size() + "件の拠点", NamedTextColor.GOLD));
        }

        List<Component> lore = new ArrayList<>();
        for (BaseLocation loc : group) {
            lore.add(Component.text(loc.getName() + " (" + loc.getOwnerName() + ")", NamedTextColor.GRAY));
            lore.add(Component.text(String.format("  %s X:%d Y:%d Z:%d",
                    loc.getDimensionLabel(), loc.getX(), loc.getY(), loc.getZ()), NamedTextColor.DARK_GRAY));
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack compassItem(double scale, String dimensionLabel) {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("原点 (X:0 Z:0) - " + dimensionLabel, NamedTextColor.YELLOW));
        meta.lore(List.of(
                Component.text("上=北 / 下=南 / 右=東 / 左=西", NamedTextColor.GRAY),
                Component.text(String.format("1マス ≈ %.0fブロック", scale), NamedTextColor.GRAY)
        ));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof EfpGuiHolder holder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        List<BaseLocation> group = holder.getLocationsAt(event.getRawSlot());
        if (group == null) {
            return;
        }
        for (BaseLocation loc : group) {
            player.sendMessage(Component.text(
                    String.format("%s (%s) - [%s] X:%d Y:%d Z:%d",
                            loc.getName(), loc.getOwnerName(), loc.getDimensionLabel(),
                            loc.getX(), loc.getY(), loc.getZ()),
                    NamedTextColor.AQUA));
        }
    }
}