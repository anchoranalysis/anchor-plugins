package ch.ethz.biol.cell.imageprocessing.chnl.provider;

class FilterState {
	private int numPixels = 0;
	private int sumIntensity = 0;
	
	public FilterState() {
		super();
	}
	
	public void addPixel( int val ) {
		sumIntensity += val;
		numPixels++;
	}
	
	public int calcMean() {
		if (numPixels==0) {
			return 0;
		}
		return sumIntensity / numPixels;
	}

	public void add( FilterState fs ) {
		this.numPixels = numPixels + fs.numPixels;
		this.sumIntensity = sumIntensity + fs.sumIntensity;
	}
	
	public void subtract( FilterState fs ) {
		this.numPixels = numPixels - fs.numPixels;
		this.sumIntensity = sumIntensity - fs.sumIntensity;
	}
}