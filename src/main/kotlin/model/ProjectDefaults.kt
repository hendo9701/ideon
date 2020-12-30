package model

enum class ProjectDefaults(private val pName: String) {
  NAME("name"),
  ROOT("root"),
  SRC("src"),
  OUT("out"),
  CFG("config.json"),
  GEN("gen"),
  MAIN_GRAMMAR("main_grammar");

  override fun toString(): String {
    return pName
  }

}