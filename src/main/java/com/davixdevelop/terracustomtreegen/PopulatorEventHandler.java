package com.davixdevelop.terracustomtreegen;

import com.davixdevelop.terracustomtreegen.baker.SegmentsBaker;
import io.github.terra121.event.InitEarthRegistryEvent;
import io.github.terra121.generator.data.IEarthDataBaker;
import io.github.terra121.generator.populate.IEarthPopulator;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PopulatorEventHandler {
	
	@SubscribeEvent
	public void populate(InitEarthRegistryEvent<IEarthPopulator> event) {
		event.registry().set("trees", new CustomTreePopulator());
	}
	
	@SubscribeEvent
	public void baker(InitEarthRegistryEvent<IEarthDataBaker> event) {
		event.registry().addLast("davixdevelop_linesegments", new SegmentsBaker());
	}
}
