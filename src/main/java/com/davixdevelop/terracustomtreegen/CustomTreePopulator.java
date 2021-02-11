package com.davixdevelop.terracustomtreegen;

import com.davixdevelop.terracustomtreegen.baker.SegmentsBaker;
import com.davixdevelop.terracustomtreegen.repo.CustomTreeRepository;
import com.davixdevelop.terracustomtreegen.schematic.Schematic;
import com.google.common.collect.ImmutableSet;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorldServer;
import net.buildtheearth.terraplusplus.dep.net.daporkchop.lib.common.ref.Ref;
import net.buildtheearth.terraplusplus.dep.net.daporkchop.lib.common.ref.ThreadRef;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.generator.EarthGenerator;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorPipelines;
import net.buildtheearth.terraplusplus.generator.data.TreeCoverBaker;
import net.buildtheearth.terraplusplus.generator.populate.IEarthPopulator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

import java.util.*;

public class CustomTreePopulator implements IEarthPopulator {
	
	public static final Set<Block> EXTRA_SURFACE = ImmutableSet.of(
            Blocks.SAND,
            Blocks.SANDSTONE,
            Blocks.RED_SANDSTONE,
            Blocks.CLAY,
            Blocks.HARDENED_CLAY,
            Blocks.STAINED_HARDENED_CLAY,
            Blocks.SNOW,
            Blocks.MYCELIUM);

	public static final Set<Block> WOOD_BLOCKS = ImmutableSet.of(
			Blocks.LOG,
			Blocks.LOG2,
			Blocks.OAK_FENCE,
			Blocks.BIRCH_FENCE,
			Blocks.SPRUCE_FENCE,
			Blocks.ACACIA_FENCE,
			Blocks.JUNGLE_FENCE,
			Blocks.DARK_OAK_FENCE
	);

	protected static final Ref<byte[]> RNG_CACHE = ThreadRef.soft(() -> new byte[16 * 16]);
	
	public static final CustomTreeRepository TREE_REPO = new CustomTreeRepository();
	
	protected EarthGenerator generator;
	
	@Override
	public void populate(World world, Random random, CubePos pos, Biome biome, CachedChunkData data) {
		if(!data.intersectsSurface(pos.getY())) {
			return;
		}
		
		if(generator == null) {
    		generator = (EarthGenerator) ((ICubicWorldServer) world).getCubeGenerator();
    	}
		
		byte[] treeCover = data.getCustom(EarthGeneratorPipelines.KEY_DATA_TREE_COVER, TreeCoverBaker.FALLBACK_TREE_DENSITY);
		
		byte[] rng = RNG_CACHE.get();
		random.nextBytes(rng);
		
		List<CustomTreeGen> trees  = new ArrayList<>();

		for (int i = 0, x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++, i++) {
                if ((rng[i] & 0xFF) < (treeCover[i] & 0xFF)) {

                	/*BlockPos blockPos = ((ICubicWorld) world).getSurfaceForCube(pos, x, z, 0, ICubicWorld.SurfaceType.OPAQUE);
                	if(blockPos != null)
                		trees.add(new CustomTreeGen((treeCover[i] / 1.50d) / 255.0d, blockPos));*/

                	BlockPos blockPos = new BlockPos(pos.getMinBlockX() + x, quickElev(world, pos.getMinBlockX() + x, pos.getMinBlockZ() + z, pos.getMinBlockY() - 1, pos.getMaxBlockY() + 1) + 1, pos.getMinBlockZ() + z);
                	trees.add(new CustomTreeGen((treeCover[i] / 1.50d) / 255.0d, blockPos));
                }
            }
        }


		if(trees.size() > 0) 
			trees = TREE_REPO.getTreeGenerators(trees, world, biome, generator.projection, pos, random);


		if(trees.size() > 8)
			trees = this.treeBounds(trees, random, world, pos, 1, -1);


