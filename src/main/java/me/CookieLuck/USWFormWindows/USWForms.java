package me.CookieLuck.USWFormWindows;

import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.utils.TextFormat;
import me.CookieLuck.GameLevel;
import me.CookieLuck.Main;
import me.CookieLuck.USWFormWindows.FormWindowUSWS;

public class USWForms {

    static FormWindow selectRoom;

    public static FormWindowUSWS getSelectRoom(Main plugin){
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
        return fw;
    }

}
