package com.kylantraynor.voronoi;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

public class VCell{
	Voronoi voronoi;
	VSite site;
	TreeSet<VHalfEdge> edges = new TreeSet<VHalfEdge>(getCheapEdgeSorter());
	VTriangle[] triangles;
	double[] xVertices;
	double[] zVertices;
	
	public String getName(){
		return "p" + site.id;
	}
	
	public VCell(VSite site, Voronoi v) {
		this.site = site;
		this.voronoi = v;
	}

	public VCell() {
	}

	/**
	 * Only for testing purposes. Remove in final version.
	 * @param g
	 */
	public Graphics draw(Graphics g){
		/*for(VHalfEdge e : edges){
			e.draw(g);
		}*/
		g.setColor(getColor());
		fill(g);
		g.setColor(Color.BLACK);
		return g;
	}
	
	public Graphics drawParabola(Graphics g, float sweeplineZ){
		g.setColor(Color.BLACK);
		g.drawString(getName(), (int) site.x + 4, (int) site.z + 4); 
		g.setColor(getColor());
		for(int x1 = 0; x1 < 799; x1+= 1){
			g.drawLine(x1, (int) parabola(x1, sweeplineZ), x1 + 1, (int) parabola(x1 + 1, sweeplineZ));
		}
		return g;
	}

	public Color getColor(){
		return new Color(255 - (int)((site.x / 1000.0) * 255), (int)((site.z / 1000.0) * 255), (int)(((site.x * site.z)/(1000*1000)) * 255));
	}
	
	public float parabola(float x, float sweepZ) {
		float dx = site.x - x;
		float dz = site.z - sweepZ;
		
		if(dz == 0){
			return Float.NaN;
		}
		
		return (dx * dx - sweepZ * sweepZ + site.z * site.z)/(2 * dz);
	}
	
	public void clip(float xmin, float zmin, float xmax, float zmax){
		for(VHalfEdge e : edges.toArray(new VHalfEdge[edges.size()])){
			e.clip(xmin, zmin, xmax, zmax);
			if(!e.isVisible()){
				edges.remove(e);
			}
		}
		regularizeEdges();
		if(!isClosed()){
			VHalfEdge[] a = edges.toArray(new VHalfEdge[edges.size()]);
			VHalfEdge e1 = edges.first();
			VHalfEdge e2 = edges.first();
			for(int i = 0; i < a.length; i++){
				int j = i + 1;
				if(i == a.length - 1) j = 0;
				if(a[i].getEndPoint() == a[j].getStartPoint()) continue;
				// Fill the gap
				e1 = a[i];
				e2 = a[j];
				
				float x1 = e1.getEndPoint().x;
				float z1 = e1.getEndPoint().z;
				float x2 = e2.getStartPoint().x;
				float z2 = e2.getStartPoint().z;
				
				ArrayList<VectorXZ> directions = new ArrayList<VectorXZ>();
				ArrayList<VectorXZ> midPoints = new ArrayList<VectorXZ>();
				float currentx = x1;
				float currentz = z1;
				
				if(!(x2 == xmin || x2 == xmax || z2 == zmin || z2 == zmax)){
					directions.add(new VectorXZ(x2 - x1, z2 - z1));
				} else {
					while(true){
						if((currentx == x2) && (x2 == xmin)){
							//if we're on the left edge
							directions.add(new VectorXZ(0, z2 - currentz));
							break;
						} else if((currentx == x2) && (x2 == xmax)){
							//if we're on the right edge
							directions.add(new VectorXZ(0, z2 - currentz));
							break;
						} else if((currentz == z2) && (z2 == zmin)){
							//if we're on the top edge
							directions.add(new VectorXZ(x2 - currentx, 0));
							break;
						} else if((currentz == z2) && (z2 == zmax)){
							//if we're on the bottom edge
							directions.add(new VectorXZ(x2 - currentx, 0));
							break;
						} else {
							VectorXZ corner = getNextCorner(currentx, currentz, xmin, zmin, xmax, zmax);
							directions.add(new VectorXZ(corner.x - currentx, corner.z - currentz));
							midPoints.add(corner);
							currentx = corner.x;
							currentz = corner.z;
						}
					}
				}
				for(int k = 0; k < directions.size(); k++){
					if(k == 0){
						if(k < midPoints.size()){
							new VFakeEdge(this, directions.get(k), e1.getEndPoint(), midPoints.get(k));
						} else {
							new VFakeEdge(this, directions.get(k), e1.getEndPoint(), e2.getStartPoint());
						}
					} else if(k < midPoints.size()){
						new VFakeEdge(this, directions.get(k), midPoints.get(k - 1), midPoints.get(k));
					} else {
						new VFakeEdge(this, directions.get(k), midPoints.get(k - 1), e2.getStartPoint());
					}
				}
			}
		}
		regularizeEdges();
	}
	
