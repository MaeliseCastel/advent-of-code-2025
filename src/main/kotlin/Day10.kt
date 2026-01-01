package maelise.castel

import java.util.*
import kotlin.math.roundToLong
import maelise.castel.utils.readLines

private const val LIGHT_ON = '#'
private const val LIGHT_OFF = '.'

fun main() {
    val indicatorLightsRegex = Regex("\\[([.#]+)\\]")
    val buttonWiringSchematicsRegex = Regex("\\(([^)]+)\\)")
    val joltageRequirementsRegex = Regex("\\{([\\d,\\s]+)\\}")
    val machineManuals =
        readLines("/day10.txt").map { line ->
            val indicatorLights =
                indicatorLightsRegex.find(line)?.groupValues[1]!!.map { char ->
                    when (char) {
                        LIGHT_ON -> true
                        LIGHT_OFF -> false
                        else -> throw IllegalArgumentException("Invalid character for indicator light: $char")
                    }
                }
            val buttonWiringSchematics =
                buttonWiringSchematicsRegex
                    .findAll(line)
                    .map { match -> match.groupValues[1].split(",").map { it.toInt() } }
                    .toList()
            val joltageRequirements =
                joltageRequirementsRegex.find(line)?.groupValues[1]!!.split(",").map { it.toInt() }
            MachineManual(indicatorLights, buttonWiringSchematics, joltageRequirements)
        }

    //    val sortedMachineManuals = machineManuals.sortedBy { it.buttonWiringSchematics.size }
    val joltageSystems =
        machineManuals.mapIndexed { indexMachinalManual, machineManual ->
            val equations =
                machineManual.joltageRequirements.mapIndexed { indexJoltageRequirement, joltageRequirement ->
                    val operands =
                        machineManual.buttonWiringSchematics
                            .mapIndexed { index, buttonWiringSchematics ->
                                if (buttonWiringSchematics.contains(indexJoltageRequirement)) index else null
                            }
                            .filterNotNull()
                    JoltageEquation(joltageRequirement, operands)
                }
            JoltageSystem(equations, indexMachinalManual)
        }

    val (uniqueSolutions, multipleSolutions) =
        joltageSystems
            .mapIndexed { index, joltageSystem -> index to solveJoltageSystem(joltageSystem) }
            .partition { (_, solutionResult) ->
                solutionResult is JoltageRequirementSolution.UniqueJoltageRequirementSolution
            }

    println(uniqueSolutions.size)
    val uniqueSolutionsNumberOfSteps =
        uniqueSolutions.sumOf { (_, solutionResult) ->
            val uniqueSolution = solutionResult as JoltageRequirementSolution.UniqueJoltageRequirementSolution
            uniqueSolution.values.values.sumOf { it.toLong() }
        }
    println("Sum of minimum steps to configure joltages for unique solutions: $uniqueSolutionsNumberOfSteps")

    val multipleSolutionsNumberOfSteps =
        multipleSolutions.sumOf { (index, solutionResult) ->
            getMinimumStepsToConfigureJoltageWithReducedSystemAStar(
                solutionResult as JoltageRequirementSolution.ReducedSystem,
                machineManuals[index],
            )
        }

//    println(getMinimumStepsToConfigureJoltageWithReducedSystemAStar(
//        multipleSolutions[2].second as JoltageRequirementSolution.ReducedSystem,
//        machineManuals[2],
//    ))
//
//    println(machineManuals[2].joltageRequirements)

//    println("Sum of minimum steps to configure joltages for multiple solutions: $multipleSolutionsNumberOfSteps")

//    println(uniqueSolutionsNumberOfSteps + multipleSolutionsNumberOfSteps)
    //    println(machineManuals)
    //    val minimumStepsToConfigureLights =
    //        machineManuals.sumOf { machineManual -> val minimumStepsToConfigureLights =
    //            getMinimumStepsToConfigureLights(machineManual)
    //            minimumStepsToConfigureLights
    //        }
    //    println("Sum of minimum steps to configure lights: $minimumStepsToConfigureLights")

    //    var count = 0
    //    val size = sortedMachineManuals.size
    //    val startTime = System.currentTimeMillis()
    //    val minimumStepsToConfigureJoltages =
    //        sortedMachineManuals.sumOf { machineManual ->
    //            val minimumStepsToConfigureJoltage = getMinimumStepsToConfigureJoltageAStar(machineManual)
    //            val endTime = System.currentTimeMillis()
    //            count++
    //            println(
    //                "Did $count/$size in ${endTime - startTime} ms - machine manual size:
    // ${machineManual.buttonWiringSchematics.size}")
    //            minimumStepsToConfigureJoltage
    //        }
    //    println("Sum of minimum steps to configure joltages: $minimumStepsToConfigureJoltages")
}

