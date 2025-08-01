package me.trouper.dupealias.server.gui.dupe.sub;

import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.alias.utils.FormatUtils;
import me.trouper.alias.utils.ItemBuilder;
import me.trouper.alias.utils.SoundPlayer;
import me.trouper.dupealias.DupeAlias;
import me.trouper.dupealias.DupeContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class DupeReplicatorGui extends AbstractDupeGui<DupeReplicatorGui.ReplicatorSession> implements DupeContext {

    private final int[] inputRing = {1, 2, 3, 12, 21, 20, 19, 10};
    private final int[] outputRing = {5, 6, 7, 16, 25, 24, 23, 14};
    private final ItemStack emptyLightBlue = EMPTY(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
    private final ItemStack emptyCyan = EMPTY(Material.CYAN_STAINED_GLASS_PANE);
    private final ItemStack emptyWhite = EMPTY(Material.WHITE_STAINED_GLASS_PANE);
    private final ItemStack emptyBlue = EMPTY(Material.BLUE_STAINED_GLASS_PANE);

    @Override
    protected ReplicatorSession createSession(Player player) {
        return new ReplicatorSession(player, player.getInventory().getItemInMainHand());
    }

    @Override
    public ReplicatorSession getSession(Player player) {
        ReplicatorSession session = super.getSession(player);
        session.setDelayTicks(getDupe().getPermissionValue(player, "dupealias.gui.replicator.refresh.", getConfig().replicator.baseRefreshDelayTicks,false));
        session.setCooldownTicks(getDupe().getPermissionValue(player, "dupealias.gui.replicator.cooldown.", getConfig().replicator.baseInputCooldownTicks,false));
        session.open();
        return session;
    }

    public class ReplicatorSession extends AbstractDupeSession {
        private ItemStack input;
        private int timer = 0;
        private int delayTicks;
        private int currentDelayTicks = 0;
        private int cooldownTicks;
        private int currentCooldownTicks = 0;
        private boolean ready = false;

        public ReplicatorSession(Player owner, ItemStack input) {
            super(owner, DupeAlias.getDupeAlias().getDictionary().guiDupe.guiReplicator.title, 3);
            getVerbose().send("Creating a new replicator with input of {0}", input.getType().name());
            setInput(input);
            this.delayTicks = getDupe().getPermissionValue(owner, "dupealias.gui.replicator.refresh.", getConfig().replicator.baseRefreshDelayTicks,false);
            this.cooldownTicks = getDupe().getPermissionValue(owner, "dupealias.gui.replicator.cooldown.", getConfig().replicator.baseInputCooldownTicks,false);
        }

        @Override
        protected QuickGui buildGui(String title, int rows) {
            return QuickGui.create()
                    .titleMini(title)
                    .rows(rows)
                    .fillSlots(EMPTY(Material.BLACK_STAINED_GLASS_PANE), null, 0, 9, 18, 4, 13, 22, 8, 17, 26) // Background
                    .fillSlots(emptyLightBlue, null, inputRing)
                    .fillSlots(EMPTY(Material.BLUE_STAINED_GLASS_PANE), null, outputRing)
                    .item(15, createPopulatedItem(null,1))
                    .onGlobalClick((g, e) -> {
                        if (e.getClickedInventory() != null && e.getSlot() == 15) {
                            ItemStack clicked = e.getCurrentItem();
                            ItemStack cursor = e.getCursor();
                            ItemStack slot = e.getClickedInventory().getItem(e.getSlot());
                            if (shouldBlockClick(clicked) || shouldBlockClick(cursor) || shouldBlockClick(slot)) {
                                e.setCancelled(true);
                                return;
                            }

                            ready = false;
                            currentDelayTicks = 0;
                            SoundPlayer.play(getOwner(), Sound.ENTITY_ITEM_PICKUP, 0.8F, 1.2F);

                            e.setCancelled(false);
                            return;
                        }
                        if (e.getSlot() == 11) {
                            if (currentCooldownTicks > 0) {
                                SoundPlayer.play(getOwner(),Sound.BLOCK_NOTE_BLOCK_BASS);
                                return;
                            } else {
                                currentCooldownTicks = cooldownTicks;
                            }

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
                    .clickSound(null,0,0)
                    .onClose((g, e) -> close())
                    .build();
        }

        @Override
        protected void tick() {
            timer++;

            Inventory inv = getGui().getInventory();

            if (timer % 2 == 0) for (int i = 0; i < outputRing.length; i++) {
                int currentSlot = outputRing[i];
                int nextSlot = outputRing[(i+1) % outputRing.length];
                ItemStack currentItem = inv.getItem(currentSlot);
                if (currentItem != null && currentItem.isSimilar(emptyLightBlue)) {
                    inv.setItem(currentSlot,emptyCyan);
                    inv.setItem(nextSlot,emptyLightBlue);
                    break;
                }
            }

            ItemStack output = inv.getItem(15);

            if (currentCooldownTicks > 1) {
                currentCooldownTicks--;
                inv.setItem(11,createInputItem(null, (double) currentCooldownTicks / (double) cooldownTicks));
            } else if (currentCooldownTicks == 1) {
                currentCooldownTicks--;
                getGui().getInventory().setItem(11, createInputItem(input, 1));
                SoundPlayer.play(getOwner(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1, 0.8F);
            }

            if (input == null || input.getType() == Material.AIR) {
                if (output != null && !output.isSimilar(createPopulatedItem(null,1))) {
                    inv.setItem(15, createPopulatedItem(null,1));
                }
                return;
            }

            if (timer % 20 == 0) {
                SoundPlayer.play(getOwner(), Sound.BLOCK_BEACON_AMBIENT, 0.5F, 1.2F);
            }

            if (!ready) {
                currentDelayTicks++;
                double progress = Math.min(1.0, (double) currentDelayTicks / delayTicks);
                inv.setItem(15, createPopulatedItem(input, progress));
                if (currentDelayTicks >= delayTicks) {
                    ready = true;
                    SoundPlayer.play(getOwner(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1, 0.8F);
                }
                return;
            }

            if (input.isSimilar(output)) return;

            inv.setItem(15, createPopulatedItem(input, 1));
        }

        public boolean setInput(ItemStack newInput) {
            if (getDupe().isUnique(newInput)) {
                SoundPlayer.play(getOwner(), Sound.ENTITY_VILLAGER_NO, 1, 0.8F);
                warningAny(getOwner(), dict().guiDupe.guiReplicator.uniqueItemWarning, FormatUtils.formatEnum(newInput.getType()));
                getGui().getInventory().setItem(11, createInputItem(this.input, 1));
                return false;
            }
            this.input = newInput.clone();
            getGui().getInventory().setItem(11, createInputItem(this.input, 1));
            return true;
        }

        public void setDelayTicks(int delayTicks) {
            this.delayTicks = delayTicks;
        }

        public void setCooldownTicks(int cooldownTicks) {
            this.cooldownTicks = cooldownTicks;
        }

        private void activateRings(Inventory inv) {
            for (int i : inputRing) {
                inv.setItem(i,emptyWhite);
            }
            for (int i : outputRing) {
                inv.setItem(i,emptyCyan);
            }
            inv.setItem(outputRing[0], emptyLightBlue);
        }

        private void deactivateRings(Inventory inv) {
            for (int i : inputRing) {
                inv.setItem(i, emptyLightBlue);
            }
            for (int i : outputRing) {
                inv.setItem(i,emptyBlue);
            }
        }
    }

    private ItemStack createInputItem(ItemStack input, double cooldownProgress) {
        if (cooldownProgress < 1) {
            return ItemBuilder.of(EMPTY(Material.BARRIER))
                    .displayName(dict().guiDupe.guiReplicator.inputItemDisplayName)
                    .loreComponent(getTextSystem().createProgressBar(cooldownProgress, '|',30, TextColor.color(0xFF895A),TextColor.color(0x6F6F6F)))
                    .loreMiniMessage(dict().guiDupe.guiReplicator.inputItemLoreCooldown)
                    .build();
        }
        if (input == null || input.getType() == Material.AIR) {
            return ItemBuilder.headOfTexture("http://textures.minecraft.net/texture/86bd920b402815ad89018df82977be9f7ea19e799ecf016f7f0da4ab47ca23c5")
                    .displayName(dict().guiDupe.guiReplicator.inputItemDisplayName)
                    .loreMiniMessage(dict().guiDupe.guiReplicator.inputItemLoreNoItemSelected)
                    .build();
        } else {
            return ItemBuilder.headOfTexture("http://textures.minecraft.net/texture/32d250f5336449b32bfe990bdfd307a1b39ae5ca07e9a1593b1bb6ed33ec14ba")
                    .displayName(dict().guiDupe.guiReplicator.inputItemDisplayName)
                    .loreMiniMessage(dict().guiDupe.guiReplicator.inputItemLoreItemSelected.stream()
                            .map(line->line.replace("{0}", FormatUtils.formatEnum(input.getType())))
                            .toList())
                    .build();
        }
    }
}