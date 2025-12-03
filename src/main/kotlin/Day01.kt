package maelise.castel

import maelise.castel.utils.readLines
import kotlin.math.abs

private const val STARTING_INDEX = 50

fun main() {
    val rotations = readLines("/day01.txt").map { line -> parseRotation(line) }

  val nbOfStoppedAtZero = countStoppedAtZero(rotations)
  println(nbOfStoppedAtZero)

  val zerosEncountered = countZerosEncountered(rotations)
  println(zerosEncountered)
}

private fun countStoppedAtZero(rotations: List<Rotation>): Int {
  var currentIndex = STARTING_INDEX
  var nbOfStoppedAtZero = 0
  rotations.forEach { rotation ->
    currentIndex = computeNextIndex(rotation, currentIndex)
    if (currentIndex == 0) {
      nbOfStoppedAtZero++
    }
  }
  return nbOfStoppedAtZero
}

fun countZerosEncountered(rotations: List<Rotation>): Int {
  var currentIndex = STARTING_INDEX
  var zerosEncountered = 0
  rotations.forEach { rotation ->
    zerosEncountered +=
        when (rotation.direction) {
          Direction.LEFT -> {
            val difference = currentIndex - rotation.clicks
              val zerosEncounteredWhenCurrentIndexAtZero = abs(difference / 100)
              if (currentIndex != 0 && difference / 100.0 <= 0.0) zerosEncounteredWhenCurrentIndexAtZero + 1
            else zerosEncounteredWhenCurrentIndexAtZero
          }
          Direction.RIGHT -> (currentIndex + rotation.clicks) / 100
        }
    currentIndex = computeNextIndex(rotation, currentIndex)
  }

  return zerosEncountered
}

private fun computeNextIndex(rotation: Rotation, currentIndex: Int): Int =
    when (rotation.direction) {
      Direction.LEFT -> (currentIndex - rotation.clicks).mod(100)
      Direction.RIGHT -> (currentIndex + rotation.clicks) % 100
    }

class Rotation(val direction: Direction, val clicks: Int)

enum class Direction {
  LEFT,
  RIGHT,
}

private fun parseRotation(rotationString: String): Rotation {
  val direction =
      when (rotationString[0]) {
        'L' -> Direction.LEFT
        else -> Direction.RIGHT
      }
  val clicks = rotationString.substring(1).toInt()
  return Rotation(direction, clicks)
}
