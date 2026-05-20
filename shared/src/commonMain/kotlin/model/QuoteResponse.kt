package model

data class QuoteResponse(
    val fiat: String,
    val crypto: String,
    val amount: Double,
    val cryptoAmount: Double
)
