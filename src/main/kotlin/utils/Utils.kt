package maelise.castel.utils

fun readLines(filePath: String) = readText(filePath).lines()

fun readText(filePath: String) = {}.javaClass.getResource(filePath)!!.readText()

fun readColumns(filePath: String): List<String> {
    val lines = readLines(filePath)
    val numberOfColumns = lines.maxOf { it.length }
    return (0 until numberOfColumns).map { index ->
        lines.mapNotNull { line -> line.getOrNull(index) }.joinToString("")
    }
}

data class Range(val start: Long, val end: Long)

data class Matrix<T>(val grid: List<List<T>>)
