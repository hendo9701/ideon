package view

import common.*
import javafx.scene.effect.DropShadow
import javafx.scene.text.FontWeight.BOLD
import tornadofx.*

class ParseTreeStyle : Stylesheet() {

    init {
        toolBar {
            backgroundColor += DEFAULT_SECONDARY_COLOR
        }

        s(button, comboBox, slider) {
            fontFamily = DEFAULT_FONT_FAMILY
            fontSize = DEFAULT_FONT_SIZE * 0.80
            fontWeight = BOLD
            borderColor += box(DEFAULT_SECONDARY_COLOR)
            borderInsets += box(0.5.px)
            borderWidth += box(2.px)
            borderRadius += box(1.5.px)

        }
        s(button, comboBox) {
            backgroundColor += DEFAULT_VIVID_COLOR
            textFill = DEFAULT_SECONDARY_COLOR
            and(hover) {
                effect = DropShadow()
            }
        }
        comboBox {
            select(cell) {
                backgroundColor += DEFAULT_VIVID_COLOR
                fontWeight = BOLD
                fontFamily = DEFAULT_FONT_FAMILY
                fontSize = DEFAULT_FONT_SIZE * 0.80
                textFill = DEFAULT_SECONDARY_COLOR
                and(selected) {
                    backgroundColor += DEFAULT_VIVID_COLOR
                }
                and(hover) {
                    backgroundColor += DEFAULT_OPAQUE_COLOR
                }
            }
        }

        slider {
            effect = DropShadow()
            backgroundColor += DEFAULT_SECONDARY_COLOR
            select(track, thumb, coloredTrack) {
                backgroundColor += DEFAULT_OPAQUE_COLOR
            }
            select(axis) {
                select(axisTickMark, axisMinorTickMark) {
                    stroke = DEFAULT_OPAQUE_COLOR
                }
            }

            select(animatedThumb) {
                backgroundColor += DEFAULT_OPAQUE_COLOR
            }
        }

        progressBar {
            select(track) {
                backgroundInsets += box(0.px)
                backgroundRadius += box(0.px)
            }
            select(bar) {
                backgroundColor += DEFAULT_VIVID_COLOR
            }
        }

        STATUS_LABEL {
            fontFamily = DEFAULT_FONT_FAMILY
            fontSize = DEFAULT_FONT_SIZE * 0.90
            textFill = DEFAULT_VIVID_COLOR
        }
    }

    companion object {
        private val coloredTrack by cssclass()
        private val animatedThumb by cssclass()
        val STATUS_LABEL by cssclass()
    }
}