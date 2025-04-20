package yusama125718.man10Reversi;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Man10Reversi extends JavaPlugin {

    public static JavaPlugin mreversi;
    public static Map<String, GameManager> games = new ConcurrentHashMap<>();
    public static ItemStack black_head;
    public static ItemStack white_head;

    @Override
    public void onEnable() {
        mreversi = this;
        new Events(this);
        Config.LoadConfig();
        getCommand("mreversi").setExecutor(new Commands());
        black_head = HeadUtils.getBlackHead();
        white_head = HeadUtils.getWhiteHead();
    }

    @Override
    public void onDisable() {
        if (!games.isEmpty()){
            for (GameManager m: games.values()) m.ForceEnd();
        }
    }
}
