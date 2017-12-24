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
	private int attackPointByDistance=0, attackPointByDirection=0;
	private int defendPointByBullet, defendPointByDirection=0, defendPointByHitByRobot=0;
	private double energy;
	private Point2D.Double position;
	private double velocity=0;
	private boolean isTeammate;

	/**
	 * RobotDataListからのみ呼び出す．直接コンストラクタを使用してはならない．
	 *
	 * @param name
	 * @param isTeammate
	 */
	public RobotData(String name, boolean isTeammate){
		this.name = name;
		this.isTeammate=isTeammate;
		if(isTeammate) {
			defendPointByBullet = 2;
		}else {
			defendPointByBullet = 0;
		}
		defendPointByDirection = 0;
		position = new Point2D.Double();
	}

	/**
	 * このロボットの攻撃ポイントを返す．
	 *
	 * @return このロボットの攻撃ポイント
	 */
	public int getAttackPoint(){
		return attackPointByDirection + attackPointByDistance;
	}

	/**
	 * このロボットの距離攻撃ポイントをpointにする．
	 *
	 * @param point
	 */
	public void setDistanceAttackPoint(int point){
		attackPointByDistance = point;
	}

	/**
	 * このロボットの方向攻撃ポイントをpointにする．
	 *
	 * @param point
	 */
	public void setDirectionAttackPoint(int point){
		attackPointByDirection = point;
	}

	/**
	 * このロボットの防御ポイントを返す．
	 *
	 * @return
	 */
	public int getDefendPoint(){
		return defendPointByBullet + defendPointByDirection + defendPointByHitByRobot;
	}

	/**
	 * このロボットの弾防御ポイントにpointを加える．
	 *
	 * @param point
	 */
	public void addBulletDefendpoint(int point){
		defendPointByBullet += point;
	}

	/**
	 * このロボットの方向防御ポイントをpointにする．
	 *
	 * @param point
	 */
	public void setDirectionDefendpoint(int point){
		defendPointByDirection = point;
	}

	/**
	 * このロボットの弾防御ポイントからpointを引く． 防御ポイントが負になる場合は0にする．
	 *
	 * @param point
	 */
	public void subDefendpoint(int point){
		defendPointByBullet -= point;
		if(defendPointByBullet < 0){
			defendPointByBullet = 0;
		}
	}

	public void addDefendPointByHitByRobot(int point){
		defendPointByHitByRobot += point;
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
	public double getDistance(double x,double y) {
		return Math.sqrt(Math.pow((x - getPosition().getX()), 2) + Math.pow((y -getPosition().getY()), 2));
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
	 * このロボットの残り体力を記録する，
	 *
	 * @param energy
	 */
	public void setEnergy(double energy){
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
	 * このロボットが味方かどうかを返す．
	 *
	 * @return このロボットが味方ならtrue，敵ならfalse
	 */
	public boolean isTeammate(){
		return isTeammate;
	}
}
