package com.dds.geofence;

public class GeofenceDetailData extends GeofenceData
{
	protected String lineColor;
	protected String lineWidth;
	protected String polygonColor;
	protected String polygonCoordinates;
	
	public GeofenceDetailData()
	{
		super();
	}
	
    public GeofenceDetailData(String regionId, String regionName, String category, String parentId, String description, String lineColor, String lineWidth, String polygonColor, String polygonCoordinates) {
    	super(regionId, regionName, category, parentId, description);
    	this.lineColor = lineColor;
    	this.lineWidth = lineWidth;
    	this.polygonColor = polygonColor;
    	this.polygonCoordinates = polygonCoordinates;
    	
    }

    public GeofenceDetailData(String regionId, String regionName, String category, String parentId, String description, String guName, String doName, String viewName, String lineColor, String lineWidth, String polygonColor, String polygonCoordinates) {
        super(regionId, regionName, category, parentId, description, guName, doName, viewName);
        this.lineColor = lineColor;
    	this.lineWidth = lineWidth;
    	this.polygonColor = polygonColor;
    	this.polygonCoordinates = polygonCoordinates;
    }
    
    public String getLineColor()
    {
    	return this.lineColor;
    }
    
    public String getLineWidth()
    {
    	return this.lineWidth;
    }
    
    public String getPolygonColor()
    {
    	return this.polygonColor;
    }
    
    public String getPolygonCoordinates()
    {
    	return this.polygonCoordinates;
    }
    
    public void setLineColor(String lineColor)
    {
    	this.lineColor = lineColor;
    }
    
    public void setLineWidth(String lineWidth)
    {
    	this.lineWidth = lineWidth;
    }
    
    public void setPolygonColor(String polygonColor)
    {
    	this.polygonColor = polygonColor;
    }
    
    public void setPolygonCoordinates(String polygonCoordinates)
    {
    	this.polygonCoordinates = polygonCoordinates;
    }

}
