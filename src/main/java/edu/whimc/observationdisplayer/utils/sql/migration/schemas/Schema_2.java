package edu.whimc.observationdisplayer.utils.sql.migration.schemas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import edu.whimc.observationdisplayer.utils.sql.migration.SchemaVersion;

public class Schema_2 extends SchemaVersion {

    private static final String ADD_CATEGORY =
            "ALTER TABLE whimc_observations ADD COLUMN category VARCHAR(64);";

    @Override
    protected SchemaVersion next() {
        return null;
    }

    @Override
    protected int getVersion() {
        return 2;
    }

    @Override
    protected void migrateRoutine(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(ADD_CATEGORY)) {
            statement.execute();
        }
    }

}
