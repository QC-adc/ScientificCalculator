package com.calc.scientific.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.calc.scientific.engine.CalculatorEngine

class CalculatorViewModel : ViewModel() {

    var expression by mutableStateOf("")
        private set
    var display by mutableStateOf("0")
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var isDegreeMode by mutableStateOf(true)
        private set
    var history by mutableStateOf(listOf<String>())
        private set

    private val maxLen = 60

    fun append(value: String) {
        if (expression.length >= maxLen) return
        error = null
        if (display == "0" && value !in ".+-×÷^") expression = ""
        expression += value
        updateDisplay()
    }

    fun appendFunction(name: String) {
        if (expression.length >= maxLen - name.length) return
        error = null
        val last = expression.lastOrNull()
        if (last != null && (last.isDigit() || last == ')' || last == 'π' || last == 'e'))
            expression += "×"
        expression += "$name("
        updateDisplay()
    }

    fun appendConstant(name: String) {
        if (expression.length >= maxLen) return
        error = null
        val last = expression.lastOrNull()
        if (last != null && (last.isDigit() || last == ')' || last == 'π' || last == 'e'))
            expression += "×"
        expression += name
        updateDisplay()
    }

    fun backspace() {
        if (expression.isNotEmpty()) { error = null; expression = expression.dropLast(1); updateDisplay() }
    }

    fun clear() { expression = ""; display = "0"; error = null }

    fun clearAll() { clear(); history = emptyList() }

    fun calculate() {
        if (expression.isBlank()) return
        var expr = expression
        val open = expr.count { it == '(' }; val close = expr.count { it == ')' }
        if (open > close) expr += ")".repeat(open - close)
        val result = CalculatorEngine.evaluate(expr, isDegreeMode)
        if (result.error != null) { error = result.error }
        else {
            history = listOf("$expression = ${result.display}") + history.take(49)
            expression = result.display; display = result.display; error = null
        }
    }

    fun toggleAngleMode() { isDegreeMode = !isDegreeMode }

    fun selectHistoryItem(item: String) {
        val parts = item.split(" = ")
        if (parts.size == 2) { expression = parts[0]; updateDisplay(); error = null }
    }

    private fun updateDisplay() { display = if (expression.isEmpty()) "0" else expression }
}
