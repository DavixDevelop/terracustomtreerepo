package com.davixdevelop.terracustomtreegen.repo;

import com.davixdevelop.terracustomtreegen.TerraTreeRepoMod;
import com.davixdevelop.terracustomtreegen.CustomTreeGen;
import com.davixdevelop.terracustomtreegen.schematic.Schematic;
import com.davixdevelop.terracustomtreegen.schematic.SchematicOffset;
import net.buildtheearth.terraplusplus.TerraMod;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Represents a repository of schematics of custom trees in the assets
 *
 * @author DavixDevelop
 *
 */
public class CustomTreeRepository {
	public static final double LOWER_LIMIT = (0.95 / 3);
	public static final double UPPER_LIMIT = (0.95 / 3) * 2;
	public static final double MIDDLE_LIMIT = 0.95 / 2;
	public static final int MAX_TREE_HEIGHT = 27;

	private TreeRepoData data;

	public CustomTreeRepository() {
		this.data = new TreeRepoData();
		try {
			this.data = readFromDat();
			//readFromAssets(false);
		}catch(IOException | ClassNotFoundException ex) {
			this.data.treeMeta = null;
			TerraTreeRepoMod.LOGGER.error("Error while reading custotreerepo.dat. Custom Trees disabled and falling back to only vanilla trees.");
			ex.printStackTrace();
		}

	}

	/**
	 * Get list of tree metadata
	 * @return The trees metadata list
	 */
	public List<TreeBiome> getTreeMeta(){
		return data.treeMeta;
	}

	/**
	 * Returns a schematic of a tree at the specidifed index
	 *
	 * @param index The index of the tree)
	 * @return The schematic of the tree
	 */
	public Schematic getTreeSchematic(int index) {
		return this.data.trees.get(index);
	}

