package org.libremc.weLoveCapitalism;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ItemGUI implements Listener {
    private final Inventory inv;

    public ItemGUI(ItemStack item) {
        inv = Bukkit.createInventory(null, 9, "Chestshop preview");

        ItemStack display = item.clone();

        inv.setItem(4, display);

        Bukkit.getServer().getPluginManager().registerEvents(this, WeLoveCapitalism.getInstance()); // register the InventoryDragEvent and InventoryClickEvent

    }

    public void openInventory(final HumanEntity ent) {
        ent.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!e.getInventory().equals(inv)) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent e) {
        if (e.getInventory().equals(inv)) {
            e.setCancelled(true);
        }
    }
}