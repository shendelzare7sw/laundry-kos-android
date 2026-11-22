package com.laundry_kos.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.laundry_kos.R
import com.laundry_kos.core.designsystem.LaundryKosTheme
import com.laundry_kos.core.designsystem.rememberIsWideLayout
import com.laundry_kos.core.di.AppContainer
import com.laundry_kos.data.repository.LaundryKosRepository
import com.laundry_kos.feature.auth.LoginScreen
import com.laundry_kos.feature.auth.PropertiPickerScreen
import com.laundry_kos.feature.auth.SessionUiState
import com.laundry_kos.feature.auth.SessionViewModel
import com.laundry_kos.feature.auth.SplashScreen
import com.laundry_kos.feature.beranda.BerandaScreen
import com.laundry_kos.feature.beranda.BerandaViewModel
import com.laundry_kos.feature.laporan.LaporanScreen
import com.laundry_kos.feature.laporan.LaporanViewModel
import com.laundry_kos.feature.laundry.LaundryFormScreen
import com.laundry_kos.feature.laundry.LaundryFormViewModel
import com.laundry_kos.feature.laundry.LaundryListScreen
import com.laundry_kos.feature.laundry.LaundryListViewModel
import com.laundry_kos.feature.notifikasi.NotifikasiViewModel
import com.laundry_kos.feature.notifikasi.NotificationBellButton
import com.laundry_kos.feature.notifikasi.NotificationListDialog
import com.laundry_kos.feature.pengaturan.PengaturanScreen
import com.laundry_kos.feature.pengaturan.PengaturanViewModel
import com.laundry_kos.feature.pengguna.PenggunaScreen
import com.laundry_kos.feature.pengguna.PenggunaViewModel
import com.laundry_kos.feature.penghuni.PenghuniDetailScreen
import com.laundry_kos.feature.penghuni.PenghuniDetailViewModel
import com.laundry_kos.feature.penghuni.PenghuniListScreen
import com.laundry_kos.feature.penghuni.PenghuniListViewModel
import com.laundry_kos.feature.properti.PropertiSetupScreen
import com.laundry_kos.feature.properti.PropertiSetupViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class OperasionalTab(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
) {
    BERANDA("beranda", R.string.nav_beranda, Icons.Default.Home),
    LAUNDRY("laundry", R.string.nav_laundry, Icons.Default.LocalLaundryService),
    PENGHUNI("penghuni", R.string.nav_penghuni, Icons.Default.Groups),
    LAPORAN("laporan", R.string.nav_laporan, Icons.Default.Assessment),
}

private const val ROUTE_PENGATURAN = "pengaturan"
private const val ROUTE_PENGGUNA = "pengguna"
private const val ROUTE_LAUNDRY_NEW = "laundry/new"
private const val ROUTE_LAUNDRY_EDIT = "laundry/edit/{id}"
private const val ROUTE_PENGHUNI_DETAIL = "penghuni/detail/{id}"

@Composable
fun LaundryKosRoot(
    container: AppContainer,
) {
    val sessionViewModel: SessionViewModel = viewModel(factory = SessionViewModel.Factory(container))
    val sessionState by sessionViewModel.state.collectAsStateWithLifecycle()
    var minSplashElapsed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1_150L)
        minSplashElapsed = true
    }

    val showSplash = !minSplashElapsed || sessionState is SessionUiState.Checking

    Crossfade(
        targetState = showSplash,
        animationSpec = tween(durationMillis = 320),
        label = "rootSplashCrossfade",
    ) { inSplash ->
        if (inSplash) {
            SplashScreen()
        } else {
            when (val state = sessionState) {
                SessionUiState.Checking -> SplashScreen()
                is SessionUiState.Unauthenticated -> {
                    LoginScreen(
                        uiState = state,
                        onLogin = sessionViewModel::login,
                    )
                }
                is SessionUiState.SelectProperti -> {
                    OwnerGedungNavHost(
                        uiState = state,
                        container = container,
                        sessionViewModel = sessionViewModel,
                    )
                }
                is SessionUiState.Authenticated -> {
                    key(state.propertiAktif.id) {
                        AuthenticatedAppShell(
                            authState = state,
                            container = container,
                            onReleaseProperti = sessionViewModel::releaseProperti,
                            onLogout = sessionViewModel::logout,
                        )
                    }
                }
            }
        }
    }
}

