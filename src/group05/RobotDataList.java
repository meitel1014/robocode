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
	private List<RobotData> datalist = Collections.synchronizedList(new ArrayList<RobotData>());;
	private int walls = 3;
	private boolean isDroidDead=false;

	/**
	 * 自分の名前を受け取りそれに対する{@link RobotData}を作成する．
	 *
	 * @param name
	 */
	public RobotDataList(String name){
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
	 * ターゲットにする敵ロボットの{@link RobotData}を返す．
	 *
	 * @return ターゲットにする敵ロボットの{@link RobotData}
	 */
	public RobotData getTarget(String myName){
		if(myName.contains("Leader")){
			if(walls > 0){
				for(RobotData data: datalist){
					if(data.getName().contains("Walls")){
						if(data.isLeader){
							return data;
						}
					}
				}
			}else{

			}
		}else{
			if(walls > 1){
				for(RobotData data: datalist){
					if(data.getName().contains("Walls")){
						if(!data.isLeader && !data.isTargetted){
							data.isTargetted = true;
							return data;
						}
					}
				}
			}
		}
		return null;
	}

	public int size() {
		return datalist.size();
	}

	/**
	 * 全ての敵ロボットの{@link RobotData}を持つListを返す．
	 *
	 * @return 全ての敵ロボットの{@link RobotData}を持つList
	 */
	public List<RobotData> getEnemies(){
		List<RobotData> enemies = Collections.synchronizedList(new ArrayList<RobotData>());
		for(RobotData data: datalist){
			if(!data.isTeammate()){
				enemies.add(data);
			}
		}
		return enemies;
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
	 * 生存しているWallsの数を返す．
	 *
	 * @return 生存しているWallsの数
	 */
	public int walls(){
		return walls;
	}

	public boolean isDroidDead() {
		return isDroidDead;
	}

	public boolean isReady() {
		return datalist.size()==9;
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

		if(name.contains("G05_Sub")) {
			isDroidDead=true;
		}
	}
}
