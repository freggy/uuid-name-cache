package de.bergwerklabs.uuidcache.server.cache;

import com.google.common.cache.CacheLoader;
import de.bergwerklabs.framework.commons.database.tablebuilder.Database;
import de.bergwerklabs.framework.commons.database.tablebuilder.statement.Statement;
import de.bergwerklabs.framework.commons.database.tablebuilder.statement.StatementResult;

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

     AbstractCacheLoader(UuidCache cache, Database database) {
        this.cache = cache;
        this.database = database;
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
}
