package payment

import model.PaymentResult

/**
 * Platform-agnostic payment utilities for UPI Intent flow.
 *
 * This object lives in [commonMain] so it can be shared across Android and iOS
 * without any platform-specific code. The actual Intent launching happens in the
 * Android-side [MainActivity].
 */
object PaymentManager {

    /**
     * Builds a standards-compliant UPI deep-link URI string.
     *
     * Format: upi://pay?pa=<upiId>&pn=<name>&tn=<note>&am=<amount>&cu=INR
     *
     * @param amount  Payment amount as a string with 2 decimal places (e.g. "10.00")
     * @param upiId   Payee UPI Virtual Payment Address (e.g. "merchant@upi")
     * @param name    Payee display name shown in the UPI app
     * @param note    Short transaction note (default: "Payment")
     * @return        A URI string ready to be wrapped in an Android ACTION_VIEW Intent
     */
    fun buildUpiUri(
        amount: String,
        upiId: String,
        name: String,
        note: String = "Payment"
    ): String {
        // URL-encode spaces in name and note to keep the URI valid
        val encodedName = name.replace(" ", "%20")
        val encodedNote = note.replace(" ", "%20")
        return "upi://pay?pa=$upiId&pn=$encodedName&tn=$encodedNote&am=$amount&cu=INR"
    }

    /**
     * Parses the response string returned by a UPI app after a payment attempt.
     *
     * UPI apps (Google Pay, PhonePe, Paytm, etc.) return a query-string in the
     * Intent result data, typically in the form:
     *   "Status=SUCCESS&txnRef=123456789&responseCode=00"
     *
     * @param response Raw response query-string from the UPI Intent result,
     *                 or null if the user pressed Back / cancelled.
     * @return [PaymentResult] with parsed [status], [txnRef], and [message]
     */
    fun parsePaymentResponse(response: String?): PaymentResult {
        // Null response means the user cancelled (pressed Back)
        if (response == null) {
            return PaymentResult(
                status  = "CANCELLED",
                txnRef  = null,
                message = "Payment was cancelled by the user."
            )
        }

        // Parse "Key=Value&Key=Value" pairs into a map (case-insensitive keys)
        val params: Map<String, String> = response
            .split("&")
            .mapNotNull { pair ->
                val parts = pair.split("=", limit = 2)
                if (parts.size == 2) parts[0].trim() to parts[1].trim()
                else null
            }
            .toMap()

        val status  = params["Status"]?.uppercase() ?: "FAILED"
        val txnRef  = params["txnRef"] ?: params["txnId"]   // different apps use different keys
        val message = when (status) {
            "SUCCESS"   -> "Payment successful! Ref: ${txnRef ?: "N/A"}"
            "CANCELLED" -> "Payment was cancelled."
            else        -> params["responseCode"]
                               ?.let { "Payment failed (Code: $it)." }
                           ?: "Payment failed. Please try again."
        }

        return PaymentResult(
            status  = status,
            txnRef  = txnRef,
            message = message
        )
    }
}
