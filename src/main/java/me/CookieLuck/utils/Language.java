package me.CookieLuck.utils;

import cn.nukkit.utils.Config;

import java.util.HashMap;
import java.util.Objects;

/**
 * @author lt_name
 */
public class Language {

    private final Config config;

    public Language(Config config) {
        this.config = config;
    }

    public String translateString(String key) {
        return this.translateString(key, new Object[]{});
    }

    public String translateString(String key, Object... params) {
        String string = this.config.getString(key, "§c Language reading error!");
        if (params != null && params.length > 0) {
            for (int i = 1; i < params.length + 1; i++) {
                string = string.replace("%" + i + "%", Objects.toString(params[i-1]));
            }
        }
        return string;
    }

    public void update(Config newConfig) {
        HashMap<String, String> cache = new HashMap<>();
        for (String key : this.config.getKeys()) {
            if (newConfig.getKeys().contains(key)) {
                cache.put(key, this.config.getString(key, "§c Language reading error!"));
            }else {
                cache.remove(key);
                this.config.remove(key);
            }
        }
        for (String key : newConfig.getKeys()) {
            if (!cache.containsKey(key)) {
                String string = newConfig.getString(key, "§c Language reading error!");
                this.config.set(key, string);
                cache.put(key, string);
            }
        }
        this.config.save(true);
    }

}
