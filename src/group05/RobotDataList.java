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
	private boolean isLeaderDead = false;
	private boolean isDroidDead = false;
	private boolean isReady=false;
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
		boolean isTeammate = false;
		if(name.contains("G05")){
			isTeammate = true;
		}
		RobotData newdata = new RobotData(name, isTeammate);
		datalist.add(newdata);
		return newdata;
	}

	/**
	 * ターゲットにする敵ロボットの{@link RobotData}を返す．
	 *
	 * @param myName このメソッドを呼び出すロボットの名前
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
			if(walls > 1 && !isLeaderDead){
				return getWallTarget(myName);
			}else{
				return getGroupTarget(myName);
			}
		}
	}


	//Wallsの中からmyNameが狙うべきロボットを選ぶ．
	private RobotData getWallTarget(String myName){
		if(myName.contains("Leader")){
			for(RobotData data: getWalls(myName)){
				if(data.getName().contains("Walls")){
					if(data.isLeader()){
						return data;
					}
				}
			}
		}else{
			for(RobotData data: getWalls(myName)){
				if(data.getName().contains("Walls")){
					if(!data.isLeader()){
						return data;
					}
				}
			}
		}
		// 元々のターゲットが見つからなかった時用 Walls優先
		for(RobotData data: getWalls(myName)){
			if(data.getName().contains("Walls")){
				return data;
			}
		}
		return targetAll();
	}

	//敵グループの中からmyNameが狙うべきロボットを選ぶ．
	private RobotData getGroupTarget(String myName){
		if(myName.contains("Leader") && !isDroidDead){
			return null;
		}else{
			if(hasDroid()){
				for(RobotData data: getEnemyGroup()){
					if(data.isLeader()){
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
		if(enemy.isEmpty()){
			return targetAll();
		}
		if(enemy.size() == 1){
			return enemy.get(0);
		}
		for(int i = 0; i < enemy.size(); i++){
			if(enemy.get(i).isLeader()){
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

	// すべての敵から適当に一体選んで返す
	private RobotData targetAll(){
		return getEnemies().get(0);
	}

	/**
	 * 一緒にRamfireする味方の{@link RobotData}を返す．
	 *
	 * @param myName このメソッドを呼び出すロボットの名前
	 *
	 * @return 一緒にRamfireする味方の{@link RobotData}
	 */
	public RobotData getFriend(String myName){
		if(isDroidDead){
			if(myName.contains("Leader")){
				for(RobotData robo: datalist){
					if(robo.getName().contains("Sub")){
						return robo;
					}
				}
			}else{
				for(RobotData robo: datalist){
					if(robo.getName().contains("Leader")){
						return robo;
					}
				}
			}
		}else{
			for(RobotData robo: datalist){
				if(robo.getName().equals(myName)){
					continue;
				}
				if(robo.getName().contains("Sub")){
					return robo;
				}
			}
		}
		return null;
	}

	/**
	 * 敵グループを全滅させて敵がWallsだけになっているかを返す．
	 *
	 * @return 敵がWallsだけか
	 */
	public boolean isFinalize(){
		return getEnemyGroup().isEmpty() && walls > 0;
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

	/**
	 * 全てのWallsの{@link RobotData}を持つListを返す．
	 *
	 * @return 全てのWallsの{@link RobotData}を持つList
	 */
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
		for(RobotData data: datalist){
			if(!data.getName().equals(myName)){
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

	/**
	 * 自分のLeaderが死んでいるかを返す．
	 *
	 * @return 自分のLeaderが死んでいるか
	 */
	public boolean isLeaderDead(){
		return isLeaderDead;
	}

	/**
	 * 自分の子機が一機でも死んでいるかを返す．
	 *
	 * @return 自分の子機が一機でも死んでいるか
	 */
	public boolean isDroidDead(){
		return isDroidDead;
	}

	/**
	 * 全てのロボットを登録できたかを返す．
	 *
	 * @return 全てのロボットを登録できたか
	 */
	public boolean isReady(){
		if(isReady){
			return true;
		}
		if(datalist.size() == 9) {
			isReady=true;
			return true;
		}
		return false;
	}

	/**
	 * 敵グループがDroidを使用しているかを返す．
	 *
	 * @return 敵グループがDroidを使用しているか
	 */
	public boolean hasDroid(){
		if(hasDroid != -1){
			return hasDroid == 1;
		}
		if(isReady()){
			System.out.println("judgedroid");
			for(RobotData g: getEnemyGroup()){
				if(g.isDroid()){
					System.out.println("droid");
					hasDroid = 1;
					return true;
				}
			}
			System.out.println("notdroid");
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
		for(int i = 0; i < 9; i++){
			try{
				if(name.equals(datalist.get(i).getName())){
					datalist.remove(i);
				}
			}catch(IndexOutOfBoundsException e){
				break;
			}
		}
		if(name.contains("G05_Leader")){
			isLeaderDead = true;
		}
		if(name.contains("G05_Sub")){
			isDroidDead = true;
		}
	}
}
