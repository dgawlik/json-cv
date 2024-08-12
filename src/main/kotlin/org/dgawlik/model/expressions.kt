package org.dgawlik.org.dgawlik.model

import org.dgawlik.model.*


val arithmeticOperators = listOf("+", "-", "*", "/")

val comparisonOperators = listOf("==", "!=",  "<=", ">=", "<", ">",)

val logicalOperators = listOf("&&", "||")

val stringOperators = listOf("|")

val identifierChars = ('a'..'z')
    .union('A'..'Z')
    .union('0'..'9')
    .union(setOf('_', '.'))


open class Expression

open class LiteralExpression : Expression()

open class BinaryExpression(open val left: Expression, open val operator: String, open val right: Expression) :
    Expression()

data class LongLiteralExpression(val value: Long) : LiteralExpression()

data class DoubleLiteralExpression(val value: Double) : LiteralExpression()

data class StringLiteralExpression(val value: String) : LiteralExpression()

data class BooleanLiteralExpression(val value: Boolean) : LiteralExpression()

data class IdentifierExpression(val segments: List<String>) : LiteralExpression()

fun Expression.eval(valueRoot: Value? = null): Any? {
    return when (this) {
        is LongLiteralExpression -> value
        is DoubleLiteralExpression -> value
        is BooleanLiteralExpression -> value
        is StringLiteralExpression -> value
        is IdentifierExpression -> {
            var itRoot = valueRoot
            while (itRoot != null) {

                if (itRoot.query(segments) != null) {
                    break
                }

                itRoot = itRoot.parent
            }

            if (itRoot == null) {
                throw IllegalArgumentException("Identifier not found")
            } else {
                val result = itRoot.query(segments)
                when (result) {
                    is LongValue -> result.toLong()
                    is DoubleValue -> result.toDouble()
                    is BooleanValue -> result.toBoolean()
                    is StringValue -> result.text
                    is ExpressionValue -> result.expr?.eval(itRoot)
                    else -> throw IllegalArgumentException("Invalid identifier type")
                }
            }
        }

        is ArithmeticExpression -> {
            val left = left.eval(valueRoot)
            val right = right.eval(valueRoot)
            when (operator) {
                "+" -> when (left) {
                    is Long -> left + (right as Number).toLong()
                    is Double -> left + (right as Number).toDouble()
                    else -> throw IllegalArgumentException("Invalid operator $operator")
                }

                "-" -> when (left) {
                    is Long -> left - (right as Number).toLong()
                    is Double -> left - (right as Number).toDouble()
                    else -> throw IllegalArgumentException("Invalid operator $operator")
                }

                "*" -> when (left) {
                    is Long -> left * (right as Number).toLong()
                    is Double -> left * (right as Number).toDouble()
                    else -> throw IllegalArgumentException("Invalid operator $operator")
                }

                "/" -> when (left) {
                    is Long -> left / (right as Number).toLong()
                    is Double -> left / (right as Number).toDouble()
                    else -> throw IllegalArgumentException("Invalid operator $operator")
                }

                else -> throw IllegalArgumentException("Invalid operator $operator")
            }
        }

        is BooleanExpression -> {
            val left = left.eval(valueRoot)
            val right = right.eval(valueRoot)


            when (operator) {
                "&&" -> when (left) {
                    is Boolean -> left && (right as Boolean)
                    else -> throw IllegalArgumentException("Invalid operator $operator")
                }

                "||" -> when (left) {
                    is Boolean -> left || (right as Boolean)
                    else -> throw IllegalArgumentException("Invalid operator $operator")
                }

                "==" -> left == right
                "!=" -> left != right

                "<" -> when (left) {
                    is Long -> left < right as Long
                    is Double -> left < right as Double
                    else -> throw IllegalArgumentException("Invalid operator $operator")
                }

                ">" -> when (left) {
                    is Long -> left > right as Long
                    is Double -> left > right as Double
                    else -> throw IllegalArgumentException("Invalid operator $operator")
                }

                "<=" -> when (left) {
                    is Long -> left <= right as Long
                    is Double -> left <= right as Double
                    else -> throw IllegalArgumentException("Invalid operator $operator")
                }

                ">=" -> when (left) {
                    is Long -> left >= right as Long
                    is Double -> left >= right as Double
                    else -> throw IllegalArgumentException("Invalid operator $operator")
                }

                else -> throw IllegalArgumentException("Invalid operator $operator")
            }
        }

        is StringExpression -> {
            val left = left.eval(valueRoot)
            val right = right.eval(valueRoot)
            when (operator) {
                "|" -> left.toString() + right.toString()
                else -> throw IllegalArgumentException("Invalid operator $operator")
            }
        }

        else -> throw IllegalArgumentException("Unknown expression type")
    }
}


data class ArithmeticExpression(
    override val left: Expression,
    override val operator: String,
    override val right: Expression
) : BinaryExpression(left, operator, right)

data class BooleanExpression(
    override val left: Expression,
    override val operator: String,
    override val right: Expression
) : BinaryExpression(left, operator, right)

data class StringExpression(
    override val left: Expression,
    override val operator: String,
    override val right: Expression
) : BinaryExpression(left, operator, right)


