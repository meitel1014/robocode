package group05;

import robocode.Droid;

abstract public class G05_Sub extends G05 implements Droid{
	@Override
	public Mode getMode(){
		if(data.walls() <= 1){
			return Mode.RAMFIRE;
		}else{
			return Mode.WALL;
		}
	}
}
