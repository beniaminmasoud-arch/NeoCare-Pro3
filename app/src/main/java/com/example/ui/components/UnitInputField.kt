package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.TealLightContainer
import com.example.ui.theme.TealPrimary

@Composable
fun UnitInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    helperText: String? = null,
    isDecimal: Boolean = true,
    testTag: String = "unit_input_field"
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            // Allow numbers and decimal point
            val filtered = if (isDecimal) {
                input.filter { it.isDigit() || it == '.' }
            } else {
                input.filter { it.isDigit() }
            }
            onValueChange(filtered)
        },
        label = {
            Text(text = label, fontSize = 12.sp)
        },
        suffix = if (unit != null) {
            {
                Text(
                    text = unit,
                    color = TealPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        } else null,
        supportingText = if (helperText != null) {
            {
                Text(text = helperText, fontSize = 10.sp, color = SlateMuted)
            }
        } else null,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number
        ),
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TealPrimary,
            unfocusedBorderColor = SlateBorder,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}
