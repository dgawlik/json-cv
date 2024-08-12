import org.dgawlik.model.*
import org.dgawlik.org.dgawlik.model.*

fun parseJson(input: String): Value {
    val tokens = tokenize(input.toCharArray())
    return parseValue(tokens).first
}

fun parseValue(tokens: List<TokenString>, startPos: Int = 0, parent: Value? = null): Pair<Value, Int> {
    return when (tokens[startPos]) {
        is OpenWhiskerToken -> parseObject(startPos, tokens, parent)
        is OpenBracketToken -> parseArray(startPos, tokens, parent)
        is PrimitiveToken -> parseLiteral(startPos, tokens, parent) to startPos + 1
        is ExpressionToken -> parseLiteral(startPos, tokens, parent) to startPos + 1
        else -> throw IllegalArgumentException("Invalid value start at position ${tokens[startPos].range.first}")
    }
}

fun parseLiteral(startPos: Int, tokens: List<TokenString>, parent: Value?): Value {
    val token = tokens.get(startPos)
    return when (token) {
        is NullToken -> Null(parent, token.range)
        is BooleanToken -> BooleanValue(parent, token.range, token.text.toBoolean())
        is StringToken -> StringValue(parent, token.range, token.text.substring(1..<token.text.length - 1))
        is NumberToken -> when (token.isDouble) {
            true -> DoubleValue(parent, token.range, token.text.toDouble())
            false -> LongValue(parent, token.range, token.text.toLong())
        }

        is ExpressionToken -> {
            val expr = buildExpressionTree(
                tokenizeExpression(token.text)!!
            )
            return ExpressionValue(parent, expr, token.range, token.text)
        }

        else -> throw IllegalArgumentException("Invalid literal at position ${token.range.first}")
    }
}

fun parseArray(startPos: Int, tokens: List<TokenString>, parent: Value?): Pair<ArrayValue, Int> {
    var pos = startPos

    if (pos >= tokens.size) {
        throw IllegalArgumentException("Invalid element of array at position ${tokens[startPos].range.first}")
    } else if (tokens[pos] !is OpenBracketToken) {
        throw IllegalArgumentException("Invalid array element at position ${tokens[pos].range.first}")
    }

    pos++

    val children = mutableListOf<Value>()
    val av = ArrayValue(parent, tokens[startPos].range.first..tokens[pos].range.last, children)


    while (pos < tokens.size && tokens[pos] !is CloseBracketToken) {

        if (children.isNotEmpty()) {
            if (tokens[pos] !is CommaToken) {
                throw IllegalArgumentException("Invalid array element at position ${tokens[pos].range.first}")
            }
            pos++
        }

        val (value, newPos) = parseValue(tokens, pos, av)

        pos = newPos

        children.add(value)
    }

    if (pos >= tokens.size) {
        throw IllegalArgumentException("Invalid element of array at position ${tokens[startPos].range.first}")
    } else if (tokens[pos] !is CloseBracketToken) {
        throw IllegalArgumentException("Invalid array element at position ${tokens[pos].range.first}")
    }

    return av to pos + 1
}


fun parseObject(startPos: Int, tokens: List<TokenString>, parent: Value?): Pair<ObjectValue, Int> {
    var pos = startPos

    if (pos >= tokens.size) {
        throw IllegalArgumentException("Invalid element of object at position ${tokens[startPos].range.first}")
    } else if (tokens[pos] !is OpenWhiskerToken) {
        throw IllegalArgumentException("Invalid object element at position ${tokens[pos].range.first}")
    }

    pos++

    val children = mutableMapOf<String, Value>()
    val ov = ObjectValue(parent, tokens[startPos].range.first..tokens[pos].range.last, children)

    while (pos < tokens.size && tokens[pos] !is CloseWhiskerToken) {
        if (children.isNotEmpty()) {
            if (tokens[pos] !is CommaToken) {
                throw IllegalArgumentException("Invalid array element at position ${tokens[pos].range.first}")
            }
            pos++
        }

        val key = parseLiteral(pos, tokens, ov)
        if (key !is StringValue) {
            throw IllegalArgumentException("Invalid object key at position ${tokens[pos].range.first}")
        }

        pos++

        if (pos >= tokens.size || tokens[pos] !is ColonToken) {
            throw IllegalArgumentException("Invalid object element at position ${tokens[tokens.size - 1].range.first}")
        }

        pos++

        val (value, newPos) = parseValue(tokens, pos, ov)

        pos = newPos

        children[key.text] = value
    }

    if (pos >= tokens.size) {
        throw IllegalArgumentException("Invalid element of array at position ${tokens[startPos].range.first}")
    } else if (tokens[pos] !is CloseWhiskerToken) {
        throw IllegalArgumentException("Invalid array element at position ${tokens[pos].range.first}")
    }

    return ov to pos + 1
}

//
//
//// GRAMMAR
////
//// value :== object | array | primitive | expressionRoot
//
//// expressionRoot := ':=' expression
//// expression :== arithmeticExpression | booleanExpression | stringExpression
//// valueOrReference :== primitive | reference
//// reference :== identifier ('.' identifier)*
//// identifier :== [a-zA-Z_][a-zA-Z0-9_]*
//
//// arithmeticExpression :== valueOrReference | arithmeticFactor ('+' | '-' | '*' | '/') arithmeticFactor
//// arithmeticFactor :== valueOrReference | '(' arithmeticExpression ')'
//// booleanExpression :== valueOrReference | booleanFactor ('&&' || '||') booleanFactor
//// booleanFactor :== valueOrReference | comparison | '(' booleanExpression ')'
//// comparison :== valueOrReference ('==' | '!=' | '<' | '<=' | '>' | '>=') valueOrReference
//// stringExpression :== '`' <<kotlin template string>> '`'
//
//// object :== '{' (string ':' value (',' string ':' value)*)? '}'
//// array :== '[' (value (',' value)*)? ']'
//// primitive :== string | long | double | boolean | null
//// string :== '"' [^"]* '"'
//// long :== '-'? [1-9][0-9]*
//// double :== '-'? [1-9][0-9]* '.' [0-9]+
//// boolean :== 'true' | 'false'
//// null :== 'null'
//
