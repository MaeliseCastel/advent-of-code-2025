package maelise.castel

import maelise.castel.utils.readLines
import java.util.*

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

    val sortedMachineManuals = machineManuals.sortedBy { it.buttonWiringSchematics.size }
    //    println(machineManuals)
    //    val minimumStepsToConfigureLights =
    //        machineManuals.sumOf { machineManual -> val minimumStepsToConfigureLights =
    //            getMinimumStepsToConfigureLights(machineManual)
    //            minimumStepsToConfigureLights
    //        }
    //    println("Sum of minimum steps to configure lights: $minimumStepsToConfigureLights")

    var count = 0
    val size = sortedMachineManuals.size
    val startTime = System.currentTimeMillis()
    val minimumStepsToConfigureJoltages =
        sortedMachineManuals.sumOf { machineManual ->
            val minimumStepsToConfigureJoltage = getMinimumStepsToConfigureJoltageAStar(machineManual)
            val endTime = System.currentTimeMillis()
            count++
            println(
                "Did $count/$size in ${endTime - startTime} ms - machine manual size: ${machineManual.buttonWiringSchematics.size}")
            minimumStepsToConfigureJoltage
        }
    println("Sum of minimum steps to configure joltages: $minimumStepsToConfigureJoltages")
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

private data class NodeOfJoltages(val joltages: IntArray, val steps: Int, val estimate: Int)

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

private fun anyStateTooHigh(currentState: IntArray, goalState: IntArray): Boolean = currentState.zip(goalState).any { (a, b) -> a > b }

private class JoltageState(val arr: IntArray) {
    override fun equals(other: Any?): Boolean = other is JoltageState && arr.contentEquals(other.arr)

    override fun hashCode(): Int = arr.contentHashCode()
}

private fun heuristicForJoltages(currentState: IntArray, goalState: IntArray): Int =
    currentState.zip(goalState).sumOf { (currentJoltage, goalJoltage) -> (goalJoltage - currentJoltage) }
