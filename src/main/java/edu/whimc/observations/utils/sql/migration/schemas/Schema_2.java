package edu.whimc.observations.utils.sql.migration.schemas;

import edu.whimc.observations.utils.sql.migration.SchemaVersion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Schema_2 extends SchemaVersion {

    private static final String ADD_CATEGORY =
            "ALTER TABLE whimc_observations ADD COLUMN category VARCHAR(64);";

    public Schema_2() {
        super(2, new Schema_3());
    }

    @Override
    protected void migrateRoutine(Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement(ADD_CATEGORY)) {
            statement.execute();
        }catch (SQLException e){
        }

    }

}
