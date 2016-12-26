package com.kylantraynor.voronoi;

import java.awt.Color;
import java.awt.Graphics;

public class VEdge {
	
	public VCell parentCell;
	public VCell childCell;
	
	public VectorXZ direction;
	public VectorXZ fixedPoint;
	
	public VectorXZ vertexA = VectorXZ.NaN;
	public VectorXZ vertexB = VectorXZ.NaN;
	
	public VHalfEdge leftHEdge;
	public VHalfEdge rightHEdge;
	private VectorXZ infiniteDirection;
	public boolean createdOnIntersection = false;

	public VEdge(VCell cell1, VCell cell2) {
		this.parentCell = cell1;
		this.childCell = cell2;
		this.direction = (new VectorXZ(-(childCell.site.z - parentCell.site.z), (childCell.site.x - parentCell.site.x))).normalize();
		this.infiniteDirection = this.direction;
		this.fixedPoint = new VectorXZ((parentCell.site.x + childCell.site.x) * 0.5f, (parentCell.site.z + childCell.site.z) * 0.5f);
		
		leftHEdge = new VHalfEdge(this, childCell, false);
		rightHEdge = new VHalfEdge(this, parentCell, true);
	}
	
	public boolean isInfinite(){
		if(vertexA.isInfinite() || vertexB.isInfinite())
			return true;
		return false;
	}
	
	public boolean isNaN(){
		if(vertexA.isNaN() || vertexB.isNaN())
			return true;
		return false;
	}
	
	public void draw(Graphics g){
		if(!isInfinite()){
			g.setColor(new Color((int)(((direction.x / 2f) + 0.5) * 255), 0, (int)(((direction.z / 2f) + 0.5) * 255)));
			g.drawLine((int) vertexA.x, (int) vertexA.z, (int) vertexB.x, (int) vertexB.z);
		}
	}
	
	public VectorXZ getDirection(){
		//if(isInfinite){
			return direction;
		/*} else {
			return new VectorXZ(vertexB.x - vertexA.x, vertexB.z - vertexA.z);
		}*/
	}

	public static VEdge bisect(VCell cellLeft, VCell cellRight){
		return new VEdge(cellLeft, cellRight);
	}
	
	public float getZ(float x){
		return VectorXZ.getRayIntersection(this.fixedPoint,
				this.direction, new VectorXZ(x, 0), new VectorXZ(0, -1f)).z;
		/*if(direction.z == 0){
			return Float.NaN;
		} else {
			return (float) (getA() * x + getB());
		}*/
	}
	
	public VectorXZ intersect(VEdge e){
		return VectorXZ.getRayIntersection(this.fixedPoint,
				this.direction, e.fixedPoint, e.direction);
		/*if(getA() == Float.NaN || e.getA() == Float.NaN) return null;
		float x = (e.getB() - getB()) / (getA() - e.getA());
		float z = getA() * x + getB();
		return new VectorXZ(x, z);*/
	}
	
	public VHalfEdge getLeftHalfEdge(){
		return leftHEdge;
	}
	
	public VCell getMostRecentCell(){
		if(parentCell.site.z < childCell.site.z) return childCell;
		return parentCell;
	}
	
	public VHalfEdge getRightHalfEdge(){
		return rightHEdge;
	}
	
	public void addVertex(VectorXZ v){
		if(vertexA.isNaN()){
			vertexA = v;
		} else if(vertexB.isNaN()){
			vertexB = v;
		}
	}
	
	public void clip(float x1, float z1, float x2, float z2) {
		for(int i = 0; i < 4; i++){
			clip(x1, z1, x2, z2, i);
		}
	}
	
