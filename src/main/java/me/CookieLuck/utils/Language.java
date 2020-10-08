package me.CookieLuck.utils;

import cn.nukkit.utils.Config;

/**
 * @author lt_name
 */
public class Language {

    private final Config config;

    public Language(Config config) {
        this.config = config;
    }

    public String translateString(String key) {
        return this.translateString(key, new String[]{});
    }

    public String translateString(String key, String... params) {
        String string = this.config.getString(key);
        if (params != null && params.length > 0) {
            for (int i = 1; i < params.length + 1; i++) {
                string = string.replace("%" + i, params[i-1]);
            }
        }
        return string;
    }


}
