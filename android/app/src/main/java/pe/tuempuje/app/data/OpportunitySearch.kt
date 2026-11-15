package pe.tuempuje.app.data

internal data class OpportunityFilters(
    val query: String = "",
    val category: String = "Todas",
    val location: String = "",
    val mode: String = "",
    val availableOnly: Boolean = false,
    val noExperienceOnly: Boolean = false,
    val recentDays: Int = 0,
    val prioritizeInterests: Boolean = true,
)

internal fun searchOpportunities(
    items: List<Opportunity>, filters: OpportunityFilters, interests: Set<String>, nowSeconds: Long,
): List<Opportunity> {
    val query = filters.query.trim()
    val location = filters.location.trim()
    val matches = items.filter { item ->
        val matchesMode = filters.mode.isBlank() || item.workMode == filters.mode ||
            (filters.mode == "Remoto" && item.workMode == null && item.location.contains("remoto", ignoreCase = true))
        item.status == "published" && (filters.category == "Todas" || item.category == filters.category) &&
            (location.isBlank() || item.location.contains(location, ignoreCase = true)) && matchesMode &&
            (!filters.availableOnly || item.selectedCount < item.vacancies) &&
            (!filters.noExperienceOnly || item.noExperienceRequired == true) &&
            (filters.recentDays == 0 || (item.createdAtSeconds > 0 && item.createdAtSeconds >= nowSeconds - filters.recentDays * 86400L)) &&
            (query.isBlank() || listOf(item.title, item.organization, item.publicBusinessName, item.publicBusinessSector, item.category, item.location)
                .any { it.contains(query, ignoreCase = true) })
    }
    return if (filters.prioritizeInterests) matches.sortedWith(compareByDescending<Opportunity> { it.category in interests }.thenByDescending { it.createdAtSeconds })
    else matches.sortedByDescending { it.createdAtSeconds }
}
