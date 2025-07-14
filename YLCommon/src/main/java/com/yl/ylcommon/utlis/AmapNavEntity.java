package com.yl.ylcommon.utlis;

public class AmapNavEntity {
    private String addressName;
    private String latLot;
    private  String  poiName;

    public String getAddressName() {
        return addressName;
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }

    public String getLatLot() {
        return latLot;
    }

    public void setLatLot(String latLot) {
        this.latLot = latLot;
    }

    public String getPoiName() {
        return poiName;
    }

    public void setPoiName(String poiName) {
        this.poiName = poiName;
    }

    @Override
    public String toString() {
        return "AmapNavEntity{" +
                "addressName='" + addressName + '\'' +
                ", latLot='" + latLot + '\'' +
                ", poiName='" + poiName + '\'' +
                '}';
    }

    public AmapNavEntity(String addressName, String latLot, String poiName) {
        this.addressName = addressName;
        this.latLot = latLot;
        this.poiName = poiName;
    }

    public AmapNavEntity() {
    }
}
