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
	private boolean isDroidDead = false;
	private int hasDroid = -1;

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
				return getWallTarget(myName);
			}else{
				return getGroupTarget(myName);
			}
		}else{
			if(walls > 1){
				return getWallTarget(myName);
			}else{
				return getGroupTarget(myName);
			}
		}
	}

	private RobotData getWallTarget(String myName){
		if(myName.contains("Leader")){
			for(RobotData data: getWalls(myName)){
				if(data.getName().contains("Walls")){
					if(data.isLeader){
						return data;
					}
				}
			}
			// 元々のターゲットが見つからなかった時用
			for(RobotData data: getWalls(myName)){
				if(data.getName().contains("Walls")){
					return data;
				}
			}
		}else{
			for(RobotData data: getWalls(myName)){
				if(data.getName().contains("Walls")){
					if(!data.isLeader){
						return data;
					}
				}
			}
			// 元々のターゲットが見つからなかった時用
			for(RobotData data: getWalls(myName)){
				if(data.getName().contains("Walls")){
					return data;
				}
			}
		}

		return null;
	}

	private RobotData getGroupTarget(String myName){
		if(myName.contains("Leader") && !isDroidDead){
			return null;
		}else{
			if(hasDroid()){
				for(RobotData data: getEnemyGroup()){
					if(data.isLeader){
						return data;
					}
				}
			}
		}

		return targetGroupSub();
	}

	// 敵機が残り1機ならそれを，2機残っていれば体力の少ない子機を狙う
	private RobotData targetGroupSub(){
		List<RobotData> enemy = getEnemyGroup();
		if(enemy.size() == 1){
			return enemy.get(0);
		}

		for(int i = 0; i < enemy.size(); i++){
			if(enemy.get(i).isLeader){
				enemy.remove(i);
				break;
			}
		}

		if(enemy.size() == 1){
			return enemy.get(0);
		}

		if(enemy.get(0).getEnergy() < enemy.get(1).getEnergy()){
			return enemy.get(0);
		}else{
			return enemy.get(1);
		}

	}

	public int size(){
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
	 * 全ての敵チームの{@link RobotData}を持つListを返す．
	 *
	 * @return 全ての敵チームの{@link RobotData}を持つList
	 */
	public List<RobotData> getEnemyGroup(){
		List<RobotData> enemies = Collections.synchronizedList(new ArrayList<RobotData>());
		for(RobotData data: getEnemies()){
			if(!data.isTeammate() && !data.getName().contains("Walls")){
				enemies.add(data);
			}
		}
		return enemies;
	}

	public List<RobotData> getWalls(String myName){
		List<RobotData> enemies = Collections.synchronizedList(new ArrayList<RobotData>());
		for(RobotData data: getEnemies()){
			if(data.getName().contains("Walls")){
				enemies.add(data);
			}
		}
		if(myName.contains("Sub2") && !isDroidDead){
			Collections.reverse(enemies);
		}
		return enemies;
	}

	/**
	 * 自分以外の全てのロボットの{@link RobotData}を持つListを返す．
	 *
	 * @return 全てのロボットの{@link RobotData}を持つList
	 */
	public List<RobotData> getAll(String myName){
		List<RobotData> robots = Collections.synchronizedList(new ArrayList<RobotData>());

		for(RobotData data:datalist) {
			if(!data.getName().equals(myName)) {
				robots.add(data);
			}
		}

		return robots;
	}

	/**
	 * 生存しているWallsの数を返す．
	 *
	 * @return 生存しているWallsの数
	 */
	public int walls(){
		return walls;
	}

	public boolean isDroidDead(){
		return isDroidDead;
	}

	public boolean isReady(){
		return datalist.size() == 9;
	}

	public boolean hasDroid(){
		if(hasDroid != -1){
			return hasDroid == 1? true: false;
		}
		if(isReady()){
			for(RobotData g: getEnemyGroup()){
				if(g.isDroid){
					hasDroid = 1;
					return true;
				}
			}

			hasDroid = 0;
			return false;
		}
		return false;
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

		if(name.contains("G05_Sub")){
			isDroidDead = true;
		}
	}
}
