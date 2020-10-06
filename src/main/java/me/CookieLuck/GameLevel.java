package me.CookieLuck;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.utils.TextFormat;
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

	//METHODS

	public void die(Player p){
		p.setGamemode(3);

		Map<Integer, Item> map = p.getInventory().getContents();

		Iterator<Integer> it = map.keySet().iterator();

		int itemID;
		while (it.hasNext()) {
			itemID = it.next();
			Item item = map.get(itemID);
			p.dropItem(item);
		}
		alive.remove(p);
		dead.add(p);
		p.getInventory().clearAll();
		p.getServer().getLevelByName(world).addSound(p.getLocation(), Sound.AMBIENT_WEATHER_THUNDER,1,(float)0.8);
		if (p.getPosition().getX() <= 0) {

			Position pos = new Position(p.getPosition().getX(), 40, p.getPosition().getZ());
			p.teleport(pos);
		}
	}

	public void win(Player p){

		gameStarted = false;
		waiting = true;
		building = true;
		for (Player player : alive) {
			player.sendTitle("", TextFormat.DARK_AQUA + "" + TextFormat.BOLD + "" + p.getName() + " WON THE GAME!");
		}

		for (Player player : dead) {
			player.sendTitle("", TextFormat.DARK_AQUA + "" + TextFormat.BOLD + "" + p.getName() + " WON THE GAME!");
		}
		alive = new ArrayList<>();
		dead = new ArrayList<>();
		new Thread( new Runnable() {
			public void run()  {
				try  {
					Thread.sleep( 5000 );
				} catch (InterruptedException ignored) {}
				for(int i = 0; i<alive.size();i++){
					Location loc = new Location(p.getServer().getDefaultLevel().getSpawnLocation().getX(),p.getServer().getDefaultLevel().getSpawnLocation().getY(),p.getServer().getDefaultLevel().getSpawnLocation().getZ(), p.getServer().getDefaultLevel());
					alive.get(i).teleport(loc);
					dead.get(i).setGamemode(0);
				}

				for (Player player : dead) {
					Location loc = new Location(p.getServer().getDefaultLevel().getSpawnLocation().getX(), p.getServer().getDefaultLevel().getSpawnLocation().getY(), p.getServer().getDefaultLevel().getSpawnLocation().getZ(), p.getServer().getDefaultLevel());
					player.teleport(loc);
					player.setGamemode(0);
				}

				p.getServer().getLevelByName(world).unload(true);

				File destiny = new File(plugin.getDataFolder()+"../"+"../"+"../"+"/worlds/"+world);
				File backup = new File(plugin.getDataFolder() + "/LevelBackups/"+world);
				FileUtils.copyDirectoryContents(backup,destiny);
				p.getServer().loadLevel(world);

			}
		} ).start();
		new Thread( new Runnable() {
			public void run()  {
				try  {
					Thread.sleep( 30000 );
				} catch (InterruptedException ignored) {

				}

				building = false;


			}
		} ).start();
	}

	public void leave(Player p){
		Location loc = new Location(plugin.lobby.getSpawnLocation().x,plugin.lobby.getSpawnLocation().y,plugin.lobby.getSpawnLocation().z,plugin.lobby);
		p.teleport(loc);
		p.setGamemode(0);
		if(alive.contains(p)){
			alive.remove(p);
		}else{
			dead.remove(p);
		}

		p.getInventory().clearAll();

	}

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
		if(building){
			p.sendMessage(TextFormat.RED + "MAP NOT BUILDED YET, WAIT");
			return;
		}else if(!plugin.getServer().isLevelLoaded(world)){
			plugin.getServer().loadLevel(world);
		}

		if(GameLevel.getGameLevelByWorld(((p.getLevel().getName()))) != null){
			p.sendMessage(TextFormat.RED+""+TextFormat.BOLD+"ALREADY IN GAME");
			return;
		}

		if(spawnList.size() == maxPlayers){

			Location loc = new Location(this.spawnList.get(this.alive.size()).x,this.spawnList.get(this.alive.size()).y,this.spawnList.get(this.alive.size()).z, p.getServer().getLevelByName(this.world));
			p.teleport(loc);

		}else{
			Location loc = new Location(p.getServer().getLevelByName(this.world).getSpawnLocation().x,p.getServer().getLevelByName(this.world).getSpawnLocation().y,p.getServer().getLevelByName(this.world).getSpawnLocation().z,p.getServer().getLevelByName(this.world));
			p.teleport(loc);
		}alive.add(p);
		p.setGamemode(2);
	}

	public boolean startableGame() {
		if (configuring) {
			return false;
		}
		return !emptySpawns;
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
