package repository

import api.MockBanxaApi
import model.QuoteResponse

class CryptoRepository(
    private val api: MockBanxaApi
) {

    suspend fun getQuote(): QuoteResponse {
        return api.getQuote()
    }
}
