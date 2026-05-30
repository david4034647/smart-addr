/*
 * Decompiled with CFR 0.152.
 */
package rrd.bean;

public class Region {
    private int regionId;
    private String regionName;
    private int parRegionId;

    public int getRegionId() {
        return this.regionId;
    }

    public void setRegionId(int regionId) {
        this.regionId = regionId;
    }

    public String getRegionName() {
        return this.regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public int getParRegionId() {
        return this.parRegionId;
    }

    public void setParRegionId(int parRegionId) {
        this.parRegionId = parRegionId;
    }
}
