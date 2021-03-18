package edu.whimc.observationdisplayer.utils.sql.migration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;

import edu.whimc.observationdisplayer.ObservationDisplayer;
import edu.whimc.observationdisplayer.utils.sql.migration.schemas.Schema_1;

public class SchemaManager {

    public static final String VERSION_FILE_NAME = ".schema_version";

    private static final SchemaVersion BASE_SCHEMA = new Schema_1();

    private ObservationDisplayer plugin;
    private Connection connection;

    public SchemaManager(ObservationDisplayer plugin, Connection connection) {
        this.plugin = plugin;
        this.connection = connection;
    }

    protected Connection getConnection() {
        return this.connection;
    }

    protected File getVersionFile() {
        return new File(this.plugin.getDataFolder(), VERSION_FILE_NAME);
    }

    private int getCurrentVersion() {
        try {
            return Integer.parseInt(new String(Files.readAllBytes(getVersionFile().toPath())));
        } catch (NumberFormatException | IOException exc) {
            exc.printStackTrace();
            return 0;
        }
    }

    public boolean initialize() {
        int curVersion = getCurrentVersion();

        SchemaVersion schema = BASE_SCHEMA;
        while (schema != null) {
            if (schema.getVersion() > curVersion) {
                this.plugin.getLogger().info("Migrating to schema" + schema.getVersion() + "...");
                if (!schema.migrate(this)) {
                    return false;
                }
            }
            schema = schema.next();
        }

        return true;
    }

}
