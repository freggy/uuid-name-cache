package de.bergwerklabs.uuidcache.server.cache.online;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.bergwerklabs.api.cache.pojo.players.online.PlayerEntry;

import java.util.UUID;

/**
 * Created by Yannic Rieger on 13.03.2018.
 * <p>
 *
 * @author Yannic Rieger
 */
public class OnlinePlayerCache {

    private Cache<UUID, PlayerEntry> onlinePlayerCache = CacheBuilder.newBuilder().build();

    public void updateEntry(PlayerEntry entry) {
        this.onlinePlayerCache.put(entry.getMapping().getUuid(), entry);
    }

    public void removeEntry(UUID uuid) {
        this.onlinePlayerCache.asMap().remove(uuid);
    }

    public PlayerEntry getEntry(UUID uuid) {
        return this.onlinePlayerCache.getIfPresent(uuid);
    }
}
