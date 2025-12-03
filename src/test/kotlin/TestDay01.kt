import maelise.castel.Direction
import maelise.castel.Rotation
import maelise.castel.countZerosEncountered
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TestDay01 {

    @Test
    fun `should count zeros encountered when none`() {
        // given
        val rotations = listOf(Rotation(Direction.LEFT, clicks = 41))

        // when
        val zerosEncountered = countZerosEncountered(rotations)

        // then
        assertThat(zerosEncountered).isEqualTo(0)
    }

    @Test
    fun `should count zeros encountered when exactly one zero - going left`() {
        // given
        val rotations = listOf(Rotation(Direction.LEFT, clicks = 50))

        // when
        val zerosEncountered = countZerosEncountered(rotations)

        // then
        assertThat(zerosEncountered).isEqualTo(1)
    }

    @Test
    fun `should count zeros encountered when exactly one zero - going right`() {
        // given
        val rotations = listOf(Rotation(Direction.RIGHT, clicks = 50))

        // when
        val zerosEncountered = countZerosEncountered(rotations)

        // then
        assertThat(zerosEncountered).isEqualTo(1)
    }

    @Test
    fun `should count zeros encountered when multiple zeros - going left`() {
        // given
        val rotations = listOf(Rotation(Direction.LEFT, clicks = 1000))

        // when
        val zerosEncountered = countZerosEncountered(rotations)

        // then
        assertThat(zerosEncountered).isEqualTo(10)
    }

    @Test
    fun `should count zeros encountered when multiple zeros - going right`() {
        // given
        val rotations = listOf(Rotation(Direction.RIGHT, clicks = 1000))

        // when
        val zerosEncountered = countZerosEncountered(rotations)

        // then
        assertThat(zerosEncountered).isEqualTo(10)
    }

    @Test
    fun `should count zeros encountered when multiple zeros and exactly stopping on zero - going left`() {
        // given
        val rotations = listOf(Rotation(Direction.LEFT, clicks = 1050))

        // when
        val zerosEncountered = countZerosEncountered(rotations)

        // then
        assertThat(zerosEncountered).isEqualTo(11)
    }

    @Test
    fun `should count zeros encountered when multiple zeros and exactly stopping on zero - going right`() {
        // given
        val rotations = listOf(Rotation(Direction.RIGHT, clicks = 1050))

        // when
        val zerosEncountered = countZerosEncountered(rotations)

        // then
        assertThat(zerosEncountered).isEqualTo(11)
    }
}
