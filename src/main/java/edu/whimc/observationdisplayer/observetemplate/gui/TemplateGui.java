package edu.whimc.observationdisplayer.observetemplate.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import edu.whimc.observationdisplayer.ObservationDisplayer;
import edu.whimc.observationdisplayer.observetemplate.models.ObservationType;
import edu.whimc.observationdisplayer.utils.Utils;

public final class TemplateGui implements Listener {

    private String inventoryName;
    private int inventorySize;

    private ItemStack fillerItem;

    private ItemStack cancelItem;
    private int cancelPosition;

    private Inventory inventory;

    private Map<Integer, ObservationType> templateSlots = new HashMap<>();

    private Map<ObservationType, Consumer<Player>> templateActions = new HashMap<>();

    private ObservationDisplayer plugin;

    public TemplateGui(ObservationDisplayer plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
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

        this.inventory = Bukkit.createInventory(null, this.inventorySize, Utils.color(this.inventoryName));

        // Add in filler items
        for (int slot = 0; slot < this.inventory.getSize(); slot++) {
            this.inventory.setItem(slot, this.fillerItem);
        }

        // Add cancel item
        this.inventory.setItem(this.cancelPosition, this.cancelItem);

        // Add template-specific items
        for (ObservationType type : ObservationType.values()) {
            String pathRoot = "templates." + type + ".gui.";
            FileConfiguration config = this.plugin.getConfig();

            ItemStack item = new ItemStack(Material.matchMaterial(config.getString(pathRoot + "item")));
            int position = config.getInt(pathRoot + "position");
            setName(item, config.getString(pathRoot + "name"));
            setLore(item, config.getStringList(pathRoot + "lore"));

            this.templateSlots.put(position, type);
            this.inventory.setItem(position, item);
        }
    }

    public void addConsumer(ObservationType type, Consumer<Player> action) {
        this.templateActions.put(type, action);
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

        // Only care about clicks in our inventory
        if (event.getClickedInventory() != this.inventory) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();

        // Do nothing if they didn't click anything important
        if (clicked == null || clicked == this.fillerItem) {
            return;
        }

        // Only care if the clicker was a player
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        // Close the inventory if they click the cancel button
        if (clicked.equals(this.cancelItem)) {
            event.getWhoClicked().closeInventory();
            return;
        }

        ObservationType type = this.templateSlots.getOrDefault(event.getSlot(), null);
        if (type == null) {
            return;
        }

        // Close the inventory and execute the action for this template type
        Player player = (Player) event.getWhoClicked();
        player.closeInventory();
        this.templateActions.getOrDefault(type, p -> {}).accept(player);
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

    private void setLore(ItemStack item, List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore
                .stream()
                .map(Utils::color)
                .collect(Collectors.toList()));
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
