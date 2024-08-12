package org.dgawlik.model

import org.dgawlik.org.dgawlik.model.Expression
import org.dgawlik.org.dgawlik.model.eval

abstract class Value(open val parent: Value?, private val rng: IntRange) {

    fun range(): IntRange {
        return rng
    }

    fun toJsonObject(): Any? {
        return when (this) {
            is ObjectValue -> {
                val map = mutableMapOf<String, Any?>()
                for ((key, value) in children) {
                    map[key] = value.toJsonObject()
                }
                map.toMap()
            }
            is ArrayValue -> {
                val list = mutableListOf<Any?>()
                for (value in children) {
                    list.add(value.toJsonObject())
                }
                list.toList()
            }
            is LongValue -> value
            is DoubleValue -> value
            is StringValue -> text
            is BooleanValue -> value
            is Null -> null
            is ExpressionValue -> expr?.eval(this)
            else -> throw IllegalArgumentException("Value is not an object")
        }
    }
}

abstract class Literal(override val parent: Value?, private val rng: IntRange) : Value(parent, rng)


open class Null(override val parent: Value?,private val rng: IntRange) : Literal(parent, rng)

open class LongValue(override val parent: Value?, private val rng: IntRange, val value: Long) : Literal(parent, rng)

open class DoubleValue(override val parent: Value?, private val rng: IntRange, val value: Double) : Literal(parent, rng)

open class StringValue(override val parent: Value?,private val rng: IntRange, val text: String) : Literal(parent, rng)

open class BooleanValue(override val parent: Value?,private val rng: IntRange, val value: Boolean) : Literal(parent, rng)

open class ExpressionValue(override val parent: Value?, val expr: Expression?, private val rng: IntRange, val text: String) : Literal(parent, rng)

open class ArrayValue(override val parent: Value?, private val rng: IntRange, val children: List<Value>) : Value(parent, rng)

open class ObjectValue(override val parent: Value?,private val rng: IntRange, val children: Map<String, Value>) : Value(parent, rng)




