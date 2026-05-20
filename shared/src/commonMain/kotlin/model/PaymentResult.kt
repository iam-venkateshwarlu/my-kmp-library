package model

/**
 * Represents the outcome of a UPI payment attempt.
 *
 * @param status   "SUCCESS", "FAILED", or "CANCELLED"
 * @param txnRef   UPI transaction reference number returned by the UPI app (if any)
 * @param message  Human-readable status message for display in the UI
 */
data class PaymentResult(
    val status: String,
    val txnRef: String?,
    val message: String?
)
