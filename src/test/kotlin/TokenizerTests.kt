import org.dgawlik.org.dgawlik.model.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class TokenizerTests {

    @Test
    fun `detects unescaped double quote`() {
        val input = "\"".toCharArray()
        val result = isUnescapedDoubleQuote(input, 0)
        assert(result)
    }

    @Test
    fun `doesn't detect escaped double quote`() {
        val input = "\\\"".toCharArray()
        val result = isUnescapedDoubleQuote(input, 1)
        assert(!result)
    }

    @Test
    fun `detects if escaping is itself escaped with double quote`() {
        val input = "\\\\\"".toCharArray()
        val result = isUnescapedDoubleQuote(input, 2)
        assert(result)
    }

    @Test
    fun `correctly tokenizes 'null'`() {
        val input = "null".toCharArray()
        val result = input.takeNull(0)
        assert(result != null)
        assert(result!!.text == "null")
        assert(result.range == 0 until 4)
    }

    @Test
    fun `doesn't tokenize malformed null 'null'`() {
        val input = "nu".toCharArray()
        val result = input.takeNull(0)
        assert(result == null)
    }

    @Test
    fun `correctly tokenizes string`() {
        val input = "\"hello\"".toCharArray()
        val result = input.takeString(0)
        assert(result != null)
        assert(result!!.text == "\"hello\"")
        assert(result.range == 0 until 7)
    }

    @Test
    fun `correctly tokenizes string with escaped quote`() {
        val input = "\"hello\\\" world\"".toCharArray()
        val result = input.takeString(0)
        assert(result != null)
        assert(result!!.text == "\"hello\\\" world\"")
        assert(result.range == 0 until "\"hello\\\" world\"".length)
    }

    @Test
    fun `correctly tokenizes 'false'`() {
        val input = "false".toCharArray()
        val result = input.takeBoolean(0)
        assert(result != null)
        assert(result!!.text == "false")
        assert(result.range == 0 until 5)
    }

    @Test
    fun `correctly tokenizes 'true'`() {
        val input = "true".toCharArray()
        val result = input.takeBoolean(0)
        assert(result != null)
        assert(result!!.text == "true")
        assert(result.range == 0 until 4)
    }

    @Test
    fun `doesn't tokenize malformed boolean`() {
        val input = "tru".toCharArray()
        val result = input.takeBoolean(0)
        assert(result == null)
    }

    @Test
    fun `should parse 0`() {
        val input = "0".toCharArray()
        val result = input.takeNumber(0)
        assert(result != null)
        assert(result!!.text == "0")
        assert(result.range == 0 until 1)
    }

    @Test
    fun `shouldn't parse 00`() {
        val input = "00".toCharArray()
        val result = input.takeNumber(0)
        assert(result == null)
    }

    @Test
    fun `should parse long`() {
        val input = "1234567890".toCharArray()
        val result = input.takeNumber(0)
        assert(result != null)
        assert(result!!.text == "1234567890")
        assert(result.range == 0 until 10)
    }

    @Test
    fun `should parse double`() {
        val input = "123.456".toCharArray()
        val result = input.takeNumber(0)
        assert(result != null)
        assert(result!!.text == "123.456")
        assert(result.range == 0 until 7)
    }

    @Test
    fun `should parse double with exponent`() {
        val input = "123.456e-7".toCharArray()
        val result = input.takeNumber(0)
        assert(result != null)
        assert(result!!.text == "123.456e-7")
        assert(result.range == 0 until 10)
    }

    @Test
    fun `should parse double with exponent (2)`() {
        val input = "123.456E-7".toCharArray()
        val result = input.takeNumber(0)
        assert(result != null)
        assert(result!!.text == "123.456E-7")
        assert(result.range == 0 until 10)
    }

    @Test
    fun `shouldn't parse malformed double`() {
        val input = "123.a".toCharArray()
        val result = input.takeNumber(0)
        assert(result == null)
    }

    @Test
    fun `shouldn't parse malformed double (2)`() {
        val input = "123a".toCharArray()
        val result = input.takeNumber(0)
        assert(result == null)
    }

    @Test
    fun `shouldn't parse malformed double (3)`() {
        val input = "123E-10a".toCharArray()
        val result = input.takeNumber(0)
        assert(result == null)
    }

    @Test
    fun `should tokenize all valid tokens`() {
        val input = "{},[]: 123.345 1 \"str\" true null".toCharArray()
        val tokens = tokenize(input).map { it.text }
        assert(tokens.size == 11)
        listOf("{", "}", ",", "[", "]", ":", "123.345", "1", "\"str\"", "true", "null").zip(tokens).forEach {
            val (expected, actual) = it
            assert(expected == actual)
        }
    }

    @Test
    fun `should reject invalid token`() {
        val input = "a".toCharArray()
        assertThrows<IllegalArgumentException> {
            tokenize(input)
        }
    }

    @Test
    fun `should reject invalid token (2)`() {
        val input = ">".toCharArray()
        assertThrows<IllegalArgumentException> {
            tokenize(input)
        }
    }
}