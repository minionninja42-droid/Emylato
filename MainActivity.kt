package com.aether.launcher

import android.app.WallpaperManager
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF090B0F)
private val Panel = Color(0xFF171B22)
private val Muted = Color(0xFFADB4C0)
private val White = Color(0xFFF5F6F8)

data class LaunchableApp(val label: String, val packageName: String, val icon: android.graphics.drawable.Drawable)

class MainActivity : ComponentActivity() {
    private var apps by mutableStateOf<List<LaunchableApp>>(emptyList())
    private var selectedWallpaperUri: Uri? = null

    private val wallpaperPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) WallpaperManager.getInstance(this).setBitmap(bitmap)
                }
            } catch (_: Exception) { }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshApps()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                background = Ink, surface = Panel, primary = Color(0xFFDDE3EE),
                onBackground = White, onSurface = White
            )) {
                AetherApp(
                    apps = apps,
                    onLaunch = { packageName ->
                        packageManager.getLaunchIntentForPackage(packageName)?.let(::startActivity)
                    },
                    onWallpaper = { wallpaperPicker.launch(arrayOf("image/*")) },
                    onSetDefault = { openHomeSettings() }
                )
            }
        }
    }

    private fun refreshApps() {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        apps = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .mapNotNull { info ->
                val activity = info.activityInfo ?: return@mapNotNull null
                LaunchableApp(
                    info.loadLabel(packageManager).toString(),
                    activity.packageName,
                    info.loadIcon(packageManager)
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    private fun openHomeSettings() {
        try {
            startActivity(Intent(android.provider.Settings.ACTION_HOME_SETTINGS))
        } catch (_: Exception) {
            startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
        }
    }
}

@Composable
private fun AetherApp(
    apps: List<LaunchableApp>,
    onLaunch: (String) -> Unit,
    onWallpaper: () -> Unit,
    onSetDefault: () -> Unit
) {
    var page by remember { mutableStateOf("home") }
    var query by remember { mutableStateOf("") }
    var themeName by remember { mutableStateOf("Mono Forest") }
    var showAllApps by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(Color(0xFF202832), Ink, Color(0xFF08090B)))
    )) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp)) {
            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("AETHER", color = Muted, fontSize = 12.sp, letterSpacing = 4.sp, fontWeight = FontWeight.SemiBold)
                Icon(Icons.Default.Wifi, contentDescription = "Сеть", tint = Muted)
            }
            when (page) {
                "home" -> HomePage(
                    apps = apps, onLaunch = onLaunch,
                    onOpenApps = { page = "apps" },
                    onThemes = { page = "themes" },
                    onSettings = { page = "settings" },
                    onWallpaper = onWallpaper
                )
                "apps" -> AppsPage(apps, query, { query = it }, onLaunch, { page = "home" })
                "themes" -> ThemesPage(themeName, { themeName = it }, { page = "home" })
                else -> SettingsPage(onSetDefault, onWallpaper, { page = "home" })
            }
            Spacer(Modifier.weight(1f))
            NavigationBar(containerColor = Color(0xCC11151B), contentColor = White) {
                NavigationBarItem(selected = page == "home", onClick = { page = "home" }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Домой") })
                NavigationBarItem(selected = page == "apps", onClick = { page = "apps" }, icon = { Icon(Icons.Default.GridView, null) }, label = { Text("Приложения") })
                NavigationBarItem(selected = page == "themes", onClick = { page = "themes" }, icon = { Icon(Icons.Default.Palette, null) }, label = { Text("Темы") })
                NavigationBarItem(selected = page == "settings", onClick = { page = "settings" }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Настройки") })
            }
        }
    }
}

@Composable
private fun HomePage(
    apps: List<LaunchableApp>, onLaunch: (String) -> Unit,
    onOpenApps: () -> Unit, onThemes: () -> Unit, onSettings: () -> Unit, onWallpaper: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(top = 54.dp)) {
        Text("01:38", color = White, fontSize = 58.sp, fontWeight = FontWeight.ExtraLight)
        Text("ВОСКРЕСЕНЬЕ  •  8 НОЯБРЯ", color = Muted, fontSize = 11.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(30.dp))
        GlassPanel {
            Text("Добро пожаловать", color = White, fontSize = 21.sp, fontWeight = FontWeight.Medium)
            Text("Тихий минимализм. Всё важное — рядом.", color = Muted, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickAction("Обои", Icons.Default.Wallpaper, onWallpaper)
                QuickAction("Темы", Icons.Default.Palette, onThemes)
                QuickAction("Настройки", Icons.Default.Tune, onSettings)
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("ИЗБРАННОЕ", color = Muted, fontSize = 11.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(10.dp))
        val favorites = apps.take(4)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            favorites.forEach { app ->
                Column(Modifier.weight(1f).clickable { onLaunch(app.packageName) }, horizontalAlignment = Alignment.CenterHorizontally) {
                    AppIcon(app)
                    Spacer(Modifier.height(6.dp))
                    Text(app.label, color = White, fontSize = 10.sp, maxLines = 1)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        GlassPanel(Modifier.fillMaxWidth().clickable { onOpenApps() }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Apps, null, tint = White)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Все приложения", color = White, fontWeight = FontWeight.Medium)
                    Text("${apps.size} приложений на устройстве", color = Muted, fontSize = 12.sp)
                }
                Icon(Icons.Default.ArrowForward, null, tint = Muted)
            }
        }
    }
}

