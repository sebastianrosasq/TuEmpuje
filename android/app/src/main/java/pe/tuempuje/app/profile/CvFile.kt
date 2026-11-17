package pe.tuempuje.app.profile

import android.content.ContentResolver
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.InputStream

const val MAX_CV_BYTES = 700_000
const val MAX_CV_IMPORT_BYTES = 4_000_000

fun readCvPdf(resolver: ContentResolver, uri: Uri): ByteArray {
    val input = resolver.openInputStream(uri) ?: throw IllegalArgumentException("No se pudo abrir el archivo")
    return input.use(::readCvPdfStream)
}

internal fun readCvPdfStream(stream: InputStream): ByteArray {
    return readBoundedPdf(stream, MAX_CV_BYTES, "El PDF supera el límite de 700 kB")
}

fun readCvForAnalysis(resolver: ContentResolver, uri: Uri): ByteArray {
    val input = resolver.openInputStream(uri) ?: throw IllegalArgumentException("No se pudo abrir el archivo")
    return input.use { readBoundedPdf(it, MAX_CV_IMPORT_BYTES, "El PDF supera el límite de 4 MB") }
}

internal fun readBoundedPdf(stream: InputStream, limit: Int, sizeError: String): ByteArray {
    val bytes = run {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            val count = stream.read(buffer)
            if (count < 0) break
            if (output.size() + count > limit) throw IllegalArgumentException(sizeError)
            output.write(buffer, 0, count)
        }
        output.toByteArray()
    }
    require(bytes.size >= 5 && bytes.copyOfRange(0, 5).contentEquals("%PDF-".toByteArray())) { "Selecciona un archivo PDF válido" }
    return bytes
}
