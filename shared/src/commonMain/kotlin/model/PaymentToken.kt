package model

/**
 * Holds the raw payment token returned by Google Pay.
 * Must be sent to the backend — never process directly in the app.
 *
 * @param token  Raw tokenizationData.token JSON string from Google Pay
 */
data class PaymentToken(val token: String)
