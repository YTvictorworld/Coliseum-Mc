package es.vic;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class Coliseum extends JavaPlugin implements Listener {
    public static Coliseum instance;
    List<Coliseo> coliseos = new ArrayList<Coliseo>();
    HashMap<Integer, ItemStack> equipment = new HashMap<Integer, ItemStack>();

    @Override
    public void onEnable() {
        instance = this;
        if (!(new File(getDataFolder(), "config.yml").exists()))
            saveDefaultConfig();
        for (String key : getConfig().getConfigurationSection("coliseums").getKeys(false)) {
            ConfigurationSection config = getConfig().getConfigurationSection("coliseums." + key);
            HashMap<Integer, ItemStack> equipment = new HashMap<>();
            config.getStringList("equipment")
                    .forEach(equip -> equipment.put(Integer.parseInt(equip.split(";")[0]), stringToItemStack(equip)));
            Coliseo coliseo = new Coliseo(key, stringToLocationBlock(config.getString("button")),
                    config.getInt("minPlayers"), config.getInt("maxPlayers"), equipment, config.getInt("lives"),
                    config.getStringList("locations").stream().map(this::stringToLocation)
                            .collect(Collectors.toList()));
            coliseos.add(coliseo);
        }
        System.out.println(
                ChatColor.translateAlternateColorCodes('&', "&aSe han registrado " + coliseos.size() + " coliseos."));
        getServer().getPluginManager().registerEvents(this, this);
        System.out.println("|_ Coliseum-MC _| Ha sido cargado correctamente.");

    }

    public Location stringToLocation(String location) {
        return new Location(Bukkit.getWorld(location.split(";")[0]), Integer.parseInt(location.split(";")[1]),
                Integer.parseInt(location.split(";")[2]), Integer.parseInt(location.split(";")[3]),
                Integer.parseInt(location.split(";")[4]), Integer.parseInt(location.split(";")[5]));
    }

    public Location stringToLocationBlock(String location) {
        return new Location(Bukkit.getWorld(location.split(";")[0]), Integer.parseInt(location.split(";")[1]),
                Integer.parseInt(location.split(";")[2]), Integer.parseInt(location.split(";")[3]));
    }

    public ItemStack stringToItemStack(String item) {
        return new ItemStack(Material.getMaterial(item.split(";")[1]), Integer.parseInt(item.split(";")[2]));
    }

    @EventHandler
    public void onPlayerDeath(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.PLAYER)
            return;
        Player player = (Player) event.getEntity();
        if (player.getHealth() - event.getDamage() <= 0) {
            Coliseo coliseo = coliseos.stream().filter(coliseum -> coliseum.hasPlayer(player)).findFirst().orElse(null);
            if (coliseo == null)
                return;
            if (coliseo.state == "STARTED") {
                coliseo.deathPlayer(player);
                event.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void offPlayer(PlayerQuitEvent e) {
        Coliseo coliseo = coliseos.stream().filter(coliseum -> coliseum.hasPlayer(e.getPlayer())).findFirst()
                .orElse(null);
        if (coliseo == null)
            return;
        coliseo.eliminatePlayer(e.getPlayer());
    }

    @EventHandler
    public void PlayerInteractEvent(PlayerInteractEvent e) {
        Action action = e.getAction();
        Player player = e.getPlayer();
        Block block = e.getClickedBlock();
        if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
            if (block.getType() == Material.OAK_BUTTON) {
                Coliseo coliseo = coliseos.stream().filter(coliseum -> coliseum.button.equals(block.getLocation()))
                        .findFirst().orElse(null);
                System.out.println(block.getLocation().toString());
                coliseos.forEach(coliseoasd -> System.out.println(coliseoasd.button.toString()));
                if (coliseo == null)
                    return;
                coliseo.addPlayer(player);
            }
        }
    }

}
