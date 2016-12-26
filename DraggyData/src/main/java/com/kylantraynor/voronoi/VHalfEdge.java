package com.kylantraynor.voronoi;

import java.awt.Color;
import java.awt.Graphics;

public class VHalfEdge {
	
	public VectorXZ startPoint = VectorXZ.NaN, endPoint = VectorXZ.NaN;
	public VEdge edge;
	public VCell cell;
	public boolean flipped = false;
	public VHalfEdge previous;
	public VHalfEdge next;
	private boolean visible;
	
	public VHalfEdge(VEdge edge, VCell cell, boolean flipped) {
		this.edge = edge;
		this.cell = cell;
		this.flipped = flipped;
		if(cell != null)
			this.cell.edges.add(this);
	}
	
	public VHalfEdge getNext(){
		if(cell.edges.size() <= 1) return null;
		VHalfEdge[] cellEdges = cell.edges.toArray(new VHalfEdge[cell.edges.size()]);
		for(int i = 0; i < cellEdges.length; i++){
			int j = i + 1;
			if(i == cellEdges.length - 1){
				j = 0;
			}
			if(cellEdges[i] == this){
				if(cellEdges[i] == cellEdges[j]) continue;
				if(cellEdges[i].getEndPoint() == cellEdges[j].getStartPoint()){
					return cellEdges[j];
				}
			}
		}
		return null;
	}
	
	public VHalfEdge getPrevious(){
		if(cell.edges.size() <= 1) return null;
		VHalfEdge[] cellEdges = cell.edges.toArray(new VHalfEdge[cell.edges.size()]);
		for(int i = 0; i < cellEdges.length; i++){
			int j = i - 1;
			if(i == 0){
				j = cellEdges.length - 1;
			}
			if(cellEdges[i] == this){
				if(cellEdges[i] == cellEdges[j]) continue;
				if(cellEdges[i].getStartPoint() == cellEdges[j].getEndPoint()){
					return cellEdges[j];
				}
			}
		}
		return null;
	}
	
	public void draw(Graphics g){
		if(!isInfinite()){
			float baseAngle = getAngleFromOrigin();
			/*if((baseAngle < 2) == (getStartPoint().x < getEndPoint().x)){
				this.flipped = !this.flipped;
			}*/
			if(baseAngle < 1){
				g.setColor(Color.BLUE);
			} else if(baseAngle < 2){
				g.setColor(Color.RED);
			} else if(baseAngle < 3){
				g.setColor(Color.GREEN);
			} else{
				g.setColor(Color.ORANGE);
			}
			int x1 = (int) ((getStartPoint().x * 9 + cell.site.x)/10);
			int z1 = (int) ((getStartPoint().z * 9 + cell.site.z)/10);
			int x2 = (int) ((getEndPoint().x * 9 + cell.site.x) /10);
			int z2 = (int) ((getEndPoint().z * 9 + cell.site.z) /10);
			float angle = (baseAngle/4f);
			//g.setColor(new Color((int) (angle * 255), 0, (int) (255 - (angle * 255))));
			g.drawLine(x1, z1, x2, z2);
			g.drawOval(x1 - 2, z1 - 2, 4, 4);
			//g.setColor(Color.BLACK);
			//g.drawString("" + baseAngle, (x1 + x2) / 2  + 5, (z1 + z2) / 2  + 5);
			//g.drawString("(" + getEndPoint().x + "," + getEndPoint().z + ")", x2 + 5, z2 -5);
		} else {
			float baseAngle = getAngleFromOrigin();
			/*if((baseAngle < 2) == (getStartPoint().x < getEndPoint().x)){
				this.flipped = !this.flipped;
			}*/
			if(baseAngle < 1){
				g.setColor(Color.BLUE);
			} else if(baseAngle < 2){
				g.setColor(Color.RED);
			} else if(baseAngle < 3){
				g.setColor(Color.GREEN);
			} else{
				g.setColor(Color.ORANGE);
			}
			if(edge != null)
				if(edge.createdOnIntersection)
					g.setColor(Color.PINK);
				else
					g.setColor(Color.BLACK);
			
			int x1 = 0;
			int z1 = 0;
			if(Float.isInfinite(getStartPoint().x))
				x1 = (int) getStartPoint().x < 0 ? -4000 : 5000;
			else
				x1 = (int) ((getStartPoint().x * 9 + cell.site.x)/10);
			if(Float.isInfinite(getStartPoint().z))
				z1 = (int) getStartPoint().z < 0 ? -4000 : 5000;
			else
				z1 = (int) ((getStartPoint().z * 9 + cell.site.z)/10);
			int x2 = 0;
			int z2 = 0;
			if(Float.isInfinite(getEndPoint().x))
				x2 = (int) getEndPoint().x < 0 ? -4000 : 5000;
			else
				x2 = (int) ((getEndPoint().x * 9 + cell.site.x)/10);
			if(Float.isInfinite(getEndPoint().z))
				z2 = (int) getEndPoint().z < 0 ? -4000 : 5000;
			else
				z2 = (int) ((getEndPoint().z * 9 + cell.site.z)/10);
			float angle = (baseAngle/4f);
			//g.setColor(new Color((int) (angle * 255), 0, (int) (255 - (angle * 255))));
			g.drawLine(x1, z1, x2, z2);
			g.drawOval(x1 - 2, z1 - 2, 4, 4);
			
			g.setColor(Color.BLACK);
			if(this instanceof VHalfEdge)
				g.drawString("(" + edge.getDirection().x + "," + edge.getDirection().z + ")", (int) getFixedPoint().x  + 5, (int) getFixedPoint().z  + 5);
		}
	}
	
