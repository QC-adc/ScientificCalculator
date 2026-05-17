package com.calc.scientific.engine

import kotlin.math.*

object CalculatorEngine {

    private val operators = setOf("+", "-", "×", "÷", "^", "!")
    private val functions = mapOf(
        "sin" to 1, "cos" to 1, "tan" to 1,
        "asin" to 1, "acos" to 1, "atan" to 1,
        "sinh" to 1, "cosh" to 1, "tanh" to 1,
        "log" to 1, "ln" to 1,
        "sqrt" to 1, "cbrt" to 1,
        "abs" to 1, "floor" to 1, "ceil" to 1,
        "exp" to 1
    )
    private val constants = mapOf(
        "π" to PI, "pi" to PI,
        "e" to E
    )

    data class CalcResult(
        val display: String,
        val error: String? = null
    )

    fun evaluate(expression: String, degreeMode: Boolean = true): CalcResult {
        try {
            val prepared = prepare(expression) ?: return CalcResult("", null)
            val tokens = tokenize(prepared)
            val postfix = shuntingYard(tokens)
            val result = evalPostfix(postfix, degreeMode)
            return CalcResult(formatResult(result))
        } catch (e: Exception) {
            return CalcResult(expression, "语法错误")
        }
    }

    private fun prepare(expr: String): String? {
        if (expr.isBlank()) return null
        var s = expr.trim()
        s = s.replace("×", "*").replace("÷", "/").replace("−", "-")
        if (s.startsWith("+")) s = s.drop(1)
        s = s.replace(Regex("(\\d)([π(e])")) { "${it.groupValues[1]}*${it.groupValues[2]}" }
        s = s.replace(Regex("([πe)])(\\d)")) { "${it.groupValues[1]}*${it.groupValues[2]}" }
        s = s.replace(Regex("([πe)])([π(e])")) { "${it.groupValues[1]}*${it.groupValues[2]}" }
        if (s.startsWith("-")) s = "~" + s.drop(1)
        s = s.replace(Regex("[(,](-)")) { it.groupValues[0].replace("-", "~") }
        return s
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < expr.length) {
            val c = expr[i]
            when {
                c.isDigit() || c == '.' -> {
                    var num = ""
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                        num += expr[i]; i++
                    }
                    tokens.add(num)
                }
                c == '~' -> {
                    var num = "~"; i++
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                        num += expr[i]; i++
                    }
                    tokens.add(num)
                }
                c in "+-*/^()" || c == ',' -> { tokens.add(c.toString()); i++ }
                c == '!' -> { tokens.add("!"); i++ }
                c.isLetter() -> {
                    var name = ""
                    while (i < expr.length && (expr[i].isLetter() || expr[i] == 'π')) {
                        name += expr[i]; i++
                    }
                    if (name in constants) tokens.add(constants[name]!!.toString())
                    else tokens.add(name)
                }
                c.isWhitespace() -> i++
                else -> i++
            }
        }
        return tokens
    }

    private fun shuntingYard(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = mutableListOf<String>()
        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> output.add(token)
                token in functions -> stack.add(token)
                token == "," -> {
                    while (stack.isNotEmpty() && stack.last() != "(")
                        output.add(stack.removeLast())
                }
                token in operators -> {
                    val prec1 = precedence(token)
                    val leftAssoc = leftAssociative(token)
                    while (stack.isNotEmpty()) {
                        val top = stack.last()
                        if (top == "(" || top in functions) break
                        val prec2 = precedence(top)
                        if (prec2 > prec1 || (prec2 == prec1 && leftAssoc))
                            output.add(stack.removeLast())
                        else break
                    }
                    stack.add(token)
                }
                token == "(" -> stack.add(token)
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.last() != "(")
                        output.add(stack.removeLast())
                    stack.removeLastOrNull()
                    if (stack.isNotEmpty() && stack.last() in functions)
                        output.add(stack.removeLast())
                }
            }
        }
        while (stack.isNotEmpty()) output.add(stack.removeLast())
        return output
    }

    private fun evalPostfix(postfix: List<String>, degreeMode: Boolean): Double {
        val stack = mutableListOf<Double>()
        for (token in postfix) {
            when {
                token.toDoubleOrNull() != null -> stack.add(token.toDouble())
                token == "~" -> { val a = stack.removeLastOrNull() ?: error(""); stack.add(-a) }
                token == "!" -> { val a = stack.removeLastOrNull() ?: error(""); stack.add(factorial(a)) }
                token in operators -> {
                    if (token == "~") { val a = stack.removeLastOrNull() ?: error(""); stack.add(-a); continue }
                    val b = stack.removeLastOrNull() ?: error("")
                    val a = if (stack.isNotEmpty() && token != "^") stack.removeLastOrNull() else null
                    stack.add(applyOp(token, a ?: 0.0, b))
                }
                token in functions -> {
                    val a = stack.removeLastOrNull() ?: error("")
                    stack.add(applyFunc(token, a, degreeMode))
                }
            }
        }
        return stack.lastOrNull() ?: 0.0
    }

    private fun precedence(op: String) = when (op) {
        "+", "-" -> 2; "×", "*", "÷", "/" -> 3; "~" -> 4; "^" -> 5; "!" -> 6
        else -> 0
    }

    private fun leftAssociative(op: String) = op != "^" && op != "~"

    private fun applyOp(op: String, a: Double, b: Double): Double = when (op) {
        "+" -> a + b; "-" -> a - b
        "×", "*" -> a * b; "÷", "/" -> if (b == 0.0) error("") else a / b
        "^" -> a.pow(b); "!" -> factorial(a)
        else -> error("")
    }

    private fun applyFunc(name: String, a: Double, degreeMode: Boolean): Double {
        val trigFuncs = listOf("sin", "cos", "tan")
        val arg = if (degreeMode && name in trigFuncs) Math.toRadians(a) else a
        return when (name) {
            "sin" -> sin(arg); "cos" -> cos(arg); "tan" -> tan(arg)
            "asin" -> if (degreeMode) Math.toDegrees(asin(a)) else asin(a)
            "acos" -> if (degreeMode) Math.toDegrees(acos(a)) else acos(a)
            "atan" -> if (degreeMode) Math.toDegrees(atan(a)) else atan(a)
            "sinh" -> sinh(a); "cosh" -> cosh(a); "tanh" -> tanh(a)
            "ln" -> if (a <= 0) error("") else ln(a)
            "log" -> if (a <= 0) error("") else log10(a)
            "sqrt" -> if (a < 0) error("") else sqrt(a); "cbrt" -> cbrt(a)
            "abs" -> abs(a); "floor" -> floor(a); "ceil" -> ceil(a)
            "exp" -> exp(a)
            else -> error("")
        }
    }

    private fun factorial(n: Double): Double {
        if (n < 0 || n != floor(n)) error("")
        val k = n.toInt()
        return (1..maxOf(k, 1)).fold(1.0) { acc, i -> acc * i }
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN()) return "未定义"
        if (value.isInfinite()) return "无穷大"
        return if (value == floor(value) && !value.isInfinite()) {
            value.toLong().toString()
        } else {
            val str = String.format("%.10f", value).trimEnd('0').trimEnd('.')
            if (str.length > 15) String.format("%.6e", value) else str
        }
    }
}
