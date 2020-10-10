package me.CookieLuck.ChestSystem;

import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;

public class ItemList {
    private List<Item> items;

    public ItemList(boolean clean){
        if(clean){
            items = new ArrayList<Item>();
            return;
        }
        items = new ArrayList<Item>();
        for(int i = 0; i<10; i++){
            Item item = Item.get((int)(Math.random() * (327 - 256) + 256));

            this.items.add(item);
        }

    }

    public List<Item> getItems(){return items;}

    @Override
    public String toString() {
        String output = "";
        for(int i = 0; i<items.size();i++){
            output += items.get(i).getId()+"\n";
        }
        return output;
    }

    public void addItem(Item item){
        items.add(item);
    }

}
