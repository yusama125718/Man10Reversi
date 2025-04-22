package yusama125718.man10Reversi;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;

import java.util.*;

public class Data {
    public static Map<Integer, ItemStack> icons = new HashMap<>();

    public enum Ability{
        None("効果なし", 0),
        Creeper("クリーパー", 1),
        OneShot("ワンショット", 2),
        BreakShot("破壊の一撃", 3),
        FakeStone("偽物の石", 4),
        SecondChance("セカンドチャンス", 5);

        private final String label;
        private final int id;

        Ability(String label, int id) {
            this.label = label;
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public int getId(){
            return id;
        }

        public static Ability fromLabel(String str){
            for (Ability a : Ability.values()) {
                if (a.getLabel().equals(str)) {
                    return a;
                }
            }
            return null;
        }

        public static Ability fromId(int i){
            for (Ability a : Ability.values()) {
                if (a.getId() == i) {
                    return a;
                }
            }
            return null;
        }

        public static Ability getRandom(){
            Random rand = new Random();
            int i = rand.nextInt(5) + 1;
            return fromId(i);
        }
    }

    public static final Map<Ability, List<Component>> ability_details = Map.ofEntries(
        Map.entry(
            Ability.Creeper, List.of(
                Component.text("==持ち前の爆発力で周りの石を吹き飛ばす=="),
                Component.text("石を返した後に隣接するマスの石を破壊する")
            )
        ),
        Map.entry(
            Ability.OneShot, List.of(
                    Component.text("==狙い澄ました一撃は絶対に外さない=="),
                    Component.text("空いているマスの好きな位置に石を置ける"),
                    Component.text("※5ターン目以降から使えます")
            )
        ),
        Map.entry(
            Ability.BreakShot, List.of(
                    Component.text("==その一撃は誰にも止められない=="),
                    Component.text("既に置かれた石を自分の石に変えれる"),
                    Component.text("※ただし、設置時に他の石は返らない")
            )
        ),
        Map.entry(
            Ability.FakeStone, List.of(
                Component.text("==幻影を見抜けるかな？=="),
                Component.text("相手の色の石を設置できる"),
                Component.text("※自分の石を返せる位置にしか置けないが、設置時に自分の石は返らない")
            )
        ),
        Map.entry(
            Ability.SecondChance, List.of(
                Component.text("==私のターンは、終わらない=="),
                Component.text("もう一度自分のターンになる"),
                Component.text("※5ターン目以降から使えます")
            )
        )
    );

    public static ItemStack getAbilityIcon(Ability a){
        ItemStack i = icons.get(a.id);
        if (i == null) i = Helper.CreateItemStack(Material.EMERALD, a.label, 0);
        return i;
    }
}
