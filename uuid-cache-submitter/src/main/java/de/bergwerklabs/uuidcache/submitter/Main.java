package de.bergwerklabs.uuidcache.submitter;

import com.google.gson.Gson;
import de.bergwerklabs.framework.commons.database.tablebuilder.Database;
import de.bergwerklabs.framework.commons.database.tablebuilder.DatabaseType;
import de.bergwerklabs.framework.commons.database.tablebuilder.statement.Statement;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.FileReader;
import java.sql.Timestamp;

/**
 * Created by Yannic Rieger on 10.03.2018.
 * <p>
 *
 * @author Yannic Rieger
 */
public class Main extends Plugin implements Listener {

    private final String INSERT_QUERY = "INSERT INTO uuidcache (uuid, display_name, last_login) VALUES (?, ?, ?) ON " +
                                         "DUPLICATE KEY UPDATE display_name = ?, last_login = ?";
    private Database database;

    @Override
    public void onEnable() {
        this.getProxy().getPluginManager().registerListener(this, this);

        try {
            String path = this.getDataFolder().getAbsolutePath() + "/config.json";
            Config config = new Gson().fromJson(new FileReader(path), Config.class);

            this.database = new Database(
                    DatabaseType.MySQL,
                    config.getHost(),
                    config.getDatabase(),
                    config.getUser(),
                    config.getPassword()
            );
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        this.getProxy().getScheduler().runAsync(this, () -> {
            try (Statement statement = this.database.prepareStatement(this.INSERT_QUERY)) {
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                statement.executeUpdate(
                        player.getUniqueId().toString(),
                        player.getName(),
                        timestamp,
                        player.getName(),
                        timestamp
                );
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}
