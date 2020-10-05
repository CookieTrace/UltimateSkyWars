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

	int id;
	Main plugin;
	List<Spawn> spawnList;
	String world;
	public boolean configuring;
	public int maxPlayers;
	public boolean emptySpawns;
	public boolean building;
	public boolean waiting;
	public boolean invulnerable;
	public boolean gamestarted;
	public List<Player> dead;
	public List<Player> alive;

	GameLevel(int id, String world, int maxPlayers, Main plugin){

		building = false;
		this.plugin = plugin;
		this.id = id;
		this.spawnList = new ArrayList<Spawn>();
		this.world = world;
		emptySpawns = true;
		configuring = false;
		this.maxPlayers = maxPlayers;
		waiting = true;
		invulnerable = false;
		dead = new ArrayList<Player>();
		gamestarted = false;
		alive = new ArrayList<Player>();
		addToMain();
	}

	private void addToMain(){
		Main.gameLevels.add(this);
		plugin.saveGameLevels();
		new GameThread(plugin, this.world).runTaskTimer(plugin, 0, 1);
	}

	
	GameLevel(int id, List spawns,String world, int maxPlayers, Main plugin){

		this(id,world,maxPlayers,plugin);
		this.spawnList = spawns;
		emptySpawns = false;

	}


	public void die(Player p){
		p.setGamemode(3);

		Map<Integer, Item> map = p.getInventory().getContents();

		Iterator<Integer> it = map.keySet().iterator();

		int itemID = 0;
		while (it.hasNext()) {
			itemID = (int) it.next();
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

		gamestarted = false;
		waiting = true;
		building = true;
		for(int i = 0; i<alive.size();i++){
			alive.get(i).sendTitle("",TextFormat.DARK_AQUA+""+TextFormat.BOLD+""+p.getName()+" WON THE GAME!");
		}

		for(int i = 0; i<dead.size();i++){
			dead.get(i).sendTitle("",TextFormat.DARK_AQUA+""+TextFormat.BOLD+""+p.getName()+" WON THE GAME!");
		}
		alive = new ArrayList<Player>();
		dead = new ArrayList<Player>();
		new Thread( new Runnable() {
			public void run()  {
				try  { Thread.sleep( 5000 ); }
				catch (InterruptedException ie)  {}
				for(int i = 0; i<alive.size();i++){
					Location loc = new Location(p.getServer().getDefaultLevel().getSpawnLocation().getX(),p.getServer().getDefaultLevel().getSpawnLocation().getY(),p.getServer().getDefaultLevel().getSpawnLocation().getZ(), p.getServer().getDefaultLevel());
					alive.get(i).teleport(loc);
					dead.get(i).setGamemode(0);
				}

				for(int i = 0; i<dead.size();i++){
					Location loc = new Location(p.getServer().getDefaultLevel().getSpawnLocation().getX(),p.getServer().getDefaultLevel().getSpawnLocation().getY(),p.getServer().getDefaultLevel().getSpawnLocation().getZ(), p.getServer().getDefaultLevel());
					dead.get(i).teleport(loc);
					dead.get(i).setGamemode(0);
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
				try  { Thread.sleep( 30000 );} catch (InterruptedException e) {

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
		String spawnsString = "";
		for(int i = 0; i<spawnList.size();i++) {
			spawnsString += ""+spawnList.get(i).x+":"+spawnList.get(i).y+":"+spawnList.get(i).z;
			spawnsString += "\n";
			
		}
		return(id+"\n"+world+"\n"+maxPlayers+"\n"+spawnsString);
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
		if (emptySpawns) {
			return false;
		}
		return true;
	}
	
	public static GameLevel getGameLevelByWorld(String world) {
		for(int i = 0; i<Main.gameLevels.size();i++) {
			if(Main.gameLevels.get(i).world.equals(world)) {
				return Main.gameLevels.get(i);
			}
		}
		return null;
	}
	public static GameLevel getGameLevelById(int id) {
		return Main.gameLevels.get(id);
	}
	

}
