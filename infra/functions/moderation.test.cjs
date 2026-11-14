const test = require('node:test');
const assert = require('node:assert/strict');
const { initializeApp } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const { listOpenReports, resolveReport, listOpenDisputes, resolveDispute } = require('./moderation.cjs');

initializeApp({ projectId: 'demo-tuempuje' });
const db = getFirestore();

test('la revisión cierra un reporte y conserva una oportunidad cuando corresponde', async () => {
  await db.collection('opportunities').doc('moderacion-conservar').set({ status: 'published' });
  await db.collection('reports').doc('moderacion-conservar').set({ status: 'open', opportunityId: 'moderacion-conservar' });
  assert.ok((await listOpenReports(db)).some(report => report.id === 'moderacion-conservar'));
  assert.equal(await resolveReport(db, 'moderacion-conservar', 'keep', 'Equipo'), 'kept');
  assert.equal((await db.collection('opportunities').doc('moderacion-conservar').get()).get('status'), 'published');
  assert.equal((await db.collection('reports').doc('moderacion-conservar').get()).get('status'), 'resolved');
  await assert.rejects(resolveReport(db, 'moderacion-conservar', 'remove', 'Equipo'));
});

test('retirar una oportunidad y cerrar el reporte son operaciones conjuntas', async () => {
  await db.collection('opportunities').doc('moderacion-retirar').set({ status: 'published' });
  await db.collection('reports').doc('moderacion-retirar').set({ status: 'open', opportunityId: 'moderacion-retirar' });
  assert.equal(await resolveReport(db, 'moderacion-retirar', 'remove', 'Equipo'), 'removed');
  assert.equal((await db.collection('opportunities').doc('moderacion-retirar').get()).get('status'), 'removed');
  assert.equal((await db.collection('reports').doc('moderacion-retirar').get()).get('resolution'), 'removed');
  assert.equal((await db.collection('moderationActions').where('reportId', '==', 'moderacion-retirar').get()).size, 1);
});

test('un reporte sin oportunidad no se resuelve parcialmente', async () => {
  await db.collection('reports').doc('moderacion-faltante').set({ status: 'open', opportunityId: 'no-existe' });
  await assert.rejects(resolveReport(db, 'moderacion-faltante', 'remove', 'Equipo'));
  assert.equal((await db.collection('reports').doc('moderacion-faltante').get()).get('status'), 'open');
});

test('se puede revisar una referencia y ocultarla con registro de la decisión', async () => {
  await db.collection('workReferences').doc('referencia').set({ visible: true });
  await db.collection('referenceDisputes').doc('referencia').set({ status: 'open' });
  assert.ok((await listOpenDisputes(db)).some(report => report.id === 'referencia'));
  assert.equal(await resolveDispute(db, 'referencia', 'hide', 'Equipo'), 'hide');
  assert.equal((await db.collection('workReferences').doc('referencia').get()).get('visible'), false);
  assert.equal((await db.collection('workReferences').doc('referencia').get()).get('moderationHidden'), true);
  assert.equal((await db.collection('referenceDisputes').doc('referencia').get()).get('status'), 'resolved');
  assert.equal((await db.collection('moderationActions').where('referenceId', '==', 'referencia').get()).size, 1);
  await assert.rejects(resolveDispute(db, 'referencia', 'keep', 'Equipo'));
});
