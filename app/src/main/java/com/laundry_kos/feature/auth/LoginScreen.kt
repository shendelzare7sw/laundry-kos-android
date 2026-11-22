package com.laundry_kos.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.laundry_kos.R
import com.laundry_kos.core.designsystem.LaundryCard
import com.laundry_kos.core.designsystem.LaundryKosTheme
import com.laundry_kos.core.designsystem.MaroonPrimaryButton

@Composable
fun LoginScreen(
    uiState: SessionUiState.Unauthenticated,
    onLogin: (username: String, password: String) -> Unit,
) {
    val ext = LaundryKosTheme.extendedColors
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

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
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = BorderStroke(1.dp, ext.line),
            shadowElevation = 2.dp,
            modifier = Modifier.size(76.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo_favicon),
                contentDescription = stringResource(R.string.app_name),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
            )
        }
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

        // Banner sesi dicabut / akun dinonaktifkan / info keluar normal
        if (!uiState.bannerMessage.isNullOrBlank()) {
            LoginNoticeBanner(
                title = uiState.bannerTitle,
                message = uiState.bannerMessage,
                isError = uiState.bannerIsError,
                isBlocked = uiState.bannerTitle == "Akun Dinonaktifkan" || uiState.bannerTitle == "Properti Tidak Aktif",
                modifier = Modifier.widthIn(max = 460.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Banner error login (termasuk blokir akun nonaktif, properti nonaktif, rate limit 429, atau gangguan jaringan)
        val loginErr = uiState.loginError
        if (loginErr != null && usernameError == null && passwordError == null) {
            val blockedTitle = when (loginErr.code) {
                "AKUN_NONAKTIF" -> "Akun Dinonaktifkan"
                "PROPERTI_TIDAK_AKTIF", "PROPERTI_TIDAK_TERIKAT" -> "Properti Tidak Aktif"
                "TOO_MANY_REQUESTS" -> "Terlalu Banyak Percobaan"
                else -> if (loginErr.httpCode == 403) "Akses Masuk Ditolak" else "Gagal Masuk"
            }
            LoginNoticeBanner(
                title = blockedTitle,
                message = loginErr.summaryMessage(),
                isError = true,
                isBlocked = loginErr.isAccountOrPropertyBlocked || loginErr.httpCode == 403,
                modifier = Modifier.widthIn(max = 460.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        LaundryCard(modifier = Modifier.widthIn(max = 460.dp)) {
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
                    shape = RoundedCornerShape(12.dp),
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
                    shape = RoundedCornerShape(12.dp),
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
                            if (username.isNotBlank() && password.isNotBlank() && !uiState.isLoggingIn) {
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
    }
}

@Composable
private fun LoginNoticeBanner(
    title: String?,
    message: String,
    isError: Boolean,
    isBlocked: Boolean,
    modifier: Modifier = Modifier,
) {
    val ext = LaundryKosTheme.extendedColors
    val bgColor = if (isError) ext.dangerContainer else ext.okContainer
    val borderColor = if (isError) ext.danger.copy(alpha = 0.40f) else ext.ok.copy(alpha = 0.35f)
    val iconColor = if (isError) ext.danger else ext.ok
    val titleColor = if (isError) ext.danger else ext.ok
    val textColor = if (isError) ext.maroonDarker else MaterialTheme.colorScheme.onSurface
    val icon = when {
        !isError -> Icons.Default.CheckCircle
        isBlocked -> Icons.Default.Block
        else -> Icons.Default.Warning
    }

    val hasTitle = !title.isNullOrBlank()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = if (hasTitle) Alignment.Top else Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier
                    .then(if (hasTitle) Modifier.padding(top = 2.dp) else Modifier)
                    .size(20.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                if (hasTitle) {
                    Text(
                        text = title!!,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = titleColor,
                    )
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor,
                )
            }
        }
    }
}
