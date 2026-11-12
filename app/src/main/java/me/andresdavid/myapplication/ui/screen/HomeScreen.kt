package me.andresdavid.myapplication.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.andresdavid.myapplication.data.DailyLogItem
import me.andresdavid.myapplication.data.FirebaseRepository
import me.andresdavid.myapplication.data.UserProfile
import me.andresdavid.myapplication.ui.theme.SelunaProyectoTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen(
    onNavigateToTab: (Int) -> Unit = {},
    onNavigateToDailyLog: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    var selectedBottomNav by remember { mutableIntStateOf(0) }
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var todayLog by remember { mutableStateOf<DailyLogItem?>(null) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var aiTipText by remember { mutableStateOf<String?>(null) }
    var isAiLoading by remember { mutableStateOf(true) }
    var userQuestion by remember { mutableStateOf("") }
    var isAskingGemini by remember { mutableStateOf(false) }
    var geminiAnswer by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isAiLoading = true
        val profile = FirebaseRepository.getUserProfile()
        userProfile = profile
        val logs = FirebaseRepository.getDailyLogs()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        todayLog = logs.find { it.date == todayStr } ?: logs.maxByOrNull { it.timestamp }

        val nameStr = if (!profile?.name.isNullOrBlank()) profile!!.name else FirebaseRepository.currentUserName
        val conds = profile?.conditions?.filter { it != "Ninguna de las anteriores" && it.isNotBlank() } ?: emptyList()
        val generatedTip = me.andresdavid.myapplication.data.GeminiService.generateCycleAdvice(
            userName = nameStr,
            phase = profile?.approach ?: "TRACK_CYCLE",
            symptoms = todayLog?.symptoms ?: emptyList(),
            moods = todayLog?.moods ?: emptyList(),
            conditions = conds
        )
        aiTipText = generatedTip
        isAiLoading = false
    }

    // Dynamic date string
    val calendar = Calendar.getInstance()
    val dayOfWeekStr = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "LUNES"
        Calendar.TUESDAY -> "MARTES"
        Calendar.WEDNESDAY -> "MIÉRCOLES"
        Calendar.THURSDAY -> "JUEVES"
        Calendar.FRIDAY -> "VIERNES"
        Calendar.SATURDAY -> "SÁBADO"
        Calendar.SUNDAY -> "DOMINGO"
        else -> ""
    }
    val monthStr = when (calendar.get(Calendar.MONTH)) {
        Calendar.JANUARY -> "ENERO"
        Calendar.FEBRUARY -> "FEBRERO"
        Calendar.MARCH -> "MARZO"
        Calendar.APRIL -> "ABRIL"
        Calendar.MAY -> "MAYO"
        Calendar.JUNE -> "JUNIO"
        Calendar.JULY -> "JULIO"
        Calendar.AUGUST -> "AGOSTO"
        Calendar.SEPTEMBER -> "SEPTIEMBRE"
        Calendar.OCTOBER -> "OCTUBRE"
        Calendar.NOVEMBER -> "NOVIEMBRE"
        Calendar.DECEMBER -> "DICIEMBRE"
        else -> ""
    }
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    val formattedDate = "$dayOfWeekStr, $dayOfMonth DE $monthStr"

    // Profile formatted labels
    val userName = if (!userProfile?.name.isNullOrBlank()) userProfile!!.name else FirebaseRepository.currentUserName

    val approachLabel = when (userProfile?.approach) {
        "CONCEIVE" -> "Buscando concebir"
        "PREVENT_PREGNANCY" -> "Prevenir embarazo"
        "TRACK_CYCLE" -> "Conocer y registrar mi ciclo"
        else -> "Perfil Ginecológico"
    }

    val regularityLabel = when (userProfile?.regularity) {
        "REGULAR" -> "Ciclo Regular"
        "VARIES" -> "Ciclo Variable"
        "NOT_SURE" -> "En evaluación"
        else -> "Ciclo Regular"
    }

    val contraceptiveLabel = when (userProfile?.contraceptive) {
        "PILLS" -> "Pastillas"
        "IUD" -> "DIU"
        "IMPLANT" -> "Implante"
        "NONE" -> "Sin anticonceptivo"
        else -> "Natural"
    }

    val periodDaysCount = if ((userProfile?.periodDays ?: 0) > 0) userProfile!!.periodDays else 5

    val userConditions = userProfile?.conditions?.filter {
        it != "Ninguna de las anteriores" && it.isNotBlank()
    } ?: emptyList()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedBottomNav == 0,
                    onClick = {
                        selectedBottomNav = 0
                        onNavigateToTab(0)
                    },
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
                    onClick = { onNavigateToTab(1) },
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
                // Top App Bar: Brand + Avatar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
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

                    // Avatar Circle (Clickable for Sign Out)
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
                            androidx.compose.material3.Button(
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

                // Greeting & Notification Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = formattedDate,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8C7F84),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "¡Hola, $userName! 🌸",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF33262A)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (userProfile?.goal?.isNotBlank() == true) "Objetivo: ${userProfile!!.goal}" else "Escucha a tu cuerpo en su ritmo natural.",
                            fontSize = 13.sp,
                            color = Color(0xFF8C7F84)
                        )
                    }

                    // Notification Bell Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF0F3)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notificaciones",
                            tint = Color(0xFF33262A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Main Cycle Widget Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                        .shadow(2.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top Badge: Dynamic Approach
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFF0F3)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF33262A))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = approachLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF6E565E)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Circular Cycle Progress Ring
                        Box(
                            modifier = Modifier.size(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 14.dp.toPx()
                                val diameter = size.minDimension - strokeWidth
                                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                                val arcSize = Size(diameter, diameter)

                                // Segment 1: Menstruation Phase
                                drawArc(
                                    color = Color(0xFFF7C2D2),
                                    startAngle = -45f,
                                    sweepAngle = 75f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )

                                // Segment 2: Follicular Phase
                                drawArc(
                                    color = Color(0xFFD3ECE1),
                                    startAngle = 35f,
                                    sweepAngle = 140f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )

                                // Segment 3: Luteal Phase
                                drawArc(
                                    color = Color(0xFFE2EDF8),
                                    startAngle = 180f,
                                    sweepAngle = 130f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )

                                // Indicator Dot
                                val radius = diameter / 2
                                val center = Offset(size.width / 2, size.height / 2)
                                val angleRad = Math.toRadians(180.0)
                                val dotX = center.x + radius * cos(angleRad).toFloat()
                                val dotY = center.y + radius * sin(angleRad).toFloat()

                                drawCircle(
                                    color = Color(0xFF33262A),
                                    radius = 8.dp.toPx(),
                                    center = Offset(dotX, dotY)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 3.dp.toPx(),
                                    center = Offset(dotX, dotY)
                                )
                            }

                            // Center Text inside Progress Ring
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "PERIODO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF8C7F84),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$periodDaysCount días",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF33262A)
                                )
                                Text(
                                    text = "de duracion",
                                    fontSize = 12.sp,
                                    color = Color(0xFF8C7F84)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Bottom Row: 2 Summary Pill Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Left Badge: Regularidad
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFFFAF0F3)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = null,
                                        tint = Color(0xFF8C7F84),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Regularidad",
                                            fontSize = 11.sp,
                                            color = Color(0xFF8C7F84)
                                        )
                                        Text(
                                            text = regularityLabel,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF33262A)
                                        )
                                    }
                                }
                            }

                            // Right Badge: Anticonceptivo
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFFE2F0EC)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Shield,
                                        contentDescription = null,
                                        tint = Color(0xFF2E594B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Método",
                                            fontSize = 11.sp,
                                            color = Color(0xFF386657)
                                        )
                                        Text(
                                            text = contraceptiveLabel,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E594B)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Medical Conditions Card (Only if user has entered conditions)
                if (userConditions.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 18.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFAF0F3)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.MedicalServices,
                                    contentDescription = null,
                                    tint = Color(0xFF6B535E),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Diagnósticos registrados",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF33262A)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = userConditions.joinToString(", "),
                                    fontSize = 12.sp,
                                    color = Color(0xFF6E565E)
                                )
                            }
                        }
                    }
                }

                // "¿Cómo te sientes hoy?" Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿Cómo te sientes hoy?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF33262A)
                    )
                    Text(
                        text = "Registrar +",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF805C6B),
                        modifier = Modifier.clickable { onNavigateToDailyLog() }
                    )
                }

                // 4 Quick Logging Cards Row (Shows REAL log values if entered, otherwise "Sin registro")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Card 1: Síntomas
                    val symptomsText = if (todayLog?.symptoms?.isNotEmpty() == true) {
                        "${todayLog!!.symptoms.size} registrados"
                    } else {
                        "Sin registro"
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clickable { onNavigateToDailyLog() },
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF0F3FC)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ShowChart,
                                    contentDescription = null,
                                    tint = Color(0xFF4A68A5),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Síntomas",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF33262A),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = symptomsText,
                                fontSize = 10.sp,
                                color = Color(0xFF8C7F84),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Card 2: Ánimo
                    val moodText = if (todayLog?.moods?.isNotEmpty() == true) {
                        todayLog!!.moods.first()
                    } else {
                        "Sin registro"
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clickable { onNavigateToDailyLog() },
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFFF0F3)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.SentimentSatisfied,
                                    contentDescription = null,
                                    tint = Color(0xFF805C6B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Ánimo",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF33262A),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = moodText,
                                fontSize = 10.sp,
                                color = Color(0xFF8C7F84),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Card 3: Flujo
                    val flowText = if (todayLog?.flowLevel?.isNotBlank() == true) {
                        todayLog!!.flowLevel
                    } else {
                        "Sin registro"
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clickable { onNavigateToDailyLog() },
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFFEAEE)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.WaterDrop,
                                    contentDescription = null,
                                    tint = Color(0xFF33262A),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Flujo",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF33262A),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = flowText,
                                fontSize = 10.sp,
                                color = Color(0xFF8C7F84),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Card 4: Medicación
                    val medStatusText = when {
                        todayLog?.medicationTaken == true -> "Tomada"
                        userProfile?.contraceptive != null && userProfile?.contraceptive != "NONE" -> "Pendiente"
                        else -> "Sin registro"
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clickable { onNavigateToDailyLog() },
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFE5F5F0)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.LocalCafe,
                                    contentDescription = null,
                                    tint = Color(0xFF2E594B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Medicación",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF33262A),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = medStatusText,
                                fontSize = 10.sp,
                                color = Color(0xFF2E594B),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // GEMINI AI Card in HomeScreen
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFFE2F0EC)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF2E594B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "GEMINI AI • CONSEJO DE SELUNA",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E594B),
                                        letterSpacing = 0.5.sp
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF2E594B)
                                    ) {
                                        Text(
                                            text = "Gemini 1.5",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Asistente de bienestar para $userName",
                                    fontSize = 10.sp,
                                    color = Color(0xFF386657)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isAiLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color(0xFF2E594B),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Gemini AI está analizando tu ciclo...",
                                    fontSize = 12.sp,
                                    color = Color(0xFF2E594B)
                                )
                            }
                        } else {
                            val defaultTipText = when {
                                userConditions.isNotEmpty() ->
                                    "Para tus condiciones de ${userConditions.joinToString()}: Prioriza hidratación constante, descanso restaurativo y actividades suaves."
                                userProfile?.approach == "CONCEIVE" ->
                                    "Tu enfoque es concebir: Registra diariamente tus sensaciones y monitorea tus ventanas de mayor fertilidad."
                                userProfile?.approach == "PREVENT_PREGNANCY" ->
                                    "Tu enfoque es prevenir embarazo: Revisa tus días clave y mantén al día tus registros de bienestar."
                                else ->
                                    "Sigue escuchando a tu cuerpo. Completa tus registros diarios para calibrar predicciones precisas con Seluna."
                            }

                            Text(
                                text = aiTipText ?: defaultTipText,
                                fontSize = 12.sp,
                                color = Color(0xFF2E594B),
                                lineHeight = 17.sp
                            )
                        }

                        // Response to custom user query
                        geminiAnswer?.let { answer ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Respuesta de Gemini AI:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E594B)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = answer,
                                        fontSize = 12.sp,
                                        color = Color(0xFF33262A),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive query input for Gemini
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = userQuestion,
                                onValueChange = { userQuestion = it },
                                placeholder = {
                                    Text(
                                        "Pregúntale algo a Gemini AI...",
                                        fontSize = 12.sp,
                                        color = Color(0xFF6E8079)
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    disabledContainerColor = Color.White,
                                    focusedBorderColor = Color(0xFF2E594B),
                                    unfocusedBorderColor = Color.Transparent
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable(enabled = userQuestion.isNotBlank() && !isAskingGemini) {
                                        val query = userQuestion.trim()
                                        if (query.isNotBlank()) {
                                            isAskingGemini = true
                                            coroutineScope.launch {
                                                val resp = me.andresdavid.myapplication.data.GeminiService.askGemini(query)
                                                geminiAnswer = resp
                                                isAskingGemini = false
                                                userQuestion = ""
                                            }
                                        }
                                    },
                                color = if (userQuestion.isNotBlank() && !isAskingGemini) Color(0xFF2E594B) else Color(0xFFB2C8C0)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isAskingGemini) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "Enviar a Gemini",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // "Esta semana" Section Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Esta semana",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF33262A)
                    )
                    Text(
                        text = "Periodo de $periodDaysCount días",
                        fontSize = 12.sp,
                        color = Color(0xFF8C7F84)
                    )
                }

                // 7 Days Circles Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
                    val days = listOf(
                        Triple("S", "${(currentDay - 2).coerceAtLeast(1)}", Color(0xFFFFF0F3)),
                        Triple("D", "${(currentDay - 1).coerceAtLeast(1)}", Color(0xFFFFF0F3)),
                        Triple("L", "$currentDay", Color(0xFF33262A)), // Today
                        Triple("M", "${currentDay + 1}", Color(0xFFE2F0EC)),
                        Triple("M", "${currentDay + 2}", Color(0xFFE2F0EC)),
                        Triple("J", "${currentDay + 3}", Color(0xFFFFF0F3)),
                        Triple("V", "${currentDay + 4}", Color(0xFFE2EDF8))
                    )

                    days.forEach { (dayLetter, dayNum, bgColor) ->
                        val isToday = dayNum == "$currentDay"
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = dayLetter,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF8C7F84)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isToday) Color(0xFF33262A) else bgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNum,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isToday) Color.White else Color(0xFF33262A)
                                )
                            }
                        }
                    }
                }

                // Primary Action Button: + Registrar Síntomas y Notas
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
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF33262A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Registrar Síntomas y Notas",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF33262A)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SelunaProyectoTheme {
        HomeScreen()
    }
}
