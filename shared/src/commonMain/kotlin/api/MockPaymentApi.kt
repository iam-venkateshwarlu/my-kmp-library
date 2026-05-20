package api

import kotlinx.coroutines.delay
import model.PaymentResult
import model.PaymentToken
import kotlinx.datetime.Clock

/**
 * Mock backend API simulating POST /process-payment.
 *
 * In production, replace this with a real Ktor HTTP client call to your server.
 * Your server then forwards the token to Stripe / Razorpay for actual charging.
 *
 * Request body (conceptual):  { token: String, amount: Double }
 * Response body (conceptual): { status: "SUCCESS"|"FAILED", message: String }
 */
class MockPaymentApi {

    /**
     * Simulates a network call to the payment backend.
     * Always returns SUCCESS when the token is non-blank (safe for TEST mode).
     *
     * @param paymentToken  Google Pay token to forward to backend
     * @param amount        Charge amount
     */
    suspend fun processPayment(
        paymentToken: PaymentToken,
        amount: Double = 10.00
    ): PaymentResult {
        delay(1500) // simulate network round-trip

        return if (paymentToken.token.isNotBlank()) {
            PaymentResult(
                status  = "SUCCESS",
                txnRef  = "TXN_MOCK_${Clock.System.now().toEpochMilliseconds()}",
                message = "Payment of $$amount processed successfully."
            )
        } else {
            PaymentResult(
                status  = "FAILED",
                txnRef  = null,
                message = "Invalid payment token received by backend."
            )
        }
    }
}
