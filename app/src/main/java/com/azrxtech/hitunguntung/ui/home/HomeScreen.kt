package com.azrxtech.hitunguntung.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azrxtech.hitunguntung.ui.home.component.FeaturedKulakanCard
import com.azrxtech.hitunguntung.ui.home.component.HeaderSection
import com.azrxtech.hitunguntung.ui.home.component.HorizontalMenuCard
import com.azrxtech.hitunguntung.ui.home.component.StandardMenuCard
import com.azrxtech.hitunguntung.ui.home.component.TopBarSection
import com.azrxtech.hitunguntung.ui.theme.HitungUntungTheme

@Composable
fun HomeScreen(
    onNavigateToKulakan: () -> Unit = {},
    onNavigateToMargin: () -> Unit = {},
    onNavigateToDiskon: () -> Unit = {},
    onNavigateToKembalian: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        TopBarSection()

        Spacer(modifier = Modifier.height(32.dp))

        HeaderSection()

        Spacer(modifier = Modifier.height(32.dp))

        FeaturedKulakanCard(onClick = { onNavigateToKulakan })

        Spacer(modifier = Modifier.height(16.dp))

        StandardMenuCard(
            title = "Kalkulator Margin",
            subtitle = "Tentukan harga jual ideal untuk keuntungan maksimal.",
            iconBgColor = colorScheme.secondaryContainer,
            iconContent = {
                Text(
                    text = "%",
                    color = colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            onClick = onNavigateToMargin
        )

        Spacer(modifier = Modifier.height(16.dp))

        StandardMenuCard(
            title = "Kalkulator Diskon",
            subtitle = "Simulasi promo bertingkat dan diskon kupon pelanggan.",
            iconBgColor = colorScheme.tertiaryContainer,
            iconContent = {
                Icon(
                    imageVector = Icons.Rounded.LocalOffer,
                    contentDescription = "Diskon",
                    tint = colorScheme.onTertiaryContainer
                )
            },
            onClick = onNavigateToDiskon
        )

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalMenuCard(
            title = "Kalkulator Kembalian",
            subtitle = "Hitung uang kembalian kasir dengan cepat dan hindari kesalahan transaksi harian.",
            iconBgColor = colorScheme.primaryContainer,
            iconContent = {
                Icon(
                    imageVector = Icons.Rounded.Sync,
                    contentDescription = "Kembalian",
                    tint = colorScheme.onPrimaryContainer
                )
            },
            onClick = onNavigateToKembalian
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HitungUntungTheme {
        HomeScreen()
    }
}