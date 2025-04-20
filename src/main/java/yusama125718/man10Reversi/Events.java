package yusama125718.man10Reversi;

import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;

import static yusama125718.man10Reversi.Man10Reversi.games;

public class Events implements Listener {
    public Events(Plugin plugin){
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void BlockDamage(BlockDamageEvent e){
        if (!e.getPlayer().hasPermission("mreversi.p")) return;
        if (e.getPlayer().hasPermission("mreversi.op") && BoardManager.tmp_board.containsKey(e.getPlayer().getUniqueId())){
            String result = BoardManager.tmp_board.get(e.getPlayer().getUniqueId()).SetLocation(e.getBlock(), e.getPlayer());
            e.getPlayer().sendMessage(Component.text(result));
            e.setCancelled(true);
        }
        else if (!games.isEmpty()){
            GameManager g = Helper.GetGameForUUID(games.values(), e.getPlayer().getUniqueId());
            if (g != null && g.state == GameManager.GameState.ONGAME && Helper.BlockInBoard(e.getBlock(), g.board)){
                g.Place(e.getBlock(), e.getPlayer());
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void BlockBreak(BlockBreakEvent e){
        if (games.isEmpty()) return;
        for (GameManager g: games.values()){
            if (g.state != GameManager.GameState.RECRUITMENT && Helper.BlockInBoard(e.getBlock(), g.board)){
                e.getPlayer().sendMessage(Component.text(Config.prefix + "§rゲーム中のボード内のブロックは破壊できません"));
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void BlockPlace(BlockPlaceEvent e){
        if (games.isEmpty()) return;
        for (GameManager g: games.values()){
            if (g.state != GameManager.GameState.RECRUITMENT && Helper.BlockInBoard(e.getBlock(), g.board)){
                e.getPlayer().sendMessage(Component.text(Config.prefix + "§rゲーム中のボード内にはブロックを設置できません"));
                e.setCancelled(true);
            }
        }
    }
}
