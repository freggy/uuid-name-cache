package de.bergwerklabs.uuidcache.server;

import com.google.gson.Gson;
import de.bergwerklabs.api.cache.pojo.PlayerNameToUuidMapping;
import de.bergwerklabs.atlantis.api.corepackages.cache.online.OnlinePlayerCacheRequestPacket;
import de.bergwerklabs.atlantis.api.corepackages.cache.online.OnlinePlayerCacheResponsePacket;
import de.bergwerklabs.atlantis.api.corepackages.cache.uuid.NameToUuidRequestPacket;
import de.bergwerklabs.atlantis.api.corepackages.cache.uuid.NameToUuidResponsePacket;
import de.bergwerklabs.atlantis.api.corepackages.cache.uuid.UuidToNameRequestPacket;
import de.bergwerklabs.atlantis.api.corepackages.cache.uuid.UuidToNameResponsePacket;
import de.bergwerklabs.atlantis.client.base.util.AtlantisPackageService;
import de.bergwerklabs.framework.commons.database.tablebuilder.Database;
import de.bergwerklabs.framework.commons.database.tablebuilder.DatabaseType;
import de.bergwerklabs.uuidcache.server.cache.online.OnlinePlayerCache;
import de.bergwerklabs.uuidcache.server.cache.uuid.UuidCache;

import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by Yannic Rieger on 10.03.2018.
 * <p>
 * Main class for UUID Cache server.
 *
 * @author Yannic Rieger
 */
public class Main {

    private static final AtlantisPackageService SERVICE = new AtlantisPackageService(
            NameToUuidRequestPacket.class,
            UuidToNameRequestPacket.class,
            OnlinePlayerCacheRequestPacket.class
    );

    private static final OnlinePlayerCache ONLINE_PLAYER_CACHE = new OnlinePlayerCache();

    private static UuidCache cache;

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> cache.shutdown()));

        try {
            Config config = new Gson().fromJson(new FileReader("config.json"), Config.class);
            Database database = new Database(
                    DatabaseType.MySQL,
                    config.getHost(),
                    config.getDatabase(),
                    config.getUser(),
                    config.getPassword()
            );
            UuidCacheUpdater.start(database);
            cache = new UuidCache(database);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        SERVICE.addListener(NameToUuidRequestPacket.class, packet -> {
            PlayerNameToUuidMapping mapping = cache.resolveNameToUuid(packet.getName());
            SERVICE.sendResponse(new NameToUuidResponsePacket(mapping), packet);
        });


        SERVICE.addListener(UuidToNameRequestPacket.class, packet -> {
            PlayerNameToUuidMapping mapping = cache.resolveUuidToName(packet.getUuid());
            SERVICE.sendResponse(new UuidToNameResponsePacket(mapping), packet);
        });

        SERVICE.addListener(OnlinePlayerCacheRequestPacket.class, packet -> {
            SERVICE.sendResponse(
                    new OnlinePlayerCacheResponsePacket(ONLINE_PLAYER_CACHE.getEntry(packet.getUuid())),
                    packet
            );
        });
    }
}
