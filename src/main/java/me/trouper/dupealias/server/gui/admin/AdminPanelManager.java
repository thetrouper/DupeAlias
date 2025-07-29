package me.trouper.dupealias.server.gui.admin;

import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.data.GlobalRule;
import me.trouper.dupealias.data.ItemsAdderItem;
import me.trouper.dupealias.server.ItemTag;
import me.trouper.dupealias.server.gui.CommonItems;
import me.trouper.dupealias.server.gui.admin.config.ConfigGui;
import me.trouper.dupealias.server.gui.admin.globalrule.GlobalRuleEditorGui;
import me.trouper.dupealias.server.gui.admin.globalrule.GlobalRuleListGui;
import me.trouper.dupealias.server.gui.admin.globalrule.criteria.*;
import me.trouper.dupealias.server.gui.admin.globalrule.criteria.regex.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminPanelManager implements DupeContext, CommonItems {

    public void openMainGui(Player player) {
        new MainAdminGui(this).open(player);
    }

    public void openHeldItemGui(Player player) {
        new HeldItemGui(this).open(player);
    }

    public void openBulkTagGui(Player player) {
        new BulkTagGui(this).open(player);
    }
    
    public void openHelpGui(Player player) {
        new HelpGui(this).open(player);
    }

    public void openGlobalRuleList(Player player) {
        new GlobalRuleListGui(this).createGUI(player).open(player);
    }

    public void openConfigGui(Player player) {
        new ConfigGui(this).open(player);
    }

    public void openGlobalRuleEditor(Player player, GlobalRule rule) {
        new GlobalRuleEditorGui(this, rule).open(player);
    }

    public void openMaterialSelector(Player player, GlobalRule rule) {
        new GlobalRuleMaterialSelector(this, rule).createGUI(player).open(player);
    }

    public void openPotionEffectEditor(Player player, GlobalRule rule) {
        new GlobalRulePotionEffectEditor(this, rule).createGUI(player).open(player);
    }

    public void openArmorTrimEditor(Player player, GlobalRule rule) {
        new GlobalRuleArmorTrimEditor(this, rule).open(player);
    }

    public void openNameCriteriaEditor(Player player, GlobalRule rule) {
        new GlobalRuleNameEditor(this,rule).open(player);
    }

    public void openLoreCriteriaEditor(Player player, GlobalRule rule) {
        new GlobalRuleLoreEditor(this,rule).open(player);
    }

    public void openNbtCriteriaEditor(Player player, GlobalRule rule) {
        new GlobalRuleNbtTagEditor(this,rule).open(player);
    }

    public void openCompoundCriteriaEditor(Player player, GlobalRule rule) {
        new GlobalRuleCompoundTagEditor(this,rule).open(player);
    }

    public void openModelDataEditor(Player player, GlobalRule rule) {
        new GlobalRuleModelDataEditor(this,rule).open(player);
    }

    public void openItemsAdderParser(Player player, GlobalRule rule) {
        new GlobalRuleItemsAdderParser(this,rule).open(player);
    }

    public void openEnchantmentEditor(Player player, GlobalRule rule) {
        new GlobalRuleEnchantmentEditor(this, rule).createGUI(player).open(player);
    }

    public void openAttributeEditor(Player player, GlobalRule rule) {
        new GlobalRuleAttributeEditor(this, rule).createGUI(player).open(player);
    }

    public void openItemFlagEditor(Player player, GlobalRule rule) {
        new GlobalRuleItemFlagEditor(this, rule).open(player);
    }

    public ItemStack createExplainedItem(ItemStack item) {
        if (item == null || item.isEmpty()) {
            return ItemBuilder.create(Material.GRAY_STAINED_GLASS_PANE)
                    .displayName("<gray><italic>No item held</italic>")
                    .loreMiniMessage("<aqua>💡 <white>Hold an item to get information on it")
                    .build();
        }

        ItemsAdderItem iai = new ItemsAdderItem();
        try {
            iai = new ItemsAdderItem(item);
        } catch (IllegalArgumentException ignored) {}

        List<String> lore = new ArrayList<>();
        lore.add("<white><bold>Held Item Explanation:</bold>");

        Set<ItemTag> activeTags = new HashSet<>();

        for (ItemTag tag : ItemTag.values()) {
            boolean global = getDupe().checkGlobalRuleTag(item, tag);
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

        if (!iai.equals(new ItemsAdderItem())) {
            lore.add("<gray>• Is from ItemsAdder. (" + iai.namespace + ":" + iai.id + ")");
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

    public List<String> getItemTagStatus(ItemStack item) {
        List<String> lore = new ArrayList<>();
        lore.add("<white>Held Item: <yellow>" + item.getType().name());
        lore.add("");

        List<String> individualTags = new ArrayList<>();
        for (ItemTag tag : ItemTag.values()) {
            if (getDupe().hasIndividualTag(item,tag)) {
                individualTags.add("<" + getTagColor(tag) + ">" + (getDupe().checkIndividualTag(item,tag) ? "✔" : "❌") + " " + tag.getName());
            }
        }

        List<String> globalTags = new ArrayList<>();
        for (ItemTag tag : ItemTag.values()) {
            if (getDupe().checkGlobalRuleTag(item, tag)) {
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

    public String getTagColor(ItemTag tag) {
        return switch (tag) {
            case UNIQUE -> "green";
            case FINAL -> "red";
            case INFINITE -> "blue";
            case PROTECTED -> "dark_purple";
        };
    }
}