package org.libremc.wlcAPI.datatypes;

import java.util.UUID;

public class Tariff {
    private UUID government_tariffing;
    private UUID government_tariffed;
    private int percentage;

    public Tariff(UUID nation_tariffing, UUID nation_tariffed, int percentage){
        this.government_tariffing = nation_tariffing;
        this.government_tariffed = nation_tariffed;
        this.percentage = percentage;
    }

    public void setGovernmentTariffed(UUID nation_tariffed) {
        this.government_tariffed = nation_tariffed;
    }

    public UUID getGovernmentTariffed() {
        return government_tariffed;
    }

    public void setGovernmentTariffing(UUID nation_tariffing) {
        this.government_tariffing = nation_tariffing;
    }

    public UUID getGovernmentTariffing() {
        return government_tariffing;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public int getPercentage() {
        return percentage;
    }

}
