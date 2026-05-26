package viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import api.MockBanxaApi
import kotlinx.coroutines.launch
import model.CardDetails
import model.PaymentToken
import model.QuoteResponse
import notification.NotificationService
import notification.PushNotificationEvent
import payment.PaymentState
import payment.PaymentUseCase
import repository.CryptoRepository

class CryptoViewModel : ViewModel() {

    private val repository          = CryptoRepository(MockBanxaApi())
    private val paymentUseCase      = PaymentUseCase()
    private val notificationService = NotificationService()

    // ── Notification State ─────────────────────────────────────────────────────

    /** Non-null when a foreground push notification has been received. */
    var notificationEvent by mutableStateOf<PushNotificationEvent?>(null)
        private set

    /** Called by the UI to dismiss the in-app notification banner. */
    fun dismissNotification() {
        notificationEvent = null
    }

    // ── Card Management State ──────────────────────────────────────────────────

    var savedCards by mutableStateOf<List<CardDetails>>(emptyList())
        private set

    var showAddCardForm by mutableStateOf(false)
        private set

    fun onAddCardClick() {
        showAddCardForm = true
    }

    fun onDismissAddCard() {
        showAddCardForm = false
    }

    fun saveCard(card: CardDetails) {
        savedCards = savedCards + card
        showAddCardForm = false
    }

    // ── Existing crypto quote state ────────────────────────────────────────────

    var state by mutableStateOf<QuoteResponse?>(null)
        private set

    fun loadQuote() {
        viewModelScope.launch {
            state = repository.getQuote()
        }
    }

    // ── Google Pay payment state ───────────────────────────────────────────────

    var paymentState by mutableStateOf<PaymentState>(PaymentState.Idle)
        private set

    /** Called by UI when the "Buy with Google Pay" button is tapped. */
    fun startPayment() {
        paymentState = PaymentState.Loading
    }

    /**
     * Called by MainActivity after Google Pay returns a valid token.
     * Sends the token to the mock backend via [PaymentUseCase].
     */
    fun processPaymentToken(token: PaymentToken) {
        viewModelScope.launch {
            paymentState = PaymentState.Loading
            try {
                val result = paymentUseCase.execute(token)
                paymentState = when (result.status) {
                    "SUCCESS" -> PaymentState.Success(result)
                    else      -> PaymentState.Error(result.message ?: "Payment failed.")
                }
            } catch (e: Exception) {
                paymentState = PaymentState.Error(e.message ?: "Unexpected error.")
            }
        }
    }

    /** Called when the user dismisses the Google Pay sheet. */
    fun onPaymentCancelled() {
        paymentState = PaymentState.Error("Payment was cancelled.")
    }

    /** Called when a Google Pay API or backend error occurs. */
    fun onPaymentError(message: String) {
        paymentState = PaymentState.Error(message)
    }

    /** Resets payment state back to Idle when user dismisses the result card. */
    fun resetPaymentState() {
        paymentState = PaymentState.Idle
    }

    // ── Firebase Push Notification Initialisation ──────────────────────────────

    init {
        // Retrieve and log the FCM token once at ViewModel creation
        notificationService.getToken { token ->
            if (token != null) {
                // In production: send `token` to your backend to target this device
                println("FCMToken: $token")
            }
        }

        // Register a foreground message listener — updates notificationEvent state
        // which the UI observes to show the in-app banner
        notificationService.listenForMessages { event ->
            notificationEvent = event
        }
    }
}
