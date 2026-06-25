package com.genralstaff.home.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.genralstaff.R
import com.genralstaff.responseModel.ShopItemsResponse
import com.genralstaff.utils.SelectedOptionChoice
import com.genralstaff.utils.SelectedOptionGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.DecimalFormat

class ProductOptionsBottomSheet : BottomSheetDialogFragment() {

    lateinit var product: ShopItemsResponse.Body.Data
    var initialQuantity: Int? = null
    var initialSelectedOptions: List<SelectedOptionGroup>? = null
    var onAdd: ((ShopItemsResponse.Body.Data, List<SelectedOptionGroup>, Int) -> Unit)? = null

    private var quantity = 1
    private val selections = mutableMapOf<Int, MutableSet<Int>>()

    private lateinit var tvQuantity: TextView
    private lateinit var btnAdd: android.widget.Button
    private lateinit var optionGroupsContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_product_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preloadInitialSelections()

        val tvProductName = view.findViewById<TextView>(R.id.tvProductName)
        val tvBasePrice = view.findViewById<TextView>(R.id.tvBasePrice)
        val btnClose = view.findViewById<View>(R.id.btnClose)
        val btnMinus = view.findViewById<View>(R.id.btnMinus)
        val btnPlus = view.findViewById<View>(R.id.btnPlus)
        tvQuantity = view.findViewById(R.id.tvQuantity)
        btnAdd = view.findViewById(R.id.btnAdd)
        optionGroupsContainer = view.findViewById(R.id.optionGroupsContainer)

        tvProductName.text = product.name
        val basePrice = product.price?.toDoubleOrNull() ?: 0.0
        tvBasePrice.text = "${formatPrice(basePrice)} MRU"
        tvQuantity.text = "$quantity"

