package com.davixdevelop.terracustomtreegen.baker;

import com.davixdevelop.terracustomtreegen.SegmentLinearFunc;
import com.davixdevelop.terracustomtreegen.TerraTreeRepoMod;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import net.buildtheearth.terraplusplus.dataset.IElementDataset;
import net.buildtheearth.terraplusplus.dataset.IScalarDataset;
import net.buildtheearth.terraplusplus.dataset.geojson.GeoJsonObject;
import net.buildtheearth.terraplusplus.dataset.geojson.Geometry;
import net.buildtheearth.terraplusplus.dataset.geojson.geometry.*;
import net.buildtheearth.terraplusplus.dataset.geojson.object.Feature;
import net.buildtheearth.terraplusplus.dataset.geojson.object.FeatureCollection;
import net.buildtheearth.terraplusplus.generator.CachedChunkData.Builder;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorPipelines;
import net.buildtheearth.terraplusplus.generator.GeneratorDatasets;
import net.buildtheearth.terraplusplus.generator.data.IEarthDataBaker;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;
import net.minecraft.util.math.ChunkPos;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import net.buildtheearth.terraplusplus.generator.EarthGeneratorPipelines;

/**
 * A DataBaker that convert raw osm data to segments, and save's them under a custom key
 * @author DavixDevelop
 *
 */
public class SegmentsBaker implements IEarthDataBaker<GeoJsonObject[][]>{
	
	public static final String KEY_CUSTOM_TREE_REPO_ROAD_SEGMENTS = "davixdevelop_terratreerepo_road_segments";
	public static final String KEY_CUSTOM_TREE_REPO_FREEWAY_SEGMENTS = "davixdevelop_terratreerepo_freeway_segments";
	public static final String KEY_CUSTOM_TREE_REPO_PATH_SEGMENTS = "davixdevelop_terratreerepo_path_segments";
	public static final String KEY_CUSTOM_TREE_REPO_BUILDING_SEGMENTS = "davixdevelop_terratreerepo_building_segments";
	
	private static GeographicProjection PROJECTION; 
	
	public static final Set<SegmentLinearFunc> FALLBACK_CUSTOM_TREE_REPO_SEGMENTS = new HashSet<>();
	public static final Set<Set<SegmentLinearFunc>> FALLBACK_CUSTOM_TREE_REPO_POLYGONS = new HashSet<>();

	@Override
	public void bake(ChunkPos pos, Builder builder, GeoJsonObject[][] rawGsonObjects) {
		if(rawGsonObjects == null)
			return;

		List<RawSegments> buildings = new ArrayList<>();

		List<List<RawSegments>> segments = new ArrayList<>();
		segments.add(new ArrayList<>());
		segments.add(new ArrayList<>());
		segments.add(new ArrayList<>());

		int baseX = Coords.cubeToMinBlock(pos.x);
		int baseZ = Coords.cubeToMinBlock(pos.z);
		Bounds2d chunkBounds = Bounds2d.of(baseX, baseX + 16, baseZ, baseZ + 16);
		chunkBounds = chunkBounds.expand(16.0d);
		
		try {
			for(GeoJsonObject[] objects : rawGsonObjects) {
				for(GeoJsonObject object : objects) {
					if(object instanceof Feature) {
						Feature feature = (Feature) object;
						if(feature.geometry().bounds().toCornerBB(PROJECTION, true).fromGeo().intersects(chunkBounds)){
							segments = convertToLines(segments, feature);
							buildings = convertToPolygons(buildings, feature);
						}

					}else if(object instanceof FeatureCollection) {
						FeatureCollection featureCol = (FeatureCollection) object;
						for (Feature feature : featureCol) {
							if(feature.geometry().bounds().toCornerBB(PROJECTION, true).fromGeo().intersects(chunkBounds)){
								segments = convertToLines(segments, feature);
								buildings = convertToPolygons(buildings, feature);
							}
						}
					}
				}
			}
			
			builder.putCustom(KEY_CUSTOM_TREE_REPO_ROAD_SEGMENTS, segments.get(0).stream().flatMap(x -> x.lines.stream()).collect(Collectors.toSet()));
			builder.putCustom(KEY_CUSTOM_TREE_REPO_FREEWAY_SEGMENTS, segments.get(1).stream().flatMap(x -> x.lines.stream()).collect(Collectors.toSet()));
			builder.putCustom(KEY_CUSTOM_TREE_REPO_PATH_SEGMENTS, segments.get(2).stream().flatMap(x -> x.lines.stream()).collect(Collectors.toSet()));
			Set<Set<SegmentLinearFunc>> builds = new HashSet<>();
			buildings.forEach(v -> {
				builds.add(v.lines);
			});
			builder.putCustom(KEY_CUSTOM_TREE_REPO_BUILDING_SEGMENTS, builds);
			
			
		}catch(OutOfProjectionBoundsException ex) {
			TerraTreeRepoMod.LOGGER.error("Error while baking segment data");
			ex.printStackTrace();
		}
	}

	@Override
	public CompletableFuture<GeoJsonObject[][]> requestData(ChunkPos pos, GeneratorDatasets datasets, Bounds2d bounds,
			CornerBoundingBox2d boundsGeo) throws OutOfProjectionBoundsException {
		if(PROJECTION == null)
			PROJECTION = datasets.projection();

		CompletableFuture<double[]> treeCoverF = datasets.<IScalarDataset>getCustom(EarthGeneratorPipelines.KEY_DATASET_TREE_COVER).getAsync(boundsGeo, 16, 16);
		boolean hasTrees = false;

		double[] treeCover = treeCoverF.join();
		for (double v : treeCover)
			if (v > 0.0d) {
				hasTrees = true;
				break;
			}


		if(!hasTrees)
			return null;

		return datasets.<IElementDataset<GeoJsonObject[]>>getCustom(EarthGeneratorPipelines.KEY_DATASET_OSM_RAW)
				.getAsync(bounds.expand(16.0d).toCornerBB(datasets.projection(), false).toGeo());
	}
	
