package com.azrxtech.hitunguntung.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.util.Locale

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
