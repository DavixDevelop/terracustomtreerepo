from osgeo import ogr
from qgis.core import *

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
    
    
def processMap(task, trees):
    #csvdata = []
    
    conti = QgsVectorLayer("C:/Users/david/Documents/GitHub/terracustomtreerepo/project_resources/continents/continents.shp","continents","ogr")
    climti = QgsVectorLayer("C:/Users/david/Documents/GitHub/terracustomtreerepo/project_resources/wgs/climate_wgs.shp","climate_wgs","ogr")   
     
    csvfile = open("C:/Users/david/Documents/GitHub/terracustomtreerepo/treemap/tree_map.csv",'w')
    
    p = -1
    for x in range(0, 720):
        long = (((360 - x) / (-2)) + ((360 - (x+ 1)) / (-2))) / 2
        for y in range(0, 360):
            lat = (((32400 - (y *180)) / 360) + ((32400 - ((y + 1) * 180)) / 360)) / 2
            p = p + 1
            index = x * 360 + y
            task.setProgress(int(round((p * 100) /  259200)))
            cname = None
            for feature in conti.getFeatures():
                if(feature.geometry().contains(QgsPointXY(long,lat))):
                    cname = str(feature[1]).strip()
                    break
            if cname:
                if cname == "Australia" or cname == "Oceania":
                     csvfile.write("{g};\n".format(g=index))
                else:
                    gcode = None
                    for feature in climti.getFeatures():
                        if(feature.geometry().contains(QgsPointXY(long,lat))):
                            gcode = int(feature[1])
                            break
                    
                    if gcode:
                        btindexes = []
                        c = 0
                        for bt in trees:
                            if cname in bt.continets:
                                if gcode in bt.climates:
                                    btindexes.append(c)
                            c = c + 1
                        if len(btindexes) > 0:
                            btides = ','.join(map(str, btindexes))
                            csvfile.write("{g};{b}\n".format(g=index,b=btides))
                        else:
                            csvfile.write("{g};\n".format(g=index))
                        
                    else:
                        csvfile.write("{g};\n".format(g=index))
            else:
                csvfile.write("{g};\n".format(g=index))
    
    #with open("C:/Users/david/Documents/GitHub/terraplusplus-fast/tree_map.csv",'w') as cf:
    #    cf.writelines("%s\n" % tile for tile in csvdata)
    
    csvfile.close()
    
    return p
    
def mapCompleted(exception, result=None):
    if result is not None:
            QgsMessageLog.logMessage(
                'Created csv, with {count} rows'.format(count=result),
                CATEGORY, Qgis.Info)

biomeTrees = []

counter = 0
csvfile = open('C:/Users/david/Documents/GitHub/terracustomtreerepo/treemap/trees.csv', 'r')
for line in csvfile:
    if counter > 0:
        data = line.strip().split(';')
        cont = parseToListTrim(data[3])
        larg = parseToList(data[5].strip())
        sma = parseToList(data[6].strip())
        bio = parseToList(data[7].strip())
        clim = parseToList(data[8].strip())
        biomeTrees.append(BiomeTree(cont, larg, sma, bio, clim))
    else:
        counter = 1
csvfile.close()

process_task = QgsTask.fromFunction('Create tree map', processMap, on_finished=mapCompleted, trees=biomeTrees)
QgsApplication.taskManager().addTask(process_task)
        