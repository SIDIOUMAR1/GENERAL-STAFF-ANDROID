package com.genralstaff.utils

import com.genralstaff.responseModel.ShopItemsResponse

data class SelectedOptionChoice(
    val name: String,
    val price: Double
)

data class SelectedOptionGroup(
    val groupName: String,
    val choices: List<SelectedOptionChoice>
)

data class StaffCartLine(
    val product: ShopItemsResponse.Body.Data,
    var quantity: Int,
    var selectedOptions: List<SelectedOptionGroup> = emptyList()
) {
    val unitPrice: Double
        get() {
            val base = product.price?.toDoubleOrNull() ?: 0.0
            val optionsPrice = selectedOptions.sumOf { g -> g.choices.sumOf { it.price } }
            return base + optionsPrice
        }

    val totalPrice: Double get() = unitPrice * quantity

    val optionsSummary: String
        get() = selectedOptions.flatMap { it.choices.map { c -> c.name } }.joinToString(", ")
}