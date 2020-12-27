package org.antlr.ide.common.util

import common.util.readFromJson
import common.util.writeToJson
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert
import org.junit.Test
import java.io.File

/**
 * @author Hayder
 */

class JsonUtilsTest {


    @Test
    fun `successful reading is made from JSON file`() {
        val person = this.javaClass.getResource("/samples/person.json").file
        val expected = mapOf<String, Any>("name" to "Peter", "age" to 23, "single" to true)
        Assert.assertThat(readFromJson(person).get(), `is`(expected))
    }

    @Test
    fun `successful writing is made to JSON file`() {
//    val root = File(this.javaClass.getResource("/samples/").file)
        val tmp = File("tmp.json")
//    System.err.println("Root is $root")
        System.err.println("Tmp is ${tmp.absolutePath}")
        val sample = mapOf<String, Any>("word" to "Food", "cooked" to false, "grams" to 345)
        assert(writeToJson(tmp.absolutePath, sample))
        tmp.delete()
    }
}
