package me.trouper.dupealias.server.gui.dupe;

import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.alias.utils.FormatUtils;
import me.trouper.alias.utils.ItemBuilder;
import me.trouper.alias.utils.SoundPlayer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ReplicatorGui extends AbstractDupeGui<ReplicatorGui.ReplicatorSession> {

    @Override
    protected ReplicatorSession createSession(Player player) {
        return new ReplicatorSession(player, player.getInventory().getItemInMainHand());
    }

    public class ReplicatorSession extends AbstractDupeSession {
        private ItemStack input;
        private int ticks = 0;

        public ReplicatorSession(Player owner, ItemStack input) {
            super(owner, "<gradient:#cc22ff:#cc99ff><bold>REPLICATOR</gradient>", 3);
            getVerbose().send("Creating a new replicator with input of {0}", input.getType().name());
            setInput(input);
        }

        @Override
        protected QuickGui buildGui(String title, int rows) {
            return QuickGui.create()
                    .titleMini(title)
                    .rows(rows)
                    .fillSlots(EMPTY(Material.BLACK_STAINED_GLASS_PANE), null, 0, 9, 18, 4, 13, 22, 8, 17, 26) // Background
                    .fillSlots(EMPTY(Material.LIGHT_BLUE_STAINED_GLASS_PANE), null, 1, 2, 3, 10, 12, 19, 20, 21) // Input ring
                    .fillSlots(EMPTY(Material.BLUE_STAINED_GLASS_PANE), null, 5, 6, 7, 14, 16, 23, 24, 25) // Replication ring
                    .item(15, new ItemStack(Material.AIR))
                    .onGlobalClick((g, e) -> {
                        if (e.getSlot() == 15) {
                            e.setCancelled(false);
                            return;
                        }
                        if (e.getSlot() == 11) {
                            Inventory inv = getGui().getInventory();
                            ItemStack cursor = e.getCursor();
                            if (cursor == null || cursor.getType() == Material.AIR) {
                                setInput(new ItemStack(Material.AIR));
                                SoundPlayer.play(getOwner(),Sound.ITEM_BUNDLE_REMOVE_ONE);
                                getOwner().stopSound(Sound.BLOCK_BEACON_AMBIENT);
                                deactivateRings(inv);
                            } else {
                                if (setInput(cursor)) {
                                    SoundPlayer.play(getOwner(),Sound.ITEM_BUNDLE_INSERT);
                                    activateRings(inv);
                                }
                            }
                        }
                    })
                    .onClose((g, e) -> close())
                    .build();
        }

        @Override
        protected long getTickDelay(Player player) {
            return 1;
        }

        @Override
        protected void tick() {
            ticks++;
            Inventory inv = getGui().getInventory();
            ItemStack output = inv.getItem(15);

            if (input == null || input.getType() == Material.AIR) {
                if (output != null && output.getType() != Material.AIR) {
                    inv.setItem(15, new ItemStack(Material.AIR));
                }
                return;
            }

            if (ticks % 20 == 0) {
                SoundPlayer.play(getOwner(), Sound.BLOCK_BEACON_AMBIENT, 0.5F, 1.2F);
            }

            if (input.isSimilar(output)) return;

            inv.setItem(15, createPopulatedItem(input));
            SoundPlayer.play(getOwner(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1, 0.8F);
        }

        public boolean setInput(ItemStack newInput) {
            if (getDupe().isUnique(newInput)) {
                SoundPlayer.play(getOwner(), Sound.ENTITY_VILLAGER_NO, 1, 0.8F);
                warningAny(getOwner(), "Your {0} is or contains a unique item!", FormatUtils.formatEnum(newInput.getType()));
                return false;
            }
            this.input = newInput.clone();
            getGui().getInventory().setItem(11, createInputItem(this.input));
            return true;
        }

        public ItemStack getInput() {
            return input;
        }
    }

    private ItemStack createInputItem(ItemStack input) {
        if (input == null || input.getType() == Material.AIR) {
            return ItemBuilder.headOfTexture("http://textures.minecraft.net/texture/86bd920b402815ad89018df82977be9f7ea19e799ecf016f7f0da4ab47ca23c5")
                    .displayName("<gold>Replicator Input")
                    .loreMiniMessage("<gray>No item selected.")
                    .loreMiniMessage("<dark_red>Drag an item into this slot.")
                    .build();
        } else {
            return ItemBuilder.headOfTexture("http://textures.minecraft.net/texture/32d250f5336449b32bfe990bdfd307a1b39ae5ca07e9a1593b1bb6ed33ec14ba")
                    .displayName("<gold>Replicator Input")
                    .loreMiniMessage("<white>Item: " + FormatUtils.formatEnum(input.getType()))
                    .loreMiniMessage("<dark_green>Replication Ready!")
                    .build();
        }
    }

    private void activateRings(Inventory inv) {
        inv.setItem(1, EMPTY(Material.WHITE_STAINED_GLASS_PANE));
        inv.setItem(2, EMPTY(Material.WHITE_STAINED_GLASS_PANE));
        inv.setItem(3, EMPTY(Material.WHITE_STAINED_GLASS_PANE));
        inv.setItem(10, EMPTY(Material.WHITE_STAINED_GLASS_PANE));
        inv.setItem(12, EMPTY(Material.WHITE_STAINED_GLASS_PANE));
        inv.setItem(19, EMPTY(Material.WHITE_STAINED_GLASS_PANE));
        inv.setItem(20, EMPTY(Material.WHITE_STAINED_GLASS_PANE));
        inv.setItem(21, EMPTY(Material.WHITE_STAINED_GLASS_PANE));

        inv.setItem(5, EMPTY(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
        inv.setItem(6, EMPTY(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
        inv.setItem(7, EMPTY(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
        inv.setItem(14, EMPTY(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
        inv.setItem(16, EMPTY(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
        inv.setItem(23, EMPTY(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
        inv.setItem(24, EMPTY(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
        inv.setItem(25, EMPTY(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
    }

    private void deactivateRings(Inventory inv) {
        inv.setItem(1, EMPTY(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
        inv.setItem(2, EMPTY(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
        inv.setItem(3, EMPTY(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
        inv.setItem(10, EMPTY(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
        inv.setItem(12, EMPTY(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
        inv.setItem(19, EMPTY(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
        inv.setItem(20, EMPTY(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
        inv.setItem(21, EMPTY(Material.LIGHT_BLUE_STAINED_GLASS_PANE));

        inv.setItem(5, EMPTY(Material.BLUE_STAINED_GLASS_PANE));
        inv.setItem(6, EMPTY(Material.BLUE_STAINED_GLASS_PANE));
        inv.setItem(7, EMPTY(Material.BLUE_STAINED_GLASS_PANE));
        inv.setItem(14, EMPTY(Material.BLUE_STAINED_GLASS_PANE));
        inv.setItem(16, EMPTY(Material.BLUE_STAINED_GLASS_PANE));
        inv.setItem(23, EMPTY(Material.BLUE_STAINED_GLASS_PANE));
        inv.setItem(24, EMPTY(Material.BLUE_STAINED_GLASS_PANE));
        inv.setItem(25, EMPTY(Material.BLUE_STAINED_GLASS_PANE));
    }

    public class ReplicatorConfig {
        final int baseRefreshDelayTicks = 1;
        final int baseInputCooldownTicks = 20;
        final Map<String,Integer> permissionRefreshDelayTicks = new HashMap<>();
        final Map<String,Integer> permissionInputCooldownTicks = new HashMap<>();
    }
}