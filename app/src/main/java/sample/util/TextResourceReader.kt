package sample.util

import android.content.Context
import android.content.res.Resources
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

fun readTextFileFromResource(context: Context, resourceId: Int): String {
    try {
        val bufferedReader = BufferedReader(
            InputStreamReader(
                context.resources.openRawResource(resourceId)
            )
        )

        val text = bufferedReader.readLines()

        return text.joinToString("\n")
    } catch (e: IOException) {
        throw RuntimeException("Cannot open resource ${resourceId}, $e")
    } catch (nfe: Resources.NotFoundException) {
        throw RuntimeException("Resource not found: ${resourceId}, $nfe")
    }
}
