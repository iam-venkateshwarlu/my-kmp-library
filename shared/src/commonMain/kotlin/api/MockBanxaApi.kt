package api

import kotlinx.coroutines.delay
import model.QuoteResponse

class MockBanxaApi {

    suspend fun getQuote(): QuoteResponse {

        delay(1000)

        return QuoteResponse(
            fiat = "USD",
            crypto = "BTC",
            amount = 500.0,
            cryptoAmount = 0.005
        )
    }
}
