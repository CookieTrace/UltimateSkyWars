package me.CookieLuck;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.inventory.InventoryCloseEvent;
import cn.nukkit.event.inventory.InventoryOpenEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.event.player.PlayerInteractEvent.Action;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.inventory.BaseInventory;
import cn.nukkit.inventory.ChestInventory;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.TextFormat;
import me.CookieLuck.ChestSystem.ItemList;
import me.CookieLuck.USWFormWindows.FormWindowUSW;
import me.CookieLuck.USWFormWindows.FormWindowUSWS;
import me.CookieLuck.USWFormWindows.USWForms;

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
	public void openChest(PlayerInteractEvent e){
		if(e.getBlock().getId() == Block.CHEST || e.getBlock().getFullId() == 45.4){
			Player p = e.getPlayer();
			if(GameLevel.getGameLevelByWorld(p.getLevel().getName()) == null){
				return;
			}
			GameLevel gl = GameLevel.getGameLevelByWorld(p.getLevel().getName());
			gl.addChest(p,e.getBlock());


		}
	}


	@EventHandler
	public void openChestInventory(InventoryOpenEvent e){
		if(GameLevel.getGameLevelByWorld(e.getPlayer().getLevel().getName()) == null){
			return;
		}
		if(e.getInventory().getName().equalsIgnoreCase("Chest")){
			ItemList il = GameLevel.getGameLevelByWorld(e.getPlayer().getLevel().getName()).getItemList();

			for(int i = 0; i<e.getInventory().getSize(); i++){
				if(Math.random() < 0.5){
					e.getInventory().setItem(i,il.getItems().get((int)(Math.random() * (il.getItems().size() - 0) + 0)));
				}

			}
		}

	}

	@EventHandler
	public void closeChest(InventoryCloseEvent e){
		if(e.getInventory().getName().equalsIgnoreCase("Chest")){
			Player p = e.getPlayer();
			if(GameLevel.getGameLevelByWorld(p.getLevel().getName()) == null){
				return;
			}

			GameLevel gl = GameLevel.getGameLevelByWorld(p.getLevel().getName());
			Block b = gl.getChestToDestroy(p);

			p.getLevel().setBlock(b.getLocation(), Block.get(Block.AIR));
			Vector3 loc = new Vector3(b.getX()+0.5,b.getY(),b.getZ()+0.5);
			p.getLevel().addParticleEffect(loc, ParticleEffect.EXPLOSION_DEATH);

			p.getLevel().addSound(p.getLocation(),Sound.BLOCK_TURTLE_EGG_DROP,1,1);
			gl.removeChest(p);
		}
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
			if(fw.getId() == 0){
				ElementInput ei = new ElementInput("","7");
				if(fw.getResponse() != null){
					FormWindowUSW fww = new FormWindowUSW(fw.getResponse().getClickedButton().getText(),"Players");
					fww.addElement(ei);
					e.getPlayer().showFormWindow(fww);
				}

			}

			if(fw.getId() == 3){
				if(fw.getResponse() != null && !fw.getResponse().getClickedButton().getText().equals(TextFormat.DARK_AQUA+""+TextFormat.BOLD+"Ultimate"+TextFormat.DARK_GREEN+"SkyWars")){
					GameLevel.getGameLevelByWorld(fw.getResponse().getClickedButton().getText().split("│")[0].split(" ")[0]).joinPlayer(p);
				}

			}

			if(fw.getId() == 9){

			}
		}

		if(e.getWindow() instanceof FormWindowUSW){
			FormWindowUSW fw;
			fw = (FormWindowUSW) e.getWindow();
				GameLevel gl = new GameLevel(Main.gameLevels.size(), fw.getRelated(),Integer.parseInt(fw.getResponse().getInputResponse(0)),main);
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
	public void oninteractform(PlayerFormRespondedEvent e){
		if(e.getWindow() instanceof FormWindowUSWS) {
			FormWindowUSWS fw = (FormWindowUSWS) e.getWindow();
			if(fw.getResponse() == null){
				return;
			}
			if(fw.getResponse().getClickedButton().getText().equalsIgnoreCase(TextFormat.DARK_AQUA+""+TextFormat.BOLD+"Ultimate"+TextFormat.DARK_GREEN+"SkyWars")){

				e.getPlayer().showFormWindow(USWForms.getSelectRoom(main));
			}
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		CompoundTag tag = e.getItem() == null ? null : e.getItem().getNamedTag();
		if (tag == null) {
			return;
		}
		int uswItemType = tag.getInt("UltimateSkyWarsItem");

		Player p = e.getPlayer();
		GameLevel gameLevel = GameLevel.getGameLevelByWorld(p.getLevel().getName());
		if(gameLevel == null) {
			return;
		}

		if (uswItemType == 10) {
			gameLevel.leave(p);
			return;
		}
		if (gameLevel.isConfiguring()) {
			if (e.getAction() == Action.RIGHT_CLICK_AIR) {
				if (uswItemType == 11) {
					p.getInventory().clearAll();
					p.getUIInventory().clearAll();
					if (gameLevel.getSpawnList().size() != gameLevel.getMaxPlayers()) {
						gameLevel.getSpawnList().add(new Spawn((int)p.getTargetBlock(1000).getX(),
								p.getTargetBlock(1000).getY()+1,
								(int)p.getTargetBlock(1000).getZ()));
						p.getLevel().addSound(p.getPosition(), Sound.BUBBLE_POP, 1, (float) 0.6);
					}

				}else if (uswItemType == 12) {
					p.getInventory().clearAll();
					p.getUIInventory().clearAll();
					if (gameLevel.getSpawnList().size() > 0) {
						p.getLevel().addSound(p.getPosition(), Sound.BLOCK_TURTLE_EGG_CRACK, 1, (float) 0.6);
						gameLevel.getSpawnList().remove(gameLevel.getSpawnList().size() - 1);
					}
				}
			}
			if (uswItemType == 13) {
				p.getInventory().clearAll();
				p.getUIInventory().clearAll();
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