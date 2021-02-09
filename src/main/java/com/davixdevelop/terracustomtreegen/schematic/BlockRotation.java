package com.davixdevelop.terracustomtreegen.schematic;

public class BlockRotation {
	public int[] br;
	
	/**
	 * 
	 * @param r An array of integers, that represent the meta value of the direction the block faces
	 */
	public BlockRotation(int... r) {
		br = r;
	}
	
	/**
	 * Returns a side/index for the meta
	 * @param meta The meta of the block
	 * @return The side/index of the meta. Default's to -1 if not found
	 */
	public int getSide(int meta) {
		for(int r = 0; r < br.length; r++) {
			if(br[r] == meta) return r;
		}
		return -1;
	}
	
	/**
	 * Returns the mata value for the side/index
	 * @param side The side/index 
	 * @return The meta for the side/index
	 */
	public int getMeta(int side) {
		return br[side];
	}
}