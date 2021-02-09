package com.davixdevelop.terracustomtreegen;

/**
 * Simple class that represents  a line segment with a radius
 * @author DavixDebelop
 *
 */
public class SegmentLinearFunc {
	private double k;
	private double n;
	private double ra;
	private double l;
	private double ri;
	private double t;
	private double b;
	private boolean constXc = false;
	private double constX = 0;
	private boolean constYc = false;
	private double constY = 0;
	
	public SegmentLinearFunc(double[] A, double[] B, double r) {
		if(B[0] != A[0] && B[1] != A[1]) {
			k = (B[1] - A[1]) / (B[0] - A[0]);
			n = A[1] - k * A[0];
		}
		else if(B[0] == A[0]) {
			constXc = true;
			constX = B[0];
		}
		else if(B[1] == A[1]) {
			constYc = true;
			constY = B[1];
		}
			
			
		
		this.l = A[0];
		this.ri = B[0];
		this.b = A[1];
		this.t = B[1];
		this.ra = r;
	}
	
	//Smerni koli�nik
	public double getK() {return k;}
	//Odsek na ordinatni osi
	public double getN() {return n;}
	//Radius of segment
	public double getR() {return ra;}
	
	public double getY(double x) {
		if(constYc)
			return constY;
		return k * x + n;
	}
	
	public double getX(double y) {
		if(constXc)
			return constX;
		return (y - n) / k;
		
	}
	
	//Get left bound
	public double getAx() {return l;}
	
	//Get right bound
	public double getBx() {return ri;}
	
	//Get top bound
	public double getBy() {return t;}
	
	//Get bottom bound
	public double getAy() {return b;}
	
	public boolean isConstantX() {return constXc;}
	public boolean isConstantY() {return constYc;}
	
	public double getConstantX() {return constX;}
	public double getConstantY() {return constY;}

	public boolean equals(Object object){
		if(object instanceof SegmentLinearFunc){
			SegmentLinearFunc seg = (SegmentLinearFunc) object;
			if(seg.getAx() == getAx() && seg.getAy() == getAy() && seg.getBx() == getBx() && seg.getBy() == getBy() && seg.getR() == getR())
				return true;
		}
		return false;
	}
}
