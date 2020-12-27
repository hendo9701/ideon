package common.util

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * @author Hayder
 */

class FileUtilsTest {

    private val loremText = """
    Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod
    tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam,
    quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo
    consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse
    cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non
    proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
    """.trimIndent()

    private val loremFile = File(this.javaClass.getResource("/samples/lorem.txt").file)

    @Before
    fun setUp() {
        assert(loremFile.exists())
    }

    @Test
    fun createsFileForWritingWhenNotExists() {
        val tmpLoremFile = File("_lorem.txt")
        if (setContent(content = loremText, destination = tmpLoremFile))
            tmpLoremFile.delete()
        else
            fail()
    }


    fun `gets empty optional when file does not exists`() {
        Assert.assertFalse(getContent(File("fake-file")).isPresent)
    }

    @Test
    fun successfulRead() {
        val content = getContent(loremFile).get()
        assertThat(content, `is`(loremText))
    }

    @Test
    fun successfulWrite() {
        val tmpLoremFile = File("tmp-lorem.txt")

        assert(setContent(loremText, tmpLoremFile))
        val written = getContent(tmpLoremFile).get()

        assertThat(written, `is`(loremText))

        tmpLoremFile.delete()
    }

}