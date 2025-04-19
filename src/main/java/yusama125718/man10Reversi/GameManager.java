package yusama125718.man10Reversi;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static yusama125718.man10Reversi.Man10Reversi.games;

public class GameManager {
    public UUID p1;
    public UUID p2;
    public GameState state;
    public BoardManager.Board board;
    public boolean tuenIs1;

    public enum GameState{
        RECRUITMENT,
        INITGAME,
        ONGAME
    }

    public GameManager(UUID P1, BoardManager.Board b){
        p1 = P1;
        p2 = null;
        state = GameState.RECRUITMENT;
        board = b;
        waitingTimer();
    }

    public String JoinGame(UUID uuid){
        if (p2 != null) return Config.prefix + "§rこのボードは満員です";
        p2 = uuid;
        state = GameState.INITGAME;
        World world = Bukkit.getWorld(board.world);
        if (world == null) {
            SendMessage(Config.prefix + "§rワールドが見つかりませんでした");
            return Config.prefix + "§rエラーが発生しました";
        }
        double x_start = Math.min(board.x1, board.x2);
        double x_end = Math.max(board.x1, board.x2);
        double z_start = Math.min(board.z1, board.z2);
        double z_end = Math.max(board.z1, board.z2);
        for (double x = x_start; x <= x_end; x++){
            for (double z = z_start; z <= z_end; z++){
                Block block = world.getBlockAt((int) x, (int) board.y, (int) z);
                block.setType(Material.SMOOTH_STONE);
                block = world.getBlockAt((int) x, (int) board.y + 1, (int) z);
                block.setType(Material.AIR);
            }
        }
        Player p1_p = Bukkit.getPlayer(p1);
        Player p2_p = Bukkit.getPlayer(p2);
        if (p1_p == null || p2_p == null) {
            SendMessage(Config.prefix + "§rプレイヤーの取得に失敗したのでゲームを強制終了します");
            games.remove(board.name);
            return Config.prefix + "§r対戦相手が見つかりませんでした";
        }
        String first;
        String second;
        Random rand = new Random();
        if (rand.nextInt(2) == 0) {
            tuenIs1 = true;
            first = "§c§l" + p1_p.getName() + "§r";
            second = "§9§l" + p2_p.getName() + "§r";
        } else {
            tuenIs1 = false;
            first = "§9§l" + p2_p.getName() + "§r";
            second = "§c§l" + p1_p.getName() + "§r";
        }
        SendMessage(Config.prefix + "§r§e§l対戦相手が決まりました");
        SendMessage(Config.prefix + "§r§e§l先手：" + first);
        SendMessage(Config.prefix + "§r§e§l後手：" + second);
        SendMessage(Config.prefix + "§r§f§l" + first + "は石を置いて下さい（地面を右クリックで設置可能）");
        state = GameState.ONGAME;
        return Config.prefix + "§r参加しました";
    }

    public void SendMessage(String message){
        Component c = Component.text(message);
        Player player1 = Bukkit.getPlayer(p1);
        Player player2 = Bukkit.getPlayer(p2);
        if (player1 != null) player1.sendMessage(c);
        if (player2 != null) player2.sendMessage(c);
    }

    public void waitingTimer(){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            Player p = Bukkit.getPlayer(p1);
            if (p == null) {
                SendMessage(Config.prefix + "§rプレイヤーの取得に失敗したのでゲームを強制終了します");
                games.remove(board.name);
                return;
            }
            int count = 120;
            int stay = 20;
            while (count > 0 && state == GameState.RECRUITMENT) {
                if (count < stay) stay = count;
                Bukkit.broadcast(Component.text(Config.prefix + "§c§l" + p.getName() + "§rがリバーシを募集中！あと" + count + "秒"));
                Bukkit.broadcast(Component.text("§e§l[ここをクリックで参加する]").clickEvent(runCommand("/mreversi join " + board.name)));
                try {
                    Thread.sleep(1000 * stay); // 1000ミリ秒（=1秒）スリープ
                } catch (InterruptedException e) {
                    e.printStackTrace(); // 割り込みされたときの処理
                }
                count -= stay;
            }
            if (state != GameState.RECRUITMENT) return;
            Bukkit.broadcast(Component.text(Config.prefix + "§c§l" + p.getName() + "§rのリバーシは対戦相手が現れなかったため解散しました"));
            games.remove(board.name);
        });
        executor.shutdown();
    }
}
