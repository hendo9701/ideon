package common.util

import common.DEFAULT_VIVID_COLOR
import common.util.IconName.*
import common.util.IconName.FOLDER
import common.util.IconName.FOLDER_OPEN
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconName
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconName.*
import tornadofx.*

enum class IconName {
  FOLDER,
  FOLDER_OPEN,
  REGULAR_FILE,
  JAVA_FILE,
  ANTLR_FILE,
  CLASS_FILE,
  JSON_FILE
}

fun makeIcon(name: IconName) =
    FontAwesomeIcon().apply {
      when (name) {
        FOLDER -> setIcon(FontAwesomeIconName.FOLDER)
        FOLDER_OPEN -> setIcon(FontAwesomeIconName.FOLDER_OPEN)
        REGULAR_FILE -> setIcon(FILE_TEXT)
        JAVA_FILE -> setIcon(CUBES)
        ANTLR_FILE -> setIcon(CUBE)
        CLASS_FILE -> setIcon(COGS)
        JSON_FILE -> setIcon(THUMB_TACK)
      }
      style { fontSize = 15.px; fontFamily = "FontAwesome"; fill = DEFAULT_VIVID_COLOR }
    }


