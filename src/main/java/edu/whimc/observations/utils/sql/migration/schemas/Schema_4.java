package edu.whimc.observations.utils.sql.migration.schemas;

import edu.whimc.observations.utils.sql.migration.SchemaVersion;

import java.sql.*;

public class Schema_4 extends SchemaVersion {

    private static final String ADD_CATEGORY =
            "ALTER TABLE whimc_observations ADD COLUMN observation_color_stripped TEXT;";

    public Schema_4() {
        super(4, null);
    }

    @Override
    protected void migrateRoutine(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(ADD_CATEGORY)) {
            statement.execute();
        }
    }

}