	/**
	 * Convert's a feature to line segments
	 * @param lines The list of set of line segments
	 * @param feature The feature
	 * @return he list of set of lines
	 * @throws OutOfProjectionBoundsException
	 */
	protected List<List<RawSegments>> convertToLines(List<List<RawSegments>> lines, Feature feature) throws OutOfProjectionBoundsException{
		if(feature.properties() == null)
			return lines;

		if(feature.properties().containsKey("highway")) {
			int lanes = -1;
			boolean isMain = false; 
			boolean isMinor = false; 
			boolean isSide = false;
			boolean isFreeway = false;
			boolean isPath = false;
			String id = feature.id();
			
			for(Map.Entry<String, String> entry : feature.properties().entrySet()) {
				String k = entry.getKey();
				String v = entry.getValue();
				if(k.equals("highway")) {
					switch (v) {
						case "primary":
						case "raceway":
							isMain = true;
							break;
						case "residential":
						case "tertiary":
							isMinor = true;
							break;
						case "secondary":
						case "primary_link":
						case "secondary_link":
						case "living_street":
						case "bus_guideway":
						case "service":
						case "unclassified":
							isSide = true;
							break;
						case "motorway":
						case "trunk":
							isFreeway = true;
							break;
						default:
							isPath = true;
							break;
					}
				}else if(k.equals("lanes")) {
					lanes = Integer.parseInt(v);
				}
			}
			

			
			double radius = 3.0d;
			if(isFreeway)
				radius = ((6 * lanes) >> 1) + 2;
			else if(isMain){
				if(lanes == -1)
					lanes = 2;
				radius = lanes << 1;
			}

			else if(isMinor){
				if(lanes == -1)
					radius = 1.5d;
				else
					radius = lanes;
			}
			else if(isSide){
				if(lanes == -1)
					lanes = 1;
				radius = (3 * lanes + 1) >> 1;
			}

			else if(isPath)
				radius = 1.0d;
			
			if(isMain || isMinor || isSide || isFreeway || isPath){
				int ind = (isMain || isMinor || isSide) ? 0 : (isFreeway) ? 1 : 2;
				List<RawSegments> rl = lines.get(ind);
				if(lines.get(ind).stream().anyMatch(x -> x.ID.equals(id))){
					RawSegments ri = lines.get(ind).stream().filter(x -> x.ID.equals(id)).findFirst().get();
					int rsi  = rl.indexOf(ri);
					ri.lines = convertToSegments(feature.geometry().project(PROJECTION::fromGeo), radius, ri.lines);
					rl.set(rsi, ri);

				}else{
					RawSegments nw = new RawSegments();
					nw.ID = id;
					nw.lines = convertToSegments(feature.geometry().project(PROJECTION::fromGeo), radius, new HashSet<>());
					rl.add(nw);
				}
				lines.set(ind, rl);
			}

		}
		
		return lines;
	}
	
	protected List<RawSegments> convertToPolygons(List<RawSegments> polygons, Feature feature) throws OutOfProjectionBoundsException {
		if(feature.properties() == null)
			return polygons;

		if(feature.properties().containsKey("building")) {
			String  id = feature.id();
			Geometry geometry = feature.geometry();
			
			if(geometry instanceof Polygon) {
				Polygon polygon = (Polygon) geometry;
				polygons = convertPolygonToSegments(polygons, id, polygon);

			}else if(geometry instanceof MultiPolygon) {
				MultiPolygon multiPoly = (MultiPolygon) geometry;
				for (Polygon polygon : multiPoly)
					polygons = convertPolygonToSegments(polygons, id, polygon);
			}
			
		}
		
		return polygons;
	}

	protected  List<RawSegments> convertPolygonToSegments(List<RawSegments> polygons, String id, Polygon polygon) throws OutOfProjectionBoundsException {
		if(polygons.stream().anyMatch(x -> x.ID.equals(id))){
			RawSegments rp = polygons.stream().filter(x -> x.ID.equals(id)).findFirst().get();
			int rpi = polygons.indexOf(rp);
			rp.lines = convertToSegments(polygon.outerRing().project(PROJECTION::fromGeo), 1.0d, rp.lines);
			polygons.set(rpi, rp);
		}else{
			RawSegments rw = new RawSegments();
			rw.ID = id;
			rw.lines = convertToSegments(polygon.outerRing().project(PROJECTION::fromGeo), 1.0d, new HashSet<>());
			polygons.add(rw);
		}

		return  polygons;
	}
	
	protected Set<SegmentLinearFunc> convertToSegments(Geometry geometry, double radius, Set<SegmentLinearFunc> segments){
		Point[] points = new Point[0];
		
		if(geometry instanceof LineString) {
			LineString lineString = (LineString) geometry;
			points = lineString.points();
		}else if(geometry instanceof MultiLineString) {
			MultiLineString multiLine = (MultiLineString) geometry;
			for (LineString lineString : multiLine) segments = convertToSegments(lineString, radius, segments);
		}



		if(points.length > 0) {
			for(int p = 0; p < points.length - 1; p++) {
				SegmentLinearFunc seg =new SegmentLinearFunc(new double[] {points[p].lon(), points[p].lat()}, new double[] {points[p + 1].lon(), points[p + 1].lat()}, radius);
				segments.add(seg);
			}
		}
		
		return segments;
	}
}
