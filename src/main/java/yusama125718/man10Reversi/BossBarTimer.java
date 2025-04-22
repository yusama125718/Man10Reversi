package yusama125718.man10Reversi;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

import static yusama125718.man10Reversi.Man10Reversi.mreversi;

public class BossBarTimer {
    private BossBar bossBar;
    private int seconds;
    private String barTitle;
    private List<int[]> placeable;
    private int[] lastPlaced;
    private final BoardManager.Board board;
    private BukkitTask task;
    private GameManager manager;
    private List<Player> targets;
    private double y_offset;

    public BossBarTimer(GameManager m, List<Player> players, BoardManager.Board b, int sec, String Title, List<int[]> Placeable, int[] Last){
        manager = m;
        board = b;
        seconds = sec;
        barTitle = Title;
        placeable = Placeable;
        lastPlaced = Last;
        targets = players;
    }

    public void StartTimer(boolean playsound){
        bossBar = Bukkit.createBossBar(barTitle, BarColor.PURPLE, BarStyle.SOLID);
        bossBar.setProgress(1.0);
        final World world = Bukkit.getWorld(board.world);
        final Particle.DustOptions placeable_color = new Particle.DustOptions(Color.PURPLE, 1.0F);
        for (Player p: targets) {
            bossBar.addPlayer(p);
            if (playsound) p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
        if (manager.selectAbility == Data.Ability.BreakShot) y_offset = 1.8;
        else y_offset = 1.1;
        task = new BukkitRunnable() {
            int timeLeft = seconds;

            @Override
            public void run() {
                bossBar.setTitle(barTitle + "（" + timeLeft + " 秒）");
                if (timeLeft <= 0) {
                    bossBar.setProgress(0);
                    bossBar.removeAll();
                    manager.ForcePlace();
                    this.cancel();
                    return;
                }
                bossBar.setProgress((double) timeLeft / seconds);
                if (timeLeft <= 3){
                    for (Player p: targets) p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1.0f, 2.0f);
                }

                if (world != null){
                    if (lastPlaced != null){
                        double x = board.x1 + lastPlaced[0] + 0.5;
                        double y = board.y + 1.8;
                        double z = board.z1 + lastPlaced[1] + 0.5;
                        if (board.x1 > board.x2) x -= 9;
                        if (board.z1 > board.z2) z -= 9;
                        Location loc = new Location(world, x, y, z);
                        world.spawnParticle(Particle.FLAME, loc, 15, 0,0,0,0);
                    }
                    if (!placeable.isEmpty()){
                        for (int[] pos: placeable){
                            double x = board.x1 + pos[0] + 0.5;
                            double y = board.y + y_offset;
                            double z = board.z1 + pos[1] + 0.5;
                            if (board.x1 > board.x2) x -= 9;
                            if (board.z1 > board.z2) z -= 9;
                            Location loc = new Location(world, x, y, z);
                            world.spawnParticle(Particle.REDSTONE, loc, 15, 0, 0, 0, 0, placeable_color);
                        }
                    }
                }
                timeLeft--;
            }
        }.runTaskTimer(mreversi, 0L, 20L);
    }

    public void Restart(int sec, String Title, List<int[]> Placeable, int[] Last, boolean playsound){
        seconds = sec;
        barTitle = Title;
        placeable = Placeable;
        lastPlaced = Last;
        if (manager.selectAbility == Data.Ability.BreakShot) y_offset = 1.8;
        else y_offset = 1.1;
        StopTimer();
        StartTimer(playsound);
    }

    public void StopTimer(){
        task.cancel();
        bossBar.removeAll();
    }
}
