package de.bergwerklabs.uuidcache.server;

import de.bergwerklabs.framework.commons.database.tablebuilder.Database;
import de.bergwerklabs.framework.commons.database.tablebuilder.DatabaseType;

/**
 * Created by Yannic Rieger on 10.03.2018.
 * <p>
 *
 * @author Yannic Rieger
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Host is: " + args[0]);
        System.out.println("Database is: " + args[1]);
        System.out.println("Username is: " + args[2]);
        System.out.println("Password is: " + args[3]);

        UuidCacheUpdater.start(new Database(DatabaseType.MySQL, args[0], args[1], args[2], args[3]));

        System.out.println("Starting cache...");
    }
}
