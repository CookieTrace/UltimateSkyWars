package me.CookieLuck;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerDropItemEvent;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerInteractEvent.Action;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.form.element.ElementInput;
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
			if (gameLevel.configuring || gameLevel.waiting) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void irse(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		GameLevel gameLevel = GameLevel.getGameLevelByWorld(((p.getLevel().getName())));
		if(gameLevel != null){
			if(!gameLevel.waiting && !gameLevel.configuring && !gameLevel.building){
				gameLevel.die(p);
				return;
			}
			if(gameLevel.waiting){
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
					GameLevel.getGameLevelByWorld(fw.getResponse().getClickedButton().getText()).joinPlayer(p);
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

			if (gameLevel.invulnerable) {
				e.setCancelled(true);
			}

			if (e.getDamage() >= p.getHealth()) {
				e.setCancelled(true);
				gameLevel.die(p);

			}
		}

	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		GameLevel gameLevel = GameLevel.getGameLevelByWorld(e.getPlayer().getLevel().getName());
		if(gameLevel == null){
			return;
		}

		Player p = e.getPlayer();
		if (e.getItem().getCustomName().equals(TextFormat.DARK_GREEN + "BACK TO LOBBY")) {
			gameLevel.leave(p);
		}

		if (gameLevel.configuring) {
			if (e.getAction() == Action.RIGHT_CLICK_AIR) {
				if (e.getItem().getCustomName().equals(TextFormat.DARK_GREEN + "WAND")) {
					p.getInventory().clearAll();
					p.getUIInventory().clearAll();
					if (gameLevel.spawnList.size() != gameLevel.maxPlayers) {
						gameLevel.spawnList.add(new Spawn(p.getTargetBlock(1000).getX(),
								p.getTargetBlock(1000).getY() + 1,
								p.getTargetBlock(1000).getZ()));
						p.getLevel().addSound(p.getPosition(), Sound.BUBBLE_POP, 1, (float) 0.6);
					}

				}else if (e.getItem().getCustomName().equals(TextFormat.DARK_RED + "BACK")) {
					p.getInventory().clearAll();
					p.getUIInventory().clearAll();
					if (gameLevel.spawnList.size() > 0) {
						p.getLevel().addSound(p.getPosition(), Sound.BLOCK_TURTLE_EGG_CRACK, 1, (float) 0.6);
						gameLevel.spawnList.remove(gameLevel.spawnList.size() - 1);
					}
				}
			}
			if (e.getItem().getCustomName().equals(TextFormat.DARK_GREEN + "DONE")) {
				p.getInventory().clearAll();
				p.getLevel().addSound(p.getPosition(), Sound.RANDOM_LEVELUP, 1, (float) 0.8);
				gameLevel.configuring = false;
				gameLevel.emptySpawns = false;
				gameLevel.waiting = true;
				
				p.sendActionBar(TextFormat.DARK_GREEN + "" + TextFormat.BOLD + "NICE!");
				main.saveGameLevels();

			}



		}
	}

}