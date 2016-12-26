package com.kylantraynor.voronoi;

public class VCellNode extends VNode {
	public VCell cell;
	
	public VCellNode(VCell cell) {
		this.cell = cell;
	}
	
	public VEdgeNode getEdgeNode(int direction) {
		if(direction == VNode.Parent) return (VEdgeNode)this.getParent();
		VNode current = this;
		while(current.hasParent()){
			if(current.getParent().getSubNode(VNode.getOpposite(direction)) == current){
				return (VEdgeNode) current.getParent();
			} else {
				current = current.getParent();
			}
		}
		return null;
	}
}
