package me.trouper.dupealias.server.commands;

import me.trouper.alias.server.commands.Args;
import me.trouper.alias.server.commands.CommandRegistry;
import me.trouper.alias.server.commands.Permission;
import me.trouper.alias.server.commands.QuickCommand;
import me.trouper.alias.server.commands.completions.CompletionBuilder;
import me.trouper.dupealias.DupeContext;
import me.trouper.dupealias.server.ItemTag;
import me.trouper.dupealias.server.gui.admin.AdminGui;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandRegistry(
        value = "dupealias",
        permission = @Permission(value = "dupealias.admin", message = "Only server administrators can use this command."),
        usage = "/da <gui|tag> [unique|final|infinite|useless] [<material>|global|remove] [remove]",
        blocksAllowed = false,
        printStackTrace = true
)
public class AdminCommand implements QuickCommand, DupeContext {

    @Override
    public void handleCommand(CommandSender sender, Command command, String label, Args args) {
        if (args.isEmpty()) {
            openBaseGui(sender);
            return;
        }

        switch (args.get(0).toString()) {
            case "debug" -> {
                handleDebug(sender,args);
            }

            case "gui" -> {
                openBaseGui(sender);
            }

            case "tag" -> {
                handleTag(sender,args);
            }

            default -> {
                errorAny(sender,"Invalid subcommand!");
            }
        }
    }

    @Override
    public void handleCompletion(CommandSender sender, Command command, String label, Args args, CompletionBuilder b) {
        quickDebugArgs(b,getCommonConfig().debuggerExclusions.stream().toList())
        .then(
                b.arg("tag")
                        .then(b.argEnum(ItemTag.class)
                                .then(
                                        b.argEnum(Material.class)
                                                .then(
                                                        b.arg("remove") // Global Scope
                                                )
                                ).then(
                                        b.arg("global")
                                                .then(
                                                        b.arg("remove") // Global Scope
                                                )
                                ).then(
                                        b.arg("remove","false")
                                )
                        )
        ).then(
                b.arg("gui")
        );
    }