private data class MachineManual(
    val indicatorLights: List<Boolean>,
    val buttonWiringSchematics: List<List<Int>>,
    val joltageRequirements: List<Int>
)

private data class NodeOfLights(
    val stateOfLights: List<Boolean>,
    val parent: NodeOfLights? = null,
    val numberOfVisitedNodsSoFar: Int = 0,
) {
    fun estimatedCost(goalState: List<Boolean>): Long =
        (parent?.estimatedCost(goalState) ?: heuristicForLights(stateOfLights, goalState)) +
            heuristicForLights(stateOfLights, goalState) +
            numberOfVisitedNodsSoFar
}

private fun getMinimumStepsToConfigureLights(machineManual: MachineManual): Long {
    val currentStateOfLights = (0..<machineManual.indicatorLights.size).map { false }
    val goalStateOfLights = machineManual.indicatorLights
    val initialNode = NodeOfLights(currentStateOfLights)
    val nodesToVisit = PriorityQueue<NodeOfLights>((compareBy { it.estimatedCost(goalStateOfLights) }))
    nodesToVisit.add(initialNode)
    while (nodesToVisit.isNotEmpty()) {
        val currentNode = nodesToVisit.poll()
        if (currentNode.stateOfLights == goalStateOfLights) {
            val path = reconstructPathOfLights(currentNode)
            return path.size - 1L
        }
        machineManual.buttonWiringSchematics.forEach { buttonWiring ->
            val newStateOfLights = currentNode.stateOfLights.toMutableList()
            buttonWiring.forEach { lightIndex -> newStateOfLights[lightIndex] = !newStateOfLights[lightIndex] }
            val newNode = NodeOfLights(newStateOfLights, currentNode, currentNode.numberOfVisitedNodsSoFar + 1)
            nodesToVisit.add(newNode)
        }
    }
    return 0L
}

private fun heuristicForLights(currentState: List<Boolean>, goalState: List<Boolean>): Long {
    return currentState.zip(goalState).count { (currentLight, goalLight) -> currentLight != goalLight }.toLong()
}

private fun reconstructPathOfLights(nodeOfLights: NodeOfLights): List<NodeOfLights> {
    val path = mutableListOf<NodeOfLights>()
    var currentNode: NodeOfLights? = nodeOfLights
    while (currentNode != null) {
        path.add(currentNode)
        currentNode = currentNode.parent
    }
    return path.reversed()
}

private data class NodeOfJoltages(val joltages: IntArray, val steps: Int, val estimate: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NodeOfJoltages

        if (steps != other.steps) return false
        if (estimate != other.estimate) return false
        if (!joltages.contentEquals(other.joltages)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = steps
        result = 31 * result + estimate
        result = 31 * result + joltages.contentHashCode()
        return result
    }
}

private class PartOfJoltageNode(val arr: IntArray) {
    override fun equals(other: Any?) = other is PartOfJoltageNode && arr.contentEquals(other.arr)

    override fun hashCode() = arr.contentHashCode()
}

private fun getMinimumStepsToConfigureJoltageAStar(machineManual: MachineManual): Long {
    val initialState = IntArray(machineManual.joltageRequirements.size) { 0 }
    val goalState = machineManual.joltageRequirements.toIntArray()
    val nodesToVisit = PriorityQueue<NodeOfJoltages>(compareBy { it.steps + it.estimate })
    val alreadyVisitedNodes = mutableMapOf<PartOfJoltageNode, Int>()
    nodesToVisit.add(NodeOfJoltages(initialState, 0, heuristicForJoltages(initialState, goalState)))
    while (nodesToVisit.isNotEmpty()) {
        val (currentNode, steps, _) = nodesToVisit.poll()
        if (currentNode.contentEquals(goalState)) return steps.toLong()
        val alreadyVisitedKey = PartOfJoltageNode(currentNode)
        val previousSteps = alreadyVisitedNodes[alreadyVisitedKey]
        if (previousSteps != null && previousSteps <= steps) continue
        alreadyVisitedNodes[alreadyVisitedKey] = steps
        for (button in machineManual.buttonWiringSchematics) {
            if (button.all { currentNode[it] >= goalState[it] }) continue
            val next = currentNode.copyOf()
            for (idx in button) next[idx]++
            if (anyStateTooHigh(next, goalState)) continue
            nodesToVisit.add(NodeOfJoltages(next, steps + 1, heuristicForJoltages(next, goalState)))
        }
    }
    return 0L
}

