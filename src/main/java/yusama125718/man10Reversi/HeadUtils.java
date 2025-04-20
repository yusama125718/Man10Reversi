package yusama125718.man10Reversi;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;

import java.net.URL;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeadUtils {
    private static final String black = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmE1MmQ1NzlhZmUyZmRmN2I4ZWNmYTc0NmNkMDE2MTUwZDk2YmViNzUwMDliYjI3MzNhZGUxNWQ0ODdjNDJhMSJ9fX0=";
    private static final String white = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDM1MzE3YTk1NjcwM2Y5MjllZmQyMDEzNTY1Njc1ZmVjY2I2MGJjY2QxYzZlNWE4YWQ0YjkxNTUzOWI3YTU0ZiJ9fX0=";

    public static ItemStack getBlackHead(){
        return createSkinnedHead(black);
    }

    public static ItemStack getWhiteHead(){
        return createSkinnedHead(white);
    }

    public static ItemStack createSkinnedHead(String base64) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(null, UUID.randomUUID().toString());
        PlayerTextures texture = profile.getTextures();
        URL url;
        try {
            String prepare = new String(Base64.getDecoder().decode(base64));
            // {"textures":{"SKIN":{"url":"テクスチャの URL"}}}
            Matcher matcher = Pattern.compile("[\"{a-zA-Z:]+(http://[a-zA-Z0-9./]+)[}\"]+").matcher(prepare);
            if (!matcher.matches()) return head;
            url = new URL(matcher.group(1));
        } catch (Exception e) {
            e.printStackTrace();
            return head;
        }
        texture.setSkin(url);
        profile.setTextures(texture);
        skullMeta.setPlayerProfile(profile);
        head.setItemMeta(skullMeta);
        return head;
    }
}