	/**
	 * This method returns and array of WordGenAbstractTree's and Schematics
	 * depending on the location, and biome
	 *
	 * @param trees		 List of trees
	 * @param biome      The biome of the location
	 * @param tile 		 The tree data tile from the tree map
	 * @param random     The Java random
	 * @return A array of objects, containing WordGenAbstractTree's and Schematics
	 */
	public List<CustomTreeGen> getTreeGenerators(List<CustomTreeGen> trees, Biome biome, TreeData tile, Random random) {
		if (trees.size() != 0) {
			int biomeId = Biome.getIdForBiome(biome);

			// Find schematic indexes for location
			List<Integer> schematicIndex = new ArrayList<Integer>();
			List<Integer> schematicOrg = new ArrayList<Integer>();

			double canopyCategory = -1;
			for (int t = 0; t < trees.size(); t++) {
				CustomTreeGen gen = trees.get(t);

				//Only get new schematics indexes if the canopy category changes
				if(canopyCategory != ((gen.getCanopy() <= LOWER_LIMIT) ? 0 : 1)) {
					// Get applicable schematics indexes for tile
					canopyCategory = (gen.getCanopy() <= LOWER_LIMIT) ? 0 : 1;
					schematicIndex = getTreeIndexes(biomeId, gen.getCanopy(), tile);
					schematicOrg = new ArrayList<Integer>(schematicIndex);
				}

				int minTreeHeight = (int) (gen.getCanopy() * MAX_TREE_HEIGHT);
				// Tree size from canopy
				int l = random.nextInt(3) + minTreeHeight;
				List<Integer> indexes = new ArrayList<Integer>();
				boolean vines = false;

				//random.nextBoolean()
				if(!schematicIndex.isEmpty() && random.nextBoolean()){
					// Add schematic indexes to selection
					int ran = random.nextInt(schematicIndex.size());
					indexes.add(schematicIndex.get(ran));
					schematicIndex.remove(ran);
				}else {
					// Check for vines
					if (Arrays.asList(6, 21, 134, 149, 151).contains(biomeId) ||
							(random.nextBoolean() && biomeId == 22)) {
						vines = true;
					}

					int plainsChoice = random.nextInt(9);

					// Oak
					// Conditionally add oak if biome is 23 (jungle edge)
					// or 22 (jungle hills - rarer)
					// or 131 (mutated extreme hills - very, very rare)
					// or 3 (extreme hills - very rare)
					if (Arrays.asList(4, 6, 7, 18, 19, 20, 21, 29, 129, 132, 134, 149, 157, 163, 164)
							.contains(biomeId) ||
							(biomeId == 1 && plainsChoice < 5) ||
							(random.nextBoolean() && Arrays.asList(23, 151).contains(biomeId)) ||
							(random.nextBoolean() && random.nextBoolean() && Arrays.asList(22, 3).contains(biomeId)) ||
							(random.nextBoolean() && random.nextBoolean() && random.nextBoolean() && biomeId == 131)) {
						if (gen.getCanopy() <= LOWER_LIMIT) {
							if (biomeId != 6) {
								indexes.add(500);
							} else {
								indexes.add(501);
							}
						} else {
							indexes.add(502);
						}
					}

					// Spruce
					// Conditionally add spruce if biome is 29 (roofed forest)
					// or 129 (mutated plains)
					// or 22 (jungle hills - rarer)
					// or 132 (mutated forest)
					// or 4 (forest - rarer)
					// or 12 (ice plains - rarer)
					// or 140 (mutated ice flats - very, very, very rare)
					if (Arrays.asList(3, 5, 11, 13, 20, 30, 31, 32, 33, 34, 131, 133, 140, 158, 160, 161).contains(biomeId) ||
							(biomeId == 1 && plainsChoice == 8) ||
							(random.nextBoolean() && Arrays.asList(29, 129, 132).contains(biomeId)) ||
							(random.nextBoolean() && random.nextBoolean() && Arrays.asList(22, 4, 12).contains(biomeId)) ||
							(random.nextBoolean() && random.nextBoolean() && random.nextBoolean() && biomeId == 140)) {
						if (gen.getCanopy() <= LOWER_LIMIT) {
							indexes.add(503);
						} else if (gen.getCanopy() >= UPPER_LIMIT) {
							indexes.add(504);
						} else {
							indexes.add(505);
						}
					}

					// Birch
					// Conditionally add birch if biome is 23 (jungle edge)
					// or 151 (mutated jungle edge)
					// or 22 (jungle hills - very, very rare)
					// or 131 (mutated extreme hills - very, very rare)
					// or 132 (mutated forest)
					// or 3 (extreme hills - very, very rare)
					// or 30 (cold taiga)
					// or 161 (mutated redwood taiga hills)
					// ot 6 (swampland, rarer)
					// or 134 (swampland mutated)
					// or 4 (forest)
					// or 5 (taiga - rarer)
					if (Arrays.asList(18, 27, 28, 29, 129, 132, 155, 156, 157).contains(biomeId) ||
							(biomeId == 1 && plainsChoice < 8) ||
							(random.nextBoolean() && Arrays.asList(23, 151, 132, 30, 161, 134, 4).contains(biomeId)) ||
							(random.nextBoolean() && random.nextBoolean() && Arrays.asList(6, 5).contains(biomeId)) ||
							(random.nextBoolean() && random.nextBoolean() && random.nextBoolean() && Arrays.asList(22, 131, 3).contains(biomeId))) {
						if (gen.getCanopy() <= LOWER_LIMIT) {
							indexes.add(506);
						} else if (gen.getCanopy() >= UPPER_LIMIT) {
							indexes.add(507);
						} else {
							indexes.add(508);
						}
					}

					// Jungle
					// Conditionally add jungle is biome is 36 (savanna plateau)
					if (Arrays.asList(21, 22, 23, 149, 151).contains(biomeId) ||
							(random.nextBoolean() && biomeId == 36)) {
						if (gen.getCanopy() <= LOWER_LIMIT) {
							indexes.add(509);
						} else if (gen.getCanopy() >= UPPER_LIMIT) {
							indexes.add(510);
						} else {
							indexes.add(511);
						}
					}

					// Acacia
					// Conditionally add birch if biome is 21 (jungle) or 6 (jungle) && climate is 3 (BWh)
					// or 17 (desert hills) or 151 (mutated jungle edge)
					// Or if climate is 6 (BSk) and biome is 2 (desert)
					// Or 22 (jungle hills)
					// or 131 (mutated extreme hills - very rare)
					// or 132 (mutated forest)
					if (Arrays.asList(35, 36, 37, 38, 39, 163, 164, 165, 166, 167).contains(biomeId) ||
							(random.nextBoolean() && (Arrays.asList(21, 22, 17, 151, 132).contains(biomeId) || (biomeId == 2 && tile.koppenClimateIndex == 3.0))) ||
							(random.nextBoolean() && random.nextBoolean() && biomeId == 131) ||
							(tile.koppenClimateIndex == 6.0 && biomeId == 2)
					) {
						if (gen.getCanopy() <= LOWER_LIMIT) {
							indexes.add(512);
						} else if (gen.getCanopy() >= UPPER_LIMIT) {
							indexes.add(513);
						} else {
							indexes.add(514);
						}
					}

					// Dark oak
					if (Arrays.asList(29, 157).contains(biomeId)) {

						if (gen.getCanopy() <= LOWER_LIMIT) {
							indexes.add(515);
						} else {
							indexes.add(516);
						}
					}
				}


				if(indexes.isEmpty()){
					trees.remove(t);
					t--;
					continue;
				}


				// Select random tree type if biome has multiple tree types, otherwise select
				// first tree
				int sel = 0;
				if (indexes.size() > 1) {
					sel = random.nextInt(indexes.size());
				}

				if (indexes.get(sel) < 500) {
					gen.loadTree(getTreeSchematic(indexes.get(sel)), 0);
				} else {
					switch (indexes.get(sel)) {
						case 500:
							gen.loadTree(new WorldGenTrees(false, l, Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT,
									BlockPlanks.EnumType.OAK), Blocks.LEAVES.getDefaultState()
									.withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK), vines), 2);
							break;
						case 501:
							gen.loadTree(new WorldGenSwamp(), 3);
							break;
						case 502:
							gen.loadTree(new WorldGenBigTree(false), 3);
							break;
						case 503:
							gen.loadTree(new WorldGenTaiga2(false), 2);
							break;
						case 504:
							gen.loadTree(new WorldGenMegaPineTree(false, random.nextBoolean()), 2);
							break;
						case 505:
							gen.loadTree(new WorldGenTaiga1(), 1);
							break;
						case 506: {
							gen.loadTree(new WorldGenTrees(false, l, Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT,
									BlockPlanks.EnumType.BIRCH), Blocks.LEAVES.getDefaultState()
									.withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.BIRCH)
									.withProperty(BlockLeaves.CHECK_DECAY, false), vines), 2);
						}
						break;
						case 507:
							gen.loadTree(new WorldGenBirchTree(false, true), 2);
							break;
						case 508:
							gen.loadTree(new WorldGenBirchTree(false, false), 2);
							break;
						case 509: {
							gen.loadTree(new WorldGenTrees(false, minTreeHeight + random.nextInt(7), Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT,
									BlockPlanks.EnumType.JUNGLE), Blocks.LEAVES.getDefaultState()
									.withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE)
									.withProperty(BlockLeaves.CHECK_DECAY, false), vines), 2);
						}
						break;
						case 510: {
							gen.loadTree(new WorldGenMegaJungle(false, minTreeHeight, random.nextInt(20), Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT,
									BlockPlanks.EnumType.JUNGLE), Blocks.LEAVES.getDefaultState()
									.withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE)
									.withProperty(BlockLeaves.CHECK_DECAY, false)), 3);
						}
						break;
						case 511: {
							gen.loadTree(new WorldGenTrees(false, minTreeHeight + random.nextInt(9), Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT,
									BlockPlanks.EnumType.JUNGLE), Blocks.LEAVES.getDefaultState()
									.withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE)
									.withProperty(BlockLeaves.CHECK_DECAY, false), vines), 2);
						}
						break;
						case 512: {
							gen.loadTree(new WorldGenTrees(false, l, Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT,BlockPlanks.EnumType.ACACIA), Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT,BlockPlanks.EnumType.ACACIA).withProperty(BlockLeaves.CHECK_DECAY, false), vines), 2);
						}
						break;
						case 513: {
							gen.loadTree(new WorldGenMegaJungle(false, minTreeHeight, random.nextInt(7), Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT,BlockPlanks.EnumType.ACACIA), Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT,BlockPlanks.EnumType.ACACIA).withProperty(BlockLeaves.CHECK_DECAY, false)), 3);
						}
						break;
						case 514:
							gen.loadTree(new WorldGenSavannaTree(false), 4);
							break;
						case 515: {
							gen.loadTree(new WorldGenTrees(false, l, Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT,BlockPlanks.EnumType.DARK_OAK), Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.DARK_OAK).withProperty(BlockLeaves.CHECK_DECAY, false), vines), 2);
						}
						break;
						case 516:
							gen.loadTree(new WorldGenCanopyTree(false), 3);
							break;
					}
				}

				trees.set(t, gen);
			}
		}

		return trees;
	}

	/**
	 * This method returns the schematic indexes for the location
	 *
	 * @param biomeId The biome id of the location
	 * @param canopy  The canopy value on the location
	 * @param tile    The tree data tile from the tree map
	 * @return A list of schematics indexes
	 */
	private List<Integer> getTreeIndexes(int biomeId, double canopy, TreeData tile) {
		List<Integer> ind = new ArrayList<>();
		if(tile.treeIndexes != null) {
			for (int c = 0; c < tile.treeIndexes.size(); c++) {
				if (data.treeMeta.get(tile.treeIndexes.get(c)).biomes.contains(biomeId)) {
					if (canopy <= MIDDLE_LIMIT) {
						ind.addAll(data.treeMeta.get(tile.treeIndexes.get(c)).treeS);
					} else {
						ind.addAll(data.treeMeta.get(tile.treeIndexes.get(c)).treeL);
					}
				}
			}
		}

		return ind;
	}


	private TreeRepoData readFromDat() throws IOException, ClassNotFoundException {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("assets/terracustomtreerepo/customtreerepo.dat");
		ObjectInputStream inputStream = new ObjectInputStream(stream);
		TreeRepoData dat = (TreeRepoData)inputStream.readObject();
		inputStream.close();
		TerraTreeRepoMod.LOGGER.info("Loaded " + dat.trees.size() + " tree schematics");
		return dat;
	}

	/**
	 * Loads all schematics of trees from assets and tree map. Only to be used for
	 * development purposes, to create and import the customtreerepo.dat file
	 */
	private void readFromAssets(boolean readSchems) {
		if(readSchems) {
			// The schematic index
			SchematicOffset[] treeList = { new SchematicOffset("european/EUR10L - EU Holly.schematic", 3, 0, 4, 3),
					new SchematicOffset("european/EUR17S - White Willow.schematic", 4, 0, 5, 3),
					new SchematicOffset("european/EUR3S - Silver Birch.schematic", 5, 0, 6, 4),
					new SchematicOffset("european/EUR10S - EU Holly.schematic", 2, 0, 2, 4),
					new SchematicOffset("european/EUR18L - Maritime Pine.schematic", 4, 3, 5, 4),
					new SchematicOffset("european/EUR4L - EU Beech.schematic", 13, 3, 9, 6),
					new SchematicOffset("european/EUR11L - Common Juniper.schematic", 3, 0, 4, 2),
					new SchematicOffset("european/EUR18S - Maritime Pine.schematic", 4, 0, 7, 3),
					new SchematicOffset("european/EUR4S - EU Beech.schematic", 6, 0, 5, 4),
					new SchematicOffset("european/EUR11S - Common Juniper.schematic", 4, 0, 4, 2),
					new SchematicOffset("european/EUR19L - EU Black Pine.schematic", 4, 0, 6, 3),
					new SchematicOffset("european/EUR5L - Blackthorn.schematic", 3, 0, 3, 3),
					new SchematicOffset("european/EUR12L - Linden.schematic", 6, 3, 7, 4),
					new SchematicOffset("european/EUR19S - EU Black Pine.schematic", 3, 0, 3, 2),
					new SchematicOffset("european/EUR5S - Blackthorn.schematic", 2, 0, 2, 2),
					new SchematicOffset("european/EUR12S - Linden.schematic", 5, 0, 8, 4),
					new SchematicOffset("european/EUR1L - Alder.schematic", 9, 3, 10, 6),
					new SchematicOffset("european/EUR6L - Smooth Leaved Elm.schematic", 10, 0, 10, 5),
					new SchematicOffset("european/EUR13L - Sesille Oak.schematic", 12, 3, 12, 7),
					new SchematicOffset("european/EUR1S - Alder.schematic", 7, 0, 6, 4),
					new SchematicOffset("european/EUR6S - Smooth Leaved Elm.schematic", 7, 3, 6, 4),
					new SchematicOffset("european/EUR13S - Sesille Oak.schematic", 7, 3, 8, 4),
					new SchematicOffset("european/EUR20L - Norway Spruce.schematic", 6, 0, 6, 6),
					new SchematicOffset("european/EUR7L - Common Hawthorn.schematic", 7, 0, 9, 5),
					new SchematicOffset("european/EUR14L - Scots Pine.schematic", 9, 0, 11, 6),
					new SchematicOffset("european/EUR20S - Norway Spruce.schematic", 4, 0, 4, 3),
					new SchematicOffset("european/EUR7S - Common Hawthorn.schematic", 5, 3, 5, 3),
					new SchematicOffset("european/EUR14S - Scots Pine.schematic", 5, 0, 4, 3),
					new SchematicOffset("european/EUR21L - EU Larch.schematic", 7, 0, 7, 4),
					new SchematicOffset("european/EUR8L - Hazel.schematic", 3, 3, 3, 3),
					new SchematicOffset("european/EUR15L - Aspen.schematic", 5, 0, 6, 3),
					new SchematicOffset("european/EUR21S - EU Larch.schematic", 6, 0, 6, 4),
					new SchematicOffset("european/EUR8S - Hazel.schematic", 3, 3, 2, 3),
					new SchematicOffset("european/EUR15S - Aspen.schematic", 4, 0, 4, 3),
					new SchematicOffset("european/EUR2L - Common Ash.schematic", 11, 3, 13, 6),
					new SchematicOffset("european/EUR9L - EU Hornbeam.schematic", 8, 0, 9, 5),
					new SchematicOffset("european/EUR16 - EU Rowan.schematic", 4, 3, 4, 2),
					new SchematicOffset("european/EUR2S - Common Ash.schematic", 6, 3, 8, 4),
					new SchematicOffset("european/EUR9S - EU Hornbeam.schematic", 6, 0, 5, 3),
					new SchematicOffset("european/EUR17L - White Willow.schematic", 7, 3, 8, 6),
					new SchematicOffset("european/EUR3L - Silver Birch.schematic", 9, 0, 7, 4),
					new SchematicOffset("african/AFR10L - Tamboti.schematic", 6, 3, 7, 4),
					new SchematicOffset("african/AFR1S - Grandidier's Baobab.schematic", 7, 3, 5, 5),
					new SchematicOffset("african/AFR6L - Knobthorn.schematic", 11, 3, 11, 9),
					new SchematicOffset("african/AFR10S - Tamboti.schematic", 4, 0, 4, 3),
					new SchematicOffset("african/AFR2L - African Baobab.schematic", 7, 3, 11, 8),
					new SchematicOffset("african/AFR6S - Knobthorn.schematic", 6, 3, 7, 9),
					new SchematicOffset("african/AFR11L - Umbrella Thorn.schematic", 15, 3, 14, 8),
					new SchematicOffset("african/AFR2S - African Baobab.schematic", 10, 3, 8, 8),
					new SchematicOffset("african/AFR7L - Marula.schematic", 16, 3, 8, 12),
					new SchematicOffset("african/AFR11S - Umbrella Thorn.schematic", 10, 3, 11, 6),
					new SchematicOffset("african/AFR3L - Buffalo Thorn.schematic", 5, 0, 7, 4),
					new SchematicOffset("african/AFR7S - Marula.schematic", 6, 0, 7, 5),
					new SchematicOffset("african/AFR12L - Huilboerboon.schematic", 6, 3, 6, 4),
					new SchematicOffset("african/AFR3S - Buffalo Thorn.schematic", 4, 0, 4, 4),
					new SchematicOffset("african/AFR8L - Mopane.schematic", 12, 3, 13, 9),
					new SchematicOffset("african/AFR12S - Huilboerboon.schematic", 3, 0, 3, 4),
					new SchematicOffset("african/AFR4L - Bushwillow.schematic", 4, 3, 4, 6),
					new SchematicOffset("african/AFR8S - Mopane.schematic", 8, 3, 8, 7),
					new SchematicOffset("african/AFR13L - Candelabra.schematic", 3, 0, 3, 4),
					new SchematicOffset("african/AFR4S - Bushwillow.schematic", 4, 0, 4, 5),
					new SchematicOffset("african/AFR9L - Sausage Tree.schematic", 11, 3, 10, 9),
					new SchematicOffset("african/AFR13S - Candelabra.schematic", 2, 0, 2, 3),
					new SchematicOffset("african/AFR5L - Jackalberry.schematic", 12, 3, 11, 9),
					new SchematicOffset("african/AFR9S - Sausage Tree.schematic", 8, 3, 7, 6),
					new SchematicOffset("african/AFR1L - Grandidier's Baobab.schematic", 9, 3, 8, 8),
					new SchematicOffset("african/AFR5S - Jackalberry.schematic", 8, 3, 7, 5),
					new SchematicOffset("southamerican/SA10L - Brazil Nut.schematic", 12, 3, 12, 8),
					new SchematicOffset("southamerican/SA1S - Soncoya.schematic", 5, 0, 4, 4),
					new SchematicOffset("southamerican/SA10S - Brazil Nut.schematic", 7, 3, 5, 5),
					new SchematicOffset("southamerican/SA20L - Cashapona.schematic", 6, 3, 6, 5),
					new SchematicOffset("southamerican/SA11L - Cinchona Ledgeriana.schematic", 8, 0, 9, 6),
					new SchematicOffset("southamerican/SA20S - Cashapona.schematic", 5, 3, 4, 4),
					new SchematicOffset("southamerican/SA11S - Cinchona Ledgeriana.schematic", 5, 0, 6, 4),
					new SchematicOffset("southamerican/SA21L - Theobroma Cacao.schematic", 6, 0, 6, 5),
					new SchematicOffset("southamerican/SA12L - Hancornia Speciosa.schematic", 4, 0, 5, 4),
					new SchematicOffset("southamerican/SA21S - Theobroma Cacao.schematic", 4, 0, 4, 4),
					new SchematicOffset("southamerican/SA12S - Hancornia Speciosa.schematic", 3, 0, 4, 4),
					new SchematicOffset("southamerican/SA2L - Parica.schematic", 11, 3, 10, 9),
					new SchematicOffset("southamerican/SA13L - Rubbertree.schematic", 13, 3, 11, 10),
					new SchematicOffset("southamerican/SA2S - Parica.schematic",5, 3, 6, 8),
					new SchematicOffset("southamerican/SA13S - Rubbertree.schematic",8, 0, 6, 5),
					new SchematicOffset("southamerican/SA3L - Fiberpalm.schematic", 6, 0, 6, 5),
					new SchematicOffset("southamerican/SA14L - Sandbox Tree.schematic", 11, 3, 10, 6),
					new SchematicOffset("southamerican/SA3S - Fiberpalm.schematic", 3, 0, 3, 3),
					new SchematicOffset("southamerican/SA14S - Sandbox Tree.schematic", 10, 3, 9, 8),
					new SchematicOffset("southamerican/SA4L - Astrocaryum Jauari.schematic", 6, 0, 6, 4),
					new SchematicOffset("southamerican/SA15L - Leopoldinia Piassaba.schematic", 3, 0, 3, 3),
					new SchematicOffset("southamerican/SA4S - Astrocaryum Jauari.schematic", 6, 0, 6, 4),
					new SchematicOffset("southamerican/SA15S - Leopoldinia Piassaba.schematic", 3, 0, 3, 3),
					new SchematicOffset("southamerican/SA5L - Awarra.schematic", 6, 0, 6, 4),
					new SchematicOffset("southamerican/SA16L - Myrciaria Dubia.schematic", 4, 3, 5, 4),
					new SchematicOffset("southamerican/SA5S - Awarra.schematic", 5, 0, 5, 4),
					new SchematicOffset("southamerican/SA16S - Myrciaria Dubia.schematic", 2, 0, 2, 5),
					new SchematicOffset("southamerican/SA6L - Astronium Fraxinifolium.schematic", 8, 3, 7, 5),
					new SchematicOffset("southamerican/SA17L - Maripa Palm.schematic", 6, 0, 6, 5),
					new SchematicOffset("southamerican/SA6S - Astronium Fraxinifolium.schematic", 5, 0, 4, 4),
					new SchematicOffset("southamerican/SA17S - Maripa Palm.schematic", 6, 0, 6, 4),
					new SchematicOffset("southamerican/SA7L - Astronium Lecointei.schematic", 8, 0, 8, 5),
					new SchematicOffset("southamerican/SA18L - Platypodium Elegans.schematic", 11, 3, 11, 8),
					new SchematicOffset("southamerican/SA7S - Astronium Lecointei.schematic", 4, 0, 5, 4),
					new SchematicOffset("southamerican/SA18S - Platypodium Elegans.schematic", 10, 3, 8, 10),
					new SchematicOffset("southamerican/SA8L - Babassu Palm.schematic", 4, 0, 4, 4),
					new SchematicOffset("southamerican/SA19L - Yagrumo Macho.schematic", 11, 3, 7, 8),
					new SchematicOffset("southamerican/SA8S - Babassu Palm.schematic", 3, 0, 3, 3),
					new SchematicOffset("southamerican/SA19S - Yagrumo Macho.schematic", 7, 3, 6, 5),
					new SchematicOffset("southamerican/SA9L - Peach Palm.schematic", 5, 0, 5, 4),
					new SchematicOffset("southamerican/SA1L - Soncoya.schematic", 8, 0, 8, 6),
					new SchematicOffset("southamerican/SA9S - Peach Palm.schematic", 5, 0, 5, 4),
					new SchematicOffset("northamerican/NA10L - White Oak.schematic", 12, 3, 11, 8),
					new SchematicOffset("northamerican/NA20L - Fraser Fir.schematic", 4, 0, 5, 4),
					new SchematicOffset("northamerican/NA10S - White Oak.schematic", 6, 0, 5, 4),
					new SchematicOffset("northamerican/NA20S - Fraser Fir.schematic", 3, 0, 3, 5),
					new SchematicOffset("northamerican/NA11L - Red Alder.schematic", 10, 3, 6, 5),
					new SchematicOffset("northamerican/NA21L - Grand Fir.schematic", 9, 3, 7, 5),
					new SchematicOffset("northamerican/NA11S - Red Alder.schematic", 6, 0, 6, 5),
					new SchematicOffset("northamerican/NA21S - Grand Fir.schematic", 6, 0, 6, 5),
					new SchematicOffset("northamerican/NA12L - Green Ash.schematic", 9, 3, 9, 5),
					new SchematicOffset("northamerican/NA2L - Sugar Maple.schematic", 15, 3, 12, 6),
					new SchematicOffset("northamerican/NA12S - Green Ash.schematic", 6, 0, 6, 5),
					new SchematicOffset("northamerican/NA2S - Sugar Maple.schematic", 6, 0, 6, 5),
					new SchematicOffset("northamerican/NA13L - American Beech.schematic", 13, 3, 14, 6),
					new SchematicOffset("northamerican/NA3L - Loblolly Pine.schematic", 6, 0, 6, 4),
					new SchematicOffset("northamerican/NA13S - American Beech.schematic", 6, 3, 7, 7),
					new SchematicOffset("northamerican/NA3S - Loblolly Pine.schematic", 5, 0, 4, 4),
					new SchematicOffset("northamerican/NA14L - American Basswood.schematic", 8, 3, 7, 6),
					new SchematicOffset("northamerican/NA4L - Sweetgum.schematic", 6, 0, 6, 5),
					new SchematicOffset("northamerican/NA14S - American Basswood.schematic", 6, 0, 6, 6),
					new SchematicOffset("northamerican/NA4S - Sweetgum.schematic", 4, 0, 4, 4),
					new SchematicOffset("northamerican/NA15L - Paper Birch.schematic", 4, 0, 4, 4),
					new SchematicOffset("northamerican/NA5L - Balsam Fir.schematic", 7, 0, 7, 9),
					new SchematicOffset("northamerican/NA15S - Paper Birch.schematic", 3, 0, 3, 4),
					new SchematicOffset("northamerican/NA5S - Balsam Fir.schematic", 4, 0, 4, 6),
					new SchematicOffset("northamerican/NA16L - Baldcypress.schematic", 9, 3, 9, 6),
					new SchematicOffset("northamerican/NA6L - Douglas Fir.schematic", 8, 3, 9, 5),
					new SchematicOffset("northamerican/NA16S - Baldcypress.schematic", 4, 3, 5, 4),
					new SchematicOffset("northamerican/NA6S - Douglas Fir.schematic", 5, 3, 5, 7),
					new SchematicOffset("northamerican/NA17L - Alaska Cedar.schematic", 6, 3, 6, 5),
					new SchematicOffset("northamerican/NA6XL - Douglas Fir.schematic", 9, 3, 11, 5),
					new SchematicOffset("northamerican/NA17S - Alaska Cedar.schematic", 5, 0, 5, 5),
					new SchematicOffset("northamerican/NA7L - Quaking Aspen.schematic", 4, 0, 4, 4),
					new SchematicOffset("northamerican/NA18L - Atlantic White Cedar.schematic", 8, 3, 9, 5),
					new SchematicOffset("northamerican/NA7S - Quaking Aspen.schematic", 3, 0, 3, 4),
					new SchematicOffset("northamerican/NA18S - Atlantic White Cedar.schematic", 6, 3, 5, 8),
					new SchematicOffset("northamerican/NA8L - Flowering Dogwood.schematic", 6, 0, 6, 5),
					new SchematicOffset("northamerican/NA19L - Californian Red Fir.schematic", 7, 3, 9, 6),
					new SchematicOffset("northamerican/NA8S - Flowering Dogwood.schematic", 4, 0, 4, 5),
					new SchematicOffset("northamerican/NA19S - Californian Red Fir.schematic", 7, 3, 6, 6),
					new SchematicOffset("northamerican/NA9L - Lodgepole Pine.schematic", 6, 3, 7, 5),
					new SchematicOffset("northamerican/NA1L - Red Maple.schematic", 6, 0, 6, 6),
					new SchematicOffset("northamerican/NA9S - Lodgepole Pine.schematic", 6, 3, 6, 7),
					new SchematicOffset("northamerican/NA1S - Red Maple.schematic", 4, 0, 3, 4) };

			this.data.trees = new ArrayList<>();

			for (int l = 0; l < treeList.length; l++) {
				InputStream stream = this.getClass().getClassLoader()
						.getResourceAsStream("assets/terracustomtreerepo/" + treeList[l].getSchematicName());
				try {
					this.data.trees.add(Schematic.loadSchematic(stream, treeList[l].getOffsetX(), treeList[l].getOffsetY(),treeList[l].getOffsetZ(), treeList[l].getRadius()));
				} catch (IOException ex) {
					TerraMod.LOGGER.error(ex.getMessage());
				}
			}

		}

		try {
			this.data.treeMeta = readTreeMeta();

			//Write customtreerepo.dat to .minecraft folder
			ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream("customtreerepo.dat"));
			outStream.writeObject(this.data);
			outStream.close();

		}catch(IOException ex) {
			TerraMod.LOGGER.error("Error while creating customtreerepo.dat");
			ex.printStackTrace();
		}
	}

	/**
	 * Loads the tree metadata into a List of TreeBiomes from the tree_meta.csv file. Only to be used for
	 * development purposes, to import and create the customtreerepo.dat file
	 * @return
	 * @throws IOException
	 */
	public List<TreeBiome> readTreeMeta() throws IOException {
		List<TreeBiome> out = new ArrayList<TreeBiome>();
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("assets/terracustomtreerepo/tree_meta.csv");
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

		boolean skipFirst = true;
		while(reader.ready()) {
			if(skipFirst == false) {
				String rawData = reader.readLine();
				String[] data = rawData.split(";");
				TreeBiome tmp = new TreeBiome();
				tmp.continents = parseToIntegerList(data[1].trim());
				tmp.treeL = parseToIntegerList(data[2].trim());
				tmp.treeS = parseToIntegerList(data[3].trim());
				tmp.biomes = parseToIntegerList(data[4].trim());
				tmp.climate = parseToIntegerList(data[5].trim());
				out.add(tmp);
			}else {
				reader.readLine();
				skipFirst = false;
			}

		}

		reader.close();

		return out;
	}


	/**
	 * Parses a string containing numbers separated by a comma into a Integer list. Only to be used for
	 * development purposes, to import and create the customtreerepo.dat file
	 * @param data The string containing comma separated numbers
	 * @return A list of Integers
	 */
	public List<Integer> parseToIntegerList(String data){
		List<Integer> out = new ArrayList<Integer>();
		if(data.contains(",")){
			String[] p = data.split(",");
			for(int i = 0; i < p.length; i++){
				out.add(Integer.parseInt(p[i].trim()));
			}
		}else{
			out.add(Integer.parseInt(data.trim()));
		}
		return out;
	}
}
