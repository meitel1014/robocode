package group05;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.Serializable;

import robocode.*;

abstract public class G05 extends TeamRobot{
	final int dist = 100; // 一度に移動する距離
	RobotDataList data;
	final int wallpoint = 2; // 壁の重力
	boolean fired = true;// セットされた射撃が実行された後か
	double power = 0;
	int movSign = 1;
	double rturnRadians = 0;
	boolean turnCompleted = true, moveCompleted = false;

	enum Mode{
		WALL, RAMFIRE, EVADE
	};

	abstract public Mode getMode();

	public void run(){
		setBodyColor(Color.pink);
		setGunColor(Color.blue);
		setRadarColor(Color.yellow);
		setBulletColor(Color.red);
		setScanColor(Color.white);
		setAdjustGunForRobotTurn(true);
		// ロボットのデータリストを取得し自分のデータをまず登録
		data = new RobotDataList(getName());

		while(!data.isReady()){
			setTurnRadarRight(400);
			execute();
		}

		while(true){
			recordMe();
			setTurnRadarRight(10000000);
			RobotData target = data.getTarget(this.getName());
			double rTurn = getrAngleBtwRobos(target.getNextPosition(getX(), getY(), power))
					- getGunHeadingRadians();
			turnGun(rTurn);
			if(target != null && fired == true && getGunHeat() == 0){
				System.out.println("target:" + target.getName());
				double distance = target.getDistance(getX(), getY());// ターゲットからの距離
				if(distance <= 300){
					power = 3;
				}else if(distance > 300 && distance <= 600){
					power = 2;
				}else{
					power = 1;
				}
				fired = false;
			}
			if(power > 0.1 && Math.abs(getGunTurnRemaining()) < 10){
				fire(power);
				power = 0;
				fired = true;
			}
			// 移動
			getDirection();

			execute();
		}
	}

	// rTurnRadians右回転した先に最短で大砲を回転する
	public void turnGun(double rTurnRadians){
		// rTurnRadiansを-piからpiの間にする
		while(rTurnRadians > Math.PI){
			rTurnRadians -= 2 * Math.PI;
		}
		while(rTurnRadians < -Math.PI){
			rTurnRadians += 2 * Math.PI;
		}

		setTurnGunRightRadians(rTurnRadians);
		execute();

	}

