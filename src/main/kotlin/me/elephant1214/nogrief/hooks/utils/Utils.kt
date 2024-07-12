    package me.elephant1214.nogrief.hooks.utils

import me.elephant1214.nogrief.hooks.pl3xmap.MapChunk
import net.pl3x.map.core.markers.Point
import net.pl3x.map.core.markers.marker.Marker
import net.pl3x.map.core.markers.marker.Polygon
import net.pl3x.map.core.markers.marker.Polyline
import java.awt.Shape
import java.awt.geom.Area
import java.awt.geom.Path2D
import java.awt.geom.PathIterator

fun getPoly(key: String, chunks: List<MapChunk>): Polygon {
    val area = Area()
    
    chunks.forEach { chunk ->
        val minX = chunk.minX.toDouble()
        val maxX = chunk.maxX().toDouble()
        val minZ = chunk.minZ.toDouble()
        val maxZ = chunk.maxZ().toDouble()
        val path = Path2D.Double()
        path.moveTo(minX, minZ)
        path.lineTo(minX, maxZ)
        path.lineTo(maxX, maxZ)
        path.lineTo(maxX, minZ)
        path.closePath()
        area.add(Area(path))
    }
    
    return Marker.polygon(key, toLines(key, area))
}

private fun toLines(key: String, shape: Shape): List<Polyline> {
    val lines = arrayListOf<Polyline>()
    var line = Polyline(key, Point.ZERO)
    val coords = DoubleArray(6)
    val iter = shape.getPathIterator(null, 1.0)
    
    while (!iter.isDone) {
        when (iter.currentSegment(coords)) {
            PathIterator.SEG_MOVETO -> line = Polyline(key, Point.of(coords[0], coords[1]))
            PathIterator.SEG_LINETO -> line.addPoint(Point.of(coords[0], coords[1]))
            PathIterator.SEG_CUBICTO -> lines.add(line)
        }
        iter.next()
    }
    
    return lines
}
