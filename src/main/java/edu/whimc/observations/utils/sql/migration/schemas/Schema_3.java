package edu.whimc.observations.utils.sql.migration.schemas;

import edu.whimc.observations.utils.sql.migration.SchemaVersion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Schema_3 extends SchemaVersion {

    private static final String REMOVE_FACTUAL =
            "UPDATE whimc_observations SET category = CASE category WHEN 'FACTUAL' THEN NULL ELSE category END;";

    public Schema_3() {
        super(3, new Schema_4());
    }

    @Override
    protected void migrateRoutine(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(REMOVE_FACTUAL)) {
            statement.execute();
        }
    }
}
