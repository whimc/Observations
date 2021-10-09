package edu.whimc.observations.utils.sql.migration.schemas;

import edu.whimc.observations.utils.Utils;
import edu.whimc.observations.utils.sql.migration.SchemaVersion;
import org.bukkit.ChatColor;

import java.sql.*;

public class Schema_4 extends SchemaVersion {

    private static final String ADD_CATEGORY =
            "ALTER TABLE whimc_observations ADD COLUMN observation_color_stripped TEXT;";
    
    private static final String QUERY_UPDATE_OBSERVATION_NO_COLOR =
            "UPDATE whimc_observations " +
                    "SET observation_color_stripped=?" +
                    "WHERE rowid=?";

    private static final String QUERY_GET_OBSERVATIONS =
            "SELECT * " +
                    "FROM whimc_observations " +
                    "WHERE (observation_color_stripped is null or observation_color_stripped = '')";

    public Schema_4() {
        super(4, null);
    }

    @Override
    protected void migrateRoutine(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(ADD_CATEGORY)) {
            statement.execute();
        }catch (SQLException e){
        }

        try (PreparedStatement statement = connection.prepareStatement(QUERY_UPDATE_OBSERVATION_NO_COLOR)) {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(QUERY_GET_OBSERVATIONS);
            while (rs.next()) {
                String observation = rs.getString("observation");
                int rowid = rs.getInt("rowid");
                statement.setString(1, ChatColor.stripColor(Utils.color(observation)));
                statement.setInt(2, rowid);
                statement.execute();
            }
        }
    }

}
