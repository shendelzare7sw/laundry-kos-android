package com.laundry_kos.feature.pengguna

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laundry_kos.core.designsystem.EmptyStateCard
import com.laundry_kos.core.designsystem.ErrorBannerCard
import com.laundry_kos.core.designsystem.LaundryCard
import com.laundry_kos.core.designsystem.LaundryKosTheme
import com.laundry_kos.core.designsystem.LaundrySnackbarHost
import com.laundry_kos.core.designsystem.StatusBadge
import com.laundry_kos.data.dto.PenggunaItemDto
import com.laundry_kos.data.dto.SimpanPenggunaRequestDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenggunaScreen(
    viewModel: PenggunaViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val ext = LaundryKosTheme.extendedColors
    val snackbarHostState = remember { SnackbarHostState() }
    var showTambahDialog by remember { mutableStateOf(false) }
    var resetTarget by remember { mutableStateOf<PenggunaItemDto?>(null) }
    var toggleTarget by remember { mutableStateOf<PenggunaItemDto?>(null) }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { LaundrySnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kelola Pengguna",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showTambahDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Akun")
            }
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.refreshing,
            onRefresh = { viewModel.load(isRefresh = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            val data = uiState.data
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 84.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (uiState.error != null) {
                    item { ErrorBannerCard(message = uiState.error!!, onRetry = { viewModel.load() }) }
                }

                if (uiState.loading && data == null) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else if (data != null && data.data.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "Belum ada akun staf",
                            subtitle = "Tekan tombol + untuk membuat akun admin atau penjaga.",
                        )
                    }
                } else if (data != null) {
                    items(data.data, key = { it.id }) { user ->
                        LaundryCard(contentPadding = PaddingValues(14.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(user.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                        Text(
                                            text = "@${user.username} · ${user.roleLabel.ifBlank { user.role }}" +
                                                (user.propertiNama?.let { " · $it" } ?: ""),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ext.muted,
                                        )
                                    }
                                    StatusBadge(
                                        text = if (user.aktif) "Aktif" else "Nonaktif",
                                        containerColor = if (user.aktif) ext.okContainer else ext.dangerContainer,
                                        contentColor = if (user.aktif) ext.ok else ext.danger,
                                    )
                                }

                                if (user.bolehUbah) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        OutlinedButton(
                                            onClick = { toggleTarget = user },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                        ) {
                                            Text(if (user.aktif) "Nonaktifkan" else "Aktifkan")
                                        }
                                        OutlinedButton(
                                            onClick = { resetTarget = user },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ext.onGold),
                                        ) {
                                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Reset Sandi")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog Tambah Akun
    if (showTambahDialog && uiState.data != null) {
        val data = uiState.data!!
        var name by remember { mutableStateOf("") }
        var username by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var role by remember { mutableStateOf(data.peranDiizinkan.firstOrNull()?.value ?: "penjaga") }
        var selectedPropertiId by remember { mutableStateOf(data.propertiPilihan.firstOrNull()?.id) }
        var propertiExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!uiState.submitting) showTambahDialog = false },
            title = { Text("Tambah Akun Pengguna") },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 430.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Lengkap *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username (tanpa spasi) *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email (opsional)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password Awal (min 8) *") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        data.peranDiizinkan.forEach { opt ->
                            FilterChip(
                                selected = role == opt.value,
                                onClick = { role = opt.value },
                                label = { Text(opt.label) },
                            )
                        }
                    }

                    if ((role == "admin" || role == "penjaga") && data.propertiPilihan.isNotEmpty()) {
                        val selectedName = data.propertiPilihan.firstOrNull { it.id == selectedPropertiId }?.nama ?: ""
                        Box {
                            OutlinedTextField(
                                value = selectedName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Properti Penempatan") },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Box(modifier = Modifier.matchParentSize().clickable { propertiExpanded = true })
                            DropdownMenu(expanded = propertiExpanded, onDismissRequest = { propertiExpanded = false }) {
                                data.propertiPilihan.forEach { prop ->
                                    DropdownMenuItem(
                                        text = { Text(prop.nama) },
                                        onClick = {
                                            selectedPropertiId = prop.id
                                            propertiExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !uiState.submitting && name.isNotBlank() && username.isNotBlank() && password.length >= 8,
                    onClick = {
                        viewModel.tambahPengguna(
                            SimpanPenggunaRequestDto(
                                name = name.trim(),
                                username = username.trim(),
                                email = email.trim().takeIf { it.isNotEmpty() },
                                password = password,
                                role = role,
                                propertiId = if (role == "admin" || role == "penjaga") selectedPropertiId else null,
                            ),
                        ) {
                            showTambahDialog = false
                        }
                    },
                ) { Text(if (uiState.submitting) "Menyimpan..." else "Buat Akun") }
            },
            dismissButton = {
                TextButton(onClick = { showTambahDialog = false }, enabled = !uiState.submitting) { Text("Batal") }
            },
        )
    }

    // Dialog Konfirmasi Aktifkan/Nonaktifkan
    toggleTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { toggleTarget = null },
            title = { Text(if (target.aktif) "Nonaktifkan Akun?" else "Aktifkan Akun?") },
            text = {
                Text(
                    if (target.aktif) {
                        "Akun @${target.username} akan dinonaktifkan dan seluruh sesi loginnya dicabut."
                    } else {
                        "Akun @${target.username} akan diaktifkan kembali."
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val id = target.id
                    toggleTarget = null
                    viewModel.toggleAktif(id)
                }) { Text(if (target.aktif) "Nonaktifkan" else "Aktifkan", color = if (target.aktif) ext.danger else ext.ok) }
            },
            dismissButton = { TextButton(onClick = { toggleTarget = null }) { Text("Batal") } },
        )
    }

    // Dialog Reset Password
    resetTarget?.let { target ->
        var pass by remember { mutableStateOf("") }
        var confirm by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { resetTarget = null },
            title = { Text("Reset Password @${target.username}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password Baru (min 8)") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = confirm, onValueChange = { confirm = it }, label = { Text("Konfirmasi Password") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !uiState.submitting && pass.length >= 8 && pass == confirm,
                    onClick = {
                        val id = target.id
                        viewModel.resetPassword(id, pass, confirm) {
                            resetTarget = null
                        }
                    },
                ) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { resetTarget = null }) { Text("Batal") } },
        )
    }
}
