package common.antlr

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA
import java.util.*

class UnderlineErrorListener(val stack: StringBuilder = StringBuilder()) : ANTLRErrorListener {

  override fun reportAttemptingFullContext(p0: Parser?, p1: DFA?, p2: Int, p3: Int, p4: BitSet?, p5: ATNConfigSet?) {
  }

  override fun syntaxError(recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int, charPositionInLine: Int, msg: String?, e: RecognitionException?) {
    stack.append("line $line:$charPositionInLine $msg\n")
    underlineError(recognizer, offendingSymbol as Token, line, charPositionInLine)
  }

  private fun underlineError(recognizer: Recognizer<*, *>?, offendingToken: Token, line: Int, charPositionInLine: Int) {
    val tokens = recognizer!!.inputStream as CommonTokenStream
    val input = tokens.tokenSource.inputStream.toString()
    val lines = input.split("\n")
    val errorLine = lines[line - 1]
    stack.append(errorLine).append("\n")
    for (i in 0..charPositionInLine) stack.append(" ")
    val start = offendingToken.startIndex
    val stop = offendingToken.stopIndex
    if (start >= 0 && stop >= 0) for (i in start.until(stop)) stack.append("^")
    stack.append("\n")
  }

  override fun reportAmbiguity(p0: Parser?, p1: DFA?, p2: Int, p3: Int, p4: Boolean, p5: BitSet?, p6: ATNConfigSet?) {
  }

  override fun reportContextSensitivity(p0: Parser?, p1: DFA?, p2: Int, p3: Int, p4: Int, p5: ATNConfigSet?) {
  }
}