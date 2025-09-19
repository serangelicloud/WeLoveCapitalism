package org.libremc.weLoveCapitalism.datatypes;

import org.libremc.weLoveCapitalism.EmbargoManager;

import java.util.UUID;

public class Embargo {
    private UUID embargoingnation;
    private UUID embargoednation;
    boolean is_active;

    public Embargo(UUID embargoingNation, UUID embargoedNation) {
        this.embargoingnation = embargoingNation;
        this.embargoednation = embargoedNation;
        this.is_active = true;
    }

    public Embargo(UUID embargoingNation, UUID embargoedNation, boolean is_active) {
        this.embargoingnation = embargoingNation;
        this.embargoednation = embargoedNation;
        this.is_active = is_active;
    }

    public UUID getEmbargoedNation() {
        return embargoednation;
    }

    public UUID getEmbargoingNation() {
        return embargoingnation;
    }

    public void setEmbargoedNation(UUID embargoednation) {
        this.embargoednation = embargoednation;
    }

    public void setEmbargoingNation(UUID embargoingnation) {
        this.embargoingnation = embargoingnation;
    }

    public boolean isActive() {
        return is_active;
    }

    public void setActive(boolean b) {
        this.is_active = b;
    }

    public void update(){
        EmbargoManager.removeEmbargo(this);
        EmbargoManager.addEmbargo(this);
    }
}
