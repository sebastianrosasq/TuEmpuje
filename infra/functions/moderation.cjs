const { initializeApp, applicationDefault } = require('firebase-admin/app');
const { getFirestore, FieldValue } = require('firebase-admin/firestore');

async function listOpenReports(db) {
  const snapshot = await db.collection('reports').where('status', '==', 'open').limit(50).get();
  return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
}

async function listOpenDisputes(db) {
  const snapshot = await db.collection('referenceDisputes').where('status', '==', 'open').limit(50).get();
  return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
}

async function resolveDispute(db, referenceId, action, operator) {
  if (!referenceId || referenceId.includes('/') || !['keep', 'hide'].includes(action) || !operator?.trim()) {
    throw new Error('Indica una referencia, una acción válida y el nombre de quien revisa');
  }
  const disputeRef = db.collection('referenceDisputes').doc(referenceId);
  const referenceRef = db.collection('workReferences').doc(referenceId);
  const auditRef = db.collection('moderationActions').doc();
  return db.runTransaction(async transaction => {
    const dispute = await transaction.get(disputeRef);
    if (!dispute.exists || dispute.get('status') !== 'open') throw new Error('El reporte no existe o ya fue atendido');
    const reference = await transaction.get(referenceRef);
    if (!reference.exists) throw new Error('La referencia ya no existe');
    if (action === 'hide') transaction.update(referenceRef, { visible: false, moderationHidden: true });
    transaction.update(disputeRef, { status: 'resolved', resolution: action, resolvedAt: FieldValue.serverTimestamp() });
    transaction.create(auditRef, { type: 'reference', referenceId, action, operator: operator.trim().slice(0, 100), createdAt: FieldValue.serverTimestamp() });
    return action;
  });
}

async function resolveReport(db, reportId, action, operator) {
  if (!reportId || reportId.includes('/') || !['keep', 'remove'].includes(action) || !operator?.trim()) {
    throw new Error('Indica un reporte, una acción válida y el nombre de quien revisa');
  }
  const reportRef = db.collection('reports').doc(reportId);
  const auditRef = db.collection('moderationActions').doc();
  return db.runTransaction(async transaction => {
    const report = await transaction.get(reportRef);
    if (!report.exists || report.get('status') !== 'open') throw new Error('El reporte no existe o ya fue atendido');
    const opportunityId = report.get('opportunityId');
    if (typeof opportunityId !== 'string' || !opportunityId || opportunityId.includes('/')) throw new Error('La oportunidad del reporte no es válida');
    const opportunityRef = db.collection('opportunities').doc(opportunityId);
    if (action === 'remove') {
      const opportunity = await transaction.get(opportunityRef);
      if (!opportunity.exists) throw new Error('La oportunidad ya no existe');
      if (opportunity.get('status') === 'published') {
        transaction.update(opportunityRef, { status: 'removed', removedAt: FieldValue.serverTimestamp() });
      }
    }
    const result = action === 'remove' ? 'removed' : 'kept';
    transaction.update(reportRef, { status: 'resolved', resolution: result, resolvedAt: FieldValue.serverTimestamp() });
    transaction.create(auditRef, { reportId, opportunityId, action: result, operator: operator.trim().slice(0, 100), createdAt: FieldValue.serverTimestamp() });
    return result;
  });
}

async function main(args) {
  const projectIndex = args.indexOf('--project');
  const projectId = projectIndex >= 0 ? args[projectIndex + 1] : '';
  if (!projectId || projectId.startsWith('-')) throw new Error('Indica el proyecto con --project');
  const options = { projectId };
  if (!process.env.FIRESTORE_EMULATOR_HOST) options.credential = applicationDefault();
  initializeApp(options);
  const db = getFirestore();
  if (args[0] === 'list') {
    console.log(JSON.stringify(await listOpenReports(db), null, 2));
  } else if (args[0] === 'list-disputes') {
    console.log(JSON.stringify(await listOpenDisputes(db), null, 2));
  } else if (args[0] === 'resolve' || args[0] === 'resolve-dispute') {
    const operatorIndex = args.indexOf('--operator');
    if (!args.includes('--confirm') || operatorIndex < 0) throw new Error('La resolución requiere --operator y --confirm');
    console.log(await (args[0] === 'resolve' ? resolveReport : resolveDispute)(db, args[1], args[2], args[operatorIndex + 1]));
  } else {
    throw new Error('Usa list o resolve');
  }
}

if (require.main === module) main(process.argv.slice(2)).catch(error => { console.error(error.message); process.exitCode = 1; });
module.exports = { listOpenReports, resolveReport, listOpenDisputes, resolveDispute };
