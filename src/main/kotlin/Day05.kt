package maelise.castel

import maelise.castel.utils.Range
import maelise.castel.utils.readText

fun main() {
    val (rangesText, idsText) = readText("/day05.txt").split("\n\n")
    val rangesOfFreshIngredients =
        rangesText.lines().map { range ->
            val (start, end) = range.split("-")
            Range(start.toLong(), end.toLong())
        }
    val ingredientIds = idsText.lines().map { id -> id.toLong() }
    val freshIngredientsNumber = getFreshIngredientsNumber(rangesOfFreshIngredients, ingredientIds)
    println(freshIngredientsNumber)

    val mergedRanges = mergeRanges(rangesOfFreshIngredients)
    mergedRanges.sortedBy { it.start }.forEach { println(it) }
    val numberOfIdsInRanges = getNumberOfIdsInRanges(mergedRanges)
    println("Numbers in ranges: $numberOfIdsInRanges")
}

private fun getFreshIngredientsNumber(rangesOfFreshIngredients: List<Range>, ingredientIds: List<Long>): Int {
    var freshIngredientsNumber = 0
    ingredientIds.forEach { id ->
        val isFresh = rangesOfFreshIngredients.any { range -> id in range.start..range.end }
        if (isFresh) {
            freshIngredientsNumber++
        }
    }
    return freshIngredientsNumber
}

private fun mergeRanges(rangesOfFreshIngredients: List<Range>): List<Range> {
    val currentMergedRanges = rangesOfFreshIngredients.distinct().toMutableList()
    for (index in 0 until rangesOfFreshIngredients.size) {
        val currentRange = rangesOfFreshIngredients[index]
        val rangesInRange = getRangesInRange(currentRange, rangesOfFreshIngredients.distinct())
        if (rangesInRange.isEmpty()) continue
        val smallestStart = minOf(currentRange.start, rangesInRange.minBy { it.start }.start)
        val biggestEnd = maxOf(currentRange.end, rangesInRange.maxBy { it.end }.end)
        currentMergedRanges.add(Range(start = smallestStart, end = biggestEnd))
        currentMergedRanges.remove(currentRange)
        for (range in rangesInRange) {
            currentMergedRanges.remove(range)
        }
    }
    if (currentMergedRanges.size == rangesOfFreshIngredients.size) {
        return currentMergedRanges
    }
    return mergeRanges(currentMergedRanges)
}

private fun getRangesInRange(referenceRange: Range, otherRanges: List<Range>): List<Range> {
    val rangesInRange = mutableListOf<Range>()
    otherRanges.forEach { range ->
        if (range == referenceRange) return@forEach
        if (isInRange(referenceRange, range)) {
            rangesInRange.add(range)
        }
    }
    return rangesInRange
}

private fun isInRange(range: Range, otherRange: Range): Boolean {
    return otherRange.start in range.start..range.end || otherRange.end in range.start..range.end
}

private fun getNumberOfIdsInRanges(rangesOfFreshIngredients: List<Range>): Long =
    rangesOfFreshIngredients.sumOf { range -> range.end - range.start + 1 }

//259500676899025
//334606229031332
