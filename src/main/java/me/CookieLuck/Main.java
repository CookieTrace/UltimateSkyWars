package me.CookieLuck;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.item.EntityFirework;
import cn.nukkit.item.ItemFirework;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.DyeColor;
import org.iq80.leveldb.util.FileUtils;

public class Main extends PluginBase {

	static List<GameLevel> gameLevels = new LinkedList<GameLevel>();
	Level lobby;

	public void onEnable() {
		
		

		loadGameLevels();
		loadConfig();

		this.getLogger().info("Ultimate SkyWars Enabled");
		this.getServer().getPluginManager().registerEvents(new Events(this), this);
		this.getServer().setAutoSave(false);

	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		try {
			return CommandProcessor.processCommand(sender, command, args, this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	List<Spawn> procesarSpawns(int id) {
		List<Spawn> spawns = new ArrayList<>();
		try {
			FileReader fr = new FileReader(this.getDataFolder() + "/GameLevels/"+id+".uws");
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(fr);
			br.readLine();br.readLine();br.readLine();
			String line;
			while ((line = br.readLine()) != null) {
				String[] coordinates = line.split(":");
				spawns.add(new Spawn(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]),
						Double.parseDouble(coordinates[2])));
			}
			fr.close();
			br.close();
			
		} catch (IOException e) {
			
		}
		
		return spawns;
	}

	int getMaxPlayers(int id){
		try {
			FileReader fr = new FileReader(this.getDataFolder() + "/GameLevels/"+id+".uws");
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(fr);
			br.readLine();br.readLine();return(Integer.parseInt(br.readLine()));

		}catch(Exception e) {

		}
		return 0;
	}

	String procesarMundo(int id) {
		try {
			FileReader fr = new FileReader(this.getDataFolder() + "/GameLevels/"+id+".uws");
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(fr);
			br.readLine();return(br.readLine());
			
		}catch(Exception e) {
			
		}
		return null;
	}

	public void saveLobby(String world){
		try {
			FileWriter fw = new FileWriter(this.getDataFolder()+"/Config.usw");
			BufferedWriter bw = new BufferedWriter(fw);
			bw.flush();
			bw.write(world);
			bw.close();
			fw.close();
		} catch (IOException e) {
		}
	}

	public static void spawnFirework(Vector3 pos, Level level, DyeColor color, boolean flicker, boolean trail, ItemFirework.FireworkExplosion.ExplosionType explosionType) {
		ItemFirework item = new ItemFirework();
		CompoundTag tag = new CompoundTag();

		CompoundTag ex = new CompoundTag()
				.putByteArray("FireworkColor", new byte[]{(byte) color.getDyeData()})
				.putByteArray("FireworkFade", new byte[]{})
				.putBoolean("FireworkFlicker", flicker)
				.putBoolean("FireworkTrail", trail)
				.putByte("FireworkType", explosionType.ordinal());

		tag.putCompound("Fireworks", new CompoundTag("Fireworks")
				.putList(new ListTag<CompoundTag>("Explosions").add(ex))
				.putByte("Flight", 0));

		item.setNamedTag(tag);

		CompoundTag nbt = new CompoundTag()
				.putList(new ListTag<DoubleTag>("Pos")
						.add(new DoubleTag("", pos.x + 0.5))
						.add(new DoubleTag("", pos.y + 0.5))
						.add(new DoubleTag("", pos.z + 0.5)))
				.putList(new ListTag<DoubleTag>("Motion")
						.add(new DoubleTag("", 0))
						.add(new DoubleTag("", 0))
						.add(new DoubleTag("", 0)))
				.putList(new ListTag<FloatTag>("Rotation")
						.add(new FloatTag("", 0))
						.add(new FloatTag("", 0)))
				.putCompound("FireworkItem", NBTIO.putItemHelper(item));

		EntityFirework entity = new EntityFirework(level.getChunk((int) pos.x >> 4, (int) pos.z >> 4), nbt);
		try {
			Field field = entity.getClass().getDeclaredField("lifetime");
			field.setAccessible(true);
			field.set(entity, 0);
		} catch(Exception exc) {}
		entity.spawnToAll();
	}
	void saveBackups(){
		File levels = new File(this.getDataFolder()+"../"+"../"+"../"+"/worlds");
		File destiny = new File(this.getDataFolder() + "/LevelBackups");
		if(!destiny.exists()) {
			destiny.mkdir();
		}
		FileUtils.copyDirectoryContents(levels,destiny);
	}

	void loadConfig(){

		File f = new File(this.getDataFolder()+"/Config.usw");
		if(!f.exists()) {
			saveLobby(getServer().getDefaultLevel().getName());
		}
		try {
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);

			lobby = getServer().getLevelByName(br.readLine());
			fr.close();
			br.close();
		} catch (FileNotFoundException e) {

		} catch (IOException e) {
		}

	}

	void loadGameLevels(){
			File f = new File(this.getDataFolder()+"/");
			if(!f.exists()){
				f.mkdir();
			}
			f = new File(this.getDataFolder() + "/GameLevels");
			if(!f.exists()) {
				f.mkdir();
			}
			for(int i = 0; i<f.listFiles().length; i++) {
				GameLevel gl = new GameLevel(i, procesarSpawns(i), procesarMundo(i), getMaxPlayers(i), this);
				new GameThread(this, procesarMundo(i)).runTaskTimer(this, 0, 1);
			}
	}
	
	public void saveGameLevels() {
		FileWriter fw;
			Iterator it = gameLevels.iterator();
			while(it.hasNext()) {
				try {
					GameLevel gl = (GameLevel) it.next();
					fw = new FileWriter(getDataFolder() + "/GameLevels/"+gl.id+".uws");
					BufferedWriter bw = new BufferedWriter(fw);
					fw.flush();
					bw.write(gl.toString());
					bw.close();
					fw.close();
					
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			}
			saveBackups();
	}

	

}
