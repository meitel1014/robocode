package group05;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;

public class G05 extends TeamRobot {
	int dist = 50; // あたったときに逃げる距離
	boolean movingForward; //
	RobotDataList data;

	public void run() {
		// 機体のデザイン
		setBodyColor(Color.pink);
		setGunColor(Color.blue);
		setRadarColor(Color.yellow);
		setBulletColor(Color.red);
		setScanColor(Color.white);
		// 各ロボットのデータリストを作成 チームメイトのデータをまず登録
		data = new RobotDataList(getTeammates());

		while (true) {
			setAhead(100);
			double direction = this.getDirection() - this.getHeadingRadians();
			setTurnRightRadians(direction);
			// setTurnGunRight(180);
			execute();
		}
	}

	//スキャンしたときにこちらを向いているときだけ防御ポイントを1上げる
	public void onScannedRobot(ScannedRobotEvent e) {
		RobotData robo = data.get(e.getName());
		if (robo.isTeammate() == false) {
			int count = 0;//2回目以降のスキャンでもカウントすることが無いようにしたいが、毎回定義されてそう
			if (e.getBearing() > 160 || e.getBearing() < -160) {
				robo.setDirectionDefendpoint(1);
				robo.setDirectionAttackPoint(2);
				count = 1;
			} else {
				if (count == 1) {
					robo.setDirectionAttackPoint(1);
					robo.setDirectionDefendpoint(0);
					count = 0;
				}
			}
			if(e.getDistance() <= 300) {
				robo.setDistanceAttackPoint(3);
			}
			else if(e.getDistance() > 300 && e.getDistance() <= 600) {
				robo.setDistanceAttackPoint(2);
			}
			else {
				robo.setDistanceAttackPoint(1);
			}
		}
	}

	//敵の弾に当たった時に防御ポイントを1上げる
	public void onHitByBullet(HitByBulletEvent e) {
		RobotData robo = data.get(e.getName());
		if (robo.isTeammate() == false)
			robo.addBulletDefendpoint(1);//攻撃をしてきた相手の防御ポイントを1上げる
	}

	public void onHitRobot(HitRobotEvent e) {
		// double turnGunAmt = normalRelativeAngleDegrees(e.getBearing() +
		// getHeading() - getGunHeading());
		//
		// turnGunRight(turnGunAmt);
		// fire(3);
	}

	//動くべき方向をラジアンで返す
	private double getDirection() {
		double direction;//反重力法で導かれた移動する向き
		double myx = this.getX();//自ロボットのｘ座標
		double myy = this.getY();//自ロボットのｙ座標
		double forcex, forcey;//各ロボットから受けるｘ、ｙ軸方向の力
		double distance;//各ロボットとの距離（計算の過程で用いるだけなので値は一時的にしか保持しない）
		double power;//各ロボットから受ける反発力（上に同じ）
		Point2D.Double posi;//各ロボットの座標を保存（上に同じ）

		ArrayList<RobotData> list = data.getAll();

		forcex = 0;
		forcey = 0;

		for (RobotData info : list) {
			posi = info.getPosition();
			distance = Math.sqrt(Math.pow((myx - posi.getX()), 2) + Math.pow((myy + posi.getY()), 2));
			power = info.getDefendPoint() / distance;
			forcex += power * ((myx - posi.getX()) / distance);
			forcey += power * ((myy - posi.getY()) / distance);
		}
		direction = Math.acos(forcex / Math.sqrt(forcex * forcex + forcey * forcey));
		return direction;
	}
}
