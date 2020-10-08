package me.CookieLuck;

import java.util.LinkedList;
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
import me.CookieLuck.lib.scoreboard.ScoreboardUtil;

public class GameThread extends NukkitRunnable {

	private Main plugin;
	private int tick;
	private long ticks;
	private int doubleTick;
	private GameLevel gl;
	private String map;
	private int ticksSinceStart;

	public GameThread(Main plugin, String map) {
		this.plugin = plugin;
		this.map = map;
		this.gl = GameLevel.getGameLevelByWorld(map);
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

	private void inGame() {
		//METODO A SEGUIR PARA TODOS LOS USUARIOS
		if (gl.getAlive().size() != gl.getMaxPlayers() && gl.isWaiting()) {
			String bottom = plugin.language.translateString("WaitForPlayers_Bottom", gl.getAlive().size(), gl.getMaxPlayers());
			LinkedList<String> list = new LinkedList<>();
			list.add("§1");
			list.add(TextFormat.DARK_GREEN + "" + TextFormat.BOLD + "WAITING FOR PLAYERS ");
			list.add("§2");
			list.add(TextFormat.DARK_GREEN + "" + TextFormat.BOLD + "Players: " + gl.getAlive().size() + "/" + gl.getMaxPlayers());
			list.add("§3");
			for (Player player : gl.getAlive()) {
				if (this.ticks % 20 == 0) {
					player.sendActionBar(bottom);
					ScoreboardUtil.getScoreboard().showScoreboard(player, "UltimateSkyWars", list);
				}
				Item item = Item.get(262);
				item.setCustomName(this.plugin.language.translateString("BackToLobby"));
				item.getNamedTag().putInt("UltimateSkyWarsItem", 10);
				player.getInventory().setItem(0, item);
				player.getFoodData().setLevel(20);
				player.setHealth(20);
			}
		}else {
			gl.setWaiting(false);

			String bottom = TextFormat.RED + "" + TextFormat.BOLD + "" +
					gl.getAlive().size() + " ALIVE" + " | " + gl.getDead().size() + " DEADS";
			for (Player player : gl.getAlive()) {
				if (this.ticks % 20 == 0) {
					player.sendActionBar(bottom);
					LinkedList<String> list = new LinkedList<>();
					list.add("§1");
					list.add(TextFormat.GREEN + "" + TextFormat.BOLD + "Alive: " + gl.getAlive().size() + " ");
					list.add("§2");
					list.add(TextFormat.GREEN + "" + TextFormat.BOLD + "Kills: " + gl.getPlayerKills(player) + " ");
					list.add("§3");
					ScoreboardUtil.getScoreboard().showScoreboard(player, "UltimateSkyWars", list);
				}
				player.setGamemode(0);
			}

			for (Player player : gl.getDead()) {
				if (this.ticks % 20 == 0) {
					player.sendActionBar(bottom);
					LinkedList<String> list = new LinkedList<>();
					list.add("§1");
					list.add(TextFormat.GREEN + "" + TextFormat.BOLD + "Alive: " + gl.getAlive().size() + " ");
					list.add("§2");
					list.add(TextFormat.GREEN + "" + TextFormat.BOLD + "Kills: " + gl.getPlayerKills(player) + " ");
					list.add("§3");
					ScoreboardUtil.getScoreboard().showScoreboard(player, "UltimateSkyWars", list);
				}
				player.setGamemode(3);
			}

			/*if(gl.getAlive().size() == 1) {
				Player p = gl.getAlive().get(0);
				gl.win(p);
				Main.spawnFirework(p.getPosition(), p.getLevel(), DyeColor.BLUE,true,true, ItemFirework.FireworkExplosion.ExplosionType.BURST);

			}*/

			if (ticksSinceStart == 1) {
				for (Player p : gl.getAlive()) {
					p.getInventory().clearAll();
					p.getUIInventory().clearAll();
					Vector3 pos = new Vector3(p.getX(), p.getY() - 1, p.getZ());
					p.getLevel().setBlock(pos, Block.get(Block.AIR));
					p.sendTitle("", plugin.language.translateString("GameStartedTitle"));
					p.getLevel().addSound(p.getLocation(), Sound.MOB_GHAST_AFFECTIONATE_SCREAM, 1, (float) 0.3);
				}
				gl.setInvulnerable(true);
			}

			if(ticksSinceStart == 300) {
				gl.setInvulnerable(false);
				for (Player p : gl.getAlive()) {
					p.getLevel().addSound(p.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 1, (float) 0.7);
					p.sendTitle("", plugin.language.translateString("InvulnerabilityOver"));
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
					Item wand = Item.get(280);
					wand.setCustomName(this.plugin.language.translateString("SetSpawn"));
					wand.getNamedTag().putInt("UltimateSkyWarsItem", 11);
					p.getInventory().setItem(0, wand);
				}

				if (gl.getSpawnList().size() != 0) {
					Item back = Item.get(257);
					back.setCustomName(this.plugin.language.translateString("RemoveSpawn"));
					back.getNamedTag().putInt("UltimateSkyWarsItem", 12);
					p.getInventory().setItem(8, back);
				}

				if (gl.getSpawnList().size() == gl.getMaxPlayers()) {
					Item done = Item.get(262);
					done.setCustomName(this.plugin.language.translateString("DoneSettingsSpawns"));
					done.getNamedTag().putInt("UltimateSkyWarsItem", 13);
					p.getInventory().setItem(4, done);
				}

			}

		}

		this.particleEffect();
		
	}

	private void particleEffect() {
		for (Spawn spawn : gl.getSpawnList()) {
			Vector3 pos;
			if (doubleTick <= 20) {
				pos = new Vector3(spawn.x + 0.5, spawn.y + (tick * 0.1), spawn.z + 0.5);
			} else {
				pos = new Vector3(spawn.x + 0.5, (spawn.y + 2) - (tick * 0.1), spawn.z + 0.5);
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
