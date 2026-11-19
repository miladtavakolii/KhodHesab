package ir.khodhesab.domain.model

/**
 * Pure domain model representing a user's bank account or card.
 *
 * @property id Unique account identifier.
 * @property bank Associated [Bank] domain model.
 * @property accountIdentifier Account or card number identifier string.
 * @property title User-customized display title.
 * @property currentBalance Current known account balance in Rials.
 * @property lastBalanceUpdate Timestamp (epoch millis) when balance was last updated.
 * @property isActive Flag indicating whether account is active.
 */
data class Account(
    val id: Long = 0,
    val bank: Bank,
    val accountIdentifier: String,
    val title: String,
    val currentBalance: Long = 0L,
    val lastBalanceUpdate: Long = 0L,
    val isActive: Boolean = true
)
