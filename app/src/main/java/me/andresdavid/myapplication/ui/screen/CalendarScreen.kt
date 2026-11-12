package me.andresdavid.myapplication.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.andresdavid.myapplication.data.DailyLogItem
import me.andresdavid.myapplication.data.FirebaseRepository
import me.andresdavid.myapplication.data.UserProfile
import me.andresdavid.myapplication.ui.theme.SelunaProyectoTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarScreen(
    onNavigateToTab: (Int) -> Unit = {},
    onNavigateToDailyLog: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    var selectedBottomNav by remember { mutableIntStateOf(1) }
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var dailyLogs by remember { mutableStateOf<List<DailyLogItem>>(emptyList()) }
    var selectedDayNumber by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    val currentCal = Calendar.getInstance()
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
    val currentMonthTitle = monthFormat.format(currentCal.time).replaceFirstChar { it.uppercase() }

    LaunchedEffect(Unit) {
        userProfile = FirebaseRepository.getUserProfile()
        dailyLogs = FirebaseRepository.getDailyLogs()
    }

    // Find log for the selected day in current month
    val currentYearMonthPrefix = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(currentCal.time)
    val selectedDateStr = String.format(Locale.getDefault(), "%s-%02d", currentYearMonthPrefix, selectedDayNumber)
    val selectedDayLog = dailyLogs.find { it.date == selectedDateStr }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedBottomNav == 0,
                    onClick = { onNavigateToTab(0) },
                    icon = { Icon(Icons.Outlined.AccessTime, contentDescription = "Hoy") },
                    label = { Text("Hoy", fontSize = 11.sp, fontWeight = if (selectedBottomNav == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF33262A),
                        selectedTextColor = Color(0xFF33262A),
                        indicatorColor = Color(0xFFFFF0F3),
                        unselectedIconColor = Color(0xFF9E8F95),
                        unselectedTextColor = Color(0xFF9E8F95)
                    )
                )
                NavigationBarItem(
                    selected = selectedBottomNav == 1,
                    onClick = {
                        selectedBottomNav = 1
                        onNavigateToTab(1)
                    },
                    icon = { Icon(Icons.Outlined.CalendarToday, contentDescription = "Calendario") },
                    label = { Text("Calendario", fontSize = 11.sp, fontWeight = if (selectedBottomNav == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF33262A),
                        selectedTextColor = Color(0xFF33262A),
                        indicatorColor = Color(0xFFFFF0F3),
                        unselectedIconColor = Color(0xFF9E8F95),
                        unselectedTextColor = Color(0xFF9E8F95)
                    )
                )
                NavigationBarItem(
                    selected = selectedBottomNav == 2,
                    onClick = { onNavigateToTab(2) },
                    icon = { Icon(Icons.Outlined.Insights, contentDescription = "Estadísticas") },
                    label = { Text("Estadísticas", fontSize = 11.sp, fontWeight = if (selectedBottomNav == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF33262A),
                        selectedTextColor = Color(0xFF33262A),
                        indicatorColor = Color(0xFFFFF0F3),
                        unselectedIconColor = Color(0xFF9E8F95),
                        unselectedTextColor = Color(0xFF9E8F95)
                    )
                )
                NavigationBarItem(
                    selected = selectedBottomNav == 3,
                    onClick = { onNavigateToTab(3) },
                    icon = { Icon(Icons.Outlined.EditNote, contentDescription = "Registro") },
                    label = { Text("Registro", fontSize = 11.sp, fontWeight = if (selectedBottomNav == 3) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF33262A),
                        selectedTextColor = Color(0xFF33262A),
                        indicatorColor = Color(0xFFFFF0F3),
                        unselectedIconColor = Color(0xFF9E8F95),
                        unselectedTextColor = Color(0xFF9E8F95)
                    )
                )
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding(),
            color = Color(0xFFFDF8F9)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(40.dp))

                    Text(
                        text = "Seluna",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF33262A)
                    )

                    // Profile Avatar Circle (Clickable for Sign Out)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8C2A0))
                            .clickable { showSignOutDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(24.dp)) {
                            drawCircle(color = Color(0xFF6B535E), radius = 8f, center = Offset(size.width * 0.5f, size.height * 0.35f))
                            drawCircle(color = Color(0xFF6B535E), radius = 14f, center = Offset(size.width * 0.5f, size.height * 0.9f))
                        }
                    }
                }

                // Sign Out Dialog
                if (showSignOutDialog) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showSignOutDialog = false },
                        title = {
                            Text(
                                text = "Cerrar sesión",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF33262A)
                            )
                        },
                        text = {
                            Text(
                                text = "¿Estás segura de que deseas cerrar sesión en Seluna?",
                                fontSize = 14.sp,
                                color = Color(0xFF6E6065)
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showSignOutDialog = false
                                    FirebaseRepository.signOut()
                                    onSignOut()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF805C6B))
                            ) {
                                Text("Cerrar Sesión", color = Color.White, fontSize = 13.sp)
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(onClick = { showSignOutDialog = false }) {
                                Text("Cancelar", color = Color(0xFF8C7F84), fontSize = 13.sp)
                            }
                        },
                        containerColor = Color.White,
                        shape = RoundedCornerShape(24.dp)
                    )
                }

                // Month Selector Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF0F3)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Mes anterior",
                            tint = Color(0xFF33262A),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentMonthTitle,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF33262A)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE594AD))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val totalLogs = dailyLogs.size
                            Text(
                                text = "Registros guardados: $totalLogs",
                                fontSize = 12.sp,
                                color = Color(0xFF8C7F84)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF0F3)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Mes siguiente",
                            tint = Color(0xFF33262A),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Interactive Month Calendar Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .shadow(2.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Weekday Headers (L, M, M, J, V, S, D)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("L", "M", "M", "J", "V", "S", "D").forEach { day ->
                                Text(
                                    text = day,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF8C7F84),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.width(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Dynamic Month Grid (30/31 days)
                        val daysInMonth = currentCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                        val todayNum = currentCal.get(Calendar.DAY_OF_MONTH)
                        val periodDays = userProfile?.periodDays ?: 5

                        // Group days into weeks of 7
                        val cells = (1..daysInMonth).map { dayNum ->
                            val dateStr = String.format(Locale.getDefault(), "%s-%02d", currentYearMonthPrefix, dayNum)
                            val log = dailyLogs.find { it.date == dateStr }
                            val hasFlow = log != null && log.flowLevel.isNotBlank() && log.flowLevel != "Ninguno"
                            val hasData = log != null && (log.symptoms.isNotEmpty() || log.moods.isNotEmpty() || log.notes.isNotBlank() || log.medicationTaken)

                            val type = when {
                                hasFlow -> CellType.MENSTRUATION
                                dayNum <= periodDays && dailyLogs.isEmpty() -> CellType.MENSTRUATION
                                dayNum == todayNum -> CellType.TODAY
                                dayNum == 14 -> CellType.OVULATION
                                dayNum in 12..16 -> CellType.FERTILE
                                else -> CellType.REGULAR
                            }
                            CalendarCell(
                                dayNumber = dayNum,
                                isCurrentMonth = true,
                                type = type,
                                hasData = hasData,
                                isSelected = dayNum == selectedDayNumber
                            )
                        }

                        val rows = cells.chunked(7)
                        rows.forEach { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                row.forEach { cell ->
                                    CalendarDayView(
                                        cell = cell,
                                        onClick = { selectedDayNumber = cell.dayNumber }
                                    )
                                }
                                // Fill remaining slots in last row if needed
                                if (row.size < 7) {
                                    repeat(7 - row.size) {
                                        Spacer(modifier = Modifier.width(36.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Modal Dialog for Full Detail View
                if (showDetailDialog && selectedDayLog != null) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showDetailDialog = false },
                        title = {
                            Text(
                                text = "Registro del día $selectedDayNumber",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF33262A)
                            )
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("📅 Fecha: ${selectedDayLog.date}", fontSize = 13.sp, color = Color(0xFF6E6065))
                                if (selectedDayLog.flowLevel.isNotBlank()) {
                                    Text("🩸 Flujo: ${selectedDayLog.flowLevel}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF33262A))
                                }
                                if (selectedDayLog.symptoms.isNotEmpty()) {
                                    Text("🌷 Síntomas: ${selectedDayLog.symptoms.joinToString(", ")}", fontSize = 13.sp, color = Color(0xFF33262A))
                                }
                                if (selectedDayLog.moods.isNotEmpty()) {
                                    Text("😌 Ánimo: ${selectedDayLog.moods.joinToString(", ")}", fontSize = 13.sp, color = Color(0xFF33262A))
                                }
                                Text("💊 Medicación tomada: ${if (selectedDayLog.medicationTaken) "Sí" else "No"}", fontSize = 13.sp, color = Color(0xFF33262A))
                                if (selectedDayLog.notes.isNotBlank()) {
                                    Text("📝 Notas: ${selectedDayLog.notes}", fontSize = 13.sp, color = Color(0xFF6E6065))
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showDetailDialog = false
                                    onNavigateToDailyLog()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33262A))
                            ) {
                                Text("Editar en Registro", fontSize = 13.sp)
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(onClick = { showDetailDialog = false }) {
                                Text("Cerrar", color = Color(0xFF8C7F84), fontSize = 13.sp)
                            }
                        },
                        containerColor = Color.White,
                        shape = RoundedCornerShape(24.dp)
                    )
                }

                // Selected Day Details Card (Persisted Log Details)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable {
                            if (selectedDayLog != null) {
                                showDetailDialog = true
                            } else {
                                onNavigateToDailyLog()
                            }
                        },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F3))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DETALLE DEL DÍA $selectedDayNumber",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8C7F84),
                                letterSpacing = 0.5.sp
                            )
                            if (selectedDayLog != null) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFE2F0EC)
                                ) {
                                    Text(
                                        text = "✓ Registrado",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E594B),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (selectedDayLog != null) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (selectedDayLog.flowLevel.isNotBlank()) {
                                    Text(
                                        text = "🩸 Flujo: ${selectedDayLog.flowLevel}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF33262A)
                                    )
                                }
                                if (selectedDayLog.symptoms.isNotEmpty()) {
                                    Text(
                                        text = "🌷 Síntomas: ${selectedDayLog.symptoms.joinToString(", ")}",
                                        fontSize = 13.sp,
                                        color = Color(0xFF33262A)
                                    )
                                }
                                if (selectedDayLog.moods.isNotEmpty()) {
                                    Text(
                                        text = "😌 Ánimo: ${selectedDayLog.moods.joinToString(", ")}",
                                        fontSize = 13.sp,
                                        color = Color(0xFF33262A)
                                    )
                                }
                                if (selectedDayLog.medicationTaken) {
                                    Text(
                                        text = "💊 Medicación: Tomada",
                                        fontSize = 13.sp,
                                        color = Color(0xFF2E594B),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (selectedDayLog.notes.isNotBlank()) {
                                    Text(
                                        text = "📝 Notas: ${selectedDayLog.notes}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF6E6065)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                        .clickable { showDetailDialog = true },
                                    shape = CircleShape,
                                    color = Color.White
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = "🔍 Ver información completa o editar",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF33262A)
                                        )
                                    }
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "No hay registros guardados para este día.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF8C7F84)
                                )

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                        .clickable { onNavigateToDailyLog() },
                                    shape = CircleShape,
                                    color = Color.White
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = "+ Registrar síntomas para este día",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF33262A)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // LEYENDA DEL CICLO Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "LEYENDA DEL CICLO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8C7F84),
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        // 2x2 Grid of Legend Pills
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = CircleShape,
                                    color = Color(0xFFFFF0F3)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFF7C2D2))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Menstruación / Flujo",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF33262A)
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = CircleShape,
                                    color = Color(0xFFFFF0F3)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFD3ECE1))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Días Fértiles",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF33262A)
                                        )
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = CircleShape,
                                    color = Color(0xFFFFF0F3)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE2EDF8))
                                                .border(1.dp, Color(0xFF4A3E45), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Ovulación",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF33262A)
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = CircleShape,
                                    color = Color(0xFFFFF0F3)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF33262A))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Hoy",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF33262A)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Primary Action Button: Editar periodo o registrar nuevo ciclo
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable { onNavigateToDailyLog() },
                    shape = CircleShape,
                    color = Color(0xFFFFF0F3)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EditCalendar,
                            contentDescription = null,
                            tint = Color(0xFF33262A),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Registrar síntomas para hoy",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF33262A)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tus registros se guardan y sincronizan automáticamente.",
                    fontSize = 11.sp,
                    color = Color(0xFF9E8F95),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}

