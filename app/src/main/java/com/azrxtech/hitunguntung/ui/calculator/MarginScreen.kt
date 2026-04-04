package com.azrxtech.hitunguntung.ui.calculator

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azrxtech.hitunguntung.R
import com.azrxtech.hitunguntung.ui.home.component.TopBarSection
import com.azrxtech.hitunguntung.ui.theme.HitungUntungTheme
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun MarginScreen() {
    val scrollState = rememberScrollState()

    // State untuk menampung input pengguna
    var modalInput by remember { mutableStateOf(formatInputWithDots("150000")) }
    var hargaJualInput by remember { mutableStateOf(formatInputWithDots("200000")) }

    // Logika Perhitungan Reaktif
    val modal = parseFormattedInput(modalInput)
    val hargaJual = parseFormattedInput(hargaJualInput)

    val totalKeuntungan = hargaJual - modal

    // Rumus Margin: (Keuntungan / Harga Jual) * 100
    val marginPersen = if (hargaJual > 0) (totalKeuntungan / hargaJual) * 100 else 0.0

    // Rasio untuk Progress Bar
    val resultRatio = if (hargaJual > 0) (abs(totalKeuntungan) / hargaJual).toFloat().coerceIn(0f, 1f) else 0f
    val modalRatio = (1f - resultRatio).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        // App Bar menggunakan komponen yang sudah ada
        TopBarSection()

        Spacer(modifier = Modifier.height(24.dp))

        // Header Title
        Text(
            text = "ANALISIS PROFITABILITAS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Hitung Margin\nPenjualan.",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 38.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Input Form Card
        InputMarginCard(
            modalValue = modalInput,
            onModalChange = { modalInput = it },
            jualValue = hargaJualInput,
            onJualChange = { hargaJualInput = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Hasil 1: Total Keuntungan (Kartu Hijau)
        TotalKeuntunganCard(keuntungan = totalKeuntungan)

        Spacer(modifier = Modifier.height(16.dp))

        // Hasil 2: Margin Keuntungan (Progress Bar)
        MarginKeuntunganCard(
            marginPersen = marginPersen,
            resultRatio = resultRatio,
            modalRatio = modalRatio
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Insight Card (Dinamis berdasarkan persentase margin)
        DynamicInsightCard(marginPersen = marginPersen)

        Spacer(modifier = Modifier.height(24.dp))

        // Image Placeholder (Ilustrasi dekoratif bawah)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.LightGray.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_margin),
                contentDescription = "Placeholder Ilustrasi",
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun InputMarginCard(
    modalValue: String,
    onModalChange: (String) -> Unit,
    jualValue: String,
    onJualChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Field Modal
            Text(
                text = "Modal (HPP)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            MarginTextField(value = modalValue, onValueChange = onModalChange)

            Spacer(modifier = Modifier.height(20.dp))

            // Field Harga Jual
            Text(
                text = "Harga Jual",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            MarginTextField(value = jualValue, onValueChange = onJualChange)
        }
    }
}

@Composable
fun MarginTextField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(formatInputWithDots(it)) },
        leadingIcon = {
            Text(
                text = "Rp",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.4f),
                fontSize = 14.sp
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
        ),
        textStyle = LocalTextStyle.current.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun TotalKeuntunganCard(keuntungan: Double) {
    val isLoss = keuntungan < 0
    val containerColor = if (isLoss) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val contentColor = if (isLoss) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Watermark Icon di Kanan (Efek desain dari gambar)
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ShowChart,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.08f),
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 30.dp, y = 10.dp)
            )

            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Total Keuntungan",
                    color = contentColor.copy(alpha = 0.75f),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "Rp ",
                        color = contentColor.copy(alpha = 0.75f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = formatNilai(keuntungan),
                        color = contentColor,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ShowChart,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLoss) "RUGI PER TRANSAKSI" else "PER TRANSAKSI",
                        color = contentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MarginKeuntunganCard(
    marginPersen: Double,
    resultRatio: Float,
    modalRatio: Float
) {
    val isLoss = marginPersen < 0
    val resultColor = if (isLoss) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(0.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Margin Keuntungan",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${marginPersen.roundToInt()}",
                    color = resultColor,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "%",
                    color = resultColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Custom Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
            ) {
                if (resultRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(resultRatio)
                            .fillMaxHeight()
                            .background(resultColor)
                    )
                }
                if (modalRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(modalRatio)
                            .fillMaxHeight()
                            // Menggunakan warna tertiaryContainerLight atau abu kecoklatan sesuai desain
                            .background(Color(0xFFD6CFCB))
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Labels untuk Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isLoss) "RUGI" else "PROFIT",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = if (isLoss) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "MODAL",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun DynamicInsightCard(marginPersen: Double) {
    // Logika pesan dinamis berdasarkan hasil margin
    val (quote, description) = when {
        marginPersen >= 40.0 -> Pair(
            "\"Keuntungan maksimal, ruang ekspansi yang luas.\"",
            "Dengan margin sebesar ${marginPersen.roundToInt()}%, Anda memiliki profitabilitas yang sangat tinggi. Pertimbangkan untuk menggunakan sebagian margin ini untuk promosi agresif atau peningkatan kualitas layanan."
        )
        marginPersen >= 15.0 -> Pair(
            "\"Celah keuntungan yang sehat adalah kunci pertumbuhan bisnis.\"",
            "Dengan margin sebesar ${marginPersen.roundToInt()}%, Anda memiliki ruang untuk biaya operasional dan promosi. Pastikan untuk meninjau kembali HPP secara berkala agar performa dagang tetap optimal."
        )
        marginPersen > 0.0 -> Pair(
            "\"Margin tipis membutuhkan strategi volume tinggi.\"",
            "Margin Anda berada di ${marginPersen.roundToInt()}%. Karena celah profit tipis, pastikan strategi penjualan Anda berfokus pada perputaran barang yang cepat (fast-moving) agar total omzet menutupi operasional."
        )
        else -> Pair(
            "\"Peringatan: Harga jual tidak menutupi modal asli.\"",
            "Saat ini Anda beroperasi tanpa margin keuntungan atau rugi. Segera evaluasi kembali Harga Pokok Penjualan (HPP) Anda atau naikkan harga jual produk."
        )
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            // Warna biru keabu-abuan terang sesuai desain
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = quote,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = buildAnnotatedString {
                    append(description.substringBefore("${marginPersen.roundToInt()}%"))
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)) {
                        append("${marginPersen.roundToInt()}%")
                    }
                    append(description.substringAfter("${marginPersen.roundToInt()}%"))
                },
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}

// Fungsi Helper formatting (Diletakkan private agar tidak bentrok dengan screen lain)
private fun formatNilai(number: Double): String {
    if (number == 0.0) return "0"
    val localeID = Locale.forLanguageTag("id-ID")
    val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
    formatRupiah.maximumFractionDigits = 0
    return formatRupiah.format(number).replace("Rp", "").replace(",00", "").trim()
}

private fun parseFormattedInput(value: String): Double {
    return value.replace(".", "").toDoubleOrNull() ?: 0.0
}

private fun formatInputWithDots(raw: String): String {
    val digits = raw.filter { it.isDigit() }
    if (digits.isEmpty()) return ""

    val normalized = digits.trimStart('0').ifEmpty { "0" }
    return normalized.reversed().chunked(3).joinToString(".").reversed()
}

@Preview(showBackground = true)
@Composable
fun MarginScreenPreview() {
    HitungUntungTheme {
        MarginScreen()
    }
}