private fun anyStateTooHigh(currentState: IntArray, goalState: IntArray): Boolean =
    currentState.zip(goalState).any { (a, b) -> a > b }

private class JoltageState(val arr: IntArray) {
    override fun equals(other: Any?): Boolean = other is JoltageState && arr.contentEquals(other.arr)

    override fun hashCode(): Int = arr.contentHashCode()
}

private fun heuristicForJoltages(currentState: IntArray, goalState: IntArray): Int =
    currentState.zip(goalState).sumOf { (currentJoltage, goalJoltage) -> (goalJoltage - currentJoltage) }

private data class JoltageSystem(val equations: List<JoltageEquation>, val machineManualIndex: Int)

private data class JoltageEquation(
    val result: Int,
    val operands: List<Int> // button index
)

private fun solveJoltageSystem(system: JoltageSystem): JoltageRequirementSolution {
    val numberOfEquations = system.equations.size
    val variables = system.equations.flatMap { it.operands }.toSet().sorted()
    val numbersOfVariables = variables.size
    val variableIndex = variables.withIndex().associate { it.value to it.index }

    val matrix =
        Array(numberOfEquations) { rowIndex ->
            val row = DoubleArray(numbersOfVariables + 1) { 0.0 }
            val currentEquation = system.equations[rowIndex]
            for (operand in currentEquation.operands) {
                row[variableIndex[operand]!!] = 1.0
            }
            row[numbersOfVariables] = currentEquation.result.toDouble()
            row
        }

    var pivotColumn = 0
    for (i in 0 until numberOfEquations) {
        if (pivotColumn >= numbersOfVariables) break
        var pivotRow = i
        while (pivotRow < numberOfEquations && matrix[pivotRow][pivotColumn] == 0.0) pivotRow++
        if (pivotRow == numberOfEquations) {
            pivotColumn++
            if (pivotColumn >= numbersOfVariables) break
            pivotRow = i
            while (pivotRow < numberOfEquations && matrix[pivotRow][pivotColumn] == 0.0) pivotRow++
            if (pivotRow == numberOfEquations) continue
        }

        val tmp = matrix[i]
        matrix[i] = matrix[pivotRow]
        matrix[pivotRow] = tmp

        val pivotValue = matrix[i][pivotColumn]
        for (j in 0..numbersOfVariables) matrix[i][j] /= pivotValue
        for (k in i + 1 until numberOfEquations) {
            val belowPivotValue = matrix[k][pivotColumn]
            for (j in 0..numbersOfVariables) {
                matrix[k][j] -= belowPivotValue * matrix[i][j]
            }
        }
        pivotColumn++
    }

    val solution = DoubleArray(numbersOfVariables) { Double.NaN }
    val isPivot = BooleanArray(numbersOfVariables) { false }
    val freeVariableAndCoefficientByVariable = mutableMapOf<Int, Map<Int, Double>>()
    val freeVariablesIndices = variables.withIndex().map { it.index }
    val coefficientOfFreeVariablesForVariablesMatrix =
        Array(numbersOfVariables) { DoubleArray(freeVariablesIndices.size) { 0.0 } }
    for (i in 0 until numbersOfVariables) {
        if (freeVariablesIndices.contains(i)) {
            // Free variable: its own coefficient is 1
            val indexOfFreeVariable = freeVariablesIndices.indexOf(i)
            coefficientOfFreeVariablesForVariablesMatrix[i][indexOfFreeVariable] = 1.0
        }
    }
    for (i in numberOfEquations - 1 downTo 0) {
        val row = matrix[i]
        val firstNonNullElement = row.indexOfFirst { it != 0.0 && it != row.last() }
        if (firstNonNullElement == -1) {
            continue
        }
        isPivot[firstNonNullElement] = true
        var sum = row.last()
        for (j in firstNonNullElement + 1 until numbersOfVariables) {
            sum -= row[j] * (if (solution[j].isNaN()) 0.0 else solution[j])
        }
        solution[firstNonNullElement] = sum / row[firstNonNullElement]
        for (freeVariable in freeVariablesIndices.indices) {
            var coefficient = 0.0
            for (j in firstNonNullElement + 1 until numbersOfVariables) {
                coefficient -= row[j] * coefficientOfFreeVariablesForVariablesMatrix[j][freeVariable]
            }
            coefficientOfFreeVariablesForVariablesMatrix[firstNonNullElement][freeVariable] =
                coefficient / row[firstNonNullElement]
        }
        val coefficientOfFreeVariablesForVariable = mutableMapOf<Int, Double>()
        for (freeVariable in freeVariablesIndices.indices) {
            if (coefficientOfFreeVariablesForVariablesMatrix[firstNonNullElement][freeVariable] != 0.0) {
                coefficientOfFreeVariablesForVariable[freeVariablesIndices[freeVariable]] =
                    coefficientOfFreeVariablesForVariablesMatrix[firstNonNullElement][freeVariable]
            }
        }
        if (coefficientOfFreeVariablesForVariable.isNotEmpty())
            freeVariableAndCoefficientByVariable[firstNonNullElement] = coefficientOfFreeVariablesForVariable
    }

    val freeVariables = variables.filterIndexed { idx, _ -> !isPivot[idx] }
    return if (freeVariables.isEmpty()) {
        JoltageRequirementSolution.UniqueJoltageRequirementSolution(
            variables.associateWith { solution[variableIndex[it]!!] })
    } else {
        JoltageRequirementSolution.ReducedSystem(
            variables,
            solution,
            freeVariables,
            freeVariableAndCoefficientByVariable,
            system.machineManualIndex)
    }
}

