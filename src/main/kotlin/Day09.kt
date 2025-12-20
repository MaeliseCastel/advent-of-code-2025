package maelise.castel

import kotlin.math.abs
import maelise.castel.utils.readLines

fun main() {
    val coordinates =
        readLines("/day09.txt").map { line ->
            val (x, y) = line.split(",").map { it.toLong() }
            Coordinates(x, y)
        }
    val lines = buildLines(coordinates)
    val shape = Shape(lines)

    val area =
        coordinates
            .mapIndexed { index, currentCoordinates ->
                coordinates
                    .mapIndexed { otherIndex, otherCoordinates ->
                        if (index != otherIndex) {
                            val rectangle = buildRectangle(currentCoordinates, otherCoordinates)
                            val pointsToCheck = rectangle.lines.flatMap { line -> line.getAllPoints() }.toSet()
                            if (pointsToCheck.all { point ->
                                shape.isCoordinateInside(point) || lines.any { line -> line.isPointOnLine(point) }
                            }) {
                                computeArea(currentCoordinates, otherCoordinates)
                            } else {
                                0L
                            }
                        } else {
                            0L
                        }
                    }
                    .max()
            }
            .max()
    println("Largest area: $area")
}

data class Coordinates(val x: Long, val y: Long)

private data class Rectangle(val corners: List<Coordinates>) {
    val lines: Set<Line> = buildLines(corners)
}

private fun buildRectangle(firstCoordinates: Coordinates, secondCoordinates: Coordinates): Rectangle {
    if (firstCoordinates.x == secondCoordinates.x || firstCoordinates.y == secondCoordinates.y) {
        return Rectangle(listOf(firstCoordinates, secondCoordinates))
    }
    return Rectangle(
        listOf(
            Coordinates(firstCoordinates.x, firstCoordinates.y),
            Coordinates(firstCoordinates.x, secondCoordinates.y),
            Coordinates(secondCoordinates.x, firstCoordinates.y),
            Coordinates(secondCoordinates.x, secondCoordinates.y),
        ),
    )
}

private fun computeArea(firstCoordinates: Coordinates, secondCoordinates: Coordinates): Long {
    val length = maxOf(abs(firstCoordinates.x - secondCoordinates.x) + 1, 1)
    val width = maxOf(abs(firstCoordinates.y - secondCoordinates.y) + 1, 1)
    return length * width
}

private data class Line(val start: Coordinates, val end: Coordinates) {
    fun isVertical() = start.x == end.x

    fun isHorizontal() = start.y == end.y

    fun getAllPoints() =
        if (isVertical()) {
            (start.y..end.y).map { y -> Coordinates(start.x, y) }
        } else (start.x..end.x).map { x -> Coordinates(x, start.y) }

    fun isPointOnLine(coordinates: Coordinates): Boolean {
        val (x1, y1) = start
        val (x2, y2) = end
        return when {
            x1 == x2 -> coordinates.x == x1 && coordinates.y in minOf(y1, y2)..maxOf(y1, y2)
            y1 == y2 -> coordinates.y == y1 && coordinates.x in minOf(x1, x2)..maxOf(x1, x2)
            else -> false
        }
    }
}

private fun isRectangleInsideShape(rectangle: Rectangle, shape: Shape): Boolean {
    rectangle.lines.forEach { rectangleLine ->
        shape.lines.forEach { shapeLine ->
            if (doLinesIntersect(rectangleLine, shapeLine)) {
                return false
            }
        }
    }
    return true
}

