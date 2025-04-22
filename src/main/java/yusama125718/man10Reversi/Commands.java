package yusama125718.man10Reversi;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static yusama125718.man10Reversi.Man10Reversi.games;
import static yusama125718.man10Reversi.Man10Reversi.mreversi;

public class Commands implements @Nullable CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("mreversi.p")) return true;
        switch (args.length){
            case 1:
                if (args[0].equals("help")){
                    sender.sendMessage(Config.prefix + "§r/mreversi board list : リバーシを開始します");
                    sender.sendMessage(Config.prefix + "§r/mreversi start [ボード名] : リバーシを開始します");
                    sender.sendMessage(Config.prefix + "§r/mreversi join [ボード名] : リバーシに参加します");
                    sender.sendMessage(Config.prefix + "§r/mreversi open : 特殊効果選択画面を開きます※ゲーム中のみ");
                    sender.sendMessage(Config.prefix + "§r/mreversi abilities : 特殊効果の一覧を開きます");
                    if (sender.hasPermission("mreversi.op")){
                        sender.sendMessage(Config.prefix + "§r=== 管理者コマンド ===");
                        sender.sendMessage(Config.prefix + "§r/mreversi [on/off] : システムを稼働/停止します");
                        sender.sendMessage(Config.prefix + "§r/mreversi board create [名前] : ボードを作成します");
                        sender.sendMessage(Config.prefix + "§r/mreversi end [ボード名] : ゲームを強制終了します");
                    }
                }
                else if (args[0].equals("on") && sender.hasPermission("mreversi.op")){
                    if (Config.system){
                        sender.sendMessage(Config.prefix + "§r既にONです");
                        return true;
                    }
                    Config.system = true;
                    mreversi.getConfig().set("system", Config.system);
                    mreversi.saveConfig();
                    sender.sendMessage(Config.prefix + "§rONにしました");
                    return true;
                }
                else if (args[0].equals("off") && sender.hasPermission("mreversi.op")){
                    if (!Config.system){
                        sender.sendMessage(Config.prefix + "§r既にOFFです");
                        return true;
                    }
                    Config.system = false;
                    mreversi.getConfig().set("system", Config.system);
                    mreversi.saveConfig();
                    sender.sendMessage(Config.prefix + "§rOFFにしました");
                    return true;
                }
                else if (args[0].equals("open")){
                    UUID uuid = ((Player) sender).getUniqueId();
                    GameManager game =  Helper.GetGameForUUID(games.values(), uuid);
                    if (game == null){
                        sender.sendMessage(Config.prefix + "§rゲームが見つかりませんでした");
                        return true;
                    }
                    if (game.state != GameManager.GameState.THINKING && game.state != GameManager.GameState.ABILITY){
                        sender.sendMessage(Config.prefix + "§rゲームが進行中ではありません");
                        return true;
                    }
                    List<Data.Ability> abilities = game.getAbilities(uuid);
                    Inventory inv = Bukkit.createInventory(null,9, Component.text("[Man10Reversi] 特殊効果選択"));
                    int i = 0;
                    for (Data.Ability a: abilities){
                        inv.setItem(i, Data.getAbilityIcon(a));
                        i++;
                    }
                    ((Player) sender).openInventory(inv);
                    return true;
                }
                else if (args[0].equals("abilities")){
                    Inventory inv = Bukkit.createInventory(null,36, Component.text("[Man10Reversi] 特殊効果一覧"));
                    int i = 0;
                    for (Data.Ability a: Data.ability_details.keySet()){
                        if (a == Data.Ability.None) continue;
                        ItemStack item = Data.getAbilityIcon(a);
                        ItemMeta meta = item.getItemMeta();
                        meta.lore(Data.ability_details.get(a));
                        item.setItemMeta(meta);
                        inv.setItem(i, item);
                        i++;
                    }
                    ((Player) sender).openInventory(inv);
                    return true;
                }
                break;

            case 2:
                if (args[0].equals("start")){
                    if (!Config.system){
                        sender.sendMessage(Config.prefix + "§rシステムはOFFです");
                        return true;
                    }
                    if (!sender.hasPermission("mreversi.open")){
                        sender.sendMessage(Config.prefix + "§r権限がありません");
                        return true;
                    }
                    UUID uuid = ((Player) sender).getUniqueId();
                    if (Helper.ContainsUUID(games.values(), uuid)){
                        sender.sendMessage(Config.prefix + "§r別のボードでゲーム中は開けません");
                        return true;
                    }
                    if (games.containsKey(args[1])){
                        sender.sendMessage(Config.prefix + "§rそのボードは使用中です");
                        return true;
                    }
                    if (!BoardManager.boards.containsKey(args[1])){
                        sender.sendMessage(Config.prefix + "§rそのボードは存在しません");
                        return true;
                    }
                    games.put(args[1], new GameManager(uuid, BoardManager.boards.get(args[1])));
                    return true;
                }
                else if (args[0].equals("join")){
                    if (!Config.system){
                        sender.sendMessage(Config.prefix + "§rシステムはOFFです");
                        return true;
                    }
                    UUID uuid = ((Player) sender).getUniqueId();
                    if (Helper.ContainsUUID(games.values(), uuid)){
                        sender.sendMessage(Config.prefix + "§r別のボードでゲーム中は参加できません");
                        return true;
                    }
                    if (!games.containsKey(args[1])) {
                        sender.sendMessage(Config.prefix + "§rそのボードのゲームは存在しません");
                        return true;
                    }
                    GameManager m = games.get(args[1]);
                    if (m.state != GameManager.GameState.RECRUITMENT){
                        sender.sendMessage(Config.prefix + "§rそのボードはプレイヤーを募集していません");
                        return true;
                    }
                    String str = m.JoinGame(uuid);
                    sender.sendMessage(str);
                    return true;
                }
                else if (args[0].equals("board") && args[1].equals("list")){
                    sender.sendMessage(Config.prefix + "§r=== ボード一覧 ===");
                    StringBuilder boards_str = new StringBuilder();
                    for (String str: BoardManager.boards.keySet()){
                        if (boards_str.toString().isEmpty()) boards_str.append(", ");
                        boards_str.append(str);
                    }
                    sender.sendMessage(boards_str.toString());
                    return true;
                }
                else if (args[0].equals("end") && sender.hasPermission("mreversi.op")){
                    if (!games.containsKey(args[1])){
                        sender.sendMessage(Config.prefix + "§rそのボードはゲーム中ではありません");
                        return true;
                    }
                    games.get(args[1]).ForceEnd();
                    sender.sendMessage(Config.prefix + "§r終了しました");
                    return true;
                }
                break;

            case 3:
                if (args[0].equals("board") && args[1].equals("create") && sender.hasPermission("mreversi.op")){
                    if (BoardManager.boards.containsKey(args[2])){
                        sender.sendMessage(Config.prefix + "§rその名前のボードはすでに存在します");
                        return true;
                    }
                    BoardManager.tmp_board.put(((Player) sender).getUniqueId(), new BoardManager.TMP_Board(args[2], new BoardManager.Board(args[2], ((Player) sender).getWorld().getName())));
                    sender.sendMessage(Config.prefix + "§r真上から見てボードの左上の端になるブロックを左クリックして下さい");
                    return true;
                }
                break;
        }
        sender.sendMessage(Config.prefix + "§r/mreversi helpでコマンドを確認");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("mreversi.p")) return List.of();
        if (args.length == 1){
            if (sender.hasPermission("mreversi.op")) return Arrays.asList("help", "start", "join", "on", "off", "board", "end", "open", "abilities");
            else return Arrays.asList("start", "join", "board", "open", "abilities");
        }
        else if (args.length == 2){
            if (args[0].equals("start")){
                return BoardManager.boards.keySet().stream().toList();
            }
            else if (args[0].equals("join") || (args[0].equals("end") && sender.hasPermission("mreversi.op"))){
                return games.keySet().stream().toList();
            }
            else if (args[0].equals("board")){
                if (sender.hasPermission("mreversi.op")) return Arrays.asList("list", "create");
                else Collections.singletonList("list");
            }
        }
        else if (args.length == 3){
            if (args[0].equals("board") && args[1].equals("create") && sender.hasPermission("mreversi.op")){
                Collections.singletonList("[名前]");
            }
        }
        return List.of();
    }
}
