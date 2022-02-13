package es.vic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Coliseo {
    String name;
    Location button;
    Integer minPlayers = 2;
    Integer maxPlayers = 2;
    Integer players = 0;
    List<UUID> playerList = new ArrayList<UUID>();
    List<Location> locations = new ArrayList<Location>();
    HashMap<Integer, ItemStack> equipment;
    HashMap<UUID, ItemStack[]> playerStorage = new HashMap<UUID, ItemStack[]>();
    HashMap<UUID, ItemStack[]> playerExtra = new HashMap<UUID, ItemStack[]>();
    Integer lives;
    HashMap<UUID, Integer> playerLives = new HashMap<UUID, Integer>();
    String state = "WAITING";

    public Coliseo(String name, Location button, Integer minPlayers, Integer maxPlayers,
            HashMap<Integer, ItemStack> equipment, Integer lives, List<Location> locations) {
        this.name = name;
        this.button = button;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.equipment = equipment;
        this.lives = lives;
        this.locations = locations;
    }

    public boolean addPlayer(Player player) {
        if (playerList.contains(player.getUniqueId()))
            return false;
        if (players >= maxPlayers)
            return false;
        if (state != "WAITING")
            return false;
        players = players + 1;
        playerList.add(player.getUniqueId());
        for (UUID uuid : playerList)
            Bukkit.getPlayer(uuid).sendMessage(
                    "(" + players + "/" + maxPlayers + ") " + player.getDisplayName() + " se ha unido al juego.");
        if (checkState())
            startGame();
        return true;
    }

    public boolean hasPlayer(Player player) {
        if (playerList.contains(player.getUniqueId()))
            return true;
        else
            return false;
    }

    public void deathPlayer(Player player) {
        if (player.getKiller() != null)
            player.getKiller().sendMessage("Has matado a " + player.getDisplayName());
        playerLives.put(player.getUniqueId(), playerLives.get(player.getUniqueId()) - 1);
        if (playerLives.get(player.getUniqueId()) == 0) {
            eliminatePlayer(player);
            player.sendTitle("Has perdido", "F", 10, 40, 10);
        } else {
            for (UUID uuid : playerList) {
                Player entity = Bukkit.getPlayer(uuid);
                entity.setHealth(20);
                entity.setFoodLevel(20);
                entity.getInventory().clear();
                equipment.entrySet().forEach(entry -> entity.getInventory().setItem(entry.getKey(), entry.getValue()));
                entity.teleport(locations.get(playerList.indexOf(uuid)));
            }
            player.sendTitle("Has reaparecido", "te quedan estas vidas " + playerLives.get(player.getUniqueId()), 10,
                    40, 10);
            player.setGameMode(GameMode.ADVENTURE);
            for (UUID uuid : playerList) {
                if (uuid != player.getUniqueId())
                    Bukkit.getPlayer(uuid).sendTitle("Siguiente ronda", "!A darle!", 10, 40, 10);
            }
        }
    }

    public void eliminatePlayer(Player player) {
        playerList.remove(player.getUniqueId());
        players = players - 1;
        player.getInventory().clear();
        player.teleport(button); // Y devolverles sus cosas
        player.getInventory().setContents(playerStorage.get(player.getUniqueId()));
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().setExtraContents(playerExtra.get(player.getUniqueId()));
        player.setHealth(20);
        player.setFoodLevel(20);
        if (players == 1)
            endGame();
    }

    public void endGame() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(Coliseum.instance, () -> {
            for (UUID uuidPlayer : playerList) {
                System.out.println(uuidPlayer);
                Player player = Bukkit.getPlayer(uuidPlayer);
                player.getInventory().clear();
                player.teleport(button); // Y devolverles sus cosas
                player.getInventory().setContents(playerStorage.get(player.getUniqueId()));
                player.setGameMode(GameMode.SURVIVAL);
                player.getInventory().setExtraContents(playerExtra.get(player.getUniqueId()));
                player.setHealth(20);
                player.setFoodLevel(20);
                player.sendTitle("Has ganado", "Felicidades", 10, 40, 10);
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(Coliseum.instance, () -> {
                state = "WAITING";
                players = 0;
                playerList = new ArrayList<UUID>();
                playerStorage = new HashMap<UUID, ItemStack[]>();
                playerExtra = new HashMap<UUID, ItemStack[]>();
                playerLives = new HashMap<UUID, Integer>();
                Bukkit.broadcastMessage("La arena de coliseo ha sido reiniciada");
            }, 20);
        }, 40);
    }

    public boolean checkState() {
        if (players >= minPlayers)
            return true;
        else
            return false;
    }

    public void startGame() {
        for (UUID uuid : playerList)
            Bukkit.getPlayer(uuid).sendMessage("El juego comenzara en 30 segundos, esperando a nuevos jugadores.");
        Bukkit.getScheduler().scheduleSyncDelayedTask(Coliseum.instance, () -> {
            for (UUID uuid : playerList) {
                Player player = Bukkit.getPlayer(uuid);
                playerStorage.put(uuid, player.getInventory().getStorageContents()); // el inventario de un player
                playerExtra.put(uuid, player.getInventory().getExtraContents()); // La armadura
                player.getInventory().clear();
                playerLives.put(uuid, lives);
                equipment.entrySet().forEach(entry -> player.getInventory().setItem(entry.getKey(), entry.getValue()));
                player.teleport(locations.get(playerList.indexOf(uuid)));
                player.setGameMode(GameMode.ADVENTURE);
                player.sendTitle("El juego a iniciado", "¡A darle!", 10, 40, 10);
                player.setHealth(20);
                player.setFoodLevel(20);
            }
            state = "STARTED";
        }, 30 * 20);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Coliseum.instance, () -> {
            for (UUID uuid : playerList)
                Bukkit.getPlayer(uuid).sendMessage("El juego comenzara en 10 segundos.");
        }, 20 * 20);
    }

}