@Composable
private fun AppsPage(
    apps: List<LaunchableApp>, query: String, onQuery: (String) -> Unit,
    onLaunch: (String) -> Unit, onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(top = 26.dp)) {
        Text("Приложения", color = White, fontSize = 30.sp, fontWeight = FontWeight.Light)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = query, onValueChange = onQuery, modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Поиск приложений") }, leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true, shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = White, unfocusedTextColor = White,
                focusedBorderColor = Muted, unfocusedBorderColor = Color.DarkGray
            )
        )
        Spacer(Modifier.height(12.dp))
        val filtered = apps.filter { it.label.contains(query, ignoreCase = true) }
        LazyVerticalGrid(columns = GridCells.Fixed(4), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            items(filtered) { app ->
                Column(Modifier.clickable { onLaunch(app.packageName) }.padding(5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    AppIcon(app)
                    Spacer(Modifier.height(6.dp))
                    Text(app.label, color = White, fontSize = 10.sp, maxLines = 2)
                }
            }
        }
    }
}

@Composable
private fun ThemesPage(current: String, onChoose: (String) -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(top = 26.dp)) {
        Text("Темы", color = White, fontSize = 30.sp, fontWeight = FontWeight.Light)
        Text("Выбери настроение рабочего стола", color = Muted)
        Spacer(Modifier.height(20.dp))
        listOf(
            "Mono Forest" to "Монохром • стекло • глубина",
            "Nothing Dots" to "Точечные акценты и строгая сетка",
            "TECNO Glass" to "Полупрозрачные панели",
            "Midnight OLED" to "Глубокий чёрный"
        ).forEach { (name, description) ->
            GlassPanel(Modifier.fillMaxWidth().padding(bottom = 10.dp).clickable { onChoose(name) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).background(
                        Brush.linearGradient(listOf(Color(0xFF68717D), Color(0xFF101318))), RoundedCornerShape(14.dp)
                    ))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(name, color = White, fontWeight = FontWeight.SemiBold)
                        Text(description, color = Muted, fontSize = 11.sp)
                    }
                    if (current == name) Icon(Icons.Default.CheckCircle, null, tint = White)
                }
            }
        }
        Text("Активная тема: $current", color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun SettingsPage(onSetDefault: () -> Unit, onWallpaper: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(top = 26.dp)) {
        Text("Настройки", color = White, fontSize = 30.sp, fontWeight = FontWeight.Light)
        Spacer(Modifier.height(18.dp))
        SettingRow("Сделать лаунчером по умолчанию", "Открыть системный выбор домашнего приложения", Icons.Default.Home, onSetDefault)
        SettingRow("Выбрать обои", "Установить изображение рабочего стола", Icons.Default.Image, onWallpaper)
        SettingRow("Виджеты", "В этой версии доступны карточки-заготовки", Icons.Default.Widgets) {}
        SettingRow("Экран блокировки", "Безопасность остаётся под управлением Android", Icons.Default.Lock) {}
        Spacer(Modifier.height(14.dp))
        Text("Aether Launcher 1.0.0 • ранняя сборка", color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun SettingRow(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, action: () -> Unit) {
    GlassPanel(Modifier.fillMaxWidth().padding(bottom = 10.dp).clickable { action() }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Muted)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = White, fontWeight = FontWeight.Medium)
                Text(subtitle, color = Muted, fontSize = 11.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Muted)
        }
    }
}

@Composable
private fun QuickAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, action: () -> Unit) {
    Column(
        Modifier.weight(1f).background(Color(0x332F3742), RoundedCornerShape(15.dp))
            .clickable { action() }.padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = White)
        Spacer(Modifier.height(5.dp))
        Text(label, color = White, fontSize = 11.sp)
    }
}

@Composable
private fun GlassPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.background(Color(0x332A303A), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        content = content
    )
}

@Composable
private fun AppIcon(app: LaunchableApp) {
    Box(
        Modifier.size(54.dp).background(Color(0xFF252B34), RoundedCornerShape(17.dp)),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { context -> android.widget.ImageView(context).apply {
                setImageDrawable(app.icon)
                scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                setPadding(7, 7, 7, 7)
            } },
            modifier = Modifier.fillMaxSize()
        )
    }
}
