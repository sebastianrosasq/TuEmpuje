package pe.tuempuje.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import pe.tuempuje.app.MainActivity
import pe.tuempuje.app.R
import pe.tuempuje.app.data.FirebaseEnvironment
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    private const val TAG = "tuempuje-activity-checks"
    fun update(context: Context, uid: String?, enabled: Boolean) {
        val manager = WorkManager.getInstance(context)
        if (!enabled || uid == null) {
            manager.cancelUniqueWork(TAG)
            NotificationManagerCompat.from(context).cancel(1001)
            if (!enabled && uid != null) context.getSharedPreferences("avisos_estado", Context.MODE_PRIVATE).edit().remove(uid).apply()
            return
        }
        val request = PeriodicWorkRequestBuilder<ActivityNotificationWorker>(1, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setInputData(workDataOf("uid" to uid))
            .addTag(TAG)
            .build()
        manager.enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.UPDATE, request)
    }
}

class ActivityNotificationWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        val uid = inputData.getString("uid") ?: return Result.failure()
        val enabled = applicationContext.getSharedPreferences("apariencia", Context.MODE_PRIVATE).getBoolean("avisos_periodicos", false)
        if (!enabled) return Result.success()
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return Result.success()
        return try {
            val services = FirebaseEnvironment.create(applicationContext) ?: return Result.retry()
            if (services.auth.currentUser?.uid != uid) return Result.success()
            val firestore = services.firestore
            val state = applicationContext.getSharedPreferences("avisos_estado", Context.MODE_PRIVATE)
            val stored = state.getString(uid, null)
            val previous = if (stored == null) JSONObject() else JSONObject(stored)
            val current = JSONObject()
            var changed = false

            suspend fun inspect(ref: DocumentReference, status: String, employer: Boolean) {
                val last = ref.collection("messages").orderBy("createdAt", Query.Direction.DESCENDING).limit(1).get(Source.SERVER).await().documents.firstOrNull()
                val value = JSONObject().put("status", status).put("message", last?.id.orEmpty())
                val key = ref.path
                val old = previous.optJSONObject(key)
                if (hasRelevantActivity(stored != null, old?.optString("status"), old?.optString("message"), status, last?.id, last?.getString("senderId"), uid, employer)) changed = true
                current.put(key, value)
            }

            firestore.collectionGroup("applications").whereEqualTo("applicantId", uid).get(Source.SERVER).await().documents.forEach { doc ->
                inspect(doc.reference, doc.getString("status").orEmpty(), false)
            }
            firestore.collection("opportunities").whereEqualTo("ownerId", uid).get(Source.SERVER).await().documents.forEach { job ->
                job.reference.collection("applications").get(Source.SERVER).await().documents.forEach { doc ->
                    inspect(doc.reference, doc.getString("status").orEmpty(), true)
                }
            }
            if (services.auth.currentUser?.uid != uid || !applicationContext.getSharedPreferences("apariencia", Context.MODE_PRIVATE).getBoolean("avisos_periodicos", false)) return Result.success()
            state.edit().putString(uid, current.toString()).apply()
            if (changed) showNotification()
            Result.success()
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun showNotification() {
        val channel = "actividad"
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(channel, "Actividad de TuEmpuje", NotificationManager.IMPORTANCE_DEFAULT))
        val intent = PendingIntent.getActivity(applicationContext, 0, Intent(applicationContext, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(applicationContext, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Tienes novedades en TuEmpuje")
            .setContentText("Revisa tus conversaciones y postulaciones")
            .setContentIntent(intent)
            .setAutoCancel(true)
            .build()
        if (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(applicationContext).notify(1001, notification)
        }
    }
}
