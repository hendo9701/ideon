package common.antlr

enum class AntlrOption(val rep: String) {

  IMPORTED_GRAMMARS_LOCATION("imported-grammars-location"),
  PACKAGE_NAME("package"),
  LISTENER("listener"),
  VISITOR("visitor");

  companion object {
    fun encode(option: AntlrOption, value: Any): String = when (option) {
      IMPORTED_GRAMMARS_LOCATION -> "-lib ${value as String}"
      PACKAGE_NAME -> "-package ${value as String}"
      LISTENER -> {
        if (value is Boolean) {
          if (value) "-listener" else "-no-listener"
        } else throw IllegalArgumentException()
      }
      VISITOR -> {
        if (value is Boolean) {
          if (value) "-visitor" else "-no-visitor"
        } else throw IllegalArgumentException()
      }
    }
  }

}