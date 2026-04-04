package com.azrxtech.hitunguntung.ui.calculator.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azrxtech.hitunguntung.ui.calculator.formatRupiah
import com.azrxtech.hitunguntung.ui.theme.HitungUntungTheme
import java.text.NumberFormat
import java.util.Locale



@Composable
fun Header() {
    val color = MaterialTheme.colorScheme
    Column {
        Text("FITUR UTAMA", color = color.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text("Kulakan", color = color.primary, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Hitung harga jual presisi berdasarkan modal, biaya operasional, dan target profit Anda.",
            color = color.onSurfaceVariant,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun InputCardContainer(
    icon: ImageVector,
    title: String,
    rightContent: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val color = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.surfaceVariant.copy(alpha = 0.32f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icon, contentDescription = title, tint = color.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = color.onBackground)
                }
                rightContent?.invoke()
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun KulakanTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    prefix: String = "",
    suffix: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None
) {
    val color = MaterialTheme.colorScheme
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = color.onSurfaceVariant.copy(alpha = 0.75f)) },
        leadingIcon = if (prefix.isNotEmpty()) { { Text(prefix, fontWeight = FontWeight.Bold, color = color.onBackground) } } else null,
        trailingIcon = if (suffix.isNotEmpty()) { { Text(suffix, fontWeight = FontWeight.Bold, color = color.onSurfaceVariant) } } else null,
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = color.surface,
            unfocusedContainerColor = color.surface,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = color.onBackground,
            unfocusedTextColor = color.onBackground,
            cursorColor = color.primary
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun DividerWithText(text: String) {
    val color = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = color.outline.copy(alpha = 0.35f))
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = color.outline.copy(alpha = 0.35f))
    }
}

@Composable
fun SegmentedButton(isPersen: Boolean, onToggle: (Boolean) -> Unit) {
    val color = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.surfaceVariant.copy(alpha = 0.6f))
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (isPersen) color.primary else Color.Transparent)
                .clickable { onToggle(true) }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "PERSEN\n(%)",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPersen) color.onPrimary else color.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (!isPersen) color.primary else Color.Transparent)
                .clickable { onToggle(false) }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "RUPIAH\n(Rp)",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (!isPersen) color.onPrimary else color.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SummaryBoxData(title: String, value: Double, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.onPrimary.copy(alpha = 0.08f))
            .padding(12.dp)
    ) {
        Column {
            Text(title, color = color.onPrimary.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(formatRupiah(value), color = color.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}



@Preview(showBackground = true)
@Composable
private fun HeaderPreview() {
    HitungUntungTheme { Header() }
}

@Preview(showBackground = true)
@Composable
private fun InputCardContainerPreview() {
    HitungUntungTheme {
        InputCardContainer(icon = Icons.Rounded.Storefront, title = "Data Barang") {
            Text("Preview isi card", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KulakanTextFieldPreview() {
    HitungUntungTheme {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            KulakanTextField(value = "", onValueChange = {}, placeholder = "Contoh: Kopi Sachet Premium")
            KulakanTextField(value = "25000", onValueChange = {}, placeholder = "0", prefix = "Rp", keyboardType = KeyboardType.Number)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DividerWithTextPreview() {
    HitungUntungTheme { DividerWithText(text = "ATAU MASUKKAN") }
}

@Preview(showBackground = true)
@Composable
private fun SegmentedButtonPreview() {
    HitungUntungTheme {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SegmentedButton(isPersen = true, onToggle = {})
            SegmentedButton(isPersen = false, onToggle = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SummaryBoxDataPreview() {
    HitungUntungTheme {
        SummaryBoxData(title = "TOTAL MODAL", value = 1250000.0, modifier = Modifier.fillMaxWidth())
    }
}
