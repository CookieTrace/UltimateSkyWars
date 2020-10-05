package me.CookieLuck;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.mob.EntityZombie;
import cn.nukkit.form.element.Element;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.level.Sound;
import cn.nukkit.utils.TextFormat;

public class CommandProcessor {

	public static boolean processCommand(CommandSender s, Command c, String[] args, Main plugin) throws IOException {
		GameLevel gl = GameLevel.getGameLevelByWorld(((Player) s).getPlayer().getLevel().getName());

		if (c.getName().equals("usw") && args[0].equals("join")) {
			FormWindowUSWS fw = new FormWindowUSWS(3,"Slect game","Slect a game");

			for(int i = 0; i<Main.gameLevels.size();i++){
				ElementButton btn = new ElementButton(Main.gameLevels.get(i).world);
				fw.addButton(btn);
			}
				Player p = ((Player) s).getPlayer();
			p.showFormWindow(fw);
			p.getInventory().clearAll();

		}
		if (c.getName().equals("usw") && args[0].equals("leave")) {

			Player p = ((Player) s).getPlayer();
			p.getInventory().clearAll();
			GameLevel.getGameLevelByWorld(p.getLevel().getName()).leave(p);

		}

		if (c.getName().equals("usw") && args[0].equals("saveGames")) {
			Player p = ((Player) s).getPlayer();
			if(p.isOp()){
				plugin.saveGameLevels();
			}else{
				return false;
			}

		}
		if (c.getName().equals("usw") && args[0].equals("create")) {
			if(!((Player) s).getPlayer().isOp()){
				return false;
			}
			Player p = ((Player) s).getPlayer();
			FormWindowUSWS fw = new FormWindowUSWS(0, "Create GameLevel","SELECT A MAP");
			Iterator it = p.getServer().getLevels().keySet().iterator();
			while(it.hasNext()){
				int id = (int) it.next();
				if(GameLevel.getGameLevelByWorld(p.getServer().getLevels().get(id).getName()) == null && plugin.getServer().getDefaultLevel() != plugin.getServer().getLevel(id)){
					ElementButton eb = new ElementButton(p.getServer().getLevels().get(id).getName());
					fw.addButton(eb);
				}


			}

			p.showFormWindow(fw);
			
		}
		
		
		if (c.getName().equals("usw") && args[0].equals("setspawns")) {
			if(!((Player) s).getPlayer().isOp()){
				return false;
			}
			if (s instanceof Player) {
				Player p = ((Player) s).getPlayer();
				p.getInventory().clearAll();
				if (!p.isOp()) {
					return (false);
				}
			}
			gl.configuring = true;
			gl.waiting = false;

		}

		return true;
	}

}
