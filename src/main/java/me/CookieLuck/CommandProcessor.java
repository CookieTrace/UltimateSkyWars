package me.CookieLuck;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.form.element.ElementButton;

import java.io.IOException;

public class CommandProcessor {

	public static boolean processCommand(CommandSender s, Command c, String[] args, Main plugin) throws IOException {

		if (!(s instanceof Player)){
			s.sendMessage("Only use these commands in game");
			//TODO: add nice msg to this with color
		}
		Player p = null;
		if (s instanceof Player) {
			p = (Player) s;
		}
		GameLevel gl = GameLevel.getGameLevelByWorld(p.getLevel().getName());

		if	(c.getName().equalsIgnoreCase("usw")){
			sendHelp(p);
		}
		if (c.getName().equalsIgnoreCase("usw") && args[0].equalsIgnoreCase("help")){
			sendHelp(p);
		}
		if (c.getName().equalsIgnoreCase("usw") && args[0].equalsIgnoreCase("join")) {
			FormWindowUSWS fw = new FormWindowUSWS(3,"Select game","Select a game");

			for(int i = 0; i<Main.gameLevels.size();i++){
				ElementButton btn = new ElementButton(Main.gameLevels.get(i).world);
				fw.addButton(btn);
			}

			p.showFormWindow(fw);
			p.getInventory().clearAll();

		}
		if (c.getName().equalsIgnoreCase("usw") && args[0].equalsIgnoreCase("leave")) {

			p.getInventory().clearAll();
			GameLevel.getGameLevelByWorld(p.getLevel().getName()).leave(p);

		}

		if (c.getName().equalsIgnoreCase("usw") && args[0].equalsIgnoreCase("saveGames")) {
			if(p.isOp()){
				plugin.saveGameLevels();
			}else{
				return false;
			}

		}
		if (c.getName().equalsIgnoreCase("usw") && args[0].equalsIgnoreCase("create")) {
			if(!(p.getPlayer().isOp())){
				return false;
			}
			FormWindowUSWS fw = new FormWindowUSWS(0, "Create GameLevel","SELECT A MAP");
			for (Integer integer : p.getServer().getLevels().keySet()) {
				int id = (int) integer;
				if (GameLevel.getGameLevelByWorld(p.getServer().getLevels().get(id).getName()) == null) {
					ElementButton eb = new ElementButton(p.getServer().getLevels().get(id).getName());
					fw.addButton(eb);
				}


			}

			p.showFormWindow(fw);

		}


		if (c.getName().equalsIgnoreCase("usw") && args[0].equalsIgnoreCase("setspawns")) {
			if(!(p.isOp())){
				return false;
			}
			p.getInventory().clearAll();
			if (!p.isOp()) {
				return (false);
			}
			gl.configuring = true;
			gl.waiting = false;

		}

		return true;
	}

	private static void sendHelp(Player player) {
		//TODO: Make a help message to send to the player
		//message gets send when player does /usw or /usw help
	}

}
