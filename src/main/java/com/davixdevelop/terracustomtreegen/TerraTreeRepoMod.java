package com.davixdevelop.terracustomtreegen;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = TerraTreeRepoMod.MODID, name = TerraTreeRepoMod.NAME, version = TerraTreeRepoMod.VERSION, dependencies = "required-after:terraplusplus")
public class TerraTreeRepoMod
{
    public static final String MODID = "terracustomtreerepo";
    public static final String NAME = "Terra++: Custom Tree Repo addon";
    public static final String VERSION = "1.0";

    public static Logger LOGGER;
    
    

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        LOGGER = event.getModLog();
    	MinecraftForge.TERRAIN_GEN_BUS.register(new PopulatorEventHandler());
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        // some example code
        //logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }
}
