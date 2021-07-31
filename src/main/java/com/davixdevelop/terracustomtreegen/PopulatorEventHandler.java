package com.davixdevelop.terracustomtreegen;

import com.davixdevelop.terracustomtreegen.baker.SegmentsBaker;
import com.davixdevelop.terracustomtreegen.baker.TreeMapBaker;
import net.buildtheearth.terraplusplus.event.InitEarthRegistryEvent;
import net.buildtheearth.terraplusplus.generator.data.IEarthDataBaker;
import net.buildtheearth.terraplusplus.generator.populate.IEarthPopulator;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PopulatorEventHandler {

	public static final String KEY_KOPPEN = "KEY_DAVIXDEVELOP_KOPPEN_DATASET";
	public static final String KEY_CONTINENT = "KEY_DAVIXDEVELOP_CONTINETS_DATASET";

	@SubscribeEvent
	public void populate(InitEarthRegistryEvent<IEarthPopulator> event) {
		event.registry().set("trees", new CustomTreePopulator());
	}

	@SubscribeEvent
	public void baker(InitEarthRegistryEvent<IEarthDataBaker> event) {
		event.registry().addLast("davixdevelop_linesegments", new SegmentsBaker());
		event.registry().addLast("davixdevelop_treemap", new TreeMapBaker());
	}
}
