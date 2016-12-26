package com.kylantraynor.voronoi;

public class VSiteEvent extends VEvent {

	public VSite site;
	
	public VSiteEvent(VSite site) {
		this.site = site;
	}

	@Override
	public float getX() {
		return site.x;
	}

	@Override
	public float getZ() {
		return site.z;
	}
}
