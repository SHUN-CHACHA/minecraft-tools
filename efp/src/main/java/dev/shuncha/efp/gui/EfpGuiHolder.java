package dev.shuncha.efp.gui;

import dev.shuncha.efp.model.BaseLocation;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;
import java.util.Map;

public class EfpGuiHolder implements InventoryHolder {

    private final Map<Integer, List<BaseLocation>> slotMap;
    private Inventory inventory;

    public EfpGuiHolder(Map<Integer, List<BaseLocation>> slotMap) {
        this.slotMap = slotMap;
    }

    public List<BaseLocation> getLocationsAt(int slot) {
        return slotMap.get(slot);
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}