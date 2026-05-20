package com.example.cryptoapp

import App
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.example.cryptoapp.payment.GooglePayManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.wallet.PaymentData
import model.PaymentToken
import viewmodel.CryptoViewModel

class MainActivity : ComponentActivity() {

    /**
     * ViewModel held here so the ActivityResultCallback (outside Composition)
     * can call processPaymentToken() / onPaymentError() / onPaymentCancelled().
     */
    private lateinit var cryptoViewModel: CryptoViewModel

    /**
     * Modern Activity Result launcher for the Google Pay payment sheet.
     *
     * Google Pay's loadPaymentData() throws [ResolvableApiException] when it needs
     * to show the payment sheet UI. We extract the PendingIntent from the exception
     * and launch it via [StartIntentSenderForResult] — no deprecated onActivityResult.
     */
    private val googlePayLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                // Payment sheet completed — extract and process the token
                val paymentData = result.data?.let { intent -> PaymentData.getFromIntent(intent) }
                val token = paymentData?.let { data -> GooglePayManager.extractPaymentToken(data) }
                if (token != null) {
                    cryptoViewModel.processPaymentToken(PaymentToken(token))
                } else {
                    cryptoViewModel.onPaymentError("Failed to extract payment token.")
                }
            }
            Activity.RESULT_CANCELED -> {
                // User pressed Back on the payment sheet
                cryptoViewModel.onPaymentCancelled()
            }
            else -> {
                cryptoViewModel.onPaymentError("Payment failed. Please try again.")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cryptoViewModel = ViewModelProvider(this)[CryptoViewModel::class.java]

        setContent {
            App(onPayClick = { launchGooglePay() })
        }
    }

    /**
     * Checks Google Pay availability, then launches the payment sheet.
     *
     * Flow:
     * 1. isReadyToPay → device supports Google Pay?
     * 2. loadPaymentData → throws [ResolvableApiException] → launch sheet via launcher
     * 3. Result returns to [googlePayLauncher] callback above
     */
    private fun launchGooglePay() {
        val paymentsClient = GooglePayManager.createPaymentsClient(this)

        // Step 1: Check device eligibility
        paymentsClient.isReadyToPay(GooglePayManager.buildIsReadyToPayRequest())
            .addOnCompleteListener { readyTask ->
                if (!readyTask.isSuccessful || readyTask.result != true) {
                    cryptoViewModel.onPaymentError(
                        "Google Pay is not available on this device."
                    )
                    return@addOnCompleteListener
                }

                // Step 2: Load payment data (shows the sheet)
                val paymentDataRequest = GooglePayManager.buildPaymentDataRequest(
                    amount   = "10.00",
                    currency = "USD"
                )

                paymentsClient.loadPaymentData(paymentDataRequest)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Completed without re-resolution (rare in TEST mode)
                            val paymentData = task.result
                            val token = paymentData?.let { data -> GooglePayManager.extractPaymentToken(data) }
                            if (token != null) {
                                cryptoViewModel.processPaymentToken(PaymentToken(token))
                            } else {
                                cryptoViewModel.onPaymentError("Token extraction failed.")
                            }
                        } else {
                            val ex = task.exception
                            if (ex is ResolvableApiException) {
                                // Normal: show the Google Pay sheet
                                googlePayLauncher.launch(
                                    IntentSenderRequest.Builder(
                                        ex.resolution.intentSender
                                    ).build()
                                )
                            } else {
                                cryptoViewModel.onPaymentError(
                                    ex?.message ?: "Unknown payment error."
                                )
                            }
                        }
                    }
            }
    }
}