	public void recordMe(){
		RobotData me = data.get(this.getName());
		me.setPosition(this.getX(), this.getY());
		me.setEnergy(this.getEnergy());
		me.setVelocity(this.getVelocity());
		me.setrHeading(this.getHeadingRadians());
		try{
			broadcastMessage(new MyData(getName(), getX(), getY(), getEnergy(), getVelocity(), getHeadingRadians()));
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public void onScannedRobot(ScannedRobotEvent e){
		RobotData robo = data.get(e.getName());
		robo.setPosition(getPosition(e.getDistance(), e.getBearingRadians()));
		robo.setEnergy(e.getEnergy());
		robo.setVelocity(e.getVelocity());
		robo.setrHeading(e.getBearingRadians() + this.getHeadingRadians());
		try{
			broadcastMessage(new MyData(robo.getName(), robo.getPosition().getX(), robo.getPosition().getY(),
					robo.getEnergy(), robo.getVelocity(), robo.getrHeading()));
		}catch(IOException ex){
			ex.printStackTrace();
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

	public void onHitByBullet(HitByBulletEvent e){}

	public void onHitRobot(HitRobotEvent e){
		RobotData robo = data.get(e.getName());
		robo.setEnergy(e.getEnergy());
		robo.setrHeading(e.getBearingRadians() + this.getHeadingRadians());
		if(getMode() == Mode.WALL){
			escape(e.getBearingRadians());
		}
	}

	public void onHitWall(HitWallEvent e){
		escape(e.getBearingRadians());
	}

	/*
	 * 衝突したロボットや壁と反対方向に逃げる
	 */
	private void escape(double rAngle){
		System.out.println("escape");
		if(Math.abs(rAngle) <= Math.PI / 2){// 衝突先が前方にある
			ahead(-dist / 2);
		}else{// 衝突先が後方にある
			ahead(dist / 2);
		}
	}

	public void onRobotDeath(RobotDeathEvent e){
		data.remove(e.getName());
	}

	public void onMessageReceived(MessageEvent event){
		Serializable m = event.getMessage();
		if(m instanceof HitRobotEvent){
			HitRobotEvent e = (HitRobotEvent)m;
			RobotData robo = data.get(e.getName());
			robo.setEnergy(e.getEnergy());
			robo.setrHeading(e.getBearingRadians() + data.get(event.getSender()).getrHeading());
		}else if(m instanceof MyData){
			MyData sig = (MyData)m;
			RobotData robo = data.get(sig.getName());
			robo.setPosition(sig.getX(), sig.getY());
			robo.setEnergy(sig.getEnergy());
			robo.setVelocity(sig.getVelocity());
			robo.setrHeading(sig.getHeadingRadians());
		}
	}

	protected void getDirection(){
		double forcex = 0, forcey = 0;// 各ロボットから受けるｘ、ｙ軸方向の力
		Point2D.Double force;
		// ウォールを倒した後に分岐する
		if(getMode() == Mode.WALL || getMode() == Mode.EVADE){
			/*
			 * ロボットとの反重力
			 */
			for(RobotData info: data.getAll(getName())){
				force = getForce(info.getGravity(), info.getPosition());
				forcex += force.getX();
				forcey += force.getY();
			}

			/*
			 * 壁との反重力
			 */
			forcex += wallpoint / Math.pow(getX(), 1.5);
			forcex -= wallpoint / Math.pow(getBattleFieldWidth() - getX(), 1.5);
			forcey += wallpoint / Math.pow(getY(), 1.5);
			forcey -= wallpoint / Math.pow(getBattleFieldHeight() - getY(), 1.5);
			if(getDistanceRemaining() == 0 && turnCompleted){
				moveCompleted = true;
				getMove(forcex, forcey);
				setTurnRightRadians(rturnRadians);
				turnCompleted = false;
			}
			if(getTurnRemainingRadians() == 0 && moveCompleted){
				turnCompleted = true;
				setAhead(dist * movSign);
				moveCompleted = false;
			}
		}else{
			// どの戦車を狙うかを決めている。残り体力の最も少ない戦車を攻撃する。
			RobotData target = data.getTarget(this.getName());

			force = getForce(target.getGravity(), target.getPosition());
			forcex += force.getX();
			forcey += force.getY();
			attack(-forcex, -forcey);
		}
	}

	private Point2D.Double getForce(double point, Point2D.Double position){
		double distance = Math.sqrt(Math.pow((getX() - position.getX()), 2) + Math.pow((getY() -
				position.getY()), 2));
		double power = -point / Math.pow(distance, 2);
		double forcex = power * (Math.cos(getmAngleBtwRobos(position)));
		double forcey = power * (Math.sin(getmAngleBtwRobos(position)));
		return new Point2D.Double(forcex, forcey);
	}

	// 自分から見たenemyの数学角度を計算する
	public double getmAngleBtwRobos(Point2D.Double enemy){
		return Math.atan2(enemy.getY() - getY(), enemy.getX() - getX());
	}

	// 自分から見たenemyのRobocode角度を計算する
	public double getrAngleBtwRobos(Point2D.Double enemy){
		return torAngle(getmAngleBtwRobos(enemy));
	}

	/*
	 * (x,y)の分だけ移動させる
	 */
	private void getMove(double x, double y){
		double mDirection = Math.atan2(y, x);
		movSign = turnTo(mDirection);
	}

	/*
	 * mAngleの方向に最短で回転させ前進か後進かを返す
	 */
	private int turnTo(double mAngle){
		int sign;
		rturnRadians = tomAngle(mAngle) - getHeadingRadians();
		if(rturnRadians > Math.PI / 2){
			rturnRadians -= Math.PI;
			sign = -1;
		}else if(rturnRadians < -Math.PI / 2){
			rturnRadians += Math.PI;
			sign = -1;
		}else{
			sign = 1;
		}

		return sign;
	}

	/*
	 * (x,y)の分だけ滑らかに移動する
	 */
	private void attack(double x, double y){
		double mDirection = Math.atan2(y, x);
		int rev = turn(mDirection);
		setAhead(dist * rev);
	}

	/*
	 * mAngleの方向に最短で回転し前進か後進かを返す
	 */
	private int turn(double mAngle){
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
		setTurnRightRadians(rDirection);
		return sign;
	}

	/*
	 * 受け取ったロボット(wall)を追跡する
	 */
	protected void chestToWall(RobotData chest){
		// ターゲットを決定できないまま呼び出された時の対処
		if(chest == null){
			getDirection();
			return;
		}

		int keepDistance = 25;// wallと保つ距離(移動が間に合わないのであまり関係ない?)
		int range = 40;// wallの位置を分けるときの幅
		Point2D.Double wallPosition = chest.getPosition();
		Point2D.Double chestPosition = new Point2D.Double();// 追跡のために移動したい座標
		/*
		 * wallが角にいるときに玉を回避する動きをする(移動が遅すぎて間に合わない)
		 */
		if((wallPosition.getX() <= range) && (wallPosition.getY() <= range)){
			getMove(0, 20);
			out.println("左下");
		}else if((wallPosition.getX() <= range) && (wallPosition.getY() >= (this.getBattleFieldHeight() - range))){
			getMove(20, 0);
			out.println("左上");
		}else if((wallPosition.getX() >= (this.getBattleFieldWidth() - range))
				&& (wallPosition.getY() >= (this.getBattleFieldHeight() - range))){
			getMove(0, -20);
			out.println("右上");
		}else if((wallPosition.getX() >= (this.getBattleFieldWidth() - range)) && (wallPosition.getY() <= range)){
			getMove(-20, 0);
			out.println("右下");
		}
		/*
		 * wallの位置によってchestPositionを設定
		 */
		if((wallPosition.getX() <= range) && (wallPosition.getY() <= (this.getBattleFieldHeight() - range))){
			chestPosition.setLocation(wallPosition.getX() + keepDistance * Math.cos(Math.toRadians(60)),
					wallPosition.getY() + keepDistance * Math.sin(Math.toRadians(60)));
		}else if((wallPosition.getX() >= range) && (wallPosition.getY() <= range)){
			chestPosition.setLocation(wallPosition.getX() + keepDistance * Math.cos(Math.toRadians(150)),
					wallPosition.getY() + keepDistance * Math.sin(Math.toRadians(150)));
		}else if((wallPosition.getX() >= (this.getBattleFieldWidth() - range)) && (range <= wallPosition.getY())){
			chestPosition.setLocation(wallPosition.getX() + keepDistance * Math.cos(Math.toRadians(-120)),
					wallPosition.getY() + keepDistance * Math.sin(Math.toRadians(-120)));
		}else if((wallPosition.getX() <= (this.getBattleFieldWidth() - range))
				&& (wallPosition.getY() >= (this.getBattleFieldHeight() - range))){
			chestPosition.setLocation(wallPosition.getX() + keepDistance * Math.cos(Math.toRadians(-30)),
					wallPosition.getY() + keepDistance * Math.sin(Math.toRadians(-30)));
		}
		out.println("X:" + chestPosition.getX() + "Y:" + chestPosition.getY());
		getMove(chestPosition.getX() - this.getX(), chestPosition.getY() - this.getY());
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
