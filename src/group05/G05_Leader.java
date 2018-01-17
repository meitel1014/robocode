package group05;

public final class G05_Leader extends G05{
	@Override
	public Mode getMode(){
		if(data.walls() == 0) {
			if(data.isDroidDead()) {
				return Mode.RAMFIRE;
			}else {
				return Mode.EVADE;
			}
		}else{
			return Mode.WALL;
		}
	}
}
