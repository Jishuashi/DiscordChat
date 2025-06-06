package fr.jishuashi.plugins;

import com.google.gson.Gson;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonObject;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.entity.Player;

public class ChatDiscord extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        // Génère plugin.yml dans le dossier /plugins/TON_PLUGIN/ si absent
        saveDefaultConfig();

        String serverId = getConfig().getString("server-id");
        getLogger().info("Plugin activé pour : " + serverId);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(Component.text("Hello" + event.getPlayer().getName() + "!"));
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        String message = ((TextComponent) event.originalMessage()).content(); // ou serialize si tu veux du texte brut

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                String targetURL = getConfig().getString("target-url");

                URL url = new URL(targetURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json; utf-8");
                con.setDoOutput(true);

                String cleanMessage = message.replace("\"", "\\\"");

                String serverId = getConfig().getString("server-id");
                String channelId = getConfig().getString("channel-id");
                String prefix = " ";

                JsonObject obj = new JsonObject();

                if (isLuckPermsAvailable()){
                    prefix = getLuckPermsPrefix(player);

                    obj.addProperty("serverId", serverId);
                    obj.addProperty("channelId", channelId);
                    obj.addProperty("prefix", prefix);
                    obj.addProperty("player", playerName);
                    obj.addProperty("message", message); // pas besoin de nettoyer

                }else {

                    obj.addProperty("serverId", serverId);
                    obj.addProperty("channelId", channelId);
                    obj.addProperty("prefix", prefix);
                    obj.addProperty("player", playerName);
                    obj.addProperty("message", message); // pas besoin de nettoyer
                }

                String jsonInputString = new Gson().toJson(obj);

                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                con.getInputStream().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public boolean isLuckPermsAvailable() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("LuckPerms");
        return plugin != null && plugin.isEnabled();
    }

    public String getLuckPermsPrefix(Player player) {
        LuckPerms luckPerms = LuckPermsProvider.get();

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return null;

        CachedMetaData metaData = user.getCachedData().getMetaData(QueryOptions.defaultContextualOptions());
        return metaData.getPrefix(); // peut être null si pas de préfixe
    }
}