package org.dgawlik.org.dgawlik.model

import org.dgawlik.model.*


fun Value.toLong(): Long {
    return when (this) {
        is LongValue -> value
        else -> throw IllegalArgumentException("Value is not a number")
    }
}

fun Value.toDouble(): Double {
    return when (this) {
        is LongValue -> value.toDouble()
        is DoubleValue -> value
        else -> throw IllegalArgumentException("Value is not a number")
    }
}

fun Value.toStringValue(): String {
    return when (this) {
        is StringValue -> text
        is DoubleValue -> value.toString()
        is LongValue -> value.toString()
        is BooleanValue -> value.toString()
        is Null -> "null"
        is ArrayValue -> children.joinToString(prefix = "[", postfix = "]", separator = ",") { it.toStringValue() }
        is ObjectValue -> children.entries.joinToString(
            prefix = "{",
            postfix = "}",
            separator = ","
        ) { "${it.key}:${it.value}" }

        else -> throw IllegalArgumentException("Unknown value type")
    }
}

fun Value.toBoolean(): Boolean {
    return when (this) {
        is BooleanValue -> value
        else -> throw IllegalArgumentException("Value is not a boolean")
    }
}

fun Value.toMap(): Map<String, Value> {
    return when (this) {
        is ObjectValue -> children
        else -> throw IllegalArgumentException("Value is not an object")
    }
}

fun Value.toList(): List<Value> {
    return when (this) {
        is ArrayValue -> children
        else -> throw IllegalArgumentException("Value is not an array")
    }
}

fun Value.query(segments: List<String> = emptyList(), i: Int = 0): Value? {

    if (i >= segments.size || segments[i].isEmpty()){
        return this
    }

    if (this is ArrayValue && segments[i].toIntOrNull() != null) {
        val index = segments[i].toInt()
        if (index >= children.size) {
            return null
        }
        return children[index].query(segments, i + 1)
    } else if (this is ObjectValue && children.containsKey(segments[i])) {
        val field = children[segments[i]]!!
        return if (i == segments.size - 1) {
            field
        } else {
            field.query(segments, i + 1)
        }
    } else {
        return null
    }
}
