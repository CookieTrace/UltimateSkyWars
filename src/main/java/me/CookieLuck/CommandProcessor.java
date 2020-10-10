package me.CookieLuck;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.utils.TextFormat;
import me.CookieLuck.USWFormWindows.FormWindowUSWS;
import me.CookieLuck.USWFormWindows.USWForms;

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
						p.showFormWindow(USWForms.getSelectRoom(plugin));
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

						p.showFormWindow(USWForms.getCreateRoom(plugin));
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
		p.sendMessage(Main.getInstance().language.translateString("Use_Command_NOPermission"));
	}

	private static void sendHelp(Player player) {
		player.sendMessage("Commands:\n"+ TextFormat.AQUA+"/usw create\n/usw join\n/usw leave\n/usw saveWorlds");
		//TODO: Make a help message to send to the player
		//message gets send when player does /usw or /usw help
	}

}
