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
	private int attackPointByDistance, attackPointByDirection;
	private int defendPointByBullet, defendPointByDirection, defendPointByHitByRobot;
	private Point2D.Double position;
	private double velocity;
	private boolean isTeammate;

	/**
	 * RobotDataListからのみ呼び出す，直接コンストラクタを使用してはならない．
	 *
	 * @param name
	 * @param isTeammate
	 */
	public RobotData(String name, boolean isTeammate){
		this.name = name;
		this.isTeammate = isTeammate;
		attackPointByDistance = 0;
		attackPointByDirection = 0;
		defendPointByBullet = 2;
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

	public void setPosition(Point2D.Double posi){
		position = posi;
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
	 * このロボットの速度を記録する，
	 *
	 * @param v
	 */
	public void setVelocity(double v){
		velocity = v;
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
	 * このロボットが味方かどうかを返す．
	 *
	 * @return このロボットが味方ならtrue，敵ならfalse
	 */
	public boolean isTeammate(){
		return isTeammate;
	}
}
