package me.CookieLuck;

import java.util.List;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemFirework;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.NukkitRunnable;
import cn.nukkit.utils.DyeColor;
import cn.nukkit.utils.TextFormat;
import me.CookieLuck.lib.scoreboard.ltname.Scoreboard;
import me.CookieLuck.lib.scoreboard.ltname.ScoreboardData;

public class GameThread extends NukkitRunnable {
	Main plugin;
	int tick;
	long ticks;
	int doubleTick;
	GameLevel gl;
	String map;
	int ticksSinceStart;

	public GameThread(Main plugin, String map) {
		this.plugin = plugin;
		this.map = map;
		gl = GameLevel.getGameLevelByWorld(map);
	}

	@Override
	public void run() {

		if(gl.isWaiting()){
			ticksSinceStart = 0;
		}

		if(!gl.isWaiting() && !gl.isConfiguring()){
			ticksSinceStart++;
		}

		if (gl.isEmptySpawns()) {
			if (!gl.isConfiguring()) {
				noConfigurado();
			}
		}
		if (gl.isConfiguring()) {
			configurando();
		}

		if (gl.startableGame() && !gl.isConfiguring()) {
			inGame();
		}

		if (tick == 20) {
			tick = 0;
		}
		if (doubleTick == 40) {
			doubleTick = 0;
		}
		if (gl.isConfiguring() && !gl.isWaiting()) {
			tick++;
			ticks++;
			doubleTick++;
		}
		if (!gl.isConfiguring() && !gl.isWaiting()) {
			tick++;
			ticks++;
			doubleTick++;
		}
		if (gl.isConfiguring() && gl.isWaiting()) {
			tick++;
			ticks++;
			doubleTick++;
		}
		gl = GameLevel.getGameLevelByWorld(map);
		
	}

	private void inGame(){
		
			//METODO A SEGUIR PARA TODOS LOS USUARIOS
		
		if (gl.getAlive().size() != gl.getMaxPlayers() && gl.isWaiting()) {


			for (int i = 0; i<gl.getAlive().size(); i++) {
				Player p  = gl.getAlive().get(i);
				p.sendActionBar(TextFormat.DARK_GREEN + "" + TextFormat.BOLD + "WAITING FOR PLAYERS "
						+ gl.getAlive().size() + "/" + gl.getMaxPlayers());
				
				Item done = new Item(262);
				done.setCustomName(TextFormat.DARK_GREEN + "BACK TO LOBBY");
				p.getInventory().setItem(0, done);
				p.getFoodData().setLevel(20);
				p.setHealth(20);
			}
		}else{
			gl.setWaiting(false);
			List<Player> ps = gl.getAlive();
			List<Player> psmuertos = gl.getDead();


			for (Player player : ps) {
				player.sendActionBar(TextFormat.RED + "" + TextFormat.BOLD + "" + gl.getAlive().size() + " ALIVE" + " | " + gl.getDead().size() + " DEADS");
				player.setGamemode(0);
			}

			for (Player psmuerto : psmuertos) {
				psmuerto.sendActionBar(TextFormat.RED + "" + TextFormat.BOLD + "" + gl.getAlive().size() + " ALIVE" + " | " + gl.getDead().size() + " DEADS");
				psmuerto.setGamemode(3);
			}

			if(gl.getAlive().size()==1){
				Player p = gl.getAlive().get(0);
				gl.win(gl.getAlive().get(0));
				Main.spawnFirework(p.getPosition(),p.getLevel(), DyeColor.BLUE,true,true, ItemFirework.FireworkExplosion.ExplosionType.BURST);

			}

			if (ticksSinceStart == 1) {
				for (Player p : ps) {
					p.getInventory().clearAll();
					Vector3 pos = new Vector3(p.getX(), p.getY() - 1, p.getZ());
					p.getLevel().setBlock(pos, Block.get(Block.AIR));
					p.sendTitle("", TextFormat.DARK_AQUA + "" + TextFormat.BOLD + "" + "GO!");
					p.getLevel().addSound(p.getLocation(), Sound.MOB_GHAST_AFFECTIONATE_SCREAM, 1, (float) 0.3);
				}
				gl.setInvulnerable(true);
			}

			if(ticksSinceStart == 300){
				gl.setInvulnerable(false);
				for (Player p : ps) {
					p.getLevel().addSound(p.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 1, (float) 0.7);
					p.sendTitle("", TextFormat.BLUE + "" + TextFormat.BOLD + "" + "invincibility finished");
				}
			}
		}
	}

	private void configurando() {
		List<Player> players = gl.getAlive();

		for (Player p : players) {

			if (p != null) {

				p.sendActionBar(TextFormat.BOLD + "" + TextFormat.DARK_GREEN + "SET SPAWN Nº: "
						+ gl.getSpawnList().size() + " || " + (gl.getMaxPlayers() - gl.getSpawnList().size()) + " LEFT");
				p.setGamemode(1);

				if (gl.getSpawnList().size() != gl.getMaxPlayers()) {
					Item wand = new Item(280);
					wand.setCustomName("§2WAND");
					p.getInventory().setItem(0, wand);
				}

				if (gl.getSpawnList().size() != 0) {
					Item back = new Item(257);
					back.setCustomName(TextFormat.DARK_RED + "BACK");
					p.getInventory().setItem(8, back);
				}

				if (gl.getSpawnList().size() == gl.getMaxPlayers()) {
					Item done = new Item(262);
					done.setCustomName(TextFormat.DARK_GREEN + "DONE");
					p.getInventory().setItem(4, done);
				}

			}

		}

		particleEffect();
		
	}

	private void particleEffect() {
		for (int i = 0; i < gl.getSpawnList().size(); i++) {

			Vector3 pos;
			if (doubleTick <= 20) {
				pos = new Vector3(gl.getSpawnList().get(i).x + 0.5, gl.getSpawnList().get(i).y + (tick * 0.1),
						gl.getSpawnList().get(i).z + 0.5);
			} else {
				pos = new Vector3(gl.getSpawnList().get(i).x + 0.5,
						(gl.getSpawnList().get(i).y + 2) - (tick * 0.1), gl.getSpawnList().get(i).z + 0.5);
			}
			plugin.getServer().getLevelByName(gl.getWorld()).addParticleEffect(pos, ParticleEffect.REDSTONE_TORCH_DUST);

		}
	}

	private void noConfigurado() {
		if (gl.getSpawnList().size() == 0) {
			List<Player> players = gl.getAlive();

			for (Player p : players) {
				if (p != null) {
					p.sendActionBar(
							TextFormat.BOLD + "" + TextFormat.DARK_RED + "Spawns not setted, USE /usw setspawns");
				}
			}
		}
		
	}

}
