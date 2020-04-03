package alinalobanova.callchains

fun parseCallChain(input: String): CallChain {
    val chain = CallChain()
    if(input.isEmpty()){
        throw SyntaxError("Expected call chain, found nothing")
    }

    var remainingInput = input
    while (remainingInput.isNotEmpty()) {
        val endOfCall = remainingInput.indexOf('}')
        if (endOfCall == -1) {
            throw SyntaxError("Expected call chain, found '$remainingInput'")
        }

        chain.calls.add(parseCall(remainingInput.take(endOfCall + 1)))

        remainingInput = remainingInput.drop(endOfCall + 1)

        if (remainingInput.isEmpty()) {
            break
        } else if (remainingInput.startsWith(NEXT_CALL_OPERATOR)) {
            remainingInput = remainingInput.removePrefix(NEXT_CALL_OPERATOR)
        } else {
            throw SyntaxError("Expected call chain, found '$remainingInput'")
        }

    }
    return chain
}


private fun parseCall(input: String): Call {
    val startOfExpression = input.indexOf('{') + 1
    if (startOfExpression == 0) {
        throw SyntaxError("Expected call, found '$input'")
    }
    return when (val callType = input.take(startOfExpression - 1)) {
        MAP -> parseMapCall(input)
        FILTER -> parseFilterCall(input)
        else -> throw SyntaxError("Expected '$MAP' or '$FILTER', found '$callType'")
    }

}

private fun parseArithmeticOperationOrNull(c: Char): ArithmeticOperation? {
    return ArithmeticOperation.values().find { it.character == c }
}

private fun parseRelationOperationOrNull(c: Char): RelationOperation? {
    return RelationOperation.values().find { it.character == c }
}

private fun parseBooleanOperationOrNull(c: Char): BooleanOperation? {
    return BooleanOperation.values().find { it.character == c }
}


private fun findEndOfFirstArgument(input: String): Int {
    if (input.isEmpty()){
        throw SyntaxError("Expected expression, found: '$input'")
    }
    var result: Int? = null
    if (input[0] == '(') {
        var depth = 0
        for ((i, char) in input.withIndex()) {
            if (char == '(') {
                depth++
            } else if (char == ')') {
                depth--
            }
            if (depth == 0) {
                result = i + 1
                break
            }
        }

    } else if (input.startsWith(Element.TEXT)) {
        result = Element.TEXT.length

    } else if (input[0].isDigit() || input[0] == '-') {
        for (i in 1 until input.length) {
            if (!input[i].isDigit()) {
                result = i
                break
            }
        }

        if (input[0] == '-' && result == 1){
            throw SyntaxError("Expected expression, found: '$input'")
        }
    }

    if (result == null || result >= input.length) {
        throw SyntaxError("Expected binary expression, found '$input'")
    }

    return result
}


fun parseNumberExpression(input: String): NumberExpression {

    return when {
        input == Element.TEXT -> Element

        input.toIntOrNull() != null -> ConstExpression(input.toInt())

        input.startsWith('(') && input.endsWith(')') -> parseArithmeticExpression(input)

        else -> {
            if (input.isNotEmpty() &&
                input.drop(1).all { it.isDigit() } &&
                ((input[0] == '-' && input.length > 1) || input[0].isDigit())
            ) {
                throw SyntaxError("Number value is out of range: '$input'")
            }
            throw SyntaxError("Expected number expression, found '$input'")
        }
    }
}

fun parseArithmeticExpression(input: String): ArithmeticExpression {

    if (!input.startsWith('(') || !input.endsWith(')') || input.length <= 2) {
        throw SyntaxError("Expected arithmetic expression, found '$input")
    }
    val expressionContent = input.substring(1, input.length - 1)

    val endOfFirstArg = findEndOfFirstArgument(expressionContent)


    val operationChar = expressionContent[endOfFirstArg]
    val operation = parseArithmeticOperationOrNull(operationChar)
    if (operation == null) {
        if (parseRelationOperationOrNull(operationChar) != null ||
            parseBooleanOperationOrNull(operationChar) != null) {
            throw TypeError("Expected arithmetic operation, found '$expressionContent'")
        }
        throw SyntaxError("Expected arithmetic operation, found '$expressionContent'")
    }

    return ArithmeticExpression(
        parseNumberExpression(expressionContent.substring(0, endOfFirstArg)),
        operation,
        parseNumberExpression(
            expressionContent.substring(
                endOfFirstArg + 1,
                expressionContent.length
            )
        )
    )
}


fun parseBooleanExpression(input: String): BooleanExpression {
    if (!input.startsWith('(') || !input.endsWith(')') || input.length < 2) {
        try {
            parseNumberExpression(input)
        } catch (e: SyntaxError) {
            throw SyntaxError("Expected expression, found '$input")
        }
        throw TypeError("Expected boolean expression, found '$input'")
    }

    val expressionContent = input.dropLast(1).drop(1)

    val endOfFirstArg = findEndOfFirstArgument(expressionContent)

    val operationChar = expressionContent[endOfFirstArg]
    val boolOperation = parseBooleanOperationOrNull(operationChar)
    val relationOperation = parseRelationOperationOrNull(operationChar)
    return when {
        relationOperation != null -> {
             RelationExpression(
                parseNumberExpression(expressionContent.take(endOfFirstArg)),
                relationOperation,
                parseNumberExpression(expressionContent.drop(endOfFirstArg + 1))
            )
        }
        boolOperation != null -> {
            BooleanBinaryExpression(
                parseBooleanExpression(expressionContent.take(endOfFirstArg)),
                boolOperation,
                parseBooleanExpression(expressionContent.drop(endOfFirstArg + 1))
            )
        }
        parseArithmeticOperationOrNull(operationChar) != null -> {
            throw TypeError("Expected boolean expression, found '$input'")
        }
        else -> {
            throw SyntaxError("Expected boolean expression, found '$input'")
        }
    }
}


fun parseMapCall(input: String): MapCall {
    return MapCall(parseNumberExpression(input.substring(MAP.length + 1, input.length - 1)))
}

fun parseFilterCall(input: String): FilterCall {
    return FilterCall(parseBooleanExpression(input.substring(FILTER.length + 1, input.length - 1)))
}
