package io.github.crysscoder.nightmarket;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class NightMarketPlugin extends JavaPlugin implements CommandExecutor, TabCompleter {
    private UUID activeTrader;
    private long spawnTicket;
    private final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Objects.requireNonNull(getCommand("nightmarket")).setExecutor(this);
        Objects.requireNonNull(getCommand("nightmarket")).setTabCompleter(this);
        long interval = Math.max(60L, getConfig().getLong("check-interval-seconds", 300L)) * 20L;
        Bukkit.getScheduler().runTaskTimer(this, this::tryAutoSpawn, 100L, interval);
    }

    @Override
    public void onDisable() {
        despawn();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("nightmarket.admin")) {
            send(sender, "no-permission");
            return true;
        }

        String action = args.length == 0 ? "spawn" : args[0].toLowerCase();

        if (action.equals("reload")) {
            reloadConfig();
            send(sender, "reloaded");
            return true;
        }

        if (action.equals("despawn")) {
            despawn();
            send(sender, "despawned");
            return true;
        }

        Player target = sender instanceof Player player ? player : randomPlayer();

        if (target == null) {
            send(sender, "no-player");
            return true;
        }

        spawn(target);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("spawn", "despawn", "reload").stream().filter(item -> item.startsWith(args[0].toLowerCase())).toList();
        }
        return List.of();
    }

    private void tryAutoSpawn() {
        if (activeTrader != null && Bukkit.getEntity(activeTrader) != null) {
            return;
        }

        Player player = randomPlayer();

        if (player == null || !isNight(player.getWorld())) {
            return;
        }

        spawn(player);
    }

    private void spawn(Player player) {
        despawn();
        int radius = Math.max(4, getConfig().getInt("spawn-radius", 18));
        int x = player.getLocation().getBlockX() + ThreadLocalRandom.current().nextInt(-radius, radius + 1);
        int z = player.getLocation().getBlockZ() + ThreadLocalRandom.current().nextInt(-radius, radius + 1);
        Location location = player.getWorld().getHighestBlockAt(x, z).getLocation().add(0D, 1D, 0D);
        WanderingTrader trader = (WanderingTrader) player.getWorld().spawnEntity(location, EntityType.WANDERING_TRADER);
        trader.customName(Component.text("Night Market", NamedTextColor.DARK_PURPLE));
        trader.setCustomNameVisible(true);
        trader.setRemoveWhenFarAway(false);
        trader.setRecipes(recipes());
        activeTrader = trader.getUniqueId();
        long ticket = ++spawnTicket;
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (spawnTicket == ticket) {
                despawn();
            }
        }, Math.max(30L, getConfig().getLong("despawn-seconds", 240L)) * 20L);
        Bukkit.broadcast(message("spawned", Map.of("player", player.getName())));
    }

    private void despawn() {
        if (activeTrader == null) {
            return;
        }
        Entity entity = Bukkit.getEntity(activeTrader);
        if (entity != null) {
            entity.remove();
        }
        activeTrader = null;
        spawnTicket++;
    }

    private List<MerchantRecipe> recipes() {
        MerchantRecipe bottle = new MerchantRecipe(new ItemStack(Material.EXPERIENCE_BOTTLE, 16), 8);
        bottle.addIngredient(new ItemStack(Material.EMERALD, 6));
        MerchantRecipe apple = new MerchantRecipe(new ItemStack(Material.GOLDEN_APPLE, 2), 4);
        apple.addIngredient(new ItemStack(Material.EMERALD, 10));
        MerchantRecipe pearl = new MerchantRecipe(new ItemStack(Material.ENDER_PEARL, 4), 6);
        pearl.addIngredient(new ItemStack(Material.EMERALD, 8));
        return List.of(bottle, apple, pearl);
    }

    private Player randomPlayer() {
        List<? extends Player> players = Bukkit.getOnlinePlayers().stream().filter(player -> player.getWorld().getEnvironment() == World.Environment.NORMAL).toList();
        if (players.isEmpty()) {
            return null;
        }
        return players.get(ThreadLocalRandom.current().nextInt(players.size()));
    }

    private boolean isNight(World world) {
        long time = world.getTime();
        return time >= 12541 && time <= 23458;
    }

    private void send(CommandSender sender, String key) {
        send(sender, key, Map.of());
    }

    private void send(CommandSender sender, String key, Map<String, String> values) {
        sender.sendMessage(message(key, values));
    }

    private net.kyori.adventure.text.Component message(String key, Map<String, String> values) {
        String prefix = getConfig().getString("messages.prefix", "&7[&aNightMarket&7]");
        String result = getConfig().getString("messages." + key, "").replace("%prefix%", prefix);
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return legacy.deserialize(result);
    }
}