    private void handleDebug(CommandSender sender, Args args) {
          if (args.getSize() < 2) {
              errorAny(sender, "Usage: debug <toggle|include|exclude>");
              return;
          }

          final String sub = args.get(1).toString();

          switch (sub) {
              case "toggle" -> {
                  boolean result = false;
                  getCommonConfig().debugMode = result = !getCommonConfig().debugMode;
                  getCommonConfig().save();

                  getInstance().updateCommon();

                  successAny(sender,"Toggled debug mode {0}.",result ? "on" : "off");
              }
              case "exclude" -> {
                  if (args.getSize() < 3) {
                      errorAny(sender, "Usage: debug exclude <method>");
                      return;
                  }
                  final String exclusion = args.get(2).toString();
                  getCommonConfig().debuggerExclusions.add(exclusion);
                  getCommonConfig().save();

                  getInstance().updateCommon();

                  successAny(sender, "Excluded {0} from the debugger.", exclusion);
              }
              case "include" -> {
                  if (args.getSize() < 3) {
                      errorAny(sender, "Usage: debug include <method>");
                      return;
                  }
                  final String exclusion = args.get(2).toString();
                  getCommonConfig().debuggerExclusions.remove(exclusion);
                  getCommonConfig().save();

                  getInstance().updateCommon();

                  successAny(sender, "Removed exclusion for {0} on the debugger.", exclusion);
              }
          }
    }
    private void handleTag(CommandSender sender, Args args) {
        if (args.getSize() < 2) {
            errorAny(sender, "You must specify an item tag. Usage: /gui tag <tag> ...");
            return;
        }

        // Argument 1
        final ItemTag tag;
        try {
            tag = args.get(1).toEnum(ItemTag.class);
        } catch (IllegalArgumentException e) {
            errorAny(sender, "Argument '{0}' is not a valid item tag.", args.get(1).toString());
            return;
        }

        // gui tag <tag>
        if (args.getSize() == 2) {
            if (!(sender instanceof Player player)) {
                errorAny(sender, "This command can only be run by a player to tag a held item. To manage material tags, specify a material or 'global'.");
                return;
            }
            ItemStack heldItem = player.getInventory().getItemInMainHand();
            if (heldItem.getType().isAir()) {
                errorAny(sender, "You must be holding an item to tag it.");
                return;
            }
            if (getDupe().addTag(heldItem, tag)) {
                successAny(sender, "Your {0} is now tagged as {1} and {2}.", heldItem.getType(), tag.getName(), tag.getDesc());
                return;
            } else {
                infoAny(sender,"Your {0} already has the {1} tag.",heldItem.getType(),tag.getName());
            }

        }

        // Argument 2
        String subCommand = args.get(2).toString().toLowerCase();

        // gui tag <tag> remove|false
        switch (subCommand) {
            case "remove" -> {
                if (args.getSize() != 3) {
                    errorAny(sender, "Invalid arguments. Usage: /gui tag <tag> remove");
                    return;
                }
                if (!(sender instanceof Player player)) {
                    errorAny(sender, "This command can only be run by a player to remove a tag from a held item.");
                    return;
                }
                ItemStack heldItem = player.getInventory().getItemInMainHand();
                if (heldItem.getType().isAir()) {
                    errorAny(sender, "You must be holding an item to remove its tag.");
                    return;
                }
                if (getDupe().removeTag(heldItem, tag)) {
                    successAny(sender, "Removed tag {0} from your {1}.", tag.getName(), heldItem.getType());
                    return;
                } else {
                    infoAny(sender,"Your {0} does not have the {1} tag.",heldItem.getType(),tag.getName());
                }
            }
            case "false" -> {
                if (args.getSize() != 3) {
                    errorAny(sender, "Invalid arguments. Usage: /gui tag <tag> remove");
                    return;
                }
                if (!(sender instanceof Player player)) {
                    errorAny(sender, "This command can only be run by a player to add a tag from a held item.");
                    return;
                }
                ItemStack heldItem = player.getInventory().getItemInMainHand();
                if (heldItem.getType().isAir()) {
                    errorAny(sender, "You must be holding an item to set its tag.");
                    return;
                }
                getDupe().setTag(heldItem, tag, false);
                successAny(sender, "Set tag {0} from your {1} to {2}.", tag.getName(), heldItem.getType(), "false");
                return;
            }


            // gui tag <tag> global [remove]
            case "global" -> {
                if (!(sender instanceof Player player)) {
                    errorAny(sender, "The 'global' subcommand must be run by a player.");
                    return;
                }
                Material heldMaterial = player.getInventory().getItemInMainHand().getType();
                if (heldMaterial.isAir()) {
                    errorAny(sender, "You must be holding an item to use the 'global' subcommand.");
                    return;
                }

                boolean isRemove = args.getSize() > 3 && "remove".equalsIgnoreCase(args.get(3).toString());
                if (isRemove) {
                    if (args.getSize() != 4) {
                        errorAny(sender, "Invalid arguments. Usage: /gui tag <tag> global remove");
                        return;
                    }
                    if (getDupe().removeGlobalTag(heldMaterial, tag)) {
                        successAny(sender, "Removed global tag {0} from all {1} items.", tag.getName(), heldMaterial);
                    } else {
                        infoAny(sender, "{0} is not globally tagged as {1}.", heldMaterial, tag.getName());
                    }
                } else {
                    if (args.getSize() != 3) {
                        errorAny(sender, "Invalid arguments. Usage: /gui tag <tag> global");
                        return;
                    }
                    if (getDupe().addGlobalTag(heldMaterial, tag)) {
                        successAny(sender, "All {0} items are now globally tagged as {1} and {2}.", heldMaterial, tag.getName(), tag.getDesc());
                    } else {
                        infoAny(sender, "All {0} items are already tagged as {1} and {2}.", heldMaterial, tag.getName(), tag.getDesc());
                    }
                }
                return;
            }
        }

        // gui tag <tag> <material> [remove]
        try {
            Material material = args.get(2).toEnum(Material.class);
            boolean isRemove = args.getSize() > 3 && "remove".equalsIgnoreCase(args.get(3).toString());

            if (isRemove) {
                if (args.getSize() != 4) {
                    errorAny(sender, "Invalid arguments. Usage: /gui tag <tag> <material> remove");
                    return;
                }
                if (getDupe().removeGlobalTag(material, tag)) {
                    successAny(sender, "Removed global tag {0} from {1}.", tag.getName(), material);
                } else {
                    infoAny(sender, "{0} is not tagged as {1} globally.", material, tag.getName());
                }
            } else {
                if (args.getSize() != 3) {
                    errorAny(sender, "Invalid arguments. Usage: /gui tag <tag> <material>");
                    return;
                }
                getDupe().addGlobalTag(material, tag);
                if (getDupe().addGlobalTag(material, tag)) {
                    successAny(sender, "All {0} items are now tagged as {1} and {2}.", material, tag.getName(), tag.getDesc());
                } else {
                    infoAny(sender, "All {0} items are already tagged as {1} and {2}.", material, tag.getName(), tag.getDesc());
                }
            }
        } catch (IllegalArgumentException e) {
            errorAny(sender, "Invalid subcommand '{0}'. Expected 'remove', 'global', or a valid material name.", subCommand);
        }
    }


    public void openBaseGui(CommandSender sender) {
        if (sender instanceof Player player) {
            new AdminGui().openMainGui(player);
        } else {
            errorAny(sender, "Console may not open a GUI.");
        }
    }

}
