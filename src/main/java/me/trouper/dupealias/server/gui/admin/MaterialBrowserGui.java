package me.trouper.dupealias.server.gui.admin;

import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.alias.server.systems.gui.QuickPaginatedGUI;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.server.ItemTag;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaterialBrowserGui extends QuickPaginatedGUI<Material> implements DupeContext {

    private Material PICKED_MATERIAL = Material.AIR;

    @Override
    protected String getTitle(Player player) {
        return "<yellow><bold>Material Browser";
    }

    @Override
    protected List<Material> getAllItems(Player player) {
        return Arrays.stream(Material.values()).filter(material -> !material.isLegacy() && !material.isAir()).filter(Material::isItem).toList();
    }

    @Override
    protected ItemStack createDisplayItem(Material item) {
        return new AdminGui().createMaterialTagItem(item);
    }

    @Override
    protected void handleItemClick(Player player, Material item, InventoryClickEvent event) {
        PICKED_MATERIAL = item;
        new AdminGui().openGlobalMaterialGui(player,PICKED_MATERIAL);
    }

    @Override
    protected void addFilterItems(QuickGui.GuiBuilder filterGui, Player player, Set<String> filters) {
        filterGui.item(0, createFilterToggleItem("Infinite",Material.LAPIS_BLOCK,filters.contains("I")), (gui, event) ->
                toggleFilter(player,"I"));
        filterGui.item(1, createFilterToggleItem("Unique",Material.EMERALD_BLOCK,filters.contains("U")), (gui, event) ->
                toggleFilter(player,"U"));
        filterGui.item(2, createFilterToggleItem("Final",Material.REDSTONE_BLOCK,filters.contains("F")), (gui, event) ->
                toggleFilter(player,"F"));
        filterGui.item(3, createFilterToggleItem("Protected",Material.COMMAND_BLOCK,filters.contains("P")), (gui, event) ->
                toggleFilter(player,"P"));

    }

    @Override
    protected boolean testFilter(Player player, Material item, String filterKey) {
        return switch (filterKey) {
            case "I" -> getConfig().globalMaterials.getOrDefault(item,new HashSet<>()).contains(ItemTag.INFINITE);
            case "U" -> getConfig().globalMaterials.getOrDefault(item,new HashSet<>()).contains(ItemTag.UNIQUE);
            case "F" -> getConfig().globalMaterials.getOrDefault(item,new HashSet<>()).contains(ItemTag.FINAL);
            case "P" -> getConfig().globalMaterials.getOrDefault(item,new HashSet<>()).contains(ItemTag.PROTECTED);
            default -> false;
        };
    }

    @Override
    protected void openBackGUI(Player player) {
        new AdminGui().openGlobalMaterialGui(player,PICKED_MATERIAL);
    }
}
