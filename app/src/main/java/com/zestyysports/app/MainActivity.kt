package com.zestyysports.app

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.analytics.FirebaseAnalytics
import com.microsoft.clarity.Clarity
import com.microsoft.clarity.ClarityConfig
import com.microsoft.clarity.models.LogLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

// ─── Fonts ────────────────────────────────────────────────────────────────────

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val InterFontFamily = FontFamily(
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.ExtraBold),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Black)
)

// ─── Colors (matching web Tailwind palette exactly) ───────────────────────────

object ZColors {
    val Red600 = Color(0xFFDC2626)
    val Red500 = Color(0xFFEF4444)
    val Blue500 = Color(0xFF3B82F6)
    val Sky400 = Color(0xFF38BDF8)
    val Emerald400 = Color(0xFF34D399)
    val Cyan500 = Color(0xFF06B6D4)
    val Green400 = Color(0xFF4ADE80)
    val Amber500 = Color(0xFFF59E0B)
    val Blue400 = Color(0xFF60A5FA)

    // Dark theme
    val Neutral950 = Color(0xFF0A0A0A)
    val Neutral900 = Color(0xFF171717)
    val Neutral800 = Color(0xFF262626)
    val Neutral700 = Color(0xFF404040)
    val Neutral600 = Color(0xFF525252)
    val Neutral500 = Color(0xFF737373)
    val Neutral400 = Color(0xFFA3A3A3)
    val Neutral300 = Color(0xFFD4D4D4)
    val White = Color(0xFFFFFFFF)

    // Light theme
    val Neutral100 = Color(0xFFF5F5F5)
    val Neutral200 = Color(0xFFE5E5E5)
}

// ─── Data ─────────────────────────────────────────────────────────────────────

data class M3UItem(
    val id: String,
    val name: String,
    val logo: String,
    val group: String,
    val url: String
)

// ─── MainActivity ─────────────────────────────────────────────────────────────

class MainActivity : ComponentActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        Clarity.initialize(
            applicationContext,
            ClarityConfig(projectId = "x98ux3eejx", logLevel = LogLevel.None)
        )

        setContent {
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("zesty_prefs", Context.MODE_PRIVATE) }
            val isDark = remember { mutableStateOf(prefs.getBoolean("is_dark_theme", true)) }

            val colorScheme = if (isDark.value) darkColorScheme(
                background = ZColors.Neutral950,
                surface = ZColors.Neutral900,
                onBackground = ZColors.White,
                onSurface = ZColors.Neutral300
            ) else lightColorScheme(
                background = ZColors.Neutral100,
                surface = ZColors.White,
                onBackground = ZColors.Neutral900,
                onSurface = ZColors.Neutral600
            )

            val sfLetterSpacing = (-0.5).sp
            val typography = Typography(
                displayLarge = Typography().displayLarge.copy(fontFamily = InterFontFamily, letterSpacing = sfLetterSpacing),
                displayMedium = Typography().displayMedium.copy(fontFamily = InterFontFamily, letterSpacing = sfLetterSpacing),
                displaySmall = Typography().displaySmall.copy(fontFamily = InterFontFamily, letterSpacing = sfLetterSpacing),
                headlineLarge = Typography().headlineLarge.copy(fontFamily = InterFontFamily, letterSpacing = sfLetterSpacing),
                headlineMedium = Typography().headlineMedium.copy(fontFamily = InterFontFamily, letterSpacing = sfLetterSpacing),
                headlineSmall = Typography().headlineSmall.copy(fontFamily = InterFontFamily, letterSpacing = sfLetterSpacing),
                titleLarge = Typography().titleLarge.copy(fontFamily = InterFontFamily, letterSpacing = sfLetterSpacing),
                titleMedium = Typography().titleMedium.copy(fontFamily = InterFontFamily, letterSpacing = sfLetterSpacing),
                titleSmall = Typography().titleSmall.copy(fontFamily = InterFontFamily, letterSpacing = sfLetterSpacing),
                bodyLarge = Typography().bodyLarge.copy(fontFamily = InterFontFamily, letterSpacing = sfLetterSpacing),
                bodyMedium = Typography().bodyMedium.copy(fontFamily = InterFontFamily, letterSpacing = sfLetterSpacing),
                bodySmall = Typography().bodySmall.copy(fontFamily = InterFontFamily, letterSpacing = sfLetterSpacing),
                labelLarge = Typography().labelLarge.copy(fontFamily = InterFontFamily, letterSpacing = sfLetterSpacing),
                labelMedium = Typography().labelMedium.copy(fontFamily = InterFontFamily, letterSpacing = sfLetterSpacing),
                labelSmall = Typography().labelSmall.copy(fontFamily = InterFontFamily, letterSpacing = sfLetterSpacing)
            )

            MaterialTheme(colorScheme = colorScheme, typography = typography) {
                CompositionLocalProvider(
                    LocalTextStyle provides androidx.compose.ui.text.TextStyle(fontFamily = InterFontFamily)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        ZestyyApp(
                            isDark = isDark.value,
                            onThemeToggle = {
                                val next = !isDark.value
                                isDark.value = next
                                prefs.edit().putBoolean("is_dark_theme", next).apply()
                            },
                            onOrientationChange = { landscape ->
                                requestedOrientation = if (landscape)
                                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                else
                                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                            }
                        )
                    }
                }
            }
        }
    }
}

