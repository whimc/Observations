package edu.whimc.observationdisplayer.models;

import java.sql.Timestamp;

import org.bukkit.Location;

import edu.whimc.observationdisplayer.ObservationDisplayer;

public class TemplatedObservation extends Observation {

    protected TemplatedObservation(ObservationDisplayer plugin, int id, Timestamp timestamp, String playerName,
            Location viewLoc, String observation, Timestamp expiration, boolean temporary, boolean isNew) {
        super(plugin, id, timestamp, playerName, viewLoc, observation, expiration, temporary, isNew);
    }

}
