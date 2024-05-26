package me.elephant1214.nogrief.squaremap.utils

import me.elephant1214.nogrief.claims.ClaimChunk
import xyz.jpenilla.squaremap.api.Point
import xyz.jpenilla.squaremap.api.marker.Marker
import xyz.jpenilla.squaremap.api.marker.Polygon
import java.awt.Shape
import java.awt.geom.Area
import java.awt.geom.Path2D
import java.awt.geom.PathIterator

fun getPoly(chunks: List<ClaimChunk>): Polygon {
    val sortedChunks = chunks.sortedWith(compareBy({ it.chunk.x }, { it.chunk.z }))
    var combined: List<Point> = mutableListOf()
    sortedChunks.forEach { chunk ->
        val x = (chunk.chunk.x shl 4).toDouble()
        val z = (chunk.chunk.z shl 4).toDouble()
        val points = listOf(
            Point.of(x, z),
            Point.of(x, z + 16),
            Point.of(x + 16, z + 16),
            Point.of(x + 16, z)
        )
        combined = if (combined.isEmpty()) {
            points
        } else {
            merge(combined, points)
        }
    }
    return Marker.polygon(combined)
}

private fun merge(a: List<Point>, b: List<Point>): List<Point> {
    val area = Area(toShape(a))
    area.add(Area(toShape(b)))
    return toPoints(area)
}

private fun toShape(points: List<Point>): Shape {
    val path = Path2D.Double()
    points.forEachIndexed { index, _ ->
        val point = points[index]
        if (index == 0) {
            path.moveTo(point.x(), point.z())
        } else {
            path.lineTo(point.x(), point.z())
        }
    }
    path.closePath()
    return path
}

private fun toPoints(shape: Shape): List<Point> {
    val result = mutableListOf<Point>()
    val pathIter = shape.getPathIterator(null, 0.0)
    val coords = DoubleArray(6)
    while (!pathIter.isDone) {
        val segment = pathIter.currentSegment(coords)
        when (segment) {
            PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> result.add(Point.of(coords[0], coords[1]))
        }
        pathIter.next()
    }
    return result
}
