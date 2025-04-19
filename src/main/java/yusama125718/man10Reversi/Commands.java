package yusama125718.man10Reversi;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static yusama125718.man10Reversi.Man10Reversi.games;

public class Commands implements @Nullable CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("mreversi.p")) return true;
        switch (args.length){
            case 2:
                if (args[0].equals("start")){
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
}
