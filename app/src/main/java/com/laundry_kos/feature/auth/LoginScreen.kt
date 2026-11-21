package com.laundry_kos.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.laundry_kos.BuildConfig
import com.laundry_kos.R
import com.laundry_kos.core.designsystem.ErrorBannerCard
import com.laundry_kos.core.designsystem.LaundryCard
import com.laundry_kos.core.designsystem.LaundryKosTheme
import com.laundry_kos.core.designsystem.MaroonPrimaryButton

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    uiState: SessionUiState.Unauthenticated,
    currentBaseUrl: String,
    onLogin: (username: String, password: String) -> Unit,
    onChangeDebugUrl: (String) -> Unit,
) {
    val ext = LaundryKosTheme.extendedColors
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var showServerConfig by rememberSaveable { mutableStateOf(false) }
    var customUrlInput by remember(currentBaseUrl) { mutableStateOf(currentBaseUrl) }

    val usernameError = uiState.loginError?.firstFieldError("username")
    val passwordError = uiState.loginError?.firstFieldError("password")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_laundry_mark),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.size(72.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.app_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = ext.muted,
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!uiState.bannerMessage.isNullOrBlank()) {
            ErrorBannerCard(message = uiState.bannerMessage)
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (uiState.loginError != null && usernameError == null && passwordError == null) {
            ErrorBannerCard(message = uiState.loginError.summaryMessage())
            Spacer(modifier = Modifier.height(12.dp))
        }

        LaundryCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Masuk ke Akun Pengelola / Penjaga",
                    style = MaterialTheme.typography.titleMedium,
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                    isError = usernameError != null,
                    supportingText = usernameError?.let { { Text(it) } },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    isError = passwordError != null,
                    supportingText = passwordError?.let { { Text(it) } },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Tampilkan password",
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (username.isNotBlank() && password.isNotBlank()) {
                                onLogin(username, password)
                            }
                        },
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                MaroonPrimaryButton(
                    text = "Masuk",
                    loading = uiState.isLoggingIn,
                    enabled = username.isNotBlank() && password.isNotBlank(),
                    onClick = { onLogin(username, password) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (BuildConfig.DEBUG) {
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { showServerConfig = !showServerConfig }) {
                Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.size(6.dp))
                Text("Server Dev: $currentBaseUrl", style = MaterialTheme.typography.labelMedium)
            }

            if (showServerConfig) {
                LaundryCard(containerColor = ext.surfaceAlt) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Pilih Server Lokal (Debug)",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val usbUrl = "http://127.0.0.1:8000/api/v1/"
                            val emuUrl = "http://10.0.2.2:8000/api/v1/"
                            FilterChip(
                                selected = currentBaseUrl == usbUrl,
                                onClick = {
                                    customUrlInput = usbUrl
                                    onChangeDebugUrl(usbUrl)
                                },
                                label = { Text("HP Fisik (adb reverse)") },
                            )
                            FilterChip(
                                selected = currentBaseUrl == emuUrl,
                                onClick = {
                                    customUrlInput = emuUrl
                                    onChangeDebugUrl(emuUrl)
                                },
                                label = { Text("Emulator (10.0.2.2)") },
                            )
                        }
                        OutlinedTextField(
                            value = customUrlInput,
                            onValueChange = { customUrlInput = it },
                            label = { Text("Base URL API (IP LAN / Custom)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            OutlinedButton(
                                onClick = { onChangeDebugUrl(customUrlInput) },
                            ) {
                                Text("Terapkan URL")
                            }
                        }
                    }
                }
            }
        }
    }
}
