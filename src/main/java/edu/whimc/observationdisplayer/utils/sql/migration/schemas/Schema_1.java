package edu.whimc.observationdisplayer.utils.sql.migration.schemas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import edu.whimc.observationdisplayer.utils.sql.migration.SchemaVersion;

public class Schema_1 extends SchemaVersion {

    private static final String CREATE_TABLE =
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

    @Override
    protected SchemaVersion next() {
        return new Schema_2();
    }

    @Override
    protected int getVersion() {
        return 1;
    }

    @Override
    protected void migrateRoutine(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(CREATE_TABLE)) {
            statement.execute();
        }
    }


}
