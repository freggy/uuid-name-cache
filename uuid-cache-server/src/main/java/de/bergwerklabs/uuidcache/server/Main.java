package de.bergwerklabs.uuidcache.server;

import de.bergwerklabs.api.cache.pojo.PlayerNameToUuidMapping;
import de.bergwerklabs.atlantis.api.corepackages.cache.uuid.NameToUuidRequestPacket;
import de.bergwerklabs.atlantis.api.corepackages.cache.uuid.NameToUuidResponsePacket;
import de.bergwerklabs.atlantis.api.corepackages.cache.uuid.UuidToNameRequestPacket;
import de.bergwerklabs.atlantis.api.corepackages.cache.uuid.UuidToNameResponsePacket;
import de.bergwerklabs.atlantis.client.base.util.AtlantisPackageService;
import de.bergwerklabs.framework.commons.database.tablebuilder.Database;
import de.bergwerklabs.framework.commons.database.tablebuilder.DatabaseType;
import de.bergwerklabs.uuidcache.server.cache.UuidCache;

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
            UuidToNameRequestPacket.class
    );

    private static UuidCache cache;

    public static void main(String[] args) {
        System.out.println("Host is: " + args[0]);
        System.out.println("Username is: " + args[1]);
        System.out.println("Password is: " + args[2]);

        Database database = new Database(DatabaseType.MySQL, args[0], "playerdata", args[1], args[2]);

        UuidCacheUpdater.start(database);
        cache = new UuidCache(database);

        SERVICE.addListener(NameToUuidRequestPacket.class, packet -> {
             PlayerNameToUuidMapping mapping = cache.resolveNameToUuid(packet.getName());
             SERVICE.sendResponse(new NameToUuidResponsePacket(mapping), packet);
        });


        SERVICE.addListener(UuidToNameRequestPacket.class, packet -> {
            PlayerNameToUuidMapping mapping = cache.resolveUuidToName(packet.getUuid());
            SERVICE.sendResponse(new UuidToNameResponsePacket(mapping), packet);
        });
    }
}
