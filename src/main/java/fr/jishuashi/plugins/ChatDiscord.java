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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.entity.Player;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class ChatDiscord extends JavaPlugin implements Listener {

    private ChatDiscord instance;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        // Génère plugin.yml dans le dossier /plugins/TON_PLUGIN/ si absent
        saveDefaultConfig();

        String serverId = getConfig().getString("server-id");
        getLogger().info("Plugin activé pour : " + serverId);

        this.instance = this;
        startHttpServer();
    }

    private void startHttpServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(4001), 0);
            server.createContext("/mc-endpoint", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                        exchange.sendResponseHeaders(405, -1);
                        return;
                    }

                    InputStream inputStream = exchange.getRequestBody();
                    String body = new BufferedReader(new InputStreamReader(inputStream))
                            .lines().collect(Collectors.joining("\n"));
                    inputStream.close();

                    JsonObject json = new Gson().fromJson(body, JsonObject.class);
                    String player = json.has("name") ? json.get("name").getAsString() : "???";
                    String message = json.has("message") ? json.get("message").getAsString() : "message";

                    // Envoie dans le chat principal
                    Bukkit.getScheduler().runTask(instance, () -> {
                        Bukkit.broadcastMessage("§9[Discord] §r" + player + "§f: " + message);
                    });

                    String response = "Reçu";
                    exchange.sendResponseHeaders(200, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            });
            server.setExecutor(null);
            server.start();
            getLogger().info("HTTP server started on port 4001");
        } catch (IOException e) {
            e.printStackTrace();
        }
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