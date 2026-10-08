package com.example.hatakenote.core.domain.model

import kotlinx.datetime.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlantingPhotoTest {

    private val harvested = Planting(
        id = 1,
        cropId = 10,
        plantedDate = LocalDate(2026, 4, 1),
        harvestedDate = LocalDate(2026, 7, 31),
        isActive = false,
    )
    private val active = Planting(
        id = 2,
        cropId = 20,
        plantedDate = LocalDate(2026, 8, 15),
    )

    private fun photo(plantingId: Long?, plotId: Long?, takenDate: LocalDate) = PlantingPhoto(
        id = 100,
        plantingId = plantingId,
        plotId = plotId,
        filePath = "https://example.com/a.jpg",
        takenDate = takenDate,
    )

    @Test
    fun `plantingIdが一致する写真は期間外でも対象`() {
        assertTrue(photo(1, null, LocalDate(2025, 1, 1)).belongsTo(harvested))
    }

    @Test
    fun `別の作付けに紐づく写真は区画が同じでも対象外`() {
        assertFalse(photo(1, 5, LocalDate(2026, 9, 1)).belongsTo(active))
    }

    @Test
    fun `区画のみの写真は収穫済み作付けの期間内なら対象`() {
        val p = photo(null, 5, LocalDate(2026, 6, 1))
        assertTrue(p.belongsTo(harvested))
        assertFalse(p.belongsTo(active))
    }

    @Test
    fun `区画のみの写真は植え付け日と収穫日を含む`() {
        assertTrue(photo(null, 5, LocalDate(2026, 4, 1)).belongsTo(harvested))
        assertTrue(photo(null, 5, LocalDate(2026, 7, 31)).belongsTo(harvested))
        assertFalse(photo(null, 5, LocalDate(2026, 8, 1)).belongsTo(harvested))
    }

    @Test
    fun `区画のみの写真は栽培中なら植え付け日以降が対象`() {
        assertTrue(photo(null, 5, LocalDate(2026, 10, 1)).belongsTo(active))
        assertFalse(photo(null, 5, LocalDate(2026, 8, 14)).belongsTo(active))
    }

    @Test
    fun `継続収穫中の作付けは最新の収穫日より後の区画写真も対象`() {
        val continuing = active.copy(harvestedDate = LocalDate(2026, 9, 1))
        assertTrue(photo(null, 5, LocalDate(2026, 9, 20)).belongsTo(continuing))
    }
}
