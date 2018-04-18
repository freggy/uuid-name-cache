package de.bergwerklabs.uuidcache.submitter;

/**
 * Created by Yannic Rieger on 10.03.2018.
 *
 * <p>
 *
 * @author Yannic Rieger
 */
public class Config {

  private String user, database, host, password;

  public Config(String user, String database, String host, String password) {
    this.user = user;
    this.database = database;
    this.host = host;
    this.password = password;
  }

  public String getUser() {
    return user;
  }

  public String getDatabase() {
    return database;
  }

  public String getHost() {
    return host;
  }

  public String getPassword() {
    return password;
  }
}
