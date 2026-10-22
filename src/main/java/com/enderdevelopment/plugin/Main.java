package com.enderdevelopment.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class Main extends JavaPlugin implements Listener {

    private final Map<Player, Boolean> autoMineEnabled = new HashMap<>();
    private final Map<Player, Integer> targetSlot = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("miner").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("AutoMiner plugin enabled!");
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
        }
    }
    
    private void startMining(Player player) {
        running = true;
        task = Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (player.getInventory().getItemInMainHand().getType() == Material.DIAMOND_PICKAXE) {
                player.swingMainHand();
                for (String command : commands) {
                    player.performCommand(command);
                }
            } else {
                player.sendMessage("You need a diamond pickaxe in your main hand to mine.");
                stopMining();
            }
        }, 0, delay);
    }
    
    private void stopMining() {
        running = false;
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
        if (task != null) {
            task.cancel();
        }
        getLogger().info("AutoMiner plugin disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage("Usage: /miner <start|stop|add|remove|delay>");
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "start":
                if (running) {
                    player.sendMessage("Miner is already running.");
                } else {
                    startMining(player);
                    player.sendMessage("Miner started.");
                }
                break;
            case "stop":
                if (!running) {
                    player.sendMessage("Miner is not running.");
                } else {
                    stopMining();
                    player.sendMessage("Miner stopped.");
                }
                break;
            case "add":
                if (args.length < 2) {
                    player.sendMessage("Usage: /miner add <command>");
                } else {
                    String commandToAdd = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    commands.add(commandToAdd);
                    player.sendMessage("Command added: " + commandToAdd);
                }
                break;
            case "remove":
                if (args.length < 2) {
                    player.sendMessage("Usage: /miner remove <command>");
                } else {
                    String commandToRemove = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    if (commands.remove(commandToRemove)) {
                        player.sendMessage("Command removed: " + commandToRemove);
                    } else {
                        player.sendMessage("Command not found: " + commandToRemove);
                    }
                }
                break;
            case "delay":
                if (args.length < 2) {
                    player.sendMessage("Usage: /miner delay <ticks>");
                } else {
                    try {
                        delay = Integer.parseInt(args[1]);
                        player.sendMessage("Delay set to " + delay + " ticks.");
                    } catch (NumberFormatException e) {
                        player.sendMessage("Invalid delay value.");
                    }
                }
                break;
            default:
                player.sendMessage("Unknown command. Usage: /miner <start|stop|add|remove|delay>");
                break;
        }
        
        return true;
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        if (command.getName().equalsIgnoreCase("automine")) {
            boolean enabled = autoMineEnabled.getOrDefault(player, false);
            autoMineEnabled.put(player, !enabled);
            player.sendMessage("Auto-mining " + (!enabled ? "enabled" : "disabled") + "!");
            return true;
        }
        return false;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!autoMineEnabled.getOrDefault(player, false)) {
            return;
        }

        Block block = event.getBlock();
        if (block.getType() == Material.STONE || block.getType() == Material.COBBLESTONE || block.getType() == Material.IRON_ORE || block.getType() == Material.GOLD_ORE || block.getType() == Material.DIAMOND_ORE) {
            ItemStack pickaxe = player.getInventory().getItemInMainHand();
            if (pickaxe.getType() == Material.DIAMOND_PICKAXE) {
                event.setCancelled(true);
                block.breakNaturally(pickaxe);
                dropItems(player, block);
            }
        }
    }

    private void dropItems(Player player, Block block) {
        int slot = targetSlot.getOrDefault(player, 0);
        ItemStack[] contents = player.getInventory().getContents();
        for (ItemStack item : block.getDrops()) {
            if (contents[slot] == null) {
                contents[slot] = item;
            } else if (contents[slot].isSimilar(item)) {
                contents[slot].setAmount(contents[slot].getAmount() + item.getAmount());
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }
        player.getInventory().setContents(contents);
    }
}