fun validateExpressionTokens(tokens: List<String>, startPos: Int, endPos: Int): Boolean {

    var i = startPos
    while (i <= endPos) {
        if (tokens[i] == "(") {
            val nestedStart = i + 1
            val nestedEnd = findMatchingBrace(tokens, nestedStart)

            if (nestedEnd == -1 || !validateExpressionTokens(tokens, nestedStart, nestedEnd)) {
                return false
            }

            i = nestedEnd + 1
        } else if (!tokens[i].all { it in identifierChars }
            && !(tokens[i][0] == '"' && tokens[i][tokens[i].length - 1] == '"')) {
            return false
        } else {
            i++
        }

        if (i > endPos || tokens[i] == ")") {
            break
        }

        if (tokens[i] !in (arithmeticOperators + comparisonOperators + logicalOperators + stringOperators)) {
            return false
        }

        i++
    }

    return true
}

fun findMatchingBrace(tokens: List<String>, startPos: Int): Int {
    var depth = 1
    var i = startPos
    while (i < tokens.size) {
        if (tokens[i] == "(") {
            depth++
        } else if (tokens[i] == ")") {
            depth--
            if (depth == 0) {
                return i
            }
        }
        i++
    }
    return -1
}

fun splitOnOperators(expression: String): List<String>? {
    val result = mutableListOf<String>()
    var current = ""
    var i = 0
    while (i < expression.length) {
        if (expression[i] == '"') {
            val end = expression.indexOf('"', i + 1)
            if (end == -1) {
                return null
            }
            current += expression.substring(i, end + 1)
            i = end + 1
        } else if (expression[i].isWhitespace()) {
            i++
        } else if (expression[i] in setOf('(', ')')) {
            if (current.isNotEmpty()) {
                result.add(current)
                current = ""
            }
            result.add(expression[i].toString())
            i++
        } else {
            val operator = arithmeticOperators
                .union(comparisonOperators)
                .union(logicalOperators)
                .union(stringOperators)
                .firstOrNull { expression.startsWith(it, i) }
            if (operator != null) {
                if (current.isNotEmpty()) {
                    result.add(current)
                }
                result.add(operator)
                current = ""
                i += operator.length
            } else {
                current += expression[i]
                i++
            }
        }
    }
    if (current.isNotEmpty()) {
        result.add(current)
    }
    return result
}

fun tokenizeExpression(expression: String): List<String>? {
    val tokens = splitOnOperators(expression) ?: return null
    val success = validateExpressionTokens(tokens, 0, tokens.size - 1)
    return if (success) {
        tokens
    } else {
        null
    }
}

fun buildExpressionTree(
    tokens: List<String>,
    startPos: Int = 0,
    endPos: Int = tokens.size - 1
)
        : Expression? {


    if (tokens.isEmpty()) {
        return null
    }

    if (tokens.size == 1) {
        return createLiteralExpression(tokens, 0)
    }

    var currentRoot: Expression? = null
    var rhs: Expression? = null


    var i = startPos
    var opInd = 0
    var nextI = 0
    var nextOpInd = 0

    if (tokens[i] == "(") {
        val nestedEnd = findMatchingBrace(tokens, i + 1)
        currentRoot = buildExpressionTree(tokens, i + 1, nestedEnd - 1)
        opInd = nestedEnd + 1
        i = nestedEnd + 2
    } else {
        currentRoot = createLiteralExpression(tokens, i)
        opInd = i + 1
        i += 2
    }

    while (i <= endPos) {
        if (tokens[i] == "(") {
            val nestedEnd = findMatchingBrace(tokens, i + 1)
            val nestedRoot = buildExpressionTree(tokens, i + 1, nestedEnd - 1)
            rhs = nestedRoot
            nextOpInd = nestedEnd + 1
            nextI = nestedEnd + 2

        } else {
            rhs = createLiteralExpression(tokens, i)
            nextOpInd = i + 1
            nextI = i + 2
        }

        if (rhs != null) {
            if (tokens[opInd] in arithmeticOperators) {
                currentRoot = ArithmeticExpression(currentRoot!!, tokens[opInd], rhs)
                rhs = null
            } else if (tokens[opInd] in comparisonOperators.union(logicalOperators)) {
                currentRoot = BooleanExpression(currentRoot!!, tokens[opInd], rhs)
                rhs = null
            } else if (tokens[opInd] in stringOperators) {
                currentRoot = StringExpression(currentRoot!!, tokens[opInd], rhs)
                rhs = null
            } else {
                throw IllegalArgumentException("Invalid operator ${tokens[opInd]}")
            }
        }
        opInd = nextOpInd
        i = nextI
    }


    return currentRoot

}

private fun createLiteralExpression(tokens: List<String>, pos: Int): Expression? {
    return when {
        tokens[pos].toLongOrNull() != null -> LongLiteralExpression(tokens[pos].toLong())
        tokens[pos].toDoubleOrNull() != null -> DoubleLiteralExpression(tokens[pos].toDouble())
        tokens[pos] == "true" -> BooleanLiteralExpression(true)
        tokens[pos] == "false" -> BooleanLiteralExpression(false)
        tokens[pos].startsWith("\"") && tokens[pos].endsWith("\"") -> StringLiteralExpression(
            tokens[pos].substring(
                1,
                tokens[pos].length - 1
            )
        )

        tokens[pos][0].isLetterOrDigit() -> IdentifierExpression(tokens[pos].split('.'))

        else -> null
    }
}