        btnClose.setOnClickListener { dismiss() }
        btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                tvQuantity.text = "$quantity"
                updateAddButtonTitle()
            }
        }
        btnPlus.setOnClickListener {
            quantity++
            tvQuantity.text = "$quantity"
            updateAddButtonTitle()
        }

        renderOptionGroups()
        updateAddButtonTitle()

        btnAdd.setOnClickListener { tryAdd() }
    }

    private fun preloadInitialSelections() {
        initialQuantity?.let { quantity = it }
        val initialOptions = initialSelectedOptions ?: return
        val groups = product.option_groups ?: return

        for (initialGroup in initialOptions) {
            val groupIndex = groups.indexOfFirst { it.name == initialGroup.groupName }
            if (groupIndex == -1) continue
            val group = groups[groupIndex]
            val choiceIndexes = mutableSetOf<Int>()
            for (chosenChoice in initialGroup.choices) {
                val choiceIndex = group.choices.indexOfFirst { it.name == chosenChoice.name }
                if (choiceIndex != -1) choiceIndexes.add(choiceIndex)
            }
            if (choiceIndexes.isNotEmpty()) selections[groupIndex] = choiceIndexes
        }
    }

    private fun renderOptionGroups() {
        optionGroupsContainer.removeAllViews()
        val groups = product.option_groups ?: return

        for ((groupIndex, group) in groups.withIndex()) {
            val groupView = LayoutInflater.from(context).inflate(R.layout.item_option_group, optionGroupsContainer, false)
            val tvGroupName = groupView.findViewById<TextView>(R.id.tvGroupName)
            val tvRequiredBadge = groupView.findViewById<TextView>(R.id.tvRequiredBadge)
            val choicesContainer = groupView.findViewById<LinearLayout>(R.id.choicesContainer)

            tvGroupName.text = group.name
            tvRequiredBadge.visibility = if (group.required == 1) View.VISIBLE else View.GONE

            for ((choiceIndex, choice) in group.choices.withIndex()) {
                val choiceView = LayoutInflater.from(context).inflate(R.layout.item_option_choice, choicesContainer, false)
                val choiceRow = choiceView.findViewById<LinearLayout>(R.id.choiceRow)
                val ivCheck = choiceView.findViewById<ImageView>(R.id.ivCheck)
                val tvChoiceName = choiceView.findViewById<TextView>(R.id.tvChoiceName)
                val tvChoicePrice = choiceView.findViewById<TextView>(R.id.tvChoicePrice)

                tvChoiceName.text = choice.name
                val price = choice.price.toDoubleOrNull() ?: 0.0
                tvChoicePrice.text = if (price > 0) "+${formatPrice(price)} MRU" else ""

                val isSelected = selections[groupIndex]?.contains(choiceIndex) ?: false
                styleChoiceRow(choiceRow, ivCheck, isSelected)

                choiceRow.setOnClickListener {
                    handleChoiceTap(groupIndex, choiceIndex, group.type)
                }

                choicesContainer.addView(choiceView)
            }

            optionGroupsContainer.addView(groupView)
        }
    }

    private fun handleChoiceTap(groupIndex: Int, choiceIndex: Int, groupType: String) {
        val groupSelections = selections.getOrPut(groupIndex) { mutableSetOf() }

        if (groupType == "multiple") {
            if (groupSelections.contains(choiceIndex)) groupSelections.remove(choiceIndex)
            else groupSelections.add(choiceIndex)
        } else {
            if (groupSelections.contains(choiceIndex)) {
                groupSelections.clear()
            } else {
                groupSelections.clear()
                groupSelections.add(choiceIndex)
            }
        }

        renderOptionGroups()
        updateAddButtonTitle()
    }

    private fun styleChoiceRow(row: LinearLayout, ivCheck: ImageView, isSelected: Boolean) {
        row.setBackgroundResource(if (isSelected) R.drawable.option_choice_bg_selected else R.drawable.option_choice_bg)
        ivCheck.setImageResource(if (isSelected) R.drawable.ic_circle_checked else R.drawable.ic_circle_unchecked)
    }

    private fun tryAdd() {
        val groups = product.option_groups
        if (groups != null) {
            for ((index, group) in groups.withIndex()) {
                if (group.required == 1) {
                    val sel = selections[index]
                    if (sel.isNullOrEmpty()) {
                        showRequiredAlert(group.name)
                        return
                    }
                }
            }
        }

        val selectedGroups = buildSelectedGroups()
        onAdd?.invoke(product, selectedGroups, quantity)
        dismiss()
    }

    private fun showRequiredAlert(groupName: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Sélection requise")
            .setMessage("Veuillez choisir une option pour \"$groupName\".")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun buildSelectedGroups(): List<SelectedOptionGroup> {
        val groups = product.option_groups ?: return emptyList()
        val result = mutableListOf<SelectedOptionGroup>()
        for ((groupIndex, group) in groups.withIndex()) {
            val choiceIndexes = selections[groupIndex] ?: continue
            if (choiceIndexes.isEmpty()) continue
            val chosen = mutableListOf<SelectedOptionChoice>()
            for (choiceIndex in choiceIndexes) {
                val choice = group.choices.getOrNull(choiceIndex) ?: continue
                val price = choice.price.toDoubleOrNull() ?: 0.0
                chosen.add(SelectedOptionChoice(choice.name, price))
            }
            result.add(SelectedOptionGroup(group.name, chosen))
        }
        return result
    }

    private fun updateAddButtonTitle() {
        val basePrice = product.price?.toDoubleOrNull() ?: 0.0
        var optionsPrice = 0.0
        val groups = product.option_groups
        for ((groupIndex, choiceIndexes) in selections) {
            val group = groups?.getOrNull(groupIndex) ?: continue
            for (idx in choiceIndexes) {
                val choice = group.choices.getOrNull(idx) ?: continue
                optionsPrice += choice.price.toDoubleOrNull() ?: 0.0
            }
        }
        val total = (basePrice + optionsPrice) * quantity
        btnAdd.text = "Ajouter • ${formatPrice(total)} MRU"
    }

    private fun formatPrice(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            DecimalFormat("#.##").format(value)
        }
    }
}