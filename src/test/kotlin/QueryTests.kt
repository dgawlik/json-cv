import org.dgawlik.org.dgawlik.model.*
import org.junit.jupiter.api.Test

class QueryTests {

    @Test
    fun `correctly passes string`(){
        val input = "\"hello\"".toCharArray()
        val (value, _) = parseValue(tokenize(input))
        assert("hello" == value.toStringValue())
    }

    @Test
    fun `correctly passes long`(){
        val input = "1".toCharArray()
        val (value, _) = parseValue(tokenize(input))
        assert(1L == value.toLong())
    }

    @Test
    fun `correctly passes double`(){
        val input = "1.0".toCharArray()
        val (value, _) = parseValue(tokenize(input))
        assert(1.0 == value.toDouble())
    }

    @Test
    fun `correctly passes boolean`(){
        val input = "true".toCharArray()
        val (value, _) = parseValue(tokenize(input))
        assert(value.toBoolean())
    }

    @Test
    fun `correctly passes array of numbers`(){
        val input = "[1, 2, 3]".toCharArray()
        val (value, _) = parseValue(tokenize(input))
        assert(value.toList().map { it.toLong() } == listOf(1L, 2L, 3L))
    }

    @Test
    fun `query object flat`(){
        val input = "{\"a\":1, \"b\":2, \"c\":3}".toCharArray()
        val (value, _) = parseValue(tokenize(input))
        assert(value.query(listOf("a"))?.toLong() == 1L)
        assert(value.query(listOf("b"))?.toLong() == 2L)
        assert(value.query(listOf("c"))?.toLong() == 3L)
    }

    @Test
    fun `query array flat`(){
        val input = "[1, 2, 3]".toCharArray()
        val (value, _) = parseValue(tokenize(input))
        assert(value.query(listOf("0"))?.toLong() == 1L)
        assert(value.query(listOf("1"))?.toLong() == 2L)
        assert(value.query(listOf("2"))?.toLong() == 3L)
    }

    @Test
    fun `query array nested in object`(){
        val input = "{\"a\": [1, 2, 3]}".toCharArray()
        val (value, _) = parseValue(tokenize(input))
        assert(value.query(listOf("a", "0"))?.toLong() == 1L)
        assert(value.query(listOf("a", "1"))?.toLong() == 2L)
        assert(value.query(listOf("a", "2"))?.toLong() == 3L)
    }
}