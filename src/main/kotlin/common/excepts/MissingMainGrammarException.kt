package common.excepts

class MissingMainGrammarException(projectName: String) : Exception(
    "Main grammar from" +
        "project $projectName seems to be missing"
)