package com.dds.geofence;

/**
 * Created by Seungbum Heo on 2016-06-21.
 */
public class GeofenceData {
    protected String regionId;
    protected String regionName;
    protected String category;
    protected String parentId;
    protected String description;

    protected String guName;
    protected String doName;
    protected String viewName;

    public GeofenceData(String regionId, String regionName, String category, String parentId, String description) {
        this.regionId = regionId;
        this.regionName = regionName;
        this.category = category;
        this.parentId = parentId;
        this.description = description;
    }

    public GeofenceData(String regionId, String regionName, String category, String parentId, String description, String guName, String doName, String viewName) {
        this(regionId, regionName, category, parentId, description);

        this.guName = guName;
        this.doName = doName;
        this.viewName = viewName;
    }

    public GeofenceData()
    {
        this(null, null, null, null, null);

        this.guName = null;
        this.doName = null;
        this.viewName = null;
    }

    public String getRegionId() {
        return regionId;
    }

    public String getRegionName() {
        return regionName;
    }

    public String getCategory() {
        return category;
    }

    public String getParentId() {
        return parentId;
    }

    public String getDescription() {
        return description;
    }

    public String getGuName() {
        return guName;
    }

    public String getDoName() {
        return doName;
    }

    public String getViewName() {
        return viewName;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGuName(String guName) {
        this.guName = guName;
    }

    public void setDoName(String doName) {
        this.doName = doName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }
}