// ─── App Root ──────────────────────────────────────────────────────────────────

@Composable
fun ZestyyApp(isDark: Boolean, onThemeToggle: () -> Unit, onOrientationChange: (Boolean) -> Unit) {
    var selectedChannel by remember { mutableStateOf<M3UItem?>(null) }
    var channels by remember { mutableStateOf<List<M3UItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            while (true) {
                try {
                    val req = Request.Builder()
                        .url("https://raw.githubusercontent.com/nikkexe0-del/alexplaylist/refs/heads/main/adl.m3u")
                        .build()
                    val body = client.newCall(req).execute().body?.string() ?: ""
                    val parsed = parseM3U(body)
                    withContext(Dispatchers.Main) {
                        channels = parsed
                        isLoading = false
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { isLoading = false }
                }
                delay(60_000)
            }
        }
    }

    if (selectedChannel != null) {
        VideoPlayerScreen(
            channel = selectedChannel!!,
            allChannels = channels,
            isDark = isDark,
            onOrientationChange = onOrientationChange,
            onBack = { selectedChannel = null; onOrientationChange(false) },
            onChannelSelect = { selectedChannel = it }
        )
    } else {
        onOrientationChange(false)
        MainScreen(
            channels = channels,
            isLoading = isLoading,
            isDark = isDark,
            onThemeToggle = onThemeToggle,
            onPlay = { selectedChannel = it }
        )
    }
}

