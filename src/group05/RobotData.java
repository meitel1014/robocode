package group05;

import java.awt.geom.Point2D;

/**
 * ロボットのデータを管理するクラス．
 *
 * @author MEITEL
 *
 */
public class RobotData{
	private String name;
	private double energy = -1;
	private Point2D.Double position;
	private double velocity = 0;
	private double heading = 0;
	private long time = 0;
	private boolean isLeader, isDroid;
	private boolean isTeammate;

	/**
	 * RobotDataListからのみ呼び出す．直接コンストラクタを使用してはならない．
	 *
	 * @param name
	 * @param isTeammate
	 */
	public RobotData(String name, boolean isTeammate){
		this.name = name;
		this.isTeammate = isTeammate;
		position = new Point2D.Double(500, 400);
		// グループ機体の場合
		if(name.contains("G")){
			if(name.contains("Leader")){
				isLeader = true;
			}else if(name.contains("Sub")){
				isLeader = false;
			}
		}
	}

	public double getGravity(){
		return 2;
	}

	/**
	 * このロボットの座標を記録する．
	 *
	 * @param x
	 * @param y
	 */
	public void setPosition(double x, double y){
		position.setLocation(x, y);
	}

	/**
	 * このロボットの座標をposiにする．
	 *
	 * @param position
	 */
	public void setPosition(Point2D.Double position){
		this.position = position;
	}

	/**
	 * このロボットの座標を返す．
	 *
	 * @return このロボットの座標
	 */
	public Point2D.Double getPosition(){
		return position;
	}

	/**
	 * このロボットと(x,y)との距離を返す．このメソッドは主に自分と相手の距離を取得するために用いる．
	 *
	 * @param x
	 * @param y
	 *
	 * @return このロボットと(x,y)の距離
	 */
	public double getDistance(double x, double y){
		return Math.sqrt(Math.pow((x - getPosition().getX()), 2) + Math.pow((y - getPosition().getY()), 2));
	}

	/**
	 * このロボットの速度を記録する，
	 *
	 * @param velocity
	 */
	public void setVelocity(double velocity){
		this.velocity = velocity;
	}

	/**
	 * このロボットの速度を返す．
	 *
	 * @return このロボットの速度
	 */
	public double getVelocity(){
		return velocity;
	}

	/**
	 * このロボットの名前を返す．
	 *
	 * @return このロボットの名前
	 */
	public String getName(){
		return name;
	}

	/**
	 * このロボットの残り体力を記録する．
	 *
	 * @param energy
	 */
	public void setEnergy(double energy){
		if(this.energy == -1){
			if(name.contains("Walls")){
				if(energy > 110){
					isLeader = true;
				}else{
					isLeader = false;
				}
			}else{
				if(!isLeader && energy > 110){
					isDroid = true;
				}else{
					isDroid = false;
				}
			}
		}
		this.energy = energy;
	}

	/**
	 * このロボットの残り体力を返す．
	 *
	 * @return このロボットの残り体力
	 */
	public double getEnergy(){
		return energy;
	}

	/**
	 * 記録の最終更新時間を記録する． 何かを記録したらその後に必ず呼ぶこと． 引数にはRobotのgetTime()メソッドで得られる時間を用いること．
	 *
	 * @param time
	 */
	public void setTime(long time){
		this.time = time;
	}

	/**
	 * 記録の最終更新時間を返す．
	 *
	 * @return 記録の最終更新時間
	 */
	public long getTime(){
		return time;
	}

	/**
	 * ロボットの向いている数学絶対角度を記録する．
	 *
	 * @param mradians
	 */
	public void setmHeading(double mRadians){
		this.heading = mRadians;
	}

	/**
	 * ロボットの向いているRobocode絶対角度を記録する．
	 *
	 * @param rRadians
	 */
	public void setrHeading(double rRadians){
		this.heading = G05.tomAngle(rRadians);
	}

	/**
	 * ロボットの向いている数学絶対角度をを返す．
	 *
	 * @return ロボットの向いている数学絶対角度
	 */
	public double getmHeading(){
		return heading;
	}

	/**
	 * ロボットの向いているRobocode絶対角度をを返す．
	 *
	 * @return ロボットの向いているRobocode絶対角度
	 */
	public double getrHeading(){
		return G05.torAngle(heading);
	}

	/**
	 * このロボットが味方かどうかを返す．
	 *
	 * @return このロボットが味方ならtrue，敵ならfalse
	 */
	public boolean isTeammate(){
		return isTeammate;
	}

	/**
	 * このロボットがリーダーかどうかを返す．
	 *
	 * @return このロボットがリーダーならtrue，子機ならfalse
	 */
	public boolean isLeader() {
		return isLeader;
	}

	/**
	 * このロボットがDroidかどうかを返す．
	 *
	 * @return このロボットがDroidならtrue，通常子機ならfalse
	 */
	public boolean isDroid() {
		return isDroid;
	}

	/**
	 * 弾を発射する機体を基準とした相手の次の座標を返す．
	 * http://www.wakayama-u.ac.jp/~fukuyasu/dis1-2006/robocode/
	 *
	 * @param x0(弾を発射する機体の位置)
	 * @param y0(弾を発射する機体の位置)
	 * @param power(弾の強さ)
	 * @param time(現在時刻)
	 *
	 * @return
	 */
	public Point2D.Double getNextPosition(double x0, double y0, double power, double time){
		double vx = velocity * Math.cos(getmHeading());
		double vy = velocity * Math.sin(getmHeading());
		double x = position.getX() + (time - this.time) * vx;
		double y = position.getY() + (time - this.time) * vy;
		double dx = x - x0;
		double dy = y - y0;
		double vp = 20 - 3 * power;
		double A = (vx * vx) + (vy * vy) - (vp * vp);
		double B = (2 * vx * dx) + (2 * vy * dy);
		double C = (dx * dx) + (dy * dy);
		double D = (B * B) - (4 * A * C);
		double t1, t2, t = -1;
		if(D >= 0){
			t1 = (-B + Math.sqrt(D)) / (2 * A);
			t2 = (-B - Math.sqrt(D)) / (2 * A);
			if(t1 < 0){
				if(t2 >= 0){
					t = t2;
				}
			}else{
				if(t2 < 0 || t1 < t2){
					t = t1;
				}else{
					t = t2;
				}
			}
		}else{
			return null;
		}

		double retx = x + t * vx;
		double rety = y + t * vy;

		if(retx < 0){
			retx = 0;
		}else if(retx > 1000){
			retx = 1000;
		}

		if(rety < 0){
			rety = 0;
		}else if(rety > 800){
			rety = 800;
		}

		return new Point2D.Double(retx, rety);
	}
}
