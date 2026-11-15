package pe.tuempuje.app.profile

import android.content.ContentResolver
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import pe.tuempuje.app.auth.AccountUiState

enum class ResumeStyle {
    PROFESSIONAL, MODERN, FIRST_EXPERIENCE,
    CENTRADO, ORGANICO, EDITORIAL, MONOCROMO, GRAFITO, TURQUESA,
}

object ResumePdf {
    fun write(resolver: ContentResolver, destination: Uri, profile: AccountUiState,
              style: ResumeStyle = ResumeStyle.PROFESSIONAL, target: String = "", summary: String = "",
              photo: Uri? = null, languages: String = "", references: String = "") {
        if (style in ReferenceResumeTemplates.styles) {
            ReferenceResumeTemplates.write(resolver, destination, profile, style, target, summary, photo, languages, references)
            return
        }
        if (style == ResumeStyle.MODERN) {
            writeModern(resolver, destination, profile, target, summary)
            return
        }
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
                paint.color = when (style) {
                    ResumeStyle.PROFESSIONAL -> Color.rgb(28, 42, 56)
                    ResumeStyle.MODERN -> Color.rgb(24, 127, 118)
                    else -> Color.rgb(61, 85, 133)
                }
                paragraph(title, 12f, true)
                paint.color = Color.rgb(28, 42, 56)
                paragraph(value)
            }

            if (style != ResumeStyle.PROFESSIONAL) {
                paint.color = if (style == ResumeStyle.MODERN) Color.rgb(223, 243, 239) else Color.rgb(229, 235, 247)
                page.canvas.drawRoundRect(30f, 25f, 565f, 95f, 12f, 12f, paint)
                paint.color = Color.rgb(28, 42, 56)
            }
            paragraph(profile.displayName.ifBlank { "Currículum" }, 22f, true)
            paragraph(target.ifBlank { "Perfil laboral" }, 11f)
            val contact = listOf(profile.email, profile.phone, profile.district).filter { it.isNotBlank() }.joinToString("  |  ")
            section("Contacto", contact)
            section("Perfil", summary)
            section("Habilidades", profile.skills)
            section(if (style == ResumeStyle.FIRST_EXPERIENCE) "Proyectos y experiencia" else "Experiencia y proyectos", profile.experience)
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

    private data class Line(val text: String, val size: Float, val bold: Boolean, val color: Int, val gap: Float = 0f)

    private fun writeModern(resolver: ContentResolver, destination: Uri, profile: AccountUiState, target: String, summary: String) {
        val ink = Color.rgb(28, 42, 56)
        val accent = Color.rgb(20, 105, 98)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val left = mutableListOf<Line>()
        val right = mutableListOf<Line>()

        fun paragraph(out: MutableList<Line>, text: String, width: Float, size: Float, bold: Boolean, color: Int, gap: Float = 0f) {
            if (text.isBlank()) return
            paint.textSize = size
            paint.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            var first = true
            text.split('\n').forEach { part ->
                var rest = part.trim()
                if (rest.isEmpty()) { out.add(Line("", size, bold, color)); return@forEach }
                while (rest.isNotEmpty()) {
                    var count = paint.breakText(rest, true, width, null).coerceAtLeast(1)
                    if (count < rest.length) {
                        val space = rest.lastIndexOf(' ', count - 1)
                        if (space > 0) count = space
                        if (Character.isHighSurrogate(rest[count - 1])) count--
                        if (count == 0) count = Character.charCount(rest.codePointAt(0))
                    }
                    out.add(Line(rest.substring(0, count), size, bold, color, if (first) gap else 0f))
                    first = false
                    rest = rest.substring(count).trimStart()
                }
            }
        }

        fun section(out: MutableList<Line>, title: String, text: String, width: Float) {
            if (text.isBlank()) return
            paragraph(out, title.uppercase(), width, 10f, true, accent, 12f)
            paragraph(out, text, width, 10.5f, false, ink, 3f)
        }

        section(left, "Contacto", listOf(profile.email, profile.phone, profile.district).filter { it.isNotBlank() }.joinToString("\n"), 134f)
        section(left, "Habilidades", profile.skills, 134f)
        section(left, "Formación", profile.education, 134f)
        section(left, "Disponibilidad", profile.availability, 134f)
        section(left, "Portafolio", profile.portfolio, 134f)
        paragraph(right, profile.displayName.ifBlank { "Currículum" }, 330f, 22f, true, ink)
        paragraph(right, target.ifBlank { "Perfil laboral" }, 330f, 11f, false, accent, 4f)
        section(right, "Perfil", summary, 330f)
        section(right, "Experiencia y proyectos", profile.experience, 330f)

        val document = PdfDocument()
        try {
            var leftIndex = 0
            var rightIndex = 0
            var number = 0
            do {
                val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, ++number).create())
                paint.color = Color.rgb(222, 241, 238)
                page.canvas.drawRoundRect(25f, 25f, 195f, 817f, 10f, 10f, paint)
                fun draw(lines: List<Line>, start: Int, x: Float): Int {
                    var index = start
                    var y = 56f
                    while (index < lines.size) {
                        val line = lines[index]
                        val height = line.size * 1.55f
                        if (y + line.gap + height > 800f) break
                        y += line.gap
                        paint.color = line.color
                        paint.textSize = line.size
                        paint.typeface = if (line.bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                        page.canvas.drawText(line.text, x, y, paint)
                        y += height
                        index++
                    }
                    return index
                }
                leftIndex = draw(left, leftIndex, 42f)
                rightIndex = draw(right, rightIndex, 220f)
                document.finishPage(page)
            } while (leftIndex < left.size || rightIndex < right.size)
            val stream = resolver.openOutputStream(destination, "w") ?: error("No se pudo abrir el archivo")
            stream.use { document.writeTo(it) }
        } finally { document.close() }
    }
}
