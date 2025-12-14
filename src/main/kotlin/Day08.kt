package maelise.castel

import kotlin.math.pow
import kotlin.math.sqrt
import maelise.castel.utils.readLines

fun main() {
    val boxesCoordinates3D =
        readLines("/day08.txt").map { line ->
            val (x, y, z) = line.split(",").map { it.toLong() }
            Coordinates3D(x, y, z)
        }
    val connection = connectionBuildingOneLargeCircuit(boxesCoordinates3D)
    println(connection.from.x * connection.to.x)
    val circuits =
        buildCircuits(boxesCoordinates3D, numberOfShortestConnections = 1000)
    println(
        circuits
            .sortedByDescending { it.boxes.size }
            .take(3)
            .map { it.boxes.size.toLong() }.reduce { acc, size -> acc * size },
    )
}

private fun buildCircuits(boxesCoordinates3D: List<Coordinates3D>, numberOfShortestConnections: Int): List<Circuit> {
    val shortestConnections = buildShortestConnections(boxesCoordinates3D, numberOfShortestConnections)
    val circuits = mutableListOf<Circuit>()
    val circuitsOfConnections = mutableListOf<Circuit>()
    shortestConnections
        .flatMap { shortest -> listOf(shortest.from, shortest.to) }
        .forEach { coordinate ->
            val relatedConnections =
                shortestConnections.filter { connection ->
                    connection.from == coordinate || connection.to == coordinate
                }
            val distinct = relatedConnections.flatMap { listOf(it.from, it.to) }.distinct()
            val existingCircuits = circuitsOfConnections.find { it.boxes.any { box -> box in distinct } }
            if (existingCircuits != null) {
                existingCircuits.boxes.addAll(distinct)
            } else {
                val newCircuit = Circuit(distinct.toMutableSet())
                circuitsOfConnections.add(newCircuit)
            }
        }
    val notConnectedBoxesToUseInCircuits =
        boxesCoordinates3D.filter { box -> !isBoxInConnection(box, shortestConnections) }
    val singleCircuits = notConnectedBoxesToUseInCircuits.map { box -> Circuit(mutableSetOf(box)) }
    circuits.addAll(singleCircuits)
    circuits.addAll(circuitsOfConnections)
    return circuits
}

private fun buildShortestConnections(
    boxesCoordinates3D: List<Coordinates3D>,
    numberOfShortestConnections: Int
): List<Connection> {
    val shortestConnections = mutableSetOf<Connection>()
    boxesCoordinates3D.forEachIndexed { i, coordinates3D ->
        boxesCoordinates3D.forEachIndexed { j, otherCoordinates3D ->
            if (i != j) {
                val distance = euclideanDistance(coordinates3D, otherCoordinates3D)
                if (shortestConnections.size < numberOfShortestConnections) {
                    shortestConnections.add(Connection(coordinates3D, otherCoordinates3D, distance))
                } else {
                    val maxDistanceConnection = shortestConnections.maxBy { it.distance }
                    if (distance < maxDistanceConnection.distance) {
                        shortestConnections.add(Connection(coordinates3D, otherCoordinates3D, distance))
                        if (shortestConnections.size > numberOfShortestConnections) {
                            shortestConnections.remove(maxDistanceConnection)
                        }
                    }
                }
            }
        }
    }
    return shortestConnections.sortedBy { it.distance }
}

private data class Circuit(val boxes: MutableSet<Coordinates3D>)

private data class Connection(val from: Coordinates3D, val to: Coordinates3D, val distance: Long) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Connection) return false

        return (from == other.from && to == other.to) || (from == other.to && to == other.from)
    }

    override fun hashCode(): Int {
        return from.hashCode() + to.hashCode()
    }
}

private data class Coordinates3D(val x: Long, val y: Long, val z: Long)

private fun euclideanDistance(pointA: Coordinates3D, pointB: Coordinates3D): Long {
    val deltaX = squared((pointA.x - pointB.x))
    val deltaY = squared((pointA.y - pointB.y))
    val deltaZ = squared((pointA.z - pointB.z))
    return sqrt(deltaX + deltaY + deltaZ).toLong()
}

private fun squared(value: Long): Double = value.toDouble().pow(2.0)

private fun isBoxInConnection(box: Coordinates3D, connections: List<Connection>): Boolean {
    return connections.any { connection -> connection.from == box || connection.to == box }
}

private fun connectionBuildingOneLargeCircuit(boxesCoordinates3D: List<Coordinates3D>): Connection {
    val circuits = mutableListOf<Circuit>()
    val connections = mutableListOf<Connection>()
    var latestConnection: Connection? = null
    while (!isOneLargeCircuit(boxesCoordinates3D, circuits)) {
        latestConnection =
            computeShortestUnexistingConnection(boxesCoordinates3D, connections, latestConnection?.distance ?: 0L)
        connections.removeAll { it.distance < latestConnection.distance }
        connections.add(latestConnection)
        val circuitContainingFromBox = circuits.find { circuit -> latestConnection.from in circuit.boxes }
        val circuitContainingToBox = circuits.find { circuit -> latestConnection.to in circuit.boxes }
        when {
            circuitContainingFromBox == null && circuitContainingToBox == null -> {
                val newCircuit = Circuit(mutableSetOf(latestConnection.from, latestConnection.to))
                circuits.add(newCircuit)
            }
            circuitContainingFromBox != null && circuitContainingToBox == null -> {
                circuitContainingFromBox.boxes.add(latestConnection.to)
            }
            circuitContainingFromBox == null && circuitContainingToBox != null -> {
                circuitContainingToBox.boxes.add(latestConnection.from)
            }
            circuitContainingFromBox != null &&
                circuitContainingToBox != null &&
                circuitContainingFromBox != circuitContainingToBox -> {
                circuitContainingFromBox.boxes.addAll(circuitContainingToBox.boxes)
                circuits.remove(circuitContainingToBox)
            }
        }
    }
    return latestConnection!!
}

private fun isOneLargeCircuit(boxesCoordinates3D: List<Coordinates3D>, circuits: MutableList<Circuit>): Boolean {
    if (circuits.isEmpty()) return false
    return boxesCoordinates3D.all { box -> box in circuits.first().boxes }
}

private fun computeShortestUnexistingConnection(
    boxesCoordinates3D: List<Coordinates3D>,
    existingConnections: List<Connection>,
    minimumDistance: Long
): Connection {
    var shortestConnection: Connection? = null
    boxesCoordinates3D.forEachIndexed { i, coordinates3D ->
        boxesCoordinates3D.forEachIndexed { j, otherCoordinates3D ->
            if (i != j) {
                val potentialConnection =
                    Connection(coordinates3D, otherCoordinates3D, euclideanDistance(coordinates3D, otherCoordinates3D))
                if (potentialConnection.distance >= minimumDistance && potentialConnection !in existingConnections) {
                    if (shortestConnection == null || potentialConnection.distance < shortestConnection!!.distance) {
                        shortestConnection = potentialConnection
                    }
                }
            }
        }
    }
    return shortestConnection!!
}
