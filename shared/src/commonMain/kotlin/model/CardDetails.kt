package model

/**
 * Represents a saved payment card.
 *
 * @param id             Unique identifier (random string)
 * @param cardholderName Name printed on the card
 * @param cardNumber     Full card number, digits only (no spaces)
 * @param expiryMonth    2-digit expiry month  e.g. "07"
 * @param expiryYear     2-digit expiry year   e.g. "27"
 * @param cardBrand      Detected brand (VISA, MASTERCARD, …)
 * @param isDefault      Whether this is the user's default card
 */
data class CardDetails(
    val id: String,
    val cardholderName: String,
    val cardNumber: String,
    val expiryMonth: String,
    val expiryYear: String,
    val cardBrand: CardBrand,
    val isDefault: Boolean = false
) {
    /** e.g. "**** **** **** 1234" */
    val maskedNumber: String
        get() = when (cardBrand) {
            CardBrand.AMEX -> "**** ****** *${cardNumber.takeLast(4)}"
            else           -> "**** **** **** ${cardNumber.takeLast(4)}"
        }

    /** e.g. "07/27" */
    val expiry: String get() = "$expiryMonth/$expiryYear"
}
