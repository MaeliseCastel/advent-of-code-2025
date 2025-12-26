package maelise.castel

import maelise.castel.utils.Matrix
import maelise.castel.utils.readLines

fun main() {
    val exampleGiftShapes =
        listOf(
            GiftShape(
                Matrix(
                    listOf(
                        listOf(true, true, true),
                        listOf(true, true, false),
                        listOf(true, true, false),
                    ),
                )),
            GiftShape(
                Matrix(
                    listOf(
                        listOf(true, true, true),
                        listOf(true, true, false),
                        listOf(false, true, true),
                    ),
                )),
            GiftShape(
                Matrix(
                    listOf(
                        listOf(false, true, true),
                        listOf(true, true, true),
                        listOf(true, true, false),
                    ),
                )),
            GiftShape(
                Matrix(
                    listOf(
                        listOf(true, true, false),
                        listOf(true, true, true),
                        listOf(true, true, false),
                    ),
                )),
            GiftShape(
                Matrix(
                    listOf(
                        listOf(true, true, true),
                        listOf(true, false, false),
                        listOf(true, true, true),
                    ),
                )),
            GiftShape(
                Matrix(
                    listOf(
                        listOf(true, true, true),
                        listOf(false, true, false),
                        listOf(true, true, true),
                    ),
                )),
        )

    val inputGiftShapes =
        listOf(
            GiftShape(
                Matrix(
                    listOf(
                        listOf(true, true, true),
                        listOf(true, true, false),
                        listOf(false, true, true),
                    ),
                ),
            ),
            GiftShape(
                Matrix(
                    listOf(
                        listOf(true, true, true),
                        listOf(true, true, true),
                        listOf(false, false, true),
                    ),
                ),
            ),
            GiftShape(
                Matrix(
                    listOf(
                        listOf(true, false, false),
                        listOf(true, true, false),
                        listOf(true, true, true),
                    ),
                ),
            ),
            GiftShape(
                Matrix(
                    listOf(
                        listOf(true, true, true),
                        listOf(false, false, true),
                        listOf(true, true, true),
                    ),
                ),
            ),
            GiftShape(
                Matrix(
                    listOf(
                        listOf(true, true, true),
                        listOf(false, true, false),
                        listOf(true, true, true),
                    ),
                ),
            ),
            GiftShape(
                Matrix(
                    listOf(
                        listOf(false, false, true),
                        listOf(false, true, true),
                        listOf(true, true, false),
                    ),
                ),
            ),
        )

    val allWithoutTooSmall = readLines("/day12.txt")
        .map { line ->
            val (dimensions, giftsToPlaceString) = line.split(":")
            val (nbRoms, nbColumns) = dimensions.split("x").map { it.toInt() }
            val giftsToPlace =
                giftsToPlaceString.split(" ").mapNotNull {
                    if (it.isEmpty() || it.isBlank()) null else it.trim().toInt()
                }
            TreeZone(nbRoms, nbColumns, giftsToPlace)
        }
        .filter { it.easySolveA(inputGiftShapes) }
    val treeZones =
        allWithoutTooSmall
            .filter { !it.easySolveB() }
            .sortedBy { treeZone -> treeZone.getNumberOfGiftsToPlace() }

    println(allWithoutTooSmall.size - treeZones.size)
    treeZones.forEach { println("${it.nbRows}x${it.nbColumns} with ${it.nbGiftsToPlace.sum()} gifts: ${it.nbGiftsToPlace}") }
//    println(treeZones.size)

//        val start = System.currentTimeMillis()
//        val nbOfRegionsWithAllGiftsPlaced =
//            treeZones.mapIndexed { index, treeZone ->
//                println("$treeZone with ${treeZone.getNumberOfGiftsToPlace()} gifts to place")
//                if (treeZone.canPlaceAllGifts(exampleGiftShapes)) {
//                    println("Zone $index: all gifts can be placed in ${System.currentTimeMillis() - start} ms")
//                    1L
//                } else {
//                    0L
//                }
//            }

//        println(nbOfRegionsWithAllGiftsPlaced.sumOf { it })
}

private data class GiftShape(val matrix: Matrix<Boolean>) {
    fun getAllOrientations(): Set<GiftShape> {
        val orientations = mutableSetOf<GiftShape>()
        var currentMatrix = matrix
        repeat(4) {
            orientations.add(GiftShape(currentMatrix))
            currentMatrix = currentMatrix.rotate()
        }
        return orientations
    }

    fun getTakenSpace(): Int {
        return matrix.grid.sumOf { row -> row.count { it } }
    }
}

private data class TreeZone(val nbRows: Int, val nbColumns: Int, val nbGiftsToPlace: List<Int>) {
    fun getNumberOfGiftsToPlace(): Int {
        return nbGiftsToPlace.sum()
    }

    fun easySolveA(gifts: List<GiftShape>): Boolean {
        val giftsToPlace = mutableListOf<GiftShape>()
        nbGiftsToPlace.forEachIndexed { index, nbGift -> repeat(nbGift) { giftsToPlace.add(gifts[index]) } }
        val sizeOfAllGifts = giftsToPlace.sumOf { it.getTakenSpace() }
        val sizeOfGrid = nbRows * nbColumns
        if (sizeOfAllGifts > sizeOfGrid) return false
        return true
    }

    fun easySolveB(): Boolean {
        val sizeOfAllGifts = 9 * nbGiftsToPlace.sum()
        val sizeOfGrid = (nbRows / 3) * 3 * (nbColumns / 3) * 3
        return sizeOfGrid > sizeOfAllGifts
    }

//    fun canPlaceAllGifts(gifts: List<GiftShape>): Boolean {
//    }
}

private fun Matrix<Boolean>.rotate(): Matrix<Boolean> {
    val numRows = this.numberOfRows
    val numCols = this.numberOfColumns
    val rotatedMatrix = this.grid.map { row -> row.toMutableList() }.toMutableList()
    for (i in 0 until numRows) {
        for (j in 0 until numCols) {
            rotatedMatrix[j][numRows - 1 - i] = this.grid[i][j]
        }
    }
    return Matrix(rotatedMatrix)
}
