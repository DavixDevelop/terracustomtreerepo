package com.davixdevelop.terracustomtreegen.schematic;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import com.davixdevelop.terracustomtreegen.CustomTreePopulator;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.Collections;

import net.buildtheearth.terraplusplus.TerraMod;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

/**
 * Represents a schematic, with methods for reading and pasting.
 * Optimized for block ID's of Minecraft 1.12.2
 * @author DavixDevelop
 *
 */
public class Schematic implements java.io.Serializable {
	public static final Set<Block> ALLOWED_BLOCK = ImmutableSet.of(
            Blocks.AIR,
            Blocks.TALLGRASS,
            Blocks.BROWN_MUSHROOM,
            Blocks.RED_MUSHROOM,
			Blocks.DEADBUSH,
			Blocks.YELLOW_FLOWER,
			Blocks.RED_FLOWER,
			Blocks.DOUBLE_PLANT);
	
	
	private int[] blocks;
    private int[] data;
    private short width;
    private short length;
    private short height;
    private short offsetX;
    private short offsetY;
    private short offsetZ;
    private int radius;
    private int rootRadius;
	
    public Schematic(int[] blocks, int[] data, short width, short length, short height, short offsetX, short offsetY, short offsetZ, int radius, int rootRadius) {
        this.blocks = blocks;
        this.data = data;
        this.width = width;
        this.length = length;
        this.height = height;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.radius = radius;
        this.rootRadius = rootRadius;
    }
    
    public int[] getBlocks() {
        return blocks;
    }

    public int[] getData() {
        return data;
    }

    public short getWidth() {
        return width;
    }

    public short getLength() {
        return length;
    }

    public short getHeight() {
        return height;
    }
    
    public short getOffsetX() {
    	return offsetX;
    }
    
    public short getOffsetY() {
    	return offsetY;
    }
    
    public short getOffsetZ() {
    	return offsetZ;
    }
    
    public int getRadus() {
    	return radius;
    }
    
    public int getRootRadus() {
    	return rootRadius;
    }
	
    /**
     * This method read's a schematic and initializes the class object, to be used later
     * @param stream A InputStream from a schematic resource
     * @param offsetX The schematic X offset
     * @param offsetY The schematic Y offset
     * @param offsetZ The schematic Z offset
     * @return new instance of Schematic
     * @throws IOException
     */
    public static Schematic loadSchematic(InputStream stream, short offsetX, short offsetY, short offsetZ, int radius) throws IOException {
        NBTTagCompound nbtData = CompressedStreamTools.readCompressed(stream);

        short width = nbtData.getShort("Width");
        short length = nbtData.getShort("Length");
        short height = nbtData.getShort("Height");

        byte[] blockId = nbtData.getByteArray("Blocks");
        byte[] blockData = nbtData.getByteArray("Data");
        
        boolean extras = false;
        byte extraBlocks[] = null;
        byte extraBlocksNibble[] = null;
        int[] blocks = new int[blockId.length];
        int[] data = new int[blockData.length];

        if (nbtData.hasKey("AddBlocks")) {
        	extras = true;
            extraBlocksNibble = nbtData.getByteArray("AddBlocks");
            extraBlocks = new byte[extraBlocksNibble.length * 2];
            for(int i = 0; i < extraBlocksNibble.length; i++) {
            	extraBlocks[i * 2 + 0] = (byte) ((extraBlocksNibble[i] >> 4) & 0xF);
            	extraBlocks[i * 2 + 1] = (byte) (extraBlocksNibble[i] & 0xF);
            }
        }
        
        List<Integer> rootBound = new ArrayList<Integer>();
        for(int b = 0; b < 4; b++) {
        	rootBound.add(0);
        }
        
        for(int x = 0; x < width; x++) {
        	for(int y = 0; y < height; y++) {
        		for(int z = 0; z < length; z++) {
        			final int index = x + (y * length + z) * width;
        			int blockID = (blockId[index] & 0xFF) | (extras ? ((extraBlocks[index] & 0xFF) << 8) : 0);
        			final int meta = blockData[index] & 0xFF;
        			blocks[index] = blockID;
        			data[index] = meta;
        			
        			//Find root radius
        			if(y == offsetZ && blockID != 0) {
        				
        				//Right side
        				if(x > offsetX) {
        					if(x - offsetX > rootBound.get(2))
        						rootBound.set(2, x - offsetX);
        				} else { //Left side
        					if(offsetX - x > rootBound.get(0))
        						rootBound.set(0, offsetX - x);
        				}
        				
        				//Top side
        				if(z < offsetZ) {
        					if(offsetZ - z > rootBound.get(3))
        						rootBound.set(3, offsetZ - z);
        				} else { //Bottom side
        					if(z - offsetZ > rootBound.get(0))
        						rootBound.set(1, z - offsetZ);
        				}
        			}
        			
        		}
        	}
        }
        
        int rootRad = Collections.max(rootBound);
        
        stream.close();

        return new Schematic(blocks, data, width, length, height, offsetX, offsetY, offsetZ, radius, rootRad);
    }
    
