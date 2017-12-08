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
	int wallpoint = 2; //壁の重力

	public void run() {
		data = new RobotDataList(getTeammates());
		setBodyColor(Color.black);
		setGunColor(Color.blue);
		setRadarColor(Color.yellow);
		setBulletColor(Color.red);
		setScanColor(Color.white);
		// 各ロボットのデータリストを作成 チームメイトのデータをまず登録


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
			if (e.getBearing() > 160 || e.getBearing() < -160) {
				robo.setDirectionDefendpoint(1);
				robo.setDirectionAttackPoint(2);
			} else {
				robo.setDirectionAttackPoint(1);
				robo.setDirectionDefendpoint(0);
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

		/*
		 * 敵ロボットとの反重力
		 */
		for(RobotData info : list) {
			posi = info.getPosition();
			distance = Math.sqrt(Math.pow((myx - posi.getX()), 2) + Math.pow((myy + posi.getY()), 2));
			power = info.getDefendPoint() / distance;
			forcex += power * ((myx - posi.getX()) / distance);
			forcey += power * ((myy - posi.getY()) / distance);
		}

		/*
		 * 壁との反重力
		 */
		forcex += wallpoint / myx;
		forcex += wallpoint / (1000 - myx);
		forcey += wallpoint / myy;
		forcey += wallpoint / (800 - myy);

		/*
		 * ベクトルから方向への変換
		 */
		if((forcex != 0) || (forcey != 0)) {
			if(forcey >=0) {
				direction = Math.acos(forcex/Math.sqrt(forcex*forcex+forcey*forcey));
			}else {
				direction = Math.PI+Math.acos(forcex/Math.sqrt(forcex*forcex+forcey*forcey));
			}
		}else {
			direction = 0;
		}
		return direction;
	}
}
