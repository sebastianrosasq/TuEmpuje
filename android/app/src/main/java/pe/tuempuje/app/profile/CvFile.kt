package pe.tuempuje.app.profile

import android.content.ContentResolver
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.InputStream

const val MAX_CV_BYTES = 700_000

fun readCvPdf(resolver: ContentResolver, uri: Uri): ByteArray {
    val input = resolver.openInputStream(uri) ?: throw IllegalArgumentException("No se pudo abrir el archivo")
    return input.use(::readCvPdfStream)
}

internal fun readCvPdfStream(stream: InputStream): ByteArray {
    val bytes = run {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            val count = stream.read(buffer)
            if (count < 0) break
            if (output.size() + count > MAX_CV_BYTES) throw IllegalArgumentException("El PDF supera el límite de 700 kB")
            output.write(buffer, 0, count)
        }
        output.toByteArray()
    }
    require(bytes.size >= 5 && bytes.copyOfRange(0, 5).contentEquals("%PDF-".toByteArray())) { "Selecciona un archivo PDF válido" }
    return bytes
}
