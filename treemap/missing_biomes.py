from osgeo import ogr
from qgis.core import *
from osgeo import gdal
from datetime import datetime
import numpy as np
import traceback


class BiomeTree:
    def __init__(self, name, leaf_type, climatetext, continets, biometext, large, small, biomes, climates): 
        self.name = name
        self.leaf_type = leaf_type
        self.climatetext = climatetext
        self.continets = continets
        self.biometext = biometext
        self.large = large
        self.small = small
        self.biomes = biomes
        self.climates = climates

class BiomeMapping:
    def __init__(self, biome_id, biome_name, biome_climate, parent_biome_ids):
        self.id = biome_id
        self.name = biome_name
        self.climate = biome_climate
        self.parents = parent_biome_ids
        

CATEGORY = "MissingBiomes"

find_missing = False

#131, 38, 161, 134, 160, 140
biome_mapping = [
    BiomeMapping(129, "mutated plains", ["Csb"], [1]),
    BiomeMapping(131, "mutated extreme hills", ["Cwc"], [3, 20]),
    BiomeMapping(132, "mutated forest", ["Cfa","Cfb"], [4, 18]),
    BiomeMapping(161, "mutated redwood taiga hills", ["Dsd"], [5, 19]),
    BiomeMapping(134, "mutated swampland", ["Dwb"], [6]),
    BiomeMapping(160, "mutated redwood taiga", ["Dwd"], [5, 19]),
    BiomeMapping(13, "ice mountains", ["Dfd"], [5, 19]),
    BiomeMapping(140, "mutated ice flats", ["EF"], [5, 19])
]


def parseToList(input):
    parsed = []
    if "," in input:
        slist = input.split(",")
        for n in slist:
            parsed.append(int(n))
    else:
        parsed.append(int(input))
    return parsed

def parseToListTrim(input):
    parsed = []
    if "," in input:
        slist = input.split(",")
        for s in slist:
            parsed.append(s.strip())
    else:
        parsed.append(input.strip())
    return parsed


def processTrees(task):
    time_start = datetime.now()

    trees = []

    try:
        counter = 0
        csvfile = open('C:/Users/david/Documents/GitHub/terracustomtreerepo/treemap/trees_org.csv', 'r')
        for line in csvfile:
            if counter > 0:
                data = line.strip().split(';')
                name = data[0]
                leaf_type = data[1]
                climatetext = data[2]
                cont = parseToListTrim(data[3])
                biometext = parseToListTrim(data[4].strip())
                larg = parseToList(data[5].strip())
                sma = parseToList(data[6].strip())
                bio = parseToList(data[7].strip())
                clim = parseToListTrim(data[8].strip())
                trees.append(BiomeTree(name, leaf_type, climatetext, cont, biometext, larg, sma, bio, clim))
            else:
                counter = 1
        csvfile.close()

        out_file = "C:/Users/david/Documents/GitHub/terracustomtreerepo/treemap/trees.csv"

        missing_biomes = []  

        biome_addon_biome_ids = [21,23,35,1,129,151,22,131,132,3,36,38,30,161,6,134,32,160,4,29,5,13,140]

        if find_missing:
            used_biomes = []

            for t in trees:
                for b in t.biomes:
                    if b in biome_addon_biome_ids and b not in used_biomes:
                        used_biomes.append(b)

            
            
            if len(used_biomes) > 0:
                QgsMessageLog.logMessage(
                'Found used biomes: {bi}'.format(bi=used_biomes),
                CATEGORY, Qgis.Info)

                missing_biomes.extend(biome_addon_biome_ids)
                for b in used_biomes:
                    missing_biomes.remove(b)

            if len(missing_biomes) > 0:
                QgsMessageLog.logMessage('Missing biome ids: {bi}'.format(bi=missing_biomes), CATEGORY, Qgis.Info)
        
        if find_missing is False and len(biome_mapping) > 0:
            QgsMessageLog.logMessage('Injecting biome mapping into CSV', CATEGORY, Qgis.Info)

            csvfile = open(out_file, 'w')
            csvfile.write("name;wat;climatetext;continent;biometext;large;small;biome;climate\n")

            updated_trees = []

            for t in trees: 
                for m in biome_mapping:
                    #Check if biome mapping in not already defined for the tree
                    if m.id not in t.biomes:

                        for b in t.biomes:
                            #Check if tree biome is in biome mapping parents
                            if b in m.parents:
                                append = False
                                for c in m.climate:
                                    if c in t.climates:
                                        append = True
                                        break
                                
                                if append:
                                    #Add biome mapping to tree biomes
                                    t.biomes.append(m.id)
                                    #Add biome name to biometext
                                    if m.name not in t.biometext:
                                        t.biometext.append(m.name)
                                break

                updated_trees.append(t)

            for t in updated_trees: 
                cont = ','.join(map(str, t.continets))
                biome_text = ','.join(map(str, t.biometext))
                larg = ','.join(map(str, t.large))
                sma = ','.join(map(str, t.small))
                bio = ','.join(map(str, t.biomes))
                clim = ','.join(map(str, t.climates))

                csvfile.write("{name};{wat};{climatetext};{continent};{biometext};{large};{small};{biome};{climate}\n".format(name=t.name, wat = t.leaf_type, climatetext = t.climatetext, continent = cont, biometext = biome_text, large = larg, small = sma, biome = bio, climate = clim))

            csvfile.close()

    except Exception as e:
        QgsMessageLog.logMessage(
                    'Error: {error}'.format(error=str(e)),
                    CATEGORY, Qgis.Info)
        traceback.print_exc()

    return time_start

def processCompleted(exception, result=None):
    if result is not None:
        time_end = datetime.now()
        eclipsed = (time_end - result).total_seconds() / 60.0
        minutes = math.floor(eclipsed)
        seconds = math.floor((eclipsed - minutes) * 60)

        if find_missing: 
            QgsMessageLog.logMessage(
                'Found missing biomes in {minutes} minutes and {seconds} seconds'.format(minutes=minutes, seconds=seconds),
                CATEGORY, Qgis.Info)
        else:
            QgsMessageLog.logMessage(
                'Updated CSV with missing biomes in {minutes} minutes and {seconds} seconds'.format(minutes=minutes, seconds=seconds),
                CATEGORY, Qgis.Info)

process_task = QgsTask.fromFunction('Find missing biomes', processTrees, on_finished=processCompleted)
QgsApplication.taskManager().addTask(process_task)
