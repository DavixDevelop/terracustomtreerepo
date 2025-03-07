from osgeo import ogr
from qgis.core import *
from osgeo import gdal
from datetime import datetime
import numpy as np
import lzma
import pickle

class BiomeTree:
    def __init__(self, leaf_type, continets, large, small, biomes, climates):
        self.leaf_type = leaf_type
        self.continets = continets
        self.large = large
        self.small = small
        self.biomes = biomes
        self.climates = climates

CATEGORY = "TreeMap"

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


def processMeta(task):
    #csvdata = []
    time_start = datetime.now()

    trees = []

    counter = 0
    csvfile = open('C:/Users/david/Documents/GitHub/terracustomtreerepo/treemap/trees.csv', 'r')
    for line in csvfile:
        if counter > 0:
            data = line.strip().split(';')
            leaf_type = data[1]
            cont = parseToListTrim(data[3])
            larg = parseToList(data[5].strip())
            sma = parseToList(data[6].strip())
            bio = parseToList(data[7].strip())
            clim = parseToListTrim(data[8])
            trees.append(BiomeTree(leaf_type, cont, larg, sma, bio, clim))
        else:
            counter = 1
    csvfile.close()

    try:
        climate_mapping = {"Af" : 1,"Am" : 2, "As" : 0,"Aw" : 3,"BWh" : 4,"BWk" : 5,"BSh" : 6,"BSk" : 7,"Csa" : 8,"Csb" : 9,"Csc" : 10,"Cwa" : 11,"Cwb" : 12,"Cwc" : 13,"Cfa" : 14,"Cfb" : 15,"Cfc" : 16,"Dsa" : 17,"Dsb" : 18,"Dsc" : 19,"Dsd" : 20,"Dwa" : 21,"Dwb" : 22,"Dwc" : 23,"Dwd" : 24,"Dfa" : 25,"Dfb" : 26,"Dfc" : 27,"Dfd" : 28,"ET" : 29,"EF" : 30}
        conti_mapping = {"Africa" : 1, "Asia" : 2, "Europe" : 8, "Oceania" : 5, "South America" : 6, "Australia" : 3, "North America" : 4}

        out_file = "C:/Users/david/Documents/GitHub/terracustomtreerepo/treemap/tree_meta.csv"


        #lzc = lzma.open(out_file, 'wb',format=lzma.FORMAT_ALONE)

        csvfile = open(out_file, 'w')
        csvfile.write("leaf_type;continents;large;small;biomes;climate\n")

        for t in trees:
            cindex = []
            for c in t.continets:
                cindex.append(conti_mapping[c])
            conti = ','.join(map(str, cindex))
            larg = ','.join(map(str, t.large))
            sma = ','.join(map(str, t.small))
            bio = ','.join(map(str, t.biomes))
            clindex = []
            for c in t.climates:
                ccode = climate_mapping[c]
                if ccode != 0:
                    clindex.append(ccode)
            clim = ','.join(map(str, clindex))

            ltyp = 0
            if t.leaf_type == "Coniferous":
                ltyp = 1

            csvfile.write("{lt};{co};{la};{sm};{bi};{cl}\n".format(lt=str(ltyp),co=conti,la=larg,sm=sma,bi=bio,cl=clim))
        
        csvfile.close()

    except Exception as e:
        QgsMessageLog.logMessage(
                    'Error: {error}'.format(error=str(e)),
                    CATEGORY, Qgis.Info)

    return time_start

def metaCompleted(exception, result=None):
    if result is not None:
        time_end = datetime.now()
        eclipsed = (time_end - result).total_seconds() / 60.0
        minutes = math.floor(eclipsed)
        seconds = math.floor((eclipsed - minutes) * 60)
        QgsMessageLog.logMessage(
            'Created tree matadata in {minutes} minutes and {seconds} seconds'.format(minutes=minutes, seconds=seconds),
            CATEGORY, Qgis.Info)

process_task = QgsTask.fromFunction('Create tree metadata', processMeta, on_finished=metaCompleted)
QgsApplication.taskManager().addTask(process_task)
