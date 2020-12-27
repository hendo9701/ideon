package model

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class SimplifiedProjectModel : ViewModel() {
  val name = bind { SimpleStringProperty() }
  val location = bind { SimpleStringProperty() }
  val cancelled = bind { SimpleBooleanProperty(false) }
}