	public VectorXZ getDirection(){
		if(flipped){
			return edge.getDirection().flip();
		} else {
			return edge.getDirection();
		}
	}
	
	public VectorXZ getEndPoint(){
		if(flipped)
			return edge.vertexA;
		return edge.vertexB;
	}
	
	public VectorXZ getStartPoint(){
		if(flipped)
			return edge.vertexB;
		return edge.vertexA;
	}
	
	public void setStartPoint(VectorXZ v){
		if(flipped)
			edge.vertexB = v;
		else
			edge.vertexA = v;
	}
	
	public void setEndPoint(VectorXZ v){
		if(flipped)
			edge.vertexA = v;
		else
			edge.vertexB = v;
	}

	public float getZ(float x) {
		if((edge.getMostRecentCell().site.x < x) == (getDirection().x < 0)){
			return Float.NEGATIVE_INFINITY;
		}
		return edge.getZ(x);
	}

	public VectorXZ getFixedPoint() {
		return edge.fixedPoint;
	}
	
	public boolean isInfinite(){
		if(getStartPoint().isInfinite() || getEndPoint().isInfinite())
			return true;
		return false;
	}
	
	public void clip(float x1, float z1, float x2, float z2) {
		this.CohenSutherlandLineClip(x1, z1, x2, z2);
	}

	public void regularize() {
		float baseAngle = getAngleFromOrigin();
		if(baseAngle == 0){
			if(getStartPoint().z > getEndPoint().z){
				this.flipped = !this.flipped;
			}
		} else if(Math.abs(baseAngle - 2) < 0.000001){
			if(getStartPoint().z < getEndPoint().z){
				this.flipped = !this.flipped;
			}
		} else {
			if((baseAngle < 2) == (getStartPoint().x < getEndPoint().x)){
				this.flipped = !this.flipped;
			}
		}
	}
	
	public float getAngleFromOrigin(){
		return edge.getAngleFromOrigin(cell.site);
	}
	
	public void CohenSutherlandLineClip(float xMin, float zMin, float xMax, float zMax){
		int code1 = getStartPoint().getClipCode(xMin, zMin, xMax, zMax);
		int code2 = getEndPoint().getClipCode(xMin, zMin, xMax, zMax);
		
		boolean accept = false;
		while(true){
			if((code1 | code2) == 0){
				accept = true;
				break;
			} else if((code1 & code2) != 0){
				break;
			} else {
				int codeOut = (code1 != 0) ? code1 : code2;
				float x = 0, z = 0;
				if((codeOut & 8) != 0){
					x = getFixedPoint().x + getDirection().x * (zMax - getFixedPoint().z)/getDirection().z;
					z = zMax;
				} else if((codeOut & 4) != 0){
					x = getFixedPoint().x + getDirection().x * (zMin - getFixedPoint().z)/getDirection().z;
					z = zMin;
				} else if((codeOut & 2) != 0){
					z = getFixedPoint().z + getDirection().z * (xMax - getFixedPoint().x)/getDirection().x;
					x = xMax;
				} else if((codeOut & 1) != 0){
					z = getFixedPoint().z + getDirection().z * (xMin - getFixedPoint().x)/getDirection().x;
					x = xMin;
				}
				
				if(codeOut == code1){
					setStartPoint(new VectorXZ(x, z));
					code1 = getStartPoint().getClipCode(xMin, zMin, xMax, zMax);
				} else {
					setEndPoint(new VectorXZ(x, z));
					code2 = getEndPoint().getClipCode(xMin, zMin, xMax, zMax);
				}
			}
		}
		
		if(accept){
			this.visible = true;
		} else {
			this.visible = false;
		}
	}

	public boolean isVisible() {
		return visible;
	}
}
