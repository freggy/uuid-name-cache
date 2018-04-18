package de.bergwerklabs.uuidcache.server;

/**
 * Created by Yannic Rieger on 11.03.2018.
 *
 * <p>
 *
 * @author Yannic Rieger
 */
public class Config {

  private String user, host, password, database;

  public Config(String user, String host, String password, String database) {
    this.user = user;
    this.host = host;
    this.password = password;
    this.database = database;
  }

  public String getUser() {
    return user;
  }

  public String getHost() {
    return host;
  }

  public String getPassword() {
    return password;
  }

  public String getDatabase() {
    return database;
  }
}
