package com.wix.mysql.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.wix.mysql.distribution.Version;

import static java.lang.String.format;

public enum Privilege {
  ALL("ALL"),
  ALTER("ALTER"),
  ALTER_ROUTINE("ALTER ROUTINE"),
  CREATE("CREATE"),
  CREATE_ROUTINE("CREATE ROUTINE"),
  CREATE_TABLESPACE("CREATE TABLESPACE"),
  CREATE_TEMPORARY_TABLES("CREATE TEMPORARY TABLES"),
  CREATE_USER("CREATE USER"),
  CREATE_VIEW("CREATE VIEW"),
  DELETE("DELETE"),
  DROP("DROP"),
  EVENT("EVENT"),
  EXECUTE("EXECUTE"),
  FILE("FILE"),
  GRANT_OPTION("GRANT OPTION"),
  INDEX("INDEX"),
  INSERT("INSERT"),
  LOCK_TABLES("LOCK TABLES"),
  PROCESS("PROCESS"),
  PROXY("PROXY"),
  REFERENCES("REFERENCES"),
  RELOAD("RELOAD"),
  REPLICATION_CLIENT("REPLICATION CLIENT"),
  REPLICATION_SLAVE("REPLICATION SLAVE"),
  SELECT("SELECT"),
  SHOW_DATABASES("SHOW DATABASES"),
  SHOW_VIEW("SHOW VIEW"),
  SHUTDOWN("SHUTDOWN"),
  SUPER("SUPER"),
  TRIGGER("TRIGGER"),
  UPDATE("UPDATE"),
  USAGE("USAGE"),
  CREATE_ROLE("CREATE_ROLE", MySqlMajorVersion.v8),
  DROP_ROLE("DROP_ROLE", MySqlMajorVersion.v8);


  private enum MySqlMajorVersion {
    v5(5),
    v8(8);
    private final int mysqlVersion;

    MySqlMajorVersion(int version) {
      this.mysqlVersion = version;
    }
  }

  private final String value;
  private final ArrayList<MySqlMajorVersion> mysqlMajorVersions;

  Privilege(String value, MySqlMajorVersion mysqlMajorVersion) {
    this.value = value;
    this.mysqlMajorVersions = new ArrayList<>();
    this.mysqlMajorVersions.add(mysqlMajorVersion);
  }

  Privilege(String value) {
    this.value = value;
    this.mysqlMajorVersions = new ArrayList<>(Arrays.asList(MySqlMajorVersion.values()));
  }

  void checkCompatiblityWithVersion(Version version) {
    if(this
        .mysqlMajorVersions
        .stream()
        .map(v -> v.mysqlVersion)
        .noneMatch(major -> version.getMajorVersion().startsWith(String.valueOf(major)))) {
      throw new IllegalArgumentException(format("Privilege %s not compatible with %s", this.toString(), version.toString()));
    }
  }
}
