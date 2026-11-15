package pe.tuempuje.app.profile

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import pe.tuempuje.app.auth.AccountUiState

/** Las vistas previas y el PDF utilizan los mismos dibujos y datos. */
object ReferenceResumeTemplates {
    val styles = listOf(ResumeStyle.CENTRADO, ResumeStyle.ORGANICO, ResumeStyle.EDITORIAL,
        ResumeStyle.MONOCROMO, ResumeStyle.GRAFITO, ResumeStyle.TURQUESA)

    fun label(style: ResumeStyle) = when (style) {
        ResumeStyle.CENTRADO -> "Clásico centrado"
        ResumeStyle.ORGANICO -> "Morado orgánico"
        ResumeStyle.EDITORIAL -> "Editorial arena"
        ResumeStyle.MONOCROMO -> "Elegante en negro"
        ResumeStyle.GRAFITO -> "Grafito"
        ResumeStyle.TURQUESA -> "Turquesa"
        else -> "Profesional"
    }

    private data class Line(val text: String, val size: Float, val bold: Boolean, val color: Int, val gap: Float = 0f, val rule: Boolean = false)
    private val ink = Color.rgb(35, 35, 37)
    private val white = Color.WHITE
    private fun color(hex: String) = Color.parseColor(hex)
    private fun accent(style: ResumeStyle) = when (style) {
        ResumeStyle.ORGANICO -> color("#663365")
        ResumeStyle.TURQUESA -> color("#258BA2")
        else -> ink
    }

