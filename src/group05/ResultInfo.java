package group05;

import java.io.Serializable;

public class ResultInfo implements Serializable{
	boolean lost;

	public ResultInfo(boolean lost) {
		this.lost=lost;
	}

	public boolean hasLost() {
		return lost;
	}
}
