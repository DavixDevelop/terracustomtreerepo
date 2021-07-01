package com.davixdevelop.terracustomtreegen;

import com.davixdevelop.terracustomtreegen.baker.SegmentsBaker;
import com.davixdevelop.terracustomtreegen.baker.TreeMapBaker;
import com.davixdevelop.terracustomtreegen.dataset.ContinentsDataset;
import com.davixdevelop.terracustomtreegen.dataset.ContinentsDataset;
import com.davixdevelop.terracustomtreegen.dataset.KoppenClimate;
import net.buildtheearth.terraplusplus.event.InitDatasetsEvent;
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

	@SubscribeEvent
	public void datasets(InitDatasetsEvent event){
		boolean addKoppen = true;
		try{
			if(event.get(KEY_KOPPEN) != null)
				addKoppen = false;
		}catch (Exception ex){ }
		if(addKoppen)
			event.register(KEY_KOPPEN, new KoppenClimate());

		event.register(KEY_CONTINENT, new ContinentsDataset());
	}
}
