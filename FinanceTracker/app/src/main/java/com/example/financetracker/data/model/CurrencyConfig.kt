package com.example.financetracker.data.model

enum class CurrencyPosition {
    LEFT, RIGHT
}

data class CurrencyConfig(
    val symbol: String = "€",
    val position: CurrencyPosition = CurrencyPosition.RIGHT
) {
    fun format(amount: String): String {
        return if (position == CurrencyPosition.LEFT) {
            "$symbol $amount"
        } else {
            "$amount $symbol"
        }
    }

    fun format(amount: Double): String {
        val formattedAmount = java.text.DecimalFormat("#,###.##").format(amount)
        return format(formattedAmount)
    }
}