import org.dgawlik.model.ArrayValue
import org.dgawlik.model.Null
import org.dgawlik.model.ObjectValue
import org.dgawlik.org.dgawlik.model.ColonToken
import org.dgawlik.org.dgawlik.model.tokenize
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ParserTests {


    @Test
    fun `correctly parses a valid literal`(){
        val input = "null".toCharArray()
        val result = parseLiteral(0, tokenize(input), null)
        assert(result is Null)
    }

    @Test
    fun `parseLiteral returns null for everything else`(){
        assertThrows<IllegalArgumentException> { parseLiteral(0, listOf(ColonToken(0..0)), null)  }
    }

    @Test
    fun `correctly parses empty object`(){
        val input = "{}".toCharArray()
        val (result, _) = parseValue(tokenize(input))
        assert(result is ObjectValue)
    }

    @Test
    fun `correctly parses empty array`(){
        val input = "[]".toCharArray()
        val (result, _) = parseValue(tokenize(input))
        assert(result is ArrayValue)
    }

    @Test
    fun `correctly parses array of longs`(){
        val input = "[1, 2, 3]".toCharArray()
        val (result, _) = parseValue(tokenize(input))
        assert(result is ArrayValue)
    }

    @Test
    fun `correctly parses array of doubles`(){
        val input = "[1.12, 2e-10, -3.0]".toCharArray()
        val (result, _) = parseValue(tokenize(input))
        assert(result is ArrayValue)
    }

    @Test
    fun `correctly parses array of nulls`(){
        val input = "[null, null]".toCharArray()
        val (result, _) = parseValue(tokenize(input))
        assert(result is ArrayValue)
    }

    @Test
    fun `correctly parses array of booleans`(){
        val input = "[true, false, true]".toCharArray()
        val (result, _) = parseValue(tokenize(input))
        assert(result is ArrayValue)
    }

    @Test
    fun `correctly parses array of strings`(){
        val input = "[\"test1\", \"test2\"]".toCharArray()
        val (result, _) = parseValue(tokenize(input))
        assert(result is ArrayValue)
    }

    @Test
    fun `correctly parses array with heterogeneous data`(){
        val input = "[\"test1\", 1, 2.0, true, null]".toCharArray()
        val (result, _) = parseValue(tokenize(input))
        assert(result is ArrayValue)
    }

    @Test
    fun `arrays support nesting`(){
        val input = "[[1, 2], [3, 4], []]".toCharArray()
        val (result, _) = parseValue(tokenize(input))
        assert(result is ArrayValue)
    }

    @Test
    fun `objects support heterogeneous data`(){
        val input = "{\"key1\": 1, \"key2\": 2.0, \"key3\": true, \"key4\": null}".toCharArray()
        val (result, _) = parseValue(tokenize(input))
        assert(result is ObjectValue)
    }

    @Test
    fun `objects support nested arrays`(){
        val input = "{\"key1\": [1,2], \"key2\": []}".toCharArray()
        val (result, _) = parseValue(tokenize(input))
        assert(result is ObjectValue)
    }

    @Test
    fun `arrays support nested objects` () {
        val input = "[{\"key1\": 1, \"key2\": 2}, {\"key3\": 3}]".toCharArray()
        val (result, _) = parseValue(tokenize(input))
        assert(result is ArrayValue)
    }


}