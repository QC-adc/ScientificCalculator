package com.calc.scientific.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calc.scientific.viewmodel.CalculatorViewModel

// 颜色
private val DarkBg = Color(0xFF1A1A2E)
private val DarkSurface = Color(0xFF16213E)
private val DarkCard = Color(0xFF0F3460)
private val Accent = Color(0xFFE94560)
private val AccentPurple = Color(0xFF7B2CBF)
private val SciBlue = Color(0xFF4CC9F0)
private val NumBtn = Color(0xFF2D2D44)
private val OpBtn = Color(0xFF3D3D5C)
private val EqBtn = Color(0xFFE94560)
private val FuncBtn = Color(0xFF1E3A5F)
private val DisplayBg = Color(0xFF0D0D1A)

@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel = viewModel()) {
    var showHistory by remember { mutableStateOf(false) }
    Surface(modifier = Modifier.fillMaxSize(), color = DarkBg) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            // 顶栏
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (showHistory) "历史记录" else "科学计算器", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = { showHistory = !showHistory }) {
                    Text(if (showHistory) "返回" else "历史", color = SciBlue, fontSize = 14.sp)
                }
            }

            if (showHistory) {
                HistoryPanel(viewModel.history, onSelect = { viewModel.selectHistoryItem(it); showHistory = false }, onClear = { viewModel.clearAll() })
            } else {
                // 显示区域
                Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(DisplayBg).padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { viewModel.toggleAngleMode() }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                            Text(if (viewModel.isDegreeMode) "DEG" else "RAD", color = SciBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        if (viewModel.error != null) Text(viewModel.error!!, color = Accent, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(viewModel.display, color = if (viewModel.error != null) Accent else Color.White,
                        fontSize = if (viewModel.display.length > 20) 26.sp else 34.sp,
                        fontWeight = FontWeight.Light, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth(),
                        maxLines = 3, overflow = TextOverflow.Ellipsis)
                }

                Spacer(Modifier.height(8.dp))
                // 科学函数按钮
                Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DarkSurface).padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SciBtn("sin", Modifier.weight(1f)) { viewModel.appendFunction("sin") }
                        SciBtn("cos", Modifier.weight(1f)) { viewModel.appendFunction("cos") }
                        SciBtn("tan", Modifier.weight(1f)) { viewModel.appendFunction("tan") }
                        SciBtn("(", Modifier.weight(1f)) { viewModel.append("(") }
                        SciBtn(")", Modifier.weight(1f)) { viewModel.append(")") }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SciBtn("asin", Modifier.weight(1f)) { viewModel.appendFunction("asin") }
                        SciBtn("acos", Modifier.weight(1f)) { viewModel.appendFunction("acos") }
                        SciBtn("atan", Modifier.weight(1f)) { viewModel.appendFunction("atan") }
                        SciBtn("xⁿ", Modifier.weight(1f)) { viewModel.append("^") }
                        SciBtn("n!", Modifier.weight(1f)) { viewModel.append("!") }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SciBtn("ln", Modifier.weight(1f)) { viewModel.appendFunction("ln") }
                        SciBtn("log", Modifier.weight(1f)) { viewModel.appendFunction("log") }
                        SciBtn("√", Modifier.weight(1f)) { viewModel.appendFunction("sqrt") }
                        ConstBtn("π", Modifier.weight(1f)) { viewModel.appendConstant("π") }
                        ConstBtn("e", Modifier.weight(1f)) { viewModel.appendConstant("e") }
                    }
                }

                Spacer(Modifier.height(8.dp))
                // 数字键盘
                Numpad(viewModel)
            }
        }
    }
}

@Composable
private fun SciBtn(label: String, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.height(42.dp).clip(RoundedCornerShape(8.dp)).background(FuncBtn).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ConstBtn(label: String, modifier: Modifier, onClick: () -> Unit) {
    Box(modifier.height(42.dp).clip(RoundedCornerShape(8.dp)).background(DarkCard).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Numpad(vm: CalculatorViewModel) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 第1行: C ⌫ ( ) ÷
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FuncBtn("C") { vm.clear() }; FuncBtn("⌫") { vm.backspace() }
            FuncBtn("(") { vm.append("(") }; FuncBtn(")") { vm.append(")") }
            OpBtn("÷") { vm.append("÷") }
        }
        // 第2行: 7 8 9 ×
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DigBtn("7") { vm.append("7") }; DigBtn("8") { vm.append("8") }; DigBtn("9") { vm.append("9") }
            OpBtn("×") { vm.append("×") }
        }
        // 第3行: 4 5 6 -
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DigBtn("4") { vm.append("4") }; DigBtn("5") { vm.append("5") }; DigBtn("6") { vm.append("6") }
            OpBtn("-") { vm.append("-") }
        }
        // 第4行: 1 2 3 +
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DigBtn("1") { vm.append("1") }; DigBtn("2") { vm.append("2") }; DigBtn("3") { vm.append("3") }
            OpBtn("+") { vm.append("+") }
        }
        // 第5行: 0(宽) . =
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DigBtn("0", Modifier.weight(2f)) { vm.append("0") }
            DigBtn(".") { vm.append(".") }
            EqBtn("=") { vm.calculate() }
        }
    }
}

@Composable
private fun DigBtn(text: String, modifier: Modifier = Modifier.weight(1f), onClick: () -> Unit) {
    Button(onClick, modifier.height(56.dp), RoundedCornerShape(12.dp), ButtonDefaults.buttonColors(containerColor = NumBtn), contentPadding = PaddingValues(0.dp)) {
        Text(text, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Normal)
    }
}

@Composable
private fun OpBtn(text: String, modifier: Modifier = Modifier.weight(1f), onClick: () -> Unit) {
    Button(onClick, modifier.height(56.dp), RoundedCornerShape(12.dp), ButtonDefaults.buttonColors(containerColor = OpBtn), contentPadding = PaddingValues(0.dp)) {
        Text(text, color = SciBlue, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EqBtn(text: String, modifier: Modifier = Modifier.weight(1f), onClick: () -> Unit) {
    Button(onClick, modifier.height(56.dp), RoundedCornerShape(12.dp), ButtonDefaults.buttonColors(containerColor = EqBtn), contentPadding = PaddingValues(0.dp)) {
        Text(text, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FuncBtn(text: String, modifier: Modifier = Modifier.weight(1f), onClick: () -> Unit) {
    Button(onClick, modifier.height(56.dp), RoundedCornerShape(12.dp), ButtonDefaults.buttonColors(containerColor = DarkCard.copy(alpha = 0.8f)), contentPadding = PaddingValues(0.dp)) {
        Text(text, color = Color.White.copy(alpha = 0.8f), fontSize = 18.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun HistoryPanel(history: List<String>, onSelect: (String) -> Unit, onClear: () -> Unit) {
    Column(Modifier.fillMaxWidth().weight(1f)) {
        if (history.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("暂无计算记录", color = Color.Gray, fontSize = 16.sp)
            }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onClear) { Text("清空全部", color = Accent, fontSize = 13.sp) }
            }
            LazyColumn(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(history) { item ->
                    Card(Modifier.fillMaxWidth().clickable { onSelect(item) }, RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkSurface)) {
                        Text(item, color = Color.White.copy(alpha = 0.85f), fontSize = 15.sp, modifier = Modifier.padding(14.dp), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
