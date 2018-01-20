package group05;

import java.io.Serializable;

public class ResultInfo implements Serializable{
	private static final long serialVersionUID = 32047325204L;

	private boolean lost;

	public ResultInfo(boolean lost) {
		this.lost=lost;
	}

	public boolean hasLost() {
		return lost;
	}
}
