package maelise.castel

import maelise.castel.utils.Matrix
import maelise.castel.utils.readLines

// Helped by mdeshayes (thanks!!) for
// 1. great advice on how to reason about general problems: find the immediate solution to the problem first, to prune
// down the possibilities (and here to just solve the problem)
// 2. spotting a bug in my first implementation of canFitAllGifts()

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

    val treeZones =
        readLines("/day12.txt").map { line ->
            val (dimensions, giftsToPlaceString) = line.split(":")
            val (nbRoms, nbColumns) = dimensions.split("x").map { it.toInt() }
            val giftsToPlace =
                giftsToPlaceString.split(" ").mapNotNull {
                    if (it.isEmpty() || it.isBlank()) null else it.trim().toInt()
                }
            TreeZone(nbRoms, nbColumns, giftsToPlace)
        }

    val treeZonesWithoutTooSmallOnes = treeZones.filterNot { treeZone -> treeZone.isTooSmall(inputGiftShapes) }

    val treeZoneThatCanForSureFitAllGifts =
        treeZonesWithoutTooSmallOnes.filter { treeZone -> treeZone.canFitAllGifts() }

    println("Number of tree zones: ${treeZones.size}")
    println("Number of tree zones without too small ones: ${treeZonesWithoutTooSmallOnes.size}")
    println("Number of tree zones that can for sure fit all gifts: ${treeZoneThatCanForSureFitAllGifts.size}")
}

private data class GiftShape(val matrix: Matrix<Boolean>) {
    fun getTakenSpace(): Int {
        return matrix.grid.sumOf { row -> row.count { it } }
    }
}

private data class TreeZone(val nbRows: Int, val nbColumns: Int, val nbGiftsToPlace: List<Int>) {
    fun getNumberOfGiftsToPlace(): Int = nbGiftsToPlace.sum()

    fun isTooSmall(gifts: List<GiftShape>): Boolean {
        val giftsToPlace = mutableListOf<GiftShape>()
        nbGiftsToPlace.forEachIndexed { index, nbGift -> repeat(nbGift) { giftsToPlace.add(gifts[index]) } }
        val sizeOfAllGifts = giftsToPlace.sumOf { it.getTakenSpace() }
        val sizeOfGrid = nbRows * nbColumns
        return sizeOfAllGifts > sizeOfGrid
    }

    fun canFitAllGifts(): Boolean {
        val sizeOfAllGifts = 9 * getNumberOfGiftsToPlace()
        val sizeOfGrid = ((nbRows / 3) * 3).toLong() * ((nbColumns / 3) * 3)
        return sizeOfGrid >= sizeOfAllGifts
    }
}
