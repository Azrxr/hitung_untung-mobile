package com.azrxtech.hitunguntung.ui.home.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azrxtech.hitunguntung.ui.theme.HitungUntungTheme

@Composable
fun TopBarSection() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Image(
            painter = painterResource(id = com.azrxtech.hitunguntung.R.drawable.ic_brand),
            contentDescription = "Logo Aplikasi",
            Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Kalkulator Dagang",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TopBarSectionPreview() {
    HitungUntungTheme {
        TopBarSection()
    }
}