		if(trees.size() > 0) {

			Set<SegmentLinearFunc> roads = data.getCustom(SegmentsBaker.KEY_CUSTOM_TREE_REPO_ROAD_SEGMENTS, SegmentsBaker.FALLBACK_CUSTOM_TREE_REPO_SEGMENTS);
			Set<SegmentLinearFunc> freeways = data.getCustom(SegmentsBaker.KEY_CUSTOM_TREE_REPO_FREEWAY_SEGMENTS, SegmentsBaker.FALLBACK_CUSTOM_TREE_REPO_SEGMENTS);
			Set<SegmentLinearFunc> paths = data.getCustom(SegmentsBaker.KEY_CUSTOM_TREE_REPO_PATH_SEGMENTS, SegmentsBaker.FALLBACK_CUSTOM_TREE_REPO_SEGMENTS);
			Set<Set<SegmentLinearFunc>> buildings = data.getCustom(SegmentsBaker.KEY_CUSTOM_TREE_REPO_BUILDING_SEGMENTS, SegmentsBaker.FALLBACK_CUSTOM_TREE_REPO_POLYGONS);


			if(roads.size() > 0) {
				trees = offsetTrees(trees, roads, Blocks.CONCRETE, 2, 5,1, world, pos);
        	}


        	if(paths.size() > 0) {
        		trees = offsetTrees(trees, paths, Blocks.GRASS_PATH, 1, 2,1, world, pos);
        	}
        	
        	if(freeways.size() > 0) {
        		trees = offsetTrees(trees, freeways, Blocks.CONCRETE, 8, 9,1, world, pos);
        	}
        	
        	if(buildings.size() > 0) {
        		trees = checkForBuildings(trees, buildings, pos);
        	}
		}
		
