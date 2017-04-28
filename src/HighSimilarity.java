

public class HighSimilarity {

	int modelSize;
	int fragNum;

	public HighSimilarity(int fragmentNum, int modSize) {
		fragNum = fragmentNum;
		modelSize = modSize;
	}

	public int getModelSize() {
		return modelSize;
	}

	public void setModelSize(int modelSize) {
		this.modelSize = modelSize;
	}

	public int getFragNum() {
		return fragNum;
	}

	public void setFragNum(int fragNum) {
		this.fragNum = fragNum;
	}
}//end class
