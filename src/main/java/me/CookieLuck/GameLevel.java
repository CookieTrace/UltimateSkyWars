package me.CookieLuck;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import me.CookieLuck.lib.scoreboard.ScoreboardUtil;
import org.iq80.leveldb.util.FileUtils;

public class GameLevel {

	private int id;
	private Main plugin;
	private List<Spawn> spawnList;
	private String world;
	private boolean configuring;
	private int maxPlayers;
	private boolean emptySpawns;
	private boolean building;
	private boolean waiting;
	private boolean invulnerable;
	private boolean gameStarted;
	private List<Player> dead;
	private List<Player> alive;
	private HashMap<Player, Integer> playerKills = new HashMap<>();

	//CONSTRUCTOR AND MAIN COMUNICATION

	GameLevel(int id, String world, int maxPlayers, Main plugin){
		this.building = false;
		this.plugin = plugin;
		this.id = id;
		this.spawnList = new ArrayList<>();
		this.world = world;
		this.emptySpawns = true;
		this.configuring = false;
		this.maxPlayers = maxPlayers;
		this.waiting = true;
		this.invulnerable = false;
		this.dead = new ArrayList<>();
		this.gameStarted = false;
		this.alive = new ArrayList<>();
		addToMain();
		plugin.getLogger().info("[UltimateSkyWars] "+ TextFormat.GOLD+"LOADED ROOM: " + this.world);
	}

	GameLevel(int id, List<Spawn> spawns,String world, int maxPlayers, Main plugin){
		this(id,world,maxPlayers,plugin);
		this.spawnList = spawns;
		emptySpawns = false;

	}

	private void addToMain(){
		Main.gameLevels.add(this);
		plugin.saveGameLevels();
		new GameThread(plugin, this.world).runTaskTimer(plugin, 0, 1);
	}

	//GETTERS AND SETTERS

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Main getPlugin() {
		return plugin;
	}

	public void setPlugin(Main plugin) {
		this.plugin = plugin;
	}

	public List<Spawn> getSpawnList() {
		return spawnList;
	}

	public void setSpawnList(List<Spawn> spawnList) {
		this.spawnList = spawnList;
	}

	public String getWorld() {
		return world;
	}

	public void setWorld(String world) {
		this.world = world;
	}

	public boolean isConfiguring() {
		return configuring;
	}

	public void setConfiguring(boolean configuring) {
		this.configuring = configuring;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}

	public boolean isEmptySpawns() {
		return emptySpawns;
	}

	public void setEmptySpawns(boolean emptySpawns) {
		this.emptySpawns = emptySpawns;
	}

	public boolean isBuilding() {
		return building;
	}

	public void setBuilding(boolean building) {
		this.building = building;
	}

	public boolean isWaiting() {
		return waiting;
	}

	public void setWaiting(boolean waiting) {
		this.waiting = waiting;
	}

	public boolean isInvulnerable() {
		return invulnerable;
	}

	public void setInvulnerable(boolean invulnerable) {
		this.invulnerable = invulnerable;
	}

	public boolean isGameStarted() {
		return gameStarted;
	}

	public void setGameStarted(boolean gameStarted) {
		this.gameStarted = gameStarted;
	}

	public List<Player> getDead() {
		return dead;
	}

	public void setDead(List<Player> dead) {
		this.dead = dead;
	}

	public List<Player> getAlive() {
		return alive;
	}

	public void setAlive(List<Player> alive) {
		this.alive = alive;
	}

	public HashMap<Player, Integer> getPlayerKills() {
		return this.playerKills;
	}

	public int getPlayerKills(Player player) {
		return this.playerKills.getOrDefault(player, 0);
	}

	public void addPlayerKills(Player player) {
		this.playerKills.put(player, this.playerKills.getOrDefault(player, 0) + 1);
	}

	//METHODS

	public void die(Player p, EntityDamageEvent.DamageCause cause) {

		p.setGamemode(3);
		for (Item item : p.getInventory().getContents().values()) {
			if (item != null && item.getId() != 0) {
				p.dropItem(item);
			}
		}
		this.alive.remove(p);
		this.dead.add(p);
		if(cause.name().equalsIgnoreCase("VOID")) {
			p.teleport(p.getLevel().getSafeSpawn());
			p.getServer().getLevelByName(world).addSound(p.getLevel().getSafeSpawn(), Sound.AMBIENT_WEATHER_THUNDER,1,(float)0.8);
		}
		p.getInventory().clearAll();
		p.getUIInventory().clearAll();
		p.getServer().getLevelByName(world).addSound(p.getLocation(), Sound.AMBIENT_WEATHER_THUNDER,1,(float)0.8);

	}

