package edu.whimc.observations.libraries;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import edu.whimc.observations.utils.Utils;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

public final class SignMenuFactory {

    private final Plugin plugin;

    private final Map<UUID, Menu> inputs = new HashMap<>();

    public SignMenuFactory(Plugin plugin) {
        this.plugin = plugin;
        this.listen();
    }

    public Menu newMenu(List<String> text) {
        return new Menu(text);
    }

    private void listen() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this.plugin, PacketType.Play.Client.UPDATE_SIGN) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();

                Menu menu = inputs.remove(player.getUniqueId());

                if (menu == null) {
                    return;
                }
                event.setCancelled(true);

                boolean success = menu.response.test(player, event.getPacket().getStringArrays().read(0));

                if (!success && menu.reopenIfFail && !menu.forceClose) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> menu.open(player), 2L);
                }
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        player.sendBlockChange(menu.location, menu.location.getBlock().getBlockData());
                    }
                }, 2L);
            }
        });
    }

    public final class Menu {

        private final List<String> text;

        private BiPredicate<Player, String[]> response;
        private boolean reopenIfFail;

        private Location location;

        private boolean forceClose;

        Menu(List<String> text) {
            this.text = text;
        }

        public Menu reopenIfFail(boolean value) {
            this.reopenIfFail = value;
            return this;
        }

        public Menu response(BiPredicate<Player, String[]> response) {
            this.response = response;
            return this;
        }

        public void open(Player player) {
            Objects.requireNonNull(player, "player");
            if (!player.isOnline()) {
                return;
            }
            location = player.getLocation();
            location.setY(location.getBlockY() - 4);

            player.sendBlockChange(location, Material.OAK_SIGN.createBlockData());
            player.sendSignChange(
                    location,
                    text.stream().map(Utils::color).toList().toArray(new String[4])
            );

            PacketContainer openSign = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR);
            BlockPosition position = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            openSign.getBlockPositionModifier().write(0, position);
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, openSign);
            } catch (InvocationTargetException exception) {
                exception.printStackTrace();
            }

            inputs.put(player.getUniqueId(), this);
        }

        /**
         * closes the menu. if force is true, the menu will close and will ignore the reopen
         * functionality. false by default.
         *
         * @param player the player
         * @param force  decides whether or not it will reopen if reopen is enabled
         */
        public void close(Player player, boolean force) {
            this.forceClose = force;
            if (player.isOnline()) {
                player.closeInventory();
            }
        }

        public void close(Player player) {
            close(player, false);
        }
    }
}
