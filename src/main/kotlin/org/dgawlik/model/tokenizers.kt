package org.dgawlik.org.dgawlik.model


open class TokenString(open val text: String, open val range: IntRange)

open class  PrimitiveToken(override val text: String, override val range: IntRange) : TokenString("", range)

data class NullToken(override val range: IntRange) : PrimitiveToken("null", range)

data class BooleanToken(override val range: IntRange, val s: String) : PrimitiveToken(s, range)

data class NumberToken(override val range: IntRange, val s: String, val isDouble: Boolean) : PrimitiveToken(s, range)

data class StringToken(override val range: IntRange, val s: String) : PrimitiveToken(s, range)

data class ExpressionToken(override val range: IntRange, val s: String) : TokenString(s, range)

data class OpenWhiskerToken(override val range: IntRange) : TokenString("{", range)

data class CloseWhiskerToken(override val range: IntRange) : TokenString("}", range)

data class OpenBracketToken(override val range: IntRange) : TokenString("[", range)

data class CloseBracketToken(override val range: IntRange) : TokenString("]", range)

data class CommaToken(override val range: IntRange) : TokenString(",", range)

data class ColonToken(override val range: IntRange) : TokenString(":", range)



fun CharArray.takeNull(startPos: Int): TokenString? {

    if (startPos + 3 >= this.size) {
        return null
    }

    val extracted = this.sliceArray(startPos until startPos + 4).joinToString("")

    return if (extracted == "null" &&
        (startPos + 4 == this.size || this[startPos + 4].isWhitespace()
                || this[startPos + 4] in setOf(',', '}', ']'))
    ) {
        NullToken(startPos until startPos + 4)
    } else {
        null
    }
}

fun CharArray.takeBoolean(startPos: Int): TokenString? {

    var pos = startPos
    while (pos < this.size && this[pos].isLetter() && !this[pos].isWhitespace() &&
        this[pos] !in setOf(',', '}', ']')) {
        pos++
    }

    val extracted = this.sliceArray(startPos until pos).joinToString("")

    if (extracted == "true") {
        return BooleanToken(startPos until pos, "true")
    } else if (extracted == "false") {
        return BooleanToken(startPos until pos, "false")
    } else {
        return null
    }
}

fun CharArray.takeNumber(startPos: Int): TokenString? {
    var pos = startPos
    var hasDot = false
    var hasExp = false

    if (pos < this.size && this[pos] == '-') {
        pos++
    }

    if (pos == this.size) {
        return null
    }

    if (this[pos] == '0') {
        pos++
    }

    if (pos < this.size && this[pos] == '0') {
        return null
    }

    while (pos < this.size) {
        if (this[pos].isDigit()) {
            pos++
        } else if (this[pos].isWhitespace() || this[pos] in setOf('e', 'E', '.', ',', ']', '}')) {
            break
        } else {
            return null
        }
    }


    if (pos < this.size && this[pos] == '.') {
        hasDot = true
        pos++
        while (pos < this.size) {
            if (this[pos].isDigit()) {
                pos++
            } else if (this[pos].isWhitespace() || this[pos] in setOf('e', 'E', ',', ']', '}')) {
                break
            } else {
                return null
            }
        }
    }

    if (pos < this.size && (this[pos] == 'e' || this[pos] == 'E')) {
        hasExp = true
        pos++
        if (pos < this.size && (this[pos] == '+' || this[pos] == '-')) {
            pos++
        }
        while (pos < this.size) {
            if (this[pos].isDigit()) {
                pos++
            } else if (this[pos].isWhitespace() || this[pos] in setOf(',', ']', '}')) {
                break
            } else {
                return null
            }
        }
    }
    return if (pos > startPos) {
        val extracted = this.sliceArray(startPos until pos).joinToString("")
        NumberToken(startPos until pos, extracted, hasDot || hasExp)
    } else {
        null
    }
}

