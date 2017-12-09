package group05;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import robocode.*;

abstract public class G05 extends TeamRobot{
	int dist = 50; // あたったときに逃げる距離
	boolean movingForward; //
	RobotDataList data;
	int wallpoint = 10; // 壁の重力

	public void run(){
		data = new RobotDataList(getTeammates());
		setBodyColor(Color.black);
		setGunColor(Color.blue);
		setRadarColor(Color.yellow);
		setBulletColor(Color.red);
		setScanColor(Color.white);
		// 各ロボットのデータリストを作成 チームメイトのデータをまず登録

		setAdjustGunForRobotTurn(true);

		boolean fired=true;//セットされた射撃が実行された後か
		double power=0;
		while(true){
			setTurnRadarRight(10000000);
			RobotData target = data.getTarget();

			double mTargetAngle;//自分から見たターゲットの角度
			if(target != null && fired==true){
				double distance = Math.sqrt(Math.pow((getX() - target.getPosition().getX()), 2) + Math.pow((getY() +
						target.getPosition().getY()), 2));//ターゲットからの距離
				if(distance <= 300){
					power = 3;
				}else if(distance > 300 && distance <= 600){
					power = 2;
				}else{
					power = 1;
				}

				mTargetAngle = getmAngleBtwRobos(target.getPosition());
				double rTurn = getRoboAngleFromMathAngle(mTargetAngle) - getGunHeadingRadians();//回転する量
				setTurnGunRightRadians(rTurn);
				fired=false;
			}

			if(getGunHeat() == 0 && power> 0.1 && Math.abs(getGunTurnRemaining()) < 2){
				fire(power);
				power = 0;
				fired=true;
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

		if(robo.isTeammate() == false){
			robo.setPosition(getPosition(e.getDistance(), e.getBearingRadians()));

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

	private Point2D.Double getPosition(double distance, double absRoboRadians){
		double x = getX() + distance * Math.cos(absRoboRadians);
		double y = getY() + distance * Math.sin(absRoboRadians);

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
		ahead(-100);
		ahead(100);
	}

	public void onHitWall(HitWallEvent e){
		clearAllEvents();
		setTurnRadarRight(10000000);
		getDirection();
	}

	public void RobotDeathEvent(String robotName){
		data.remove(robotName);
	}

	// 動くべき方向をラジアンで求める
	private void getDirection(){
		double direction;// 反重力法で導かれた移動する向き
		double myx = this.getX();// 自ロボットのｘ座標
		double myy = this.getY();// 自ロボットのｙ座標
		double forcex, forcey;// 各ロボットから受けるｘ、ｙ軸方向の力
		double distance;// 各ロボットとの距離（計算の過程で用いるだけなので値は一時的にしか保持しない）
		double power;// 各ロボットから受ける反発力（上に同じ）
		Point2D.Double posi;// 各ロボットの座標を保存（上に同じ）

		ArrayList<RobotData> list = data.getAll();

		forcex = 0;
		forcey = 0;

		/*
		 * 敵ロボットとの反重力
		 */
		for(RobotData info: list){
			posi = info.getPosition();
			distance = Math.sqrt(Math.pow((myx - posi.getX()), 2) + Math.pow((myy +
					posi.getY()), 2));
			power = info.getDefendPoint() / Math.pow(distance, 2);
			forcex += power * (Math.cos(getmAngleBtwRobos(posi)));
			forcey += power * (Math.sin(getmAngleBtwRobos(posi)));
		}

		/*
		 * 壁との反重力
		 */
		forcex += wallpoint / Math.pow(myx, 1.5);
		forcex -= wallpoint / Math.pow(1000 - myx, 1.5);
		forcey += wallpoint / Math.pow(myy, 1.5);
		forcey -= wallpoint / Math.pow(800 - myy, 1.5);

		move(forcex, forcey);
	}

	private double getmAngleBtwRobos(Point2D.Double enemy){
		double mAngle = Math.atan2(enemy.getY() - getY(), enemy.getX() - getX());
		if(enemy.getX() - getX() < 0){
			mAngle += Math.PI;
		}
		return mAngle;
	}

	void move(double x, double y){
		double distance = 100;
		double mDirection = Math.atan2(y, x);
		if(x < 0){
			mDirection += Math.PI;
		}
		int rev = turnTo(mDirection);
		setAhead(distance * rev);
	}

	int turnTo(double mAngle){
		double rDirection;
		int sign;
		rDirection = getRoboAngleFromMathAngle(mAngle) - getHeadingRadians();
		if(rDirection > Math.PI / 2){
			rDirection -= Math.PI;
			sign = -1;
		}else if(rDirection < -Math.PI / 2){
			rDirection += Math.PI;
			sign = -1;
		}else{
			sign = 1;
		}
		setTurnRightRadians(rDirection);
		return sign;
	}

	/*
	 * 数学角度からrobocodeの角度への変換
	 */
	private double getRoboAngleFromMathAngle(double mRadian){
		double rDirection = -(mRadian - (Math.PI / 2));
		return rDirection;
	}
}