	public void clip(float x1, float z1, float x2, float z2, int direction){
		switch(direction){
		case 0: //left
			if(vertexA.x < x1 && vertexB.x < x1){
				return;
			} else if(vertexA.x < x1){
				vertexA = VectorXZ.getRayIntersection(this.fixedPoint, this.direction, new VectorXZ(x1,z1), new VectorXZ(0,1));
			} else if(vertexB.x < x1){
				vertexB = VectorXZ.getRayIntersection(this.fixedPoint, this.direction, new VectorXZ(x1,z1), new VectorXZ(0,1));
			}
			break;
		case 1: //top
			if(vertexA.z < z1 && vertexB.z < z1){
				return;
			} else if(vertexA.z < z1){
				vertexA = VectorXZ.getRayIntersection(this.fixedPoint, this.direction, new VectorXZ(x1,z1), new VectorXZ(0,1));
			} else if(vertexB.z < z1){
				vertexB = VectorXZ.getRayIntersection(this.fixedPoint, this.direction, new VectorXZ(x1,z1), new VectorXZ(0,1));
			}
			break;
		case 2: //right
			if(vertexA.x < x2 && vertexB.x < x2){
				return;
			} else if(vertexA.x < x2){
				vertexA = VectorXZ.getRayIntersection(this.fixedPoint, this.direction, new VectorXZ(x2,z2), new VectorXZ(0,1));
			} else if(vertexB.x < x2){
				vertexB = VectorXZ.getRayIntersection(this.fixedPoint, this.direction, new VectorXZ(x2,z2), new VectorXZ(0,1));
			}
			break;
		case 3: //bottom
			if(vertexA.z < z2 && vertexB.z < z2){
				return;
			} else if(vertexA.z < z2){
				vertexA = VectorXZ.getRayIntersection(this.fixedPoint, this.direction, new VectorXZ(x2,z2), new VectorXZ(0,1));
			} else if(vertexB.z < z2){
				vertexB = VectorXZ.getRayIntersection(this.fixedPoint, this.direction, new VectorXZ(x2,z2), new VectorXZ(0,1));
			}
			break;
		}
	}

	/*
	public void flip() {
		VectorXZ t = vertexA;
		vertexA = vertexB;
		vertexB = t;
		direction = direction.flip();
	}*/
	
	public VectorXZ getProjectionOf(VectorXZ point){
		return VectorXZ.getRayIntersection(fixedPoint, direction, point, direction.getOrthogonal());
	}
	
	public float getAngleFromOrigin(VectorXZ origin){
		return getProjectionOf(origin).getCheapAngle(origin);
	}
	
	public void cleanUp(){
		if(vertexA.isNaN()){
			float x = infiniteDirection.x > 0 ? Float.NEGATIVE_INFINITY : (infiniteDirection.x < 0 ? Float.POSITIVE_INFINITY : fixedPoint.x);
			float z = infiniteDirection.z > 0 ? Float.NEGATIVE_INFINITY : (infiniteDirection.z < 0 ? Float.POSITIVE_INFINITY : fixedPoint.z);
			addVertex(new VectorXZ(x, z));
		}
		float x = infiniteDirection.x < 0 ? Float.NEGATIVE_INFINITY : (infiniteDirection.x > 0 ? Float.POSITIVE_INFINITY : fixedPoint.x);
		float z = infiniteDirection.z < 0 ? Float.NEGATIVE_INFINITY : (infiniteDirection.z > 0 ? Float.POSITIVE_INFINITY : fixedPoint.z);
		addVertex(new VectorXZ(x, z));
	}

	public void setInfiniteDirection(VectorXZ v) {
		this.infiniteDirection = v;
	}
	
	public VectorXZ getInfiniteDirection(){
		return this.infiniteDirection;
	}
	
	public VCell getOutsideCell(VCell c1, VCell c2, VCell c3){
		boolean canBeC1 = true;
		boolean canBeC2 = true;
		boolean canBeC3 = true;
		if(childCell == c1 || parentCell == c1) canBeC1 = false;
		if(childCell == c2 || parentCell == c2) canBeC2 = false;
		if(childCell == c3 || parentCell == c3) canBeC3 = false;
		if(canBeC1)
			return c1;
		else if(canBeC2)
			return c2;
		else if(canBeC3)
			return c3;
		else
			return null;
	}
}