fun CharArray.takeString(startPos: Int): TokenString? {

    if (this[startPos] != '"') {
        return null
    }

    var endPos = startPos + 1
    while (endPos < this.size && isUnescapedDoubleQuote(this, endPos).not()) {
        endPos++
    }

    if (endPos == this.size) {
        return null
    }

    return StringToken(startPos..endPos, this.sliceArray(startPos..endPos).joinToString(""))
}

fun isUnescapedDoubleQuote(text: CharArray, pos: Int): Boolean {
    if (pos >= text.size) {
        return false
    }

    return if (pos - 2 >= 0 &&
        (text[pos - 2] == '\\' && text[pos - 1] == '\\' && text[pos] == '"')
    ) {

        true
    } else if (pos - 1 >= 0 && text[pos - 1] == '\\' && text[pos] == '"') {
        false
    } else {
        text[pos] == '"'
    }
}

fun CharArray.takeExpression(startPos: Int): TokenString? {

    var pos = startPos

    if (pos >= this.size || this[pos++] != '$') {
        return null
    }

    if (pos >= this.size || this[pos++] != '{') {
        return null
    }

    while (pos < this.size && this[pos] != '}') {
        pos++
    }

    if (pos >= this.size || this[pos] != '}'){
        return null
    }

    return ExpressionToken(startPos+2 until pos, this.sliceArray(startPos+2 until pos).joinToString(""))
}


fun CharArray.skipWhitespaces(startPos: Int): Int {
    var pos = startPos
    while (pos < this.size && this[pos].isWhitespace()) {
        pos++
    }
    return pos
}


fun tokenize(input: CharArray): List<TokenString> {
    val tokens = mutableListOf<TokenString>()
    var pos = 0

    while (pos < input.size) {
        when {
            input[pos].isWhitespace() -> {
                pos = input.skipWhitespaces(pos)
            }

            input[pos] == 'n' -> {
                val token = input.takeNull(pos)
                if (token != null) {
                    tokens.add(token)
                    pos = token.range.last + 1
                } else {
                    throw IllegalArgumentException("Invalid token at position $pos")
                }
            }

            input[pos] == 't' || input[pos] == 'f' -> {
                val token = input.takeBoolean(pos)
                if (token != null) {
                    tokens.add(token)
                    pos = token.range.last + 1
                } else {
                    throw IllegalArgumentException("Invalid token at position $pos")
                }
            }

            input[pos] == '$' -> {
                val token = input.takeExpression(pos)
                if (token != null) {
                    tokens.add(token)
                    pos = token.range.last + 2
                } else {
                    throw IllegalArgumentException("Invalid token at position $pos")
                }
            }

            input[pos].isDigit() || input[pos] == '-' -> {
                val token = input.takeNumber(pos)
                if (token != null) {
                    tokens.add(token)
                    pos = token.range.last + 1
                } else {
                    throw IllegalArgumentException("Invalid token at position $pos")
                }
            }

            input[pos] == '"' -> {
                val token = input.takeString(pos)
                if (token != null) {
                    tokens.add(token)
                    pos = token.range.last + 1
                } else {
                    throw IllegalArgumentException("Invalid token at position $pos")
                }
            }

            input[pos] == '{' -> {
                tokens.add(OpenWhiskerToken(pos..pos))
                pos++
            }

            input[pos] == '}' -> {
                tokens.add(CloseWhiskerToken(pos..pos))
                pos++
            }

            input[pos] == ':' -> {
                tokens.add(ColonToken(pos..pos))
                pos++
            }

            input[pos] == '[' -> {
                tokens.add(OpenBracketToken(pos..pos))
                pos++
            }

            input[pos] == ']' -> {
                tokens.add(CloseBracketToken(pos..pos))
                pos++
            }

            input[pos] == ',' -> {
                tokens.add(CommaToken(pos..pos))
                pos++
            }

            else -> {
                throw IllegalArgumentException("Invalid token at position $pos")
            }
        }
    }

    return tokens
}


