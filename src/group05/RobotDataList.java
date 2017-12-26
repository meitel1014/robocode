package group05;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;;

/**
 * 各ロボットに対する{@link RobotData}を格納するクラス．
 *
 * @author MEITEL
 *
 */
public class RobotDataList{
	private static RobotDataList data = new RobotDataList();// シングルトン
	private List<RobotData> datalist = Collections.synchronizedList(new ArrayList<RobotData>());;
	private int walls = 3;

	private RobotDataList(){}

	public static RobotDataList getInstance(){
		return data;
	}

	/**
	 * 自分の名前を受け取りそれに対する{@link RobotData}を作成する．
	 *
	 * @param name
	 * @return
	 */
	public void setMe(String name){
		datalist.add(new RobotData(name, true));
	}

	/**
	 * 名前がnameであるロボットの{@link RobotData}を返す．
	 * 未登録のロボットの名前が引数である場合は新しく{@link RobotData}を作成しそれを返す．
	 *
	 * @param name
	 * @return 名前がnameであるロボットの{@link RobotData}
	 */
	public RobotData get(String name){
		for(RobotData data: datalist){
			if(name.equals(data.getName())){
				return data;
			}
		}

		// 未登録の場合
		RobotData newdata = new RobotData(name, false);
		datalist.add(newdata);
		return newdata;
	}

	/**
	 * 最も攻撃ポイントが高いロボットの{@link RobotData}を返す．
	 *
	 * @return 最も攻撃ポイントが高いロボットの{@link RobotData}
	 */
	public RobotData getTarget(){
		int point = -1;
		RobotData ret = null;

		for(RobotData data: this.getEnemies()){
			if(point < data.getAttackPoint()){
				point = data.getAttackPoint();
				ret = data;
			}
		}

		return ret;
	}

	/**
	 * 全ての敵ロボットの{@link RobotData}を持つListを返す．
	 *
	 * @return 全ての敵ロボットの{@link RobotData}を持つList
	 */
	public List<RobotData> getEnemies(){
		List<RobotData> ret = Collections.synchronizedList(new ArrayList<RobotData>());
		for(RobotData data: datalist){
			if(!data.isTeammate()){
				ret.add(data);
			}
		}
		return ret;
	}

	/**
	 * 全てのロボットの{@link RobotData}を持つListを返す．
	 *
	 * @return 全てのロボットの{@link RobotData}を持つList
	 */
	public List<RobotData> getAll(){
		return datalist;
	}

	/**
	 * 全てのロボットの防御ポイントの合計を返す．
	 *
	 * @return 全てのロボットの防御ポイントの合計
	 */
	public double getTotalDefendPoint(){
		double ret = 0;
		for(int i = 0; i < this.getAll().size(); i++){
			ret += datalist.get(i).getDefendPoint();
		}

		return ret;
	}

	/**
	 * 生存しているWallsの数を返す．
	 *
	 * @return 生存しているWallsの数
	 */
	public int walls(){
		return walls;
	}

	/**
	 * 名前がnameであるロボットのデータを削除する． このメソッドはロボットが死んだ時に呼び出す．
	 *
	 * @param name
	 */
	public void remove(String name){
		if(name.contains("Walls")){
			walls--;
		}

		for(int i = 0; i < datalist.size(); i++){
			if(name.equals(datalist.get(i).getName())){
				datalist.remove(i);
			}
		}
	}
}
