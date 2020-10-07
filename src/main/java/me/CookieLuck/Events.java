package me.CookieLuck;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.event.player.PlayerInteractEvent.Action;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.utils.TextFormat;

public class Events implements Listener {

	private final Main main;

	Events(Main main) {
		this.main = main;
	}

	@EventHandler
	public void itemdrop(PlayerDropItemEvent e) {
		GameLevel gameLevel = GameLevel.getGameLevelByWorld(e.getPlayer().getLevel().getName());
		if(gameLevel != null){
			if (gameLevel.isConfiguring() || gameLevel.isWaiting()) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void testing(PlayerMoveEvent e){



	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		Server.getInstance().getScheduler().scheduleDelayedTask(this.main, () -> {
			if(GameLevel.getGameLevelByWorld(player.getLevel().getName()) != null) {
				player.teleport(this.main.lobby.getSafeSpawn());
				player.setGamemode(0);
			}
		}, 10);
	}

	@EventHandler
	public void irse(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		GameLevel gameLevel = GameLevel.getGameLevelByWorld(((p.getLevel().getName())));
		if(gameLevel != null){
			if(!gameLevel.isWaiting() && !gameLevel.isConfiguring() && !gameLevel.isBuilding()){
				gameLevel.die(p, EntityDamageEvent.DamageCause.CUSTOM);
				return;
			}
			if(gameLevel.isWaiting()){
				gameLevel.leave(e.getPlayer());
			}

		}
	}


	@EventHandler
	public void interfaz(PlayerFormRespondedEvent e) {
		Player p = e.getPlayer();
		

		if(e.getWindow() instanceof FormWindowUSWS){
			FormWindowUSWS fw;
			fw = (FormWindowUSWS) e.getWindow();
			if(fw.id == 0){
				ElementInput ei = new ElementInput("","7");
				if(fw.getResponse() != null){
					FormWindowUSW fww = new FormWindowUSW(fw.getResponse().getClickedButton().getText(),"Players");
					fww.addElement(ei);
					e.getPlayer().showFormWindow(fww);
				}

			}

			if(fw.id == 3){
				if(fw.getResponse() != null){
					GameLevel.getGameLevelByWorld(fw.getResponse().getClickedButton().getText().split("│")[0].split(" ")[0]).joinPlayer(p);
				}

			}
		}

		if(e.getWindow() instanceof FormWindowUSW){
			FormWindowUSW fw;
			fw = (FormWindowUSW) e.getWindow();
				GameLevel gl = new GameLevel(Main.gameLevels.size(), fw.related,Integer.parseInt(fw.getResponse().getInputResponse(0)),main);
				gl.joinForcePlayer(p);
		}

		
	}

	@EventHandler
	public void damaged(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			GameLevel gameLevel = GameLevel.getGameLevelByWorld(((p.getLevel().getName())));
			if (gameLevel == null) {
				return;
			}

			if (gameLevel.isInvulnerable()) {
				e.setCancelled(true);
			}

			if (e.getDamage() >= p.getHealth()) {
				e.setCancelled(true);
				gameLevel.die(p,e.getCause());
				if (e instanceof EntityDamageByEntityEvent) {
					Entity damager = ((EntityDamageByEntityEvent) e).getDamager();
					if (damager instanceof Player) {
						gameLevel.addPlayerKills((Player) damager);
					}
				}
			}
		}

	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Item item = e.getItem();
		if (item == null) {
			return;
		}
		Player p = e.getPlayer();
		GameLevel gameLevel = GameLevel.getGameLevelByWorld(p.getLevel().getName());
		if(gameLevel == null) {
			return;
		}

		if (item.getCustomName().equals(TextFormat.DARK_GREEN + "BACK TO LOBBY")) {
			gameLevel.leave(p);
		}


		if (gameLevel.isConfiguring()) {
			if (e.getAction() == Action.RIGHT_CLICK_AIR) {
				if (item.getCustomName().equals(TextFormat.DARK_GREEN + "WAND")) {
					p.getInventory().clearAll();
					p.getUIInventory().clearAll();
					if (gameLevel.getSpawnList().size() != gameLevel.getMaxPlayers()) {
						gameLevel.getSpawnList().add(new Spawn((int)p.getTargetBlock(1000).getX(),
								p.getTargetBlock(1000).getY()+1,
								(int)p.getTargetBlock(1000).getZ()));
						p.getLevel().addSound(p.getPosition(), Sound.BUBBLE_POP, 1, (float) 0.6);
					}

				}else if (item.getCustomName().equals(TextFormat.DARK_RED + "BACK")) {
					p.getInventory().clearAll();
					p.getUIInventory().clearAll();
					if (gameLevel.getSpawnList().size() > 0) {
						p.getLevel().addSound(p.getPosition(), Sound.BLOCK_TURTLE_EGG_CRACK, 1, (float) 0.6);
						gameLevel.getSpawnList().remove(gameLevel.getSpawnList().size() - 1);
					}
				}
			}
			if (item.getCustomName().equals(TextFormat.DARK_GREEN + "DONE")) {
				p.getInventory().clearAll();
				p.getLevel().addSound(p.getPosition(), Sound.RANDOM_LEVELUP, 1, (float) 0.8);
				gameLevel.setConfiguring(false);
				gameLevel.setEmptySpawns(false);
				gameLevel.setWaiting(true);

				p.sendActionBar(TextFormat.DARK_GREEN + "" + TextFormat.BOLD + "NICE!");
				main.saveGameLevels();
				new GameThread(gameLevel.getPlugin(),gameLevel.getWorld()).runTaskTimer(gameLevel.getPlugin(), 0, 1);;

			}



		}
	}

}