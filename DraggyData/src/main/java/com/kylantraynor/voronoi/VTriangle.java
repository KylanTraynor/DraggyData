package com.kylantraynor.voronoi;

public class VTriangle {
	
	public VectorXZ[] points = new VectorXZ[3];
	private VCell owner;
	
	public VTriangle(VCell cell, VectorXZ p1, VectorXZ p2){
		this.setOwner(cell);
		points[0] = p1;
		points[1] = p2;
		points[2] = cell.site;
	}

	public VTriangle(VHalfEdge edge) {
		this(edge.cell, edge.getStartPoint(), edge.getEndPoint());
	}

	public VCell getOwner() {
		return owner;
	}

	public void setOwner(VCell owner) {
		this.owner = owner;
	}

	public boolean isInside(VectorXZ point) {
		VectorXZ v0 = points[2].substract(points[0]);
		VectorXZ v1 = points[1].substract(points[0]);
		VectorXZ v2 = point.substract(points[0]);
		
		float dot00 = v0.dotProduct(v0);
		float dot01 = v0.dotProduct(v1);
		float dot02 = v0.dotProduct(v2);
		float dot11 = v1.dotProduct(v1);
		float dot12 = v1.dotProduct(v2);
		
		float denom = ((dot00 * dot11) - (dot01 * dot01));
		if(denom == 0) return false;
		float u = (dot11 * dot02 - dot01 * dot12) / denom;
		float v = (dot00 * dot12 - dot01 * dot02) / denom;
		
		return (u >= 0) && (v >= 0) && (u + v < 1);
	}
}
