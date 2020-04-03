package alinalobanova.callchains

import java.lang.Exception
import java.lang.StringBuilder

const val MAP = "map"
const val FILTER = "filter"
const val NEXT_CALL_OPERATOR = "%>%"

val NO_OP_FILTER = FilterCall(
    RelationExpression(
        ConstExpression(0),
        RelationOperation.Equals,
        ConstExpression(0)
    )
)
val NO_OP_MAP = MapCall(Element)


enum class ArithmeticOperation(val character: Char) {
    Plus('+'),
    Minus('-'),
    Multiply('*')
}

enum class RelationOperation(val character: Char) {
    Equals('='),
    MoreThan('>'),
    LessThan('<')
}

enum class BooleanOperation(val character: Char) {
    And('&'),
    Or('|')
}


sealed class Expression {
    abstract fun print(stringBuilder: StringBuilder)
}

sealed class NumberExpression : Expression()
sealed class BooleanExpression : Expression()

data class ConstExpression(val value: Int) : NumberExpression() {
    override fun print(stringBuilder: StringBuilder) {
        stringBuilder.append(value)
    }
}


data class RelationExpression(
    val firstArgument: NumberExpression,
    val operation: RelationOperation,
    val secondArgument: NumberExpression
) : BooleanExpression() {
    override fun print(stringBuilder: StringBuilder) {
        stringBuilder.append("(")
        firstArgument.print(stringBuilder)
        stringBuilder.append(operation.character)
        secondArgument.print(stringBuilder)
        stringBuilder.append(")")
    }
}

data class ArithmeticExpression(
    val firstArgument: NumberExpression,
    val operation: ArithmeticOperation,
    val secondArgument: NumberExpression
) : NumberExpression() {
    override fun print(stringBuilder: StringBuilder) {
        stringBuilder.append("(")
        firstArgument.print(stringBuilder)
        stringBuilder.append(operation.character)
        secondArgument.print(stringBuilder)
        stringBuilder.append(")")
    }
}


data class BooleanBinaryExpression(
    val firstArgument: BooleanExpression,
    val operation: BooleanOperation,
    val secondArgument: BooleanExpression
) : BooleanExpression() {
    override fun print(stringBuilder: StringBuilder) {
        stringBuilder.append("(")
        firstArgument.print(stringBuilder)
        stringBuilder.append(operation.character)
        secondArgument.print(stringBuilder)
        stringBuilder.append(")")
    }
}


object Element : NumberExpression() {
    override fun print(stringBuilder: StringBuilder) {
        stringBuilder.append(TEXT)
    }

    const val TEXT = "element"
}

sealed class Call {
    abstract val expression: Expression
    abstract fun print(stringBuilder: StringBuilder)
}

data class FilterCall(override val expression: BooleanExpression) : Call() {
    override fun print(stringBuilder: StringBuilder) {
        stringBuilder.append("$FILTER{")
        expression.print(stringBuilder)
        stringBuilder.append('}')
    }
}


data class MapCall(override val expression: NumberExpression) : Call() {
    override fun print(stringBuilder: StringBuilder) {
        stringBuilder.append("$MAP{")
        expression.print(stringBuilder)
        stringBuilder.append('}')
    }
}

data class CallChain(val calls: MutableList<Call> = mutableListOf()) {
    fun print(stringBuilder: StringBuilder) {
        for ((i, v) in calls.withIndex()) {
            v.print(stringBuilder)
            if (i != calls.size - 1) {
                stringBuilder.append(NEXT_CALL_OPERATOR)
            }
        }
    }
}

class SyntaxError(message: String) : Exception(message)

class TypeError(message: String) : Exception(message)