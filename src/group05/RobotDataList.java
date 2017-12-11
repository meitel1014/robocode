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
	private List<RobotData> datalist;

	/**
	 * 味方ロボットの名前をString配列で受け取りそれらに対する{@link RobotData}を作成する．
	 *
	 * @param robotnames
	 */
	public RobotDataList(String[] robotnames){
		datalist = Collections.synchronizedList(new ArrayList<RobotData>());
		if(robotnames == null){
			return;
		}
		for(String name: robotnames){
			datalist.add(new RobotData(name, true));
		}
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

	public RobotData getTarget(){
		int point = -1;
		RobotData ret = null;

		for(RobotData data:this.getEnemies()){
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
		List<RobotData> ret =Collections.synchronizedList(new ArrayList<RobotData>());
		for(RobotData data: datalist){
			if(!data.isTeammate()){
				ret.add(data);
			}
		}
		return ret;
	}

	public List<RobotData> getAll(){
		return datalist;
	}

	public void remove(String name){
		for(int i = 0; i < datalist.size(); i++){
			if(name.equals(datalist.get(i).getName())){
				datalist.remove(i);
			}
		}
	}
}