// ─── Main Screen ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    channels: List<M3UItem>,
    isLoading: Boolean,
    isDark: Boolean,
    onThemeToggle: () -> Unit,
    onPlay: (M3UItem) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("zesty_prefs", Context.MODE_PRIVATE) }

    // State - mirrors web app exactly
    var currentTab by remember { mutableStateOf(prefs.getString("active_tab", "home") ?: "home") }
    var searchQuery by remember { mutableStateOf("") }
    var activeGroup by remember { mutableStateOf("All") }
    var favorites by remember { mutableStateOf(prefs.getStringSet("favorites", emptySet()) ?: emptySet()) }
    var displayedItemCount by remember { mutableStateOf(40) }
    var showPopup by remember { mutableStateOf(true) }

    LaunchedEffect(favorites) { prefs.edit().putStringSet("favorites", favorites).apply() }
    LaunchedEffect(currentTab) { prefs.edit().putString("active_tab", currentTab).apply() }

    // Groups with priority order like web app
    val groups = remember(channels) {
        val allGroups = channels.map { it.group }.filter { it.isNotBlank() }.distinct().sorted()
        val priority = listOf("FIFA World Cup 2026", "Cricket", "Football")
        val ordered = mutableListOf<String>()
        priority.forEach { p -> allGroups.find { it.equals(p, true) }?.let { ordered.add(it) } }
        allGroups.forEach { g -> if (priority.none { it.equals(g, true) }) ordered.add(g) }
        listOf("All") + ordered
    }

    val filteredChannels = remember(channels, currentTab, searchQuery, activeGroup, favorites) {
        displayedItemCount = 40
        when (currentTab) {
            "favorites" -> channels.filter { favorites.contains(it.id) }
            "search" -> if (searchQuery.isNotBlank()) channels.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            } else emptyList()
            else -> if (activeGroup == "All") channels else channels.filter { it.group == activeGroup }
        }
    }

    val bg = MaterialTheme.colorScheme.background
    val onBg = MaterialTheme.colorScheme.onBackground

    Scaffold(
        containerColor = bg,
        bottomBar = {
            // Web app bottom nav — exactly mirrors mobile nav bar in web
            NavigationBar(
                containerColor = if (isDark)
                    ZColors.Neutral950.copy(alpha = 0.92f)
                else
                    ZColors.White.copy(alpha = 0.92f),
                tonalElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(
                            1.dp,
                            if (isDark) ZColors.White.copy(alpha = 0.08f) else ZColors.Neutral200
                        )
                    )
            ) {
                // Live status dot at top center — matches web
                NavigationBarItem(
                    selected = currentTab == "home",
                    onClick = { currentTab = "home" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(20.dp)) },
                    label = {
                        Text(
                            "Home",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ZColors.Red500,
                        selectedTextColor = ZColors.Red500,
                        unselectedIconColor = if (isDark) ZColors.Neutral500 else ZColors.Neutral500,
                        unselectedTextColor = if (isDark) ZColors.Neutral500 else ZColors.Neutral500,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = currentTab == "favorites",
                    onClick = { currentTab = "favorites" },
                    icon = {
                        Icon(
                            if (currentTab == "favorites") Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorites",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            "Favorites",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ZColors.Red500,
                        selectedTextColor = ZColors.Red500,
                        unselectedIconColor = ZColors.Neutral500,
                        unselectedTextColor = ZColors.Neutral500,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = currentTab == "search",
                    onClick = { currentTab = "search" },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(20.dp)) },
                    label = {
                        Text(
                            "Search",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ZColors.Red500,
                        selectedTextColor = ZColors.Red500,
                        unselectedIconColor = ZColors.Neutral500,
                        unselectedTextColor = ZColors.Neutral500,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 12.dp, end = 12.dp, top = 0.dp, bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ─── Hero Section ──────────────────────────────────────────
                item(span = { GridItemSpan(maxLineSpan) }) {
                    HeroSection(isDark = isDark, channelCount = channels.size)
                }

                // ─── Search Bar (when search tab active) ──────────────────
                if (currentTab == "search") {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 0.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            placeholder = {
                                Text(
                                    "Search channels...",
                                    color = ZColors.Neutral500,
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = ZColors.Neutral500,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = onBg,
                                unfocusedTextColor = onBg,
                                focusedPlaceholderColor = ZColors.Neutral500,
                                unfocusedPlaceholderColor = ZColors.Neutral500,
                                focusedBorderColor = ZColors.Red600,
                                unfocusedBorderColor = if (isDark) ZColors.White.copy(alpha = 0.1f) else ZColors.Neutral200,
                                focusedContainerColor = if (isDark) ZColors.Neutral900 else ZColors.White,
                                unfocusedContainerColor = if (isDark) ZColors.Neutral900 else ZColors.White,
                            )
                        )
                    }
                }

                // ─── Group Chips (when home tab active) ────────────────────
                if (currentTab == "home") {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        val chipState = rememberLazyListState()
                        LazyRow(
                            state = chipState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            flingBehavior = rememberSnapFlingBehavior(chipState)
                        ) {
                            items(groups) { group ->
                                val isSelected = activeGroup == group && currentTab == "home"
                                Box(
                                    modifier = Modifier
                                        .bounceClick { activeGroup = group; currentTab = "home" }
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            if (isSelected) ZColors.Red600
                                            else if (isDark) ZColors.Neutral900.copy(alpha = 0.6f)
                                            else ZColors.White
                                        )
                                        .then(
                                            if (!isSelected) Modifier.border(
                                                1.dp,
                                                if (isDark) ZColors.White.copy(0.1f) else ZColors.Neutral200,
                                                RoundedCornerShape(50)
                                            ) else Modifier
                                        )
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = group.uppercase(),
                                        color = if (isSelected) ZColors.White
                                        else if (isDark) ZColors.Neutral400 else ZColors.Neutral600,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // ─── Search Results Header ─────────────────────────────────
                if (currentTab == "search" && searchQuery.isNotBlank()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "SEARCH RESULTS",
                                color = ZColors.Red500,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(Modifier.width(16.dp))
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(ZColors.Red500.copy(alpha = 0.1f))
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                "${filteredChannels.size} FOUND",
                                fontSize = 9.sp,
                                color = if (isDark) ZColors.Neutral600 else ZColors.Neutral500,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                // ─── Loading ───────────────────────────────────────────────
                if (isLoading) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = ZColors.Red600,
                                modifier = Modifier.size(40.dp),
                                strokeWidth = 3.dp
                            )
                        }
                    }
                }

                // ─── Empty State ───────────────────────────────────────────
                else if (filteredChannels.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .border(
                                    2.dp,
                                    if (isDark) ZColors.White.copy(0.05f) else ZColors.Neutral200,
                                    RoundedCornerShape(24.dp)
                                )
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "NO MATCHING CHANNELS.",
                                fontSize = 12.sp,
                                color = if (isDark) ZColors.Neutral600 else ZColors.Neutral500,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }

                // ─── Channel Grid ──────────────────────────────────────────
                else {
                    items(
                        items = filteredChannels.take(displayedItemCount),
                        key = { it.id }
                    ) { channel ->
                        ChannelMiniCard(
                            channel = channel,
                            isDark = isDark,
                            isFavorite = favorites.contains(channel.id),
                            onToggleFavorite = { id ->
                                favorites = if (favorites.contains(id)) favorites - id else favorites + id
                            },
                            onPlay = onPlay
                        )
                    }

                    // Load More
                    if (filteredChannels.size > displayedItemCount) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .bounceClick { displayedItemCount += 40 }
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            if (isDark) ZColors.White.copy(0.05f)
                                            else ZColors.Neutral200
                                        )
                                        .border(
                                            1.dp,
                                            if (isDark) ZColors.White.copy(0.1f) else ZColors.Neutral300,
                                            RoundedCornerShape(50)
                                        )
                                        .padding(horizontal = 40.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        "LOAD MORE CHANNELS",
                                        color = if (isDark) ZColors.White else ZColors.Neutral700,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 2.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // ─── Footer ───────────────────────────────────────────────
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ZestyyFooter(isDark = isDark)
                }
            }

            // ─── Floating Info Popup (matching web) ───────────────────────
            AnimatedVisibility(
                visible = showPopup,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 56.dp, start = 16.dp, end = 100.dp) // leave room for theme button
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) ZColors.Neutral900 else ZColors.White
                    ),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        1.dp,
                        if (isDark) ZColors.White.copy(alpha = 0.08f) else ZColors.Neutral200
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(ZColors.Amber500.copy(0.12f), CircleShape)
                                .padding(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = ZColors.Amber500,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "If streams fail, visit ",
                                fontSize = 10.sp,
                                color = if (isDark) ZColors.Neutral300 else ZColors.Neutral600,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "nikkitv.vercel.app",
                                fontSize = 10.sp,
                                color = ZColors.Amber500,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.clickable {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse("https://nikkitv.vercel.app"))
                                    )
                                }
                            )
                        }
                        IconButton(
                            onClick = { showPopup = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Close",
                                tint = if (isDark) ZColors.Neutral500 else ZColors.Neutral400,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // ─── Theme Toggle Pill (top-right, matching web) ──────────────
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 56.dp, end = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .bounceClick { onThemeToggle() }
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isDark) ZColors.White.copy(0.1f)
                            else ZColors.Neutral900.copy(0.85f)
                        )
                        .border(
                            1.dp,
                            if (isDark) ZColors.White.copy(0.15f) else ZColors.Neutral700.copy(0.15f),
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isDark) "LIGHT MODE" else "DARK MODE",
                        color = ZColors.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 8.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

