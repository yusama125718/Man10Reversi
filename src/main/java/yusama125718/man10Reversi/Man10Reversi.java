package yusama125718.man10Reversi;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Man10Reversi extends JavaPlugin {

    public static JavaPlugin mreversi;
    public static Map<String, GameManager> games = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        mreversi = this;
        new Events(this);
        Config.LoadConfig();
        getCommand("mreversi").setExecutor(new Commands());
    }
}
