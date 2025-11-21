package notes.project.notes

class CursorMapping {

    private val mapping = mutableMapOf<Int, Int>()

    private var currentIndex = 0
    private var transformedIndex = 0

    operator fun get(index: Int): Int =
        mapping[index]!!

    fun getOriginalFromTransformed(index: Int): Int =
        mapping.entries.last { it.value == index }.key

    fun addPlainMapping() {
        mapping[currentIndex++] = transformedIndex++
    }

    fun resetMapping() {
        mapping.clear()
        currentIndex = 0
        transformedIndex = 0
    }

    fun addPlainMappings(count: Int) {
        (0..<count).forEach { _ ->
            addPlainMapping()
        }
    }

    fun skipMapping() {
        skipMappings(1)
    }

    fun skipMappings(count: Int) {
        // If not empty, the mapping will be on the correct
        // index, but too far forward with the transformedIndex
        if (mapping.isNotEmpty()) {
            transformedIndex--

            (0..<count - 1).forEach { _ ->
                mapping[currentIndex++] = transformedIndex
            }
            addPlainMapping()
        } else {
            mapping[currentIndex] = transformedIndex
            (0..<count).forEach { _ ->
                mapping[currentIndex++] = transformedIndex
            }
        }
    }

    // Used for adding mappings for text like **bold**.
    // It skips the first two asterisks, then adds plain mappings
    // for the text, then skips the last two.
    fun skipAddSkipMappings(skip: Int, add: Int) {
        skipMappings(skip)
        addPlainMappings(add)
        skipMappings(skip)
    }

    override fun toString(): String = mapping.toString()
}