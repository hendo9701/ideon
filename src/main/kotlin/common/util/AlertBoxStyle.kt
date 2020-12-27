package common.util

import javafx.scene.paint.Color
import tornadofx.*

class AlertBoxStyle : Stylesheet() {
    init {
        severe {
            backgroundColor += c("#ffcdd2")
        }
        info {
            backgroundColor += c("#dcedc8")
        }
        warn {
            backgroundColor += c("#fff9c4")
        }
        default {
            backgroundColor += c("#f5f5f5")
        }
        header {
            fontSize = 14.px
        }
        action {
            fontSize = 16.px
            fontFamily = "FontAwesome"
        }
        field {
            fontSize = 12.px
        }
        right {
            jfxFocusColor.value = c("#dcedc8")
            jfxUnfocusColor.value = c("#dcedc8")
        }
        wrong {
            jfxFocusColor.value = c("#ffcdd2")
            jfxUnfocusColor.value = c("#ffcdd2")
        }
    }

    companion object {
        val severe by cssclass()
        val info by cssclass()
        val warn by cssclass()
        val default by cssclass()
        val header by cssclass()
        val action by cssclass()
        val field by cssclass()
        val right by cssclass()
        val wrong by cssclass()
        private val jfxFocusColor by cssproperty<Color>("-jfx-focus-color")
        private val jfxUnfocusColor by cssproperty<Color>("-jfx-unfocus-color")

    }
}