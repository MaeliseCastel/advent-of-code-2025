package maelise.castel

import maelise.castel.utils.readLines

private const val ROLL_PAPER = '@'

fun main() {
    val matrix = Matrix(readLines("/day04.txt").map { line -> line.map { it } })

    val pickedUpPaper = getNumberOfPickedUpPapers(matrix)
    println("Number of picked up roll papers: $pickedUpPaper")
}

private data class Matrix(val grid: List<List<Char>>)

private fun getNumberOfPickedUpPapers(matrix: Matrix): Int {
    val (numberOfPickedUpPapers,matrixWithPickedUpPapers) = pickUpAccessiblePapers(matrix)
    if (numberOfPickedUpPapers == 0) {
        return 0
    }
    return numberOfPickedUpPapers + getNumberOfPickedUpPapers(matrixWithPickedUpPapers)
}

private fun pickUpAccessiblePapers(matrix: Matrix): Pair<Int, Matrix> {
    var count = 0
    val gridWithPickedUpPapers = matrix.grid.map { it.toMutableList() }
    for (line in matrix.grid.indices) {
        for (column in matrix.grid[line].indices) {
            val neighbours = getNeighbours(line, column, matrix)
            val numberOfSurroundingRollPapers = neighbours.count { it == ROLL_PAPER }
            if (matrix.grid[line][column] == ROLL_PAPER && numberOfSurroundingRollPapers < 4) {
                gridWithPickedUpPapers[line][column] = '.'
                count++
            }
        }
    }
    return Pair(count,Matrix(gridWithPickedUpPapers))
}

private fun getNeighbours(line: Int, column: Int, matrix: Matrix): List<Char> {
    return MatrixDirection.entries.mapNotNull { matrixDirection ->
        val neighbourLine = line + matrixDirection.lineDelta
        val neighbourColumn = column + matrixDirection.columnDelta
        if (isInMatrix(neighbourLine, neighbourColumn, matrix)) {
            matrix.grid[neighbourLine][neighbourColumn]
        } else {
            null
        }
    }
}

private fun isInMatrix(line: Int, column: Int, matrix: Matrix) = line in matrix.grid.indices && column in matrix.grid[line].indices

private enum class MatrixDirection(val lineDelta: Int, val columnDelta: Int) {
    UP(-1, 0),
    UP_LEFT(-1, -1),
    UP_RIGHT(-1, 1),
    DOWN(1, 0),
    DOWN_LEFT(1, -1),
    DOWN_RIGHT(1, 1),
    LEFT(0, -1),
    RIGHT(0, 1),
}
