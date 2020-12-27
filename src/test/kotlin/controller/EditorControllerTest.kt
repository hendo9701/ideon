package controller

import common.antlr.AntlrOption
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert
import org.junit.Test

class EditorControllerTest {

    @Test
    fun `successful retrieval of options`() {
        val myMap = mapOf<String, Any>(
                AntlrOption.IMPORTED_GRAMMARS_LOCATION.rep to "some location",
                AntlrOption.VISITOR.rep to false
        )

        val actual = EditorController.filterArguments(myMap)
        val encodeExpectation = arrayOf(
                "-lib some location",
                "-no-visitor"
        )

        Assert.assertThat(actual, `is`(encodeExpectation))
    }

    @Test
    fun `empty map is retrieved when no options are provided`() {
        val myMap = mapOf<String, Any>("x" to false, "y" to "Hello")

        val actual = EditorController.filterArguments(myMap)
        val encodeExpectation = emptyArray<String>()

        Assert.assertThat(actual, `is`(encodeExpectation))

    }

    @Test(expected = java.lang.IllegalArgumentException::class)
    fun `exception thrown when expected value differs in type`() {
        val myMap = mapOf<String, Any>(AntlrOption.VISITOR.rep to "Hello")
        EditorController.filterArguments(myMap)

    }

}