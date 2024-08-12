import org.dgawlik.model.ExpressionValue
import org.dgawlik.org.dgawlik.model.*
import org.junit.jupiter.api.Test

class ExpressionTests {

    @Test
    fun `correctly parses a valid expression`() {
        val input = "1 + 2"
        val tokens = tokenizeExpression(input)
        assert(tokens != null)
    }

    @Test
    fun `correctly parses a valid long expression`() {
        val input = "1 + 2 + 3.0 + a.a + b"
        val tokens = tokenizeExpression(input)
        assert(tokens != null)
    }

    @Test
    fun `correctly parses a valid parenthesized expression`() {
        val input = "(1 + 2) + (3.0 + (a.a + b))"
        val tokens = tokenizeExpression(input)
        assert(tokens != null)
    }

    @Test
    fun `correctly builds tree`() {
        val input = "(1 + 2) + (3.0 + (a.a + b))"
        val tree = buildExpressionTree(tokenizeExpression(input)!!)
    }

    @Test
    fun `evals currectly simple expression`() {
        val input = "1 + 2 + 3"
        val tree = buildExpressionTree(tokenizeExpression(input)!!)
        assert(6L == tree?.eval() as Long)
    }

    @Test
    fun `evals currectly parenthesized expression`() {
        val input = "3 * (2 + 1)"
        val tree = buildExpressionTree(tokenizeExpression(input)!!)
        assert(9L == tree?.eval() as Long)
    }

    @Test
    fun `evals correctly simple string expressions` () {
        val input = "\"hello\" | \" \" | \"world\""
        val tree = buildExpressionTree(tokenizeExpression(input)!!)
        assert("hello world" == tree?.eval() as String)
    }

    @Test
    fun `evals correctly comparisons` () {
        val input = "1 < 2"
        val tree = buildExpressionTree(tokenizeExpression(input)!!)
        val result = tree?.eval() as Boolean
        assert(result)
    }

    @Test
    fun `evals correctly comparisons (2)` () {
        val input = "(1 < 2) && (2 < 1)"
        val tree = buildExpressionTree(tokenizeExpression(input)!!)
        val result = tree?.eval() as Boolean
        assert(!result)
    }

    @Test
    fun `evals use lookups correctly` () {
        val input = "{\"a\": 1, \"b\": 2, \"c\": \${a + b}}".toCharArray()
        val (json, _) = parseValue(tokenize(input))
        val expr = json.query(listOf("c")) as ExpressionValue
        assert(3L == expr.expr?.eval(json) as Long)
    }
}