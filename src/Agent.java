
public class Agent {
	int hypo;
	boolean status;

	public Agent(int hypothesis, boolean s) {
		hypo = hypothesis;
		status = s;
	}

	public int getH() {
		return hypo;
	}

	public void setH(int h) {
		hypo = h;
	}

	public Boolean getS() {
		return status;
	}

	public void setS(Boolean s) {
		status = s;
	}
}// end class Agent