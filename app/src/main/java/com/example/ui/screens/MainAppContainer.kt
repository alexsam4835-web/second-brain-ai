package com.example.ui.screens

import android.util.Base64
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.api.GeminiClient
import com.example.data.ChatMessage
import com.example.data.MemoryItem
import com.example.data.ReminderItem
import com.example.ui.theme.*
import com.example.viewmodel.SecondBrainViewModel
import kotlinx.coroutines.launch
import java.util.*

enum class AppTab {
    DASHBOARD, TIMELINE, CHAT, REMINDERS, EXTRA_LABS, BILLING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: SecondBrainViewModel) {
    var isAuthenticated by remember { mutableStateOf(false) }
    var userEmailInput by remember { mutableStateOf("alexsam4835@gmail.com") }
    var userPasswordInput by remember { mutableStateOf("••••••••••••") }
    var authError by remember { mutableStateOf("") }

    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val isUploading by viewModel.isUploading.collectAsStateWithLifecycle()

    if (!isAuthenticated) {
        // --- 1. AUTHENTICATION & ONBOARDING SYSTEM ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkBg, DarkSurface)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Futuristic Glowing Brain Logo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(colors = listOf(NeonCyan.copy(alpha = 0.4f), Color.Transparent)))
                        .border(2.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Second Brain Logo",
                        tint = NeonCyan,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SECOND BRAIN",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 3.sp,
                        color = NeonCyan
                    )
                )

                Text(
                    text = "Your Personal AI Memory Assistant",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Credentials Panel
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Secure User Login",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = userEmailInput,
                            onValueChange = { userEmailInput = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, tint = NeonCyan, contentDescription = null) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedLabelColor = NeonCyan
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("username_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = userPasswordInput,
                            onValueChange = { userPasswordInput = it },
                            label = { Text("Access Password / OTP") },
                            leadingIcon = { Icon(Icons.Default.Lock, tint = NeonCyan, contentDescription = null) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedLabelColor = NeonCyan
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (authError.isNotEmpty()) {
                            Text(
                                text = authError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (userEmailInput.isBlank()) {
                                    authError = "Please enter a valid credential"
                                } else {
                                    isAuthenticated = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = OnPrimaryLight),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_button")
                        ) {
                            Text(
                                text = "Initialize Synchronization",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        // Social SSO/Mobile Triggers
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { isAuthenticated = true },
                                border = BorderStroke(1.dp, NeonCyan),
                                modifier = Modifier.weight(1f).height(44.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                            ) {
                                Icon(Icons.Default.Stars, contentDescription = "Google Logo", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Google SSO", fontSize = 11.sp, maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = { isAuthenticated = true },
                                border = BorderStroke(1.dp, NeonCyan),
                                modifier = Modifier.weight(1f).height(44.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                            ) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = "OTP SMS Code", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mobile OTP", fontSize = 11.sp, maxLines = 1)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Demo Key Configured in Secrets. Non-volatile SQLite ready.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        // --- AUTHENTICATED APP SHELL ---
        var currentTab by remember { mutableStateOf(AppTab.DASHBOARD) }
        var showAddNoteDialog by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Brain Icon",
                                tint = NeonCyan,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SECOND BRAIN",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = TextWhite
                                )
                            )
                        }
                    },
                    actions = {
                        // Premium indicator badge
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isPremium) ElectricBlue else DarkSurfaceVariant)
                                .clickable { viewModel.togglePremium() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isPremium) Icons.Default.WorkspacePremium else Icons.Default.Bolt,
                                    contentDescription = "Premium Level",
                                    tint = if (isPremium) Color.Yellow else NeonCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isPremium) "PREMIUM ACCESS" else "STARTER FREE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPremium) Color.White else NeonCyan
                                )
                            }
                        }

                        // Synced Status indicator
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isUploading) BrightPurple else Color(0xFF00E676))
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = DarkSurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = currentTab == AppTab.DASHBOARD,
                        onClick = { currentTab = AppTab.DASHBOARD },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Core", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OnPrimaryLight,
                            unselectedIconColor = TextMuted,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.TIMELINE,
                        onClick = { currentTab = AppTab.TIMELINE },
                        icon = { Icon(Icons.Default.List, contentDescription = "Timeline") },
                        label = { Text("Brain", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OnPrimaryLight,
                            unselectedIconColor = TextMuted,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.CHAT,
                        onClick = { currentTab = AppTab.CHAT },
                        icon = { Icon(Icons.AutoMirrored.Default.Chat, contentDescription = "AI Chat") },
                        label = { Text("AI Chat", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OnPrimaryLight,
                            unselectedIconColor = TextMuted,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.REMINDERS,
                        onClick = { currentTab = AppTab.REMINDERS },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Reminders") },
                        label = { Text("Remind", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OnPrimaryLight,
                            unselectedIconColor = TextMuted,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.EXTRA_LABS,
                        onClick = { currentTab = AppTab.EXTRA_LABS },
                        icon = { Icon(Icons.Default.Share, contentDescription = "WhatsApp & Admin Sandbox") },
                        label = { Text("Labs", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OnPrimaryLight,
                            unselectedIconColor = TextMuted,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == AppTab.BILLING,
                        onClick = { currentTab = AppTab.BILLING },
                        icon = { Icon(Icons.Default.Info, contentDescription = "Pricing") },
                        label = { Text("Plan", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OnPrimaryLight,
                            unselectedIconColor = TextMuted,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan
                        )
                    )
                }
            },
            floatingActionButton = {
                if (currentTab == AppTab.DASHBOARD || currentTab == AppTab.TIMELINE) {
                    FloatingActionButton(
                        onClick = { showAddNoteDialog = true },
                        containerColor = NeonCyan,
                        contentColor = OnPrimaryLight,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Fast Add", modifier = Modifier.size(24.dp))
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(DarkBg)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        fadeIn() + slideInHorizontally() togetherWith fadeOut() + slideOutHorizontally()
                    },
                    label = "TabTransition"
                ) { targetTab ->
                    when (targetTab) {
                        AppTab.DASHBOARD -> DashboardScreen(viewModel, onNavigateToLabs = { currentTab = AppTab.EXTRA_LABS }, onAddClick = { showAddNoteDialog = true })
                        AppTab.TIMELINE -> TimelineScreen(viewModel)
                        AppTab.CHAT -> ChatAssistantScreen(viewModel)
                        AppTab.REMINDERS -> RemindersScreen(viewModel)
                        AppTab.EXTRA_LABS -> ExtraLabsScreen(viewModel)
                        AppTab.BILLING -> BillingScreen(viewModel)
                    }
                }

                // If processing / uploading with Gemini API -> show fullscreen glowing scrim overlay
                if (isUploading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.82f))
                            .clickable(enabled = false) {},
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(50.dp))
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "RECONSTRUCTING COGNITIVE NODES...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Running OCR / generating tags with Gemini-3.5-flash",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Dialog for Manual Adding of Quick Memory
            if (showAddNoteDialog) {
                var itemTitle by remember { mutableStateOf("") }
                var itemContent by remember { mutableStateOf("") }
                var selectedType by remember { mutableStateOf("text") }
                val types = listOf("text", "pdf", "screenshot", "link")
                var limitError by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { showAddNoteDialog = false },
                    title = {
                        Text(
                            "Brain Node Digitizer",
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Fast insert coordinates, links or articles. AI analyzes automatically.", fontSize = 12.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = itemTitle,
                                onValueChange = { itemTitle = it },
                                label = { Text("Title Hint") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = CardBorder, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite, focusedLabelColor = NeonCyan)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = itemContent,
                                onValueChange = { itemContent = it },
                                label = { Text("Raw Text Data / Link") },
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = CardBorder, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite, focusedLabelColor = NeonCyan)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Node Type:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                types.forEach { typ ->
                                    val isSel = selectedType == typ
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSel) NeonCyan else DarkSurfaceVariant)
                                            .clickable { selectedType = typ }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = typ.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) OnPrimaryLight else TextMuted
                                        )
                                    }
                                }
                            }

                            if (limitError) {
                                Text(
                                    "🚨 Free starter tier reached (max 4 memories). Go to Plan tab to secure premium index!",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (itemContent.isNotBlank()) {
                                    viewModel.processUpload(
                                        titleHint = if (itemTitle.isBlank()) "Quick $selectedType" else itemTitle,
                                        mediaType = selectedType,
                                        inputText = itemContent
                                    ) { success ->
                                        if (success) {
                                            showAddNoteDialog = false
                                        } else {
                                            limitError = true
                                        }
                                    }
                                }
                            }
                        ) {
                            Text("Digitize with AI", color = NeonCyan, fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.border(1.dp, CardBorder, RoundedCornerShape(24.dp)),
                    dismissButton = {
                        TextButton(onClick = { showAddNoteDialog = false }) {
                            Text("Cancel", color = TextMuted)
                        }
                    },
                    containerColor = DarkSurface,
                    shape = RoundedCornerShape(24.dp)
                )
            }
        }
    }
}

