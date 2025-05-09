package yusama125718.man10Reversi;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static yusama125718.man10Reversi.Man10Reversi.*;

public class GameManager {
    public UUID p1;
    public UUID p2;
    public int p1_count;
    public int p2_count;
    public GameState state;
    public BoardManager.Board board;
    public boolean firstIs1;
    public boolean turnIs1;
    // 盤面情報
    // 0:未設置, 1:黒, 2:白
    public int[][] grid = new int[8][8];
    public List<int[]> placeable = new ArrayList<>();
    public BossBarTimer timer;
    public Data.Ability selectAbility;
    public List<Data.Ability> p1Abilities = new ArrayList<>();
    public List<Data.Ability> p2Abilities = new ArrayList<>();
    public int turn_count = 1;
    // 挟めるか確認する時のオフセット
    private static final List<int[]> search_offsets = List.of(
        new int[]{1, 0},
        new int[]{1, 1},
        new int[]{0, 1},
        new int[]{-1, 1},
        new int[]{-1, 0},
        new int[]{-1, -1},
        new int[]{0, -1},
        new int[]{1, -1}
    );

    public enum GameState{
        RECRUITMENT,
        INITGAME,
        THINKING,
        ABILITY,
        GAMEEND
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
        // 物理ボード初期化
        int x_start = Math.min(board.x1, board.x2);
        int x_end = Math.max(board.x1, board.x2);
        int z_start = Math.min(board.z1, board.z2);
        int z_end = Math.max(board.z1, board.z2);
        for (int x = x_start; x <= x_end; x++){
            for (int z = z_start; z <= z_end; z++){
                Block block = world.getBlockAt(x, board.y, z);
                block.setType(Material.SMOOTH_STONE);
                block = world.getBlockAt(x, board.y + 1, z);
                block.setType(Material.AIR);
            }
        }
        // 特殊効果抽選
        for (int i = 1; i <= Config.max_ability; i++){
            p1Abilities.add(Data.Ability.getRandom());
            p2Abilities.add(Data.Ability.getRandom());
        }
        // 盤面初期化
        InitBoard();
        grid[3][3] = 1;
        grid[4][3] = 2;
        grid[3][4] = 2;
        grid[4][4] = 1;
        UpdateBoard();
        selectAbility = Data.Ability.None;
        // メッセージ送信
        Player p1_p = Bukkit.getPlayer(p1);
        Player p2_p = Bukkit.getPlayer(p2);
        if (p1_p == null || p2_p == null) {
            SendMessage(Config.prefix + "§rプレイヤーの取得に失敗したのでゲームを強制終了します");
            ForceEnd();
            return Config.prefix + "§r対戦相手が見つかりませんでした";
        }
        String first;
        String second;
        Random rand = new Random();
        if (rand.nextInt(2) == 0) {
            firstIs1 = true;
            turnIs1 = true;
            first = "§c§l" + p1_p.getName() + "§r";
            second = "§9§l" + p2_p.getName() + "§r";
        } else {
            firstIs1 = false;
            turnIs1 = false;
            first = "§9§l" + p2_p.getName() + "§r";
            second = "§c§l" + p1_p.getName() + "§r";
        }
        UpdatePlaceable();
        List<Player> players = List.of(
            p1_p,
            p2_p
        );
        timer = new BossBarTimer(this, players, board, Config.max_thinking + 20, first + "のターン", placeable, null);
        timer.StartTimer(true);
        SendMessage(Config.prefix + "§r§e§l対戦相手が決まりました");
        SendMessage(Config.prefix + "§r§e§l先手：" + first);
        SendMessage(Config.prefix + "§r§e§l後手：" + second);
        SendMessage(Config.prefix + "§r§f§l" + first + "は石を置いて下さい（地面を左クリックで設置可能）");
        state = GameState.THINKING;
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
                ForceEnd();
                return;
            }
            int count = Config.recruitment_time;
            int stay = Config.recruitment_interval;
            while (count > 0 && state == GameState.RECRUITMENT && games.containsValue(this)) {
                if (count < stay) stay = count;
                Bukkit.broadcast(Component.text(Config.prefix + "§c§l" + p.getName() + "§rがリバーシを募集中！あと" + count + "秒"));
                Bukkit.broadcast(Component.text("§e§l[ここをクリックで参加する]").clickEvent(runCommand("/mreversi join " + board.name)));
                try {
                    Thread.sleep(1000L * stay); // 1000ミリ秒（=1秒）スリープ
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

    public void InitBoard(){
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                grid[row][column] = 0;
            }
        }
    }

