package de.bergwerklabs.uuidcache.submitter;

import com.google.gson.Gson;
import de.bergwerklabs.api.cache.pojo.PlayerNameToUuidMapping;
import de.bergwerklabs.api.cache.pojo.players.online.ConnectedServer;
import de.bergwerklabs.api.cache.pojo.players.online.PlayerEntry;
import de.bergwerklabs.atlantis.api.corepackages.cache.online.PlayerOnlineCacheUpdatePacket;
import de.bergwerklabs.atlantis.api.corepackages.cache.online.RemoveOnlinePlayerCacheEntry;
import de.bergwerklabs.atlantis.client.base.util.AtlantisPackageService;
import de.bergwerklabs.framework.commons.database.tablebuilder.Database;
import de.bergwerklabs.framework.commons.database.tablebuilder.DatabaseType;
import de.bergwerklabs.framework.commons.database.tablebuilder.statement.Statement;
import de.bergwerklabs.framework.permissionbridge.PermissionBridge;
import de.bergwerklabs.permissionbridge.luckperms.common.LuckPermsBridge;
import java.io.FileReader;
import java.sql.Timestamp;
import java.util.Arrays;
import me.lucko.luckperms.LuckPerms;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

/**
 * Created by Yannic Rieger on 10.03.2018.
 *
 * <p>
 *
 * @author Yannic Rieger
 */
public class Main extends Plugin implements Listener {

  private final AtlantisPackageService SERVICE = new AtlantisPackageService();

  private final String INSERT_QUERY =
      "INSERT INTO uuidcache (uuid, display_name, last_login, last_update) VALUES "
          + "(?, ?, ?, ?) ON DUPLICATE KEY UPDATE display_name = ?, last_login = ?";

  private Database database;
  private PermissionBridge bridge;

  @Override
  public void onEnable() {
    this.getProxy().getPluginManager().registerListener(this, this);
    this.bridge = new LuckPermsBridge(LuckPerms.getApi());

    try {
      String path = this.getDataFolder().getAbsolutePath() + "/config.json";
      Config config = new Gson().fromJson(new FileReader(path), Config.class);

      this.database =
          new Database(
              DatabaseType.MySQL,
              config.getHost(),
              config.getDatabase(),
              config.getUser(),
              config.getPassword());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @EventHandler
  public void onPlayerPostLogin(PostLoginEvent event) {
    ProxiedPlayer player = event.getPlayer();
    this.getProxy()
        .getScheduler()
        .runAsync(
            this,
            () -> {
              try (Statement statement = this.database.prepareStatement(this.INSERT_QUERY)) {
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                // Set last update to the current timestamp because this is the latest data we have
                // from them.
                // This will also prevent too many entries being checked in the next update cycle.
                statement.executeUpdate(
                    player.getUniqueId().toString(), // uuid
                    player.getName(), // display_name
                    timestamp, // last_login
                    timestamp, // last_update
                    player.getName(),
                    timestamp);
              } catch (Exception ex) {
                ex.printStackTrace();
              }
            });
  }

  @EventHandler
  public void onPlayerDisconnect(PlayerDisconnectEvent event) {
    SERVICE.sendPackage(new RemoveOnlinePlayerCacheEntry(event.getPlayer().getUniqueId()));
  }

  @EventHandler
  public void onServerConnected(ServerConnectedEvent event) {
    ProxiedPlayer player = event.getPlayer();
    ServerInfo server = event.getServer().getInfo();

    String[] components = server.getName().split("_");
    String[] arr = Arrays.copyOfRange(components, 1, components.length);

    String id = components[0];
    String service = String.join("_", arr);

    System.out.println(server.getName());

    SERVICE.sendPackage(
        new PlayerOnlineCacheUpdatePacket(
            new PlayerEntry(
                new PlayerNameToUuidMapping(player.getName(), player.getUniqueId()),
                this.bridge.getGroupIdAsInt(player.getUniqueId()),
                new ConnectedServer(id, service, server.getMotd(), server.getAddress()))));
  }
}
