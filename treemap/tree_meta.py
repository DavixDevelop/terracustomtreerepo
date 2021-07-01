from osgeo import ogr
from qgis.core import *
from osgeo import gdal
from datetime import datetime
import numpy as np
import lzma
import pickle

class BiomeTree:
    def __init__(self, continets, large, small, biomes, climates):
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
            cont = parseToListTrim(data[3])
            larg = parseToList(data[5].strip())
            sma = parseToList(data[6].strip())
            bio = parseToList(data[7].strip())
            clim = parseToListTrim(data[8])
            trees.append(BiomeTree(cont, larg, sma, bio, clim))
        else:
            counter = 1
    csvfile.close()

    try:
        climate_mapping = {"Af" : 1,"Am" : 2, "As" : 0,"Aw" : 3,"BWh" : 4,"BWk" : 5,"BSh" : 6,"BSk" : 7,"Csa" : 8,"Csb" : 9,"Csc" : 10,"Cwa" : 11,"Cwb" : 12,"Cwc" : 13,"Cfa" : 14,"Cfb" : 15,"Cfc" : 16,"Dsa" : 17,"Dsb" : 18,"Dsc" : 19,"Dsd" : 20,"Dwa" : 21,"Dwb" : 22,"Dwc" : 23,"Dwd" : 24,"Dfa" : 25,"Dfb" : 26,"Dfc" : 27,"Dfd" : 28,"ET" : 29,"EF" : 30}
        conti_mapping = {"Africa" : 1, "Asia" : 2, "Europe" : 8, "Oceania" : 5, "South America" : 6, "Australia" : 3, "North America" : 4}

        out_file = "C:/Users/david/Documents/GitHub/terracustomtreerepo/treemap/tree_meta.csv"


        #lzc = lzma.open(out_file, 'wb',format=lzma.FORMAT_ALONE)

        csvfile = open(out_file, 'w')
        csvfile.write("continents;large;small;biomes;climate\n")

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

            csvfile.write("{co};{la};{sm};{bi};{cl}\n".format(co=conti,la=larg,sm=sma,bi=bio,cl=clim))

        #long = (((360 - x) / (-2)) + ((360 - (x+ 1)) / (-2))) / 2
        #lat = (((32400 - (y *180)) / 360) + ((32400 - ((y + 1) * 180)) / 360)) / 2
        """
        p = -1
        cname = None
        for x in range(0, 43200): #720

            for y in range(0, 21600): #360

                p += 1
                #index = x * 360 + y
                task.setProgress(int(round((p * 100) / 933120000)))

                gcode = climti[y][x]

                if gcode != 0:
                    if cname is None or x % 720 == 0 and y % 360 == 0:
                        long = (((x + 0.5) * 360) / 43200) - 180
                        lat = (((y + 0.5) * 180) / 21600) - 90
                        for feature in conti.getFeatures():
                            if(feature.geometry().contains(QgsPointXY(long,lat))):
                                cname = str(feature[1]).strip()
                    if cname:
                        if cname == "Australia" or cname == "Oceania":
                            map_data[y][x][0] = 0;
                            map_data[y][x][1] = "";
                            #csvfile.write("{g};\n".format(g=index))
                        else:

                            for feature in climti.getFeatures():
                                if(feature.geometry().contains(QgsPointXY(long,lat))):
                                    gcode = int(feature[1])
                                    break

                            gclass = climate_mapping[gcode - 1]
                            btindexes = []
                            c = 0
                            for bt in trees:
                                if cname in bt.continets:
                                    if gclass in bt.climates:
                                        btindexes.append(c)
                                c = c + 1
                            if len(btindexes) > 0:
                                btides = ','.join(map(str, btindexes))
                                map_data[y][x][0] = len(btides);
                                map_data[y][x][1] = btides;
                                #csvfile.write("{g};{b}\n".format(g=index,b=btides))
                            else:
                                map_data[y][x][0] = 0;
                                map_data[y][x][1] = "";
                                #csvfile.write("{g};\n".format(g=index))
                                #csvfile.write("{g};\n".format(g=index))
                    else:
                        map_data[y][x][0] = 0;
                        map_data[y][x][1] = "";
                else:
                    map_data[y][x][0] = 0;
                    map_data[y][x][1] = "";
                    #csvfile.write("{g};\n".format(g=index))
                """



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
