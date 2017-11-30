package group05;

import java.awt.Color;

import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;

public class G05 extends TeamRobot{
	int dist = 50; // あたったときに逃げる距離
	boolean movingForward; //
	RobotDataList data;

	public void run(){
		// 機体のデザイン
		setBodyColor(Color.pink);
		setGunColor(Color.blue);
		setRadarColor(Color.yellow);
		setBulletColor(Color.red);
		setScanColor(Color.white);
		// 各ロボットのデータリストを作成 チームメイトのデータをまず登録
		data = new RobotDataList(getTeammates());

		while(true){
			// setAhead(100);
			// setTurnRight(90);
			// setTurnGunRight(180);
			// execute();
		}
	}

	// ロボットをスキャンした時
	public void onScannedRobot(ScannedRobotEvent e){
		// // 敵が近くにいて自分の体力が多い時
		// if (e.getDistance() < 50 && getEnergy() > 50) {
		// fire(3);
		// } // otherwise, fire 1.
		// else {
		// fire(1);
		// }
		// // Call scan again, before we turn the gun
		// scan();
	}

	// 敵の弾に当たった時
	public void onHitByBullet(HitByBulletEvent e){
		// turnRight(normalRelativeAngleDegrees(90 - (getHeading() -
		// e.getHeading())));
		//
		// ahead(dist);
		// dist *= -1;
		// scan();
	}

	// 敵にぶつかった時
	public void onHitRobot(HitRobotEvent e){
		// double turnGunAmt = normalRelativeAngleDegrees(e.getBearing() +
		// getHeading() - getGunHeading());
		//
		// turnGunRight(turnGunAmt);
		// fire(3);
	}
}
