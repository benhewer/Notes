package notes.project.notes

import kotlin.test.Test
import kotlin.test.assertEquals

class CursorMappingTest {

    val mapping = CursorMapping()

    @Test
    fun `Add plain mapping works`() {
        mapping.addPlainMapping()

        assertEquals(0, mapping[0])
    }

    @Test
    fun `Adding many mappings works`() {
        mapping.addPlainMappings(5)

        (0..<5).forEach {
            assertEquals(it, mapping[it])
        }
    }

    @Test
    fun `Skipping mappings works`() {
        mapping.addPlainMapping()
        mapping.skipMappings(5)

        (0..5).forEach {
            assertEquals(0, mapping[it])
        }
    }

    @Test
    fun `Skipping mappings works for small skips`() {
        mapping.addPlainMapping()
        mapping.skipMappings(1)

        (0..1).forEach {
            assertEquals(0, mapping[it])
        }
    }

    @Test
    fun `Skipping mappings works when mapping is empty`() {
        mapping.skipMappings(1)
        mapping.addPlainMapping()

        assertEquals(0, mapping[0])
        assertEquals(0, mapping[1])
    }

    @Test
    fun `Skip add skip mapping works`() {
        mapping.addPlainMapping()
        mapping.skipAddSkipMappings(2, 5)

        val expected = listOf(0) + 0 + (0..5) + 5 + 5

        (0..5).forEach {
            assertEquals(expected[it], mapping[it])
        }
    }
}