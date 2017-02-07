package com.kylantraynor.voronoi;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class Voronoi<T extends VCell> {
	//private VCell[] cells;
	private VCell[] cells;
	
	private float minXBound;
	private float minZBound;
	private float maxXBound;
	private float maxZBound;
	/*
	public Voronoi(VectorXZ[] sites, Float[] ws, float x1, float z1, float x2, float z2) {
		this.cells = new VCell[sites.length];
		
		for(int i = 0; i < sites.length; i++){
			VSite s = new VSite(sites[i].x, sites[i].z, ws[i]);
			s.id = i;
			VCell c = new VCell(s, this);
			this.cells[i] = c;
		}
		
		minXBound = x1 < x2 ? x1 : x2;
		maxXBound = x1 < x2 ? x2 : x1;
		minZBound = z1 < z2 ? z1 : z2;
		maxZBound = z1 < z2 ? z2 : z1;
	}
	*/
	
	@SuppressWarnings("unchecked")
	public Voronoi(Class<T> cls, VectorXZ[] sites, Float[] ws, float x1, float z1, float x2, float z2) {
		this.cells = (T[]) Array.newInstance(cls, sites.length);
		for(int i = 0; i < sites.length; i++){
			VSite s = new VSite(sites[i].x, sites[i].z, ws[i]);
			s.id = i;
			VCell c = null;
			try {
				c = cls.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			c.site = s;
			c.voronoi = this;
			this.cells[i] = c;
		}
		
		minXBound = x1 < x2 ? x1 : x2;
		maxXBound = x1 < x2 ? x2 : x1;
		minZBound = z1 < z2 ? z1 : z2;
		maxZBound = z1 < z2 ? z2 : z1;
	}
	
	@SuppressWarnings("unchecked")
	public Voronoi(Class<T> cls, VSite[] sites, float x1, float z1, float x2, float z2){
		this.cells = (T[]) Array.newInstance(cls, sites.length);
		for(int i = 0; i < sites.length; i++){
			sites[i].id = i;
			try {
				this.cells[i] = cls.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.cells[i].site = sites[i];
			this.cells[i].voronoi = this;
		}
		
		minXBound = x1 < x2 ? x1 : x2;
		maxXBound = x1 < x2 ? x2 : x1;
		minZBound = z1 < z2 ? z1 : z2;
		maxZBound = z1 < z2 ? z2 : z1;
	}
	
	public void generate(){
		this.done = false;
		PriorityQueue<VEvent> q = new PriorityQueue<VEvent>(getComp());
		HashMap<VNode, VVertexEvent> currentCircles = new HashMap<VNode, VVertexEvent>();
		VNode blRoot = null;
		VEvent p = null;
		for(VCell cell : cells){
			q.add(new VSiteEvent(cell.site));
		}
		while((p = q.poll()) != null){
			if(p instanceof VSiteEvent){
				blRoot = processSiteEvent(p, q, blRoot);
			} else if (p instanceof VVertexEvent){
				currentCircles.remove(((VVertexEvent) p).middleCell);
				if(!((VVertexEvent) p).isValid()){
					continue;
				}
				blRoot = processVertexEvent((VVertexEvent) p, q, blRoot);
			}
		}
		cleanupEdges();
		for(VCell c : cells){
			c.clip(minXBound, minZBound, maxXBound, maxZBound);
		}
		
		this.done = true;
	}

	private PriorityQueue<VEvent> q = new PriorityQueue<VEvent>(getComp());
	private VNode blRoot = null;
	public float sweepLine = 0;
	private VEvent p;
	
	public boolean done = false;
	
	public void generateStep(){
		if(q.size() == 0 && !done){
			blRoot = null;
			sweepLine = 0;
			p = null;
			for(VCell c : cells){
				c.edges.clear();
				q.add(new VSiteEvent(c.site));
			}
		} else if(q.size() > 0 && !done){
			p = q.poll();
			sweepLine = p.getZ();
			if(p instanceof VSiteEvent){
				blRoot = processSiteEvent(p, q, blRoot);
			} else if(p instanceof VVertexEvent){
				if(((VVertexEvent) p).isValid())
					blRoot = processVertexEvent((VVertexEvent) p, q, blRoot);
			}
			
			if(q.size() == 0){
				cleanupEdges();
				for(VCell c : cells){
					c.clip(minXBound, minZBound, maxXBound, maxZBound);
				}
				
				this.done = true;
			}
		}
	}
	
	private void cleanupEdges() {
		for(VCell c : cells){
			for(VHalfEdge e : c.edges){
				e.edge.cleanUp();
			}
		}
	}

	private VNode processVertexEvent(VVertexEvent p, PriorityQueue<VEvent> q,
			VNode blRoot) {
		// We have p1, pi, pj, pk, p2 we want p1, pi, pk, p2
		//System.out.printf("\nProcessing Intersection at " + (int)p.intersection.x + ", " +(int)p.intersection.z + ".");
		//System.out.printf(" Intersection between (" + p.leftCell.cell.getName() + ", " + p.middleCell.cell.getName() + ", " + p.rightCell.cell.getName()+ ").");
		// Delete Arc pj from the beachline
		VCellNode pj = p.middleCell;
		VCellNode pi = pj.getNode(VNode.Left);
		VCellNode pk = pj.getNode(VNode.Right);
		
		if(pi == null || pj.getParent() == null || pk == null || (pi.cell != p.leftCell.cell) || (pk.cell != p.rightCell.cell)){
			//System.out.printf("\nInvalid Intersection.");
			return blRoot;
		}
		
		VCellNode p1 = (VCellNode) pi.getNode(VNode.Left);
		VCellNode p2 = (VCellNode) pk.getNode(VNode.Right);
		VEdgeNode pjSuperficialEdge = (VEdgeNode) pj.getParent();
		VEdgeNode pjDeepEdge = pj.getEdgeNode(VNode.Right);
		
		if(pjSuperficialEdge.getSubNode(VNode.Left) == pj){
			pjDeepEdge = pi.getEdgeNode(VNode.Right);
			
			pjSuperficialEdge.getParent().replace(pjSuperficialEdge, pjSuperficialEdge.getSubNode(VNode.Right));
		} else {
			pjDeepEdge = pj.getEdgeNode(VNode.Right);
			
			pjSuperficialEdge.getParent().replace(pjSuperficialEdge, pjSuperficialEdge.getSubNode(VNode.Left));
		}
		
		// Set the intersection as the end point of the edges.
		pjSuperficialEdge.edge.edge.addVertex(p.intersection);
		VCell o1 = pjSuperficialEdge.edge.edge.getOutsideCell(pi.cell, pj.cell, pk.cell);
		VectorXZ v1 = pjSuperficialEdge.edge.getFixedPoint().substract(pjSuperficialEdge.edge.edge.getProjectionOf(o1.site));
		pjSuperficialEdge.edge.edge.setInfiniteDirection(v1);	
		
		pjDeepEdge.edge.edge.addVertex(p.intersection);
		VCell o2 = pjDeepEdge.edge.edge.getOutsideCell(pi.cell, pj.cell, pk.cell);
		VectorXZ v2 = pjDeepEdge.edge.getFixedPoint().substract(pjDeepEdge.edge.edge.getProjectionOf(o2.site));
		pjDeepEdge.edge.edge.setInfiniteDirection(v2);
		
		// Start a new Edge between pi and pk.
		VEdge bisector = VEdge.bisect(pi.cell, pk.cell);
		VEdgeNode edge = new VEdgeNode(bisector.getLeftHalfEdge());
		
		edge.setSubNode(VNode.Left, pjDeepEdge.getSubNode(VNode.Left));
		edge.setSubNode(VNode.Right, pjDeepEdge.getSubNode(VNode.Right));
		if(pjDeepEdge.hasParent())
			pjDeepEdge.getParent().replace(pjDeepEdge, edge);
		else blRoot = edge;
		
		// Set the intersection as the starting point of that edge.
		bisector.addVertex(p.intersection);
		VectorXZ v3 = bisector.fixedPoint.substract(bisector.getProjectionOf(pj.cell.site));
		bisector.setInfiniteDirection(v3);
		
		// Remove any intersection event related to pj.
		if(p1 != null)
			removeIntersectionsOf(q, p1, pi, pj);
		if(p2 != null)
			removeIntersectionsOf(q, pj, pk, p2);
		
		// Add new events for p1, pi, pk and pi pk, p2
		if(p1 != null){
			VEdge p1piEdge = pi.getEdgeNode(VNode.Left).edge.edge;
			addIntersectionFor(q, p1, pi, pk, p1piEdge, edge.edge.edge);
		}
		if(p2 != null){
			VEdge pkp2Edge = pk.getEdgeNode(VNode.Right).edge.edge;
			addIntersectionFor(q, pi, pk, p2, edge.edge.edge, pkp2Edge);
		}
		return blRoot;
	}
	
	private VNode processSiteEvent(VEvent p, PriorityQueue<VEvent> q, VNode blRoot) {
		// Determine which arc (called pj) lies immediately below this point in the beach line.
		if(blRoot == null){
			blRoot = new VCellNode(getCell(((VSiteEvent)p).site));
			return blRoot;
		}
		
		// We have (p1, pj, p2) we want (p1, pj', pi, pj", p2)
		
		VCellNode pj = (VCellNode) VNode.findNodeAt(blRoot, p.getX(), p.getZ());
		VCellNode p1 = (VCellNode) pj.getNode(VNode.Left);
		VCellNode p2 = (VCellNode) pj.getNode(VNode.Right);
		
		// Split arc pj, replacing it with 3 arcs (pj', pi, pj") in the beach line.
		VCellNode pi = new VCellNode(getCell(((VSiteEvent)p).site));
		VCellNode pjLeft = null;
		VCellNode pjRight = null;
		
		// Create the edge between pj and pi.
		VEdge bisector = VEdge.bisect(pj.cell, pi.cell);
		VEdgeNode edgeLeft = new VEdgeNode(bisector.getLeftHalfEdge());
		VEdgeNode edgeRight = new VEdgeNode(bisector.getRightHalfEdge());
		
		if(Math.abs(bisector.parentCell.site.z - bisector.childCell.site.z) < 0.0000000001){
			if(bisector.parentCell.site.x < bisector.childCell.site.x){
				pjLeft = new VCellNode(pj.cell);
				edgeLeft = new VEdgeNode(bisector.getLeftHalfEdge());
				edgeLeft.setSubNode(VNode.Left, pjLeft);
				edgeLeft.setSubNode(VNode.Right, pi);
			} else {
				pjRight = new VCellNode(pj.cell);
				edgeLeft = new VEdgeNode(bisector.getRightHalfEdge());
				edgeLeft.setSubNode(VNode.Left, pi);
				edgeLeft.setSubNode(VNode.Right, pjRight);
			}
		} else {
			pjLeft = new VCellNode(pj.cell);
			pjRight = new VCellNode(pj.cell);
			edgeLeft = new VEdgeNode(bisector.getLeftHalfEdge());
			edgeRight = new VEdgeNode(bisector.getRightHalfEdge());
			edgeLeft.setSubNode(VNode.Left, pjLeft);
			edgeLeft.setSubNode(VNode.Right, edgeRight);
			edgeRight.setSubNode(VNode.Left, pi);
			edgeRight.setSubNode(VNode.Right, pjRight);
		}
		
		if(pj.hasParent()){
			pj.getParent().replace(pj, edgeLeft);
		} else {
			return edgeLeft;
		}
		
		// Remove intersections involving pj.
		if(p1 != null && p2 != null){
			removeIntersectionsOf(q, p1, pj, p2);
		}
		// Create intersections involving pi.
		if(p1 != null && pjLeft != null){
			VEdge p1pjEdge = pjLeft.getEdgeNode(VNode.Left).edge.edge;
			addIntersectionFor(q, p1, pjLeft, pi, p1pjEdge, edgeLeft.edge.edge);
		}
		if(p2 != null && pjRight != null){
			VEdge pjp2Edge = pjRight.getEdgeNode(VNode.Right).edge.edge;
			addIntersectionFor(q, pi, pjRight, p2, edgeRight.edge.edge, pjp2Edge);	
		}
		
		return blRoot;
	}

	private void addIntersectionFor(PriorityQueue<VEvent> q, VCellNode cell1,
			VCellNode cell2, VCellNode cell3, VEdge leftEdge, VEdge rightEdge) {
		VVertexEvent ve = new VVertexEvent(cell1, cell2, cell3, leftEdge, rightEdge);
		if(ve.isValid()){
			q.add(ve);
		}
	}

	private void removeIntersectionsOf(PriorityQueue<VEvent> q, VCellNode cell1, VCellNode cell2, VCellNode cell3) {
		for(VEvent e : q.toArray(new VEvent[q.size()])){
			if(e instanceof VVertexEvent){
				VVertexEvent v = (VVertexEvent) e;
				if((v.leftCell == cell1) && 
						(v.middleCell == cell2) && 
						(v.rightCell == cell3)){
					q.remove(e);
				}
			}
		}
	}

	public T getCell(VSite site){
		for(VCell c : cells){
			if(c.site == site) return (T) c;
		}
		return null;
	}
	
	public T getCellAt(VectorXZ v){
		for(VCell c : cells){
			if(c.isInside(v)) return (T) c;
		}
		return null;
	}
	
	public VTriangle getTriangleAt(VectorXZ v){
		for(VCell c: cells){
			for(VTriangle t : c.getTriangles()){
				if(t.isInside(v)) return t;
			}
		}
		return null;
	}
	
	public VTriangle getTriangleAt(VectorXZ v, VTriangle lastKnown){
		if(lastKnown == null) return getTriangleAt(v);
		if(lastKnown.isInside(v)) return lastKnown;
		for(VTriangle t : lastKnown.getOwner().getTriangles()){
			if(t.isInside(v)) return t;
		}
		for(VCell c : lastKnown.getOwner().getNeighbours()){
			if(c == null) continue;
			for(VTriangle t : c.getTriangles()){
				if(t.isInside(v)) return t;
			}
		}
		return getTriangleAt(v);
	}

	private Comparator<VEvent> getComp(){
		return (a, b) ->{
			if(a.getZ() < b.getZ()) return -1;
			if(a.getZ() > b.getZ()) return 1;
			if(a.getX() < b.getX()) return -1;
			if(a.getX() > b.getX()) return 1;
			return 0;
		};
	}

	public boolean isDone() {
		return this.done;
	}

	public T[] getCells() {
		return (T[]) this.cells;
	}
}
