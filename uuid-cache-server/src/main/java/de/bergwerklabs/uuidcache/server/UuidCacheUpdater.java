package de.bergwerklabs.uuidcache.server;

import de.bergwerklabs.framework.commons.database.tablebuilder.Database;
import de.bergwerklabs.framework.commons.database.tablebuilder.statement.Statement;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by Yannic Rieger on 09.02.2018.
 *
 * <p>Updates names for cached UUIDs.
 *
 * @author Yannic Rieger
 */
public class UuidCacheUpdater {

  private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);
  private static final ExecutorService SERVICE = Executors.newFixedThreadPool(10);
  private static final String EXPIRED_ENTRIES_QUERY =
      "SELECT * FROM uuidcache WHERE last_login < now() - INTERVAL 5 DAY OR last_update < now() - INTERVAL 2 DAY";
  private static final String UPDATE_QUERY =
      "INSERT INTO uuidcache (uuid, display_name) VALUES (?, ?) ON DUPLICATE KEY UPDATE display_name = ?, last_update = ?";

  private static Database db = null;

  public static void start(Database database) {
    System.out.println("Starting update interval...");
    db = database;
  }

  /**
   * Writes update to database
   *
   * @param uuid {@link UUID} of the player.
   * @param name name of the user.
   * @param update time in milliseconds when it was updated.
   */
  private static void writeUpdate(UUID uuid, String name, long update) {
    SERVICE.submit(
        () -> {
          try {
            Timestamp timestamp = new Timestamp(update);
            Statement statement = db.prepareStatement(UPDATE_QUERY);
            statement.executeUpdate(uuid.toString(), name, name, timestamp);
            statement.close();
            System.out.println("Updated entry for " + name + ". Time is " + timestamp);
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        });
  }
}
