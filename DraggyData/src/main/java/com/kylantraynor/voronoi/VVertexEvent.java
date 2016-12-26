package com.kylantraynor.voronoi;

public class VVertexEvent extends VEvent{
	
	public VCellNode leftCell;
	public VCellNode middleCell;
	public VCellNode rightCell;
	
	public VectorXZ intersection;
	private boolean valid = true;

	public VVertexEvent(VCellNode cell1, VCellNode cell2, VCellNode cell3, VEdge leftEdge,
			VEdge rightEdge) {
		this.leftCell = cell1;
		this.middleCell = cell2;
		this.rightCell = cell3;
		this.intersection = VectorXZ.getRayIntersection(
				leftEdge.fixedPoint, leftEdge.direction,
				rightEdge.fixedPoint, rightEdge.direction);
	}
	
	public boolean isValid(){
		return (intersection != VectorXZ.NaN && isCounterClockwise());
	}
	
	private boolean isCounterClockwise(){
		float dx1, dx2, dz1, dz2;
		dx1 = middleCell.cell.site.x - leftCell.cell.site.x; dz1 = middleCell.cell.site.z - leftCell.cell.site.z;
		dx2 = rightCell.cell.site.x - leftCell.cell.site.x; dz2 = rightCell.cell.site.z - leftCell.cell.site.z;
		if (dx1*dz2 > dz1*dx2) return true;
		if (dx1*dz2 < dz1*dx2) return false;
		if ((dx1*dx2 < 0) || (dz1*dz2 < 0)) return false;
		return false;
	}

	@Override
	public float getX() {
		return intersection.x;
	}

	@Override
	public float getZ() {
		return intersection.z + (float) intersection.distance(middleCell.cell.site);
	}

	public void setValid(boolean b) {
		this.valid = b;
	}
}
