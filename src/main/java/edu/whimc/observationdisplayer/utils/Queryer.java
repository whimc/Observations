package edu.whimc.observationdisplayer.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import edu.whimc.observationdisplayer.Observation;
import edu.whimc.observationdisplayer.ObservationDisplayer;

/**
 * Handles storing position data
 * @author Jack Henhapl
 *
 */
public class Queryer {

    /** Query for inserting an observation into the database. */
    private static final String SAVE_OBSERVATION_QUERY = "INSERT INTO whimc_observations (time, name, world, x, y, z, pitch, yaw, observation) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    /** Query for getting all observations from the database. */
    private static final String GET_OBSERVATIONS_QUERY = "SELECT * FROM  whimc_observations";

    /** Query for making an observation inactive. */
    private static final String MAKE_OBSERVATION_INACTIVE_QUERY = "UPDATE whimc_observations SET active=0 WHERE id={id}";

    private MySQLConnection sqlConnection;

    public Queryer(ObservationDisplayer plugin, Consumer<Queryer> callback) {
        this.sqlConnection = new MySQLConnection(plugin);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            final boolean success = sqlConnection.initialize();
            Bukkit.getScheduler().runTask(plugin, () -> {
                callback.accept(success ? this : null);
            });
        });
    }

    /**
     * Generates a PreparedStatement for saving an observation.
     * @param connection MySQL Connection
     * @param player Player observing
     * @param loc Location of player
     * @param observation Text of observation
     * @return PreparedStatement
     * @throws SQLException If there's an error making the statement
     */
    private PreparedStatement getStatement(Connection connection, String playerName, Location loc, String observation) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SAVE_OBSERVATION_QUERY, Statement.RETURN_GENERATED_KEYS);

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        String world = loc.getWorld().getName();
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        float pitch = loc.getPitch();
        float yaw = loc.getYaw();

        statement.setLong(1, timestamp.getTime());
        statement.setString(2, playerName);
        statement.setString(3, world);
        statement.setDouble(4, x);
        statement.setDouble(5, y);
        statement.setDouble(6, z);
        statement.setFloat(7, pitch);
        statement.setFloat(8, yaw);
        statement.setString(9, observation);

        return statement;
    }

    /**
     * Stores an observation into the database and returns the obervation's ID
     */
    public int storeObservation(Timestamp timestamp, String playerName, Location loc, String observation) {
        Connection connection = null;
        PreparedStatement statement = null;

        Utils.debug("Storing observation to database:");

        try {
            connection = this.sqlConnection.getConnection();
            statement = getStatement(connection, playerName, loc, observation);

            String query = statement.toString().substring(statement.toString().indexOf(" ") + 1);
            Utils.debug("  " + query);

            statement.executeUpdate();

            ResultSet idRes = statement.getGeneratedKeys();
            idRes.next();

            int id = idRes.getInt(1);

            Utils.debug("Observation saved with id " + id + ".");

            return id;


        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

            try {
                if (connection != null) connection.close();
            } catch (SQLException exc) {
                exc.printStackTrace();
                return -1;
            }

            try {
                if (statement != null) statement.close();
            } catch (SQLException exc) {
                exc.printStackTrace();
                return -1;
            }
        }

        return -1;
    }

    /**
     * Loads observations from the database.
     */
    public void loadObservations(ObservationDisplayer plugin) {
        Connection connection = null;
        Statement statement = null;
        ResultSet results = null;

        Utils.debug("Loading observations:");

        try {
            connection = this.sqlConnection.getConnection();
            statement = connection.createStatement();

            Utils.debug("  " + GET_OBSERVATIONS_QUERY);

            results = statement.executeQuery(GET_OBSERVATIONS_QUERY);

            Utils.debug("Observations found:");

            while (results.next()) {

                byte active = results.getByte("active");
                int id = results.getInt("id");

                if (active == 0) {
                    Utils.debug("  - " + id + " | Inactive -> skipping");
                    continue;
                }


                Timestamp timestamp = new Timestamp(results.getLong("time"));
                String name = results.getString("name");
                World world = Bukkit.getWorld(results.getString("world"));
                if (world == null) {
                    Utils.debug("  - "  + id + " | world '" + results.getString("world") + "' not found -> skipping");
                    continue;
                }
                Location loc = new Location(
                        world,
                        results.getDouble("x"),
                        results.getDouble("y"),
                        results.getDouble("z"),
                        results.getFloat("yaw"),
                        results.getFloat("pitch"));
                String observation = results.getString("observation");

                Utils.debug("  - " + id +
                        " | " + timestamp.getTime() +
                        " | " + name +
                        " | (" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")" +
                        " | " + observation);
                Observation.loadObservation(plugin, id, timestamp, name, loc, observation);
            }

            Utils.debug("Observations loaded.");

        } catch (SQLException exc) {
            exc.printStackTrace();
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException exc) {
                exc.printStackTrace();
            }

            try {
                if (statement != null) statement.close();
            } catch (SQLException exc) {
                exc.printStackTrace();
            }
        }
    }

    /**
     * Makes an obseration inactive in the database.
     * @param id Id of the observation
     */
    public void makeObservationInactive(int id) {
        Connection connection = null;
        PreparedStatement statement = null;

        Utils.debug("Making observation id " + id + " inactive:");

        try {
            connection = this.sqlConnection.getConnection();
            String query = MAKE_OBSERVATION_INACTIVE_QUERY.replace("{id}", id + "");
            statement = connection.prepareStatement(query);

            Utils.debug("  " + query);

            statement.executeUpdate();

            Utils.debug("Observation set as inactive.");

        } catch (SQLException exc) {
            exc.printStackTrace();
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException exc) {
                exc.printStackTrace();
            }

            try {
                if (statement != null) statement.close();
            } catch (SQLException exc) {
                exc.printStackTrace();
            }
        }
    }

}