sealed class JoltageRequirementSolution {
    data class UniqueJoltageRequirementSolution(val values: Map<Int, Double>) : JoltageRequirementSolution()

    data class ReducedSystem(
        val variables: List<Int>,
        val solution: DoubleArray, // NaN for free variables
        val freeVariables: List<Int>,
        val dependencyMap: Map<Int, Map<Int, Double>>, // dependentVarIdx -> (freeVarIdx -> coeff)
        val machineManualIndex: Int,
    ) : JoltageRequirementSolution()
}

private fun getMinimumStepsToConfigureJoltageWithReducedSystemAStar(
    joltageReducedSystem: JoltageRequirementSolution.ReducedSystem,
    machineManual: MachineManual
): Long {
    val initialState = mutableListOf<Int>()
    var initialSteps = 0
    machineManual.joltageRequirements.forEach { _ -> initialState.add(0) }
    joltageReducedSystem.solution.forEachIndexed { indexOfButton, solution ->
        if (solution.isNaN()) return@forEachIndexed
        val button = machineManual.buttonWiringSchematics[indexOfButton]
        initialSteps += solution.toInt()
        for (index in button) {
            initialState[index] += solution.toInt()
        }
    }
    val initialStateIntArray = initialState.toIntArray()
    val goalState = machineManual.joltageRequirements.toIntArray()
    val possibleButtonsToVisit =
        machineManual.buttonWiringSchematics
            .mapIndexed { index, button -> if (index in joltageReducedSystem.freeVariables) index to button else null }
            .filterNotNull()
    val nodesToVisit = PriorityQueue<NodeOfJoltages>(compareBy { it.steps + it.estimate })
    val alreadyVisitedNodes = mutableMapOf<PartOfJoltageNode, Int>()
    nodesToVisit.add(
        NodeOfJoltages(initialStateIntArray, initialSteps, heuristicForJoltages(initialStateIntArray, goalState)))
    while (nodesToVisit.isNotEmpty()) {
        val (currentNode, steps, _) = nodesToVisit.poll()
        if (currentNode.contentEquals(goalState) && currentNode.all { it >= 0 }) return steps.toLong()
        val alreadyVisitedKey = PartOfJoltageNode(currentNode)
        val previousSteps = alreadyVisitedNodes[alreadyVisitedKey]
        if (previousSteps != null && previousSteps <= steps) continue
        alreadyVisitedNodes[alreadyVisitedKey] = steps
        var currentNumberOfSteps = steps + 1
        for (button in possibleButtonsToVisit) {
            val next = currentNode.copyOf()
            button.second.forEach { wiring ->
                val dependentButtons =
                    joltageReducedSystem.dependencyMap.filter { (_, coeffByFreeVariable) ->
                        coeffByFreeVariable.containsKey(button.first)
                    }
                dependentButtons.forEach { (buttonDependent, coeffByFreeVariable) ->
                    val coeff = coeffByFreeVariable[button.first]!!
                    val intCoeff = coeff.roundToLong().toInt()
                    currentNumberOfSteps += intCoeff
                    val buttonInMachineManual = machineManual.buttonWiringSchematics[buttonDependent]
                    buttonInMachineManual.forEach { wiringOfDependentButton ->
                        next[wiringOfDependentButton] += intCoeff
                    }
                }
                next[wiring]++
            }
            if (anyStateTooHigh(next, goalState)) continue
            nodesToVisit.add(NodeOfJoltages(next, currentNumberOfSteps, heuristicForJoltages(next, goalState)))
        }
    }
    return 0L
}