/**
 * NavHost khusus Owner / DevAdmin saat berada di Panel Manajemen Global (sebelum masuk ke properti tertentu):
 * - Daftar Gedung (Pilih, Tambah, Edit)
 * - Setup Gedung (`/properti/{id}/setup`)
 * - Kelola Pengguna (`/pengguna`)
 */
@Composable
private fun OwnerGedungNavHost(
    uiState: SessionUiState.SelectProperti,
    container: AppContainer,
    sessionViewModel: SessionViewModel,
) {
    val repository = container.repository
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "gedung/list",
        enterTransition = { fadeIn(animationSpec = tween(150)) },
        exitTransition = { fadeOut(animationSpec = tween(110)) },
        popEnterTransition = { fadeIn(animationSpec = tween(150)) },
        popExitTransition = { fadeOut(animationSpec = tween(110)) },
    ) {
        composable("gedung/list") {
            PropertiPickerScreen(
                uiState = uiState,
                notificationStore = container.notificationStore,
                onSelect = sessionViewModel::selectProperti,
                onRefresh = sessionViewModel::refreshPropertiList,
                onTambahProperti = sessionViewModel::tambahProperti,
                onUbahProperti = sessionViewModel::ubahProperti,
                onOpenSetup = { propId -> navController.navigate("gedung/setup/$propId") },
                onOpenPengguna = { navController.navigate("gedung/pengguna") },
                onClearDialogError = sessionViewModel::clearPropertiDialogError,
                onClearSnackbar = sessionViewModel::clearSelectPropertiSnackbar,
                onLogout = sessionViewModel::logout,
            )
        }

        composable(
            route = "gedung/setup/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType }),
        ) { entry ->
            val id = entry.arguments?.getInt("id") ?: return@composable
            val vm: PropertiSetupViewModel = viewModel(
                key = "setup_$id",
                factory = PropertiSetupViewModel.Factory(repository, id),
            )
            PropertiSetupScreen(
                viewModel = vm,
                onBack = {
                    sessionViewModel.refreshPropertiList()
                    navController.popBackStack()
                },
                onOpenKelolaPengguna = { navController.navigate("gedung/pengguna") },
            )
        }

        composable("gedung/pengguna") {
            val vm: PenggunaViewModel = viewModel(
                key = "global_pengguna",
                factory = PenggunaViewModel.Factory(repository),
            )
            PenggunaScreen(
                viewModel = vm,
                onBack = {
                    sessionViewModel.refreshPropertiList()
                    navController.popBackStack()
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthenticatedAppShell(
    authState: SessionUiState.Authenticated,
    container: AppContainer,
    onReleaseProperti: () -> Unit,
    onLogout: () -> Unit,
) {
    val repository = container.repository
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route
    val isShellRoute = OperasionalTab.entries.any { it.route == currentRoute } || currentRoute == ROUTE_PENGATURAN
    val isWideLayout = rememberIsWideLayout()
    val ext = LaundryKosTheme.extendedColors

    val isGlobalRole = authState.user.role == "owner" || authState.user.role == "dev_admin"
    val bolehKelolaPengaturan = authState.permissions.kelolaPengaturan
    val bolehKelolaPengguna = authState.permissions.kelolaPengguna

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showNotifDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    val notifikasiViewModel: NotifikasiViewModel = viewModel(
        key = "notifikasi_${authState.propertiAktif.id}",
        factory = NotifikasiViewModel.Factory(
            repository = repository,
            appContext = container.appContext,
            notificationStore = container.notificationStore,
            activitySyncEngine = container.activitySyncEngine,
            propertiAktif = authState.propertiAktif,
            currentUserId = authState.user.id,
            isGlobalRole = isGlobalRole,
        ),
    )

    fun navigateTopLevel(route: String) {
        if (currentRoute == route) return
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun closeDrawerThen(action: () -> Unit) {
        scope.launch {
            drawerState.close()
            action()
        }
    }

    val pageTitle = when (currentRoute) {
        OperasionalTab.BERANDA.route -> "Beranda"
        OperasionalTab.LAUNDRY.route -> "Laundry"
        OperasionalTab.PENGHUNI.route -> "Penghuni"
        OperasionalTab.LAPORAN.route -> "Laporan Laundry"
        ROUTE_PENGATURAN -> if (bolehKelolaPengaturan) "Pengaturan" else "Akun & Sesi"
        else -> authState.propertiAktif.nama
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isShellRoute,
        drawerContent = {
            // Sidebar Marun Gelap persis `layouts/app.blade.php` dengan teks PUTIH kontras tinggi
            ModalDrawerSheet(
                drawerContainerColor = ext.maroonDark,
                drawerContentColor = Color.White,
                modifier = Modifier.width(292.dp),
            ) {
                Column(modifier = Modifier.fillMaxHeight()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            modifier = Modifier.size(38.dp),
                        ) {
                            Image(
                                painter = painterResource(R.drawable.app_logo_favicon),
                                contentDescription = stringResource(R.string.app_name),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.app_name).uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                ),
                                color = ext.goldSoft,
                            )
                            Text(
                                text = authState.propertiAktif.nama,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.14f))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "OPERASIONAL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                            ),
                            color = Color.White.copy(alpha = 0.60f),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        )

                        OperasionalTab.entries.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationDrawerItem(
                                selected = selected,
                                onClick = { closeDrawerThen { navigateTopLevel(tab.route) } },
                                icon = {
                                    Icon(
                                        tab.icon,
                                        contentDescription = null,
                                        tint = if (selected) ext.goldSoft else Color.White.copy(alpha = 0.85f),
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(tab.labelRes),
                                        color = Color.White,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    )
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = Color.White.copy(alpha = 0.18f),
                                    selectedTextColor = Color.White,
                                    unselectedContainerColor = Color.Transparent,
                                    unselectedTextColor = Color.White,
                                ),
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "MANAJEMEN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                            ),
                            color = Color.White.copy(alpha = 0.60f),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        )

                        if (isGlobalRole) {
                            NavigationDrawerItem(
                                selected = false,
                                onClick = { closeDrawerThen(onReleaseProperti) },
                                icon = {
                                    Icon(Icons.Default.Apartment, contentDescription = null, tint = ext.goldSoft)
                                },
                                label = {
                                    Text(
                                        text = "Kelola / Ganti Gedung",
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium,
                                    )
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    unselectedContainerColor = Color.Transparent,
                                    unselectedTextColor = Color.White,
                                ),
                            )
                        }

                        if (bolehKelolaPengguna) {
                            NavigationDrawerItem(
                                selected = currentRoute == ROUTE_PENGGUNA,
                                onClick = { closeDrawerThen { navController.navigate(ROUTE_PENGGUNA) } },
                                icon = {
                                    Icon(Icons.Default.ManageAccounts, contentDescription = null, tint = Color.White.copy(alpha = 0.85f))
                                },
                                label = {
                                    Text(
                                        text = "Kelola Pengguna",
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium,
                                    )
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = Color.White.copy(alpha = 0.18f),
                                    selectedTextColor = Color.White,
                                    unselectedContainerColor = Color.Transparent,
                                    unselectedTextColor = Color.White,
                                ),
                            )
                        }

                        val settingSelected = currentRoute == ROUTE_PENGATURAN
                        NavigationDrawerItem(
                            selected = settingSelected,
                            onClick = { closeDrawerThen { navigateTopLevel(ROUTE_PENGATURAN) } },
                            icon = {
                                Icon(
                                    imageVector = if (bolehKelolaPengaturan) Icons.Default.Settings else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (settingSelected) ext.goldSoft else Color.White.copy(alpha = 0.85f),
                                )
                            },
                            label = {
                                Text(
                                    text = if (bolehKelolaPengaturan) "Pengaturan & Akun" else "Akun & Sesi",
                                    color = Color.White,
                                    fontWeight = if (settingSelected) FontWeight.Bold else FontWeight.Medium,
                                )
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color.White.copy(alpha = 0.18f),
                                selectedTextColor = Color.White,
                                unselectedContainerColor = Color.Transparent,
                                unselectedTextColor = Color.White,
                            ),
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.14f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ext.goldSoft,
                            modifier = Modifier.size(36.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = authState.user.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ext.maroonDarker,
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = authState.user.name,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = authState.user.roleLabel.ifBlank { authState.user.role },
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.78f),
                            )
                        }
                        IconButton(
                            onClick = {
                                scope.launch { drawerState.close() }
                                showLogoutConfirm = true
                            },
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Keluar",
                                tint = ext.goldSoft,
                            )
                        }
                    }
                }
            }
        },
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = if (isShellRoute) ScaffoldDefaults.contentWindowInsets else WindowInsets(0, 0, 0, 0),
            topBar = {
                if (isShellRoute) {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "Buka menu navigasi",
                                    tint = Color.White,
                                )
                            }
                        },
                        title = {
                            Column {
                                Text(
                                    text = pageTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = authState.propertiAktif.nama,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ext.goldSoft,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        },
                        actions = {
                            val unreadCount by notifikasiViewModel.unreadCount.collectAsStateWithLifecycle()
                            NotificationBellButton(
                                count = unreadCount,
                                tint = Color.White,
                                onClick = {
                                    notifikasiViewModel.refresh(emitSystem = false)
                                    showNotifDialog = true
                                },
                            )
                            if (isGlobalRole) {
                                IconButton(onClick = onReleaseProperti) {
                                    Icon(
                                        Icons.Default.SwapHoriz,
                                        contentDescription = "Ganti Gedung",
                                        tint = ext.goldSoft,
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            },
            bottomBar = {
                if (isShellRoute && !isWideLayout) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                    ) {
                        OperasionalTab.entries.forEach { tab ->
                            val selected = currentRoute == tab.route
                            val label = stringResource(tab.labelRes)
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(tab.route) },
                                icon = { Icon(tab.icon, contentDescription = label) },
                                label = {
                                    Text(
                                        text = label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = ext.goldContainer,
                                    unselectedIconColor = ext.muted,
                                    unselectedTextColor = ext.muted,
                                ),
                            )
                        }
                        NavigationBarItem(
                            selected = currentRoute == ROUTE_PENGATURAN,
                            onClick = { scope.launch { drawerState.open() } },
                            icon = { Icon(Icons.Default.Menu, contentDescription = "Menu") },
                            label = { Text("Menu", maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = ext.goldContainer,
                                unselectedIconColor = ext.muted,
                                unselectedTextColor = ext.muted,
                            ),
                        )
                    }
                }
            },
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (isShellRoute && isWideLayout) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight(),
                    ) {
                        OperasionalTab.entries.forEach { tab ->
                            val selected = currentRoute == tab.route
                            val label = stringResource(tab.labelRes)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(tab.route) },
                                icon = { Icon(tab.icon, contentDescription = label) },
                                label = { Text(label, maxLines = 1) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = ext.goldContainer,
                                    unselectedIconColor = ext.muted,
                                    unselectedTextColor = ext.muted,
                                ),
                            )
                        }
                        NavigationRailItem(
                            selected = currentRoute == ROUTE_PENGATURAN,
                            onClick = { scope.launch { drawerState.open() } },
                            icon = { Icon(Icons.Default.Menu, contentDescription = "Menu") },
                            label = { Text("Menu", maxLines = 1) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = ext.goldContainer,
                                unselectedIconColor = ext.muted,
                                unselectedTextColor = ext.muted,
                            ),
                        )
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = OperasionalTab.BERANDA.route,
                    enterTransition = { fadeIn(animationSpec = tween(150)) },
                    exitTransition = { fadeOut(animationSpec = tween(110)) },
                    popEnterTransition = { fadeIn(animationSpec = tween(150)) },
                    popExitTransition = { fadeOut(animationSpec = tween(110)) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    composable(OperasionalTab.BERANDA.route) { entry ->
                        val vm: BerandaViewModel = viewModel(
                            key = "beranda_${authState.propertiAktif.id}",
                            factory = BerandaViewModel.Factory(repository),
                        )
                        val needsRefresh by entry.savedStateHandle
                            .getStateFlow("refresh_data", false)
                            .collectAsStateWithLifecycle()
                        LaunchedEffect(needsRefresh) {
                            if (needsRefresh) {
                                vm.load(isRefresh = true)
                                notifikasiViewModel.refresh()
                                entry.savedStateHandle["refresh_data"] = false
                            }
                        }
                        BerandaScreen(
                            viewModel = vm,
                            permissions = authState.permissions,
                            onCatatLaundry = { navController.navigate(ROUTE_LAUNDRY_NEW) },
                            onOpenLaundryList = { navigateTopLevel(OperasionalTab.LAUNDRY.route) },
                            onOpenPenghuniList = { navigateTopLevel(OperasionalTab.PENGHUNI.route) },
                            onOpenPenghuniDetail = { id -> navController.navigate("penghuni/detail/$id") },
                        )
                    }

                    composable(OperasionalTab.LAUNDRY.route) { entry ->
                        val vm: LaundryListViewModel = viewModel(
                            key = "laundry_${authState.propertiAktif.id}",
                            factory = LaundryListViewModel.Factory(repository),
                        )
                        val needsRefresh by entry.savedStateHandle
                            .getStateFlow("refresh_data", false)
                            .collectAsStateWithLifecycle()
                        LaunchedEffect(needsRefresh) {
                            if (needsRefresh) {
                                vm.load(isRefresh = true)
                                notifikasiViewModel.refresh()
                                entry.savedStateHandle["refresh_data"] = false
                            }
                        }
                        LaundryListScreen(
                            viewModel = vm,
                            permissions = authState.permissions,
                            onAddLaundry = { navController.navigate(ROUTE_LAUNDRY_NEW) },
                            onEditLaundry = { id -> navController.navigate("laundry/edit/$id") },
                        )
                    }

                    composable(OperasionalTab.PENGHUNI.route) { entry ->
                        val vm: PenghuniListViewModel = viewModel(
                            key = "penghuni_${authState.propertiAktif.id}",
                            factory = PenghuniListViewModel.Factory(repository),
                        )
                        val needsRefresh by entry.savedStateHandle
                            .getStateFlow("refresh_data", false)
                            .collectAsStateWithLifecycle()
                        LaunchedEffect(needsRefresh) {
                            if (needsRefresh) {
                                vm.load(isRefresh = true)
                                notifikasiViewModel.refresh()
                                entry.savedStateHandle["refresh_data"] = false
                            }
                        }
                        PenghuniListScreen(
                            viewModel = vm,
                            onOpenDetail = { id -> navController.navigate("penghuni/detail/$id") },
                        )
                    }

                    composable(OperasionalTab.LAPORAN.route) {
                        val vm: LaporanViewModel = viewModel(
                            key = "laporan_${authState.propertiAktif.id}",
                            factory = LaporanViewModel.Factory(repository),
                        )
                        LaporanScreen(viewModel = vm)
                    }

                    composable(ROUTE_PENGATURAN) {
                        val vm: PengaturanViewModel? = if (bolehKelolaPengaturan) {
                            viewModel(
                                key = "pengaturan_${authState.propertiAktif.id}",
                                factory = PengaturanViewModel.Factory(repository),
                            )
                        } else {
                            null
                        }
                        PengaturanScreen(
                            viewModel = vm,
                            user = authState.user,
                            propertiAktif = authState.propertiAktif,
                            bolehKelolaPengaturan = bolehKelolaPengaturan,
                            canSwitchProperti = isGlobalRole,
                            onSwitchProperti = onReleaseProperti,
                            onLogout = { showLogoutConfirm = true },
                        )
                    }

                    composable(ROUTE_PENGGUNA) {
                        val vm: PenggunaViewModel = viewModel(
                            key = "pengguna_${authState.propertiAktif.id}",
                            factory = PenggunaViewModel.Factory(repository),
                        )
                        PenggunaScreen(
                            viewModel = vm,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable(ROUTE_LAUNDRY_NEW) {
                        val vm: LaundryFormViewModel = viewModel(
                            factory = LaundryFormViewModel.Factory(repository, editId = null),
                        )
                        LaundryFormScreen(
                            viewModel = vm,
                            onBack = { navController.popBackStack() },
                            onSaved = {
                                navController.previousBackStackEntry?.savedStateHandle?.set("refresh_data", true)
                                navController.popBackStack()
                            },
                            onOpenExistingEdit = { existingId ->
                                navController.navigate("laundry/edit/$existingId") {
                                    popUpTo(ROUTE_LAUNDRY_NEW) { inclusive = true }
                                }
                            },
                        )
                    }

                    composable(
                        route = ROUTE_LAUNDRY_EDIT,
                        arguments = listOf(navArgument("id") { type = NavType.IntType }),
                    ) { entry ->
                        val id = entry.arguments?.getInt("id") ?: return@composable
                        val vm: LaundryFormViewModel = viewModel(
                            key = "laundry_edit_$id",
                            factory = LaundryFormViewModel.Factory(repository, editId = id),
                        )
                        LaundryFormScreen(
                            viewModel = vm,
                            onBack = { navController.popBackStack() },
                            onSaved = {
                                navController.previousBackStackEntry?.savedStateHandle?.set("refresh_data", true)
                                navController.popBackStack()
                            },
                            onOpenExistingEdit = { existingId ->
                                navController.navigate("laundry/edit/$existingId") {
                                    popUpTo(ROUTE_LAUNDRY_EDIT) { inclusive = true }
                                }
                            },
                        )
                    }

                    composable(
                        route = ROUTE_PENGHUNI_DETAIL,
                        arguments = listOf(navArgument("id") { type = NavType.IntType }),
                    ) { entry ->
                        val id = entry.arguments?.getInt("id") ?: return@composable
                        val vm: PenghuniDetailViewModel = viewModel(
                            key = "penghuni_detail_$id",
                            factory = PenghuniDetailViewModel.Factory(repository, penghuniId = id),
                        )
                        val needsRefresh by entry.savedStateHandle
                            .getStateFlow("refresh_data", false)
                            .collectAsStateWithLifecycle()
                        LaunchedEffect(needsRefresh) {
                            if (needsRefresh) {
                                vm.load(isRefresh = true)
                                notifikasiViewModel.refresh()
                                entry.savedStateHandle["refresh_data"] = false
                            }
                        }
                        PenghuniDetailScreen(
                            viewModel = vm,
                            onBack = { navController.popBackStack() },
                            onEditLaundry = { laundryId -> navController.navigate("laundry/edit/$laundryId") },
                            onMutated = {
                                navController.previousBackStackEntry?.savedStateHandle?.set("refresh_data", true)
                            },
                        )
                    }
                }
            }
        }
    }

    if (showNotifDialog) {
        val notifications by notifikasiViewModel.notifications.collectAsStateWithLifecycle()
        NotificationListDialog(
            notifications = notifications,
            onItemClick = { notif ->
                notifikasiViewModel.markAsRead(notif.id)
                showNotifDialog = false
                when {
                    notif.penghuniId != null -> navController.navigate("penghuni/detail/${notif.penghuniId}")
                    notif.laundryId != null -> navigateTopLevel(OperasionalTab.LAUNDRY.route)
                }
            },
            onMarkAllRead = notifikasiViewModel::markAllAsRead,
            onClearAll = notifikasiViewModel::clearAll,
            onSeeAllPenghuni = {
                showNotifDialog = false
                navigateTopLevel(OperasionalTab.PENGHUNI.route)
            },
            onDismiss = { showNotifDialog = false },
        )
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Keluar dari Laundry-Kos?") },
            text = { Text("Sesi login Anda akan diakhiri dan Anda perlu masuk kembali untuk mengakses aplikasi.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirm = false
                        onLogout()
                    },
                ) {
                    Text("Ya, Keluar", color = ext.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Batal") }
            },
        )
    }
}
