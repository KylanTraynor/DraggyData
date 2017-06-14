package com.kylantraynor.voronoi;

public class VEdgeNode extends VNode{
	public VHalfEdge edge;
	
	public VEdgeNode(VHalfEdge halfEdge) {
		edge = halfEdge;
	}
}
