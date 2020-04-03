package alinalobanova.callchains

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.StringBuilder

internal class Tests {
    @Test
    fun print() {
        val input = "map{(element+10)}%>%filter{((element>10)|(element=5))}%>%map{(element*element)}"
        val stringBuilder = StringBuilder()
        parseCallChain(input).print(stringBuilder)
        assertEquals(input, stringBuilder.toString())
    }

    @Test
    fun wrongType() {
        var input = "filter{(element+6)}"
        assertThrows<TypeError> {
            parseCallChain(input)
        }

        input = "map{((element>20)+76)}"
        assertThrows<TypeError> {
            parseCallChain(input)
        }

        input = "map{((element>20)&76)}"
        assertThrows<TypeError> {
            parseCallChain(input)
        }

        input = "filter{((element>20)-76)}"
        assertThrows<TypeError> {
            parseCallChain(input)
        }

        input = "filter{((element>20)-76)}%!%map{((element>20)&76)}"
        assertThrows<TypeError> {
            parseCallChain(input)
        }

        input = "filter{element}"
        assertThrows<TypeError> {
            parseCallChain(input)
        }


    }

    @Test
    fun wrongInput() {

        var input = "map{(ой)}"
        assertThrows<SyntaxError> {
            parseCallChain(input)
        }

        input = "kva{(ой)}"
        assertThrows<SyntaxError> {
            parseCallChain(input)
        }

        input = "map{(element!10)}"
        assertThrows<SyntaxError> {
            parseCallChain(input)
        }

        input = "map{{(ой)}"
        assertThrows<SyntaxError> {
            parseCallChain(input)
        }


        input = "map{(-*element)}"
        assertThrows<SyntaxError> {
            parseCallChain(input)
        }

        input = "filter{((element>20)-76)"
        assertThrows<SyntaxError> {
            parseCallChain(input)
        }

        input = ""
        assertThrows<SyntaxError> {
            parseCallChain(input)
        }

        input = "filter{}"
        assertThrows<SyntaxError> {
            parseCallChain(input)
        }


        input = "filter{((element-20)=76)}кв"
        assertThrows<SyntaxError> {
            parseCallChain(input)
        }

        input = "filter((element-20)=76)}"
        assertThrows<SyntaxError> {
            parseCallChain(input)
        }

        input = "filter{()}"
        assertThrows<SyntaxError> {
            parseCallChain(input)
        }


        input = "map{()}"
        assertThrows<SyntaxError> {
            parseCallChain(input)
        }

        input = "filter{(element!10)}"
        assertThrows<SyntaxError> {
            parseCallChain(input)
        }

        input = "filter{(10)}"
        assertThrows<SyntaxError> {
            parseCallChain(input)
        }

        input = "filter{((element-20)=76)}%!%map{((element>20)&76)}"
        assertThrows<SyntaxError> {
            parseCallChain(input)
        }

        input = "filter{(element>10000000000000000)}"
        assertThrows<SyntaxError> {
            parseCallChain(input)
        }

        input = "map{(-*element)}"
        assertThrows<SyntaxError> {
            parseCallChain(input)
        }

    }


    @Test
    fun parseNumberExpression() {
        assertEquals(
            ConstExpression(value = -566),
            parseNumberExpression("-566")
        )
        assertEquals(
            Element,
            parseNumberExpression("element")
        )
        assertEquals(
            ConstExpression(value = 34),
            parseNumberExpression("34")
        )
    }

    @Test
    fun parseArithmeticExpression() {
        assertEquals(
            ArithmeticExpression(
                firstArgument = Element,
                operation = ArithmeticOperation.Minus,
                secondArgument = ConstExpression(value = 34)
            ),
            parseArithmeticExpression("(element-34)")
        )
    }

    @Test
    fun parseBooleanExpression() {
        assertEquals(

            RelationExpression(
                firstArgument = Element,
                operation = RelationOperation.LessThan,
                secondArgument = ConstExpression(value = 34)
            ),
            parseBooleanExpression("(element<34)")
        )
    }


    @Test
    fun parseMapCall() {
        assertEquals(
            MapCall(
                expression = ArithmeticExpression(
                    firstArgument = Element,
                    operation = ArithmeticOperation.Multiply,
                    secondArgument = ConstExpression(value = 67)
                )
            ),
            parseMapCall("map{(element*67)}")
        )
        assertEquals(
            MapCall(
                expression = ArithmeticExpression(
                    firstArgument = ArithmeticExpression(
                        firstArgument = Element,
                        operation = ArithmeticOperation.Multiply,
                        secondArgument = ConstExpression(value = 67)
                    ),
                    operation = ArithmeticOperation.Minus,
                    secondArgument = ConstExpression(value = 10)
                )
            ),
            parseMapCall("map{((element*67)-10)}")
        )
    }