    public void Place(Block b, Player p) {
        UUID turn;
        if (turnIs1) turn = p1;
        else turn = p2;
        if (turn != p.getUniqueId()) return;
        int place_color = (turnIs1 && firstIs1) || (!turnIs1 && !firstIs1) ? 1 : 2;
        // 盤面座標取得
        int x = b.getX() - board.x1;
        int z = b.getZ() - board.z1;
        if (board.x1 > board.x2) x += 8;
        if (board.z1 > board.z2) z += 8;
        switch (selectAbility){
            case Creeper -> {
                if (grid[x][z] != 0) {
                    p.sendMessage(Config.prefix + "§rそこには置けません！");
                    return;
                }
                // 返せるコマを取得
                List<int[]> change_list = new ArrayList<>();
                for (int[] offset : search_offsets) {
                    List<int[]> temp_list = new ArrayList<>();
                    int search_row = x + offset[0];
                    int search_column = z + offset[1];
                    while (0 <= search_row && search_row <= 7 && 0 <= search_column && search_column <= 7 && grid[search_row][search_column] != 0 && grid[search_row][search_column] != place_color) {
                        temp_list.add(new int[]{search_row, search_column});
                        search_row += offset[0];
                        search_column += offset[1];
                    }
                    if (0 <= search_row && search_row <= 7 && 0 <= search_column && search_column <= 7 && grid[search_row][search_column] == place_color)
                        change_list.addAll(temp_list);
                }
                if (change_list.isEmpty()) {
                    p.sendMessage(Config.prefix + "§rそこには置けません！");
                    return;
                }
                // 盤面データを更新
                for (int[] loc : change_list) grid[loc[0]][loc[1]] = place_color;
                grid[x][z] = place_color;
                final List<int[]> creeper_offset = List.of(
                        new int[]{1, 0},
                        new int[]{0, 1},
                        new int[]{-1, 0},
                        new int[]{0, -1}
                );
                for (int[] offset : creeper_offset) {
                    int x_remove = x + offset[0];
                    int z_remove = z + offset[1];
                    // 盤外の場合消さない
                    if (0 > x_remove || x_remove > 7 || 0 > z_remove || z_remove > 7) continue;
                    grid[x_remove][z_remove] = 0;
                }
            }
            case OneShot -> {
                if (grid[x][z] != 0) {
                    p.sendMessage(Config.prefix + "§rそこには置けません！");
                    return;
                }
                // 返せるコマを取得
                List<int[]> change_list = new ArrayList<>();
                for (int[] offset : search_offsets) {
                    List<int[]> temp_list = new ArrayList<>();
                    int search_row = x + offset[0];
                    int search_column = z + offset[1];
                    while (0 <= search_row && search_row <= 7 && 0 <= search_column && search_column <= 7 && grid[search_row][search_column] != 0 && grid[search_row][search_column] != place_color) {
                        temp_list.add(new int[]{search_row, search_column});
                        search_row += offset[0];
                        search_column += offset[1];
                    }
                    if (0 <= search_row && search_row <= 7 && 0 <= search_column && search_column <= 7 && grid[search_row][search_column] == place_color)
                        change_list.addAll(temp_list);
                }
                // 盤面データを更新
                for (int[] loc : change_list) grid[loc[0]][loc[1]] = place_color;
                grid[x][z] = place_color;
            }
            case BreakShot -> {
                if (grid[x][z] == 0 || grid[x][z] == place_color) {
                    p.sendMessage(Config.prefix + "§rそこには置けません！");
                    return;
                }
                grid[x][z] = place_color;
            }
            case FakeStone -> {
                place_color = place_color == 1 ? 2 : 1;
                if (grid[x][z] != 0) {
                    p.sendMessage(Config.prefix + "§rそこには置けません！");
                    return;
                }
                // 返せるコマを取得
                List<int[]> change_list = new ArrayList<>();
                for (int[] offset : search_offsets) {
                    List<int[]> temp_list = new ArrayList<>();
                    int search_row = x + offset[0];
                    int search_column = z + offset[1];
                    while (0 <= search_row && search_row <= 7 && 0 <= search_column && search_column <= 7 && grid[search_row][search_column] != 0 && grid[search_row][search_column] != place_color) {
                        temp_list.add(new int[]{search_row, search_column});
                        search_row += offset[0];
                        search_column += offset[1];
                    }
                    if (0 <= search_row && search_row <= 7 && 0 <= search_column && search_column <= 7 && grid[search_row][search_column] == place_color)
                        change_list.addAll(temp_list);
                }
                if (change_list.isEmpty()) {
                    p.sendMessage(Config.prefix + "§rそこには置けません！");
                    return;
                }
                grid[x][z] = place_color;
            }
            default -> {
                if (grid[x][z] != 0) {
                    p.sendMessage(Config.prefix + "§rそこには置けません！");
                    return;
                }
                // 返せるコマを取得
                List<int[]> change_list = new ArrayList<>();
                for (int[] offset : search_offsets) {
                    List<int[]> temp_list = new ArrayList<>();
                    int search_row = x + offset[0];
                    int search_column = z + offset[1];
                    while (0 <= search_row && search_row <= 7 && 0 <= search_column && search_column <= 7 && grid[search_row][search_column] != 0 && grid[search_row][search_column] != place_color) {
                        temp_list.add(new int[]{search_row, search_column});
                        search_row += offset[0];
                        search_column += offset[1];
                    }
                    if (0 <= search_row && search_row <= 7 && 0 <= search_column && search_column <= 7 && grid[search_row][search_column] == place_color)
                        change_list.addAll(temp_list);
                }
                if (change_list.isEmpty()) {
                    p.sendMessage(Config.prefix + "§rそこには置けません！");
                    return;
                }
                // 盤面データを更新
                for (int[] loc : change_list) grid[loc[0]][loc[1]] = place_color;
                grid[x][z] = place_color;
            }

        }
        UpdateCount();
        // 盤面を更新
        UpdateBoard();
        if (selectAbility != Data.Ability.None) {
            if (turnIs1) p1Abilities.remove(selectAbility);
            else p2Abilities.remove(selectAbility);
        }
        UUID uuid = p1;
        if (turnIs1) uuid = p2;
        Player next_p = Bukkit.getPlayer(uuid);
        if (next_p == null) {
            SendMessage(Config.prefix + "§rプレイヤーの取得に失敗したのでゲームを強制終了します");
            ForceEnd();
            return;
        }
        String current_name = "§c§l" + p.getName() + "§r";
        String next_name = "§9§l" + next_p.getName() + "§r";
        if (!turnIs1) {
            current_name = "§9§l" + p.getName() + "§r";
            next_name = "§c§l" + next_p.getName() + "§r";
        }
        SendMessage(Config.prefix + "§r" + current_name + "がX:" + x + ", Z:" + z + "に配置しました");
        if (p1_count == 0 || p2_count == 0 || p1_count + p2_count == 64){
            GameEnd();
            return;
        }
        SendMessage(Config.prefix + "§r§f§l現在の状況は以下の通りです");
        if (turnIs1){
            SendMessage(Config.prefix + "§r" + current_name + ":" + p1_count + "枚");
            SendMessage(Config.prefix + "§r" + next_name + ":" + p2_count + "枚");
        }
        else {
            SendMessage(Config.prefix + "§r" + next_name + ":" + p1_count + "枚");
            SendMessage(Config.prefix + "§r" + current_name + ":" + p2_count + "枚");
        }
        if (selectAbility == Data.Ability.SecondChance) {
            next_name = current_name;
            turnIs1 = !turnIs1;
            SendMessage(Config.prefix + "§r§e§l私のターンは、まだ終わらない！");
        }
        if (selectAbility != Data.Ability.None){
            switch (selectAbility){
                case Creeper -> {
                    p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    next_p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.0f, 1.0f);
                }
                case OneShot -> {
                    p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 1.0f, 2.0f);
                    next_p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 1.0f, 2.0f);
                }
                case BreakShot -> {
                    p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0f, 2.0f);
                    next_p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0f, 2.0f);
                }
                case FakeStone -> {
                    p.playSound(p.getLocation(), Sound.ENTITY_WITCH_CELEBRATE, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    next_p.playSound(p.getLocation(), Sound.ENTITY_WITCH_CELEBRATE, SoundCategory.PLAYERS, 1.0f, 1.0f);
                }
                case SecondChance -> {
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    next_p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
                }
            }
        }
        selectAbility = Data.Ability.None;
        timer.Restart(Config.max_thinking, next_name + "のターン", placeable, new int[]{ x, z }, selectAbility == Data.Ability.None);
        SendMessage(Config.prefix + "§r" + next_name + "のターンです。石を置いて下さい（地面を左クリックで設置可能）");
        turnIs1 = !turnIs1;
        UpdatePlaceable();
        state = GameState.THINKING;
        turn_count++;
        if (placeable.isEmpty()) {
            SendMessage(Config.prefix + "§r" + next_name + "は置ける場所がないので" + current_name + "にターンが移ります");
            SendMessage(Config.prefix + "§r" + current_name + "のターンです。石を置いて下さい（地面を左クリックで設置可能）");
            timer.Restart(Config.max_thinking, current_name + "のターン", placeable, new int[]{x, z}, false);
            turnIs1 = !turnIs1;
            UpdatePlaceable();
            if (placeable.isEmpty()) {
                SendMessage(Config.prefix + "§r" + current_name + "も置ける場所がないのでゲームを終了します");
                GameEnd();
            }
        }
    }
    
    public void SelectAbility(Player p, Data.Ability a){
        UUID uuid = turnIs1 ? p1 : p2;
        if (p.getUniqueId() != uuid){
            p.sendMessage(Config.prefix + "§rあなたのターンではありません");
            return;
        }
        if ((a == Data.Ability.SecondChance || a == Data.Ability.OneShot) && turn_count < 5){
            p.sendMessage(Config.prefix + "§rこの特殊効果はまだ使えません");
            return;
        }
        Player p1_p = Bukkit.getPlayer(p1);
        Player p2_p = Bukkit.getPlayer(p2);
        if (p1_p == null || p2_p == null) {
            SendMessage(Config.prefix + "§rプレイヤーの取得に失敗したのでゲームを強制終了します");
            ForceEnd();
            return;
        }
        selectAbility = a;
        p1_p.playSound(p.getLocation(), Sound.ENCHANT_THORNS_HIT, SoundCategory.PLAYERS, 1.0f, 1.0f);
        p2_p.playSound(p.getLocation(), Sound.ENCHANT_THORNS_HIT, SoundCategory.PLAYERS, 1.0f, 1.0f);
        String name = turnIs1 ? "§c§l" + p.getName() + "§r" : "§9§l" + p.getName() + "§r";
        SendMessage(Config.prefix + "§r" + name + "は" + a.getLabel() + "を発動しました！");
        UpdatePlaceable();
        timer.Restart(Config.max_thinking, name + "のターン", placeable, null, false);
        state = GameState.ABILITY;
    }
    
    public List<Data.Ability> getAbilities(UUID uuid){
        if (uuid == p1) return p1Abilities;
        else if (uuid == p2) return p2Abilities;
        else return null;
    }

    public void UpdateCount(){
        int temp_p1 = 0;
        int temp_p2 = 0;
        int p1_color = 1;
        int p2_color = 2;
        if (!firstIs1){
            p1_color = 2;
            p2_color = 1;
        }
        for (int[] row: grid){
            for (int value: row){
                if (value == p1_color) temp_p1++;
                else if (value == p2_color) temp_p2++;
            }
        }
        p1_count = temp_p1;
        p2_count = temp_p2;
    }

    public void UpdateBoard(){
        for (int row = 0; row < 8; row++){
            for (int column = 0; column < 8; column++){
                ItemStack stone = white_head;
                if (grid[row][column] == 1) stone = black_head;
                else if (grid[row][column] == 0) stone = new ItemStack(Material.AIR);
                World world = Bukkit.getWorld(board.world);
                if (world == null) {
                    SendMessage(Config.prefix + "§rワールドが見つからなかった為強制終了します。");
                    ForceEnd();
                    return;
                }
                PlaceHead(row, column, world, stone);
            }
        }
    }

    private void PlaceHead(int row, int column, World world, ItemStack stone){
        int offset_x = row;
        if (board.x1 > board.x2) offset_x -= 8;
        int offset_z = column;
        if (board.z1 > board.z2) offset_z -= 8;
        Block block = world.getBlockAt(board.x1 + offset_x, board.y + 1, board.z1 + offset_z);
        block.setType(stone.getType());

        if (stone.getType().equals(Material.PLAYER_HEAD)){
            // BlockStateとして取得
            BlockState state = block.getState();
            if (state instanceof Skull skull) {
                // アイテムのメタ情報をスカルにコピー（スキンやUUID）
                ItemMeta meta = stone.getItemMeta();
                if (meta instanceof SkullMeta skullMeta) skull.setPlayerProfile(skullMeta.getPlayerProfile());

                // 更新して設置反映
                skull.update();
            }
        }
    }

    public void ForceEnd(){
        state = GameState.GAMEEND;
        if (timer != null) timer.StopTimer();
        games.remove(board.name);
    }

    public void GameEnd(){
        state = GameState.GAMEEND;
        if (timer != null) timer.StopTimer();
        Player p1_p = Bukkit.getPlayer(p1);
        Player p2_p = Bukkit.getPlayer(p2);
        if (p1_p == null || p2_p == null) {
            SendMessage(Config.prefix + "§rプレイヤーの取得に失敗したのでゲームを強制終了します");
            ForceEnd();
            return;
        }
        String p1_name = "§c§l" + p1_p.getName() + "§r";
        String p2_name = "§9§l" + p2_p.getName() + "§r";
        SendMessage(Config.prefix + "§r§e§lゲーム終了！！");
        InitBoard();
        UpdateBoard();
        new BukkitRunnable() {
            int p1_remaining = p1_count;
            int p2_remaining = p2_count;
            int placed = 0;

            @Override
            public void run() {
                World w = Bukkit.getWorld(board.world);
                int x = placed % 8;
                int z = placed / 8;
                if (p1_remaining > 0){
                    ItemStack i = firstIs1 ? black_head : white_head;
                    PlaceHead(x, z, w, i);
                    p1_remaining--;
                }
                if (p2_remaining > 0){
                    ItemStack i = firstIs1 ? white_head : black_head;
                    PlaceHead(7 - x, 7 - z, w, i);
                    p2_remaining--;
                }
                p1_p.playSound(p1_p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1.0f, 2.0f);
                p2_p.playSound(p2_p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1.0f, 2.0f);
                placed++;
                if (p1_remaining <= 0 && p2_remaining <= 0){
                    SendMessage(Config.prefix + "§r" + p1_name + ":" + p1_count + "枚");
                    SendMessage(Config.prefix + "§r" + p2_name + ":" + p2_count + "枚");
                    if (p1_count > p2_count) SendMessage(Config.prefix + "§r" + p1_name + "の勝利！");
                    else if (p1_count < p2_count) SendMessage(Config.prefix + "§r" + p2_name + "の勝利！");
                    else SendMessage(Config.prefix + "§r引き分け！");
                    games.remove(board.name);
                    this.cancel();
                    return;
                }
            }
        }.runTaskTimer(mreversi, 0L, 7L);
    }

    public void UpdatePlaceable(){
        int place_color = (turnIs1 && firstIs1) || (!turnIs1 && !firstIs1) ? 1 : 2;
        placeable.clear();
        if (selectAbility == Data.Ability.FakeStone) place_color = place_color == 1 ? 2 : 1;
        switch (selectAbility){
            case OneShot -> {
                for (int row = 0; row < 8; row++) {
                    for (int column = 0; column < 8; column++) {
                        if (grid[row][column] == 0) placeable.add(new int[]{row, column});
                    }
                }
            }
            case BreakShot -> {
                int enemy = place_color == 1 ? 2 : 1;
                for (int row = 0; row < 8; row++) {
                    for (int column = 0; column < 8; column++) {
                        if (grid[row][column] == enemy) placeable.add(new int[]{row, column});
                    }
                }
            }
            default -> {
                for (int row = 0; row < 8; row++) {
                    for (int column = 0; column < 8; column++) {
                        if (grid[row][column] != 0) continue;
                        for (int[] offset: search_offsets) {
                            List<int[]> temp_list = new ArrayList<>();
                            int search_row = row + offset[0];
                            int search_column = column + offset[1];
                            while (0 <= search_row && search_row <= 7 && 0 <= search_column && search_column <= 7 && grid[search_row][search_column] != 0 && grid[search_row][search_column] != place_color) {
                                temp_list.add(new int[]{search_row, search_column});
                                search_row += offset[0];
                                search_column += offset[1];
                            }
                            if (!temp_list.isEmpty() && 0 <= search_row && search_row <= 7 && 0 <= search_column && search_column <= 7 && grid[search_row][search_column] == place_color) {
                                placeable.add(new int[]{row, column});
                                break;
                            }
                        }
                    }
                }
            }
        }

    }

    public void ForcePlace(){
        UUID uuid = p1;
        if (!turnIs1) uuid = p2;
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) {
            SendMessage(Config.prefix + "§rプレイヤーの取得に失敗したのでゲームを強制終了します");
            ForceEnd();
            return;
        }
        World world = Bukkit.getWorld(board.world);
        if (world == null) {
            SendMessage(Config.prefix + "§rワールドの取得に失敗したのでゲームを強制終了します");
            ForceEnd();
            return;
        }
        int x = board.x1 + placeable.get(0)[0];
        int z = board.z1 + placeable.get(0)[1];
        if (board.x1 > board.x2) x -= 8;
        if (board.z1 > board.z2) z -= 8;
        Block b = world.getBlockAt(x, board.y, z);
        Place(b, p);
    }
}
