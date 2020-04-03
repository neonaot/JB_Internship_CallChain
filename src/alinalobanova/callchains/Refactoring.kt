package alinalobanova.callchains

private fun substituteInNumberExpression(where: NumberExpression, toWhat: NumberExpression): NumberExpression {
    return when (where) {
        is ArithmeticExpression -> ArithmeticExpression(
            substituteInNumberExpression(where.firstArgument, toWhat),
            where.operation,
            substituteInNumberExpression(where.secondArgument, toWhat)
        )
        is Element -> toWhat
        is ConstExpression -> where
    }
}

private fun substituteInBooleanExpression(where: BooleanExpression, toWhat: NumberExpression): BooleanExpression {
    return when (where) {
        is RelationExpression -> RelationExpression(
            substituteInNumberExpression(where.firstArgument, toWhat),
            where.operation,
            substituteInNumberExpression(where.secondArgument, toWhat)
        )
        is BooleanBinaryExpression -> BooleanBinaryExpression(
            substituteInBooleanExpression(where.firstArgument, toWhat),
            where.operation,
            substituteInBooleanExpression(where.secondArgument, toWhat)
        )
    }
}

fun joinTwoMapCalls(first: MapCall, second: MapCall): MapCall {
    return MapCall(
        substituteInNumberExpression(
            where = second.expression,
            toWhat = first.expression
        )
    )
}

fun joinTwoFilterCalls(first: FilterCall, second: FilterCall): FilterCall {
    return FilterCall(
        BooleanBinaryExpression(
            first.expression,
            BooleanOperation.And,
            second.expression
        )
    )
}

private fun swapMapAndFollowingFilter(chain: CallChain, mapIndex: Int) {
    val filterIndex = mapIndex + 1
    val newFilter = FilterCall(
        substituteInBooleanExpression(
            (chain.calls[filterIndex] as FilterCall).expression,
            (chain.calls[mapIndex] as MapCall).expression
        )
    )
    chain.calls[filterIndex] = chain.calls[mapIndex]
    chain.calls[mapIndex] = newFilter
}

/**
 * Joins every sequence of calls of the same type in the [chain].
 * After that operation the resulting chain is equivalent to the chain before,
 * and the types of calls are alternating.
 **/
fun joinRepeated(chain: CallChain) {
    var i = 0
    while (i < chain.calls.size - 1) {

        val call1 = chain.calls[i]
        val call2 = chain.calls[i + 1]
        if (call1 is MapCall && call2 is MapCall) {
            val newMap = joinTwoMapCalls(call1, call2)
            chain.calls.removeAt(i)
            chain.calls.removeAt(i)
            chain.calls.add(i, newMap)
        } else if (call1 is FilterCall && call2 is FilterCall) {
            val newFilter = joinTwoFilterCalls(call1, call2)
            chain.calls.removeAt(i)
            chain.calls.removeAt(i)
            chain.calls.add(i, newFilter)
        } else {
            i++
        }
    }

}

/**
 * Transforms the [chain] to the form: "<filter-call> “%>%” <map-call>".
 **/
fun refactorCallChain(chain: CallChain) {
    if (chain.calls.isEmpty()) {
        chain.calls.add(NO_OP_FILTER)
        chain.calls.add(NO_OP_MAP)
        return
    }

    joinRepeated(chain)

    var firstMapIndex = 0
    if (chain.calls[0] is FilterCall) {
        firstMapIndex = 1
    }

    while (firstMapIndex + 1 < chain.calls.size) {
        swapMapAndFollowingFilter(chain, mapIndex = firstMapIndex)
        joinRepeated(chain)
        firstMapIndex = 1
    }
    
    if (chain.calls.size == 1) {
        if (chain.calls[0] is MapCall) {
            chain.calls.add(0, NO_OP_FILTER)
        } else {
            chain.calls.add(NO_OP_MAP)
        }
    }
}
