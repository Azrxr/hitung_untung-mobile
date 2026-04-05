package com.azrxtech.hitunguntung.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Inventory
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azrxtech.hitunguntung.ui.calculator.component.DividerWithText
import com.azrxtech.hitunguntung.ui.calculator.component.Header
import com.azrxtech.hitunguntung.ui.calculator.component.InputCardContainer
import com.azrxtech.hitunguntung.ui.calculator.component.KulakanTextField
import com.azrxtech.hitunguntung.ui.calculator.component.SegmentedButton as CalcSegmentedButton
import com.azrxtech.hitunguntung.ui.calculator.component.SummaryComponent
import com.azrxtech.hitunguntung.ui.home.component.TopBarSection
import com.azrxtech.hitunguntung.ui.theme.HitungUntungTheme
import com.azrxtech.hitunguntung.util.RibuanVisualTransformation
import java.text.NumberFormat
import java.util.Locale
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.BackHandler
import com.azrxtech.hitunguntung.customeads.manager.AdManager

@Composable
fun KulakanScreen(
    onBackClick: (() -> Unit)? = null
) {
    // Intercept tombol back untuk registerClick
    if (onBackClick != null) {
        BackHandler { onBackClick() }
    }
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val color = MaterialTheme.colorScheme

    var namaBarang by remember { mutableStateOf("") }
    var jumlahDus by remember { mutableStateOf("") }
    var isiPerDus by remember { mutableStateOf("") }
    var totalHargaBeli by remember { mutableStateOf("") }
    var hargaPerDus by remember { mutableStateOf("") }
    var ongkir by remember { mutableStateOf("") }
    var parkir by remember { mutableStateOf("") }
    var isPersen by remember { mutableStateOf(true) }
    var targetKeuntungan by remember { mutableStateOf("15") }

    val qtyDus = jumlahDus.toIntOrNull() ?: 0
    val qtyIsiPerDus = isiPerDus.toIntOrNull() ?: 0
    val tBeli = totalHargaBeli.toDoubleOrNull() ?: ((hargaPerDus.toDoubleOrNull() ?: 0.0) * qtyDus)
    val tOngkir = ongkir.toDoubleOrNull() ?: 0.0
    val tParkir = parkir.toDoubleOrNull() ?: 0.0
    val tUntungInput = targetKeuntungan.toDoubleOrNull() ?: 0.0

    val totalModal = tBeli + tOngkir + tParkir
    val hppPerDus = if (qtyDus > 0) totalModal / qtyDus else 0.0
    val hppPerPcs = if (qtyIsiPerDus > 0) hppPerDus / qtyIsiPerDus else 0.0
    val targetUntungTotal = if (isPersen) totalModal * (tUntungInput / 100) else tUntungInput
    val hargaJualDus = hppPerDus + (if (qtyDus > 0) targetUntungTotal / qtyDus else 0.0)
    val hargaJualPcs = hppPerPcs + (if (qtyDus > 0 && qtyIsiPerDus > 0) targetUntungTotal / (qtyDus * qtyIsiPerDus) else 0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        TopBarSection()
        Spacer(modifier = Modifier.height(24.dp))
        Header()
        Spacer(modifier = Modifier.height(24.dp))

        InputCardContainer(icon = Icons.Rounded.Inventory, title = "Data Barang") {
            Text("Nama Barang", fontSize = 12.sp, color = color.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            KulakanTextField(
                value = namaBarang,
                onValueChange = { newValue -> namaBarang = newValue },
                placeholder = "Contoh: Kopi Sachet Premium"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Jumlah Dus", fontSize = 12.sp, color = color.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    KulakanTextField(
                        value = jumlahDus,
                        onValueChange = { newValue -> jumlahDus = newValue.filter { it.isDigit() } },
                        placeholder = "0",
                        keyboardType = KeyboardType.Number,
                        visualTransformation = RibuanVisualTransformation()
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Isi per Dus", fontSize = 12.sp, color = color.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    KulakanTextField(
                        value = isiPerDus,
                        onValueChange = { newValue -> isiPerDus = newValue.filter { it.isDigit() } },
                        placeholder = "0",
                        suffix = "PCS",
                        keyboardType = KeyboardType.Number,
                        visualTransformation = RibuanVisualTransformation()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        InputCardContainer(icon = Icons.Rounded.Payments, title = "Pembelian") {
            Text("Total Harga Beli (Semua Dus)", fontSize = 12.sp, color = color.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            KulakanTextField(
                value = totalHargaBeli,
                onValueChange = { newValue ->
                    totalHargaBeli = newValue.filter { it.isDigit() }
                    hargaPerDus = ""
                },
                placeholder = "0",
                prefix = "Rp",
                keyboardType = KeyboardType.Number,
                visualTransformation = RibuanVisualTransformation()
            )

            DividerWithText(text = "ATAU MASUKKAN")

            Text("Harga per Dus", fontSize = 12.sp, color = color.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            KulakanTextField(
                value = hargaPerDus,
                onValueChange = { newValue ->
                    hargaPerDus = newValue.filter { it.isDigit() }
                    totalHargaBeli = ""
                },
                placeholder = "0",
                prefix = "Rp",
                keyboardType = KeyboardType.Number,
                visualTransformation = RibuanVisualTransformation()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        InputCardContainer(icon = Icons.Rounded.LocalShipping, title = "Biaya Tambahan") {
            Text("Ongkir", fontSize = 12.sp, color = color.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            KulakanTextField(
                value = ongkir,
                onValueChange = { newValue -> ongkir = newValue.filter { it.isDigit() } },
                placeholder = "0",
                prefix = "Rp",
                keyboardType = KeyboardType.Number,
                visualTransformation = RibuanVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Parkir & Lainnya", fontSize = 12.sp, color = color.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            KulakanTextField(
                value = parkir,
                onValueChange = { newValue -> parkir = newValue.filter { it.isDigit() } },
                placeholder = "0",
                prefix = "Rp",
                keyboardType = KeyboardType.Number,
                visualTransformation = RibuanVisualTransformation()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        InputCardContainer(
            icon = Icons.AutoMirrored.Rounded.TrendingUp,
            title = "Target\nKeuntungan",
            rightContent = {
                CalcSegmentedButton(
                    isPersen = isPersen,
                    onToggle = { newValue -> isPersen = newValue }
                )
            }
        ) {
            KulakanTextField(
                value = targetKeuntungan,
                onValueChange = { newValue -> targetKeuntungan = newValue.filter { it.isDigit() } },
                placeholder = "Contoh: 15",
                prefix = if (isPersen) "" else "Rp",
                suffix = if (isPersen) "%" else "",
                keyboardType = KeyboardType.Number,
                visualTransformation = if (isPersen) androidx.compose.ui.text.input.VisualTransformation.None else RibuanVisualTransformation()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "*Margin keuntungan akan ditambahkan ke HPP",
                fontSize = 10.sp,
                color = color.onSurfaceVariant,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        val shareText = """
            🧾 Kalkulator Kulakan
            
            Barang: ${namaBarang.ifEmpty { "-" }}
            Jumlah: $qtyDus Dus (${qtyDus * qtyIsiPerDus} pcs)
            
            💰 Modal:
            Total: ${formatRupiah(totalModal)}
            
            🎯 Target:
            Untung: ${formatRupiah(targetUntungTotal)} ${if(isPersen) "($targetKeuntungan%)" else ""}
            
            🏷️ Harga Jual:
            - per pcs: ${formatRupiah(hargaJualPcs)}
            - per dus: ${formatRupiah(hargaJualDus)}
        """.trimIndent()

        SummaryComponent(
            totalModal = totalModal,
            targetUntung = targetUntungTotal,
            hppPerDus = hppPerDus,
            hppPerPcs = hppPerPcs,
            rekomendasiHargaDus = hargaJualDus,
            rekomendasiHargaPcs = hargaJualPcs,
            onShare = {
                AdManager.registerClick()
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            },
            onSalin = {
                AdManager.registerClick()
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Kalkulator Kulakan", shareText)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Tersalin ke papan klip", Toast.LENGTH_SHORT).show()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Dihitung otomatis berdasarkan Standar Akuntansi Dagang Mikro.",
            fontSize = 10.sp,
            color = color.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

fun formatRupiah(number: Double): String {
    if (number == 0.0) return "Rp 0"
    val localeID = Locale.forLanguageTag("id-ID")
    val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
    formatRupiah.maximumFractionDigits = 0
    val formatted = formatRupiah.format(number)
    return formatted.replace("Rp", "Rp ").replace(",00", "")
}

@Preview(showBackground = true)
@Composable
fun KulakanScreenPreview() {
    HitungUntungTheme {
        KulakanScreen()
    }
}