package com.laundry_kos.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.laundry_kos.data.repository.LaundryKosRepository
import com.laundry_kos.feature.auth.LoginScreen
import com.laundry_kos.feature.auth.PropertiPickerScreen
import com.laundry_kos.feature.auth.PropertiSwitcherDialog
import com.laundry_kos.feature.auth.SessionUiState
import com.laundry_kos.feature.auth.SessionViewModel
import com.laundry_kos.feature.beranda.BerandaScreen
import com.laundry_kos.feature.beranda.BerandaViewModel
import com.laundry_kos.feature.laporan.LaporanScreen
import com.laundry_kos.feature.laporan.LaporanViewModel
import com.laundry_kos.feature.laundry.LaundryFormScreen
import com.laundry_kos.feature.laundry.LaundryFormViewModel
import com.laundry_kos.feature.laundry.LaundryListScreen
import com.laundry_kos.feature.laundry.LaundryListViewModel
import com.laundry_kos.feature.pengaturan.PengaturanScreen
import com.laundry_kos.feature.pengaturan.PengaturanViewModel
import com.laundry_kos.feature.penghuni.PenghuniDetailScreen
import com.laundry_kos.feature.penghuni.PenghuniDetailViewModel
import com.laundry_kos.feature.penghuni.PenghuniListScreen
import com.laundry_kos.feature.penghuni.PenghuniListViewModel

private enum class MainTab(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
) {
    BERANDA("beranda", R.string.nav_beranda, Icons.Default.Home),
    LAUNDRY("laundry", R.string.nav_laundry, Icons.Default.LocalLaundryService),
    PENGHUNI("penghuni", R.string.nav_penghuni, Icons.Default.Groups),
    LAPORAN("laporan", R.string.nav_laporan, Icons.Default.Assessment),
    PENGATURAN("pengaturan", R.string.nav_pengaturan, Icons.Default.Settings),
}

private const val ROUTE_LAUNDRY_NEW = "laundry/new"
private const val ROUTE_LAUNDRY_EDIT = "laundry/edit/{id}"
private const val ROUTE_PENGHUNI_DETAIL = "penghuni/detail/{id}"

@Composable
fun LaundryKosRoot(
    repository: LaundryKosRepository,
) {
    val sessionViewModel: SessionViewModel = viewModel(factory = SessionViewModel.Factory(repository))
    val sessionState by sessionViewModel.state.collectAsStateWithLifecycle()
    val storedSession by sessionViewModel.storedSession.collectAsStateWithLifecycle()

    when (val state = sessionState) {
        SessionUiState.Checking -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        is SessionUiState.Unauthenticated -> {
            LoginScreen(
                uiState = state,
                currentBaseUrl = storedSession.baseUrl,
                onLogin = sessionViewModel::login,
                onChangeDebugUrl = sessionViewModel::updateDebugBaseUrl,
            )
        }
        is SessionUiState.SelectProperti -> {
            PropertiPickerScreen(
                uiState = state,
                onSelect = sessionViewModel::selectProperti,
                onLogout = sessionViewModel::logout,
            )
        }
        is SessionUiState.Authenticated -> {
            // Re-key seluruh NavHost & ViewModel per properti aktif agar perpindahan properti bersih total
            key(state.propertiAktif.id) {
                AuthenticatedAppShell(
                    authState = state,
                    repository = repository,
                    onOpenPropertiSwitcher = sessionViewModel::openPropertiSwitcher,
                    onSelectProperti = sessionViewModel::selectProperti,
                    onDismissPropertiSwitcher = sessionViewModel::dismissPropertiSwitcher,
                    onLogout = sessionViewModel::logout,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthenticatedAppShell(
    authState: SessionUiState.Authenticated,
    repository: LaundryKosRepository,
    onOpenPropertiSwitcher: () -> Unit,
    onSelectProperti: (Int) -> Unit,
    onDismissPropertiSwitcher: () -> Unit,
    onLogout: () -> Unit,
) {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route
    val isMainTab = MainTab.entries.any { it.route == currentRoute }
    val ext = LaundryKosTheme.extendedColors
    val canSwitchProperti = authState.propertiList.size > 1

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (isMainTab) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.clickable(enabled = canSwitchProperti) {
                                onOpenPropertiSwitcher()
                            },
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_laundry_mark),
                                contentDescription = stringResource(R.string.app_name),
                                modifier = Modifier.size(30.dp),
                            )
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = authState.propertiAktif.nama,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    if (canSwitchProperti) {
                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Ganti properti",
                                            tint = ext.goldSoft,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                                Text(
                                    text = "${authState.user.name} · ${authState.user.roleLabel.ifBlank { authState.user.role }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ext.goldSoft,
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
            if (isMainTab) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) {
                    MainTab.entries.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = stringResource(tab.labelRes)) },
                            label = {
                                Text(
                                    text = stringResource(tab.labelRes),
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
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.BERANDA.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            composable(MainTab.BERANDA.route) { entry ->
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
                        entry.savedStateHandle["refresh_data"] = false
                    }
                }
                BerandaScreen(
                    viewModel = vm,
                    permissions = authState.permissions,
                    onCatatLaundry = { navController.navigate(ROUTE_LAUNDRY_NEW) },
                    onOpenPenghuniDetail = { id -> navController.navigate("penghuni/detail/$id") },
                )
            }

            composable(MainTab.LAUNDRY.route) { entry ->
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

            composable(MainTab.PENGHUNI.route) {
                val vm: PenghuniListViewModel = viewModel(
                    key = "penghuni_${authState.propertiAktif.id}",
                    factory = PenghuniListViewModel.Factory(repository),
                )
                PenghuniListScreen(
                    viewModel = vm,
                    onOpenDetail = { id -> navController.navigate("penghuni/detail/$id") },
                )
            }

            composable(MainTab.LAPORAN.route) {
                val vm: LaporanViewModel = viewModel(
                    key = "laporan_${authState.propertiAktif.id}",
                    factory = LaporanViewModel.Factory(repository),
                )
                LaporanScreen(viewModel = vm)
            }

            composable(MainTab.PENGATURAN.route) {
                val vm: PengaturanViewModel = viewModel(
                    key = "pengaturan_${authState.propertiAktif.id}",
                    factory = PengaturanViewModel.Factory(repository),
                )
                PengaturanScreen(
                    viewModel = vm,
                    user = authState.user,
                    propertiAktif = authState.propertiAktif,
                    canSwitchProperti = canSwitchProperti,
                    onSwitchProperti = onOpenPropertiSwitcher,
                    onLogout = onLogout,
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
                        entry.savedStateHandle["refresh_data"] = false
                    }
                }
                PenghuniDetailScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onEditLaundry = { laundryId -> navController.navigate("laundry/edit/$laundryId") },
                )
            }
        }
    }

    if (authState.showPropertiSwitcher) {
        PropertiSwitcherDialog(
            propertiList = authState.propertiList,
            activeId = authState.propertiAktif.id,
            isSwitching = authState.isSwitchingProperti,
            onSelect = onSelectProperti,
            onDismiss = onDismissPropertiSwitcher,
        )
    }
}
