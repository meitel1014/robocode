package group05;

import java.awt.Color;
import java.awt.geom.Point2D;

import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;

abstract public class G05 extends TeamRobot{
	final int dist = 100; // 一度に移動する距離
	RobotDataList data;
	final int wallpoint = 2; // 壁の重力

	public void run(){
		setBodyColor(Color.pink);
		setGunColor(Color.blue);
		setRadarColor(Color.yellow);
		setBulletColor(Color.red);
		setScanColor(Color.white);
		// 各ロボットのデータリストを作成 自分のデータをまず登録
		data = RobotDataList.getInstance();
		data.setMe(getName());

		setAdjustGunForRobotTurn(true);

		boolean fired = true;// セットされた射撃が実行された後か
		double power = 0;
		while(true){
			setTurnRadarRight(10000000);
			RobotData target = data.getTarget();

			if(target != null && fired == true){
				double distance = target.getDistance(getX(), getY());// ターゲットからの距離
				if(distance <= 300){
					power = 3;
				}else if(distance > 300 && distance <= 600){
					power = 2;
				}else{
					power = 1;
				}

				//ウォールの数が0になったときに分岐
				if(data.walls() != 0) {
					double rTurn = getGunHeadingRadians() + getAngleBtwRobos(target.getPosition()) - Math.PI / 2;
					setTurnGunLeftRadians(rTurn);
					fired = false;
				}else {
						//大砲の向きを戦車の向きと同じにしている。
						//角度が0から2πになるようにif文で分岐した。

						if(this.getGunHeadingRadians() - this.getHeadingRadians() > 0) {
							double rTurn = getGunHeadingRadians() - getHeadingRadians();
							setTurnGunLeftRadians(rTurn);
							fired = false;
						}else {
							double rTurn = getHeadingRadians() - getGunHeadingRadians();
							setTurnGunLeftRadians(rTurn);
							fired = false;
						}
				}
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

	// スキャンしたときにこちらを向いているときだけ防御ポイントを1上げる
	public void onScannedRobot(ScannedRobotEvent e){
		RobotData robo = data.get(e.getName());
		robo.setPosition(getPosition(e.getDistance(), e.getBearingRadians()));
		robo.setEnergy(e.getEnergy());

		if(robo.isTeammate() == false){
			System.out.println(e.getName() + getPosition(e.getDistance(), e.getBearingRadians()));

			if(e.getBearing() > 160 || e.getBearing() < -160){
				robo.setDirectionDefendpoint(1);
				robo.setDirectionAttackPoint(2);
			}else{
				robo.setDirectionAttackPoint(1);
				robo.setDirectionDefendpoint(0);
			}
			if(e.getDistance() <= 300){
				robo.setDistanceAttackPoint(3);
			}else if(e.getDistance() > 300 && e.getDistance() <= 600){
				robo.setDistanceAttackPoint(2);
			}else{
				robo.setDistanceAttackPoint(1);
			}
		}
	}

	// ロボットとの距離と角度からそのロボットの座標を計算する
	private Point2D.Double getPosition(double distance, double rrelRoboRadians){
		double rabsRoboRadians = rrelRoboRadians + getHeadingRadians();
		double mabsRoboRadians = torAngle(rabsRoboRadians);
		double x = getX() + distance * Math.cos(mabsRoboRadians);
		double y = getY() + distance * Math.sin(mabsRoboRadians);

		return new Point2D.Double(x, y);
	}

	// 敵の弾に当たった時に防御ポイントを1上げる
	public void onHitByBullet(HitByBulletEvent e){
		RobotData robo = data.get(e.getName());
		if(robo.isTeammate() == false){
			robo.addBulletDefendpoint(1);// 攻撃をしてきた相手の防御ポイントを1上げる
		}
	}

	public void onHitRobot(HitRobotEvent e){
		RobotData robo = data.get(e.getName());
		robo.setEnergy(e.getEnergy());
		escape(e.getBearingRadians());
	}

	public void onHitWall(HitWallEvent e){
		escape(e.getBearingRadians());
	}

	/*
	 * 衝突したロボットや壁と反対方向に逃げる
	 */
	private void escape(double rAngle){
		System.out.println("escape");
		clearAllEvents();
		setTurnRadarRight(1000);
		if(Math.abs(rAngle) < Math.PI / 2){// 衝突先が前方にある
			ahead(-dist);
		}else{// 衝突先が後方にある
			ahead(dist);
		}
	}

	public void onRobotDeath(RobotDeathEvent e){
		data.remove(e.getName());
	}

	private void getDirection(){
		double myx = this.getX();// 自ロボットのｘ座標
		double myy = this.getY();// 自ロボットのｙ座標
		double forcex, forcey;// 各ロボットから受けるｘ、ｙ軸方向の力
		Point2D.Double force;

		forcex = 0;
		forcey = 0;

		//ウォールを倒した後に分岐する

		if(data.walls() != 0) {

			/*
			 * 敵ロボットとの反重力
			 */
			for(RobotData info: data.getAll()){
				System.out.println(info.getName() + "defence:" + info.getDefendPoint());
				System.out.println("posi:" + info.getPosition());
				force = getForce(info.getDefendPoint(), info.getPosition());
				forcex += force.getX();
				forcey += force.getY();
			}

			force = getForce(data.getTotalDefendPoint(),
					new Point2D.Double(getBattleFieldWidth() / 2, getBattleFieldHeight() / 2));
			forcex += force.getX();
			forcey += force.getY();

			/*
			 * 壁との反重力
			 */
			forcex += wallpoint / Math.pow(myx, 1.5);
			forcex -= wallpoint / Math.pow(getBattleFieldWidth() - myx, 1.5);
			forcey += wallpoint / Math.pow(myy, 1.5);
			forcey -= wallpoint / Math.pow(getBattleFieldHeight() - myy, 1.5);

			System.out.println("forcex:" + forcex);
			System.out.println("forcey:" + forcey);
			move(forcex, forcey);

		}else {
			//どの戦車を狙うかを決めている。残り体力の最も少ない戦車を攻撃する。
			RobotData target = null;

			for(RobotData info: data.getAll()){
				if(info.getEnergy() < target.getEnergy() || target == null)
					target = info;
			}
			//防御ポイントは使用していない
			force = getForce(target.getDefendPoint(), target.getPosition());
			forcex += force.getX();
			forcey += force.getY();

			System.out.println("forcex:" + forcex);
			System.out.println("forcey:" + forcey);
			move(-forcex, -forcey);
		}
	}

	private Point2D.Double getForce(double point, Point2D.Double position){
		double distance = Math.sqrt(Math.pow((getX() - position.getX()), 2) + Math.pow((getY() -
				position.getY()), 2));
		double power = -point / Math.pow(distance, 2);
		double forcex = power * (Math.cos(getAngleBtwRobos(position)));
		double forcey = power * (Math.sin(getAngleBtwRobos(position)));
		double angle = Math.atan2(forcey, forcex);
		System.out.println("angle:" + Math.toDegrees(angle));
		System.out.println("x:" + forcex + "y:" + forcey);
		return new Point2D.Double(forcex, forcey);
	}

	// 自分から見たenemyの角度を計算する
	private double getAngleBtwRobos(Point2D.Double enemy){
		return Math.atan2(enemy.getY() - getY(), enemy.getX() - getX());
	}

	/*
	 * (x,y)の分だけ移動する
	 */
	private void move(double x, double y){
		double mDirection = Math.atan2(y, x);

		int rev = turnTo(mDirection);
		ahead(dist * rev);
	}

	/*
	 * mAngleの方向に最短で回転する
	 */
	private int turnTo(double mAngle){
		double rDirection;
		int sign;
		rDirection = tomAngle(mAngle) - getHeadingRadians();
		if(rDirection > Math.PI / 2){
			rDirection -= Math.PI;
			sign = -1;
		}else if(rDirection < -Math.PI / 2){
			rDirection += Math.PI;
			sign = -1;
		}else{
			sign = 1;
		}
		turnRightRadians(rDirection);
		return sign;
	}

	/*
	 * robocodeの角度から数学角度への変換
	 */
	public static double tomAngle(double rRadian){
		return -rRadian + Math.PI / 2;
	}

	/*
	 * 数学角度からrobocodeの角度への変換
	 */
	public static double torAngle(double mRadian){
		return -(mRadian - (Math.PI / 2));
	}
}
