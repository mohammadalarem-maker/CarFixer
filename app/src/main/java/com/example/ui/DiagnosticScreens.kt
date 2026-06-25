package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.R
import com.example.data.DiagnosticMessage
import com.example.data.DiagnosticSession
import com.example.network.DiagnosticResult
import com.example.network.RetrofitClient
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AppNavigation(viewModel: DiagnosticViewModel) {
    // Force RTL layout direction for Arabic
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val currentSession by viewModel.currentSession.collectAsState()
        val isAILoading by viewModel.isAILoading.collectAsState()
        val allSessions by viewModel.allSessions.collectAsState()

        var showNewDialog by remember { mutableStateOf(false) }
        var initialCategory by remember { mutableStateOf("عام") }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (currentSession == null) {
                DashboardScreen(
                    allSessions = allSessions,
                    onStartDiagnosis = { category ->
                        initialCategory = category
                        showNewDialog = true
                    },
                    onSelectSession = { session ->
                        viewModel.selectSession(session.id)
                    },
                    onDeleteSession = { id ->
                        viewModel.deleteSession(id)
                    }
                )
            } else {
                DiagnosticScreen(
                    session = currentSession!!,
                    isAILoading = isAILoading,
                    onBack = {
                        viewModel.selectSession(null)
                    },
                    onSendMessage = { text ->
                        viewModel.sendMessage(text)
                    }
                )
            }

            if (showNewDialog) {
                NewDiagnosisDialog(
                    initialCategory = initialCategory,
                    onDismiss = { showNewDialog = false },
                    onStart = { carModel, symptom, category ->
                        showNewDialog = false
                        viewModel.startNewSession(carModel, symptom, category)
                    }
                )
            }
        }
    }
}

