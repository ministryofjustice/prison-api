package uk.gov.justice.hmpps.prison.values

enum class AccountCode(val code: String, val codeName: String) {
  SPENDS("SPND", "spends"),
  SAVINGS("SAV", "savings"),
  CASH("REG", "cash"),
  ;

  companion object {
    fun byCodeName(name: String) = entries.find { it.codeName == name.lowercase() }
    fun exists(name: String) = byCodeName(name) != null
  }
}
