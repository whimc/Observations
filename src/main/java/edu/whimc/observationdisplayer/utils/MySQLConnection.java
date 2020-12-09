package edu.whimc.observationdisplayer.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import edu.whimc.observationdisplayer.ObservationDisplayer;

public class MySQLConnection  {

    public static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";
    public static final String URL_TEMPLATE = "jdbc:mysql://%s:%s/%s";
    public static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS `whimc_observations` (" +
            "  `rowid`       INT    AUTO_INCREMENT NOT NULL," +
            "  `time`        BIGINT                NOT NULL," +
            "  `uuid`        VARCHAR(36)           NOT NULL," +
            "  `username`    VARCHAR(16)           NOT NULL," +
            "  `world`       VARCHAR(64)           NOT NULL," +
            "  `x`           DOUBLE                NOT NULL," +
            "  `y`           DOUBLE                NOT NULL," +
            "  `z`           DOUBLE                NOT NULL," +
            "  `yaw`         FLOAT                 NOT NULL," +
            "  `pitch`       FLOAT                 NOT NULL," +
            "  `observation` TEXT                  NOT NULL," +
            "  `active`      BOOLEAN               NOT NULL," +
            "  `expiration`  BIGINT                        ," +
            "  PRIMARY KEY    (`rowid`)," +
            "  INDEX uuid     (`uuid`)," +
            "  INDEX username (`username`));";

    private Connection connection;
    private String host, database, username, password, url;
    private int port;

    public MySQLConnection(ObservationDisplayer plugin) {
        this.host = plugin.getConfig().getString("mysql.host", "localhost");
        this.port = plugin.getConfig().getInt("mysql.port", 3306);
        this.database = plugin.getConfig().getString("mysql.database", "minecraft");
        this.username = plugin.getConfig().getString("mysql.username", "user");
        this.password = plugin.getConfig().getString("mysql.password", "pass");

        this.url = String.format(URL_TEMPLATE, host, port, database);
    }

    public boolean initialize() {
        if (getConnection() == null) {
            return false;
        }

        try {
            PreparedStatement statement = this.connection.prepareStatement(CREATE_TABLE);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public Connection getConnection() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                return this.connection;
            }

            Class.forName(DRIVER_CLASS);
            this.connection = DriverManager.getConnection(this.url, this.username, this.password);
        } catch (SQLException | ClassNotFoundException e) {
            return null;
        }

        return this.connection;
    }

}
