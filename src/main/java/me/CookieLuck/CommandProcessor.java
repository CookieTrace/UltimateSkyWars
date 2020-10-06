package me.CookieLuck;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.utils.TextFormat;

public class CommandProcessor {

	public static boolean processCommand(CommandSender s, Command c, String[] args, Main plugin) {
		if (s instanceof Player) {
			Player p = (Player) s;
			GameLevel gl = GameLevel.getGameLevelByWorld(p.getLevel().getName());
			if (c.getName().equalsIgnoreCase("usw")) {
				if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
					sendHelp(p);
					return true;
				}
				switch (args[0]) {
					case "join":
						if (!p.hasPermission("usw.join")) {
							sendNoPerm(p);
							return true;
						}
						FormWindowUSWS fw = new FormWindowUSWS(3, "Select game", "Select a game");
						for (GameLevel gameLevel : Main.gameLevels) {
							fw.addButton(new ElementButton(gameLevel.getWorld()));
						}
						p.showFormWindow(fw);
						p.getInventory().clearAll();
						break;
					case "leave":
						if (!p.hasPermission("usw.leave")) {
							sendNoPerm(p);
							return true;
						}
						if (gl != null) {
							p.getInventory().clearAll();
							p.getUIInventory().clearAll();
							gl.leave(p);
						}else {
							//TODO There is no game room in the location map
						}
						break;
					case "saveWorlds":
						if (!p.hasPermission("usw.saveworlds")) {
							sendNoPerm(p);
							return true;
						}
						plugin.saveGameLevels();
						break;
					case "create":
						if (!p.hasPermission("usw.create")) {
							sendNoPerm(p);
							return true;
						}
						FormWindowUSWS fw2 = new FormWindowUSWS(0, "Create GameLevel", "SELECT A MAP");
						for (Integer integer : p.getServer().getLevels().keySet()) {
							int id = integer;
							if (GameLevel.getGameLevelByWorld(p.getServer().getLevels().get(id).getName()) == null) {
								if(plugin.getServer().getDefaultLevel() != plugin.getServer().getLevel(id)){
									ElementButton eb = new ElementButton(p.getServer().getLevels().get(id).getName());
									fw2.addButton(eb);
								}

							}
						}
						p.showFormWindow(fw2);
						break;
					case "setspawns":
						if (!p.hasPermission("usw.setspawns")) {
							sendNoPerm(p);
							return true;
						}
						if (gl != null) {
							p.getInventory().clearAll();
							p.getUIInventory().clearAll();
							gl.setConfiguring(true);
							gl.setWaiting(false);
						}else {
							//TODO There is no game room in the location map
						}
						break;
					default:
						sendHelp(p);
						break;

				}
			}
		}else {
			s.sendMessage("Only use these commands in game");
			//TODO: add nice msg to this with color
		}
		return true;
	}

	private static void sendNoPerm(Player p) {
		p.sendMessage("You do not have permission to do this command");
	}

	private static void sendHelp(Player player) {
		player.sendMessage("Commands:\n"+ TextFormat.AQUA+"/usw create\n/usw join\n/usw leave\n/usw saveWorlds");
		//TODO: Make a help message to send to the player
		//message gets send when player does /usw or /usw help
	}

}
