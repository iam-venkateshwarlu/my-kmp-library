package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.CardBrand
import model.CardDetails
import kotlin.random.Random

/**
 * A simple form to collect card details and save them to the ViewModel.
 */
@Composable
fun AddCardForm(
    onSave: (CardDetails) -> Unit,
    onCancel: () -> Unit
) {
    var holderName by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var expiryMonth by remember { mutableStateOf("") }
    var expiryYear by remember { mutableStateOf("") }

    // Brand detection logic from CardBrand enum
    val detectedBrand = CardBrand.detect(number)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Add New Card",
                fontSize = 20.sp,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = holderName,
                onValueChange = { holderName = it },
                label = { Text("Cardholder Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = number,
                onValueChange = { if (it.length <= 16) number = it.filter { c -> c.isDigit() } },
                label = { Text("Card Number (${detectedBrand.displayName})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = expiryMonth,
                    onValueChange = { if (it.length <= 2) expiryMonth = it.filter { c -> c.isDigit() } },
                    label = { Text("MM") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = expiryYear,
                    onValueChange = { if (it.length <= 2) expiryYear = it.filter { c -> c.isDigit() } },
                    label = { Text("YY") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (holderName.isNotBlank() && number.length >= 13) {
                            onSave(
                                CardDetails(
                                    id = Random.nextInt(10000, 99999).toString(),
                                    cardholderName = holderName,
                                    cardNumber = number,
                                    expiryMonth = expiryMonth,
                                    expiryYear = expiryYear,
                                    cardBrand = detectedBrand
                                )
                            )
                        }
                    },
                    enabled = holderName.isNotBlank() && number.length >= 13
                ) {
                    Text("Save Card")
                }
            }
        }
    }
}
