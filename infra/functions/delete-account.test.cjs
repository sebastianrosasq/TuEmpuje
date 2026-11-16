const test = require("node:test");
const assert = require("node:assert/strict");
const { initializeApp } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");
const { getFirestore } = require("firebase-admin/firestore");

initializeApp({ projectId: "demo-tuempuje" });
const db = getFirestore();
const endpoint = "http://127.0.0.1:5001/demo-tuempuje/us-central1/deleteAccount";

async function call(token) {
  return fetch(endpoint, { method: "POST", headers: { "content-type": "application/json", ...(token ? { authorization: `Bearer ${token}` } : {}) }, body: JSON.stringify({ data: {} }) });
}

test("la eliminación requiere autenticación y elimina datos asociados sin afectar a terceros", async () => {
  assert.equal((await call()).status, 401);
  const signup = await fetch("http://127.0.0.1:9099/identitytoolkit.googleapis.com/v1/accounts:signUp?key=demo", {
    method: "POST", headers: { "content-type": "application/json" },
    body: JSON.stringify({ email: "persona@example.test", password: "Prueba1234", returnSecureToken: true }),
  });
  const account = await signup.json();
  assert.ok(account.idToken);
  const uid = account.localId;
  await db.collection("users").doc(uid).set({ uid });
  const own = db.collection("opportunities").doc("propia");
  const other = db.collection("opportunities").doc("ajena");
  await own.set({ ownerId: uid, selectedCount: 0 });
  await own.collection("applications").doc("otra-persona").set({ applicantId: "otra-persona" });
  await other.set({ ownerId: "otra-persona", selectedCount: 1 });
  await other.collection("applications").doc(uid).set({ applicantId: uid, status: "selected" });
  await other.collection("applications").doc(uid).collection("messages").doc("mensaje").set({ senderId: uid, text: "Hola" });
  await other.collection("applications").doc(uid).collection("privateFiles").doc("cv").set({ contentType: "application/pdf" });
  await db.collection("reports").doc("reporte-propio").set({ reporterId: uid, opportunityId: "ajena" });
  await db.collection("reports").doc("reporte-ajeno").set({ reporterId: "otra-persona", opportunityId: "propia" });
  await db.collection("workReferences").doc("propia_otra-persona").set({ opportunityId: "propia", workerId: "otra-persona", authorId: uid });
  await db.collection("referenceDisputes").doc("propia_otra-persona").set({ workerId: "otra-persona" });
  await db.collection("workReferences").doc(`ajena_${uid}`).set({ opportunityId: "ajena", workerId: uid, authorId: "otra-persona" });
  await db.collection("referenceDisputes").doc(`ajena_${uid}`).set({ workerId: uid });
  await db.collection("workReferences").doc("tercero_conservado").set({ opportunityId: "tercero", workerId: "otra-persona", authorId: "tercero" });
  const response = await call(account.idToken);
  assert.equal(response.status, 200, await response.text());
  assert.equal((await db.collection("users").doc(uid).get()).exists, false);
  assert.equal((await own.get()).exists, false);
  assert.equal((await own.collection("applications").doc("otra-persona").get()).exists, false);
  assert.equal((await other.get()).get("selectedCount"), 0);
  assert.equal((await other.collection("applications").doc(uid).get()).exists, false);
  assert.equal((await other.collection("applications").doc(uid).collection("messages").get()).empty, true);
  assert.equal((await other.collection("applications").doc(uid).collection("privateFiles").get()).empty, true);
  assert.equal((await db.collection("reports").get()).empty, true);
  assert.equal((await db.collection("workReferences").get()).size, 1);
  assert.equal((await db.collection("referenceDisputes").get()).empty, true);
  assert.ok((await db.collection("deletionJobs").doc(uid).get()).get("expireAt"));
  await assert.rejects(getAuth().getUser(uid), { code: "auth/user-not-found" });
});