// --- SUB SCREEN: 1. DASHBOARD ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: SecondBrainViewModel,
    onNavigateToLabs: () -> Unit,
    onAddClick: () -> Unit
) {
    val memories by viewModel.searchedMemories.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val recentMemories = memories.take(4)
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            // Large Greeting Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, Alex Sam",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextMuted
                    )
                    Text(
                        text = "Synchronized Mind",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant)
                        .border(1.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User avatar",
                        tint = NeonCyan
                    )
                }
            }
        }

        // Live Dynamic AI Search Tool
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Ask your Second Brain anything...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, tint = NeonCyan, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, tint = TextMuted, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedContainerColor = DarkSurface.copy(alpha = 0.5f),
                    unfocusedContainerColor = DarkSurface.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth().testTag("search_bar")
            )
        }

        // Fast Quick Sandbox Operations Row
        item {
            Column {
                Text(
                    text = "Quick Mind Integrations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionBtn(
                        label = "Sandbox Labs",
                        icon = Icons.Default.Science,
                        col = BrightPurple,
                        onClick = onNavigateToLabs
                    )
                    QuickActionBtn(
                        label = "Add Link/Note",
                        icon = Icons.Default.EditNote,
                        col = NeonCyan,
                        onClick = onAddClick
                    )
                }
            }
        }

        // Limit Warning for free users
        if (!isPremium && memories.size >= 4) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Limit Reach", tint = Color.Red)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Standard Capacity Exhausted", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 13.sp)
                            Text("Starter account is capped at 4 items. Navigate to Plan tab to secure premium.", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Memory Nodes Feed (Query Results)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (searchQuery.isNotEmpty()) "Matching Memories" else "Recent Memories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                if (searchQuery.isNotEmpty()) {
                    Text(
                        text = "${memories.size} found",
                        fontSize = 12.sp,
                        color = NeonCyan
                    )
                }
            }
        }

        if (recentMemories.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CloudQueue, contentDescription = "Empty", tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No memories indexed under query.", color = TextMuted, fontSize = 13.sp)
                        Text("Try tapping Sandbox Labs below to mock inputs!", color = NeonCyan, fontSize = 11.sp)
                    }
                }
            }
        } else {
            items(recentMemories) { item ->
                MemoryCompactCard(item, onDelete = { viewModel.deleteMemory(item) })
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun QuickActionBtn(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, col: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .clickable { onClick() }
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(col.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = col, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(label, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

// --- SUB SCREEN: 2. TIMELINE ---
@Composable
fun TimelineScreen(viewModel: SecondBrainViewModel) {
    val memories by viewModel.searchedMemories.collectAsStateWithLifecycle()
    var selectedCategoryFilter by remember { mutableStateOf("all") }
    val categories = listOf("all", "finance", "study", "travel", "medical", "passwords", "assignments", "general")

    val filteredMemories = remember(memories, selectedCategoryFilter) {
        if (selectedCategoryFilter == "all") {
            memories
        } else {
            memories.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Chronological Memory",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )
        Text(
            text = "A temporal stream charting your consolidated intelligence data.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSel = selectedCategoryFilter == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSel) NeonCyan else DarkSurfaceVariant)
                        .clickable { selectedCategoryFilter = cat }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        cat.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isSel) OnPrimaryLight else TextWhite
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredMemories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Explore, contentDescription = "Empty Category", tint = TextMuted, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No item registers in this coordinate.", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredMemories) { item ->
                    MemoryTimelineItem(item, onDelete = { viewModel.deleteMemory(item) })
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun MemoryCompactCard(item: MemoryItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .testTag("task_item_card"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type Badge
                val badgeColor = when (item.mediaType) {
                    "screenshot" -> NeonCyan
                    "pdf" -> BrightPurple
                    "voice_note" -> ElectricBlue
                    "link" -> Color(0xFFFF5252)
                    else -> TextMuted
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.mediaType.uppercase(),
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = "importance", tint = Color.Yellow, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Score ${item.importanceScore}/10",
                        color = TextWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Wipe Memory", tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.extractedSummary,
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 16.sp
            )

            if (item.rawText.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Raw OCR: ${item.rawText}",
                    fontSize = 10.sp,
                    color = TextMuted.copy(alpha = 0.5f),
                    maxLines = 1,
                    fontWeight = FontWeight.Light
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tags loop
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item.tags.split(",").forEach { tag ->
                    val cleanTag = tag.trim()
                    if (cleanTag.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DarkSurfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("#$cleanTag", fontSize = 9.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemoryTimelineItem(item: MemoryItem, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Temporal axis point
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (item.mediaType == "screenshot") NeonCyan else BrightPurple)
                    .border(2.dp, DarkBg, CircleShape)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(130.dp)
                    .background(CardBorder)
            )
        }

        // Timeline Body
        Box(modifier = Modifier.weight(1f)) {
            MemoryCompactCard(item, onDelete = onDelete)
        }
    }
}

// --- SUB SCREEN: 3. AI CHAT ASSISTANT ---
@Composable
fun ChatAssistantScreen(viewModel: SecondBrainViewModel) {
    val messages by viewModel.chatHistory.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    var userTextQuery by remember { mutableStateOf("") }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Second Brain RAG Chat",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "Asks anything. The AI searches your Room document contexts directly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            IconButton(onClick = { viewModel.clearChat() }) {
                Icon(Icons.Default.ClearAll, contentDescription = "Wipe chat logs", tint = Color.Red.copy(alpha = 0.7f))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat Conversation Frame
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface.copy(alpha = 0.3f))
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
        ) {
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Empty Chat", tint = NeonCyan, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Awaiting mental prompts...",
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ask questions like:\n• \"Which PDF has python details?\"\n• \"Show restaurant shared by mom.\"\n• \"What are my bank numbers?\"",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        ChatBubble(msg)
                    }

                    if (isChatLoading) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(4.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(bottomEnd = 16.dp, topStart = 16.dp, topEnd = 16.dp))
                                        .background(DarkSurfaceVariant)
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = NeonCyan)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("AI Brain scanning resources...", fontSize = 11.sp, color = NeonCyan)
                                    }
                                }
                            }
                        }
                    }
                }

                // Scroll to bottom whenever a new node loads
                LaunchedEffect(messages.size, isChatLoading) {
                    if (messages.isNotEmpty()) {
                        coroutineScope.launch {
                            listState.animateScrollToItem(messages.size - 1)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dynamic Input console
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = userTextQuery,
                onValueChange = { userTextQuery = it },
                placeholder = { Text("Query memory index...", color = TextMuted) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (userTextQuery.isNotBlank() && !isChatLoading) {
                        viewModel.sendChatMessage(userTextQuery)
                        userTextQuery = ""
                        keyboardController?.hide()
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                modifier = Modifier.weight(1f).height(50.dp)
            )

            IconButton(
                onClick = {
                    if (userTextQuery.isNotBlank() && !isChatLoading) {
                        viewModel.sendChatMessage(userTextQuery)
                        userTextQuery = ""
                        keyboardController?.hide()
                    }
                },
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonCyan)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Query Link", tint = OnPrimaryLight)
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.sender == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        val shape = if (isUser) {
            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp)
        } else {
            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp)
        }

        val containerColor = if (isUser) NeonCyan else DarkSurfaceVariant
        val textColor = if (isUser) OnPrimaryLight else TextWhite

        Box(
            modifier = Modifier
                .clip(shape)
                .background(containerColor)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = msg.text,
                color = textColor,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

// --- SUB SCREEN: 4. REMINDERS ---
@Composable
fun RemindersScreen(viewModel: SecondBrainViewModel) {
    val reminders by viewModel.allReminders.collectAsStateWithLifecycle()
    var manualTitle by remember { mutableStateOf("") }
    var manualDate by remember { mutableStateOf("Tomorrow") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "AI Extracted Reminders",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )
        Text(
            text = "These tasks were systematically identified inside your notes and file indexes.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Fast manual scheduler
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Insert Standalone Coordinator", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualTitle,
                        onValueChange = { manualTitle = it },
                        placeholder = { Text("Task (e.g. Call Mom)", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, focusedTextColor = TextWhite, focusedContainerColor = DarkSurface, unfocusedContainerColor = DarkSurface),
                        modifier = Modifier.weight(1.5f).height(46.dp)
                    )
                    OutlinedTextField(
                        value = manualDate,
                        onValueChange = { manualDate = it },
                        placeholder = { Text("Due (e.g. Friday)", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, focusedTextColor = TextWhite, focusedContainerColor = DarkSurface, unfocusedContainerColor = DarkSurface),
                        modifier = Modifier.weight(1f).height(46.dp)
                    )
                    IconButton(
                        onClick = {
                            if (manualTitle.isNotBlank()) {
                                viewModel.insertManualReminder(manualTitle, manualDate)
                                manualTitle = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonCyan)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Add manual target", tint = OnPrimaryLight)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (reminders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Assignment, contentDescription = "None", tint = TextMuted, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Zero deadlines detected in brain database.", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reminders) { reminder ->
                    ReminderRow(reminder,
                        onToggle = { comp -> viewModel.toggleReminderStatus(reminder.id, comp) },
                        onDelete = { viewModel.deleteReminder(reminder) }
                    )
                }
            }
        }
    }
}

@Composable
fun ReminderRow(reminder: ReminderItem, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (reminder.isCompleted) CardBorder else NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = reminder.isCompleted,
                    onCheckedChange = onToggle,
                    colors = CheckboxDefaults.colors(checkedColor = NeonCyan, uncheckedColor = NeonCyan)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Text(
                        text = reminder.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (reminder.isCompleted) TextMuted else TextWhite,
                        style = if (reminder.isCompleted) LocalTextStyle.current.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else LocalTextStyle.current
                    )
                    Text(
                        text = "Due: ${reminder.detectedDateString}",
                        fontSize = 11.sp,
                        color = BrightPurple
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete target reminder", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

// --- SUB SCREEN: 5. SANDBOX LABS (WHATSAPP, DEMO OCR AND ADMIN) ---
@Composable
fun ExtraLabsScreen(viewModel: SecondBrainViewModel) {
    val keyState = GeminiClient.getApiKey().isNotEmpty()
    val tokensUsed by viewModel.aiTokensUsed.collectAsStateWithLifecycle()
    val activeUsers by viewModel.activeUsersCount.collectAsStateWithLifecycle()
    val memories by viewModel.allMemories.collectAsStateWithLifecycle()

    var whatsappInput by remember { mutableStateOf("Amit (WhatsApp): Hey Alex, check out this python assignment guidelines! Deadline to send candidate files is next Thursday. Let's practice viva tomorrow at 9 AM.") }
    var activeLabTab by remember { mutableStateOf(0) } // 0: Presets Sandbox, 1: Whatsapp Sync, 2: Admin Control

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "AI Integration & Sandbox Lab",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )
        Text(
            text = "Test instant industrial integrations, WhatsApp chatbot simulations, or study admin dials.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive Dial Controls
        TabRow(
            selectedTabIndex = activeLabTab,
            containerColor = DarkSurface,
            contentColor = NeonCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeLabTab]),
                    color = NeonCyan
                )
            }
        ) {
            Tab(selected = activeLabTab == 0, onClick = { activeLabTab = 0 }) { Text("Presets", modifier = Modifier.padding(10.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            Tab(selected = activeLabTab == 1, onClick = { activeLabTab = 1 }) { Text("WhatsApp", modifier = Modifier.padding(10.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            Tab(selected = activeLabTab == 2, onClick = { activeLabTab = 2 }) { Text("Admin Console", modifier = Modifier.padding(10.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (activeLabTab) {
            0 -> {
                // Presets Sandbox List
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("💡 Smart Emulation Engine", fontWeight = FontWeight.Bold, color = NeonCyan)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("In emulator environments, you won't have real physical documents. Click below to mock OCR and multimodal scanning, invoking our genuine on-device Gemini API instantly!", fontSize = 11.sp, color = TextWhite)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (keyState) Color(0xFF00E676) else Color.Red)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (keyState) "Gemini Key Configured & Active" else "API Key Missing (Simulating outputs)",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }

                    item {
                        PresetScannerCard(
                            title = "Mock National ID Screenshot OCR",
                            description = "Base64 payload detailing 'Alex Sam. Birth Year 1998. Aadhaar: 4835...'",
                            type = "screenshot",
                            input = "Mock Screenshot upload payload detailing full OCR parameters",
                            onClick = {
                                viewModel.processUpload(
                                    titleHint = "Simulated National Aadhaar Card",
                                    mediaType = "screenshot",
                                    inputText = "NAME: ALEX SAM. BIRTH YEAR: 1998. NO: 4835 1284 4215. DEPT: UIDAI"
                                )
                            }
                        )
                    }

                    item {
                        PresetScannerCard(
                            title = "Mock Rx Prescription Receipt",
                            description = "Simulate camera scan of medical prescription containing dosages and task reminders.",
                            type = "screenshot",
                            input = "Prescription Rx details",
                            onClick = {
                                viewModel.processUpload(
                                    titleHint = "Simulated Rx Prescription Scan",
                                    mediaType = "screenshot",
                                    inputText = "Rx Medicine: Paracetamol 500mg daily. Tablet: Multivitamin and iron tonic. Take every morning until Saturday, June 13th."
                                )
                            }
                        )
                    }

                    item {
                        PresetScannerCard(
                            title = "Mock CS PDF Syllabus Syllabus Outline",
                            description = "Emulates document upload containing Python scope and examination timetables.",
                            type = "pdf",
                            input = "PDF byte indices",
                            onClick = {
                                viewModel.processUpload(
                                    titleHint = "Python CS Course Guidelines Guide",
                                    mediaType = "pdf",
                                    inputText = "Syllabus CS204. Midterm examination scheduled for Tuesday June 9th at lab room 403. Carry physical blueprint prints."
                                )
                            }
                        )
                    }

                    item {
                        PresetScannerCard(
                            title = "Mock Audio Note forwarded: 'Train Ticket'",
                            description = "Simulates processing an exported audio file forwarded by Amit regarding reservations.",
                            type = "voice_note",
                            input = "Forwarded note",
                            onClick = {
                                viewModel.processUpload(
                                    titleHint = "Simulated Voice note transcript: Ticket",
                                    mediaType = "voice_note",
                                    inputText = "Hi, regarding train ticket booking: reservation code PNR-84215. Flight departs at 14:00 on April 28th."
                                )
                            }
                        )
                    }
                }
            }

            1 -> {
                // WhatsApp Synchronizer simulation
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "WhatsApp Sync simulator",
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Second Brain simulates chatbot loops which automatically parse shared text threads and index data chronologically.",
                        fontSize = 11.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = whatsappInput,
                        onValueChange = { whatsappInput = it },
                        modifier = Modifier.fillMaxWidth().height(110.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, focusedTextColor = TextWhite, focusedContainerColor = DarkSurface, unfocusedContainerColor = DarkSurface),
                        label = { Text("Simulate Export Chat") }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (whatsappInput.isNotBlank()) {
                                viewModel.simulateWhatsAppImport(whatsappInput)
                                whatsappInput = "Thread Imported successfully to Memory timeline!"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = OnPrimaryLight),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Simulate Chat Processing", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("How it operates:", fontWeight = FontWeight.Bold, color = BrightPurple, fontSize = 12.sp)
                            Text("1. In production, users share/forward media direct into Second Brain from social chat platforms.\n2. In-app OCR splits files into structural tags automatically.", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }

            2 -> {
                // Admin control analytics dashboard
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AdminDataBlock(
                                title = "API Tokens Spent",
                                count = tokensUsed.toString(),
                                col = NeonCyan,
                                modifier = Modifier.weight(1.0f)
                            )
                            AdminDataBlock(
                                title = "Synthesizer registers",
                                count = memories.size.toString(),
                                col = BrightPurple,
                                modifier = Modifier.weight(1.0f)
                            )
                        }
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AdminDataBlock(
                                title = "Virtual Active Index",
                                count = activeUsers.toString(),
                                col = ElectricBlue,
                                modifier = Modifier.weight(1.0f)
                            )
                            AdminDataBlock(
                                title = "Db Status",
                                count = "Normal",
                                col = Color.Green,
                                modifier = Modifier.weight(1.0f)
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { viewModel.clearAllData() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Wipe SQLite Database Registers")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PresetScannerCard(title: String, description: String, type: String, input: String, onClick: () -> Unit) {
    var hasImported by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 13.sp)
                Text(description, color = TextMuted, fontSize = 11.sp, lineHeight = 14.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    onClick()
                    hasImported = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (hasImported) DarkSurfaceVariant else NeonCyan, contentColor = if (hasImported) TextMuted else OnPrimaryLight),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(if (hasImported) "Synced" else "Inject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminDataBlock(title: String, count: String, col: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(modifier = Modifier.height(6.dp))
            Text(count, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = col)
        }
    }
}

// --- SUB SCREEN: 6. BILLING PLAN & LANDING PAGE ---
@Composable
fun BillingScreen(viewModel: SecondBrainViewModel) {
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    var emailWaitlist by remember { mutableStateOf("") }
    var joinResponse by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Secure Memory Subscriptions",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = TextWhite
            )
            Text(
                "Scale up your cognitive database indexes with premium services.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }

        item {
            // Interactive Toggle upgrade
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, NeonCyan, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Second Brain Premium", fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("Unlimited uploads, advanced OCR, daily digests.", fontSize = 11.sp, color = TextMuted)
                        }
                        Text("$4.99/mo", fontWeight = FontWeight.ExtraBold, color = NeonCyan)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.togglePremium() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = OnPrimaryLight),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text(
                            text = if (isPremium) "Downgrade back to Free Plan" else "Activate Premium Index instantly!",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Feature Comparison Grid
        item {
            Text("Coordinate Tiers", fontWeight = FontWeight.Bold, color = TextWhite)
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TierRow("SQLite Memory Capacity", "4 Items", "Unlimited")
                    Divider(color = CardBorder, modifier = Modifier.padding(vertical = 8.dp))
                    TierRow("Gemini-3.5 RAG chat", "Basic Search", "Full Deductions")
                    Divider(color = CardBorder, modifier = Modifier.padding(vertical = 8.dp))
                    TierRow("Smart Reminders", "Visual Log", "Automatic Calendar")
                    Divider(color = CardBorder, modifier = Modifier.padding(vertical = 8.dp))
                    TierRow("Cloud Sync Simulation", "❌ Local only", "✅ Encrypted Cloud")
                }
            }
        }

        // Waitlist registration fields
        item {
            Text("Secure Waitlist Index", fontWeight = FontWeight.Bold, color = TextWhite)
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Register below to join waitlist for upcoming Gmail, Telegram, Notion, Slack sync triggers!", fontSize = 11.sp, color = TextWhite)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = emailWaitlist,
                            onValueChange = { emailWaitlist = it },
                            placeholder = { Text("Email (e.g. alex@gmail.com)", fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, focusedTextColor = TextWhite, focusedContainerColor = DarkSurface, unfocusedContainerColor = DarkSurface),
                            modifier = Modifier.weight(1.0f).height(46.dp)
                        )
                        Button(
                            onClick = {
                                if (emailWaitlist.contains("@")) {
                                    joinResponse = "Successfully registered $emailWaitlist!"
                                    emailWaitlist = ""
                                } else {
                                    joinResponse = "Enter valid email address!"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrightPurple),
                            modifier = Modifier.height(46.dp)
                        ) {
                            Text("Secure Seat", fontSize = 11.sp)
                        }
                    }
                    if (joinResponse.isNotEmpty()) {
                        Text(joinResponse, color = NeonCyan, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }

        // Client Testimonial Feed
        item {
            Text("Personal Testimonials", fontWeight = FontWeight.Bold, color = TextWhite)
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("\"Second Brain completely saved my college life! I forward everything from Telegram, and I recall dates instantly during exams!\"", color = TextMuted, fontSize = 12.sp, style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("- Amit Sharma, CS Undergrad", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TierRow(metric: String, free: String, prem: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(metric, fontSize = 11.sp, color = TextMuted, modifier = Modifier.width(130.dp))
        Text(free, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(90.dp))
        Text(prem, fontSize = 11.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
    }
}
