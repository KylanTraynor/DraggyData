package com.kylantraynor.voronoi;

public class VFakeEdge extends VHalfEdge{

	private VectorXZ direction, startPoint, endPoint;
	
	public VFakeEdge(VCell cell, VectorXZ dir, VectorXZ startPoint, VectorXZ endPoint) {
		super(null, null, false);
		this.cell = cell;
		this.direction = dir;
		this.startPoint = startPoint;
		this.endPoint = endPoint;
		this.cell.edges.add(this);
	}
	
	public VectorXZ getStartPoint(){
		if(this.flipped)
			return endPoint;
		return startPoint;
	}
	
	public void setStartPoint(VectorXZ v){
		if(this.flipped)
			endPoint = v;
		startPoint = v;
	}
	
	public VectorXZ getEndPoint(){
		if(this.flipped)
			return startPoint;
		return endPoint;
	}
	
	public void setEndPoint(VectorXZ v){
		if(this.flipped)
			startPoint = v;
		endPoint = v;
	}
	
	public VectorXZ getDirection(){
		if(this.flipped)
			return direction.flip();
		return direction;
	}
	
	public VectorXZ getFixedPoint(){
		return startPoint;
	}
	
	public float getAngleFromOrigin(){
		VectorXZ projection = VectorXZ.getRayIntersection(getFixedPoint(), getDirection(), cell.site, getDirection().getOrthogonal());
		return projection.getCheapAngle(cell.site);
	}
}
