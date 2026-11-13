const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");
const { getFirestore, FieldValue, Timestamp } = require("firebase-admin/firestore");

initializeApp();
const db = getFirestore();

async function deleteQuery(query) {
  while (true) {
    const page = await query.limit(100).get();
    if (page.empty) return;
    const batch = db.batch();
    page.docs.forEach(doc => batch.delete(doc.ref));
    await batch.commit();
  }
}

exports.deleteAccount = onCall({ region: "us-central1", timeoutSeconds: 540 }, async request => {
  const uid = request.auth?.uid;
  const authTime = request.auth?.token?.auth_time;
  if (!uid) throw new HttpsError("unauthenticated", "Inicia sesión para continuar");
  if (typeof authTime !== "number" || Math.floor(Date.now() / 1000) - authTime > 300 || authTime > Math.floor(Date.now() / 1000) + 60) {
    throw new HttpsError("failed-precondition", "Confirma tu identidad de nuevo para continuar");
  }

  const marker = db.collection("deletionJobs").doc(uid);
  await marker.set({ startedAt: FieldValue.serverTimestamp() }, { merge: true });

  // The marker blocks new writes while the operation is resumed or completed.
  while (true) {
    const owned = await db.collection("opportunities").where("ownerId", "==", uid).limit(25).get();
    if (owned.empty) break;
    for (const doc of owned.docs) {
      await deleteQuery(db.collection("reports").where("opportunityId", "==", doc.id));
      await db.recursiveDelete(doc.ref);
    }
  }
  while (true) {
    const applications = await db.collectionGroup("applications").where("applicantId", "==", uid).limit(100).get();
    if (applications.empty) break;
    for (const application of applications.docs) {
      await deleteQuery(application.ref.collection("messages"));
      await db.runTransaction(async transaction => {
        const current = await transaction.get(application.ref);
        const opportunityRef = application.ref.parent.parent;
        const opportunity = await transaction.get(opportunityRef);
        if (!current.exists) return;
        if (current.get("status") === "selected" && opportunity.exists) {
          transaction.update(opportunityRef, { selectedCount: Math.max(0, (opportunity.get("selectedCount") || 0) - 1) });
        }
        transaction.delete(application.ref);
      });
    }
  }
  await deleteQuery(db.collection("reports").where("reporterId", "==", uid));
  await db.collection("users").doc(uid).delete();
  try {
    await getAuth().deleteUser(uid);
  } catch (error) {
    if (error.code !== "auth/user-not-found") throw error;
  }
  // Keep a tombstone while previously issued Firebase ID tokens can still be valid.
  await marker.set({ completedAt: FieldValue.serverTimestamp(), expireAt: Timestamp.fromMillis(Date.now() + 2 * 60 * 60 * 1000) }, { merge: true });
  return { deleted: true };
});