	private VectorXZ getNextCorner(float currentx, float currentz, float xmin, float zmin, float xmax, float zmax) {
		if((currentx == xmax)){
			if(currentz == zmax){
				return new VectorXZ(xmin, zmax);
			} else {
				return new VectorXZ(xmax, zmax);
			}
		} else if(currentx == xmin){
			if(currentz == zmin){
				return new VectorXZ(xmax, zmin);
			} else {
				return new VectorXZ(xmin, zmin);
			}
		} else {
			if(currentz == zmax){
				return new VectorXZ(xmin, zmax);
			} else {
				return new VectorXZ(xmax, zmin);
			}
		}
	}
	
	public void createPolygon(){
		ArrayList<VectorXZ> vertices = new ArrayList<VectorXZ>();
		for(VHalfEdge e : edges){
			if(!vertices.contains(e.getStartPoint()))
				vertices.add(e.getStartPoint());
			if(!vertices.contains(e.getEndPoint()))
				vertices.add(e.getEndPoint());
		}
		
		vertices.sort(getCheapComp());
		xVertices = new double[vertices.size()];
		zVertices = new double[vertices.size()];
		for(int i = 0; i < vertices.size(); i++){
			xVertices[i] = vertices.get(i).getX();
			zVertices[i] = vertices.get(i).getZ();
		}
	}

	public void fill(Graphics g){
		//NOT IMPLEMENTED
		//g.fillPolygon(Polygon());
	}
	
	public void generateTriangles(){
		triangles = new VTriangle[edges.size()];
		Iterator<VHalfEdge> iter = edges.iterator();
		int i = 0;
		while(iter.hasNext()){
			triangles[i] = new VTriangle(iter.next());
			i++;
		}
	}
	
	public double[] getVerticesX(){
		if(xVertices == null)
			createPolygon();
		return xVertices;
	}
	
	public double[] getVerticesZ(){
		if(zVertices == null)
			createPolygon();
		return zVertices;
	}
	
	public VTriangle[] getTriangles(){
		if(triangles == null) generateTriangles();
		return triangles;
	}
	
	public boolean isInside(VectorXZ v){
		for(VTriangle t : getTriangles()){
			if(t.isInside(v)) return true;
		}
		return false;
	}
	
	public VCell[] getNeighbours(){
		VCell[] result = new VCell[edges.size()];
		Iterator<VHalfEdge> iter = edges.iterator();
		int i = 0;
		
		while(iter.hasNext()){
			VEdge e = iter.next().edge;
			if(e == null){
				i++;
				continue;
			}
			if(e.childCell == this){
				result[i] = e.parentCell;
			} else {
				result[i] = e.childCell;
			}
			i++;
		}
		
		return result;
	}
	
	public boolean isClosed(){
		
		for(VHalfEdge e : edges){
			if(e.getNext() == null)
				return false;
		}
		return true;
	}
	
	void regularizeEdges(){
		for(VHalfEdge e : edges){
			if(!e.isInfinite()){
				e.regularize();
			}
		}
	}
	
	public boolean isCounterClockWise(VHalfEdge e){
		if(e.getAngleFromOrigin() == 0){
			if(e.getStartPoint().z > e.getEndPoint().z) return false;
			return true;
		}
		float angleS = e.getStartPoint().getCheapAngle(site);
		float angleE = e.getEndPoint().getCheapAngle(site);
		if((angleS < 1 && angleE > 3) || (angleS > 3 && angleE < 1)){
			return angleS - angleE > 0;
		}
		return angleE - angleS > 0;
	}
	
	private Comparator<VHalfEdge> getCheapEdgeSorter() {
		return (a, b) -> {
			float angleS = a.getAngleFromOrigin();
			float angleE = b.getAngleFromOrigin();
			float angle = angleS - angleE;
			if(angle > 0) return 1;
			if(angle < 0) return -1;
			return 0;
		};
	}
	
	public Comparator<? super VectorXZ> getCheapComp(){
		return (a, b) ->{
			float angleS = (float) a.getCheapAngle(site);
			float angleE = (float) b.getCheapAngle(site);
			float angle = angleS - angleE;
			if(angle > 0) return 1;
			if(angle < 0) return -1;
			return 0;
		};
	}
	
	public Comparator<? super VectorXZ> getComp(){
		return (a, b) ->{
			float angleS = (float) Math.atan2(a.z - site.z, a.x - site.x);
			float angleE = (float) Math.atan2(b.z - site.z, b.x - site.x);
			float angle = angleS - angleE;
			//float angleE = (float) Math.atan2(a.getEndPoint().z - site.z, a.getEndPoint().x - site.x);
			if(angle > 0) return 1;
			if(angle < 0) return -1;
			return 0;
		};
	}

	public VSite getSite() {
		return this.site;
	}
	
	public Voronoi getVoronoi(){
		return voronoi;
	}
}