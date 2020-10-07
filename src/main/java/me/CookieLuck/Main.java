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

	public static List<GameLevel> gameLevels = new LinkedList<>();
	public String worldsDir;
	public Level lobby;

	@Override
	public void onEnable() {
		this.loadConfig();
		this.loadGameLevels();

		this.getLogger().info("Ultimate SkyWars Enabled");
		this.getServer().getPluginManager().registerEvents(new Events(this), this);
		this.getServer().setAutoSave(false);

	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return CommandProcessor.processCommand(sender, command, args, this);
	}
	
	List<Spawn> procesarSpawns(int id) {
		List<Spawn> spawns = new ArrayList<>();
		try {
			FileReader fr = new FileReader(this.getDataFolder() + "/GameLevels/"+id+"/Config.uws");
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
			FileReader fr = new FileReader(this.getDataFolder() + "/GameLevels/"+id+"/Config.uws");
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(fr);
			br.readLine();br.readLine();return(Integer.parseInt(br.readLine()));

		}catch(Exception e) {

		}
		return 0;
	}

	String procesarMundo(int id) {
		try {
			FileReader fr = new FileReader(this.getDataFolder() + "/GameLevels/"+id+"/Config.uws");
			BufferedReader br = new BufferedReader(fr);
			br.readLine();return(br.readLine());


		}catch(Exception e) {
			
		}
		return null;
	}

	public void saveLobby(){
		File f = new File(getDataFolder()+"/");
		if(!f.exists()){
			f.mkdir();
		}
		try {
			FileWriter fw = new FileWriter(this.getDataFolder()+"/Config.usw");
			BufferedWriter bw = new BufferedWriter(fw);
			bw.flush();
			bw.write(getServer().getDefaultLevel().getName()+ "\n" + this.getServer().getFilePath() + "/worlds");
			bw.close();
			fw.close();
		} catch (IOException ignored) {
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

	public void saveBackups() {
		File levels = new File(worldsDir);
		File destiny = new File(this.getDataFolder() + "/LevelBackups");
		if(!destiny.exists()) {
			destiny.mkdir();
		}
		if(!levels.exists()){
			getServer().getLogger().error("[UltimateSkyWars] WORLDS FOLDER NOT FOUND, CHANGE IT ON Config.usw");
			return;
		}
		//TODO Only backup game maps
		FileUtils.copyDirectoryContents(levels, destiny);
	}

	public void loadConfig() {

		File f = new File(this.getDataFolder() + "/Config.usw");
		if(!f.exists()) {
			saveLobby();
		}
		try {
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);

			lobby = getServer().getLevelByName(br.readLine());
			worldsDir = br.readLine();
			fr.close();
			br.close();
		} catch (Exception ignored) {

		}


	}

	void loadGameLevels() {
		File f = new File(this.getDataFolder()+"/");
		if(!f.exists()){
			f.mkdir();
		}
		f = new File(this.getDataFolder() + "/GameLevels");
		if(!f.exists()) {
			f.mkdir();
		}
		for(int i = 0; i<f.listFiles().length; i++) {
			f = new File(this.getDataFolder() + "/GameLevels/"+i);
			if(!f.exists()) {
				f.mkdir();
			}
			GameLevel gl = new GameLevel(i, procesarSpawns(i), procesarMundo(i), getMaxPlayers(i), this);
			new GameThread(this, procesarMundo(i)).runTaskTimer(this, 0, 1);
		}
		//this.saveBackups();
	}
	
	public void saveGameLevels() {
		FileWriter fw;
		for (GameLevel gameLevel : gameLevels) {
			try {
				File f = new File(this.getDataFolder() + "/GameLevels/"+gameLevel.getId());
				if(!f.exists()) {
					f.mkdir();
				}
				fw = new FileWriter(getDataFolder() + "/GameLevels/" + gameLevel.getId() + "/Config.uws");
				BufferedWriter bw = new BufferedWriter(fw);
				fw.flush();
				bw.write(gameLevel.toString());
				bw.close();
				fw.close();

			} catch (IOException e) {

				e.printStackTrace();
			}
		}
			saveBackups();
	}

	

}
