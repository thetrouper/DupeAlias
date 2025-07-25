package me.trouper.dupealias.server.gui.admin.globalrule;

import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.alias.server.systems.gui.QuickPaginatedGUI;
import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.data.GlobalRule;
import me.trouper.dupealias.server.gui.CommonItems;
import me.trouper.dupealias.server.gui.admin.AdminPanelManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GlobalRuleMaterialSelector extends QuickPaginatedGUI<Material> implements DupeContext, CommonItems {

    private final AdminPanelManager manager;
    private final GlobalRule rule;

    public GlobalRuleMaterialSelector(AdminPanelManager manager, GlobalRule rule) {
        this.manager = manager;
        this.rule = rule;
    }

    @Override
    protected String getTitle(Player player) {
        return "<gradient:#4caf50:#388e3c><bold>Select Materials (" + rule.materialMode.name() + ")</bold></gradient>";
    }

    @Override
    protected List<Material> getAllItems(Player player) {
        return Arrays.stream(Material.values())
                .filter(mat -> !mat.isLegacy() && !mat.isAir() && mat.isItem())
                .toList();
    }

    @Override
    protected ItemStack createDisplayItem(Material material) {
        boolean selected = rule.effectedMaterials.contains(material);
        ItemBuilder builder = ItemBuilder.create(material)
                .displayName((selected ? "<green>" : "<gray>") + "<bold>" + material.name())
                .loreMiniMessage("<white>Material: <yellow>" + material.name());
        
        if (selected) {
            builder.enchant(Enchantment.MENDING,1);
            builder.loreMiniMessage(
                    "",
                    "<green>✓ Selected",
                    "",
                    "<yellow>▶ <white>Click to remove");
        } else {
            builder.loreMiniMessage(
                    "",
                    "<gray>Not selected",
                    "",
                    "<yellow>▶ <white>Click to add");
        }
                
        return builder.build();
    }

    @Override
    protected void handleItemClick(Player player, Material material, InventoryClickEvent event) {
        if (rule.effectedMaterials.contains(material)) {
            rule.effectedMaterials.remove(material);
            infoAny(player, "Removed {0} from material list", material.name());
        } else {
            rule.effectedMaterials.add(material);
            successAny(player, "Added {0} to material list", material.name());
        }

        getConfig().save();
        createGUI(player).open(player);
    }

    @Override
    protected void addFilterItems(QuickGui.GuiBuilder filterGui, Player player, Set<String> filters) {
        filterGui.item(0, createFilterToggleItem("Selected Only", Material.LIME_DYE, filters.contains("S")),
                (gui, event) -> toggleFilter(player, "S"));
        filterGui.item(1, createFilterToggleItem("Blocks", Material.STONE, filters.contains("B")),
                (gui, event) -> toggleFilter(player, "B"));
        filterGui.item(2, createFilterToggleItem("Items", Material.STICK, filters.contains("I")),
                (gui, event) -> toggleFilter(player, "I"));
        filterGui.item(3, createFilterToggleItem("Tools", Material.DIAMOND_PICKAXE, filters.contains("T")),
                (gui, event) -> toggleFilter(player, "T"));
        filterGui.item(4, createFilterToggleItem("Armor", Material.DIAMOND_CHESTPLATE, filters.contains("A")),
                (gui, event) -> toggleFilter(player, "A"));
    }

    @Override
    protected boolean testFilter(Player player, Material material, String filterKey) {
        return switch (filterKey) {
            case "S" -> rule.effectedMaterials.contains(material);
            case "B" -> material.isBlock();
            case "I" -> !material.isBlock();
            case "T" -> material.name().contains("_AXE") || material.name().contains("_PICKAXE") ||
                    material.name().contains("_SHOVEL") || material.name().contains("_HOE") ||
                    material.name().contains("_SWORD");
            case "A" -> material.name().contains("_HELMET") || material.name().contains("_CHESTPLATE") ||
                    material.name().contains("_LEGGINGS") || material.name().contains("_BOOTS");
            default -> false;
        };
    }

    @Override
    protected void openBackGUI(Player player) {
        manager.openGlobalRuleEditor(player, rule);
    }

    @Override
    public QuickGui createGUI(Player player) {
        QuickGui gui = super.createGUI(player);

        gui.updateItem(47, ItemBuilder.create(Material.BARRIER)
                .displayName("<red><bold>Clear All")
                .loreMiniMessage(Arrays.asList(
                        "<gray>Remove all selected materials",
                        "",
                        "<yellow>▶ <white>Click to clear"
                ))
                .build(), (q, event) -> {
            rule.effectedMaterials.clear();
            getConfig().save();
            successAny(event.getWhoClicked(), "Cleared all materials");
            createGUI((Player) event.getWhoClicked()).open((Player) event.getWhoClicked());
        });

        return gui;
    }
}