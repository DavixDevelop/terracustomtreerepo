package com.davixdevelop.terracustomtreegen.repo;

import java.util.List;

/**
 * Internal class, representing which biomes and schematic indexes a tile contains
 * @author DavixDevelop
 *
 */
public class TreeBiome implements java.io.Serializable {
	public List<Integer> continents;
	public List<Integer> biomes;
	public List<Integer> treeL;
	public List<Integer> treeS;
	public List<Integer> climate;
}
