package com.onebitsailor.codestoclientd.logic.format

object CodeFormatter {

    fun format(source: String, indentSize: Int = 4): String {
        val lines = source.replace("\r\n", "\n").split("\n")
        val indentUnit = " ".repeat(indentSize)
        var depth = 0
        val result = StringBuilder()

        for (rawLine in lines) {
            val line = rawLine.trim().replace("\t", indentUnit)
            if (line.isEmpty()) {
                result.append("\n")
                continue
            }

            val leadingCloses = line.takeWhile { it == '}' || it == ')' || it == ']' }.length
            val effectiveDepth = (depth - leadingCloses).coerceAtLeast(0)

            result.append(indentUnit.repeat(effectiveDepth))
            result.append(line)
            result.append("\n")

            val opens = line.count { it == '{' || it == '(' || it == '[' }
            val closes = line.count { it == '}' || it == ')' || it == ']' }
            depth = (depth + opens - closes).coerceAtLeast(0)
        }

        return result.toString().trimEnd('\n')
    }
}