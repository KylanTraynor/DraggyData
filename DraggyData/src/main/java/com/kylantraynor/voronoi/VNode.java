package com.kylantraynor.voronoi;

public class VNode{
	public static final int Left = 1;
	public static final int Right = 2;
	public static final int Parent = 0;
	
	public final VNode[] nodes = new VNode[3];
	
	public VNode getSubNode(int direction){
		return nodes[direction];
	}
	
	public VNode getParent(){
		return getSubNode(VNode.Parent);
	}
	
	public void setSubNode(int direction, VNode node){
		if(direction == VNode.Parent){
			setParent(node);
			return;
		}
		VNode oldNode = nodes[direction];
		if(oldNode != null)
			oldNode.setParent(null);
		nodes[direction] = node;
		if(node != null)
			node.setParent(this);
	}
	
	private void setParent(VNode node){
		nodes[VNode.Parent] = node;
	}
	
	public boolean hasSubNodes(){
		if(hasSubNode(VNode.Left) || hasSubNode(VNode.Right)) return true;
		return false;
	}
	
	public boolean hasSubNode(int direction){
		return nodes[direction] != null;
	}
	
	public boolean hasParent(){
		return hasSubNode(VNode.Parent);
	}
	
	public VNode getFirstNode(){
		VNode current = this;
		do{
			if(current.hasParent()){
				current = current.getParent();
			} else {
				break;
			}
		}while(true);
		do{
			if(current.hasSubNode(Left)){
				current = current.getSubNode(Left);
			} else {
				break;
			}
		} while(true);
		return current;
	}
	
	public VCellNode getNode(int direction){
		if(direction == VNode.Parent) return null;
		VNode current = this;
		/* Goes to the first node which has a node in the requested direction different from
		 * the current branch.
		 * If the first node has a node in the requested direction, then skip this part.
		 */
		if(!current.hasSubNode(direction)){
			do{
				if(current.hasParent()){
					if(current.getParent().getSubNode(direction) != current){
						// The parent node has a node in the requested direction that isn't the current node.
						// So we want to go there, so we stop this loop going up.
						current = current.getParent();
						break;
					} else {
						current = current.getParent();
						continue;
					}
				} else {
					// If we end up at the root, then there was no node in the requested direction.
					// Basically, we've reached the end of the beachline in that direction.
					return null;
				}
			} while (true);
		}
		// Go in the requested direction.
		current = current.getSubNode(direction);
		// Follow the tree all the way down in the opposite direction.
		while(current.hasSubNode(VNode.getOpposite(direction))){
			current = current.getSubNode(VNode.getOpposite(direction));
		}
		return (VCellNode) current;
	}
	
	public static int getOpposite(int direction){
		switch(direction){
		case 1 : return 2;
		case 2 : return 1;
		default : return 0;
		}
	}

	public static VNode findNodeAt(VNode blRoot, float x, float sweepZ) {
		VNode current = blRoot;
		while(current != null){
			if(current.hasSubNodes() && current instanceof VEdgeNode){
				String edgeName = "("+((VEdgeNode)current).edge.edge.parentCell.getName()+","+((VEdgeNode)current).edge.edge.childCell.getName()+")";
				VCell mostRecentCell = ((VEdgeNode)current).edge.edge.childCell.site.z < ((VEdgeNode)current).edge.edge.parentCell.site.z ? ((VEdgeNode)current).edge.edge.parentCell : ((VEdgeNode)current).edge.edge.childCell; 
				// Then it's an edge node.
				// Get the Z value of the edge at x.
				float edgeZ = ((VEdgeNode)current).edge.getZ(x);
				// If the edge is vertical
				if(Float.isNaN(edgeZ)){
					int dir = ((VEdgeNode)current).edge.getFixedPoint().x < x ? Right : Left;
					current = current.getSubNode(dir);
					continue;
				}
				// Get the Z value of the parabola at x for sweepline z.
				float arcZ = mostRecentCell.parabola(x, sweepZ);
				// Check if arcZ is NaN.
				if(Float.isNaN(arcZ)){
					if(mostRecentCell.site.x < x){
						current = current.getSubNode(Right);
					} else {
						current = current.getSubNode(Left);
					}
					continue;
				}
				// Check if edge goes toward the left or the right
				boolean edgeGoesLeft = ((VEdgeNode)current).edge.getDirection().x < 0;
				// Checks if Z(edge) > Z(parabola)
				// If edge goes left and difference is positive, go right
				// If edge goes right and difference is negative, go right
				// Else, it goes left.
				boolean arcIsAbove = arcZ - edgeZ > 0;
				int dir = (edgeGoesLeft == !arcIsAbove) ? Left : Right;
				current = current.getSubNode(dir);
				continue;
			}
			break;
		}
		return current;
	}

	public void replace(VNode oldNode, VNode newNode){
		if(getSubNode(Left) == oldNode){
			setSubNode(Left, newNode);
		} else if(getSubNode(Right) == oldNode){
			setSubNode(Right, newNode);
		} else {
			try {
				throw new Exception();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		/*
		if(oldNode.hasParent()){
			if(oldNode.getParent().getSubNode(VNode.Left) == oldNode){
				oldNode.getParent().setSubNode(VNode.Left, newNode);
			} else {
				oldNode.getParent().setSubNode(VNode.Right, newNode);
			}
		}
		*/
	}

}