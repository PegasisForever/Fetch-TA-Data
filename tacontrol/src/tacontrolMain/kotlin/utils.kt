import platform.posix.fdopen
import platform.posix.fflush
import platform.posix.fprintf

private val STDERR = fdopen(2, "w")
fun printlnErr(message: String) {
    fprintf(STDERR, message + "\n")
    fflush(STDERR)
}
