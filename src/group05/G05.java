package group05;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.Serializable;

import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.MessageEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;
import robocode.WinEvent;

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
			if(target != null){
				double distance = target.getDistance(getX(), getY());// ターゲットからの距離
				power = getPower(distance);
				double rTurn = getrAngleBtwRobos(target.getNextPosition(getX(), getY(), power, getTime()))
						- getGunHeadingRadians() - getGunTurnRemainingRadians();
				setTurnGunRightRadians(normalize(rTurn));
				execute();
				if(Math.abs(getGunTurnRemaining()) < 10 && getGunHeat() == 0){
					fire(power);
				}
			}
			// 移動
			getDirection();
			execute();
		}
	}

	public void recordMe(){
		RobotData me = data.get(this.getName());
		me.setPosition(this.getX(), this.getY());
		me.setEnergy(this.getEnergy());
		me.setVelocity(this.getVelocity());
		me.setrHeading(this.getHeadingRadians());
		me.setTime(getTime());
		try{
			broadcastMessage(
					new MyData(getName(), getX(), getY(), getEnergy(), getVelocity(), getHeadingRadians(), getTime()));
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public void onScannedRobot(ScannedRobotEvent e){
		RobotData robo = data.get(e.getName());
		robo.setPosition(getPosition(e.getDistance(), e.getBearingRadians()));
		robo.setEnergy(e.getEnergy());
		robo.setVelocity(e.getVelocity());
		robo.setrHeading(e.getHeadingRadians());
		robo.setTime(getTime());
		try{
			broadcastMessage(new MyData(robo.getName(), robo.getPosition().getX(), robo.getPosition().getY(),
					robo.getEnergy(), robo.getVelocity(), robo.getrHeading(), getTime()));
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
		robo.setPosition(getHitRobotPosition(e.getBearingRadians()));
		if(!e.isMyFault()){
			robo.setVelocity(0);
		}
		try{
			broadcastMessage(e);
		}catch(IOException e1){
			e1.printStackTrace();
		}
		if(getMode() != Mode.RAMFIRE){
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
			robo.setPosition(getHitRobotPosition(e.getBearingRadians()));
			robo.setTime(getTime() - 1);// 1tick遅れて送られるため
			if(!e.isMyFault()){
				robo.setVelocity(0);
			}
		}else if(m instanceof MyData){
			MyData sig = (MyData)m;
			RobotData robo = data.get(sig.getName());
			robo.setPosition(sig.getX(), sig.getY());
			robo.setEnergy(sig.getEnergy());
			robo.setVelocity(sig.getVelocity());
			robo.setrHeading(sig.getHeadingRadians());
			robo.setTime(sig.getTime());
		}
	}

	// 衝突したロボットへの相対角度から座標を返す
	public Point2D.Double getHitRobotPosition(double rBearingRadians){
		double mAngle = tomAngle(rBearingRadians + getHeadingRadians());
		double x = getX() + Math.cos(mAngle);
		double y = getY() + Math.sin(mAngle);
		return new Point2D.Double(x, y);
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
			forcex += wallpoint / Math.pow(getX(), 2);
			forcex -= wallpoint / Math.pow(getBattleFieldWidth() - getX(), 2);
			forcey += wallpoint / Math.pow(getY(), 2);
			forcey -= wallpoint / Math.pow(getBattleFieldHeight() - getY(), 2);
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
			RobotData target = data.getTarget(this.getName());
			System.out.println(getName() + ":target:" + target.getName());
			force = getForce(5*target.getGravity(), target.getPosition());
			forcex -= force.getX();
			forcey -= force.getY();
			RobotData friend= data.getFriend(getName());
			if(friend!=null) {
				force = getForce(2, friend.getPosition());
				forcex += force.getX();
				forcey += force.getY();
			}

			attack(forcex, forcey);
		}
	}

	// positionから質量pointの反重力を返す
	private Point2D.Double getForce(double point, Point2D.Double position){
		double distance = Math.sqrt(Math.pow((getX() - position.getX()), 2) + Math.pow((getY() -
				position.getY()), 2));
		double power = -point / Math.pow(distance, 2);
		double forcex = power * (Math.cos(getmAngleBtwRobos(position)));
		double forcey = power * (Math.sin(getmAngleBtwRobos(position)));
		return new Point2D.Double(forcex, forcey);
	}

	public double getPower(double distance){
		if(distance <= 300){
			return 3;
		}else if(distance > 300 && distance <= 600){
			return 2;
		}else{
			return 1.01;// 計算式が得らしい
		}
	}

	// 威力powerで撃った弾速でroboに当たる座標を線形予測で計算する 壁を超えたら超えない範囲まで戻す
	// roboのデータはrobo.getTime()の時点でのものであることに注意
	public Point2D.Double getNextPosition(RobotData robo){
		double vx = robo.getVelocity() * Math.cos(robo.getmHeading()); // 相手のx方向の速度
		double vy = robo.getVelocity() * Math.sin(robo.getmHeading()); // 相手のy方向の速度
		double vp = 20 - 3 * getPower(robo.getDistance(getX(), getY())); // 弾速
		double time = robo.getDistance(getX(), getY()) / vp + (getTime() - robo.getTime());
		double x = robo.getPosition().getX() + vx * time;
		if(x < 0){
			x = 0;
		}else if(x > getBattleFieldWidth()){
			x = getBattleFieldWidth();
		}
		double y = robo.getPosition().getY() + vy * time;
		if(y < 0){
			y = 0;
		}else if(y > getBattleFieldHeight()){
			y = getBattleFieldHeight();
		}
		return new Point2D.Double(x, y);
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
		rturnRadians = normalize(torAngle(mAngle) - getHeadingRadians());
		if(rturnRadians > Math.PI / 2){// 右に回転しすぎ
			rturnRadians -= Math.PI;
			return -1;
		}else if(rturnRadians < -Math.PI / 2){// 左に回転しすぎ
			rturnRadians += Math.PI;
			return -1;
		}else{
			return 1;
		}
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
		double rDirection = normalize(torAngle(mAngle) - getHeadingRadians());// 回転する量
		int ret;
		if(rDirection > Math.PI / 2){// 右に回転しすぎ
			rDirection -= Math.PI;
			ret = -1;
		}else if(rDirection < -Math.PI / 2){// 左に回転しすぎ
			rDirection += Math.PI;
			ret = -1;
		}else{
			ret = 1;
		}
		setTurnRight(rDirection);
		return ret;
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

	// 角度を-piからpiの間に標準化
	public static double normalize(double radian){
		while(radian > Math.PI){
			radian -= 2 * Math.PI;
		}
		while(radian < -Math.PI){
			radian += 2 * Math.PI;
		}
		return radian;
	}

	public void onWin(WinEvent e){
		clearAllEvents();
		turnGunRight(1000);
	}
}
