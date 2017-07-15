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
	
	@Override
	public boolean equals(Object o){
		if(o instanceof VSite){
			if(o.hashCode() == this.hashCode())
				if(x == ((VSite)o).x && z == ((VSite)o).z && weight == ((VSite)o).weight){
					return true;
				}
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		int hash = 13;
		hash = hash * Float.hashCode(x);
		hash = hash * Float.hashCode(z);
		hash = hash * Float.hashCode(weight);
		return hash;
	}
}