	public void win(Player p) {
		this.gameStarted = false;
		this.waiting = true;
		this.building = true;
		for (Player player : alive) {
			player.sendTitle("", TextFormat.DARK_AQUA + "" + TextFormat.BOLD + "" + p.getName() + " WON THE GAME!");
		}
		for (Player player : dead) {
			player.sendTitle("", TextFormat.DARK_AQUA + "" + TextFormat.BOLD + "" + p.getName() + " WON THE GAME!");
		}
		Server.getInstance().getScheduler().scheduleDelayedTask(this.plugin, () -> {
			for (Player player : new ArrayList<>(alive)) {
				this.leave(player);
			}
			for (Player player : new ArrayList<>(dead)) {
				this.leave(player);
			}
			p.getServer().getLevelByName(world).unload(true);
			CompletableFuture.runAsync(() -> {
				File destiny = new File(plugin.worldsDir + "/" + world);
				File backup = new File(plugin.getDataFolder() + "/LevelBackups/" + world);
				FileUtils.copyDirectoryContents(backup,destiny);
				p.getServer().loadLevel(world);
				//init
				alive = new ArrayList<>();
				dead = new ArrayList<>();
				playerKills.clear();
				building = false;
			});
		}, 100);
	}

	public void leave(Player p) {
		p.teleport(this.plugin.lobby.getSafeSpawn());
		p.setGamemode(0);
		p.getInventory().clearAll();
		p.getUIInventory().clearAll();
		this.alive.remove(p);
		this.dead.remove(p);
		ScoreboardUtil.getScoreboard().closeScoreboard(p);
	}

	@Override
	public String toString(){
		StringBuilder spawnsString = new StringBuilder(id+"\n"+world+"\n"+maxPlayers+"\n");
		for (Spawn spawn : spawnList) {
			spawnsString.append(spawn.x)
					.append(":").append(spawn.y)
					.append(":").append(spawn.z).append("\n");
		}
		return spawnsString.toString();
	}

	public void joinForcePlayer(Player p){
		if(!plugin.getServer().isLevelLoaded(world)){
			plugin.getServer().loadLevel(world);
		}
		Location loc = new Location(p.getServer().getLevelByName(this.world).getSpawnLocation().x,p.getServer().getLevelByName(this.world).getSpawnLocation().y,p.getServer().getLevelByName(this.world).getSpawnLocation().z,p.getServer().getLevelByName(this.world));
		p.teleport(loc);
		alive.add(p);

	}

	public void joinPlayer(Player p){
		if(this.building){
			p.sendMessage(TextFormat.RED + "MAP NOT BUILDED YET, WAIT");
			return;
		}else if(!this.plugin.getServer().isLevelLoaded(this.world)) {
			this.plugin.getServer().loadLevel(this.world);
		}

		if(GameLevel.getGameLevelByWorld(((p.getLevel().getName()))) != null){
			p.sendMessage(TextFormat.RED+""+TextFormat.BOLD+"ALREADY IN GAME");
			return;
		}

		if(this.spawnList.size() == this.maxPlayers){
			p.teleport(new Position(
					this.spawnList.get(this.alive.size()).x,
					this.spawnList.get(this.alive.size()).y,
					this.spawnList.get(this.alive.size()).z,
					p.getServer().getLevelByName(this.world)));
		}else{
			p.teleport(p.getServer().getLevelByName(this.world).getSafeSpawn());
		}
		this.alive.add(p);
		p.setGamemode(2);
	}

	public boolean startableGame() {
		if (this.configuring) {
			return false;
		}
		return !this.emptySpawns;
	}
	
	public static GameLevel getGameLevelByWorld(String world) {
		for (GameLevel gameLevel : Main.gameLevels) {
			if(gameLevel.world.equals(world)) {
				return gameLevel;
			}
		}
		return null;
	}

	public static GameLevel getGameLevelById(int id) {
		return Main.gameLevels.get(id);
	}
	

}
