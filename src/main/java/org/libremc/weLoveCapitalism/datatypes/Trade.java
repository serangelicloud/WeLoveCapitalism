package org.libremc.weLoveCapitalism.datatypes;

import javax.annotation.Nullable;
import java.util.UUID;

public class Trade {
    private Integer tradeId;
    private int chestShopId;
    private String item;
    private int multiplier;
    private int price; // Get total price paid for - for the entire thing including the multiplier
    private UUID ownerUuid;
    private UUID buyerUuid;
    private UUID townUuid;
    private UUID nationUuid;
    private UUID townBuyerUuid;
    private UUID nationBuyerUuid;
    private long timestamp;
    private boolean is_active; // If this trade should be counted in with the upkeep, then this is true. This is needed for calculating upkeep every new day

    public Trade(int chestShopId, String item, int multiplier, int price,
                 UUID ownerUuid, UUID buyerUuid, UUID townUuid, long timestamp) {
        this.chestShopId = chestShopId;
        this.item = item;
        this.multiplier = multiplier;
        this.price = price;
        this.ownerUuid = ownerUuid;
        this.buyerUuid = buyerUuid;
        this.townUuid = townUuid;
        this.timestamp = timestamp;
    }

    public Trade(int tradeId, int chestShopId, String item, int multiplier, int price,
                 UUID ownerUuid, UUID buyerUuid, UUID townUuid, @Nullable UUID nationUuid,
                 @Nullable UUID townBuyerUuid, @Nullable UUID nationBuyerUuid, long timestamp, boolean is_active) {
        this.tradeId = tradeId;
        this.chestShopId = chestShopId;
        this.item = item;
        this.multiplier = multiplier; //
        this.price = price;
        this.ownerUuid = ownerUuid;
        this.buyerUuid = buyerUuid;
        this.townUuid = townUuid;
        this.nationUuid = nationUuid;
        this.townBuyerUuid = townBuyerUuid;
        this.nationBuyerUuid = nationBuyerUuid;
        this.timestamp = timestamp;
        this.is_active = is_active;
    }

    public Trade(int chestShopId, String item, int multiplier, int price,
                 UUID ownerUuid, UUID buyerUuid, UUID townUuid, @Nullable UUID nationUuid,
                 @Nullable UUID townBuyerUuid, @Nullable UUID nationBuyerUuid, long timestamp) {
        this.chestShopId = chestShopId;
        this.item = item;
        this.multiplier = multiplier; //
        this.price = price;
        this.ownerUuid = ownerUuid;
        this.buyerUuid = buyerUuid;
        this.townUuid = townUuid;
        this.nationUuid = nationUuid;
        this.townBuyerUuid = townBuyerUuid;
        this.nationBuyerUuid = nationBuyerUuid;
        this.timestamp = timestamp;
        this.is_active = true;

    }

    public Integer getTradeId() {
        return tradeId;
    }

    public int getChestShopId() {
        return chestShopId;
    }

    public String getItem() {
        return item;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public int getPrice() {
        return price;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public UUID getBuyerUuid() {
        return buyerUuid;
    }

    public UUID getTownUuid() {
        return townUuid;
    }

    public UUID getNationUuid() {
        return nationUuid;
    }

    public UUID getTownBuyerUuid() {
        return townBuyerUuid;
    }

    public UUID getNationBuyerUuid() {
        return nationBuyerUuid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isActive() {
        return is_active;
    }

    public void setTradeId(Integer tradeId) {
        this.tradeId = tradeId;
    }

    public void setChestShopId(int chestShopId) {
        this.chestShopId = chestShopId;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public void setBuyerUuid(UUID buyerUuid) {
        this.buyerUuid = buyerUuid;
    }

    public void setTownUuid(UUID townUuid) {
        this.townUuid = townUuid;
    }

    public void setNationUuid(UUID nationUuid) {
        this.nationUuid = nationUuid;
    }

    public void setTownBuyerUuid(UUID townBuyerUuid) {
        this.townBuyerUuid = townBuyerUuid;
    }

    public void setNationBuyerUuid(UUID nationBuyerUuid) {
        this.nationBuyerUuid = nationBuyerUuid;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setActive(boolean is_active) {
        this.is_active = is_active;
    }
}