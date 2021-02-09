package com.davixdevelop.terracustomtreegen;

import com.davixdevelop.terracustomtreegen.schematic.Schematic;

import net.minecraft.util.math.BlockPos;

public class CustomTreeGen {
	public Object treeGen;
	private int canopyRadius;
	public BlockPos top1;
	public boolean movedBack = false;
	public boolean repositioned = false;
	private double canopy;
	
	public CustomTreeGen(double canopy, BlockPos pos) {
		this.canopy = canopy;
		this.top1 = pos;
	}
	
	public void loadTree(Object treeGen, int canopyRadius) {
		this.treeGen = treeGen;
		this.canopyRadius = canopyRadius;
	}
	
	public int getCanopyRadius() {
		if(treeGen instanceof Schematic)
			return ((Schematic)treeGen).getRadus();
		else
			return canopyRadius;
	}
	
	public int getRootRadius() {
		if(treeGen instanceof Schematic) {
			return ((Schematic)treeGen).getRootRadus();
		}else
			return 2;
	}
	
	public double getCanopy() {
		return canopy;
	}
}

