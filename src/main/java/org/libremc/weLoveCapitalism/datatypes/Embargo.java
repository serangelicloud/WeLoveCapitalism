package org.libremc.weLoveCapitalism.datatypes;

import java.util.UUID;

public class Embargo {
    private UUID embargoingnation;
    private UUID embargoednation;

    public Embargo(UUID embargoingNation, UUID embargoedNation) {
        this.embargoingnation = embargoingNation;
        this.embargoednation = embargoedNation;
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
}
