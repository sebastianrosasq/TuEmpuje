package pe.tuempuje.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class OpportunitySearchTest {
    private fun job(id: String, category: String = "Diseño", location: String = "Arequipa", mode: String? = null,
        noExperience: Boolean? = null, created: Long = 900_000, selected: Int = 0, status: String = "published",
        business: String = "") = Opportunity(
        id = id, ownerId = "owner", title = "Apoyo", organization = "María", category = category,
        location = location, pay = "S/ 50", duration = "Un día", description = "Apoyo en el proyecto",
        requirements = "Puntualidad", vacancies = 1, selectedCount = selected, own = false,
        status = status, createdAtSeconds = created, noExperienceRequired = noExperience, workMode = mode,
        publicBusinessName = business,
    )

    @Test fun `sin experiencia no incluye ofertas sin clasificar`() {
        val items = listOf(job("apta", noExperience = true), job("desconocida"), job("requiere", noExperience = false))
        assertEquals(listOf("apta"), searchOpportunities(items, OpportunityFilters(noExperienceOnly = true), emptySet(), 1_000_000).map { it.id })
    }

    @Test fun `filtrar remoto respeta la modalidad y admite avisos antiguos`() {
        val items = listOf(job("nuevo", mode = "Remoto"), job("antiguo", location = "Trabajo remoto"),
            job("hibrido", mode = "Híbrido", location = "Remoto y presencial"), job("otro"))
        assertEquals(listOf("nuevo", "antiguo"), searchOpportunities(items, OpportunityFilters(mode = "Remoto"), emptySet(), 1_000_000).map { it.id })
    }

    @Test fun `consulta negocio fecha y vacantes sin mostrar ofertas retiradas`() {
        val items = listOf(job("coincide", business = "Taller Central", created = 999_000),
            job("antiguo", business = "Taller Central", created = 700_000),
            job("lleno", business = "Taller Central", created = 999_000, selected = 1),
            job("retirado", business = "Taller Central", created = 999_000, status = "removed"))
        val filters = OpportunityFilters(query = "taller", availableOnly = true, recentDays = 1)
        assertEquals(listOf("coincide"), searchOpportunities(items, filters, emptySet(), 1_000_000).map { it.id })
    }

    @Test fun `orden por intereses y orden reciente tienen resultados distintos`() {
        val items = listOf(job("reciente", category = "Eventos", created = 990_000), job("interes", category = "Diseño", created = 900_000))
        assertEquals(listOf("interes", "reciente"), searchOpportunities(items, OpportunityFilters(), setOf("Diseño"), 1_000_000).map { it.id })
        assertEquals(listOf("reciente", "interes"), searchOpportunities(items, OpportunityFilters(prioritizeInterests = false), setOf("Diseño"), 1_000_000).map { it.id })
    }
}
