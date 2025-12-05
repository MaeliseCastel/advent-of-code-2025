package maelise.castel

import maelise.castel.utils.readLines

fun main() {
    val banks =
        readLines("/day03.txt").map { line ->
            val batteries =
                line
                    .mapIndexed { index, char -> index to char.digitToInt() }
                    .map { (index, joltage) -> Battery(position = index, joltage = joltage) }
            Bank(batteries)
        }
    val joltageProducedWithTwoBatteries =
        banks.map { bank ->
            val (first, second) = bank.getBiggestJoltagesInOrder(2)
            (first.toString() + second.toString()).toInt()
        }
    println(joltageProducedWithTwoBatteries.sum())
    val joltageProducedWithTwelveBatteries =
        banks.map { bank ->
            val twelveBiggestBatteriesInOrder = bank.getBiggestJoltagesInOrder(12)
            var finalNumberAsString = ""
            twelveBiggestBatteriesInOrder.forEach { finalNumberAsString += it.toString() }
            finalNumberAsString.toLong()
        }
    println(joltageProducedWithTwelveBatteries.sum())
}

private class Bank(val batteries: List<Battery>) {
    fun getBiggestJoltagesInOrder(numberOfJoltages: Int): List<Int> {
        val biggestBattery = batteries.subList(0, batteries.size - (numberOfJoltages - 1)).maxBy { it.joltage }
        val biggestBatteriesInOrder = mutableListOf(biggestBattery)
        for (index in 1 until numberOfJoltages) {
            val positionOfLastBiggestBattery = biggestBatteriesInOrder[index - 1].position
            val maxPositionToCompleteNumberOfJoltages = batteries.size - (numberOfJoltages - 1) + index
            val currentBiggest = batteries.subList(positionOfLastBiggestBattery + 1, maxPositionToCompleteNumberOfJoltages).maxBy { it.joltage }
            biggestBatteriesInOrder.add(currentBiggest)
        }
        return biggestBatteriesInOrder.map { it.joltage }
    }
}

private class Battery(val position: Int, val joltage: Int)
