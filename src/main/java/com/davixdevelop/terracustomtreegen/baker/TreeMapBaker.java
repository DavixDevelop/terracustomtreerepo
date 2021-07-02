package com.davixdevelop.terracustomtreegen.baker;

import com.davixdevelop.terracustomtreegen.PopulatorEventHandler;
import com.davixdevelop.terracustomtreegen.repo.CustomTreeRepository;
import com.davixdevelop.terracustomtreegen.repo.TreeBiome;
import com.davixdevelop.terracustomtreegen.repo.TreeData;
import net.buildtheearth.terraplusplus.dataset.IScalarDataset;
import net.buildtheearth.terraplusplus.generator.CachedChunkData;
import net.buildtheearth.terraplusplus.generator.GeneratorDatasets;
import net.buildtheearth.terraplusplus.generator.data.IEarthDataBaker;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraplusplus.util.CornerBoundingBox2d;
import net.buildtheearth.terraplusplus.util.bvh.Bounds2d;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class TreeMapBaker implements IEarthDataBaker<TreeMapBaker.Data> {

    public static CustomTreeRepository TREE_REPO = new CustomTreeRepository();

    public static final String KEY_CUSTOM_TREE_REPO_TREE_MAP = "davixdevelop_terratreerepo_tree_gens";

    @Override
    public CompletableFuture<TreeMapBaker.Data> requestData(ChunkPos chunkPos, GeneratorDatasets generatorDatasets, Bounds2d bounds2d, CornerBoundingBox2d cornerBoundingBox2d) throws OutOfProjectionBoundsException {
        CompletableFuture<double[]> continents = generatorDatasets.<IScalarDataset>getCustom(PopulatorEventHandler.KEY_CONTINENT).getAsync(cornerBoundingBox2d, 16, 16);
        CompletableFuture<double[]> climate = generatorDatasets.<IScalarDataset>getCustom(PopulatorEventHandler.KEY_KOPPEN).getAsync(cornerBoundingBox2d, 16, 16);

        return CompletableFuture.allOf(continents, climate)
                .thenApply(unused -> new Data(continents.join(), climate.join()));

    }

    @Override
    public void bake(ChunkPos chunkPos, CachedChunkData.Builder builder, TreeMapBaker.Data data) {
        TreeData treeData = new TreeData();
        if(data !=null){
            double climate = data.climates[128];
            double continent = data.continents[128];


            if(climate != 0 && continent != 0){
                treeData.treeIndexes = new ArrayList<>();

                for(int t = 0; t < TreeMapBaker.TREE_REPO.getTreeMeta().size(); t++){
                    TreeBiome treeBiome = TreeMapBaker.TREE_REPO.getTreeMeta().get(t);
                    if(treeBiome.climate.contains((int)climate) && treeBiome.continents.contains((int)continent))
                        treeData.treeIndexes.add(t);
                }
            }
        }


        builder.putCustom(KEY_CUSTOM_TREE_REPO_TREE_MAP, treeData);
    }

    public class Data {
        public Data(double[] cont, double[] climti){
            continents = cont;
            climates = climti;
        }
        public double[] continents;
        public double[] climates;
    }
}
