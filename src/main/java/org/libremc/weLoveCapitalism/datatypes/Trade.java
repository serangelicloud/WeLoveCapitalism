package org.libremc.weLoveCapitalism.datatypes;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

import java.util.UUID;

public class Trade {
    private Integer tradeId;
    private int chestShopId;
    private String item;
    private int amount;
    private int price;
    private UUID ownerUuid;
    private UUID buyerUuid;
    private UUID townUuid;
    private UUID nationUuid;
    private UUID townBuyerUuid;
    private UUID nationBuyerUuid;

    public Trade(int chestShopId, String item, int amount, int price,
                 UUID ownerUuid, UUID buyerUuid, UUID townUuid) {
        this.chestShopId = chestShopId;
        this.item = item;
        this.amount = amount;
        this.price = price;
        this.ownerUuid = ownerUuid;
        this.buyerUuid = buyerUuid;
        this.townUuid = townUuid;
    }

    public Trade(Integer tradeId, int chestShopId, String item, int amount, int price,
                 UUID ownerUuid, UUID buyerUuid, UUID townUuid, UUID nationUuid,
                 UUID townBuyerUuid, UUID nationBuyerUuid) {
        this.tradeId = tradeId;
        this.chestShopId = chestShopId;
        this.item = item;
        this.amount = amount;
        this.price = price;
        this.ownerUuid = ownerUuid;
        this.buyerUuid = buyerUuid;
        this.townUuid = townUuid;
        this.nationUuid = nationUuid;
        this.townBuyerUuid = townBuyerUuid;
        this.nationBuyerUuid = nationBuyerUuid;
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

    public int getAmount() {
        return amount;
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

    public void setTradeId(Integer tradeId) {
        this.tradeId = tradeId;
    }

    public void setChestShopId(int chestShopId) {
        this.chestShopId = chestShopId;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public void setAmount(int amount) {
        this.amount = amount;
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

}