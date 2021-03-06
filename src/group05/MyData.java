package group05;

import java.io.Serializable;

public class MyData implements Serializable{
	private static final long serialVersionUID = 546546601L;
	private String name;
	private double x, y, energy, velocity, rHeading;
	private long time;

	public MyData(String name, double x, double y, double energy, double velocity, double rHeading, long time){
		this.name = name;
		this.x = x;
		this.y = y;
		this.energy = energy;
		this.velocity = velocity;
		this.rHeading = rHeading;
		this.time = time;
	}

	public String getName(){
		return name;
	}

	public double getX(){
		return x;
	}

	public double getY(){
		return y;
	}

	public double getEnergy(){
		return energy;
	}

	public double getVelocity(){
		return velocity;
	}

	public double getHeadingRadians(){
		return rHeading;
	}

	public long getTime(){
		return time;
	}
}
