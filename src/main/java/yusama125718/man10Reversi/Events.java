package yusama125718.man10Reversi;

import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.plugin.Plugin;

public class Events implements Listener {
    public Events(Plugin plugin){
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void BlockDamage(BlockDamageEvent e){
        if (e.getPlayer().hasPermission("mreversi.op") && BoardManager.tmp_board.containsKey(e.getPlayer().getUniqueId())){
            String result = BoardManager.tmp_board.get(e.getPlayer().getUniqueId()).SetLocation(e.getBlock().getLocation(), e.getPlayer());
            e.getPlayer().sendMessage(Component.text(result));
        }
    }
}
