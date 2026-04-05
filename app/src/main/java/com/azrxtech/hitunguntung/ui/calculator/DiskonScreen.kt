package com.azrxtech.hitunguntung.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azrxtech.hitunguntung.ui.home.component.TopBarSection
import com.azrxtech.hitunguntung.ui.theme.HitungUntungTheme
import com.azrxtech.hitunguntung.util.RibuanVisualTransformation
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt
import androidx.activity.compose.BackHandler

@Composable
fun DiskonScreen(
    onBackClick: (() -> Unit)? = null
) {
    if (onBackClick != null) {
        BackHandler { onBackClick() }
    }
    val scrollState = rememberScrollState()

    // State Input
    var hargaAwalInput by remember { mutableStateOf("") }
    var potonganInput by remember { mutableStateOf("") }
    var isPersen by remember { mutableStateOf(true) } // true = %, false = Rp

    // Logika Perhitungan Reaktif
    val hargaAwal = hargaAwalInput.toDoubleOrNull() ?: 0.0
    val inputPotongan = potonganInput.toDoubleOrNull() ?: 0.0

    // Perhitungan Total Hemat dan Harga Akhir
    val totalHemat = if (isPersen) {
        hargaAwal * (inputPotongan / 100.0)
    } else {
        inputPotongan
    }.coerceAtMost(hargaAwal) // Hemat tidak boleh lebih dari harga awal

    val hargaAkhir = (hargaAwal - totalHemat).coerceAtLeast(0.0)

    // Perhitungan Persentase Ekuivalen (untuk tampilan Margin dan Tips)
    val diskonPersen = if (hargaAwal > 0) {
        (totalHemat / hargaAwal) * 100.0
    } else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {

        TopBarSection()

        Spacer(modifier = Modifier.height(24.dp))

        // Header Title
        Text(
            text = "Diskon",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Hitung potongan harga dengan presisi editorial.",
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Input Form Card
        InputDiskonCard(
            hargaAwalValue = hargaAwalInput,
            onHargaAwalChange = { hargaAwalInput = it.filter(Char::isDigit) },
            potonganValue = potonganInput,
            onPotonganChange = { potonganInput = it.filter(Char::isDigit) },
            isPersen = isPersen,
            onTogglePersen = { isPersen = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Baris Info: Total Hemat & Margin Diskon
        SavingsAndMarginRow(
            totalHemat = totalHemat,
            marginPersen = diskonPersen
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Kartu Utama: Harga Akhir Pembeli
        FinalPriceCard(hargaAkhir = hargaAkhir)

        Spacer(modifier = Modifier.height(24.dp))

        // Tips Dagang Dinamis berdasarkan besaran diskon
        DynamicTipsCard(diskonPersen = diskonPersen)

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun InputDiskonCard(
    hargaAwalValue: String,
    onHargaAwalChange: (String) -> Unit,
    potonganValue: String,
    onPotonganChange: (String) -> Unit,
    isPersen: Boolean,
    onTogglePersen: (Boolean) -> Unit
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
            // Field Harga Awal
            Text(
                text = "Harga Awal",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            DiskonTextField(
                value = hargaAwalValue,
                onValueChange = onHargaAwalChange,
                placeholder = "0",
                prefix = "Rp",
                visualTransformation = RibuanVisualTransformation()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Label Potongan Harga & Segmented Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Potongan Harga",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
                SegmentedToggleButton(isPersen = isPersen, onToggle = onTogglePersen)
            }
            Spacer(modifier = Modifier.height(8.dp))
            DiskonTextField(
                value = potonganValue,
                onValueChange = onPotonganChange,
                placeholder = if (isPersen) "Contoh: 15" else "Contoh: 50000",
                prefix = if (!isPersen) "Rp" else "",
                suffix = if (isPersen) "%" else "",
                visualTransformation = if (!isPersen) RibuanVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol Hitung Potongan (UI saja, logika sudah reaktif)
            Button(
                onClick = { /* Fokus dilepas atau bisa simpan ke history */ },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Hitung Potongan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun DiskonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    prefix: String = "",
    suffix: String = "",
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = placeholder, color = Color.LightGray) },
        leadingIcon = if (prefix.isNotEmpty()) {
            {
                Text(
                    text = prefix,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        } else null,
        trailingIcon = if (suffix.isNotEmpty()) {
            {
                Text(
                    text = suffix,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        visualTransformation = visualTransformation,
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
            fontSize = 16.sp
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun SegmentedToggleButton(isPersen: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.LightGray.copy(alpha = 0.4f))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tombol Persen
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (isPersen) MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable { onToggle(true) }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Persen (%)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPersen) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
        // Tombol Rupiah
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (!isPersen) MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable { onToggle(false) }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Rupiah (Rp)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (!isPersen) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun SavingsAndMarginRow(totalHemat: Double, marginPersen: Double) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), // Light blue-gray from theme
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ikon Piggy Bank
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD6E4E5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Savings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Total Hemat Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TOTAL HEMAT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Text(
                    text = "Rp ${formatNilai(totalHemat)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Garis Pemisah Vertikal
            HorizontalDivider(
                color = Color.LightGray,
                modifier = Modifier
                    .height(30.dp)
                    .width(1.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Margin Diskon Text
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "MARGIN",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Text(
                    text = "${marginPersen.roundToInt()}%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun FinalPriceCard(hargaAkhir: Double) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "HARGA AKHIR PEMBELI",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "Rp ",
                    fontSize = 20.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = formatNilai(hargaAkhir),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DynamicTipsCard(diskonPersen: Double) {
    // Logika pesan dinamis berdasarkan hasil margin/persentase diskon
    val tipsDescription = when {
        diskonPersen == 0.0 ->
            "Belum ada diskon yang diterapkan. Masukkan nilai harga awal dan potongan untuk melihat simulasi penjualan."
        diskonPersen > 0.0 && diskonPersen <= 10.0 ->
            "Memberikan diskon tipis (${diskonPersen.roundToInt()}%) sangat efektif untuk produk fast-moving atau sebagai pemanis syarat 'gratis ongkir'."
        diskonPersen > 10.0 && diskonPersen <= 30.0 ->
            "Memberikan diskon ${diskonPersen.roundToInt()}% pada produk bundle biasanya meningkatkan volume penjualan hingga 2.4x lipat dibanding diskon satuan."
        diskonPersen > 30.0 && diskonPersen <= 50.0 ->
            "Diskon besar (${diskonPersen.roundToInt()}%)! Sangat cocok untuk strategi cuci gudang (clearance sale) atau event flash sale untuk menarik trafik tinggi."
        else ->
            "Diskon ekstrem (${diskonPersen.roundToInt()}%)! Pastikan HPP Anda masih aman, atau gunakan produk ini sebagai 'loss leader' (produk pancingan) untuk menjual produk lain."
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment  = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = "Tips",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)) {
                        append("Tips Dagang: ")
                    }
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))) {
                        append(tipsDescription)
                    }
                },
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
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
fun DiskonScreenPreview() {
    HitungUntungTheme {
        DiskonScreen()
    }
}