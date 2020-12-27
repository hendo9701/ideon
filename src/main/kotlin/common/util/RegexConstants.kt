package common.util

val JAVA_VALID_ID_REGEX by lazy { Regex("[A-Za-z][A-Za-z0-9_]*") }
val JAVA_VALID_PACKAGE_NAME_REGEX by lazy { Regex("""(?:^\w+|\w+\.\w+)+$""") }
val WINDOWS_VALID_FOLDER_NAME_REGEX by lazy { Regex("""\A(?!(?:COM[0-9]|CON|LPT[0-9]|NUL|PRN|AUX|com[0-9]|con|lpt[0-9]|nul|prn|aux)|[\s.])[^\\/:*"?<>|]{1,254}\z""") }
val WINDOWS_VALID_FILE_PATH_REGEX by lazy { Regex("""^[a-zA-Z]:\\(((?![<>:"/\\|?*]).)+((?<![ .])\\)?)*$""") }
