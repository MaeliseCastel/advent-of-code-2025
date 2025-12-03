package maelise.castel

import maelise.castel.utils.readText

fun main() {
    val ranges =
        readText("/day02.txt").split(",").map { range ->
            val (start, end) = range.split("-")
            Range(start.toLong(), end.toLong())
        }

    val numbersWithRepeatedSequences = ranges.flatMap { it.getNumbersWithRepeatedSequences() }
    println(numbersWithRepeatedSequences.sum())
}

private data class Range(val start: Long, val end: Long) {
    val numbersOfTheRange = (start..end).toList()

    fun getNumbersWithRepeatedSequences(): List<Long> {
        val numberWithRepeatedSequences = mutableListOf<Long>()
        numbersOfTheRange.forEach { number ->
            if (number in numberWithRepeatedSequences) return@forEach
            val size = number.toString().length
            val isEvenSized = size % 2 == 0
            for (sequenceSize in 1..size / 2) {
                if (sequenceSize % 2 == 0 && !isEvenSized) continue
                if (size == 1) {
                    numberWithRepeatedSequences.add(number)
                    return@forEach
                } else if (size % sequenceSize == 0) {
                    val parts = number.toString().chunked(sequenceSize)
                    if (parts.areEqual()) {
                        numberWithRepeatedSequences.add(number)
                        return@forEach
                    }
                }
            }
        }
        return numberWithRepeatedSequences
    }
}

private fun List<String>.areEqual(): Boolean {
    if (this.isEmpty()) return false
    val first = this.first()
    return this.all { it == first }
}