		if(trees.size() > 0) {
			for(int i = 0; i < trees.size(); ++i)
				this.tryPlace(world, random, pos, trees.get(i));
		}

	}
	
	protected void tryPlace(World world, Random random, CubePos pos, CustomTreeGen gen) {
        if (gen.top1 != null && this.canPlaceAt(world, gen.top1)) {
            this.placeTree(world, random, gen);
        }
    }

    protected boolean canPlaceAt(World world, BlockPos pos) {
        BlockPos down = pos.down();
        IBlockState state = world.getBlockState(down);

        if(state.getBlock() == Blocks.WATER)
			return false;

        if (state.getBlock() != Blocks.GRASS && state.getBlock() != Blocks.DIRT) {
            //plant a bit of dirt to make sure trees spawn when they are supposed to even in certain hostile environments
            if (!this.isSurfaceBlock(world, down, state)) {
                return false;
            }
            world.setBlockState(down, Blocks.GRASS.getDefaultState());
        }

        return true;
    }

    protected boolean isSurfaceBlock(World world, BlockPos pos, IBlockState state) {
        return EXTRA_SURFACE.contains(state.getBlock());
    }

    protected void placeTree(World world, Random random, CustomTreeGen gen) {
    	if(gen.treeGen instanceof WorldGenAbstractTree) {
    		((WorldGenAbstractTree)gen.treeGen).setDecorationDefaults();

            if (((WorldGenAbstractTree)gen.treeGen).generate(world, random, gen.top1)) {
            	((WorldGenAbstractTree)gen.treeGen).generateSaplings(world, random, gen.top1);
            }
    	}else {
    		BlockPos blockPos = new BlockPos(gen.top1.getX(), gen.top1.getY(), gen.top1.getZ());
    		IBlockState state = world.getBlockState(blockPos.down());
    		if(state.getBlock() == Blocks.GRASS || EXTRA_SURFACE.contains(state.getBlock()))
				Schematic.pasteSchematic(world, gen.top1.getX(), gen.top1.getY(), gen.top1.getZ(), (Schematic)gen.treeGen, random.nextInt(4));
    	}
    }

    protected  BlockPos surfaceBlock(int x, int z, World world, CubePos pos){
		return new BlockPos(x, this.quickElev(world, x, z, pos.getMinBlockY() - 10, pos.getMaxBlockY() + 10) + 1, z);
		/*
		if(x < pos.getMinBlockX() || x > pos.getMaxBlockX() || z < pos.getMinBlockZ() || z > pos.getMaxBlockZ()) {
			return new BlockPos(x, this.quickElev(world, x, z, pos.getMinBlockY() - 10, pos.getMaxBlockY() +10) + 1, z);
		}

		int offsetX = Math.abs(x - pos.getMinBlockX());
		int offsetZ = Math.abs(z - pos.getMinBlockZ());

		BlockPos blockPos =  ((ICubicWorld) world).getSurfaceForCube(pos, offsetX, offsetZ, 0, ICubicWorld.SurfaceType.OPAQUE);
		if(blockPos == null)
			return new BlockPos(x, this.quickElev(world, x, z, pos.getMinBlockY() - 10, pos.getMaxBlockY()+ 10) + 1, z);
		else
			return blockPos;

		 */
	}
    
    protected int quickElev(World world, int x, int z, int low, int high) {
        high++;

        while (low < high - 1) {
            int y = low + (high - low) / 2;
            if (Schematic.ALLOWED_BLOCK.contains(world.getBlockState(new BlockPos(x, y, z)).getBlock())) {
                high = y;
            } else {
                low = y;
            }
        }

        return low;
    }


    /**
     * Checks if trees canopy spread interact, and if they do, remove them
     * @param  trees List of trees
     * @param  random Java random
	 * @param  world The Minecraft world
	 * @param  pos The cube position
	 * @param positiveOffset The offset between trees
	 * @param negativeOffset The negative offset to decrease the scan are around the tree
	 * @return Modified list of trees
     */
    protected List<CustomTreeGen> treeBounds(List<CustomTreeGen> trees, Random random, World world, CubePos pos, int positiveOffset, int negativeOffset) {
		Collections.shuffle(trees);
		try {
			List<CustomTreeGen> diff = new ArrayList<>();

			for(Iterator<CustomTreeGen> ai = trees.iterator(); ai.hasNext();){
				CustomTreeGen a = ai.next();

				boolean add = true;
				boolean vacinity = false;

				for (CustomTreeGen b : trees){
					if(a != b){
						double difference = Math.sqrt(Math.pow(Math.abs(b.top1.getZ() - a.top1.getZ()), 2) + Math.pow(Math.abs(a.top1.getX() - b.top1.getX()), 2)) - a.getCanopyRadius() - b.getCanopyRadius();

						//(random.nextInt(4) * -1)
						if(difference < 2) {
							SegmentLinearFunc seg = new SegmentLinearFunc(new double[]{a.top1.getX(), a.top1.getZ()}, new double[]{b.top1.getX(), b.top1.getZ()}, 1);
							int[] A = {a.top1.getX(), a.top1.getZ()};
							int[] B = {b.top1.getX(), b.top1.getZ()};

							if(!seg.isConstantX() && !seg.isConstantY()) {
								//Distance between point A and point B
								double a3 = Math.sqrt(Math.pow(A[0] - B[0], 2) + Math.pow(A[1] - B[1] , 2));


								//Hypotenuse between point A and B
								double c = Math.abs(A[1] - B[1]);

								//Alpha angle in point T
								double alpha = Math.toDegrees(Math.acos(a3 / c));
								if(Double.isNaN(alpha))
									alpha = Math.toDegrees(Math.cos(a3 / c));

								//Distance between point B and new point A2
								double a1 = a.getCanopyRadius() + positiveOffset + b.getCanopyRadius();
								//New hypotenuse
								double c1 = a1 / Math.cos(Math.toRadians(alpha));


								double A2y = (A[1] > B[1]) ?  B[1] + c1 : B[1] - c1;
								double A2x = seg.getX(A2y);

								int actualX = (int)Math.round(A2x);
								int actualZ = (int)Math.round(A2y);

								//a.top1 = new BlockPos(actualX, this.quickElev(world, actualX, actualZ, pos.getMinBlockY() - 1, pos.getMaxBlockY()) + 1, actualZ);
								a.top1 = surfaceBlock(actualX, actualZ, world, pos);
								a.repositioned = true;
								vacinity = checkVicinity(a, world, negativeOffset);


							}else if(seg.isConstantX()) {
								double A2y = (A[1] > B[1]) ? B[1] + b.getCanopyRadius() + positiveOffset + a.getCanopyRadius() : B[1] - b.getCanopyRadius() - positiveOffset - a.getCanopyRadius();
								int actualX = A[0];
								int actualZ = (int)Math.round(A2y);
								//a.top1 = new BlockPos(actualX, this.quickElev(world, actualX, actualZ, pos.getMinBlockY() - 1, pos.getMaxBlockY()) + 1, actualZ);
								a.top1 = surfaceBlock(actualX, actualZ, world, pos);
								a.repositioned = true;
								vacinity = checkVicinity(a, world, negativeOffset);
							}else if(seg.isConstantY()) {
								double A2x = (A[0] > B[0]) ? B[0] + b.getCanopyRadius() + positiveOffset + a.getCanopyRadius() : B[0] - b.getCanopyRadius() - positiveOffset - a.getCanopyRadius();
								int actualX = (int)Math.round(A2x);
								int actualZ = A[1];
								//a.top1 = new BlockPos(actualX, this.quickElev(world, actualX, actualZ, pos.getMinBlockY() - 1, pos.getMaxBlockY()) + 1, actualZ);
								a.top1 = surfaceBlock(actualX, actualZ, world, pos);
								a.repositioned = true;
								vacinity = checkVicinity(a, world, negativeOffset);
							}

						}else if(a.repositioned && difference < positiveOffset || vacinity){
							add = false;
							break;
						}
					}
				}

				if(add)
					diff.add(a);

				ai.remove();
			}

			return diff;


		}catch(Exception ex) {
			ex.printStackTrace();
		}

    	return trees;
    }

	/**
	 * Check's if a log exists around the canopy area of the tree with a negative offset
	 * @param gen The tree gen
	 * @param world The Minecraft world
	 * @param negativeOffset The negative offset to decrease the scan are around the tree
	 * @return The result of the trees
	 */
    protected boolean checkVicinity(CustomTreeGen gen, World world, int negativeOffset){

    	int offset = gen.getCanopyRadius();
    	if(gen.getCanopyRadius() - negativeOffset > 0){
    		offset = gen.getCanopyRadius() - negativeOffset;
		}
		int rl = gen.top1.getX() + offset;
		int tl = gen.top1.getZ() + offset;

		int[] T = {gen.top1.getX(), gen.top1.getZ()};

		for(int x = gen.top1.getX() - offset; x <= rl; x++) {
			for(int z = gen.top1.getZ() - offset; z <= tl; z++) {
				int y_limit = gen.top1.getY() + 5;
				for(int y = gen.top1.getY(); y < y_limit; y++){

					double r = Math.sqrt(Math.pow(T[0] - x, 2) + Math.pow(T[1] - z , 2));

					//Point lies within canopy radius
					if(r <= offset){
						BlockPos g = new BlockPos(x, y, z);

						IBlockState state = world.getBlockState(g);

						//Check if block is log
						if(WOOD_BLOCKS.contains(state.getBlock())) {
							return true;
						}
					}
				}
			}
		}
    	return false;
	}
	
    /**
     * Checks if trees lies less than the offset from the segment and moves it back, specified by the positiveOffset and returns them
     * @param trees The input tree list
     * @param segments The input segments
     * @param segBlock The block of the segment
     * @param offset The check offset
     * @param positiveOffset The offset to push back trees
     * @param world The Minecraft world
     * @param pos The CubePos
     * @return List of trees
     */
    protected List<CustomTreeGen> offsetTrees(List<CustomTreeGen> trees, Set<SegmentLinearFunc> segments, Block segBlock, int offset, int positiveOffset, int negativeOffset , World world, CubePos pos) {
    	for(Iterator<CustomTreeGen> it = trees.iterator(); it.hasNext();) {
			CustomTreeGen gen = it.next();

			if(gen.top1 == null){
				it.remove();
				continue;
			}

			
			boolean removeTree = false;
			
			for(SegmentLinearFunc seg : segments) {

				if((gen.top1.getX() >= seg.getAx() && gen.top1.getX() <= seg.getBx())
						|| (gen.top1.getZ() <= seg.getBy() && gen.top1.getZ() >= seg.getAy())) {
					//Check if tree is within segment bounds
					int[] T = {gen.top1.getX(), gen.top1.getZ()};
					
					if(!seg.isConstantX() && !seg.isConstantY()) {
						//Remove tree is the rare chance that the tree lies directly on the middle of the road
    					if(seg.getX(T[1]) == T[0] && seg.getY(T[0]) == T[1]) {
    						removeTree = true;
    						break;
    					}
    					
    					//Slope perpendicular line r on line p
    					double k2 = - (1 / seg.getK());
    					//Section of ordinate from line r 
    					double n2 = T[1] - k2 * T[0];
    					
    					//Intersection of lines p and r
    					double Px = (n2 - seg.getN()) / (seg.getK() - k2);
    					double Py = seg.getK() * Px + seg.getN();
    					
    					//Distance between intersection P and point T
    					double a = Math.sqrt(Math.pow(T[0] - Px, 2) + Math.pow(T[1] - Py , 2));
    					
    					//If tree is situated less than 2m from the edge of the segment or lies on the road, move it back
    					if(a - seg.getR() - gen.getCanopyRadius() <= offset) {
    						
    						//Point R on line p
    						double[] R = {T[0], seg.getY(T[0])};
    						//Hypotenuse between point T and R
     						double c = Math.abs(T[1] - R[1]);
     						
     						//Alpha angle in point T
    						double alpha = Math.toDegrees(Math.acos(a / c));

							if(Double.isNaN(alpha))
								alpha = Math.toDegrees(Math.cos(a / c));
    						
    						//Distance between intersection P and point T2
    						double a1 = seg.getR() + positiveOffset + gen.getCanopyRadius();
    						//New hypotenuse
    						double c1 = a1 / Math.cos(Math.toRadians(alpha));
    						
    						//Section of ordinate from line q
    						double n3 = (T[1] > R[1]) ? seg.getN() + c1 : seg.getN() - c1;
    						
    						//Intersection between line r and q
    						double T2x = (n2 - n3) / (seg.getK() - k2);
    						double T2y = seg.getK() * T2x + n3;
    						
    						int actualX = (int)Math.round(T2x);
    						int actualZ = (int)Math.round(T2y);
    						
    						//gen.top1 = new BlockPos(actualX, this.quickElev(world, actualX, actualZ, pos.getMinBlockY() - 1, pos.getMaxBlockY()) + 1, actualZ);
							gen.top1 = surfaceBlock(actualX, actualZ, world, pos);
							gen.movedBack = true;

							if(checkVicinity(gen, world, negativeOffset))
								removeTree = true;

    						continue;
    					}
					}else if(seg.isConstantY()) {
						double Py = seg.getY(0);
						if(Math.abs(T[1] - Py) - gen.getCanopyRadius() <= offset) {
							double T2y = (Py < T[1]) ? Py + seg.getR() + positiveOffset + gen.getCanopyRadius() : Py - seg.getR() - positiveOffset - gen.getCanopyRadius();
							
							int actualX = T[0];
							int actualZ = (int)Math.round(T2y);

							gen.top1 = surfaceBlock(actualX, actualZ, world, pos);
							gen.movedBack = true;

							if(checkVicinity(gen, world, negativeOffset))
								removeTree = true;

    						continue;
						}
						
					}else if(seg.isConstantX()) {
						double Px = seg.getX(0);
						if(Math.abs(T[0] - Px) - gen.getCanopyRadius() <= offset) {
							double T2x = (Px < T[0]) ? Px + seg.getR() + positiveOffset + gen.getCanopyRadius() : Px - seg.getR() - positiveOffset - gen.getCanopyRadius();
							
							int actualX = (int)Math.round(T2x);
							int actualZ = T[1];

							gen.top1 = surfaceBlock(actualX, actualZ, world, pos);
							gen.movedBack = true;

							if(checkVicinity(gen, world, negativeOffset))
								removeTree = true;

    						continue;
						}
					}
				}
			}
			
			if(removeTree) {
				it.remove();
				continue;
			}

			//Secondary check around tree canopy if tree lies less then the offset from the edge
			//This is for cases, where s tree lies on the corner and we check for neighboring roads
			int[] T = {gen.top1.getX(), gen.top1.getZ()};

			int rl = gen.top1.getX() + gen.getCanopyRadius() + offset;
			int tl = gen.top1.getZ() + gen.getCanopyRadius() + offset;

			double[] P = {0,0};
			double r1 = 0; //min radius

			for(int x = gen.top1.getX() - gen.getCanopyRadius() - offset; x <= rl; x++) {
				for(int z = gen.top1.getZ() - gen.getCanopyRadius() - offset; z <= tl; z++) {
					double r = Math.sqrt(Math.pow(T[0] - x, 2) + Math.pow(T[1] - z , 2));

					//Point lies within canopy radius
					if(r <= gen.getCanopyRadius()) {
						BlockPos g = new BlockPos(x, this.quickElev(world, x, z, pos.getMinBlockY() - 10, pos.getMaxBlockY() + 10), z);

						IBlockState state = world.getBlockState(g);

						//Check if block is segBlock
						if(state.getBlock() == segBlock) {

							//Min radius
							if(r < r1 || r1 == 0) {
								r1 = r;
								P = new double[] {x,z};
							}else if(r == r1) { //If the radius is the same, set x1 and y1 to the midpoint between the two points
								P = new double[] {(P[0] + x) / 2,(P[1] + z) / 2};
							}
						}

					}
				}
			}


			if(P[0] != 0 && P[1] != 0 && (T[0] != P[0] || T[1] != P[1])) {
				//Line between road edge and tree

				if(T[0] != P[0] && T[1] != P[1]) {
					double k = (T[1] - P[1]) / (T[0] - P[0]);

					double n = P[1] - k * P[0];

					double a = Math.sqrt(Math.pow(T[0] - P[0], 2) + Math.pow(T[1] - P[1] , 2));

					//Check if distance between tree and road edge is less than 2m
					if(a - gen.getCanopyRadius() <= offset) {
						/*
						double a3 = Math.abs(T[1] - P[1]);
						double alpha = Math.toDegrees(Math.acos(a3 / a));

						double a2 = positiveOffset + gen.getCanopyRadius();
						double c1 = a2 / Math.cos(Math.toRadians(alpha));

						double T2y = (P[1] < T[1]) ? P[1] + c1 : P[1] - c1;
						double T2x = (T2y - n) / k;

						int actualX = (int)Math.round(T2x);

						int actualZ = (int)Math.round(T2y);

						gen.top1 = new BlockPos(actualX, this.quickElev(world, actualX, actualZ, pos.getMinBlockY() - 1, pos.getMaxBlockY()) + 1, actualZ);
						gen.movedBack = true;

						if(checkVicinity(gen, world, negativeOffset))
							it.remove();
						 */

						it.remove();
						continue;
					}
				} else if(T[1] == P[1]) { //Constant y

					if(Math.abs(T[0] - P[0]) - gen.getCanopyRadius() <= offset) {
						/*
						double T2x = (P[0] < T[0]) ? P[0] + positiveOffset + gen.getCanopyRadius() : P[0] - positiveOffset - gen.getCanopyRadius();

						int actualX = (int)Math.round(T2x);
						int actualZ = T[1];

						gen.top1 = new BlockPos(actualX, this.quickElev(world, actualX, actualZ, pos.getMinBlockY() - 1, pos.getMaxBlockY()) + 1, actualZ);
						gen.movedBack = true;

						if(checkVicinity(gen, world, negativeOffset))
							it.remove();
						 */

						it.remove();
						continue;
					}

				} else if(T[0] == P[0]) { //Constant x

					if(Math.abs(T[1] - P[1]) - gen.getCanopyRadius() <= offset) {
						/*
						double T2y = (P[1] < T[1]) ? P[1] + positiveOffset + gen.getCanopyRadius() : P[1] - positiveOffset + gen.getCanopyRadius();

						int actualX = T[0];
						int actualZ = (int)Math.round(T2y);

						gen.top1 = new BlockPos(actualX, this.quickElev(world, actualX, actualZ, pos.getMinBlockY() - 1, pos.getMaxBlockY()) + 1, actualZ);
						gen.movedBack = true;


						if(checkVicinity(gen, world, negativeOffset))
							it.remove();

						 */

						it.remove();
						continue;
					}

				}

			}else if(T[0] == P[0] && T[1] == P[1]) {
				//Remove tree if lies on segment
				it.remove();
				continue;
			}

		}
    	return trees;
    }
    
    /**
     * Check if trees are inside building, and remove them if the do
     * @param trees List of trees
     * @param buildings Set of buildings
     * @return List of trees
     */
    protected List<CustomTreeGen> checkForBuildings(List<CustomTreeGen> trees, Set<Set<SegmentLinearFunc>> buildings, CubePos pos) {
    	//buildings = closeBuildings(buildings, pos);
    	
    	for(Iterator<CustomTreeGen> it = trees.iterator(); it.hasNext();) {
			CustomTreeGen gen = it.next();

			for(Set<SegmentLinearFunc> building : buildings) {
				int count = 0;
				for(SegmentLinearFunc seg : building) {
					if(!seg.isConstantX()) {
						double Px = seg.getX(gen.top1.getZ());
						
						if(gen.top1.getX() <= Px)
							count++;
					}else {
						if(gen.top1.getX() <= seg.getConstantX())
							count++;
					}
				}
				
				if(count % 2 != 0) {
					it.remove();
					break;
				}
			}
    	}
    	
    	
    	return trees;
    }
}
