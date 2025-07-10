package me.trouper.dupealias.server.gui.admin;

import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.server.ItemTag;
import me.trouper.dupealias.server.gui.CommonItems;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class AdminGui implements DupeContext, CommonItems {

    public void openMainGui(Player player) {
        QuickGui gui = QuickGui.create()
                .titleMini("<gradient:#6b6bff:#9999ff><bold>DupeAlias Admin Panel</gradient>")
                .rows(5)

                .item(11, ItemBuilder.create(player.getInventory().getItemInMainHand().isEmpty() ? Material.BARRIER : Material.DIAMOND_SWORD)
                                .displayName("<gradient:#4ecdc4:#45b7d1><bold>Held Item Actions</bold></gradient>")
                                .loreMiniMessage(
                                        "<gray>Manage tags for the item",
                                        "<gray>you're currently holding",
                                        "",
                                        "<yellow>▶ <white>Click to open menu"
                                )
                                .hideAllFlags()
                                .build(),
                        (q, event) -> openHeldItemGui(player))

                .item(13, ItemBuilder.create(Material.BOOKSHELF)
                                .displayName("<gradient:#ff6b6b:#ffa726><bold>Global Material Tags</bold></gradient>")
                                .loreMiniMessage(
                                        "<gray>Configure global tags that apply",
                                        "<gray>to all items of specific materials",
                                        "",
                                        "<yellow>▶ <white>Click to open menu"
                                )
                                .build(),
                        (q, event) -> openGlobalMaterialGui(player,player.getInventory().getItemInMainHand().getType()))

                .item(15, ItemBuilder.create(Material.KNOWLEDGE_BOOK)
                                .displayName("<gradient:#cb59b6:#8e44ad><bold>Information & Help</bold></gradient>")
                                .loreMiniMessage(
                                        "<gray>Learn about item tags and",
                                        "<gray>how to use this system",
                                        "",
                                        "<yellow>▶ <white>Click to view help"
                                )
                                .build(),
                        (q, event) -> openHelpGui(player))

                .item(29, ItemBuilder.create(Material.COMPARATOR)
                        .displayName("<gradient:#ff6bff:#ffa7ff><bold>Configuration</bold></gradient>")
                        .loreMiniMessage(
                                "<gray>Modify plugin parameters",
                                "<gray>name and colors",
                                "",
                                "<yellow>▶ <white>Click to open config"
                        )
                        .build(), (q,event) -> new ConfigGui().open(player, q))

                .item(31, createPreviewItem(player.getInventory().getItemInMainHand()))

                .item(33, ItemBuilder.create(Material.DIAMOND)
                        .displayName("<#AAAAFF><bold>Dupe<#00DDFF>Alias</bold> <white>Credits")
                        .loreMiniMessage(
                                "<dark_gray><bold>|</bold><gray> Built with Alias Development Kit",
                                "<dark_gray><bold>|</bold><gray>",
                                "<dark_gray><bold>|</bold> <gradient:#e38c01:#eccd00:#FFFFFF:#62afdd:#1f3857>Written by obvWolf</gradient>",
                                " ",
                                "<dark_gray>Copyright © 2025 DupeAlias",
                                "<dark_gray>Do Not Redistribute"
                        )
                        .build())

                .fillEmpty(EMPTY())
                .clickSound(Sound.UI_BUTTON_CLICK, 0.7f, 1.2f)
                .build();

        gui.open(player);
    }

    public void openHeldItemGui(Player player) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();

        if (heldItem.getType().isAir()) {
            errorAny(player, "You must be holding an item to use this menu!");
            return;
        }

        QuickGui gui = QuickGui.create()
                .titleMini("<gradient:#4ecdc4:#45b7d1><bold>Held Item: " + heldItem.getType().name() + "</bold></gradient>")
                .rows(4)
                .fillBorder(EMPTY(Material.LIGHT_BLUE_STAINED_GLASS_PANE))

                .item(0, BACK(),
                        (g, e) -> openMainGui(player))

                .item(13, ItemBuilder.create(heldItem.getType())
                        .displayName("<white><bold>" + heldItem.getType().name() + "</bold>")
                        .loreMiniMessage(getItemTagStatus(heldItem))
                        .build())

                .item(11, ItemBuilder.create(Material.EMERALD)
                                .displayName("<green><bold>Add UNIQUE Tag</bold>")
                                .loreMiniMessage(Arrays.asList(
                                        "<gray>Makes this specific item",
                                        "<red>unable to be duplicated",
                                        "",
                                        "<yellow>▶ <white>Left click to apply tag",
                                        "<yellow>▶ <white>Right click to remove tag",
                                        "<yellow>▶ <white>Shift click to set tag to false"
                                ))
                                .build(),
                        (g, e) -> tagHeldItem(player, ItemTag.UNIQUE, e.getClick()))

                .item(20, ItemBuilder.create(Material.BARRIER)
                                .displayName("<red><bold>Add FINAL Tag</bold>")
                                .loreMiniMessage(Arrays.asList(
                                        "<gray>Makes this specific item",
                                        "<red>unable to be modified",
                                        "",
                                        "<yellow>▶ <white>Left click to apply tag",
                                        "<yellow>▶ <white>Right click to remove tag",
                                        "<yellow>▶ <white>Shift click to set tag to false"
                                ))
                                .build(),
                        (g, e) -> tagHeldItem(player, ItemTag.FINAL, e.getClick()))

                .item(15, ItemBuilder.create(Material.WATER_BUCKET)
                                .displayName("<blue><bold>Add INFINITE Tag</bold>")
                                .loreMiniMessage(Arrays.asList(
                                        "<gray>Makes this specific item",
                                        "<green>always have max stack size",
                                        "",
                                        "<yellow>▶ <white>Left click to apply tag",
                                        "<yellow>▶ <white>Right click to remove tag",
                                        "<yellow>▶ <white>Shift click to set tag to false"
                                ))
                                .build(),
                        (g, e) -> tagHeldItem(player, ItemTag.INFINITE, e.getClick()))

                .item(24, ItemBuilder.create(Material.STRUCTURE_VOID)
                                .displayName("<dark_purple><bold>Add PROTECTED Tag</bold>")
                                .loreMiniMessage(Arrays.asList(
                                        "<gray>Makes this specific item",
                                        "<red>not able to be manually created",
                                        "",
                                        "<yellow>▶ <white>Left click to apply tag",
                                        "<yellow>▶ <white>Right click to remove tag",
                                        "<yellow>▶ <white>Shift click to set tag to false"
                                ))
                                .build(),
                        (g, e) -> tagHeldItem(player, ItemTag.PROTECTED, e.getClick()))

                .item(22, ItemBuilder.create(Material.TNT)
                                .displayName("<dark_red><bold>Remove All Tags</bold>")
                                .loreMiniMessage(Arrays.asList(
                                        "<gray>Removes all tags from",
                                        "<gray>this specific item",
                                        "",
                                        "<red>⚠ <white>This cannot be undone!",
                                        "<yellow>▶ <white>Click to remove tags"
                                ))
                                .build(),
                        (g, e) -> removeAllTagsFromHeld(player))

                .fillEmpty(EMPTY())
                .clickSound(Sound.UI_BUTTON_CLICK, 0.7f, 1.2f)
                .build();

        gui.open(player);
    }

    public void openGlobalMaterialGui(Player player, Material material) {
        if (material == null) {
            material = Material.AIR;
        }
        final Material mat = material;

        QuickGui gui = QuickGui.create()
                .titleMini("<gradient:#ff6b6b:#ffa726><bold>Global Material Tags</bold></gradient>")
                .rows(4)
                .fillBorder(EMPTY(Material.ORANGE_STAINED_GLASS_PANE))

                // Back button
                .item(0, BACK(),
                        (g, e) -> openMainGui(player))

                .item(13, createMaterialTagItem(mat))

                .item(11, ItemBuilder.create(Material.EMERALD_BLOCK)
                                .displayName("<green><bold>Global UNIQUE Tag</bold>")
                                .loreMiniMessage(Arrays.asList(
                                        "<gray>Apply UNIQUE tag to ALL items",
                                        "<gray>of the held material type",
                                        "",
                                        "<yellow>▶ <white>Left-click to add",
                                        "<yellow>▶ <white>Right-click to remove"
                                ))
                                .build(),
                        (g, e) -> handleGlobalTag(player, mat, ItemTag.UNIQUE, e.isLeftClick()))

                .item(20, ItemBuilder.create(Material.REDSTONE_BLOCK)
                                .displayName("<red><bold>Global FINAL Tag</bold>")
                                .loreMiniMessage(Arrays.asList(
                                        "<gray>Apply FINAL tag to ALL items",
                                        "<gray>of the held material type",
                                        "",
                                        "<yellow>▶ <white>Left-click to add",
                                        "<yellow>▶ <white>Right-click to remove"
                                ))
                                .build(),
                        (g, e) -> handleGlobalTag(player, mat, ItemTag.FINAL, e.isLeftClick()))

                .item(15, ItemBuilder.create(Material.LAPIS_BLOCK)
                                .displayName("<blue><bold>Global INFINITE Tag</bold>")
                                .loreMiniMessage(Arrays.asList(
                                        "<gray>Apply INFINITE tag to ALL items",
                                        "<gray>of the held material type",
                                        "",
                                        "<yellow>▶ <white>Left-click to add",
                                        "<yellow>▶ <white>Right-click to remove"
                                ))
                                .build(),
                        (g, e) -> handleGlobalTag(player, mat, ItemTag.INFINITE, e.isLeftClick()))

                .item(24, ItemBuilder.create(Material.STRUCTURE_BLOCK)
                                .displayName("<dark_purple><bold>Global PROTECTED Tag</bold>")
                                .loreMiniMessage(Arrays.asList(
                                        "<gray>Apply PROTECTED tag to ALL items",
                                        "<gray>of the held material type",
                                        "",
                                        "<yellow>▶ <white>Left-click to add",
                                        "<yellow>▶ <white>Right-click to remove"
                                ))
                                .build(),
                        (g, e) -> handleGlobalTag(player, mat, ItemTag.PROTECTED, e.isLeftClick()))

                .item(22, ItemBuilder.create(Material.COAL_BLOCK)
                                .displayName("<dark_gray><bold>Material Browser</bold>")
                                .loreMiniMessage(Arrays.asList(
                                        "<gray>Browse and manage tags",
                                        "<gray>for any material type",
                                        "",
                                        "<yellow>▶ <white>Click to open browser"
                                ))
                                .build(),
                        (g, e) -> openMaterialBrowser(player))

                .fillEmpty(EMPTY())
                .clickSound(Sound.UI_BUTTON_CLICK, 0.7f, 1.2f)
                .build();

        gui.open(player);
    }

    public void openHelpGui(Player player) {
        QuickGui gui = QuickGui.create()
                .titleMini("<gradient:#9b59b6:#8e44ad><bold>DupeAlias Help</bold></gradient>")
                .rows(6)
                .fillBorder(EMPTY(Material.PURPLE_STAINED_GLASS_PANE))

                .item(0, BACK(),
                        (g, e) -> openMainGui(player))

                .item(20, ItemBuilder.create(Material.EMERALD)
                        .displayName("<green><bold>UNIQUE Tag</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<white>What it does:",
                                "<gray>• Prevents item duplication",
                                "<gray>• Works globally or per item",
                                "",
                                "<white>Use cases:",
                                "<gray>• Crate Keys",
                                "<gray>• Special or rare items",
                                "<gray>• Admin-only gear",
                                "",
                                "<red>⚠ Conflict:",
                                "<gray>• Avoid combining with <red>INFINITE"
                        ))
                        .build())

                .item(21, ItemBuilder.create(Material.BARRIER)
                        .displayName("<red><bold>FINAL Tag</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<white>What it does:",
                                "<gray>• Blocks all item modifications",
                                "<gray>• Prevents renaming, enchanting, etc.",
                                "",
                                "<white>Use cases:",
                                "<gray>• Name-dependent items",
                                "<gray>• Rank kits or prizes",
                                "<gray>• Event rewards",
                                "",
                                "<gray>✔ Can be combined safely with all tags"
                        ))
                        .build())

                .item(22, ItemBuilder.create(Material.WATER_BUCKET)
                        .displayName("<blue><bold>INFINITE Tag</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<white>What it does:",
                                "<gray>• Enforces max stack size (99)",
                                "<gray>• Item instantly refills when used",
                                "",
                                "<white>Use cases:",
                                "<gray>• Infinite building blocks",
                                "<gray>• 'Infinity' for tipped arrows",
                                "<gray>• Creative-like resource flow",
                                "",
                                "<red>⚠ Conflicts:",
                                "<gray>• Avoid combining with <red>UNIQUE",
                                "<gray>• Avoid combining with <red>PROTECTED"
                        ))
                        .build())

                .item(23, ItemBuilder.create(Material.STRUCTURE_VOID)
                        .displayName("<dark_purple><bold>PROTECTED Tag</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<white>What it does:",
                                "<gray>• Blocks all use: crafting, consuming, enchanting",
                                "<gray>• Makes item functionally inert",
                                "<gray>• This does <red>NOT</red> prevent duping",
                                "",
                                "<white>Use cases:",
                                "<gray>• Crate keys or Coupons",
                                "<gray>• Decorative/admin-only items",
                                "",
                                "<red>⚠ Conflict:",
                                "<gray>• Avoid combining with <red>INFINITE"
                        ))
                        .build())

                .item(24, ItemBuilder.create(Material.REDSTONE_TORCH)
                        .displayName("<red><bold>Important Notes</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<white>Things to remember:",
                                "<gray>• Individual tags <i>override</i> global tags",
                                "<gray>• Global tags affect ALL of a material",
                                "<gray>• PROTECTED items are <red>not</red> UNIQUE by default",
                                "<gray>• UNIQUE items can <i>still</i> be duped with <u>external</u> exploits",
                                "",
                                "<white>Tag Combinations:",
                                "<gray>✔ <green>FINAL + PROTECTED</green> = Immutable Inert item",
                                "<gray>✔ <green>PROTECTED + UNIQUE</green> = Inert Dupe-Proof Item",
                                "<gray>❌ <red>INFINITE + UNIQUE</red> = Paradox",
                                "<gray>❌ <red>INFINITE + PROTECTED</red> = Contradiction"
                        ))
                        .build())

                .item(30, ItemBuilder.create(Material.WRITABLE_BOOK)
                        .displayName("<yellow><bold>Individual vs Global Tags</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<white>Individual Tags:",
                                "<gray>• Stored directly on the item",
                                "<gray>• Apply only to that one instance",
                                "<gray>• Use 'Held Item Actions' menu",
                                "",
                                "<white>Global Tags:",
                                "<gray>• Apply to ALL items of a material",
                                "<gray>• Managed via config",
                                "<gray>• Use 'Global Material Tags' menu",
                                "",
                                "<red>⚠ No per-world or per-player support"
                        ))
                        .build())
                .item(32, ItemBuilder.create(Material.COMMAND_BLOCK)
                        .displayName("<gold><bold>Command Equivalents</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<white>This GUI replaces these commands:",
                                "<gray>• <white>/da tag <tag>",
                                "<gray>• <white>/da tag <tag> remove",
                                "<gray>• <white>/da tag <tag> global",
                                "<gray>• <white>/da tag <tag> <material>",
                                "",
                                "<aqua>💡 <white>GUI is easier and safer!"
                        ))
                        .build())

                .item(37, ItemBuilder.create(Material.NAME_TAG)
                        .displayName("<aqua><bold>Tag Glossary</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<white><bold>UNIQUE</bold><gray>: Prevents <i>intended</i> duplication",
                                "<white><bold>FINAL</bold><gray>: Cancels editing/modification",
                                "<white><bold>PROTECTED</bold><gray>: Blocks all use (crafting, consuming)",
                                "<white><bold>INFINITE</bold><gray>: Always max stack size (99)",
                                "",
                                "<gray>Tags can be combined, but some conflict!"
                        ))
                        .build())

                .item(43, ItemBuilder.create(Material.TRIAL_KEY)
                        .displayName("<white><bold>Permissions Guide</bold>")
                        .loreMiniMessage(Arrays.asList(
                                "<white>Access Permissions:",
                                "<gray>• <white>dupealias.dupe<gray> - Use /dupe",
                                "<gray>• <white>dupealias.gui<gray> - Use duplication GUI",
                                "<gray>• Includes replicator, chest, inventory menus",
                                "",
                                "<white>Bypass Permissions:",
                                "<gray>• <white>dupealias.unique.bypass<gray> - Dupe unique items",
                                "<gray>• <white>dupealias.final.bypass<gray> - Modify final items",
                                "<gray>• <white>dupealias.protected.bypass<gray> - Use protected items",
                                "",
                                "<white>Other:",
                                "<gray>• <white>dupealias.infinite<gray> - Use infinite-tagged items",
                                "",
                                "<red>⚠ Misuse Warning:",
                                "<gray>Giving bypass perms to players allows",
                                "<gray>them to ignore tag protections entirely!"
                        ))
                        .build())

                .item(49, createExplainedItem(player.getInventory().getItemInMainHand()))

                .fillEmpty(EMPTY())
                .clickSound(Sound.UI_BUTTON_CLICK, 0.7f, 1.2f)
                .build();

        gui.open(player);
    }


    private ItemStack createExplainedItem(ItemStack item) {
        if (item == null || item.isEmpty()) {
            return ItemBuilder.create(Material.GRAY_STAINED_GLASS_PANE)
                    .displayName("<gray><italic>No item held</italic>")
                    .loreMiniMessage("<aqua>💡 <white>Hold an item to get information on it")
                    .build();
        }

        List<String> lore = new ArrayList<>();
        lore.add("<white><bold>Held Item Explanation:</bold>");

        Set<ItemTag> activeTags = new HashSet<>();

        for (ItemTag tag : ItemTag.values()) {
            boolean global = getDupe().checkGlobalTag(item.getType(), tag);
            boolean hasMeta = item.hasItemMeta() &&
                    item.getItemMeta().getPersistentDataContainer().has(tag.getKey());

            if (hasMeta) {
                Boolean individual = item.getItemMeta()
                        .getPersistentDataContainer()
                        .get(tag.getKey(), PersistentDataType.BOOLEAN);
                if (Boolean.TRUE.equals(individual)) {
                    lore.add("<gray>• <green>" + tag.getName() + "</green> (Individual): " + tag.getDesc());
                    activeTags.add(tag);
                } else {
                    lore.add("<gray>• <red>" + tag.getName() + "</red> (Individually false)");
                    if (global) {
                        lore.add("  <gray>- Global is active: " + tag.getDesc());
                        lore.add("  <gray>- Global is overridden by Individual tag.");
                        activeTags.add(tag);
                    }
                }
            } else if (global) {
                lore.add("<gray>• <yellow>" + tag.getName() + "</yellow> (Global): " + tag.getDesc());
                activeTags.add(tag);
            }
        }

        if (getDupe().isUnique(item)) {
            lore.add("<gray>• Detected UNIQUE by UniqueCheck");
            activeTags.add(ItemTag.UNIQUE);
        }

        if (lore.size() == 1) {
            lore.add("<gray>• No DupeAlias tags apply to this item");
        }

        List<String> conflicts = new ArrayList<>();
        if (activeTags.contains(ItemTag.INFINITE) && activeTags.contains(ItemTag.UNIQUE)) {
            conflicts.add("INFINITE ↔ UNIQUE");
        }
        if (activeTags.contains(ItemTag.INFINITE) && activeTags.contains(ItemTag.PROTECTED)) {
            conflicts.add("INFINITE ↔ PROTECTED");
        }

        if (!conflicts.isEmpty()) {
            lore.add("");
            lore.add("<red>⚠ <bold>Conflicts detected:</bold>");
            for (String c : conflicts) {
                lore.add("<gray>• " + c);
            }
            lore.add("<gray>Consider removing one of the above tags.");
        }

        return ItemBuilder.of(item)
                .displayName("<white><bold>Item Details</bold>")
                .loreMiniMessage(lore)
                .build();
    }


    public void openMaterialBrowser(Player player) {
        QuickGui gui = new MaterialBrowserGui().createGUI(player);
        gui.open(player);
    }

    public ItemStack createMaterialTagItem(Material material) {
        if (material.isAir()) {
            return ItemBuilder.create(Material.GRAY_STAINED_GLASS_PANE)
                    .displayName("<gray><bold>No Material Selected</bold>")
                    .loreMiniMessage(Arrays.asList(
                            "<gray>Hold an item to see",
                            "<gray>its current tag status or",
                            "<gray>select one in the browser",
                            "",
                            "<yellow>💡 <white>Hold an item and reopen this GUI"
                    ))
                    .build();
        }
        List<String> lore = new ArrayList<>();
        lore.add("<white>Material: <yellow>" + material.name());
        lore.add("");

        boolean hasUnique = getDupe().checkGlobalTag(material, ItemTag.UNIQUE);
        boolean hasFinal = getDupe().checkGlobalTag(material, ItemTag.FINAL);
        boolean hasInfinite = getDupe().checkGlobalTag(material, ItemTag.INFINITE);
        boolean hasProtected = getDupe().checkGlobalTag(material, ItemTag.PROTECTED);

        if (hasUnique || hasFinal || hasInfinite || hasProtected) {
            lore.add("<white>Global Tags:");
            if (hasUnique) lore.add("<green>✓ UNIQUE");
            if (hasFinal) lore.add("<red>✓ FINAL");
            if (hasInfinite) lore.add("<blue>✓ INFINITE");
            if (hasProtected) lore.add("<dark_purple>✓ PROTECTED");
        } else {
            lore.add("<gray>No global tags applied");
        }

        lore.add("");
        lore.add("<yellow>▶ <white>Left-click to manage tags");

        return ItemBuilder.of(material)
                .displayName("<white><bold>" + material.name() + "</bold>")
                .loreMiniMessage(lore)
                .build();
    }

    private void tagHeldItem(Player player, ItemTag tag, ClickType click) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();

        if (heldItem.getType().isAir()) {
            errorAny(player, "You must be holding an item to tag it!");
            return;
        }

        switch (click) {
            case LEFT -> {
                getDupe().addTag(heldItem, tag);
                successAny(player, "Added {0} tag to your {1}. {2}", tag.getName(), heldItem.getType(), tag.getDesc());
            }
            case RIGHT -> {
                getDupe().removeTag(heldItem, tag);
                successAny(player, "Removed {0} tag from your {1}.", tag.getName(), heldItem.getType());
            }
            case SHIFT_LEFT, SHIFT_RIGHT -> {
                getDupe().setTag(heldItem, tag, false);
                successAny(player, "Set {0} tag from your {1} to {2}.", tag.getName(), heldItem.getType(), "false");
            }
        }

        player.closeInventory();
        openHeldItemGui(player);
    }

    private void removeAllTagsFromHeld(Player player) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();

        if (heldItem.getType().isAir()) {
            errorAny(player, "You must be holding an item to remove tags from it!");
            return;
        }

        for (ItemTag tag : ItemTag.values()) {
            getDupe().removeTag(heldItem, tag);
        }

        successAny(player, "Removed all tags from your {0}.", heldItem.getType());

        player.closeInventory();
        openHeldItemGui(player);
    }

    private void handleGlobalTag(Player player, Material material, ItemTag tag, boolean isAdd) {
        if (material.isAir()) {
            errorAny(player, "You must have a material selected to use global material tagging!");
            return;
        }

        if (isAdd) {
            if (getDupe().addGlobalTag(material, tag)) {
                successAny(player, "All {0} items are now globally tagged as {1}. {2}", material, tag.getName(), tag.getDesc());
            } else {
                infoAny(player, "All {0} items are already tagged as {1}.", material, tag.getName());
            }
        } else {
            if (getDupe().removeGlobalTag(material, tag)) {
                successAny(player, "Removed global {0} tag from all {1} items.", tag.getName(), material);
            } else {
                infoAny(player, "{0} is not globally tagged as {1}.", material, tag.getName());
            }
        }

        openGlobalMaterialGui(player,material);
    }

    private ItemStack createPreviewItem(ItemStack stack) {
        if (stack.getType().isAir()) {
            return ItemBuilder.create(Material.GRAY_STAINED_GLASS_PANE)
                    .displayName("<gray><bold>No Item Held</bold>")
                    .loreMiniMessage(Arrays.asList(
                            "<gray>Hold an item to see",
                            "<gray>its current tag status",
                            "",
                            "<yellow>💡 <white>Hold an item and reopen this GUI"
                    ))
                    .build();
        }

        return ItemBuilder.create(stack.getType())
                .displayName("<white><bold>Currently Held: " + stack.getType().name() + "</bold>")
                .loreMiniMessage(getItemTagStatus(stack))
                .build();
    }

    private List<String> getItemTagStatus(ItemStack item) {
        List<String> lore = new ArrayList<>();
        lore.add("<white>Item: <yellow>" + item.getType().name());
        lore.add("");

        List<String> individualTags = new ArrayList<>();
        for (ItemTag tag : ItemTag.values()) {
            if (getDupe().hasIndividualTag(item,tag)) {
                individualTags.add("<" + getTagColor(tag) + ">" + (getDupe().checkIndividualTag(item,tag) ? "✔" : "❌") + " " + tag.getName());
            }
        }

        List<String> globalTags = new ArrayList<>();
        for (ItemTag tag : ItemTag.values()) {
            if (getDupe().checkGlobalTag(item.getType(), tag)) {
                globalTags.add("<" + getTagColor(tag) + ">🌍 " + tag.getName());
            }
        }

        if (!individualTags.isEmpty()) {
            lore.add("<white>Individual Tags:");
            lore.addAll(individualTags);
        }

        if (!globalTags.isEmpty()) {
            if (!individualTags.isEmpty()) lore.add("");
            lore.add("<white>Global Tags:");
            lore.addAll(globalTags);
        }

        if (individualTags.isEmpty() && globalTags.isEmpty()) {
            lore.add("<gray>No tags applied");
        }

        return lore;
    }

    private String getTagColor(ItemTag tag) {
        return switch (tag) {
            case UNIQUE -> "green";
            case FINAL -> "red";
            case INFINITE -> "blue";
            case PROTECTED -> "dark_purple";
        };
    }
}