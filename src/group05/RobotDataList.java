package group05;

import java.util.ArrayList;

import robocode.HitByBulletEvent;
import robocode.ScannedRobotEvent;

/**
 * 各ロボットに対する{@link RobotData}を格納するクラス．
 * @author MEITEL
 *
 */
public class RobotDataList{
	private ArrayList<RobotData> datalist;

	/**
	 * 味方ロボットの名前をString配列で受け取りそれらに対する{@link RobotData}を作成する．
	 * @param robotnames
	 */
	public RobotDataList(String[] robotnames){
		for(String name: robotnames){
			datalist.add(new RobotData(name, true));
		}
	}

	/**
	 * 名前がnameであるロボットの{@link RobotData}を返す．
	 * 未登録のロボットの名前が引数である場合は新しく{@link RobotData}を作成しそれを返す．
	 * @param name
	 * @return 名前がnameであるロボットの{@link RobotData}
	 */
	public RobotData get(String name){
		for(RobotData data: datalist){
			if(name == data.getName()){
				return data;
			}
		}

		// 未登録の場合
		RobotData newdata = new RobotData(name, false);
		datalist.add(newdata);
		return newdata;
	}

	/**
	 * 全ての敵ロボットの{@link RobotData}を持つArrayListを返す．
	 * @return 全ての敵ロボットの{@link RobotData}を持つArrayList
	 */
	public ArrayList<RobotData> getEnemies(){
		ArrayList<RobotData> ret = new ArrayList<RobotData>();
		for(RobotData data: datalist){
			if(!data.isTeammate()){
				ret.add(data);
			}
		}
		return ret;
	}

	//敵の弾に当たった時に防御ポイントを1上げる
	public void onHitByBullet(HitByBulletEvent e){
		RobotData robo = new RobotData(e.getName(),true);//とりあえずtrue代入したが、良いのか？
		if(robo.isTeammate() == false)
			robo.addDefendpoint(1);//攻撃をしてきた相手の防御ポイントを1上げる
	}

	//スキャンしたときにこちらを向いているときだけ防御ポイントを1上げる
	public void onScannedRobot(ScannedRobotEvent e) {
		RobotData robo = new RobotData(e.getName(),true);
		if(robo.isTeammate() == false) {
			int count=0;//2回目以降のスキャンでもカウントすることが無いようにしたいが、毎回定義してそう、、、
			if(e.getBearing() >160 || e.getBearing() <-160 ) {
				if(count == 0) {
					robo.addDefendpoint(1);
					count = 1;
				}
				else {
					if(count == 1) {
						robo.subDefendpoint(1);
						count = 0;
					}
				}
			}
		}
	}


}



