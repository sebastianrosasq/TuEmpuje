package pe.tuempuje.app.profile

import android.content.ContentResolver
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import pe.tuempuje.app.auth.AccountUiState

object ResumePdf {
    fun write(resolver: ContentResolver, destination: Uri, profile: AccountUiState) {
        val document = PdfDocument()
        try {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(28, 42, 56) }
            var pageNumber = 1
            var page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
            var y = 54f

            fun nextLine(text: String, size: Float, bold: Boolean) {
                paint.textSize = size
                paint.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                val lineHeight = size * 1.55f
                if (y + lineHeight > 795f) {
                    document.finishPage(page)
                    page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, ++pageNumber).create())
                    y = 54f
                }
                page.canvas.drawText(text, 46f, y, paint)
                y += lineHeight
            }

            fun paragraph(text: String, size: Float = 11f, bold: Boolean = false) {
                paint.textSize = size
                paint.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                text.split('\n').forEach { paragraph ->
                    var remaining = paragraph.trim()
                    if (remaining.isEmpty()) nextLine("", size, bold)
                    while (remaining.isNotEmpty()) {
                        var count = paint.breakText(remaining, true, 503f, null).coerceAtLeast(1)
                        if (count < remaining.length) {
                            val space = remaining.lastIndexOf(' ', count - 1)
                            if (space > 0) count = space
                            if (Character.isHighSurrogate(remaining[count - 1])) count--
                            if (count == 0) count = Character.charCount(remaining.codePointAt(0))
                        }
                        nextLine(remaining.substring(0, count), size, bold)
                        remaining = remaining.substring(count).trimStart()
                    }
                }
            }

            fun section(title: String, value: String) {
                if (value.isBlank()) return
                y += 10f
                paragraph(title, 12f, true)
                paragraph(value)
            }

            paragraph(profile.displayName.ifBlank { "Currículum" }, 22f, true)
            paragraph("Perfil laboral", 11f)
            val contact = listOf(profile.email, profile.phone, profile.district).filter { it.isNotBlank() }.joinToString("  |  ")
            section("Contacto", contact)
            section("Habilidades", profile.skills)
            section("Experiencia", profile.experience)
            section("Formación", profile.education)
            section("Disponibilidad", profile.availability)
            section("Portafolio", profile.portfolio)
            document.finishPage(page)
            val stream = resolver.openOutputStream(destination, "w") ?: error("No se pudo abrir el archivo")
            stream.use { document.writeTo(it) }
        } finally {
            document.close()
        }
    }
}
