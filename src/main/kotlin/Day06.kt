package maelise.castel

import maelise.castel.utils.readColumns

fun main() {
    val verticals = readVerticalLines("/day06.txt")
    val sumOfVerticalsComputation = computeVerticals(verticals)
    println("The sum of the verticals is ${sumOfVerticalsComputation.sum()}")
}

private fun readVerticalLines(filePath: String): List<Vertical> {
    val columns = readColumns(filePath)
    val lengthOperand = columns.maxBy { it.length }.length - 1
    var operator = '+'
    val verticals = mutableListOf<Vertical>()
    val currentOperands = mutableListOf<String>()
    columns.forEach { column ->
        if (column.isBlank() || column.isEmpty()) return@forEach
        val potentialOperator = column.last()
        if (potentialOperator.isWhitespace() && column.isNotBlank()) {
            currentOperands.add(column.dropLast(1))
        } else if (potentialOperator == '+' || potentialOperator == '*') {
            verticals.add(Vertical(currentOperands.map { it.replace(" ", "0").toLong() }, operator))
            operator = potentialOperator
            currentOperands.clear()
            currentOperands.add(column.dropLast(1))
        } else {
            if (column.length < lengthOperand) {
                column.padStart(lengthOperand - column.length, '0')
            }
            currentOperands.add(column)
        }
    }
    verticals.add(Vertical(currentOperands.map { it.replace(" ", "0").toLong() }, operator))
    return verticals.filter { it.operands.isNotEmpty() }
}

private data class Vertical(val operands: List<Long>, val operator: Char) {
    fun compute(): Long {
        val zeroFreeOperands = operands.removeZeros()
        return when (operator) {
            '+' -> zeroFreeOperands.sumOf { it }
            '*' -> zeroFreeOperands.map { it }.reduce { acc, l -> if (l == 0L) acc else acc * l }
            else -> throw IllegalArgumentException("Unknown operator: $operator")
        }
    }

    private fun List<Long>.removeZeros(): List<Long> = this.map { it.toString().replace("0", "").toLong() }
}

private fun computeVerticals(verticals: List<Vertical>): List<Long> {
    return verticals.map { vertical -> vertical.compute() }
}
