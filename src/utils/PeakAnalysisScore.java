package utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import java.util.Arrays;

/**
 * Peak analysis score. 
 * peaks analysis score is adapted from the APA score from Rao&Huntley et al., 2014: 
 * @author axel poulet
 *
 */
public class PeakAnalysisScore {
	/** Raw image of the matrix*/
	private ImagePlus m_imgRaw = new ImagePlus();
	/** HashMap of object loops*/
	private HashMap<String,Loop> m_data = new HashMap<String,Loop>();
	/** ImageProcessor of the raw ImagePlus*/
	private ImageProcessor m_ipRaw;
		
	/**
	 * Constructor of PeakAnalysisScore
	 * @param imgRaw ImagePlus raw image
	 * @param data HashMap of loops
	 */
	public PeakAnalysisScore(ImagePlus imgRaw, HashMap<String,Loop> data){
		this.m_imgRaw = imgRaw;
		this.m_data = data;
		this.m_ipRaw = m_imgRaw.getProcessor();
	}
		
	
	/**
	 * Method to compute the score of each loop. on a 11*11 square, the average of the corner (3*3) are computed. then the ration between the loops value and this avg is computed.
	 * For the regional value, the avg of the n_8 value of the loops are done, then a ratio is computed with the avg value of the corner,
	 * This method is used for the observed and oMe method.
	 * 
	 */
	public void computeScore(){
		Set<String> key = m_data.keySet();
		Iterator<String> it = key.iterator();
		while (it.hasNext()){
			String cle = it.next();
			Loop loop = m_data.get(cle);
			int x = loop.getX();
			int y = loop.getY();
			double corner = 0;
			double cornerAvg = 0;
			double center = m_ipRaw.getPixel(x, y);
			double squareCenterAvg = process3By3SquareAvg(x,y);
			int nbCorner = 0;
			if(x >= 5 && y >= 5 && x < m_imgRaw.getWidth()-5 && y < m_imgRaw.getHeight()-5){
				cornerAvg += process3By3SquareAvg(x-4,y-4); 
				corner += process3By3SquareMed(x-4,y-4);
	
				cornerAvg += process3By3SquareAvg(x-4,y+4);
				corner += process3By3SquareMed(x-4,y+4);
	
				cornerAvg += process3By3SquareAvg(x+4,y-4);
				corner += process3By3SquareMed(x+4,y-4);
	
				cornerAvg += process3By3SquareAvg(x+4,y+4);
				corner += process3By3SquareMed(x+4,y+4);
	
				nbCorner = 4;
			}
		
			if(nbCorner > 0){
				corner = corner/nbCorner;
				cornerAvg = cornerAvg/nbCorner;
				loop.setPaScoreAvg(center/cornerAvg);
				loop.setRegionalPaScoreAvg(squareCenterAvg/cornerAvg);	
				loop.setPaScoreMed(center/corner);
				loop.setRegionalPaScoreMed(squareCenterAvg/corner);	
			}
		}
	}
	

	
	/**
	 * compute the avg of3*3 square
	 * @param x int coordinate of the pixel center
	 * @param y int coordinat of the pixel center
	 * @return
	 */
	private double process3By3SquareAvg(int x, int y){
		double sum = 0;
		int nb = 0;
		for(int i = x-1; i <= x+1; ++i){
			for(int j = y-1; j <= y+1; ++j){
				if(i < m_ipRaw.getWidth() && i>0 && j < m_ipRaw.getWidth() && j > 0){
					sum += this.m_ipRaw.getf(i,j);
					nb++;
				}
			}
		}
		if(nb == 0)
			return 0;
		return sum/nb;
	}
	
	
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private double process3By3SquareMed(int x, int y){
		int []value = new int [9];
		int cmp = 0;
		for(int i = x-1; i <= x+1; ++i){
			for(int j = y-1; j <= y+1; ++j){
				if(i < m_ipRaw.getWidth() && i>0 && j < m_ipRaw.getWidth() && j > 0){
					value[cmp] = this.m_ipRaw.getPixel(i, j);
					++cmp;
				}
			}
		}
		return median(value);
	}
	
	/**
	 * 
	 * @param value
	 * @return
	 */
	private double median (int[] value ){
		Arrays.sort(value);
		double median;
		if (value.length % 2 == 0)
			median = ((double)value[value.length/2] + (double)value[value.length/2 - 1])/2;
		else
			median = (double) value[value.length/2];
		return median;
	}
}
