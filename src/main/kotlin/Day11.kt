package maelise.castel

import maelise.castel.utils.readLines

fun main() {
    val devices = readLines("/day11.txt").map { line ->
        val (label, outputsAsString) = line.split(":")
        val outputs = outputsAsString.split(" ").mapNotNull { if(it.isNotEmpty() || it.isNotBlank()) it else null }
        Device(label, outputs)
    }
//    val numberOfPaths = getNumberOfPaths(devices)
//    println("Number of paths from 'you' to 'out': $numberOfPaths")

    val numberOfPaths = getNumberOfValidPaths(devices)
    println("All paths from 'svr' to 'out': $numberOfPaths")
}

private data class Device(val label: String, val outputs: List<String>)

private fun getNumberOfPaths(devices: List<Device>): Long {
    val startingDevice = devices.first {it.label == "you"}
    val devicesToVisit = ArrayDeque<Device>()
    devicesToVisit.add(startingDevice)
    val alreadyVisitedDevices = mutableSetOf<Device>()
    var pathCounter = 0L
    while (devicesToVisit.isNotEmpty()) {
        val currentDevice = devicesToVisit.removeFirst()
        if (currentDevice.outputs.contains("out")) {
            pathCounter++
            continue
        }
        val nextDevicesToVisit = devices.filter { device ->
            currentDevice.outputs.contains(device.label) && device !in alreadyVisitedDevices
        }
        alreadyVisitedDevices.add(currentDevice)
        devicesToVisit.addAll(nextDevicesToVisit)
    }
    return pathCounter
}

private data class PathState(
    val label: String,
    val hasDac: Boolean,
    val hasFft: Boolean
)

private fun getNumberOfValidPaths(
    devices: List<Device>,
    currentDeviceLabel: String = "svr",
    hasDac: Boolean = false,
    hasFft: Boolean = false,
    visitedStates: MutableMap<PathState, Long> = mutableMapOf()
): Long {
    val currentState = PathState(currentDeviceLabel, hasDac, hasFft)
    visitedStates[currentState]?.let { return it }

    if (currentDeviceLabel == "out") {
        return if (hasDac && hasFft) 1L else 0L
    }

    val currentDevice = devices.firstOrNull { it.label == currentDeviceLabel } ?: return 0L
    var numberOfValidPaths = 0L
    for (outputLabel in currentDevice.outputs) {
        if (outputLabel != currentDeviceLabel) {
            numberOfValidPaths += getNumberOfValidPaths(
                devices,
                outputLabel,
                hasDac || outputLabel == "dac",
                hasFft || outputLabel == "fft",
                visitedStates
            )
        }
    }
    visitedStates[currentState] = numberOfValidPaths
    return numberOfValidPaths
}