    /**
     * Paste's in the schematic ant the specified location in the Minecraft world
     * @param world The Minecraft world to paste the schematic in
     * @param actualX The x coordinate, where to paste it
     * @param actualY The y coordinate, where to paste it
     * @param actualZ The z coordinate, where to paste it
     * @param schematic The schematic to paste
     * @param rotation The rotation (0-3)
     */
    @SuppressWarnings("deprecation")
	public static void pasteSchematic(World world, int actualX, int actualY, int actualZ, Schematic schematic, int rotation) {
        int[] blocks = schematic.getBlocks();
        int[] blockData = schematic.getData();

        short length = schematic.getLength();
        short width = schematic.getWidth();
        short height = schematic.getHeight();
        
        short offsetZ = schematic.getOffsetZ();
        short offsetX = schematic.getOffsetX();
        short offsetY = schematic.getOffsetY();

		/*BlockPos pastePos = new BlockPos(actualX, actualY, actualZ);
		IBlockState state = world.getBlockState(pastePos.down());
		if(!CustomTreePopulator.EXTRA_SURFACE.contains(state.getBlock()) || state.getBlock() != Blocks.GRASS){
			actualY -= 2;
			pastePos = new BlockPos(actualX, actualY, actualZ);
			state = world.getBlockState(pastePos);
			for(int yl = 0;!CustomTreePopulator.EXTRA_SURFACE.contains(state.getBlock()) || state.getBlock() != Blocks.GRASS;yl++){
				actualY -= 2;
				pastePos = new BlockPos(actualX, actualY, actualZ);
				state = world.getBlockState(pastePos);
				if(yl == 5)
				{
					return;
				}
			}
			actualY += 1;
		}*/
        
        if(rotation > 0) {
        	short newWidth = width;
        	short newLength = length;
        	if(rotation % 2 != 0) {
        		newWidth = length;
        		newLength = width;
        		if(rotation == 1) {
        			short oldX = offsetX;
        			offsetX = (short)(newWidth - offsetZ - 1);
        			offsetZ = oldX;
        		}else {
        			//Rotation 3
        			short oldX = offsetX;
        			offsetX = offsetZ;
        			offsetZ = (short)(newLength - oldX - 1);
        		}
        		
        	} else {
        		//South, rotation 2
        		offsetX = (short)(width - offsetX - 1);
        		offsetZ = (short)(length - offsetZ - 1);
        	}
        	
        	int[] blocksR = new int[blocks.length];
    		int[] blockDataR = new int[blockData.length];
    		for(int x = 0; x < width; x++) {
    			for(int y = 0; y < height; y++) {
    				for(int z = 0; z < length; z++) {
    					int index = x + (y * length + z) * width;
    					int c[] = rotateBlock(x, z, rotation, newWidth, newLength);
    					int newIndex = c[0] + (y * newLength + c[1]) * newWidth;
    					blocksR[newIndex] = blocks[index];
    					blockDataR[newIndex] = rotateMeta(blocks[index], blockData[index], rotation);
    				}
    			}
    		}
    		
    		//Replace the old values, with the new ones
    		length = newLength;
    		width = newWidth;
    		blocks = blocksR;
    		blockData = blockDataR;
        }
        
        BlockPos pos;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    int index = x + (y * length + z) * width;                    
                    //Skip air blocks
                    if(blocks[index] != 0) {
                        final Block block = Block.REGISTRY.getObjectById(blocks[index]);
                        final IBlockState blockState = block.getStateFromMeta(blockData[index]);
                        pos = new BlockPos((x + actualX) - offsetX , (y + actualY) - offsetY, (z + actualZ) - offsetZ);
                                  
                        if(ALLOWED_BLOCK.contains(world.getBlockState(pos).getBlock()))
                        	world.setBlockState(pos, blockState);
                    }
                }
            }
        }
    }
    
    public static boolean canPaste(World world, int actualX, int actualY, int actualZ) {
    	BlockPos pos = new BlockPos(actualX, actualY, actualZ);
    	IBlockState state = world.getBlockState(pos.down());
    	if(CustomTreePopulator.EXTRA_SURFACE.contains(state.getBlock())) {
    		return true;
    	}
    	/*else {
    		pos = new BlockPos(actualX, actualY, actualZ);
    		state = world.getBlockState(pos);
			if(CustomTreePopulator.EXTRA_SURFACE.contains(state.getBlock())) {
				return true;
			}
    		if(state == Blocks.AIR) {
    			int dY = actualY;
    			while(state == Blocks.AIR) {
    	    		dY = dY - 1;
    	    		pos = new BlockPos(actualX, dY, actualZ);
    	    		state = world.getBlockState(pos);
    			}
    			if(state == Blocks.GRASS) {
    				return true;
    			}
    		}

    	}*/
		return false;
    }
    
    /**
     * This method return the coordinate of a block, after flipping/rotating it
     * @param x Initial x coordinate
     * @param z Initial z coordinate
     * @param r The rotation (1-3)
     * @return Integer array, containing x and z coordinate
     */
    public static int[] rotateBlock(int x, int z, int r, short width, short lenght) {
    	int[] c = {x,z};
    	switch(r) {
			case 1: c[0] = width - z - 1; c[1] = x; break;
			case 2: c[0] = width - x - 1; c[1] = lenght - z - 1; break;
			case 3: c[0] = z; c[1] = lenght - x - 1; break;
		}
    	/*switch(r) {
    		case 1: c[0] = z; c[1] = lenght - x - 1; break;
    		case 2: c[0] = width -x - 1; c[1] = lenght - z - 1; break;
    		case 3: c[0] = width - z - 1; c[1] = x; break;
    	}*/
    	return c;
    }
    
    //Each group of 4 follows the pattern North,East,South,West
    public static BlockRotation woodRotations = new BlockRotation(4,8,4,8,5,9,5,9,6,10,6,10,7,11,7,11);
    public static BlockRotation block162 = new BlockRotation(8,4,8,4,9,5,9,5);
    public static BlockRotation pumpkinRotation = new BlockRotation(2,3,0,1);
    public static BlockRotation cocoaRotation = new BlockRotation(0,1,2,3,4,5,6,7,8,9,10,11);
    
    /**
     * This method rotates the meta of a block, if it can be rotated.
     * Currently only support's rotation of wood, acacia, dark oak, pumpkin blocks and cocoa
     * @param i The id of the block
     * @param m The meta of the block
     * @param r The rotation (1-3])
     * @return The rotated meta value
     */
    public static int rotateMeta(int i, int m, int r) {
    	if(r > 0) {
    		int side = -1;
    		int group = 0;
    		BlockRotation rot = new BlockRotation();
    		//Wood
    		if(i == 17) {
    			side = woodRotations.getSide(m);
    			rot = woodRotations;
    		} else if(i == 162) { //Acacia & Dark Oak
    			side = block162.getSide(m);
    			rot = block162;
    		} else if(Block.getIdFromBlock(Blocks.PUMPKIN) == i) { //Pumpkin
    			side = pumpkinRotation.getSide(m);
    			rot = pumpkinRotation;
    		} else if(Block.getIdFromBlock(Blocks.COCOA) == i) { //Cocoa
    			side = cocoaRotation.getSide(m);
    			rot = cocoaRotation;
    		}
    		
    		
    		
    		if(side >= 0) {
    			//Get number of group (of 4) the side is in
    			if(side >= 4) {
    				int min = 4;
    				int max = 7;
    				for(int g = 1;;g++) {
    					if(side >= min && side <= max) {
    						group = g;
    						break;
    					}
    					min += 4;
    					max += 4;
    				}
    			}
    			
    			return rot.getMeta(((side + r) % 4) + (group * 4));
    		}
    	}
    	
    	return m;
    }
}
