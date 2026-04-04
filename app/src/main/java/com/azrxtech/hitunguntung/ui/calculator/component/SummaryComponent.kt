package com.azrxtech.hitunguntung.ui.calculator.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azrxtech.hitunguntung.ui.calculator.formatRupiah
import com.azrxtech.hitunguntung.ui.theme.HitungUntungTheme

@Composable
fun SummaryComponent(
    totalModal: Double,
    targetUntung: Double,
    hppPerDus: Double,
    hppPerPcs: Double,
    rekomendasiHargaDus: Double,
    rekomendasiHargaPcs: Double,
    onShare: () -> Unit = {},
    onSalin: () -> Unit = {}
) {
    val color = androidx.compose.material3.MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.primary),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Hasil Perhitungan",
                        color = color.onPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Status Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(color.onPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(color.secondaryContainer)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "STATUS: PROFIT OPTIMAL",
                            color = color.onPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Share Icon
                IconButton(
                    onClick = onShare,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.onPrimary.copy(alpha = 0.1f))
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "Share",
                        tint = color.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Grid Layout Data
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryBoxData(title = "TOTAL MODAL", value = totalModal, modifier = Modifier.weight(1f))
                SummaryBoxData(title = "TARGET UNTUNG", value = targetUntung, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryBoxData(title = "HPP PER DUS", value = hppPerDus, modifier = Modifier.weight(1f))
                SummaryBoxData(title = "HPP PER PCS", value = hppPerPcs, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = color.onPrimary.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Harga Jual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("Rekomendasi Harga Jual / Dus", color = color.onPrimary.copy(alpha = 0.8f), fontSize = 11.sp)
                    Text(text = formatRupiah(rekomendasiHargaDus), color = color.onPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Harga / Pcs", color = color.onPrimary.copy(alpha = 0.8f), fontSize = 11.sp)
                    Text(text = formatRupiah(rekomendasiHargaPcs), color = color.onPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Button Salin
            Button(
                onClick = onSalin,
                colors = ButtonDefaults.buttonColors(containerColor = color.onPrimary, contentColor = color.primary),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Salin", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SummaryComponentPreview() {
    HitungUntungTheme {
        SummaryComponent(
            totalModal = 1250000.0,
            targetUntung = 187500.0,
            hppPerDus = 625000.0,
            hppPerPcs = 6250.0,
            rekomendasiHargaDus = 718750.0,
            rekomendasiHargaPcs = 7187.5
        )
    }
}
