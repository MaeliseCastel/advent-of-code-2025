package maelise.castel.utils

fun readLines(filePath: String) = readText(filePath).lines()

fun readText(filePath: String) = {}.javaClass.getResource(filePath)!!.readText()