// --- SCREEN 1: DASHBOARD ---
@Composable
fun DashboardScreen(
    allSessions: List<DiagnosticSession>,
    onStartDiagnosis: (String) -> Unit,
    onSelectSession: (DiagnosticSession) -> Unit,
    onDeleteSession: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "طبيب السيارات AI",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "التشخيص الذكي لمشاكل وأعطال المركبات",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Graphic Section
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_car_diagnostic_hero),
                        contentDescription = "Car Diagnostic Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient overlay to make text highly readable
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.8f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "افحص سيارتك فوراً بالذكاء الاصطناعي",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "أجب عن الأسئلة الميكانيكية التفاعلية واحصل على حلول سريعة ودقيقة",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Quick Diagnose Launchers
            item {
                Column {
                    Text(
                        text = "اختر فئة العطل للبدء",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CategoryLauncherCard(
                            title = "أعطال ميكانيكية",
                            description = "محرك، فرامل، تبريد، صوت طقطقة",
                            icon = Icons.Default.Build,
                            color = GlowingAmber,
                            modifier = Modifier.weight(1f),
                            onClick = { onStartDiagnosis("ميكانيك") }
                        )
                        CategoryLauncherCard(
                            title = "أعطال كهربائية",
                            description = "بطارية، كمبيوتر، مصابيح، فيوزات",
                            icon = Icons.Default.Warning,
                            color = GlowingAmber,
                            modifier = Modifier.weight(1f),
                            onClick = { onStartDiagnosis("كهرباء") }
                        )
                    }
                }
            }

            // Diagnostic History Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سجل التشخيصات السابقة",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "(${allSessions.size} جلسة)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (allSessions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Empty History",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد جلسات تشخيص سابقة",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "ابدأ تشخيصاً جديداً الآن لحل مشاكل سيارتك",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp).padding(top = 4.dp)
                        )
                    }
                }
            } else {
                items(allSessions) { session ->
                    HistorySessionItem(
                        session = session,
                        onClick = { onSelectSession(session) },
                        onDelete = { onDeleteSession(session.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryLauncherCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun HistorySessionItem(
    session: DiagnosticSession,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateString = remember(session.timestamp) {
        val date = Date(session.timestamp)
        val format = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar"))
        format.format(date)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (session.category == "كهرباء") GlowingAmber.copy(alpha = 0.15f)
                            else SoftAmber.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (session.category == "كهرباء") Icons.Default.Warning else Icons.Default.Build,
                        contentDescription = null,
                        tint = if (session.category == "كهرباء") GlowingAmber else SoftAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = session.carModel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = session.symptom,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = dateString,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Badge
                Box(
                    modifier = Modifier
                        .background(
                            if (session.status == "مكتمل") GlowGreen.copy(alpha = 0.15f)
                            else GlowingAmber.copy(alpha = 0.15f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = session.status,
                        fontSize = 10.sp,
                        color = if (session.status == "مكتمل") GlowGreen else GlowingAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Session",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// --- SCREEN 2: ACTIVE DIAGNOSTICS CHAT ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticScreen(
    session: DiagnosticSession,
    isAILoading: Boolean,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    val messages = remember(session.messagesJson) {
        try {
            val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, DiagnosticMessage::class.java)
            val adapter = RetrofitClient.moshiInstance.adapter<List<DiagnosticMessage>>(listType)
            adapter.fromJson(session.messagesJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    val latestMessage = messages.lastOrNull { it.role == "assistant" }
    val optionsToClick = latestMessage?.options

    // Parse solution details if complete
    val parsedDiagnosisResult = remember(session.diagnosisSummary) {
        if (session.diagnosisSummary != null) {
            try {
                val adapter = RetrofitClient.moshiInstance.adapter(DiagnosticResult::class.java)
                adapter.fromJson(session.diagnosisSummary)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    var manualInputText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = session.carModel,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "فئة: " + session.category,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Chat history List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(12.dp)) }

                items(messages) { message ->
                    MessageBubble(message = message)
                }

                if (isAILoading) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "جاري الاتصال بـ طبيب السيارات AI وتحليل الرد...",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Show report card if final diagnosis is resolved
                if (parsedDiagnosisResult?.solutionDetails != null) {
                    item {
                        SolutionReportCard(
                            details = parsedDiagnosisResult.solutionDetails,
                            onFinish = onBack
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            // Interactive Bottom Input & Quick Replies
            Surface(
                tonalElevation = 4.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Quick Reply Chips
                    if (!optionsToClick.isNullOrEmpty() && !isAILoading && session.status != "مكتمل") {
                        Text(
                            text = "اختر إجابة سريعة للفحص:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            optionsToClick.forEach { option ->
                                SuggestionChip(
                                    label = option,
                                    onClick = {
                                        onSendMessage(option)
                                    }
                                )
                            }
                        }
                    }

                    // Manual Text Field Input
                    if (session.status != "مكتمل") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = manualInputText,
                                onValueChange = { manualInputText = it },
                                placeholder = {
                                    Text(
                                        "اكتب تفاصيل الفحص أو رد مخصص هنا...",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(fontSize = 13.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedContainerColor = MaterialTheme.colorScheme.background,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.background
                                ),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 3,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = {
                                    if (manualInputText.isNotBlank()) {
                                        onSendMessage(manualInputText)
                                        manualInputText = ""
                                    }
                                }),
                                enabled = !isAILoading
                            )
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isAILoading || manualInputText.isBlank()) MaterialTheme.colorScheme.outline
                                        else MaterialTheme.colorScheme.primary
                                    )
                                    .clickable(enabled = !isAILoading && manualInputText.isNotBlank()) {
                                        onSendMessage(manualInputText)
                                        manualInputText = ""
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send Message",
                                    tint = if (isAILoading || manualInputText.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                                    else Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        // Finished session disclaimer
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Done",
                                tint = GlowGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تم تشخيص هذا العطل وحفظ الحل بنجاح.",
                                fontSize = 12.sp,
                                color = GlowGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: DiagnosticMessage) {
    val isUser = message.role == "user"
    val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
    }

    val containerColor = if (isUser) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val borderStroke = if (isUser) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(GlowingAmber.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "AI Expert",
                        tint = GlowingAmber,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                color = containerColor,
                shape = bubbleShape,
                border = borderStroke,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = if (isUser) "أنت" else "طبيب السيارات الذكي",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) MaterialTheme.colorScheme.primary else GlowingAmber,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = message.content,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (isUser) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "User",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SuggestionChip(
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

// --- SCREEN 3: SOLUTION REPORT CARD ---
@Composable
fun SolutionReportCard(
    details: com.example.network.SolutionDetails,
    onFinish: () -> Unit
) {
    val cardColor = when (details.severity) {
        "خطير" -> DangerRed.copy(alpha = 0.08f)
        "متوسط" -> GlowingAmber.copy(alpha = 0.08f)
        else -> GlowGreen.copy(alpha = 0.08f)
    }
    val badgeColor = when (details.severity) {
        "خطير" -> DangerRed
        "متوسط" -> GlowingAmber
        else -> GlowGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.5.dp, badgeColor.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = GlowGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تقرير تشخيص العطل والحل المقترح",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                // Severity Badge
                Box(
                    modifier = Modifier
                        .background(badgeColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "الخطورة: " + details.severity,
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )

            // Summary
            Text(
                text = "العطل المحتمل أساساً:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = details.summary,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            // Steps
            Text(
                text = "خطوات الفحص والإصلاح الموصى بها:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            details.steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(badgeColor.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (index + 1).toString(),
                            fontSize = 10.sp,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = step,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )

            // Cost Estimate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "التكلفة والصعوبة المتوقعة:",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = details.estimatedCost,
                        fontSize = 12.sp,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = onFinish,
                    colors = ButtonDefaults.buttonColors(containerColor = badgeColor)
                ) {
                    Text(text = "إغلاق التقرير", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// --- DIALOG: NEW DIAGNOSIS ---
@Composable
fun NewDiagnosisDialog(
    initialCategory: String,
    onDismiss: () -> Unit,
    onStart: (carModel: String, symptom: String, category: String) -> Unit
) {
    var carModel by remember { mutableStateOf("") }
    var symptom by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(initialCategory) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "تشخيص عطل جديد",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "أدخل بيانات مركبتك والعطل لمطابقة خبير الصيانة المناسب",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Input Car Model
                Text(
                    text = "نوع السيارة وسنة الصنع:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = carModel,
                    onValueChange = { carModel = it },
                    placeholder = { Text("مثال: تويوتا كامري 2020", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Selector Category
                Text(
                    text = "نوع المشكلة التقريبي:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ميكانيك", "كهرباء", "عام").forEach { cat ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selectedCategory == cat) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedCategory = cat },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedCategory == cat) Color.Black else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input Symptom
                Text(
                    text = "ما الذي تلاحظه أو تعاني منه؟",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = symptom,
                    onValueChange = { symptom = it },
                    placeholder = { Text("مثال: المحرك لا يدور عند التشغيل وتصدر طقطقة خفيفة", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Text(text = "إلغاء", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            if (carModel.isNotBlank() && symptom.isNotBlank()) {
                                onStart(carModel, symptom, selectedCategory)
                            }
                        },
                        enabled = carModel.isNotBlank() && symptom.isNotBlank(),
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(text = "ابدأ التشخيص الآن", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }
}


