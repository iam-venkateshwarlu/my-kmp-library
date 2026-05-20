package ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import payment.PaymentState
import viewmodel.CryptoViewModel

/**
 * Main screen composable shared across Android and iOS.
 *
 * Responsibilities:
 *  - Display the crypto quote (existing feature, unchanged)
 *  - Display a "Pay with Google Pay" button section
 *  - Show a loading spinner while the UPI app is open
 *  - Show a success / error result card when the UPI app returns
 *
 * @param vm          Shared ViewModel holding quote + payment state
 * @param onPayClick  Platform callback to launch the UPI Intent (Android only).
 *                    Defaults to empty lambda so iOS compiles without changes.
 */
@Composable
fun CryptoScreen(
    vm: CryptoViewModel,
    onPayClick: () -> Unit = {}
) {
    val data         = vm.state
    val paymentState = vm.paymentState
    val scrollState  = rememberScrollState()

    // Load the crypto quote once when the screen enters composition
    LaunchedEffect(Unit) {
        vm.loadQuote()
    }

    Column(
        modifier             = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment  = Alignment.CenterHorizontally
    ) {

        // ── Existing crypto quote section ──────────────────────────────────────

        Text(
            text       = "Buy Crypto",
            fontSize   = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        if (data != null) {
            Text("Fiat: ${data.fiat}")
            Text("Crypto: ${data.crypto}")
            Text("Amount: ${data.amount}")
            Text("Receive: ${data.cryptoAmount}")

            Spacer(Modifier.height(12.dp))

            Button(onClick = {}) {
                Text("Buy Now")
            }
        } else {
            // Quote is still loading
            CircularProgressIndicator()
        }

        // ── Divider separating existing content from payment section ──────────

        Spacer(Modifier.height(32.dp))
        HorizontalDivider()
        Spacer(Modifier.height(24.dp))

        // ── Payment section ────────────────────────────────────────────────────

        Text(
            text       = "Payments",
            fontSize   = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(16.dp))

        when (paymentState) {

            // UPI intent is open — show spinner + message
            is PaymentState.Loading -> {
                CircularProgressIndicator()
                Spacer(Modifier.height(10.dp))
                Text(
                    text  = "Opening payment app…",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            // Payment succeeded — show green result card
            is PaymentState.Success -> {
                PaymentResultCard(
                    message   = paymentState.result.message ?: "Payment Successful",
                    isSuccess = true,
                    onDismiss = { vm.resetPaymentState() }
                )
            }

            // Payment failed or cancelled — show red result card
            is PaymentState.Error -> {
                PaymentResultCard(
                    message   = paymentState.message,
                    isSuccess = false,
                    onDismiss = { vm.resetPaymentState() }
                )
            }

            // Default idle state — show the Pay button
            is PaymentState.Idle -> {
                Button(
                    onClick = {
                        vm.startPayment()   // transition to Loading
                        onPayClick()        // platform fires the UPI Intent
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1a73e8)  // Google Blue
                    ),
                    shape    = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text       = "Buy with Google Pay",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                }
            }
        }

        // ── Saved Cards section ────────────────────────────────────────────────

        Spacer(Modifier.height(32.dp))
        HorizontalDivider()
        Spacer(Modifier.height(24.dp))

        Text(
            text       = "Your Cards",
            fontSize   = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(16.dp))

        if (vm.showAddCardForm) {
            AddCardForm(
                onSave = { vm.saveCard(it) },
                onCancel = { vm.onDismissAddCard() }
            )
        } else {
            vm.savedCards.forEach { card ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(card.cardBrand.displayName, fontWeight = FontWeight.Bold)
                        Text(card.maskedNumber)
                        Text("Expires: ${card.expiry}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { vm.onAddCardClick() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add New Card")
            }
        }
    }
}

/**
 * Styled card displayed after a payment attempt completes.
 *
 * @param message   Result message to show the user
 * @param isSuccess true → green success card, false → red error card
 * @param onDismiss Called when the user taps "Dismiss", resets state to Idle
 */
@Composable
private fun PaymentResultCard(
    message: String,
    isSuccess: Boolean,
    onDismiss: () -> Unit
) {
    val backgroundColor = if (isSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val textColor       = if (isSuccess) Color(0xFF2E7D32) else Color(0xFFC62828)
    val label           = if (isSuccess) "✅  Payment Successful" else "❌  Payment Failed"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = label,
                fontWeight = FontWeight.Bold,
                color      = textColor,
                fontSize   = 16.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text     = message,
                color    = textColor,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = textColor)
            }
        }
    }
}
