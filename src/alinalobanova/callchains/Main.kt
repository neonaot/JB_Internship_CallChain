package alinalobanova.callchains

import java.lang.StringBuilder
import java.util.*

fun main() {
    try {
        val scanner = Scanner(System.`in`)
        val input = scanner.next()
        val chain = parseCallChain(input)
        refactorCallChain(chain)
        val stringBuilder = StringBuilder()
        chain.print(stringBuilder)
        println(stringBuilder)
    } catch (e: SyntaxError){
        println("SYNTAX ERROR")
    } catch (e: TypeError){
        println("TYPE ERROR")
    }

}

