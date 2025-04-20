package yusama125718.man10Reversi;

import org.bukkit.block.Block;

import java.util.Collection;
import java.util.UUID;

public class Helper {
    public static boolean ContainsUUID(Collection<GameManager> managers, UUID target){
        return GetGameForUUID(managers, target) != null;
    }

    public static GameManager GetGameForUUID(Collection<GameManager> managers, UUID target){
        for (GameManager g: managers){
            if (g.p1 == target || g.p2 == target) return g;
        }
        return null;
    }

    public static boolean BlockInBoard(Block loc, BoardManager.Board board){
        int x_start = Math.min(board.x1, board.x2);
        int x_end = Math.max(board.x1, board.x2);
        int z_start = Math.min(board.z1, board.z2);
        int z_end = Math.max(board.z1, board.z2);
        return x_start <= loc.getX() && loc.getX() <= x_end && z_start <= loc.getZ() && loc.getZ() <= z_end && (board.y == loc.getY() || board.y == loc.getY() - 1);
    }
}
