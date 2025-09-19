package org.libremc.weLoveCapitalism.datatypes;

import org.libremc.weLoveCapitalism.TariffManager;

import java.util.UUID;

public class Tariff {
    private UUID government_tariffing;
    private UUID government_tariffed;
    private int percentage;
    boolean is_active;

    public Tariff(UUID nation_tariffing, UUID nation_tariffed, int percentage){
        this.government_tariffing = nation_tariffing;
        this.government_tariffed = nation_tariffed;
        this.percentage = percentage;
        this.is_active = true;
    }

    public Tariff(UUID nation_tariffing, UUID nation_tariffed, int percentage, boolean is_active){
        this.government_tariffing = nation_tariffing;
        this.government_tariffed = nation_tariffed;
        this.percentage = percentage;
        this.is_active = is_active;
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

    public boolean isActive(){
        return is_active;
    }

    public void setActive(boolean b){
        is_active = b;
    }

    public void update(){
        TariffManager.removeTariff(this);
        TariffManager.addTariff(this);
    }

}
