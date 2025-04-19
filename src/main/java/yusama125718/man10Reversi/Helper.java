package yusama125718.man10Reversi;

import java.util.Collection;
import java.util.UUID;

public class Helper {
    public static boolean ContainsUUID(Collection<GameManager> managers, UUID target){
        for (GameManager g: managers){
            if (g.p1 == target || g.p2 == target) return true;
        }
        return false;
    }
}