private fun doLinesIntersect(rectangleLine: Line, shapeLine: Line): Boolean {
    if (rectangleLineIncludedInShapeLine(rectangleLine, shapeLine)) return false
    if (rectangleLine.isVertical() && shapeLine.isHorizontal()) {
        val rectangleX = rectangleLine.start.x
        val shapeY = rectangleLine.start.y
        val minY = minOf(rectangleLine.start.y, rectangleLine.end.y)
        val maxY = maxOf(rectangleLine.start.y, rectangleLine.end.y)
        val minX = minOf(shapeLine.start.x, shapeLine.end.x)
        val maxX = maxOf(shapeLine.start.x, shapeLine.end.x)
        // TODO voir comment gérer les extremités genre quand la fin d'une retangleLine touche la shapeLine (aujourd'hui
        // ça compte comme une intersection mais ça n'en est pas une)
        return rectangleX in minX..maxX && shapeY in minY..maxY
    } else if (rectangleLine.isHorizontal() && shapeLine.isVertical()) {
        val rectangleY = rectangleLine.start.y
        val shapeX = shapeLine.start.x
        val minX = minOf(rectangleLine.start.x, rectangleLine.end.x)
        val maxX = maxOf(rectangleLine.start.x, rectangleLine.end.x)
        val minY = minOf(shapeLine.start.y, shapeLine.end.y)
        val maxY = maxOf(shapeLine.start.y, shapeLine.end.y)
        return shapeX in minX..maxX && rectangleY in minY..maxY
    }
    return false
}

private fun rectangleLineIncludedInShapeLine(rectangleLine: Line, shapeLine: Line): Boolean {
    if (rectangleLine.isVertical() && shapeLine.isVertical() && rectangleLine.start.x == shapeLine.start.x) {
        val maxY = maxOf(shapeLine.start.y, shapeLine.end.y)
        val minY = minOf(shapeLine.start.y, shapeLine.end.y)
        return rectangleLine.start.y in minY..maxY && rectangleLine.end.y in minY..maxY
    } else if (rectangleLine.isHorizontal() && shapeLine.isHorizontal() && rectangleLine.start.y == shapeLine.start.y) {
        val maxX = maxOf(shapeLine.start.x, shapeLine.end.x)
        val minX = minOf(shapeLine.start.x, shapeLine.end.x)
        return rectangleLine.start.x in minX..maxX && rectangleLine.end.x in minX..maxX
    }
    return false
}

private fun buildLines(coordinates: List<Coordinates>): Set<Line> {
    val lines = mutableSetOf<Line>()
    coordinates.zipWithNext { coordinateA, coordinateB ->
        if (coordinateA.x == coordinateB.x) {
            if (coordinateA.y < coordinateB.y) {
                lines.add(Line(coordinateA, coordinateB))
            } else {
                lines.add(Line(coordinateB, coordinateA))
            }
        } else {
            if (coordinateA.x < coordinateB.x) {
                lines.add(Line(coordinateA, coordinateB))
            } else {
                lines.add(Line(coordinateB, coordinateA))
            }
        }
    }
    val firstCoordinates = coordinates.first()
    val lastCoordinates = coordinates.last()
    if (firstCoordinates.x == lastCoordinates.x) {
        if (firstCoordinates.y < lastCoordinates.y) {
            lines.add(Line(firstCoordinates, lastCoordinates))
        } else {
            lines.add(Line(lastCoordinates, firstCoordinates))
        }
    } else {
        if (firstCoordinates.x < lastCoordinates.x) {
            lines.add(Line(firstCoordinates, lastCoordinates))
        } else {
            lines.add(Line(lastCoordinates, firstCoordinates))
        }
    }
    return lines
}

private class Shape(val lines: Set<Line>) {
    private val insideCache = mutableMapOf<Coordinates, Boolean>()
    fun isCoordinateInside(coordinates: Coordinates): Boolean {
        return insideCache.getOrPut(coordinates) {
            var crossings = 0
            for (line in lines) {
                val (x1, y1) = line.start
                val (x2, y2) = line.end
                if ((y1 > coordinates.y) != (y2 > coordinates.y)) {
                    val intersectionX = (x2 - x1) * (coordinates.y - y1) / (y2 - y1).toDouble() + x1
                    if (coordinates.x < intersectionX) {
                        crossings++
                    }
                }
            }
            crossings % 2 == 1
        }
    }
}