    fun photo(resolver: ContentResolver, uri: Uri?): Bitmap? {
        if (uri == null) return null
        return runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
            var sample = 1
            while (bounds.outWidth / sample > 800 || bounds.outHeight / sample > 800) sample *= 2
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample }) }
        }.getOrNull()
    }

    private fun lines(profile: AccountUiState, style: ResumeStyle, summary: String, languages: String, references: String): Pair<List<Line>, List<Line>> {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val left = mutableListOf<Line>()
        val right = mutableListOf<Line>()
        val single = style == ResumeStyle.CENTRADO
        val leftWidth = if (single) 515f else 164f
        val rightWidth = 330f
        val leftText = if (style == ResumeStyle.MONOCROMO) white else ink
        fun paragraph(out: MutableList<Line>, value: String, width: Float, size: Float, bold: Boolean, textColor: Int, gap: Float = 0f, rule: Boolean = false) {
            if (value.isBlank()) return
            p.textSize = size
            p.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            var first = true
            value.split('\n').forEach { part ->
                var remaining = part.trim()
                if (remaining.isEmpty()) { out.add(Line("", size, bold, textColor)); return@forEach }
                while (remaining.isNotEmpty()) {
                    var count = p.breakText(remaining, true, width, null).coerceAtLeast(1)
                    if (count < remaining.length) {
                        val space = remaining.lastIndexOf(' ', count - 1)
                        if (space > 0) count = space
                        if (Character.isHighSurrogate(remaining[count - 1])) count--
                        if (count == 0) count = Character.charCount(remaining.codePointAt(0))
                    }
                    out.add(Line(remaining.substring(0, count), size, bold, textColor, if (first) gap else 0f, first && rule))
                    first = false
                    remaining = remaining.substring(count).trimStart()
                }
            }
        }
        fun section(out: MutableList<Line>, title: String, value: String, width: Float, textColor: Int) {
            if (value.isBlank()) return
            val heading = if (style == ResumeStyle.MONOCROMO && out === left) white else if (single) ink else accent(style)
            paragraph(out, title.uppercase(), width, 10f, true, heading, 12f, true)
            paragraph(out, value, width, 9.5f, false, textColor, 5f)
        }
        val contact = listOf(profile.phone, profile.email, profile.district, profile.portfolio).filter { it.isNotBlank() }.joinToString("\n")
        if (single) {
            section(left, "Datos de contacto", contact, leftWidth, ink)
            section(left, "Perfil profesional", summary, leftWidth, ink)
            section(left, "Experiencia laboral y proyectos", profile.experience, leftWidth, ink)
            section(left, "Formación académica", profile.education, leftWidth, ink)
            section(left, "Habilidades", profile.skills, leftWidth, ink)
            section(left, "Idiomas", languages, leftWidth, ink)
            section(left, "Referencias", references, leftWidth, ink)
        } else {
            if (style == ResumeStyle.MONOCROMO || style == ResumeStyle.TURQUESA) section(left, "Perfil", summary, leftWidth, leftText)
            section(left, "Contacto", contact, leftWidth, leftText)
            if (style == ResumeStyle.EDITORIAL || style == ResumeStyle.GRAFITO) section(left, "Educación", profile.education, leftWidth, leftText)
            section(left, "Idiomas", languages, leftWidth, leftText)
            section(left, "Habilidades", profile.skills, leftWidth, leftText)
            if (style != ResumeStyle.MONOCROMO && style != ResumeStyle.TURQUESA) section(right, "Sobre mí", summary, rightWidth, ink)
            if (style != ResumeStyle.EDITORIAL && style != ResumeStyle.GRAFITO) section(right, "Formación académica", profile.education, rightWidth, ink)
            section(right, "Experiencia y proyectos", profile.experience, rightWidth, ink)
            section(right, "Referencias", references, rightWidth, ink)
        }
        return left to right
    }

    private fun background(c: Canvas, style: ResumeStyle, first: Boolean, photo: Bitmap?, name: String, target: String) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        fun rect(color: Int, x: Float, y: Float, w: Float, h: Float) { p.color = color; c.drawRect(x, y, x + w, y + h, p) }
        rect(white, 0f, 0f, 595f, 842f)
        when (style) {
            ResumeStyle.ORGANICO -> {
                rect(color("#E0CEE0"), 0f, 0f, 205f, 842f)
                p.color = color("#663365"); val shape = Path().apply { moveTo(0f,0f); lineTo(135f,0f); cubicTo(90f,80f,235f,75f,205f,200f); cubicTo(185f,280f,85f,270f,0f,320f); close() }; c.drawPath(shape,p)
                p.color = color("#E0CEE0"); c.drawOval(RectF(180f,-100f,640f,70f),p)
            }
            ResumeStyle.EDITORIAL -> rect(color("#DED0BE"), 0f, 80f, 205f, 762f)
            ResumeStyle.MONOCROMO -> { rect(Color.BLACK, 0f, 0f, 205f, 842f); if (first) rect(color("#D9D5D5"), 205f, 38f, 390f, 185f) }
            ResumeStyle.GRAFITO -> { rect(color("#EDE8E4"), 0f, 120f, 205f, 722f); if (first) { rect(color("#303035"), 0f, 0f, 595f, 120f); val triangle=Path().apply { moveTo(0f,120f); lineTo(105f,150f); lineTo(205f,120f); close() }; p.color=color("#303035"); c.drawPath(triangle,p) } }
            ResumeStyle.TURQUESA -> { if (first) rect(color("#258BA2"), 0f, 0f, 595f, 100f); rect(color("#D3E8EC"), 200f, 115f, 2f, 710f) }
            else -> Unit
        }
        if (!first) return
        val cx = if (style == ResumeStyle.CENTRADO) 297f else if (style == ResumeStyle.GRAFITO) 90f else 102f
        val cy = when (style) { ResumeStyle.CENTRADO -> 83f; ResumeStyle.GRAFITO, ResumeStyle.TURQUESA -> 58f; else -> 89f }
        val radius = when (style) { ResumeStyle.GRAFITO -> 40f; ResumeStyle.TURQUESA -> 43f; else -> 68f }
        p.color = if (style == ResumeStyle.MONOCROMO) color("#D9D5D5") else color("#E9E9E9")
        val clip = Path()
        if (style == ResumeStyle.TURQUESA) { clip.moveTo(cx,cy-radius); clip.lineTo(cx+radius,cy); clip.lineTo(cx,cy+radius); clip.lineTo(cx-radius,cy); clip.close() }
        else clip.addCircle(cx,cy,radius,Path.Direction.CW)
        c.save(); c.clipPath(clip); c.drawColor(p.color)
        if (photo != null) {
            val size = minOf(photo.width,photo.height); val sx=(photo.width-size)/2; val sy=(photo.height-size)/2
            c.drawBitmap(photo, android.graphics.Rect(sx,sy,sx+size,sy+size), RectF(cx-radius,cy-radius,cx+radius,cy+radius), p)
        } else {
            p.color=color("#B5B5B5"); c.drawCircle(cx,cy-radius*.2f,radius*.24f,p); c.drawOval(RectF(cx-radius*.5f,cy+radius*.1f,cx+radius*.5f,cy+radius*.9f),p)
        }
        c.restore()
        val x = if (style == ResumeStyle.CENTRADO) 297f else if (style == ResumeStyle.GRAFITO || style == ResumeStyle.TURQUESA) 220f else 235f
        val y = when(style) { ResumeStyle.CENTRADO -> 177f; ResumeStyle.GRAFITO, ResumeStyle.TURQUESA -> 52f; ResumeStyle.MONOCROMO -> 105f; else -> 72f }
        p.color = if (style == ResumeStyle.GRAFITO || style == ResumeStyle.TURQUESA) white else accent(style)
        p.typeface = if (style == ResumeStyle.MONOCROMO) Typeface.create("serif",Typeface.NORMAL) else Typeface.DEFAULT_BOLD
        p.textSize=26f; val maxWidth=if (style == ResumeStyle.CENTRADO) 540f else 350f
        while (p.measureText(name) > maxWidth && p.textSize > 9f) p.textSize -= 1f
        val drawX=if(style==ResumeStyle.CENTRADO) x-p.measureText(name)/2 else x
        c.drawText(name,drawX,y,p)
        p.typeface=Typeface.DEFAULT; p.textSize=10f
        while (p.measureText(target)>maxWidth && p.textSize>7f) p.textSize-=.5f
        c.drawText(target,if(style==ResumeStyle.CENTRADO) x-p.measureText(target)/2 else x,y+21f,p)
    }

    private fun renderPage(canvas: Canvas, style: ResumeStyle, profile: AccountUiState, target: String, photo: Bitmap?, all: Pair<List<Line>, List<Line>>, first: Boolean, starts: Pair<Int,Int>): Pair<Int,Int> {
        background(canvas,style,first,photo,profile.displayName.ifBlank { "Tu nombre" },target.ifBlank { "Perfil laboral" })
        val p=Paint(Paint.ANTI_ALIAS_FLAG)
        fun draw(lines: List<Line>, start: Int, x: Float, initialY: Float, width: Float): Int {
            var index=start; var y=initialY
            while(index<lines.size) {
                val line=lines[index]; val height=line.size*1.5f
                if(y+line.gap+height>815f) break
                y+=line.gap; p.color=line.color; p.textSize=line.size; p.typeface=if(line.bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                canvas.drawText(line.text,x,y,p)
                if(line.rule && style != ResumeStyle.ORGANICO) { val end=x+width; val begin=(x+p.measureText(line.text)+8f).coerceAtMost(end); p.strokeWidth=.6f; canvas.drawLine(begin,y-3f,end,y-3f,p) }
                y+=height; index++
            }
            return index
        }
        val single=style==ResumeStyle.CENTRADO
        val leftY=if(!first) 45f else when(style) { ResumeStyle.CENTRADO -> 225f; ResumeStyle.GRAFITO -> 170f; ResumeStyle.TURQUESA -> 130f; else -> 180f }
        val rightY=if(!first) 45f else when(style) { ResumeStyle.MONOCROMO -> 245f; ResumeStyle.GRAFITO, ResumeStyle.TURQUESA -> 142f; else -> 145f }
        return draw(all.first,starts.first,if(single) 40f else 22f,leftY,if(single) 515f else 164f) to
            draw(all.second,starts.second,235f,rightY,330f)
    }

    fun preview(profile: AccountUiState, style: ResumeStyle, target: String, summary: String, photo: Bitmap? = null, languages: String = "", references: String = ""): Bitmap {
        val bitmap=Bitmap.createBitmap(595,842,Bitmap.Config.ARGB_8888)
        renderPage(Canvas(bitmap),style,profile,target,photo,lines(profile,style,summary,languages,references),true,0 to 0)
        return bitmap
    }

    fun write(resolver: ContentResolver, destination: Uri, profile: AccountUiState, style: ResumeStyle, target: String, summary: String, photoUri: Uri?, languages: String, references: String) {
        val doc=PdfDocument(); val photo=photo(resolver,photoUri); val all=lines(profile,style,summary,languages,references)
        try {
            var positions=0 to 0; var pageNumber=0
            do {
                val page=doc.startPage(PdfDocument.PageInfo.Builder(595,842,++pageNumber).create())
                positions=renderPage(page.canvas,style,profile,target,photo,all,pageNumber==1,positions)
                doc.finishPage(page)
            } while(positions.first<all.first.size || positions.second<all.second.size)
            (resolver.openOutputStream(destination,"w") ?: error("No se pudo abrir el archivo")).use { doc.writeTo(it) }
        } finally { doc.close(); photo?.recycle() }
    }
}