// ─── Hero Section ─────────────────────────────────────────────────────────────

@Composable
fun HeroSection(isDark: Boolean, channelCount: Int) {
    val context = LocalContext.current

    // Pulsing live dot animation
    val infiniteTransition = rememberInfiniteTransition(label = "hero_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .height(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(ZColors.Neutral900, Color.Black)))
    ) {
        // Subtle grid texture like web
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSize = 24.dp.toPx()
            val lineColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.04f)
            val strokeWidth = 1f
            var x = 0f
            while (x < size.width) {
                drawLine(lineColor, androidx.compose.ui.geometry.Offset(x, 0f), androidx.compose.ui.geometry.Offset(x, size.height), strokeWidth)
                x += gridSize
            }
            var y = 0f
            while (y < size.height) {
                drawLine(lineColor, androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), strokeWidth)
                y += gridSize
            }
        }

        // Gradient overlays matching web exactly
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = 0.9f), Color.Black.copy(alpha = 0.4f), Color.Transparent)
                )
            )
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent, Color.Transparent)
                )
            )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            // Badges row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LIVE NOW badge
                Box(
                    modifier = Modifier
                        .background(ZColors.Red600.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .border(1.dp, ZColors.Red600.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(ZColors.Red500.copy(alpha = pulseAlpha), CircleShape)
                        )
                        Text(
                            "LIVE NOW",
                            color = ZColors.Red500,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                // ULTRA HD
                Box(
                    modifier = Modifier
                        .background(ZColors.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        "ULTRA HD",
                        color = ZColors.White.copy(0.8f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                // ₹0 COST
                Box(
                    modifier = Modifier
                        .background(ZColors.Green400.copy(0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        "₹0 COST",
                        color = ZColors.Green400,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Title — matches web h2 font-black tracking-tighter
            Text(
                "zestyysports",
                color = ZColors.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1.5).sp,
                lineHeight = 40.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Worldwide channels in HD, Ad-free, 4K — for free. Access ${if (channelCount > 0) channelCount else "premium"} channels instantly.",
                color = ZColors.Neutral300,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(18.dp))

            // CTA Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Telegram button
                Box(
                    modifier = Modifier
                        .bounceClick {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/+0sACDI0bSDI2Njg9"))
                            )
                        }
                        .shadow(8.dp, RoundedCornerShape(8.dp), spotColor = ZColors.Blue500)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.horizontalGradient(listOf(ZColors.Blue500, ZColors.Sky400)))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = ZColors.Neutral950
                        )
                        Text(
                            "TELEGRAM",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = ZColors.Neutral950,
                            letterSpacing = 1.5.sp
                        )
                    }
                }

                // Instagram button
                Box(
                    modifier = Modifier
                        .bounceClick {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/nikkk.exe"))
                            )
                        }
                        .shadow(8.dp, RoundedCornerShape(8.dp), spotColor = ZColors.Emerald400)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.horizontalGradient(listOf(ZColors.Emerald400, ZColors.Cyan500)))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = ZColors.Neutral950
                        )
                        Text(
                            "@NIKKK.EXE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = ZColors.Neutral950,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }
        }
    }
}

// ─── Channel Mini Card ────────────────────────────────────────────────────────