enum class CellType {
    REGULAR,
    MENSTRUATION,
    FERTILE,
    OVULATION,
    PREDICTION,
    TODAY
}

data class CalendarCell(
    val dayNumber: Int,
    val isCurrentMonth: Boolean,
    val type: CellType = CellType.REGULAR,
    val hasData: Boolean = false,
    val isSelected: Boolean = false
)

@Composable
fun CalendarDayView(
    cell: CalendarCell,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (!cell.isCurrentMonth) {
            Text(
                text = cell.dayNumber.toString(),
                fontSize = 13.sp,
                color = Color(0xFFD0C4C8),
                textAlign = TextAlign.Center
            )
        } else {
            val bgShape = CircleShape
            val bgColor = when {
                cell.type == CellType.MENSTRUATION -> Color(0xFFF7C2D2)
                cell.type == CellType.FERTILE -> Color(0xFFD3ECE1)
                cell.type == CellType.OVULATION -> Color(0xFFE2EDF8)
                else -> Color.Transparent
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(bgShape)
                    .background(bgColor)
                    .border(
                        width = if (cell.isSelected) 2.dp else if (cell.type == CellType.TODAY) 1.5.dp else 0.dp,
                        color = if (cell.isSelected) Color(0xFF805C6B) else if (cell.type == CellType.TODAY) Color(0xFF33262A) else Color.Transparent,
                        shape = bgShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = cell.dayNumber.toString(),
                        fontSize = 13.sp,
                        fontWeight = if (cell.type == CellType.TODAY || cell.isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = Color(0xFF33262A)
                    )
                    if (cell.hasData) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF33262A))
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarScreenPreview() {
    SelunaProyectoTheme {
        CalendarScreen()
    }
}
