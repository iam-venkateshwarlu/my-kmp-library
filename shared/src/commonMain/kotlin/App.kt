import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import ui.CryptoScreen
import viewmodel.CryptoViewModel

/**
 * Root composable entry point shared between Android and iOS.
 *
 * @param onPayClick Platform-specific callback to launch the UPI Intent.
 *                   - Android: MainActivity wires this to launchUpiPayment()
 *                   - iOS: defaults to an empty lambda — no UPI code runs
 */
@Composable
fun App(onPayClick: () -> Unit = {}) {
    MaterialTheme {
        CryptoScreen(
            vm         = CryptoViewModel(),
            onPayClick = onPayClick
        )
    }
}
