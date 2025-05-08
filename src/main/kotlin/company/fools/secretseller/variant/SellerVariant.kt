package company.fools.secretseller.variant

import java.util.*

enum class SellerVariant(val id: Int) {
    DEFAULT(0),
    ASIMOV(1);

    companion object {
        private val BY_ID = entries.sortedBy { it.id }.toTypedArray()
        fun byId(id: Int): SellerVariant = BY_ID[id % BY_ID.size]
    }
}