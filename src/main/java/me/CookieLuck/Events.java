package me.CookieLuck;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.event.player.PlayerInteractEvent.Action;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.level.Sound;
import cn.nukkit.utils.TextFormat;

public class Events implements Listener {

	GameLevel plugin;
	Main main;

	Events(Main main) {
		this.main = main;
	}

	@EventHandler
	public void itemdrop(PlayerDropItemEvent e) {
		if(GameLevel.getGameLevelByWorld(e.getPlayer().getLevel().getName()) != null){
			plugin = GameLevel.getGameLevelByWorld(e.getPlayer().getLevel().getName());
			if (plugin.configuring) {
				e.setCancelled(true);
			}
			if(plugin.waiting){
				e.setCancelled(true);
			}
		}


	}

	@EventHandler
	public void irse(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if(GameLevel.getGameLevelByWorld(e.getPlayer().getLevel().getName()) != null){
			plugin = GameLevel.getGameLevelByWorld(((p.getLevel().getName())));
			if(!plugin.waiting && !plugin.configuring && !plugin.building){
				plugin.die(p);
				return;
			}
			if(plugin.waiting){
				plugin.leave(e.getPlayer());
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
				if(fw.getResponse() != null){
					ElementInput ei = new ElementInput("","7");

					FormWindowUSW fww = new FormWindowUSW(fw.getResponse().getClickedButton().getText(),"Players");
					fww.addElement(ei);
					e.getPlayer().showFormWindow(fww);
				}

			}

			if(fw.id == 3){
				if(fw.getResponse().getClickedButton().getText() != null){
					GameLevel.getGameLevelByWorld(fw.getResponse().getClickedButton().getText()).joinPlayer(p);
				}
			}
		}

		if(e.getWindow() instanceof FormWindowUSW){
			FormWindowUSW fw;
			fw = (FormWindowUSW) e.getWindow();
				GameLevel gl = new GameLevel(Main.gameLevels.size(), fw.related,Integer.parseInt(fw.getResponse().getInputResponse(0)),main);
				gl.joinPlayer(p);
		}

		
	}

	@EventHandler
	public void damaged(EntityDamageEvent e) {
		Entity pe = e.getEntity();
		if (GameLevel.getGameLevelByWorld(((pe.getLevel().getName()))) == null) {
			return;
		}
		plugin = GameLevel.getGameLevelByWorld(((pe.getLevel().getName())));
		if (e.getEntity() instanceof Player) {

			Player p = (Player) e.getEntity();

			if (plugin.invulnerable) {
				e.setCancelled(true);
			}

			if (e.getDamage() >= p.getHealth()) {
				e.setCancelled(true);
				plugin.die(p);

			}
		}

	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(GameLevel.getGameLevelByWorld(e.getPlayer().getLevel().getName()) == null){
			return;
		}
		plugin = GameLevel.getGameLevelByWorld(e.getPlayer().getLevel().getName());


		
		Player p = e.getPlayer();
		if (e.getItem().getCustomName().equals(TextFormat.DARK_GREEN + "BACK TO LOBBY")) {
			plugin.leave(p);
		}

		if (plugin.configuring) {
			if (e.getAction().equals(Action.RIGHT_CLICK_AIR)) {

				if (e.getItem().getCustomName().equals(TextFormat.DARK_GREEN + "WAND")) {

					p.getInventory().clearAll();
					if (plugin.spawnList.size() != plugin.maxPlayers) {

						plugin.spawnList.add(new Spawn(p.getTargetBlock(1000).getX(), p.getTargetBlock(1000).getY() + 1,
								p.getTargetBlock(1000).getZ()));
						p.getLevel().addSound(p.getPosition(), Sound.BUBBLE_POP, 1, (float) 0.6);
					}

				}
			}

			if (e.getAction().equals(Action.RIGHT_CLICK_AIR)) {

				if (e.getItem().getCustomName().equals(TextFormat.DARK_RED + "BACK")) {
					p.getInventory().clearAll();
					if (plugin.spawnList.size() > 0) {
						p.getLevel().addSound(p.getPosition(), Sound.BLOCK_TURTLE_EGG_CRACK, 1, (float) 0.6);
						plugin.spawnList.remove(plugin.spawnList.size() - 1);
					}

				}
			}
			if (e.getItem().getCustomName().equals(TextFormat.DARK_GREEN + "DONE")) {
				p.getInventory().clearAll();
				p.getLevel().addSound(p.getPosition(), Sound.RANDOM_LEVELUP, 1, (float) 0.8);
				plugin.configuring = false;
				plugin.emptySpawns = false;
				plugin.waiting = true;
				
				p.sendActionBar(TextFormat.DARK_GREEN + "" + TextFormat.BOLD + "NICE!");
				main.saveGameLevels();

			}



		}
	}

}