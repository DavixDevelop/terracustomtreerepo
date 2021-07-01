package com.davixdevelop.terracustomtreegen.repo;

import java.util.ArrayList;
import java.util.List;

import com.davixdevelop.terracustomtreegen.schematic.Schematic;

/**
 *  Class, representing the data of the repository
 * 
 * @author DavixDevelop
 *
 */
public class TreeRepoData implements java.io.Serializable {
	public List<Schematic> trees;
	public List<TreeBiome> treeMeta;

	public TreeRepoData() {
		this.trees = new ArrayList<Schematic>();
		this.treeMeta = new ArrayList<TreeBiome>();
	}
}
