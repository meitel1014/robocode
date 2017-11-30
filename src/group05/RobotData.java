package group05;

import java.awt.geom.Point2D;


/**
 * ロボットのデータを管理するクラス．
 * @author MEITEL
 *
 */
public class RobotData{
	private String name;
	private int attackPoint, defendPoint;
	private Point2D.Double position;
	private double velocity;
	private boolean isTeammate;

	/**
	 * RobotDataListからのみ呼び出す，直接コンストラクタを使用してはならない．
	 * @param name
	 * @param isTeammate
	 */
	public RobotData(String name, boolean isTeammate){
		this.name = name;
		this.isTeammate = isTeammate;
		attackPoint = 0;
		defendPoint = 2;
		position = new Point2D.Double();
	}

	/**
	 * このロボットの攻撃ポイントを返す．
	 * @return このロボットの攻撃ポイント
	 */
	public int getAttackPoint(){
		return attackPoint;
	}

	/**
	 * このロボットの攻撃ポイントにpointを加える．
	 * @param point
	 */
	public void addAttackPoint(int point){
		attackPoint += point;
	}

	/**
	 * このロボットの攻撃ポイントからpointを引く．
	 * 攻撃ポイントが負になる場合は0にする．
	 * @param point
	 */
	public void subAttackPoint(int point){
		attackPoint -= point;
		if(attackPoint < 0){
			attackPoint = 0;
		}
	}

	/**
	 * このロボットの防御ポイントを返す．
	 * @return
	 */
	public int getDefendPoint(){
		return defendPoint;
	}

	/**
	 * このロボットの防御ポイントにpointを加える．
	 * @param point
	 */
	public void addDefendpoint(int point){
		defendPoint += point;
	}

	/**
	 * このロボットの防御ポイントからpointを引く．
	 * 防御ポイントが負になる場合は0にする．
	 * @param point
	 */
	public void subDefendpoint(int point){
		defendPoint -= point;
		if(defendPoint < 0){
			defendPoint = 0;
		}
	}

	/**
	 * このロボットの座標を記録する．
	 * @param x
	 * @param y
	 */
	public void setPosition(double x, double y){
		position.setLocation(x, y);
	}

	/**
	 * このロボットの座標を返す．
	 * @return このロボットの座標
	 */
	public Point2D.Double getPosition(){
		return position;
	}

	/**
	 * このロボットの速度を記録する，
	 * @param v
	 */
	public void setVelocity(double v){
		velocity=v;
	}

	/**
	 * このロボットの速度を返す．
	 * @return このロボットの速度
	 */
	public double getVelocity(){
		return velocity;
	}

	/**
	 * このロボットの名前を返す．
	 * @return このロボットの名前
	 */
	public String getName(){
		return name;
	}

	/**
	 * このロボットが味方かどうかを返す．
	 * @return このロボットが味方ならtrue，敵ならfalse
	 */
	public boolean isTeammate(){
		return isTeammate;
	}
}
