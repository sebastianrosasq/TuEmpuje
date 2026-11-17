package pe.tuempuje.app.profile

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.ByteArrayInputStream

class CvFileTest {
    @Test fun acceptsPdfWithinLimit() {
        val data = "%PDF-1.4\ncontenido".toByteArray()
        assertArrayEquals(data, readCvPdfStream(ByteArrayInputStream(data)))
    }

    @Test fun rejectsIncorrectFormatAndOversizedFiles() {
        assertThrows(IllegalArgumentException::class.java) { readCvPdfStream(ByteArrayInputStream("not a pdf".toByteArray())) }
        val oversized = ByteArray(MAX_CV_BYTES + 1)
        "%PDF-".toByteArray().copyInto(oversized)
        assertThrows(IllegalArgumentException::class.java) { readCvPdfStream(ByteArrayInputStream(oversized)) }
    }

    @Test fun analysisAcceptsLargerPdfButEnforcesItsOwnLimit() {
        val larger = ByteArray(MAX_CV_BYTES + 1)
        "%PDF-".toByteArray().copyInto(larger)
        assertArrayEquals(larger, readBoundedPdf(ByteArrayInputStream(larger), MAX_CV_IMPORT_BYTES, "muy grande"))
        val oversized = ByteArray(MAX_CV_IMPORT_BYTES + 1)
        "%PDF-".toByteArray().copyInto(oversized)
        assertThrows(IllegalArgumentException::class.java) { readBoundedPdf(ByteArrayInputStream(oversized), MAX_CV_IMPORT_BYTES, "muy grande") }
        assertThrows(IllegalArgumentException::class.java) { readBoundedPdf(ByteArrayInputStream("incorrecto".toByteArray()), MAX_CV_IMPORT_BYTES, "muy grande") }
    }
}
