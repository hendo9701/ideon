package model

import common.util.logger
import common.util.readFromJson
import common.util.writeToJson
import model.ProjectDefaults.*
import java.io.File

/**
 * @author Hayder
 */

class Project private constructor(
    val name: String,
    val root: File,
    val src: File,
    val out: File,
    val cfg: File,
    val gen: File,
    mainGrammar: File? = null
) {

  var mainGrammar = mainGrammar
    set(value) {
      writeToJson(cfg.absolutePath, mapOf(MAIN_GRAMMAR.toString() to value!!.absolutePath))
      field = value
    }

  companion object {

    private val LOG by logger { }

    fun newProject(name: String, location: File): Project {
      if (!location.exists()) {
        LOG.info("File: ${location.absolutePath} was missing. Creating new file instead...")
        location.mkdir()
      }

      val root = File(location.absolutePath, name)
      root.mkdir()
      val src = File(root.absolutePath, SRC.toString())
      src.mkdir()
      val out = File(root.absolutePath, OUT.toString())
      out.mkdir()
      val cfg = File(root.absolutePath, CFG.toString())
      val gen = File(root.absolutePath, GEN.toString())
      gen.mkdir()

      val meta = mapOf(
          NAME.toString() to name,
          ROOT.toString() to root.absolutePath,
          SRC.toString() to src.absolutePath,
          OUT.toString() to out.absolutePath,
          GEN.toString() to gen.absolutePath
      )

      val success = writeToJson(cfg.absolutePath, meta)
      if (!success) LOG.warn("Failed at writing project metadata")
      return Project(name, root, src, out, cfg, gen)
    }

    fun loadProject(location: String): Project {
      val cfgPath = "$location${File.separator}$CFG"
      LOG.debug("Main grammar value is: " + readFromJson(cfgPath).get()[MAIN_GRAMMAR.toString()])
      return readFromJson(cfgPath).map {
        Project(
            name = it[NAME.toString()] as String,
            root = File(location),
            src = File(it[SRC.toString()] as String),
            out = File(it[OUT.toString()] as String),
            gen = File(it[GEN.toString()] as String),
            cfg = File(cfgPath),
            mainGrammar = if (it[MAIN_GRAMMAR.toString()] == null) null else File(it[MAIN_GRAMMAR.toString()] as String)
        )
      }.get()
    }
  }

}