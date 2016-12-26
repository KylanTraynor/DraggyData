package com.kylantraynor.voronoi;

public class VSite extends VectorXZ{

	public float weight;
	public int id;

	public VSite(float x, float z, float weight) {
		super(x, z);
		this.weight = weight;
	}
	
	public double weightedDistance(VectorXZ v){
		return Math.sqrt( (x-v.x)*(x-v.x) + (z-v.z)*(z-v.z) ) * weight;
	}
}
