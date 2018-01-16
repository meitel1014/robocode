package group05;

import java.io.IOException;

import robocode.ScannedRobotEvent;

public final class G05_Leader extends G05{
	@Override
	public Mode getMode(){
		if(data.walls() == 0&&data.isDroidDead()) {
			return Mode.RAMFIRE;
		}else{
			return Mode.WALL;
		}
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e){
		try{
			broadcastMessage(e);
		}catch(IOException e1){
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
	}
}
