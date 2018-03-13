package de.bergwerklabs.uuidcache.server.cache.uuid;

import com.google.common.cache.CacheLoader;
import de.bergwerklabs.api.cache.pojo.PlayerNameToUuidMapping;
import de.bergwerklabs.framework.commons.database.tablebuilder.Database;
import de.bergwerklabs.framework.commons.database.tablebuilder.statement.Row;
import de.bergwerklabs.framework.commons.database.tablebuilder.statement.Statement;
import de.bergwerklabs.framework.commons.database.tablebuilder.statement.StatementResult;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Created by Yannic Rieger on 10.03.2018.
 * <p>
 * Base class for cache loaders.
 *
 * @author Yannic Rieger
 */
abstract class AbstractCacheLoader<K, V> extends CacheLoader<K, V> {

    protected UuidCache cache;
    protected Database database;
    private final ExecutorService EXECUTOR = Executors.newFixedThreadPool(5);

    // In case an entry is already present just update display_name, to prevent duplicates
    private final String INSERT = "INSERT INTO uuidcache (uuid, display_name) VALUES (?, ?) ON " +
                                  "DUPLICATE KEY UPDATE display_name = ?";


     AbstractCacheLoader(UuidCache cache, Database database) {
        this.cache = cache;
        this.database = database;
     }

     public void shutdown() {
         this.EXECUTOR.shutdown();
     }

     protected <T> T execute(Function<StatementResult, T> consumer, String query, String param) {
         try (Statement statement = this.database.prepareStatement(query)) {
             StatementResult result = statement.execute(param);
             return consumer.apply(result);
         }
         catch (Exception ex) {
             ex.printStackTrace();
         }
         return null;
     }

     protected PlayerNameToUuidMapping fromRow(Row row) {
         PlayerNameToUuidMapping mapping = new PlayerNameToUuidMapping();
         UUID uuid = UUID.fromString(row.getString("uuid"));
         String name = row.getString("display_name");
         mapping.setUuid(uuid);
         mapping.setName(name);
         return mapping;
     }

     protected void writeToDatabaseAsync(PlayerNameToUuidMapping mapping) {
         this.EXECUTOR.submit(() -> {
             try (Statement statement = this.database.prepareStatement(this.INSERT)) {
                 statement.executeUpdate(mapping.getUuid().toString(), mapping.getName(), mapping.getName());
             }
             catch (Exception ex) {
                ex.printStackTrace();
             }
         });
     }
}
