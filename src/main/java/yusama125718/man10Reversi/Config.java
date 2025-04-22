package yusama125718.man10Reversi;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static yusama125718.man10Reversi.Man10Reversi.mreversi;

public class Config {

    private static final File folder = new File(mreversi.getDataFolder().getAbsolutePath() + File.separator + "boards");
    public static String prefix = "";
    public static File configfile;
    public static boolean system;
    public static Material borad_material;
    public static int recruitment_time;
    public static int recruitment_interval;
    public static int max_thinking;
    public static int max_ability;

    public static void LoadConfig(){
        mreversi.saveDefaultConfig();
        system = mreversi.getConfig().getBoolean("system");
        prefix = mreversi.getConfig().getString("prefix");
        String str = mreversi.getConfig().getString("board_material");
        if (str != null) borad_material = Material.getMaterial(str);
        if (borad_material == null || borad_material.equals(Material.AIR)) borad_material = Material.SMOOTH_STONE;
        recruitment_time = mreversi.getConfig().getInt("recruitment.time");
        recruitment_interval = mreversi.getConfig().getInt("recruitment.messageInterval");
        max_thinking = mreversi.getConfig().getInt("game.maxThinking");
        max_ability = mreversi.getConfig().getInt("game.maxAbility");
        for (Data.Ability a : Data.Ability.values()) {
            String mate_str = mreversi.getConfig().getString("icons." + a.getLabel() + ".material");
            Material material = null;
            if (mate_str != null) {
                material = Material.getMaterial(mate_str);
            }
            if (material == null) material = Material.EMERALD;
            int cmd = mreversi.getConfig().getInt("icons." + a.getLabel() + ".cmd");
            Data.icons.put(a.getId(), Helper.CreateItemStack(material, a.getLabel(), cmd));
        }
        InitFolder();
        LoadBoards();
    }

    public static void InitFolder(){
        if (mreversi.getDataFolder().listFiles() != null){
            for (File file : Objects.requireNonNull(mreversi.getDataFolder().listFiles())) {
                if (file.getName().equals("boards")) {
                    configfile = file;
                    return;
                }
            }
        }
        if (folder.mkdir()) {
            Bukkit.broadcast(Component.text(prefix + "§rボードフォルダを作成しました"), "mreversi.op");
            configfile = folder;
        } else {
            Bukkit.broadcast(Component.text(prefix + "§rボードフォルダの作成に失敗しました"), "mreversi.op");
        }
    }

    public static void LoadBoards(){
        if (configfile.listFiles() == null) return;
        for (File f: configfile.listFiles()){
            if (f.isDirectory()) continue;
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(f);
            if (yaml.get("name") == null || yaml.get("world") == null || yaml.get("x1") == null  || yaml.get("z1") == null || yaml.get("x2") == null || yaml.get("z2") == null  || yaml.get("y") == null) continue;
            BoardManager.Board b = new BoardManager.Board(yaml.getString("name"), yaml.getString("world"), yaml.getInt("x1"), yaml.getInt("z1"), yaml.getInt("x2"), yaml.getInt("z2"), yaml.getInt("y"));
            BoardManager.boards.put(yaml.getString("name"), b);
        }
    }

    public static void SaveBoards(BoardManager.Board b){
        File folder = new File(configfile.getAbsolutePath() + File.separator + b.name + ".yml");
        YamlConfiguration yml = new YamlConfiguration();        //config作成
        yml.set("name", b.name);
        yml.set("x1", b.x1);
        yml.set("x2", b.x2);
        yml.set("z1", b.z1);
        yml.set("z2", b.z2);
        yml.set("y", b.y);
        yml.set("world", b.world);
        try {
            yml.save(folder);
        } catch (IOException e) {
            e.printStackTrace();
            Bukkit.broadcast(Component.text(Config.prefix + "§r" + b.name + "の保存に失敗しました"),"mreversi.op");
        }
    }
}
