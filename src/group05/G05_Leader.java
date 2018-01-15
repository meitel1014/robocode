package group05;

public class G05_Leader extends G05{
	@Override
	public void strategy(){
		setTurnRadarRight(10000000);
		RobotData target = data.getTarget(getName());
		System.out.println(target.getName());
		if(target != null && fired == true){
			double distance = target.getDistance(getX(), getY());// ターゲットからの距離
			if(distance <= 300){
				power = 3;
			}else if(distance > 300 && distance <= 600){
				power = 2;
			}else{
				power = 1;
			}
			// ウォールの数が0になったときに分岐
			if(data.walls() > 1){
				chestToWall(target);
			}else{

			}
			double rTurn = getGunHeadingRadians() +
					getAngleBtwRobos(target.getNextPosition(getX(), getY(), power, getHeading())) - Math.PI / 2;
			setTurnGunLeftRadians(rTurn);
			fired = false;
		}
		if(getGunHeat() == 0 && power > 0.1 && Math.abs(getGunTurnRemaining()) < 10){
			fire(power);
			power = 0;
			fired = true;
		}
		if(getDistanceRemaining() < 2 && getTurnRemaining() < 10){
			getDirection();
		}
		execute();
	}
}
