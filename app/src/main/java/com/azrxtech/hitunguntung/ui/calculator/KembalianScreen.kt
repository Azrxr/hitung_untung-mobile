package com.azrxtech.hitunguntung.ui.calculator

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azrxtech.hitunguntung.ui.home.component.TopBarSection
import com.azrxtech.hitunguntung.ui.theme.HitungUntungTheme
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

// Data class untuk menampung rincian pecahan kembalian
data class PecahanItem(val nominal: Long, val jumlah: Int, val tipe: String)

@Composable
fun KembalianScreen() {
    val scrollState = rememberScrollState()

    // State untuk Input (Hanya menyimpan digit angka murni tanpa titik)
    var totalBelanjaInput by remember { mutableStateOf("") }
    var uangBayarInput by remember { mutableStateOf("") }

    // Logika Perhitungan Reaktif
    val totalBelanja = totalBelanjaInput.toDoubleOrNull() ?: 0.0
    val uangBayar = uangBayarInput.toDoubleOrNull() ?: 0.0

    // Hitung kembalian: jika uang bayar diisi tapi kurang, hasilnya akan minus
    val kembalian = if (uangBayarInput.isNotEmpty()) uangBayar - totalBelanja else 0.0
    val pecahanList = if (kembalian > 0) hitungPecahan(kembalian.toLong()) else emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        // App Bar dari komponen yang sudah ada
        TopBarSection()

        Spacer(modifier = Modifier.height(24.dp))

        // Header Title
        Text(
            text = "Kembalian",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Hitung uang kembalian pelanggan dengan akurat dan cepat untuk menjaga kepercayaan.",
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Card Input Belanja & Uang Bayar
        InputKembalianCard(
            totalBelanja = totalBelanjaInput,
            onTotalBelanjaChange = {
                // Filter hanya menerima digit agar aman dari crash
                totalBelanjaInput = it.filter { char -> char.isDigit() }
            },
            uangBayar = uangBayarInput,
            onUangBayarChange = {
                uangBayarInput = it.filter { char -> char.isDigit() }
            },
            onUangPasClick = { uangBayarInput = totalBelanjaInput },
            onQuickNominalClick = { nominal -> uangBayarInput = nominal }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Card Uang Kembalian (Hijau Besar) - Menangani Minus
        KembalianResultCard(kembalian = kembalian)

        Spacer(modifier = Modifier.height(24.dp))

        // Rincian Pecahan Uang
        if (kembalian > 0) {
            PecahanGrid(pecahanList = pecahanList)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun InputKembalianCard(
    totalBelanja: String,
    onTotalBelanjaChange: (String) -> Unit,
    uangBayar: String,
    onUangBayarChange: (String) -> Unit,
    onUangPasClick: () -> Unit,
    onQuickNominalClick: (String) -> Unit
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
            // Field Total Belanja
            Text(
                text = "Total Belanja",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            KembalianTextField(
                value = totalBelanja,
                onValueChange = onTotalBelanjaChange,
                placeholder = "0"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Field Uang Bayar
            Text(
                text = "Uang Bayar",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            KembalianTextField(
                value = uangBayar,
                onValueChange = onUangBayarChange,
                placeholder = "0"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Actions / Chip Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickNominalChip(
                    text = "Uang Pas",
                    onClick = onUangPasClick,
                    modifier = Modifier.weight(1f)
                )
                QuickNominalChip(
                    text = "Rp 50.000",
                    onClick = { onQuickNominalClick("50000") },
                    modifier = Modifier.weight(1f)
                )
                QuickNominalChip(
                    text = "Rp 100.000",
                    onClick = { onQuickNominalClick("100000") },
                    modifier = Modifier.weight(1.2f)
                )
            }
        }
    }
}

@Composable
fun KembalianTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = placeholder, color = Color.LightGray, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        leadingIcon = {
            Text(
                text = "Rp",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontSize = 16.sp
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        // Gunakan VisualTransformation untuk format titik ribuan otomatis
        visualTransformation = RibuanVisualTransformation(),
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
fun QuickNominalChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFD3E6ED))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun KembalianResultCard(kembalian: Double) {
    val isKurang = kembalian < 0
    val displayValue = abs(kembalian)
    // Teks merah terang (Light Red) jika minus, putih jika plus/pas
    val textColor = if (isKurang) Color(0xFFFF6B6B) else Color.White
    val titleText = if (isKurang) "UANG KURANG" else "UANG KEMBALIAN"
    val signStr = if (isKurang) "- Rp " else "Rp "

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Icon Watermark di Kanan
            Icon(
                imageVector = Icons.Rounded.Sync,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.1f),
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 16.dp)
            )

            Column(modifier = Modifier.padding(28.dp)) {
                Text(
                    text = titleText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = signStr,
                        fontSize = 24.sp,
                        color = textColor.copy(alpha = 0.9f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = formatNilai(displayValue),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun PecahanGrid(pecahanList: List<PecahanItem>) {
    val chunkedList = pecahanList.chunked(2)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        chunkedList.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Item Kiri
                PecahanCard(item = rowItems[0], modifier = Modifier.weight(1f))

                // Item Kanan (Bisa kosong atau diganti DashedBox jika ganjil)
                if (rowItems.size > 1) {
                    PecahanCard(item = rowItems[1], modifier = Modifier.weight(1f))
                } else {
                    DashedPlaceholderCard(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun PecahanCard(item: PecahanItem, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Konversi nama drawable secara otomatis (100000 -> bg_100k | 500 -> bg_500)
    val resourceName = if (item.nominal >= 1000) "bg_${item.nominal / 1000}k" else "bg_${item.nominal}"

    // Dapatkan ID Resource (jika file drawable belum dibuat, kembalikan nilai 0 dengan aman)
    val resId = remember(resourceName) {
        context.resources.getIdentifier(resourceName, "drawable", context.packageName)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.5.dp),
        modifier = modifier.height(100.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image (Akan muncul jika Anda sudah menambahkan gambar bg_10k.jpg dll di folder drawable)
            if (resId != 0) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = "Background Uang",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Overlay gelap agar teks tetap bisa terbaca jelas di atas gambar
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                )
            }

            // Warna teks menyesuaikan keberadaan background
            val textColor = if (resId != 0) Color.White else MaterialTheme.colorScheme.onBackground
            val subTextColor = if (resId != 0) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatNilai(item.nominal.toDouble()),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = subTextColor
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "${item.jumlah}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                    Text(
                        text = item.tipe,
                        fontSize = 11.sp,
                        color = subTextColor,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DashedPlaceholderCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(100.dp)
            .drawBehind {
                val stroke = Stroke(
                    width = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
                drawRoundRect(
                    color = Color.LightGray.copy(alpha = 0.8f),
                    style = stroke,
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "•••",
            fontSize = 24.sp,
            color = Color.LightGray,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}

// Visual Transformation Kustom untuk memberikan Titik Ribuan otomatis pada Textfield
class RibuanVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val formatter = NumberFormat.getInstance(Locale("in", "ID"))
        val formattedText = try {
            formatter.format(originalText.toLong())
        } catch (e: Exception) {
            originalText
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var dots = 0
                var transformedIndex = 0
                var originalIndex = 0
                while (originalIndex < offset && transformedIndex < formattedText.length) {
                    if (formattedText[transformedIndex] == '.') {
                        dots++
                    } else {
                        originalIndex++
                    }
                    transformedIndex++
                }
                return offset + dots
            }

            override fun transformedToOriginal(offset: Int): Int {
                var originalIndex = 0
                var transformedIndex = 0
                while (transformedIndex < offset && transformedIndex < formattedText.length) {
                    if (formattedText[transformedIndex] != '.') {
                        originalIndex++
                    }
                    transformedIndex++
                }
                return originalIndex
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}

// Algoritma untuk menghitung pecahan kembalian
private fun hitungPecahan(kembalian: Long): List<PecahanItem> {
    val daftarNominal = listOf(100000L, 50000L, 20000L, 10000L, 5000L, 2000L, 1000L, 500L, 200L, 100L)
    val hasil = mutableListOf<PecahanItem>()
    var sisa = kembalian

    for (nominal in daftarNominal) {
        if (sisa >= nominal) {
            val jumlah = (sisa / nominal).toInt()
            sisa %= nominal

            // Logika sederhana: >= 1000 adalah lembar uang kertas, < 1000 adalah koin logam
            val tipe = if (nominal >= 1000L) "lembar" else "koin"

            hasil.add(PecahanItem(nominal, jumlah, tipe))
        }
    }
    return hasil
}

// Fungsi Helper formatting
private fun formatNilai(number: Double): String {
    if (number == 0.0) return "0"
    val localeID = Locale("in", "ID")
    val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
    formatRupiah.maximumFractionDigits = 0
    return formatRupiah.format(number).replace("Rp", "").replace(",00", "").trim()
}

@Preview(showBackground = true)
@Composable
fun KembalianScreenPreview() {
    HitungUntungTheme {
        KembalianScreen()
    }
}