package me.CookieLuck;

public class Spawn {
	double x;
	double y;
	double z;

	Spawn(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public String toString() {
		return "x: "+x+", y: "+y+", z: "+z+"\n";
	}
}
