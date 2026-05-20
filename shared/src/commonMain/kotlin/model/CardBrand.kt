package model

enum class CardBrand(
    val displayName: String,
    val prefixes: List<String>,
    val cardLength: Int,
    val cvvLength: Int
) {
    VISA("Visa", listOf("4"), 16, 3),
    MASTERCARD("Mastercard", listOf("51","52","53","54","55","22","23","24","25","26","27"), 16, 3),
    AMEX("Amex", listOf("34","37"), 15, 4),
    DISCOVER("Discover", listOf("6011","65","644","645","646","647","648","649"), 16, 3),
    UNKNOWN("Card", emptyList(), 16, 3);

    companion object {
        fun detect(cardNumber: String): CardBrand {
            val digits = cardNumber.filter { it.isDigit() }
            return values().firstOrNull { brand ->
                brand.prefixes.any { prefix -> digits.startsWith(prefix) }
            } ?: UNKNOWN
        }
    }
}
