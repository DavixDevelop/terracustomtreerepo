package com.davixdevelop.terracustomtreegen.schematic;

/**
 * Simple class for storing schematic path and offset
 * @author DavixDevelop
 *
 */
public class SchematicOffset {
	private String sname = "";
	private short offsetX = 0;
	private short offsetY = 0;
	private short offsetZ = 0;
	private int radius = 0;
	
	/**
	 * 
	 * @param name The folder and schematic file name in the assets
	 * @param offsetX The offset from west to east
	 * @param offsetY The offset from bottom to top
	 * @param offsetZ The offset from north to south
	 */
	public SchematicOffset(String name, int offsetX, int offsetY, int offsetZ, int radius) {
		this.sname = name;
		this.offsetX = (short)offsetX;
		this.offsetY = (short)offsetY;
		this.offsetZ = (short)offsetZ;
		this.radius = radius;
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
	
	public String getSchematicName() {
		return sname;
	}
	
	public int getRadius() {
		return radius;
	}
}
