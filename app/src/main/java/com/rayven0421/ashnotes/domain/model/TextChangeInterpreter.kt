package com.rayven0421.ashnotes.domain.model

sealed interface TextChange {
    data class Plain(val newText: String) : TextChange
    data class Enter(val cursor: Int, val textBefore: String) : TextChange
    data class Paste(val cursor: Int, val inserted: String, val textBefore: String) : TextChange
}

object TextChangeInterpreter {
    fun interpret(oldText: String, newText: String): TextChange {
        if (oldText == newText) {
            return TextChange.Plain(newText)
        }

        var prefixLen = 0
        val maxPrefix = minOf(oldText.length, newText.length)
        while (prefixLen < maxPrefix && oldText[prefixLen] == newText[prefixLen]) {
            prefixLen++
        }

        var suffixLen = 0
        val maxSuffixOld = oldText.length - prefixLen
        val maxSuffixNew = newText.length - prefixLen
        while (suffixLen < maxSuffixOld && suffixLen < maxSuffixNew &&
            oldText[oldText.length - 1 - suffixLen] == newText[newText.length - 1 - suffixLen]
        ) {
            suffixLen++
        }

        val inserted = newText.substring(prefixLen, newText.length - suffixLen)
        val cursor = prefixLen
        val textBefore = oldText.substring(0, prefixLen) + oldText.substring(oldText.length - suffixLen)

        return when {
            inserted == "\n" -> TextChange.Enter(cursor, textBefore)
            inserted.contains("\n") -> TextChange.Paste(cursor, inserted, textBefore)
            else -> TextChange.Plain(newText)
        }
    }
}
