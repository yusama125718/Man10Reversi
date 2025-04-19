package yusama125718.man10Reversi;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BoardManager {
    public static class Board{
        double x1;
        double z1;
        double x2;
        double z2;
        double y;
        String world;
        String name;

        public Board(String Name, String World, double X1, double Z1, double X2, double Z2, double Y){
            x1 = X1;
            z1 = Z1;
            x2 = X2;
            z2 = Z2;
            y = Y;
            world = World;
            name = Name;
        }

        public Board(String Name, String World){
            world = World;
            name = Name;
        }
    }

    public enum CreateState{
        SET_X1,
        SET_X2
    }

    public static class TMP_Board {
        public String name;
        public Board board;
        public CreateState state;

        public TMP_Board(String n, Board b){
            name = n;
            board = b;
            state = CreateState.SET_X1;
        }

        public String SetLocation(Location loc, Player p){
            if (!board.world.equals(loc.getWorld().getName())) return Config.prefix + "§rワールドが移動されました。最初からやり直すか、コマンドを実行したワールドに戻って下さい。";
            if (state == CreateState.SET_X1){
                board.x1 = loc.x();
                board.z1 = loc.z();
                board.y = loc.y();
                state = CreateState.SET_X2;
                return Config.prefix + "§r次に真上から見て右下のブロックを左クリックして下さい";
            }
            else if (state == CreateState.SET_X2){
                if (board.y != loc.y()) return Config.prefix + "§r1回目の指定と高さが違います。同じ高さのブロックを指定して下さい";
                double diff_x = board.x1 - loc.x();
                double diff_z = board.z1 - loc.z();
                if ((diff_x == 9 || diff_x == -9) && (diff_z == 9 || diff_z ==-9)) return Config.prefix + "§r盤面の大きさは9×9である必要があります。";
                board.x2 = loc.x();
                board.z2 = loc.z();
                if (!this.Save()) return Config.prefix + "§r保存に失敗しました";
                boards.put(name, board);
                tmp_board.remove(p.getUniqueId());
                return Config.prefix + "§r保存しました";
            }
            return "§rエラーが発生しました。最初からやり直して下さい";
        }

        public boolean Save(){
            Config.SaveBoards(board);
            boards.put(name, board);
            return true;
        }
    }

    public static Map<String, Board> boards = new HashMap<>();
    public static Map<UUID, TMP_Board> tmp_board = new HashMap<>();
}
