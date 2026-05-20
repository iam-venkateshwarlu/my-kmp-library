package payment

import model.PaymentResult

/**
 * Sealed class representing every possible UI state for the payment flow.
 *
 * The ViewModel exposes a single [PaymentState] property that the shared
 * CryptoScreen observes to show the correct UI (button / spinner / result card).
 */
sealed class PaymentState {

    /** Default state — payment button is visible and ready. */
    object Idle : PaymentState()

    /** UPI intent has been fired; waiting for the UPI app to return a result. */
    object Loading : PaymentState()

    /** UPI app returned a SUCCESS status. */
    data class Success(val result: PaymentResult) : PaymentState()

    /**
     * UPI app returned FAILED / CANCELLED, or no UPI app was installed.
     * @param message  Human-readable error/cancellation message.
     */
    data class Error(val message: String) : PaymentState()
}
