package com.azrxtech.hitunguntung.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azrxtech.hitunguntung.ui.home.component.TopBarSection
import com.azrxtech.hitunguntung.ui.theme.HitungUntungTheme
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun MarginScreen() {
    val scrollState = rememberScrollState()

    // State untuk menampung input pengguna
    var modalInput by remember { mutableStateOf("150000") }
    var hargaJualInput by remember { mutableStateOf("200000") }

    // Logika Perhitungan Reaktif
    val modal = modalInput.toDoubleOrNull() ?: 0.0
    val hargaJual = hargaJualInput.toDoubleOrNull() ?: 0.0

    val totalKeuntungan = if (hargaJual > modal) hargaJual - modal else 0.0

    // Rumus Margin: (Keuntungan / Harga Jual) * 100
    val marginPersen = if (hargaJual > 0) (totalKeuntungan / hargaJual) * 100 else 0.0

    // Rasio untuk Progress Bar
    val profitRatio = if (hargaJual > 0) (totalKeuntungan / hargaJual).toFloat().coerceIn(0f, 1f) else 0f
    val modalRatio = if (hargaJual > 0) (modal / hargaJual).toFloat().coerceIn(0f, 1f) else 1f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 24.dp)
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
            profitRatio = profitRatio,
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
            Icon(
                imageVector = Icons.Rounded.Image,
                contentDescription = "Placeholder Ilustrasi",
                tint = Color.Gray,
                modifier = Modifier.size(48.dp)
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
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
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
        onValueChange = onValueChange,
        leadingIcon = {
            Text(
                text = "Rp",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
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
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Watermark Icon di Kanan (Efek desain dari gambar)
            Icon(
                imageVector = Icons.Rounded.ShowChart,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.05f),
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 30.dp, y = 10.dp)
            )

            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Total Keuntungan",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "Rp ",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = formatNilai(keuntungan),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.ShowChart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PER TRANSAKSI",
                        color = Color.White,
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
    profitRatio: Float,
    modalRatio: Float
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "%",
                    color = MaterialTheme.colorScheme.primary,
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
                if (profitRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(profitRatio)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary)
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
                    text = "PROFIT",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onBackground
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
            containerColor = Color(0xFFEFF5F6)
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
    val localeID = Locale("in", "ID")
    val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
    formatRupiah.maximumFractionDigits = 0
    return formatRupiah.format(number).replace("Rp", "").replace(",00", "").trim()
}

@Preview(showBackground = true)
@Composable
fun MarginScreenPreview() {
    HitungUntungTheme {
        MarginScreen()
    }
}