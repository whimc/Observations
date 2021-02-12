package edu.whimc.observationdisplayer.commands.observetemplate.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import edu.whimc.observationdisplayer.ObservationDisplayer;
import edu.whimc.observationdisplayer.utils.Utils;

public final class TemplateGui implements Listener {

    private String inventoryName;
    private int inventorySize;

    private ItemStack fillerItem;

    private ItemStack cancelItem;
    private int cancelPosition;

    private Inventory inventory;

    private ObservationDisplayer plugin;
    private TemplateGuiManager manager;

    public TemplateGui(ObservationDisplayer plugin, TemplateGuiManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        loadTemplateInventory();
    }

    private void loadTemplateInventory() {
        this.inventoryName = getString(Path.INVENTORY_NAME);
        this.inventorySize = 9 * getInt(Path.INVENTORY_ROWS);

        this.fillerItem = new ItemStack(Material.matchMaterial(getString(Path.FILLER_ITEM)));
        setName(this.fillerItem, " ");

        this.cancelItem = new ItemStack(Material.matchMaterial(getString(Path.CANCEL_ITEM)));
        this.cancelPosition = getInt(Path.CANCEL_POSITION);
        setName(this.cancelItem, getString(Path.CANCEL_NAME));
    }

    public Inventory createTemplateInventory(World world) {
        Inventory result = Bukkit.createInventory(null, this.inventorySize, Utils.color(this.inventoryName));

        // Add in filler items
        for (int slot = 0; slot < result.getSize(); slot++) {
            result.setItem(slot, this.fillerItem);
        }

        // Add cancel item
        result.setItem(this.cancelPosition, this.cancelItem);

        // Add template-specific items

        return result;
    }

    public void openTemplateInventory(Player player) {
        player.openInventory(this.inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory() != this.inventory) {
            return;
        }

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked == this.fillerItem) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        if (clicked.equals(this.cancelItem)) {
            event.getWhoClicked().closeInventory();
            Utils.msg(event.getWhoClicked(), "&aInventory closed!");
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory() == this.inventory) {
            event.setCancelled(true);
        }
    }

    private void setName(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Utils.color(name));
        item.setItemMeta(meta);
    }

    public String getString(Path path) {
        return this.plugin.getConfig().getString(Path.ROOT.getPath() + path.getPath());
    }

    public int getInt(Path path) {
        return this.plugin.getConfig().getInt(Path.ROOT.getPath() + path.getPath());
    }

    private enum Path {

        ROOT("template-gui."),

        FILLER_ITEM("filler-item"),

        INVENTORY_NAME("inventory-name"),
        INVENTORY_ROWS("rows"),

        CANCEL_ITEM("cancel.item"),
        CANCEL_POSITION("cancel.position"),
        CANCEL_NAME("cancel.name"),
        ;

        private String path;

        private Path(String path) {
            this.path = path;
        }

        public String getPath() {
            return this.path;
        }

    }

}