    @Test
    fun parseFilterCall() {
        assertEquals(
            FilterCall(
                expression = RelationExpression(
                    firstArgument = ArithmeticExpression(
                        firstArgument = Element,
                        operation = ArithmeticOperation.Multiply,
                        secondArgument = ConstExpression(value = 67)
                    ),
                    operation = RelationOperation.LessThan,
                    secondArgument = ConstExpression(value = 10)
                )
            ),
            parseFilterCall("filter{((element*67)<10)}")
        )

    }

    @Test
    fun parseCallChain() {
        val input = "filter{(element<10)}%>%map{element}%>%filter{((34<element)&(element<50))}"
        val chain = parseCallChain(input)

        assertEquals(
            CallChain(
                mutableListOf(
                    FilterCall(
                        expression = RelationExpression(
                            firstArgument = Element,
                            operation = RelationOperation.LessThan,
                            secondArgument = ConstExpression(value = 10)
                        )
                    ),
                    MapCall(expression = Element),
                    FilterCall(
                        expression = BooleanBinaryExpression(
                            firstArgument = RelationExpression(
                                firstArgument = ConstExpression(
                                    value = 34
                                ),
                                operation = RelationOperation.LessThan,
                                secondArgument = Element
                            ),
                            operation = BooleanOperation.And,
                            secondArgument = RelationExpression(
                                firstArgument = Element,
                                operation = RelationOperation.LessThan,
                                secondArgument = ConstExpression(value = 50)
                            )
                        )
                    )
                )
            ),
            chain
        )
    }


    @Test
    fun joinTwoMapCalls() {

        val map1 = MapCall(
            ArithmeticExpression(
                Element,
                ArithmeticOperation.Plus,
                ConstExpression(5)
            )
        )

        val map2 = MapCall(
            ArithmeticExpression(
                Element,
                ArithmeticOperation.Multiply,
                ConstExpression(3)
            )
        )

        assertEquals(
            MapCall(
                expression = ArithmeticExpression(
                    firstArgument = ArithmeticExpression(
                        firstArgument = Element,
                        operation = ArithmeticOperation.Plus,
                        secondArgument = ConstExpression(value = 5)
                    ),
                    operation = ArithmeticOperation.Multiply,
                    secondArgument = ConstExpression(value = 3)
                )
            ),
            joinTwoMapCalls(map1, map2)
        )
    }

    @Test
    fun joinTwoFilterCalls() {

        val filter1 = FilterCall(
            RelationExpression(
                Element,
                RelationOperation.LessThan,
                ConstExpression(5)
            )
        )
        val filter2 = FilterCall(
            RelationExpression(
                ConstExpression(
                    3
                ),
                RelationOperation.Equals,
                Element
            )
        )
        assertEquals(
            FilterCall(
                expression = BooleanBinaryExpression(
                    firstArgument = RelationExpression(
                        firstArgument = Element,
                        operation = RelationOperation.LessThan,
                        secondArgument = ConstExpression(5)
                    ),
                    operation = BooleanOperation.And,
                    secondArgument = RelationExpression(
                        firstArgument = ConstExpression(3),
                        operation = RelationOperation.Equals,
                        secondArgument = Element
                    )
                )
            ),
            joinTwoFilterCalls(filter1, filter2)
        )
    }


    @Test
    fun joinRepeated() {

        val chain = CallChain(
            mutableListOf(
                MapCall(
                    ArithmeticExpression(
                        Element,
                        ArithmeticOperation.Plus,
                        ConstExpression(5)
                    )
                ),
                MapCall(
                    ArithmeticExpression(
                        Element,
                        ArithmeticOperation.Multiply,
                        ConstExpression(3)
                    )
                ),
                FilterCall(
                    RelationExpression(
                        Element,
                        RelationOperation.MoreThan,
                        ConstExpression(239)
                    )
                ),
                MapCall(Element),
                FilterCall(
                    RelationExpression(
                        Element,
                        RelationOperation.Equals,
                        ConstExpression(30)
                    )
                ),
                FilterCall(
                    RelationExpression(
                        Element,
                        RelationOperation.LessThan,
                        ConstExpression(566)
                    )
                )
            )
        )
        joinRepeated(chain)
        assertEquals(
            CallChain(
                mutableListOf(
                    MapCall(
                        ArithmeticExpression(
                            firstArgument = ArithmeticExpression(
                                firstArgument = Element,
                                operation = ArithmeticOperation.Plus,
                                secondArgument = ConstExpression(5)
                            ),
                            operation = ArithmeticOperation.Multiply,
                            secondArgument = ConstExpression(value = 3)
                        )
                    ),
                    FilterCall(
                        expression = RelationExpression(
                            firstArgument = Element,
                            operation = RelationOperation.MoreThan,
                            secondArgument = ConstExpression(239)
                        )
                    ),
                    MapCall(expression = Element),
                    FilterCall(
                        expression = BooleanBinaryExpression(
                            firstArgument = RelationExpression(
                                firstArgument = Element,
                                operation = RelationOperation.Equals,
                                secondArgument = ConstExpression(30)
                            ),
                            operation = BooleanOperation.And,
                            secondArgument = RelationExpression(
                                firstArgument = Element,
                                operation = RelationOperation.LessThan,
                                secondArgument = ConstExpression(566)
                            )
                        )
                    )
                )
            ),
            chain
        )
    }


