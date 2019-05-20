package net.yofuzzy3.BungeePortals.Commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.yofuzzy3.BungeePortals.BungeePortals;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

public class CommandBPortals implements CommandExecutor {

    private BungeePortals plugin;
    private Map<String, List<String>> selections = new HashMap<>();

    public CommandBPortals(BungeePortals plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase("BPortals")) {
            if (sender.hasPermission("BungeePortals.command.BPortals")) {
                if (args.length == 0) {
                    sender.sendMessage(ChatColor.BLUE + "BungeePortals v" + plugin.getDescription().getVersion() + " by YoFuzzy3");
                    sender.sendMessage(ChatColor.GREEN + "/BPortals reload " + ChatColor.RED + "Reload all files and data.");
                    sender.sendMessage(ChatColor.GREEN + "/BPortals forcesave " + ChatColor.RED + "Force-save portals.");
                    sender.sendMessage(ChatColor.GREEN + "/BPortals select <filter,list> " + ChatColor.RED + "Get selection.");
                    sender.sendMessage(ChatColor.GREEN + "/BPortals clear " + ChatColor.RED + "Clear selection.");
                    sender.sendMessage(ChatColor.GREEN + "/BPortals create <destination> " + ChatColor.RED + "Create portals.");
                    sender.sendMessage(ChatColor.GREEN + "/BPortals remove <destination> " + ChatColor.RED + "Remove portals.");
                    sender.sendMessage(ChatColor.BLUE + "Visit www.spigotmc.org/resources/bungeeportals.19 for help.");
                } else if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("reload")) {
                        plugin.loadConfigFiles();
                        plugin.loadPortalsData();
                        sender.sendMessage(ChatColor.GREEN + "All configuration files and data have been reloaded.");
                    } else if (args[0].equalsIgnoreCase("forcesave")) {
                        plugin.savePortalsData();
                        sender.sendMessage(ChatColor.GREEN + "Portal data saved!");
                    } else if (args[0].equalsIgnoreCase("clear")) {
                        if (sender instanceof Player) {
                            String playerName = sender.getName();
                            if (selections.containsKey(playerName)) {
                                selections.remove(playerName);
                                sender.sendMessage(ChatColor.GREEN + "Selection cleared.");
                            } else {
                                sender.sendMessage(ChatColor.RED + "You haven't selected anything.");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Only players can use that command.");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Type /BPortals for help!");
                    }
                } else if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("select")) {
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            String playerName = player.getName();
                            Selection selection = plugin.worldEdit.getSelection(player);
							
                            if(selection == null) {
								player.sendMessage(ChatColor.RED + "You have to first create a WorldEdit selection!");
								return false;
							}
							
                            if (selection instanceof CuboidRegion) {
                                List<Location> locations = getLocationsFromCuboid((CuboidRegion) selection);
                                List<String> blocks = new ArrayList<>();
                                String[] ids = {};
                                int count = 0;
                                int filtered = 0;
                                boolean filter = false;
                                if (!args[1].equals("0")) {
                                    ids = args[1].split(",");
                                    filter = true;
                                }
                                for (Location location : locations) {
                                    Block block = player.getWorld().getBlockAt(location);
                                    if (filter) {
                                        boolean found = false;
                                        for (int i = 0; i < ids.length; i++) {
                                            String id = ids[i];
                                            if (id.equalsIgnoreCase(block.getType().toString())) {
                                            	found = true;
                                            	break;
                                            }
                                        }
                                        if (found) {
                                            blocks.add(block.getWorld().getName() + "#" + String.valueOf(block.getX()) + "#" + String.valueOf(block.getY()) + "#" + String.valueOf(block.getZ()));
                                            count++;
                                        } else {
                                            filtered++;
                                        }
                                    } else {
                                        blocks.add(block.getWorld().getName() + "#" + String.valueOf(block.getX()) + "#" + String.valueOf(block.getY()) + "#" + String.valueOf(block.getZ()));
                                        count++;
                                    }
                                }
                                selections.put(playerName, blocks);
                                sender.sendMessage(ChatColor.GREEN + String.valueOf(count) + " blocks have been selected, " + String.valueOf(filtered) + " filtered.");
                                sender.sendMessage(ChatColor.GREEN + "Use the selection in the create and remove commands.");
                            } else {
                                sender.sendMessage(ChatColor.RED + "Must be a cuboid selection!");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Only players can use that command.");
                        }
                    } else if (args[0].equalsIgnoreCase("create")) {
                        if (sender instanceof Player) {
                            String playerName = sender.getName();
                            if (selections.containsKey(playerName)) {
                                List<String> selection = selections.get(playerName);
                                for (String block : selection) {
                                    plugin.portalData.put(block, args[1]);
                                }
                                sender.sendMessage(ChatColor.GREEN + String.valueOf(selection.size()) + " portals have been created.");
                            } else {
                                sender.sendMessage(ChatColor.RED + "You haven't selected anything.");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Only players can use that command.");
                        }
                    } else if (args[0].equalsIgnoreCase("remove")) {
                        if (sender instanceof Player) {
                            String playerName = sender.getName();
                            if (selections.containsKey(playerName)) {
                                int count = 0;
                                for (String block : selections.get(playerName)) {
                                    if (plugin.portalData.containsKey(block)) {
                                        plugin.portalData.remove(block);
                                        count++;
                                    }
                                }
                                sender.sendMessage(ChatColor.GREEN + String.valueOf(count) + " portals have been removed.");
                            } else {
                                sender.sendMessage(ChatColor.RED + "You haven't selected anything.");
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Only players can use that command.");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Type /BPortals for help!");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Type /BPortals for help!");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            }
        }
        return true;
    }

    private List<Location> getLocationsFromCuboid(CuboidRegion cuboid) {
        List<Location> locations = new ArrayList<>();
        Vector minLocation = cuboid.getMinimumPoint();
        Vector maxLocation = cuboid.getMaximumPoint();
        for (int i1 = minLocation.getBlockX(); i1 <= maxLocation.getBlockX(); i1++) {
            for (int i2 = minLocation.getBlockY(); i2 <= maxLocation.getBlockY(); i2++) {
                for (int i3 = minLocation.getBlockZ(); i3 <= maxLocation.getBlockZ(); i3++) {
                	String worldName = cuboid.getWorld().getName();
                    locations.add(new Location(plugin.getServer().getWorld(worldName), i1, i2, i3));
                }
            }
        }
        return locations;
    }

}