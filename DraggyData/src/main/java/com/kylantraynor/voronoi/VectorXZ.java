package com.kylantraynor.voronoi;

public class VectorXZ {
	final static public VectorXZ NegativeInfinite = new VectorXZ(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
	final static public VectorXZ PositiveInfinite = new VectorXZ(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
	final static public VectorXZ NaN = new VectorXZ(Float.NaN, Float.NaN);
	
	final public float x;
	final public float z;
	private float length;
	
	public VectorXZ(float x, float z){
		this.x = x;
		this.z = z;
		this.length = (float) Math.sqrt(x*x + z*z);
	}
	
	public double distance(VectorXZ v){
		return Math.sqrt( (x-v.x)*(x-v.x) + (z-v.z)*(z-v.z) );
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof VectorXZ){
			if(o.hashCode() == this.hashCode())
				if(x == ((VectorXZ)o).x && z == ((VectorXZ)o).z){
					return true;
				}
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		int hash = 17;
		hash = hash * Float.hashCode(x);
		hash = hash * Float.hashCode(z);
		return hash;
	}
	
	public float length(){
		return length;
	}

	public VectorXZ normalize() {
		return new VectorXZ(x / length(), z / length());
	}
	
	public VectorXZ flip(){
		return new VectorXZ(-x, -z);
	}
	
	public static VectorXZ getRayIntersection(VectorXZ p1, VectorXZ d1, VectorXZ p2, VectorXZ d2){
		//System.out.printf("\nCalculating Ray Intersection between d1("+d1.x+","+d1.z+")p1("+p1.x+","+p1.z+") and d2("+d2.x+","+d2.z+")p2("+p2.x+","+p2.z+").");
		//r1(t1) = p1 + t1 * d1
		//r2(t2) = p2 + t2 * d2
		
		// p1 + t1 * d1 = p2 + t2 * d2
		// p1 - p2 = -t1*d1 + t2*d2
		// p1 - p2 = t2*d2 - t1*d1
		
		// let's set c = p1 - p2
		// c = t2*d2 - t1*d1
		// (1) cx = t2*d2x - t1*d1x
		// (2) cz = t2*d2z - t1*d1z
		
		// Let's solve (1)
		// -t2*d2x = -cx - t1*d1x
		// t2 = (cx + t1*d1x) / d2x
		
		// Let's solve (2)
		// -t2*d2z = -cz - t1*d1z
		// t2 = (cz + t1*d1z) / d2z
		
		// Let's solve (2) with (1)
		// cz = ((cx + t1*d1x) / d2x)*d2z - t1*d1z
		// cz = ((cx/d2x) + t1*(d1x/d2x)) * d2z - t1*d1z
		// cz = cx(d2z/d2x) + t1*((d1x*d2z)/d2x) - t1*d1z
		// cz = cx(d2z/d2x) + t1*((d1x*d2z)/d2x - d1z)
		// -t1 = (cx(d2z/d2x) - cz)/((d1x*d2z)/d2x - d1z)
		// t1 = (cz - cx(d2z/d2x))/((d1x*d2z)/d2x - d1z)
		// (Solution 1) t1 = (cz - cx*d2z/d2x)/(d1x*d2z/d2x - d1z)
		// can only use this if d2x != 0
		// and if d1x*d2z/d2x != d1z (parallels)
		
		// Let's solve (1) with (2)
		// cx = ((cz + t1*d1z) / d2z) * d2x - t1*d1x
		// cx = ((cz/d2z + t1*(d1z/d2z)) * d2x - t1*d1x
		// cx = cz(d2x/d2z) + t1(d1z*d2x/d2z) - t1*d1x
		// cx = cz(d2x/d2z) + t1(d1z*d2x/d2z - d1x)
		// cx - cz(d2x/d2z) = t1(d1z*d2x/d2z - d1x)
		// (Solution 2) t1 = (cx - cz(d2x/d2z))/(d1z*d2x/d2z - d1x)
		// can only use this if d2z != 0
		// and if d1z*d2x/d2z != d1x (parallels)
		
		float t1 = Float.NaN;
		if(d2.z != 0){
			float denom = (d1.z * d2.x / d2.z) - d1.x;
			if(denom == 0){
				return VectorXZ.NaN;
			} else {
				t1 = ((p1.x - p2.x) - (p1.z - p2.z) * (d2.x/d2.z)) / denom;
			}
		} else if(d2.x != 0) {
			float denom = (d1.x * d2.z / d2.x) - d1.z;
			if(denom == 0){
				return VectorXZ.NaN;
			} else {
				t1 = ((p1.z - p2.z) - (p1.x - p2.x) * (d2.z/d2.x)) / denom;
			}
		} else {
			return VectorXZ.NaN;
		}
		//System.out.printf("\nResult of ray intersection: p("+(p1.x + t1 * d1.x)+","+(p1.z + t1 * d1.z)+").");
		return new VectorXZ(p1.x + t1 * d1.x, p1.z + t1 * d1.z);
	}

	public float dotProduct(VectorXZ b){
		return this.x * b.x + this.z * b.z;
	}
	
	public float getAngle(VectorXZ b) {
		if(length == 0 || b.length() == 0) return Float.NaN;
		float dot = dotProduct(b);
		float cos = dot / (length * b.length());
		return (float) Math.acos(cos);
	}
	
	public float getCheapAngle(VectorXZ o){
		float dz = z - o.z;
		float dx = x - o.x;
		float m = dz / (dx + dz);
		float n = dx / (dx - dz);
		
		if(dz >= 0) return (dx >= 0 ? m : 1 + n);
		else             return (dx < 0 ? 2 + m : 3 + n);
	}
	
	public float getCheapAngle(){
		if(x == -z && x == z) System.out.printf("Division by 0!");
		float m = z / (x + z);
        float n = x / (x - z);

        if (z >= 0) return (x >= 0 ? m : 1 + n);
        else        return (x < 0 ? 2 + m : 3 + n);
	}
	
	public float getRelativeAngle(VectorXZ b){
		if(dotProduct(b) == 0) return 0;
		float m = dotProduct(b) < 0 ? -1 : 1; 
		return m * getAngle(b);
	}
	
	public VectorXZ getOrthogonal(){
		return new VectorXZ(z, -x);
	}
	
	public boolean isInfinite(){
		if(x == Float.POSITIVE_INFINITY || x == Float.NEGATIVE_INFINITY) return true;
		if(z == Float.POSITIVE_INFINITY || z == Float.NEGATIVE_INFINITY) return true;
		return false;
	}

	public boolean isNaN() {
		if(Float.isNaN(x)) return true;
		if(Float.isNaN(z)) return true;
		return false;
	}
	
	public int getClipCode(float minX, float minZ, float maxX, float maxZ){
		int code;
		code = 0;
		if(x < minX)
			code |= 1;
		else if(x > maxX)
			code |= 2;
		if(z < minZ)
			code |= 4;
		else if(z > maxZ)
			code |= 8;
		return code;
	}
	
	public VectorXZ substract(VectorXZ v){
		float x1 = this.x - v.x;
		float z1 = this.z - v.z;
		return new VectorXZ(x1, z1);
	}
	
	public float getX(){
		return x;
	}
	
	public float getZ(){
		return z;
	}
}