    @Test
    fun makeChainShorter() {

        val chainTest =
            CallChain(mutableListOf(NO_OP_FILTER))
        refactorCallChain(chainTest)


        assertEquals(
            CallChain(
                mutableListOf(
                    NO_OP_FILTER,
                    NO_OP_MAP
                )
            ), chainTest)

        chainTest.calls.clear()
        chainTest.calls.add(NO_OP_MAP)
        refactorCallChain(chainTest)
        assertEquals(
            CallChain(
                mutableListOf(
                    NO_OP_FILTER,
                    NO_OP_MAP
                )
            ), chainTest)

        chainTest.calls.clear()
        refactorCallChain(chainTest)
        assertEquals(
            CallChain(
                mutableListOf(
                    NO_OP_FILTER,
                    NO_OP_MAP
                )
            ), chainTest)


        val chain = CallChain(
            mutableListOf(
                MapCall(
                    ArithmeticExpression(
                        Element,
                        ArithmeticOperation.Plus,
                        ConstExpression(5)
                    )
                ),
                MapCall(
                    ArithmeticExpression(
                        Element,
                        ArithmeticOperation.Multiply,
                        ConstExpression(3)
                    )
                ),
                FilterCall(
                    RelationExpression(
                        Element,
                        RelationOperation.MoreThan,
                        ConstExpression(239)
                    )
                ),
                MapCall(Element),
                FilterCall(
                    RelationExpression(
                        Element,
                        RelationOperation.Equals,
                        ConstExpression(30)
                    )
                ),
                FilterCall(
                    RelationExpression(
                        Element,
                        RelationOperation.LessThan,
                        ConstExpression(566)
                    )
                )
            )
        )
        refactorCallChain(chain)

        assertEquals(
            CallChain(
                calls = mutableListOf(
                    FilterCall(
                        expression = BooleanBinaryExpression(
                            firstArgument = RelationExpression(
                                firstArgument = ArithmeticExpression(
                                    firstArgument = ArithmeticExpression(
                                        firstArgument = Element,
                                        operation = ArithmeticOperation.Plus,
                                        secondArgument = ConstExpression(
                                            value = 5
                                        )
                                    ),
                                    operation = ArithmeticOperation.Multiply,
                                    secondArgument = ConstExpression(value = 3)
                                ), operation = RelationOperation.MoreThan,
                                secondArgument = ConstExpression(value = 239)
                            ),
                            operation = BooleanOperation.And,
                            secondArgument = BooleanBinaryExpression(
                                firstArgument = RelationExpression(
                                    firstArgument = ArithmeticExpression(
                                        firstArgument = ArithmeticExpression(
                                            firstArgument = Element,
                                            operation = ArithmeticOperation.Plus,
                                            secondArgument = ConstExpression(
                                                value = 5
                                            )
                                        ),
                                        operation = ArithmeticOperation.Multiply,
                                        secondArgument = ConstExpression(
                                            value = 3
                                        )
                                    ),
                                    operation = RelationOperation.Equals,
                                    secondArgument = ConstExpression(value = 30)
                                ),
                                operation = BooleanOperation.And,
                                secondArgument = RelationExpression(
                                    firstArgument = ArithmeticExpression(
                                        firstArgument = ArithmeticExpression(
                                            firstArgument = Element,
                                            operation = ArithmeticOperation.Plus,
                                            secondArgument = ConstExpression(
                                                value = 5
                                            )
                                        ),
                                        operation = ArithmeticOperation.Multiply,
                                        secondArgument = ConstExpression(
                                            value = 3
                                        )
                                    ),
                                    operation = RelationOperation.LessThan,
                                    secondArgument = ConstExpression(value = 566)
                                )
                            )
                        )
                    ),
                    MapCall(
                        expression = ArithmeticExpression(
                            firstArgument = ArithmeticExpression(
                                firstArgument = Element,
                                operation = ArithmeticOperation.Plus,
                                secondArgument = ConstExpression(value = 5)
                            ),
                            operation = ArithmeticOperation.Multiply,
                            secondArgument = ConstExpression(value = 3)
                        )
                    )
                )
            ),
            chain
        )
    }
}