package pe.tuempuje.app.profile

import android.content.Context
import org.json.JSONObject

data class ResumeDraft(
    val style: String = ResumeStyle.CENTRADO.name,
    val goal: String = "",
    val summary: String = "",
    val skills: String = "",
    val experience: String = "",
    val education: String = "",
    val instruction: String = "",
    val languages: String = "",
    val references: String = "",
    val photoUri: String = "",
    val answers: String = "",
)

internal fun resumeDraftKey(uid: String, opportunityId: String?): String =
    "$uid/${if (opportunityId == null) "general" else "oferta/$opportunityId"}"

class ResumeDraftStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("cv_borradores", Context.MODE_PRIVATE)

    fun load(uid: String, opportunityId: String?): ResumeDraft? {
        val raw = preferences.getString(resumeDraftKey(uid, opportunityId), null) ?: return null
        return runCatching {
            val json = JSONObject(raw)
            val style = json.optString("style").takeIf { value -> ResumeStyle.entries.any { it.name == value } } ?: ResumeStyle.PROFESSIONAL.name
            ResumeDraft(style, json.optString("goal"), json.optString("summary"), json.optString("skills"),
                json.optString("experience"), json.optString("education"), json.optString("instruction"),
                json.optString("languages"), json.optString("references"), json.optString("photoUri"), json.optString("answers"))
        }.getOrNull()
    }

    fun save(uid: String, opportunityId: String?, draft: ResumeDraft): Boolean {
        val json = JSONObject().put("style", draft.style).put("goal", draft.goal).put("summary", draft.summary)
            .put("skills", draft.skills).put("experience", draft.experience).put("education", draft.education).put("instruction", draft.instruction)
            .put("languages", draft.languages).put("references", draft.references).put("photoUri", draft.photoUri)
            .put("answers", draft.answers)
        return preferences.edit().putString(resumeDraftKey(uid, opportunityId), json.toString()).commit()
    }

    fun clearAccount(uid: String) {
        val editor = preferences.edit()
        preferences.all.keys.filter { it.startsWith("$uid/") }.forEach(editor::remove)
        editor.apply()
    }
}
