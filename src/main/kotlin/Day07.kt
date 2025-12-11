package maelise.castel

import maelise.castel.utils.Matrix
import maelise.castel.utils.readLines

private const val SPLITTER = '^'
private const val START = 'S'
private const val BEAMS = '|'
private const val FREE_SPACE = '.'

fun main() {
    val tachyonManifold = Matrix(readLines("/day07.txt").map { line -> line.toList() })
    val manifoldWithMovedBeams = computeNumberOfSplit(tachyonManifold)
    println("Number of splitters in the tachyon manifold: $manifoldWithMovedBeams")
    val numberOfTimelines = computeTimelines(tachyonManifold)
    println("Number of timelines: $numberOfTimelines")
}

private fun computeNumberOfSplit(matrix: Matrix<Char>): Int {
    val startingPosition = getStartingPosition(matrix).moveDown(matrix)!!
    val coordinatesToMoveBeams = ArrayDeque<MatrixCoordinates>()
    coordinatesToMoveBeams.add(startingPosition)
    val alreadyVisited = mutableSetOf(startingPosition)
    val visitedGrid = matrix.grid.map { char -> char.toMutableList() }.toMutableList()
    var numberOfSplits = 0

    while (coordinatesToMoveBeams.isNotEmpty()) {
        val currentCoordinates = coordinatesToMoveBeams.removeFirst()
        if (visitedGrid[currentCoordinates.line][currentCoordinates.column] == SPLITTER) {
            val newCoordinatesToMoveBeams =
                currentCoordinates.moveAroundSplitter(matrix).filter { coordinates -> coordinates !in alreadyVisited }
            numberOfSplits++

            coordinatesToMoveBeams.addAll(newCoordinatesToMoveBeams)
        } else if (visitedGrid[currentCoordinates.line][currentCoordinates.column] == FREE_SPACE) {
            val newCoordinatesToMoveBeams = currentCoordinates.moveDown(matrix)
            if (newCoordinatesToMoveBeams != null && newCoordinatesToMoveBeams !in alreadyVisited) {
                coordinatesToMoveBeams.add(newCoordinatesToMoveBeams)
            }
            visitedGrid[currentCoordinates.line][currentCoordinates.column] = BEAMS
        }
    }
    return numberOfSplits
}

private fun computeTimelines(matrix: Matrix<Char>): Int {
    val startingPosition = getStartingPosition(matrix).moveDown(matrix)!!
    val numberOfPathsFromCoordinates = mutableMapOf<MatrixCoordinates, Int>()
    val visitedGrid = matrix.grid.map { char -> char.toMutableList() }.toMutableList()

    fun getNumberOfPathsFromCoordinates(coordinates: MatrixCoordinates): Int {
        numberOfPathsFromCoordinates[coordinates]?.let { return it }
        if (coordinates.isOnLastLine(matrix)) {
            numberOfPathsFromCoordinates[coordinates] = 1
            return 1
        }
        var numberOfPaths = 0
        if (matrix.grid[coordinates.line][coordinates.column] == SPLITTER) {
            val newCoordinatesToMoveBeams = coordinates.moveAroundSplitter(matrix)
            for (newCoordinates in newCoordinatesToMoveBeams) {
                numberOfPaths += getNumberOfPathsFromCoordinates(newCoordinates)
            }
        } else if (matrix.grid[coordinates.line][coordinates.column] == FREE_SPACE) {
            val newCoordinatesToMoveBeams = coordinates.moveDown(matrix)
            visitedGrid[coordinates.line][coordinates.column] = BEAMS
            if (newCoordinatesToMoveBeams != null) {
                numberOfPaths += getNumberOfPathsFromCoordinates(newCoordinatesToMoveBeams)
            }
        }
        numberOfPathsFromCoordinates[coordinates] = numberOfPaths
        return numberOfPaths
    }

    val totalNumberOfPaths = getNumberOfPathsFromCoordinates(startingPosition)
    visitedGrid.forEach { line ->
        println(line)
    }
    return totalNumberOfPaths
}

private fun getStartingPosition(matrix: Matrix<Char>): MatrixCoordinates {
    val startingColumn = matrix.grid[0].indexOf(START)
    return MatrixCoordinates(0, startingColumn)
}

private data class MatrixCoordinates(val line: Int, val column: Int) {
    fun moveDown(matrix: Matrix<Char>): MatrixCoordinates? {
        val newCoordinates = MatrixCoordinates(line + 1, column)
        return if (newCoordinates.isInMatrix(matrix)) newCoordinates else null
    }

    fun moveAroundSplitter(matrix: Matrix<Char>): List<MatrixCoordinates> {
        val left = MatrixCoordinates(line, column - 1)
        val right = MatrixCoordinates(line, column + 1)
        return listOfNotNull(
            if (left.isInMatrix(matrix)) left else null,
            if (right.isInMatrix(matrix)) right else null,
        )
    }

    fun isOnLastLine(matrix: Matrix<Char>): Boolean =
        line == matrix.grid.lastIndex
}

private fun MatrixCoordinates.isInMatrix(matrix: Matrix<Char>): Boolean =
    line in matrix.grid.indices && column in matrix.grid[line].indices

// 68991020 -> too low
