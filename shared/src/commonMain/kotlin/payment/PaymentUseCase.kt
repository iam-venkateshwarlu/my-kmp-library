package payment

import model.PaymentResult
import model.PaymentToken

/**
 * Domain use case: validates the token, then delegates to [PaymentRepository].
 */
class PaymentUseCase(
    private val repository: PaymentRepository = PaymentRepository()
) {
    suspend fun execute(token: PaymentToken, amount: Double = 10.00): PaymentResult {
        require(token.token.isNotBlank()) { "Payment token must not be blank" }
        return repository.processPayment(token, amount)
    }
}
