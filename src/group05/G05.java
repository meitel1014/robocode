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
			setTurnRadarRight(360);
			System.out.println(getName() + ":wait");
			System.out.println(getName() + ":size:" + data.size());
			execute();
		}
		System.out.println(getName() + ":ready");

		while(true){
			recordMe();
			setTurnRadarRight(10000000);
			RobotData target = data.getTarget(this.getName());
			if(target != null && fired == true){
				System.out.println("target:" + target.getName());
				double distance = target.getDistance(getX(), getY());// ターゲットからの距離
				if(distance <= 300){
					power = 3;
				}else if(distance > 300 && distance <= 600){
					power = 2;
				}else{
					power = 1;
				}

				double rTurn = getGunHeadingRadians() +
						getAngleBtwRobos(target.getNextPosition(getX(), getY(), power)) - Math.PI / 2;
				setTurnGunLeftRadians(rTurn);
				fired = false;
			}
			if(getGunHeat() == 0 && power > 0.1 && Math.abs(getGunTurnRemaining()) < 10){
				fire(power);
				power = 0;
				fired = true;
			}
			// 移動
			//TODO 追いつけないので反重力に統一
			if(getMode() == Mode.WALL){
				chestToWall(target);
			}else{
				getDirection();
			}

			execute();
		}
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
		double myx = this.getX();// 自ロボットのｘ座標
		double myy = this.getY();// 自ロボットのｙ座標
		double forcex, forcey;// 各ロボットから受けるｘ、ｙ軸方向の力
		Point2D.Double force;
		forcex = 0;
		forcey = 0;
		// ウォールを倒した後に分岐する
		if(getMode() == Mode.WALL || getMode() == Mode.EVADE){
			/*
			 * ロボットとの反重力
			 */
			for(RobotData info: data.getAll()){
				force = getForce(info.getGravity(), info.getPosition());
				forcex += force.getX();
				forcey += force.getY();
			}

			/*
			 * 壁との反重力
			 */
			forcex += wallpoint / Math.pow(myx, 1.5);
			forcex -= wallpoint / Math.pow(getBattleFieldWidth() - myx, 1.5);
			forcey += wallpoint / Math.pow(myy, 1.5);
			forcey -= wallpoint / Math.pow(getBattleFieldHeight() - myy, 1.5);
			move(forcex, forcey);
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
		double forcex = power * (Math.cos(getAngleBtwRobos(position)));
		double forcey = power * (Math.sin(getAngleBtwRobos(position)));
		return new Point2D.Double(forcex, forcey);
	}

	// 自分から見たenemyの角度を計算する
	public double getAngleBtwRobos(Point2D.Double enemy){
		return Math.atan2(enemy.getY() - getY(), enemy.getX() - getX());
	}

	//TODO 射撃を妨害しない
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
	 * (x,y)の分だけ滑らかに移動する
	 */
	private void attack(double x, double y){
		double mDirection = Math.atan2(y, x);
		int rev = turn(mDirection);
		setAhead(dist * rev);
	}

	/*
	 * mAngleの方向に最短で回転する
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
		out.println(wallPosition);
		Point2D.Double chestPosition = new Point2D.Double();// 追跡のために移動したい座標
		/*
		 * wallが角にいるときに玉を回避する動きをする(移動が遅すぎて間に合わない)
		 */
		if((wallPosition.getX() <= range) && (wallPosition.getY() <= range)){
			move(0, 20);
			out.println("左下");
		}else if((wallPosition.getX() <= range) && (wallPosition.getY() >= (this.getBattleFieldHeight() - range))){
			move(20, 0);
			out.println("左上");
		}else if((wallPosition.getX() >= (this.getBattleFieldWidth() - range))
				&& (wallPosition.getY() >= (this.getBattleFieldHeight() - range))){
			move(0, -20);
			out.println("右上");
		}else if((wallPosition.getX() >= (this.getBattleFieldWidth() - range)) && (wallPosition.getY() <= range)){
			move(-20, 0);
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
		move(chestPosition.getX() - this.getX(), chestPosition.getY() - this.getY());
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
