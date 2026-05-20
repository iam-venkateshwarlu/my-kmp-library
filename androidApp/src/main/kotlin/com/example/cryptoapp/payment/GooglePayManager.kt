package com.example.cryptoapp.payment

import android.content.Context
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import org.json.JSONArray
import org.json.JSONObject

/**
 * Android-only manager for all Google Pay API interactions.
 *
 * Responsibilities:
 *  - Create and configure [PaymentsClient]
 *  - Build [IsReadyToPayRequest] (device eligibility check)
 *  - Build [PaymentDataRequest] (launches the payment sheet)
 *  - Parse [PaymentData] → extract raw payment token
 *
 * Environment: TEST  — switch to ENVIRONMENT_PRODUCTION before release.
 * Gateway:     "example" — replace with "stripe" or "razorpay" + real credentials.
 */
object GooglePayManager {

    // ── Environment ────────────────────────────────────────────────────────────

    private val ENVIRONMENT = WalletConstants.ENVIRONMENT_TEST

    // ── Allowed payment parameters ─────────────────────────────────────────────

    private val ALLOWED_AUTH_METHODS  = JSONArray(listOf("PAN_ONLY", "CRYPTOGRAM_3DS"))
    private val ALLOWED_CARD_NETWORKS = JSONArray(listOf("AMEX", "DISCOVER", "MASTERCARD", "VISA"))

    private val cardParameters: JSONObject
        get() = JSONObject().apply {
            put("allowedAuthMethods", ALLOWED_AUTH_METHODS)
            put("allowedCardNetworks", ALLOWED_CARD_NETWORKS)
            put("billingAddressRequired", false)
        }

    // ── Tokenization specification ─────────────────────────────────────────────
    // For Stripe  → gateway="stripe", stripe:version, stripe:publishableKey
    // For Razorpay → gateway="razorpay", gatewayMerchantId

    private val tokenizationSpec: JSONObject
        get() = JSONObject().apply {
            put("type", "PAYMENT_GATEWAY")
            put("parameters", JSONObject().apply {
                put("gateway", "example")
                put("gatewayMerchantId", "exampleGatewayMerchantId")
            })
        }

    // ── Card payment method (with tokenization, for PaymentDataRequest) ────────

    private val cardPaymentMethod: JSONObject
        get() = JSONObject().apply {
            put("type", "CARD")
            put("parameters", cardParameters)
            put("tokenizationSpecification", tokenizationSpec)
        }

    // ── Card base (no tokenization, for IsReadyToPayRequest) ──────────────────

    private val cardPaymentMethodBase: JSONObject
        get() = JSONObject().apply {
            put("type", "CARD")
            put("parameters", cardParameters)
        }

    // ── Public API ─────────────────────────────────────────────────────────────

    /** Creates a [PaymentsClient] scoped to the given [context]. */
    fun createPaymentsClient(context: Context): PaymentsClient =
        Wallet.getPaymentsClient(
            context,
            Wallet.WalletOptions.Builder()
                .setEnvironment(ENVIRONMENT)
                .build()
        )

    /** Builds the request used to check if this device supports Google Pay. */
    fun buildIsReadyToPayRequest(): IsReadyToPayRequest {
        val json = JSONObject().apply {
            put("apiVersion", 2)
            put("apiVersionMinor", 0)
            put("allowedPaymentMethods", JSONArray().put(cardPaymentMethodBase))
        }
        return IsReadyToPayRequest.fromJson(json.toString())
    }

    /**
     * Builds the [PaymentDataRequest] that configures the Google Pay payment sheet.
     *
     * @param amount    Charge amount with 2 decimal places, e.g. "10.00"
     * @param currency  ISO 4217 currency code, e.g. "USD"
     */
    fun buildPaymentDataRequest(
        amount: String = "10.00",
        currency: String = "USD"
    ): PaymentDataRequest {
        val json = JSONObject().apply {
            put("apiVersion", 2)
            put("apiVersionMinor", 0)
            put("allowedPaymentMethods", JSONArray().put(cardPaymentMethod))
            put("transactionInfo", JSONObject().apply {
                put("totalPrice", amount)
                put("totalPriceStatus", "FINAL")
                put("currencyCode", currency)
                put("countryCode", "US")
            })
            put("merchantInfo", JSONObject().apply {
                put("merchantName", "Crypto App")
                // merchantId required in PRODUCTION — leave empty for TEST
            })
        }
        return PaymentDataRequest.fromJson(json.toString())
    }

    /**
     * Extracts the raw payment token from [PaymentData].
     * Token path: paymentMethodData → tokenizationData → token
     *
     * @return Raw token string to send to backend, or null on parse failure.
     */
    fun extractPaymentToken(paymentData: PaymentData): String? = try {
        val root          = JSONObject(paymentData.toJson())
        val paymentMethod = root.getJSONObject("paymentMethodData")
        val tokenData     = paymentMethod.getJSONObject("tokenizationData")
        tokenData.getString("token")
    } catch (e: Exception) {
        null
    }
}
