package me.trouper.dupealias.server.gui.admin;

import me.trouper.alias.server.systems.gui.QuickGui;
import me.trouper.alias.utils.ItemBuilder;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.server.gui.CommonItems;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class ConfigGui implements DupeContext, CommonItems {
    public void open(Player player, QuickGui backGui) {
        QuickGui gui = QuickGui.create()
                .titleMini("<dark_blue><Bold>DupeAlias Config")
                .defaultTimeout(30000)
                .rows(6)
                .item(0,BACK(),(g,e) -> backGui.open(player))
                .callback("dupe_cooldown", new QuickGui.GuiCallback() {
                    @Override
                    public void onInput(QuickGui gui, Player player, String input, QuickGui.InputSource source) {
                        try {
                            long millis = Long.parseLong(input);
                            infoAny(player,"You have set the dupe cooldown to {0}ms.",input);
                            getDupe().getConfig().dupeCooldownMillis = millis;
                            getDupe().getConfig().save();
                            open(player,backGui);
                        } catch (NumberFormatException ex) {
                            errorAny(player,"Please input a valid long number of milliseconds.");
                            requestInput(gui,player,"dupe_cooldown","Number format error, please input a value.");
                        }
                    }
                })
                .item(13, ItemBuilder.integerItem(Material.DIAMOND,"<aqua>Dupe Command Cooldown", List.of(
                        "<gray>How long players have",
                        "<gray>to wait before running",
                        "<gray>the /dupe command again.",
                        " ",
                        "<white>Click to set value."), (int) getConfig().dupeCooldownMillis),(g, e)->{
                    Player p = (Player) e.getWhoClicked();
                    requestInput(g,p,"dupe_cooldown","<aqua>Insert a long value of Milliseconds.\n<gray>     1000ms = 1 Second\n\n <yellow>The current value is set to <white>" + getConfig().dupeCooldownMillis + "\n");
                })
                .fillEmpty(EMPTY())
                .build();

        player.openInventory(gui.getInventory());
    }


    private void requestInput(QuickGui gui, Player player, String callbackId, String prompt) {
        getDupe().getGuiListener().registerWaitingPlayer(player, gui);

        gui.requestInput(player, callbackId);

        getDupe().getGuiListener().sendInputInstructions(player, prompt);
    }
}
