package model

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class ProjectTest {

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
        assert(File(NAME).deleteRecursively())
    }

    @Test
    fun projectCreatesWithFullStructure() {
        val location = File(NAME)
        val project = Project.newProject(NAME, location)

        with(project) {
            assert(src.exists())
            assert(root.exists())
            assert(out.exists())
            assert(cfg.exists())
        }

    }

    companion object {
        const val NAME = "TestProject"
    }
}