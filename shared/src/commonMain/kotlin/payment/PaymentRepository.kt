package payment

import api.MockPaymentApi
import model.PaymentResult
import model.PaymentToken

/**
 * Repository mediating between the ViewModel and the payment backend.
 * In production: swap [MockPaymentApi] for a real Ktor HTTP client.
 */
class PaymentRepository(
    private val api: MockPaymentApi = MockPaymentApi()
) {
    suspend fun processPayment(token: PaymentToken, amount: Double = 10.00): PaymentResult {
        return api.processPayment(token, amount)
    }
}
