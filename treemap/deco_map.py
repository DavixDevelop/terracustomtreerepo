from osgeo import ogr
from qgis.core import *
from osgeo import gdal
from datetime import datetime
import numpy as np
import lzma
from time import sleep

CATEGORY = "DecoMap"

def processMap(task):
    time_start = datetime.now()

    try:

        #conti = QgsVectorLayer("C:/Users/david/Documents/GitHub/terracustomtreerepo/project_resources/continents/continents.shp","continents","ogr")

        conti_mapping = {"Africa" : 1, "Asia" : 2, "Europe" : 8, "Oceania" : 5, "South America" : 6, "Australia" : 3, "North America" : 4}

        out_file = "C:/Users/david/Documents/GitHub/terracustomtreerepo/treemap/continents_map.lzma"
        out_file2 = "C:/Users/david/Documents/GitHub/terracustomtreerepo/treemap/continents_map2.tif"

        map_data = np.frombuffer(lzma.open(out_file, format=lzma.FORMAT_ALONE, mode='rb').read())
        raster_array = np.reshape(map_data, (-1, 720))

        QgsMessageLog.logMessage(
                    str(raster_array),
                    CATEGORY, Qgis.Info)
        sleep(0.1)

        mem_driver = gdal.GetDriverByName("MEM")
        conti_ras = mem_driver.Create('', 720, 360, 1, gdal.GDT_Byte)
        band = conti_ras.GetRasterBand(1)
        band.SetNoDataValue(0)
        band.WriteArray(raster_array)

        gdal.GetDriverByName('GTiff').CreateCopy(out_file2, conti_ras)

        conti_ras = None

        #map_data = np.empty((360, 720), dtype=np.uint8)
        #long = (((x + 0.5) * 360) / 43200) - 180
        #lat = (((y + 0.5) * -180) / 21600) + 90

    except Exception as e:
        QgsMessageLog.logMessage(
                    'Erroe: {error}'.format(error=str(e)),
                    CATEGORY, Qgis.Info)

    return time_start

def mapCompleted(exception, result=None):
    if result is not None:
        time_end = datetime.now()
        eclipsed = (time_end - result).total_seconds() / 60.0
        minutes = math.floor(eclipsed)
        seconds = math.floor((eclipsed - minutes) * 60)
        QgsMessageLog.logMessage(
            'Created continets map in {minutes} minutes and {seconds} seconds'.format(minutes=minutes, seconds=seconds),
            CATEGORY, Qgis.Info)

process_task = QgsTask.fromFunction('Create continents map', processMap, on_finished=mapCompleted)
QgsApplication.taskManager().addTask(process_task)
