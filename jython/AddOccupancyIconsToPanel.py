# Script to create and place SensorIcons and BlockContents for each occupancy Block on a panel
#  icons are centered on longest track segment of each block 
#  based on Bill Fitch's CreateIcons.py

import jmri
import java
import javax
import org.slf4j.LoggerFactory
import java.awt.Font

def removeBlockContentIcons(panel):
    deleteList = []     # Prevent concurrent modification
    icons = panel.getBlockContentsLabelList()
    for icon in icons:
        blk = icon.getBlock()
        if blk is not None:
            deleteList.append(icon)
    for item in deleteList:
        panel.removeFromContents(item)

def removeSensorIcons(panel):
    blockSensors = []
    for block in blocks.getNamedBeanSet():
        sensor = block.getSensor()
        if sensor is not None:
            blockSensors.append(sensor)

    deleteList = []     # Prevent concurrent modification
    icons = panel.getSensorList()
    for icon in icons:
        sensor = icon.getSensor()
        if (sensor) and (sensor in blockSensors) :
            deleteList.append(icon)

    for item in deleteList:
        panel.removeFromContents(item)

# ************************************************************
# find and store the x,y to place a sensorIcon for each Block
#   places it in segment center of longest track segment
# ************************************************************
def getBlockCenterPoints(panel):
    blockPoints.clear()
    maxSegSizes.clear()
    for tsv in panel.getTrackSegmentViews():
        blk = tsv.getBlockName()
        xy = tsv.getCentreSeg()
        pt1 = panel.getCoords(tsv.getConnect1(), tsv.getType1())
        pt2 = panel.getCoords(tsv.getConnect2(), tsv.getType2())
        segSize = jmri.util.MathUtil.distance(pt1, pt2)
        if blk not in blockPoints :
            blockPoints[blk] = xy
            maxSegSizes[blk] = segSize
        elif (segSize > maxSegSizes[blk]) : # replace only if seg is longer
            blockPoints[blk] = xy
            maxSegSizes[blk] = segSize

# **************************************************
# create and place the SensorIcon for each Block
# **************************************************
def addOccupancyIconsAndLabels(panel):
    for blockName in blockPoints.keys():
        x = blockPoints[blockName].getX() - 5 # icon is 10x10
        y = blockPoints[blockName].getY() - 5
        block = blocks.getBlock(blockName)
        if (block) :
            sensor = block.getSensor()
            if (sensor) :
                addSmallIcon(panel, sensor.getDisplayName(), x, y)
                addBlockContentLabel(panel, block, x+10, y-5)

# **************************************************
# small icon
# **************************************************
def addSmallIcon(panel, sensorName, x, y):
    icn = jmri.jmrit.display.SensorIcon(panel)
    icn.setIcon("SensorStateActive", jmri.jmrit.catalog.NamedIcon("resources/icons/smallschematics/tracksegments/circuit-occupied.gif", "active"));
    icn.setIcon("SensorStateInactive", jmri.jmrit.catalog.NamedIcon("resources/icons/smallschematics/tracksegments/circuit-empty.gif", "inactive"));
    icn.setIcon("BeanStateInconsistent", jmri.jmrit.catalog.NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif", "incons"));
    icn.setIcon("BeanStateUnknown", jmri.jmrit.catalog.NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif", "unknown"));

    # Assign the sensor and set the location
    icn.setSensor(sensorName)
    icn.setLocation(int(x), int(y))
    icn.setLevel(7)

    # Add the icon to the layout editor panel
    panel.putSensor(icn)

# **************************************************
# block content label
# **************************************************
def addBlockContentLabel(panel, block, x, y):
    label = jmri.jmrit.display.BlockContentsIcon(block.getDisplayName(), panel)
    label.setBlock(block.getDisplayName())
    label.setLocation(int(x), int(y))
    label.setLevel(7)
#     label.setBorder(javax.swing.BorderFactory.createMatteBorder(1,1,1,1, java.awt.Color.gray))
    label.setFont(java.awt.Font(java.awt.Font.DIALOG, java.awt.Font.BOLD, 14))
    label.setForeground(java.awt.Color.gray)
    panel.putItem(label)

#=====================================================================================
log = org.slf4j.LoggerFactory.getLogger("jmri.jmrit.jython.exec.script.AddOccupancyIconsToPanel")

log.info( "AddOccupancyIconsToPanel.py started" )

blockPoints = {}   # Block center segment points
maxSegSizes = {}   # Look for longest segment in each block

panels = jmri.InstanceManager.getDefault(jmri.jmrit.display.EditorManager)
layoutPanels = panels.getList(jmri.jmrit.display.layoutEditor.LayoutEditor)
if (len(layoutPanels) != 1) :
    log.error('Error finding single LayoutEditor panel.')
    exit
panel = layoutPanels[0]

removeSensorIcons(panel)
removeBlockContentIcons(panel)
getBlockCenterPoints(panel)
addOccupancyIconsAndLabels(panel)

log.info( "AddOccupancyIconsToPanel.py completed" )
