package com.laundry_kos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.laundry_kos.core.designsystem.KuotaProgressBar
import com.laundry_kos.core.designsystem.KuotaVisualStatus
import com.laundry_kos.core.designsystem.LaundryCard
import com.laundry_kos.core.designsystem.LaundryKosTheme
import com.laundry_kos.core.designsystem.Maroon
import com.laundry_kos.core.designsystem.MaroonPrimaryButton
import com.laundry_kos.core.designsystem.StatusBadge
import com.laundry_kos.core.util.Formatters

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Maroon.toArgb()),
        )
        setContent {
            LaundryKosTheme {
                Fase0FoundationPreviewScreen(
                    apiBaseUrl = BuildConfig.API_BASE_URL,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Fase0FoundationPreviewScreen(
    apiBaseUrl: String,
) {
    val ext = LaundryKosTheme.extendedColors
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_laundry_mark),
                            contentDescription = stringResource(id = R.string.app_name),
                            modifier = Modifier.size(30.dp),
                        )
                        Column {
                            Text(
                                text = stringResource(id = R.string.app_name),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Text(
                                text = stringResource(id = R.string.app_tagline),
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
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            LaundryCard {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Fondasi Fase 0 Siap",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        StatusBadge(
                            text = "API v1",
                            containerColor = ext.goldContainer,
                            contentColor = ext.onGold,
                        )
                    }
                    Text(
                        text = "Base URL aktif: $apiBaseUrl",
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.muted,
                    )
                }
            }

            LaundryCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Pratinjau Token Kuota & Format Domain",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = "Berat: ${Formatters.formatBeratKg(2370, "23,7")} • Tagihan: ${Formatters.formatRupiah(24000)}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    KuotaProgressBar(persen = 45, status = KuotaVisualStatus.NORMAL)
                    KuotaProgressBar(persen = 80, status = KuotaVisualStatus.MENDEKATI)
                    KuotaProgressBar(persen = 100, status = KuotaVisualStatus.LEWAT)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            MaroonPrimaryButton(
                text = "Lanjut ke Fase 1 — Login & API",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun Fase0ScreenPreview() {
    LaundryKosTheme {
        Fase0FoundationPreviewScreen(apiBaseUrl = "http://127.0.0.1:8000/api/v1/")
    }
}