@Composable
fun ChannelMiniCard(
    channel: M3UItem,
    isDark: Boolean,
    isFavorite: Boolean,
    onToggleFavorite: (String) -> Unit,
    onPlay: (M3UItem) -> Unit
) {
    // Pulsing live dot per card
    val infiniteTransition = rememberInfiniteTransition(label = "card_pulse_${channel.id}")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cardPulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick { onPlay(channel) },
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) ZColors.Neutral900.copy(0.4f) else ZColors.White
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (isDark) ZColors.White.copy(0.05f) else ZColors.Neutral200
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Thumbnail area — 16:9 aspect ratio like web
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDark) ZColors.Neutral950 else ZColors.Neutral100),
                contentAlignment = Alignment.Center
            ) {
                if (channel.logo.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(channel.logo)
                            .crossfade(300)
                            .build(),
                        contentDescription = channel.name,
                        contentScale = ContentScale.Inside,
                        modifier = Modifier.fillMaxSize().padding(12.dp)
                    )
                } else {
                    Text(
                        channel.name,
                        color = if (isDark) ZColors.Neutral600 else ZColors.Neutral500,
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Favorite heart (top-left, matching web)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(5.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(0.4f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onToggleFavorite(channel.id) }
                        .padding(5.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) ZColors.Red500 else ZColors.White,
                        modifier = Modifier.size(10.dp)
                    )
                }

                // LIVE badge (top-right, pulsing dot)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .background(ZColors.Red600.copy(0.1f), RoundedCornerShape(3.dp))
                        .border(1.dp, ZColors.Red600.copy(0.2f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            Modifier
                                .size(4.dp)
                                .background(ZColors.Red500.copy(pulseAlpha), CircleShape)
                        )
                        Text(
                            "LIVE",
                            color = ZColors.Red500,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Channel name + AD-FREE badge (matches web exactly)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    channel.name,
                    color = if (isDark) ZColors.White else ZColors.Neutral800,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .background(ZColors.Blue500.copy(0.12f), RoundedCornerShape(3.dp))
                        .border(1.dp, ZColors.Blue500.copy(0.2f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        "AD-FREE",
                        color = ZColors.Blue500,
                        fontSize = 6.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.3.sp
                    )
                }
            }

            if (channel.group.isNotEmpty()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    channel.group.uppercase(),
                    color = if (isDark) ZColors.Neutral600 else ZColors.Neutral500,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─── Video Player Screen ───────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoPlayerScreen(
    channel: M3UItem,
    allChannels: List<M3UItem>,
    isDark: Boolean,
    onOrientationChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onChannelSelect: (M3UItem) -> Unit
) {
    val context = LocalContext.current
    var isBuffering by remember { mutableStateOf(true) }
    var showJoinPopup by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(false) }
    var showStatsForNerds by remember { mutableStateOf(false) }

    val bandwidthMeter = remember {
        androidx.media3.exoplayer.upstream.DefaultBandwidthMeter.Builder(context).build()
    }

    val exoPlayer = remember {
        val loadControl = androidx.media3.exoplayer.DefaultLoadControl.Builder()
            .setBufferDurationsMs(50_000, 120_000, 1_500, 3_000)
            .setBackBuffer(0, false)
            .build()

        val trackSelector = androidx.media3.exoplayer.trackselection.DefaultTrackSelector(context).apply {
            setParameters(
                buildUponParameters()
                    .setForceHighestSupportedBitrate(false)
                    .setMaxVideoBitrate(Int.MAX_VALUE)
            )
        }

        ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .setBandwidthMeter(bandwidthMeter)
            .setTrackSelector(trackSelector)
            .build()
    }

    DisposableEffect(exoPlayer) {
        val listener = object : androidx.media3.common.Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == androidx.media3.common.Player.STATE_BUFFERING
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    var lastBufferToast by remember { mutableStateOf(0L) }
    LaunchedEffect(isBuffering) {
        if (isBuffering && exoPlayer.currentPosition > 0) {
            val now = System.currentTimeMillis()
            if (now - lastBufferToast > 30_000) {
                android.widget.Toast.makeText(
                    context,
                    "Low buffer. Connection unstable.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                lastBufferToast = now
            }
        }
    }

    LaunchedEffect(channel) {
        showJoinPopup = true
        val dataSourceFactory = androidx.media3.datasource.DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .setConnectTimeoutMs(60_000)
            .setReadTimeoutMs(60_000)
            .setAllowCrossProtocolRedirects(true)

        val mediaSource = androidx.media3.exoplayer.source.DefaultMediaSourceFactory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(channel.url))

        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
            onOrientationChange(false)
        }
    }

    LaunchedEffect(showControls) {
        if (showControls) {
            delay(4_000)
            showControls = false
        }
    }

    BackHandler { onBack() }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Column(
        Modifier
            .fillMaxSize()
            .background(if (isLandscape) Color.Black else MaterialTheme.colorScheme.background)
            .then(if (isLandscape) Modifier else Modifier.statusBarsPadding())
    ) {
        // ─── Player Box ─────────────────────────────────────────────────────
        Box(
            modifier = if (isLandscape) Modifier.fillMaxSize()
            else Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black)
        ) {
            // ExoPlayer surface via layout XML
            AndroidView(
                factory = {
                    (android.view.LayoutInflater.from(context)
                        .inflate(R.layout.exo_texture_view, null, false) as PlayerView)
                        .apply {
                            player = exoPlayer
                            useController = false
                            layoutParams = android.view.ViewGroup.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                },
                update = { view -> if (view.player != exoPlayer) view.player = exoPlayer },
                modifier = Modifier.fillMaxSize()
            )

            // Touch overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showControls = !showControls }
            )

            // Buffering overlay — pulsing logo like web
            if (isBuffering) {
                var pulse by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { while (true) { pulse = !pulse; delay(500) } }
                val bufAlpha by animateFloatAsState(
                    if (pulse) 1f else 0.4f,
                    tween(500),
                    label = "bufAlpha"
                )
                Box(
                    Modifier.matchParentSize().background(Color.Black.copy(0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(R.drawable.zestyy_logo),
                        contentDescription = "Buffering",
                        modifier = Modifier.height(48.dp),
                        alpha = bufAlpha
                    )
                }
            } else {
                // Watermark
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(12.dp)
                        .then(if (isLandscape) Modifier.statusBarsPadding() else Modifier),
                    contentAlignment = Alignment.TopEnd
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(R.drawable.zestyy_logo),
                        contentDescription = null,
                        modifier = Modifier.height(28.dp),
                        alpha = 0.6f
                    )
                }

                // Stats for nerds panel
                if (showStatsForNerds) {
                    var currentPos by remember { mutableStateOf(0L) }
                    var bufferedPos by remember { mutableStateOf(0L) }
                    var bwKbps by remember { mutableStateOf(0L) }
                    LaunchedEffect(Unit) {
                        while (true) {
                            currentPos = exoPlayer.currentPosition
                            bufferedPos = exoPlayer.bufferedPosition
                            bwKbps = bandwidthMeter.bitrateEstimate / 8192L
                            delay(1_000)
                        }
                    }
                    val vf = exoPlayer.videoFormat
                    val w = vf?.width ?: 1920
                    val h = vf?.height ?: 1080
                    val res = if (w > 0) "${w}x${h}" else "1920x1080"
                    val kb = vf?.bitrate?.takeIf { it > 0 }?.let { it / 1000 }?.toLong() ?: bwKbps
                    val dataMB = (kb * (currentPos / 1000f)) / 8192f

                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .statusBarsPadding()
                            .align(Alignment.TopStart)
                            .background(Color.Black.copy(0.85f))
                            .border(1.dp, ZColors.White.copy(0.2f))
                            .padding(14.dp)
                            .widthIn(min = 280.dp)
                    ) {
                        Column {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text("Stats for nerds", color = ZColors.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { showStatsForNerds = false }, Modifier.size(20.dp)) {
                                    Icon(Icons.Default.Clear, null, tint = ZColors.White, modifier = Modifier.size(14.dp))
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            listOf(
                                "Resolution" to res,
                                "Buffer Health" to "${bufferedPos / 1000} s",
                                "Network" to "$bwKbps KB/s",
                                "Data Used" to "${String.format("%.1f", dataMB)} MB"
                            ).forEach { (k, v) ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), Arrangement.SpaceBetween) {
                                    Text(k, color = ZColors.White.copy(0.6f), fontSize = 10.sp)
                                    Text(v, color = ZColors.White, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            // ─── Custom Controls Overlay ─────────────────────────────────
            if (showControls) {
                var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }
                var isMuted by remember { mutableStateOf(exoPlayer.volume == 0f) }
                var streamPos by remember { mutableStateOf(0L) }
                LaunchedEffect(Unit) {
                    while (true) { streamPos = exoPlayer.currentPosition; delay(1_000) }
                }

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(0.4f))
                ) {
                    // Top — Back + Channel Name
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .then(if (isLandscape) Modifier.statusBarsPadding() else Modifier),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, null, tint = ZColors.White)
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            channel.name,
                            color = ZColors.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Bottom — Controls bar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(0.85f))
                                )
                            )
                    ) {
                        Column {
                            // Full-width red progress bar (live)
                            Box(
                                Modifier.fillMaxWidth().height(3.dp)
                                    .background(ZColors.White.copy(0.2f))
                            ) {
                                Box(Modifier.fillMaxWidth().height(3.dp).background(ZColors.Red500))
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .then(if (isLandscape) Modifier.navigationBarsPadding() else Modifier),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                                            isPlaying = !isPlaying
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            null,
                                            tint = ZColors.White
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            isMuted = !isMuted
                                            exoPlayer.volume = if (isMuted) 0f else 1f
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                            null,
                                            tint = ZColors.White
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    // Live timer + dot
                                    val secs = (streamPos / 1000) % 60
                                    val mins = (streamPos / (1000 * 60)) % 60
                                    val hrs = streamPos / (1000 * 60 * 60)
                                    val timeStr = if (hrs > 0) "%d:%02d:%02d".format(hrs, mins, secs)
                                    else "%02d:%02d".format(mins, secs)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(5.dp).background(ZColors.Red500, CircleShape))
                                        Spacer(Modifier.width(5.dp))
                                        Text(timeStr, color = ZColors.White, fontSize = 12.sp)
                                        Spacer(Modifier.width(6.dp))
                                        Text("LIVE", color = ZColors.White.copy(0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { showStatsForNerds = !showStatsForNerds },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Info, null, tint = ZColors.White)
                                    }
                                    IconButton(
                                        onClick = { onOrientationChange(!isLandscape) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            if (isLandscape) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                            null,
                                            tint = ZColors.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } // End Player Box

        // ─── Portrait Below-Player Area ──────────────────────────────────
        if (!isLandscape) {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Channel Info header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                // Badges row
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    LiveBadge()
                                    Text(
                                        "${channel.group} · ULTRA HD",
                                        color = if (isDark) ZColors.Neutral500 else ZColors.Neutral500,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Text(
                                    channel.name.uppercase(),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 24.sp
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            // AD-FREE badge
                            Box(
                                modifier = Modifier
                                    .background(ZColors.Blue500.copy(0.12f), RoundedCornerShape(50))
                                    .border(1.dp, ZColors.Blue500.copy(0.2f), RoundedCornerShape(50))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Shield,
                                        null,
                                        tint = ZColors.Blue500,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        "Ad-Free",
                                        color = ZColors.Blue500,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Fallback info bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isDark) ZColors.Neutral900.copy(0.6f) else ZColors.White,
                                    RoundedCornerShape(50)
                                )
                                .border(
                                    1.dp,
                                    ZColors.Amber500.copy(0.2f),
                                    RoundedCornerShape(50)
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    null,
                                    tint = ZColors.Amber500,
                                    modifier = Modifier.size(14.dp)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Streams not working? Visit ",
                                        fontSize = 10.sp,
                                        color = if (isDark) ZColors.Neutral300 else ZColors.Neutral700
                                    )
                                    Text(
                                        "nikkitv.vercel.app",
                                        fontSize = 10.sp,
                                        color = ZColors.Amber500,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.clickable {
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW, Uri.parse("https://nikkitv.vercel.app"))
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // "MORE CHANNELS" label
                        Text(
                            "MORE CHANNELS",
                            fontSize = 11.sp,
                            color = if (isDark) ZColors.Neutral500 else ZColors.Neutral500,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(Modifier.height(10.dp))

                        // Horizontal channel strip — exact match of web scrollable channel bar
                        val stripState = rememberLazyListState()
                        LaunchedEffect(channel) {
                            val idx = allChannels.take(30).indexOfFirst { it.id == channel.id }
                            if (idx >= 0) stripState.animateScrollToItem(idx)
                        }
                        LazyRow(
                            state = stripState,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            flingBehavior = rememberSnapFlingBehavior(stripState),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(allChannels.take(30), key = { it.id }) { ch ->
                                val isPlaying = ch.id == channel.id
                                Box(
                                    modifier = Modifier
                                        .width(if (isPlaying) 140.dp else 100.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isDark) MaterialTheme.colorScheme.surface
                                            else ZColors.White
                                        )
                                        .border(
                                            if (isPlaying) 2.dp else 1.dp,
                                            if (isPlaying) ZColors.Red500
                                            else if (isDark) ZColors.White.copy(0.1f) else ZColors.Neutral200,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .then(if (isPlaying) Modifier.shadow(8.dp, RoundedCornerShape(12.dp), spotColor = ZColors.Red500.copy(0.3f)) else Modifier)
                                        .bounceClick { onChannelSelect(ch) }
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(16f / 9f)
                                                .background(MaterialTheme.colorScheme.background),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (ch.logo.isNotEmpty()) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(ch.logo)
                                                        .crossfade(300)
                                                        .build(),
                                                    contentDescription = ch.name,
                                                    contentScale = ContentScale.Inside,
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                            } else {
                                                Text(
                                                    ch.name.take(3).uppercase(),
                                                    color = if (isDark) ZColors.Neutral600 else ZColors.Neutral400,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 14.sp
                                                )
                                            }
                                            if (isPlaying) {
                                                Box(
                                                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                                                ) {
                                                    LiveBadge(small = true)
                                                }
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.Black)
                                                .padding(5.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    ch.name,
                                                    color = ZColors.White,
                                                    fontSize = if (isPlaying) 10.sp else 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    textAlign = TextAlign.Center
                                                )
                                                if (isPlaying) {
                                                    Text(
                                                        "Playing",
                                                        color = ZColors.Red500,
                                                        fontSize = 7.sp,
                                                        fontWeight = FontWeight.Black,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Player Footer
                item {
                    PlayerFooter(isDark = isDark)
                }

                // Full Footer
                item {
                    ZestyyFooter(isDark = isDark)
                }
            }
        }
    }

    // ─── Join Telegram Popup (player) ───────────────────────────────────────
    if (showJoinPopup && !isLandscape) {
        Dialog(onDismissRequest = { showJoinPopup = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isDark) ZColors.Neutral900 else ZColors.White)
                    .border(
                        1.dp,
                        if (isDark) ZColors.White.copy(0.1f) else ZColors.Neutral200,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Blue send icon circle — matches web
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(ZColors.Blue500.copy(0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Send,
                            null,
                            tint = ZColors.Blue500,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "JOIN TELEGRAM",
                        color = if (isDark) ZColors.White else ZColors.Neutral900,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Join our official Telegram group for updates, support, and to request channels. Also, visit ZestyyFlix for movies!",
                        color = if (isDark) ZColors.Neutral400 else ZColors.Neutral600,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(24.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Open Telegram
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .bounceClick {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/+0sACDI0bSDI2Njg9"))
                                    )
                                    showJoinPopup = false
                                }
                                .clip(RoundedCornerShape(50))
                                .background(Brush.horizontalGradient(listOf(ZColors.Blue500, ZColors.Sky400)))
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Send, null, tint = ZColors.Neutral950, modifier = Modifier.size(16.dp))
                                Text(
                                    "OPEN TELEGRAM",
                                    color = ZColors.Neutral950,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        // Visit ZestyyFlix
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .bounceClick {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse("https://zestyyflix.vercel.app"))
                                    )
                                    showJoinPopup = false
                                }
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isDark) ZColors.Neutral800 else ZColors.Neutral100
                                )
                                .border(
                                    1.dp,
                                    if (isDark) ZColors.White.copy(0.05f) else ZColors.Neutral200,
                                    RoundedCornerShape(50)
                                )
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    null,
                                    tint = if (isDark) ZColors.White else ZColors.Neutral900,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "VISIT ZESTYYFLIX",
                                    color = if (isDark) ZColors.White else ZColors.Neutral900,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Close button
                    Box(
                        modifier = Modifier
                            .bounceClick { showJoinPopup = false }
                    ) {
                        Text(
                            "Close & Watch Stream",
                            color = if (isDark) ZColors.Neutral500 else ZColors.Neutral500,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

// ─── Reusable Components ──────────────────────────────────────────────────────

@Composable
fun LiveBadge(small: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition(label = "live_badge")
    val pulseAlpha by infiniteTransition.animateFloat(
        0.4f, 1f,
        infiniteRepeatable(tween(750, easing = LinearEasing), RepeatMode.Reverse),
        label = "livePulse"
    )
    Box(
        modifier = Modifier
            .background(ZColors.Red600.copy(0.1f), RoundedCornerShape(3.dp))
            .border(1.dp, ZColors.Red600.copy(0.2f), RoundedCornerShape(3.dp))
            .padding(horizontal = if (small) 4.dp else 6.dp, vertical = if (small) 1.dp else 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Box(
                Modifier.size(if (small) 3.dp else 4.dp)
                    .background(ZColors.Red500.copy(pulseAlpha), CircleShape)
            )
            Text(
                "LIVE",
                color = ZColors.Red500,
                fontSize = if (small) 6.sp else 8.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun PlayerFooter(isDark: Boolean) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isDark) ZColors.Neutral900 else ZColors.White
            )
            .border(
                BorderStroke(
                    1.dp,
                    if (isDark) ZColors.White.copy(0.08f) else ZColors.Neutral200
                )
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            // Telegram link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.clickable {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/+0sACDI0bSDI2Njg9")))
                }
            ) {
                Icon(Icons.Default.Send, null, tint = ZColors.Blue400, modifier = Modifier.size(12.dp))
                Text("Join Telegram", color = ZColors.Blue400, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
            }
            // Instagram link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.clickable {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/nikkk.exe")))
                }
            ) {
                Icon(Icons.Default.Person, null, tint = ZColors.Green400, modifier = Modifier.size(12.dp))
                Text("@nikkk.exe", color = ZColors.Green400, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
            }
            // ZestyyFlix link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.clickable {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://zestyyflix.vercel.app")))
                }
            ) {
                Icon(Icons.Default.OpenInNew, null, tint = ZColors.Amber500, modifier = Modifier.size(12.dp))
                Text("zestyyflix.vercel.app", color = ZColors.Amber500, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
            }
            Text(
                "adfree by Nikshep",
                color = if (isDark) ZColors.Neutral600 else ZColors.Neutral400,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun ZestyyFooter(isDark: Boolean) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(R.drawable.zestyy_logo),
            contentDescription = "ZestyySports",
            modifier = Modifier.height(36.dp)
        )
        Spacer(Modifier.height(14.dp))
        Text(
            "WORLDWIDE CHANNELS IN HD, AD-FREE, 4K — FOR FREE.",
            color = if (isDark) ZColors.Neutral400 else ZColors.Neutral600,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            letterSpacing = 0.8.sp,
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            // Telegram
            Box(
                modifier = Modifier
                    .bounceClick { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/+0sACDI0bSDI2Njg9"))) }
                    .shadow(12.dp, RoundedCornerShape(8.dp), spotColor = ZColors.Blue500)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.horizontalGradient(listOf(ZColors.Blue500, ZColors.Sky400)))
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Send, null, modifier = Modifier.size(14.dp), tint = ZColors.Neutral950)
                    Text("TELEGRAM", fontSize = 10.sp, fontWeight = FontWeight.Black, color = ZColors.Neutral950, letterSpacing = 1.5.sp)
                }
            }

            // Instagram
            Box(
                modifier = Modifier
                    .bounceClick { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/nikkk.exe"))) }
                    .shadow(12.dp, RoundedCornerShape(8.dp), spotColor = ZColors.Emerald400)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.horizontalGradient(listOf(ZColors.Emerald400, ZColors.Cyan500)))
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = ZColors.Neutral950)
                    Text("@NIKKK.EXE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = ZColors.Neutral950, letterSpacing = 1.5.sp)
                }
            }

            // ZestyyFlix
            Box(
                modifier = Modifier
                    .bounceClick { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://zestyyflix.vercel.app"))) }
                    .border(1.dp, if (isDark) ZColors.White.copy(0.1f) else ZColors.Neutral200, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDark) ZColors.Neutral900 else ZColors.White)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(14.dp), tint = if (isDark) ZColors.White else ZColors.Neutral900)
                    Text(
                        "MORE FROM ZESTYY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isDark) ZColors.White else ZColors.Neutral900,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }
    }
}

// ─── Utilities ────────────────────────────────────────────────────────────────

// Bounce-click modifier — smooth spring animation matching web hover transforms
fun Modifier.bounceClick(onClick: () -> Unit) = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounceClickScale"
    )
    val currentOnClick by rememberUpdatedState(onClick)

    this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                },
                onTap = { currentOnClick() }
            )
        }
}

// M3U parser
fun parseM3U(content: String): List<M3UItem> {
    val items = mutableListOf<M3UItem>()
    val lines = content.split("\n")
    var name = ""
    var logo = ""
    var group = ""

    for (line in lines) {
        val t = line.trim()
        if (t.startsWith("#EXTINF:")) {
            group = Regex("""group-title="([^"]+)"""").find(t)?.groupValues?.get(1) ?: ""
            logo = Regex("""tvg-logo="([^"]+)"""").find(t)?.groupValues?.get(1) ?: ""
            name = t.split(",").getOrNull(1)?.trim() ?: ""
        } else if (t.isNotEmpty() && !t.startsWith("#")) {
            items.add(
                M3UItem(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
                    logo = logo,
                    group = group,
                    url = t
                )
            )
            name = ""; logo = ""; group = ""
        }
    }
    return items
}
