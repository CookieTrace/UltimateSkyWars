package me.CookieLuck;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
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
						FormWindowUSWS fw = new FormWindowUSWS(3, plugin.language.translateString("GUI_GameSelect_Title"),
								plugin.language.translateString("GUI_GameSelect_Text"));
						ElementButtonImageData im = new ElementButtonImageData("url","https://cloudburstmc.org/data/resource_icons/0/547.jpg?1598194966");
						ElementButton eb = new ElementButton(TextFormat.DARK_AQUA+""+TextFormat.BOLD+"Ultimate"+TextFormat.DARK_GREEN+"SkyWars",im);
						fw.addButton(eb);

						for (GameLevel gameLevel : Main.gameLevels) {

							if(!gameLevel.isBuilding() && gameLevel.isWaiting()){
								TextFormat color = TextFormat.DARK_GREEN;
								if(gameLevel.getAlive().size() >= gameLevel.getMaxPlayers()/2){
									color = TextFormat.GOLD;
								}
								String text = gameLevel.getWorld()+" │ "+TextFormat.BOLD+""+TextFormat.RESET+gameLevel.getAlive().size()+"/"+gameLevel.getMaxPlayers()+TextFormat.BOLD+color+" ⬤";

									fw.addButton(new ElementButton(text));
							}

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
						FormWindowUSWS fw2 = new FormWindowUSWS(0, plugin.language.translateString("GUI_CreateRoom_Title"),
								plugin.language.translateString("GUI_CreateRoom_Text"));
						for (Integer integer : p.getServer().getLevels().keySet()) {
							int id = integer;
							if (GameLevel.getGameLevelByWorld(p.getServer().getLevels().get(id).getName()) == null) {
								if(plugin.getServer().getDefaultLevel() != plugin.getServer().getLevel(id)){
									ElementButton e = new ElementButton(p.getServer().getLevels().get(id).getName());
									fw2.addButton(e);
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
