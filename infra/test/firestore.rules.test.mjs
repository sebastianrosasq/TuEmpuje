import { after, before, beforeEach, test } from 'node:test';
import { readFile } from 'node:fs/promises';
import { assertFails, assertSucceeds, initializeTestEnvironment } from '@firebase/rules-unit-testing';
import {
  collection, collectionGroup, doc, getDoc, getDocs, query, runTransaction, serverTimestamp, setDoc, updateDoc, where, writeBatch,
} from 'firebase/firestore';

const projectId = 'demo-tuempuje';
let env;

const user = (uid) => env.authenticatedContext(uid, { email: `${uid}@example.invalid` }).firestore();
const profile = (uid) => ({ uid, displayName: `Perfil ${uid}`, email: `${uid}@example.invalid`, dni: '12345678', phone: '987654321', skills: 'Edición', availability: 'Tardes', role: uid === 'cliente' ? 'employer' : 'worker' });
const opportunity = (uid) => ({
  ownerId: uid,
  ownerName: `Perfil ${uid}`,
  title: 'Ayudante de fotografía',
  description: 'Apoyo con equipo durante una sesión',
  location: 'Arequipa',
  pay: 'S/ 60 por jornada',
  duration: '4 horas',
  category: 'Ayudantía',
  requirements: 'Puntualidad',
  vacancies: 1,
  selectedCount: 0,
  status: 'published',
  createdAt: serverTimestamp(),
});
const application = (uid) => ({ applicantId: uid, applicantName: `Perfil ${uid}`, applicantSkills: 'Edición', applicantAvailability: 'Tardes', applicantExperience: '', applicantDistrict: '', applicantEducation: '', applicantPortfolio: '', status: 'pending', createdAt: serverTimestamp() });

before(async () => {
  env = await initializeTestEnvironment({
    projectId,
    firestore: { rules: await readFile(new URL('../firestore.rules', import.meta.url), 'utf8') },
  });
});

beforeEach(async () => {
  await env.clearFirestore();
  await env.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    for (const uid of ['cliente', 'candidato', 'otro']) {
      await setDoc(doc(db, 'users', uid), profile(uid));
    }
    await setDoc(doc(db, 'opportunities', 'oferta'), opportunity('cliente'));
  });
});

after(async () => {
  if (env) await env.cleanup();
});

test('solo el titular crea, lee y edita su perfil privado', async () => {
  const db = user('nueva');
  const own = doc(db, 'users', 'nueva');
  await assertSucceeds(setDoc(own, profile('nueva')));
  await assertSucceeds(getDoc(own));
  await assertSucceeds(updateDoc(own, { displayName: 'Nombre actualizado' }));
  await assertSucceeds(updateDoc(own, { skills: 'Edición', experience: 'Proyectos personales', availability: 'Tardes' }));
  await assertSucceeds(updateDoc(own, { nationality: 'Peruana' }));
  await assertFails(updateDoc(own, { nationality: '' }));
  await assertFails(getDoc(doc(user('otro'), 'users', 'nueva')));
  await assertFails(getDocs(collection(user('otro'), 'users')));
  await assertFails(updateDoc(doc(user('otro'), 'users', 'nueva'), { displayName: 'Cambio ajeno' }));
  await assertFails(updateDoc(doc(user('otro'), 'users', 'nueva'), { skills: 'Cambio ajeno' }));
  await assertFails(updateDoc(own, { skills: 'x'.repeat(501) }));
});

test('impide suplantación de correo, identidad y privilegios en perfiles', async () => {
  const db = user('nueva');
  await assertFails(setDoc(doc(db, 'users', 'nueva'), { ...profile('nueva'), email: 'otro@example.invalid' }));
  await assertFails(setDoc(doc(db, 'users', 'otro'), profile('otro')));
  await assertFails(setDoc(doc(db, 'users', 'nueva'), { ...profile('nueva'), role: 'admin' }));
  await assertFails(setDoc(doc(db, 'users', 'nueva'), { ...profile('nueva'), displayName: 'A' }));
  await assertFails(setDoc(doc(db, 'users', 'nueva'), { ...profile('nueva'), displayName: '   ' }));
  await assertFails(setDoc(doc(db, 'users', 'nueva'), { ...profile('nueva'), dni: '123' }));
  await assertFails(setDoc(doc(db, 'users', 'nueva'), { ...profile('nueva'), phone: '123456789' }));
  const { dni, ...missingDni } = profile('nueva');
  await assertFails(setDoc(doc(db, 'users', 'nueva'), missingDni));
  await assertFails(updateDoc(doc(user('cliente'), 'users', 'cliente'), { email: 'otro@example.invalid' }));
  await assertFails(updateDoc(doc(user('cliente'), 'users', 'cliente'), { role: 'admin' }));
});

test('un invitado no puede leer ni escribir datos privados', async () => {
  const guest = env.unauthenticatedContext().firestore();
  await assertFails(getDoc(doc(guest, 'users', 'cliente')));
  await assertFails(getDoc(doc(guest, 'opportunities', 'oferta')));
  await assertFails(setDoc(doc(guest, 'users', 'invitado'), profile('invitado')));
  await assertFails(setDoc(doc(guest, 'opportunities', 'nueva'), opportunity('invitado')));
});

test('una cuenta normal no puede consultar ni alterar registros de moderación', async () => {
  await env.withSecurityRulesDisabled(async context => {
    await setDoc(doc(context.firestore(), 'moderationActions', 'revision'), { action: 'removed' });
  });
  const record = doc(user('candidato'), 'moderationActions', 'revision');
  await assertFails(getDoc(record));
  await assertFails(updateDoc(record, { action: 'kept' }));
  await assertFails(setDoc(doc(user('candidato'), 'moderationActions', 'nueva'), { action: 'removed' }));
});

test('acepta documentos extranjeros válidos sin compartirlos en la postulación', async () => {
  const db = user('candidato');
  const own = doc(db, 'users', 'candidato');
  await assertSucceeds(updateDoc(own, { documentType: 'CE', dni: '12345678901' }));
  await assertFails(updateDoc(own, { documentType: 'CE', dni: 'ABC123' }));
  await assertSucceeds(setDoc(doc(db, 'opportunities', 'oferta', 'applications', 'candidato'), application('candidato')));
  await assertFails(getDoc(doc(user('cliente'), 'users', 'candidato')));
  await assertSucceeds(updateDoc(own, { documentType: 'PASAPORTE', dni: 'AB1234567' }));
  await assertFails(updateDoc(own, { documentType: 'PASAPORTE', dni: 'A'.repeat(16) }));
  await assertFails(updateDoc(own, { documentType: 'OTRO', dni: '12345678' }));
});

test('oportunidades publicadas pueden consultarse por cuentas autenticadas', async () => {
  const db = user('candidato');
  await assertSucceeds(getDoc(doc(db, 'opportunities', 'oferta')));
  await assertSucceeds(getDocs(query(collection(db, 'opportunities'), where('status', '==', 'published'))));
  await assertFails(getDocs(collection(db, 'opportunities')));
});

test('solo se crean oportunidades propias y con campos válidos', async () => {
  const db = user('cliente');
  await assertSucceeds(setDoc(doc(db, 'opportunities', 'valida'), opportunity('cliente')));
  await assertFails(setDoc(doc(db, 'opportunities', 'suplantada'), opportunity('otro')));
  await assertFails(setDoc(doc(db, 'opportunities', 'nombre-falso'), { ...opportunity('cliente'), ownerName: 'Otro nombre' }));
  await assertFails(setDoc(doc(db, 'opportunities', 'incompleta'), { ...opportunity('cliente'), title: '' }));
  await assertFails(setDoc(doc(db, 'opportunities', 'vacantes'), { ...opportunity('cliente'), vacancies: 0 }));
  await assertFails(setDoc(doc(db, 'opportunities', 'estado'), { ...opportunity('cliente'), status: 'closed' }));
  await assertFails(setDoc(doc(db, 'opportunities', 'extra'), { ...opportunity('cliente'), verified: true }));
  await assertFails(setDoc(doc(user('sinperfil'), 'opportunities', 'sin-perfil'), opportunity('sinperfil')));
  await assertFails(setDoc(doc(user('candidato'), 'opportunities', 'trabajador-no-publica'), opportunity('candidato')));
});

test('ninguna cuenta altera vacantes, estado o dueño en esta etapa', async () => {
  await assertFails(updateDoc(doc(user('cliente'), 'opportunities', 'oferta'), { vacancies: 10 }));
  await assertFails(updateDoc(doc(user('cliente'), 'opportunities', 'oferta'), { status: 'closed' }));
  await assertFails(updateDoc(doc(user('otro'), 'opportunities', 'oferta'), { ownerId: 'otro' }));
});

test('la postulación exige identidad propia y no puede duplicarse', async () => {
  const candidate = user('candidato');
  const ref = doc(candidate, 'opportunities', 'oferta', 'applications', 'candidato');
  await assertSucceeds(setDoc(ref, application('candidato')));
  await assertFails(setDoc(ref, application('candidato')));
  await assertFails(setDoc(doc(candidate, 'opportunities', 'oferta', 'applications', 'otro'), application('otro')));
  await assertFails(setDoc(doc(user('otro'), 'opportunities', 'oferta', 'applications', 'otro'), { ...application('otro'), applicantName: 'Nombre ajeno' }));
  await assertFails(setDoc(doc(user('cliente'), 'opportunities', 'oferta', 'applications', 'cliente'), application('cliente')));
  await assertFails(setDoc(doc(user('otro'), 'opportunities', 'inexistente', 'applications', 'otro'), application('otro')));
});

test('dos selecciones simultáneas no pueden cubrir la misma vacante', async () => {
  for (const uid of ['candidato', 'otro']) {
    await assertSucceeds(setDoc(doc(user(uid), 'opportunities', 'oferta', 'applications', uid), application(uid)));
  }
  const db = user('cliente');
  const job = doc(db, 'opportunities', 'oferta');
  const select = (uid) => runTransaction(db, async (transaction) => {
    const current = await transaction.get(job);
    transaction.update(job, { selectedCount: current.data().selectedCount + 1, lastSelectedApplicantId: uid });
    transaction.update(doc(db, 'opportunities', 'oferta', 'applications', uid), { status: 'selected', selectedAt: serverTimestamp() });
  });
  const results = await Promise.allSettled([select('candidato'), select('otro')]);
  if (results.filter((r) => r.status === 'fulfilled').length !== 1) throw new Error('La vacante debe asignarse una sola vez');
  const final = await getDoc(job);
  if (final.data().selectedCount !== 1) throw new Error('El conteo de seleccionados es incorrecto');
});

test('no se aceptan postulaciones nuevas cuando las vacantes están cubiertas', async () => {
  await assertSucceeds(setDoc(doc(user('candidato'), 'opportunities', 'oferta', 'applications', 'candidato'), application('candidato')));
  const db = user('cliente');
  const batch = writeBatch(db);
  batch.update(doc(db, 'opportunities', 'oferta'), { selectedCount: 1, lastSelectedApplicantId: 'candidato' });
  batch.update(doc(db, 'opportunities', 'oferta', 'applications', 'candidato'), { status: 'selected', selectedAt: serverTimestamp() });
  await assertSucceeds(batch.commit());
  await assertFails(setDoc(doc(user('otro'), 'opportunities', 'oferta', 'applications', 'otro'), application('otro')));
});

test('postulaciones solo las leen su titular y el dueño de la oportunidad', async () => {
  const path = ['opportunities', 'oferta', 'applications', 'candidato'];
  await assertSucceeds(setDoc(doc(user('candidato'), ...path), application('candidato')));
  await assertSucceeds(getDoc(doc(user('candidato'), ...path)));
  await assertSucceeds(getDoc(doc(user('cliente'), ...path)));
  await assertSucceeds(getDocs(collection(user('cliente'), 'opportunities', 'oferta', 'applications')));
  await assertFails(getDoc(doc(user('otro'), ...path)));
  await assertFails(getDocs(collection(user('otro'), 'opportunities', 'oferta', 'applications')));
  await assertFails(getDoc(doc(env.unauthenticatedContext().firestore(), ...path)));
  await assertSucceeds(getDocs(query(collectionGroup(user('candidato'), 'applications'), where('applicantId', '==', 'candidato'))));
  await assertFails(getDocs(collectionGroup(user('otro'), 'applications')));
});

test('no se pueden falsificar estados ni seleccionar sin actualizar las vacantes', async () => {
  const path = ['opportunities', 'oferta', 'applications', 'candidato'];
  await assertSucceeds(setDoc(doc(user('candidato'), ...path), application('candidato')));
  await assertFails(updateDoc(doc(user('cliente'), ...path), { status: 'selected' }));
  await assertFails(updateDoc(doc(user('candidato'), ...path), { status: 'selected' }));
  await assertFails(setDoc(doc(user('otro'), 'opportunities', 'oferta', 'applications', 'otro'), { ...application('otro'), status: 'selected' }));
});

test('solo el dueño selecciona de forma atómica y no supera las vacantes', async () => {
  await assertSucceeds(setDoc(doc(user('candidato'), 'opportunities', 'oferta', 'applications', 'candidato'), application('candidato')));
  await assertSucceeds(setDoc(doc(user('otro'), 'opportunities', 'oferta', 'applications', 'otro'), application('otro')));
  const db = user('cliente');
  const job = doc(db, 'opportunities', 'oferta');
  const first = doc(db, 'opportunities', 'oferta', 'applications', 'candidato');
  const batch = writeBatch(db);
  batch.update(job, { selectedCount: 1, lastSelectedApplicantId: 'candidato' });
  batch.update(first, { status: 'selected', selectedAt: serverTimestamp() });
  await assertSucceeds(batch.commit());
  const second = writeBatch(db);
  second.update(job, { selectedCount: 2, lastSelectedApplicantId: 'otro' });
  second.update(doc(db, 'opportunities', 'oferta', 'applications', 'otro'), { status: 'selected', selectedAt: serverTimestamp() });
  await assertFails(second.commit());
});

test('solo quien publica puede finalizar un trabajo seleccionado', async () => {
  await assertSucceeds(setDoc(doc(user('candidato'), 'opportunities', 'oferta', 'applications', 'candidato'), application('candidato')));
  const db = user('cliente');
  const job = doc(db, 'opportunities', 'oferta');
  const app = doc(db, 'opportunities', 'oferta', 'applications', 'candidato');
  const batch = writeBatch(db);
  batch.update(job, { selectedCount: 1, lastSelectedApplicantId: 'candidato' });
  batch.update(app, { status: 'selected', selectedAt: serverTimestamp() });
  await assertSucceeds(batch.commit());
  await assertFails(updateDoc(doc(user('candidato'), 'opportunities', 'oferta', 'applications', 'candidato'), { status: 'completed', completedAt: serverTimestamp() }));
  await assertFails(updateDoc(doc(user('otro'), 'opportunities', 'oferta', 'applications', 'candidato'), { status: 'completed', completedAt: serverTimestamp() }));
  await assertFails(updateDoc(app, { status: 'completed', completedAt: serverTimestamp(), paid: true }));
  await assertSucceeds(updateDoc(app, { status: 'completed', completedAt: serverTimestamp() }));
  await assertFails(updateDoc(app, { status: 'completed', completedAt: serverTimestamp() }));
});

test('los reportes son privados, válidos y no se duplican', async () => {
  const db = user('candidato');
  const ref = doc(db, 'reports', 'candidato_oferta');
  const report = { reporterId: 'candidato', opportunityId: 'oferta', reason: 'Seguridad', details: 'Solicitan datos sin explicar su uso', status: 'open', createdAt: serverTimestamp() };
  await assertFails(setDoc(doc(db, 'reports', 'candidato_otra'), { ...report, opportunityId: 'otra' }));
  await assertFails(setDoc(doc(db, 'reports', 'otro_oferta'), report));
  await assertFails(setDoc(ref, { ...report, details: 'corto' }));
  await assertSucceeds(setDoc(ref, report));
  await assertFails(setDoc(ref, report));
  await assertFails(getDoc(ref));
  await assertFails(getDocs(collection(user('otro'), 'reports')));
});


test('el bloqueo de eliminación impide nuevas escrituras y postulaciones a la oferta', async () => {
  await env.withSecurityRulesDisabled(async context => {
    await setDoc(doc(context.firestore(), 'deletionJobs', 'cliente'), { startedAt: serverTimestamp() });
  });
  await assertFails(updateDoc(doc(user('cliente'), 'users', 'cliente'), { displayName: 'Otro nombre' }));
  await assertFails(setDoc(doc(user('cliente'), 'opportunities', 'nueva'), opportunity('cliente')));
  await assertFails(setDoc(doc(user('candidato'), 'opportunities', 'oferta', 'applications', 'candidato'), application('candidato')));
  await assertFails(setDoc(doc(user('cliente'), 'deletionJobs', 'cliente'), { startedAt: serverTimestamp() }));
});

test('solo el responsable edita u oculta una oferta sin selección y se conserva la postulación', async () => {
  const owner = user('cliente');
  const job = doc(owner, 'opportunities', 'oferta');
  await assertFails(updateDoc(doc(user('otro'), 'opportunities', 'oferta'), { title: 'Oferta alterada', updatedAt: serverTimestamp() }));
  await assertSucceeds(updateDoc(job, { title: 'Ayudante de diseño', updatedAt: serverTimestamp() }));
  await assertFails(updateDoc(job, { ownerId: 'otro', updatedAt: serverTimestamp() }));
  await assertSucceeds(setDoc(doc(user('candidato'), 'opportunities', 'oferta', 'applications', 'candidato'), application('candidato')));
  await assertSucceeds(updateDoc(job, { status: 'removed', removedAt: serverTimestamp() }));
  await assertFails(getDoc(doc(user('otro'), 'opportunities', 'oferta')));
  await assertSucceeds(getDoc(doc(user('candidato'), 'opportunities', 'oferta')));
  await assertSucceeds(getDoc(job));
  await assertSucceeds(getDocs(query(collection(owner, 'opportunities'), where('ownerId', '==', 'cliente'))));
});

test('el postulante retira su candidatura y puede volver a postular', async () => {
  const ref = doc(user('candidato'), 'opportunities', 'oferta', 'applications', 'candidato');
  await assertSucceeds(setDoc(ref, application('candidato')));
  await assertFails(updateDoc(doc(user('otro'), 'opportunities', 'oferta', 'applications', 'candidato'), { status: 'withdrawn', withdrawnAt: serverTimestamp() }));
  await assertSucceeds(updateDoc(ref, { status: 'withdrawn', withdrawnAt: serverTimestamp() }));
  const { deleteField } = await import('firebase/firestore');
  await assertSucceeds(updateDoc(ref, { status: 'pending', createdAt: serverTimestamp(), withdrawnAt: deleteField(), applicantName: 'Perfil candidato' }));
});

test('retirar una selección libera la vacante en una transacción', async () => {
  const candidate = user('candidato');
  const ref = doc(candidate, 'opportunities', 'oferta', 'applications', 'candidato');
  await assertSucceeds(setDoc(ref, application('candidato')));
  const owner = user('cliente');
  await assertSucceeds(runTransaction(owner, async tx => {
    const job = doc(owner, 'opportunities', 'oferta');
    const app = doc(owner, 'opportunities', 'oferta', 'applications', 'candidato');
    await tx.get(job); await tx.get(app);
    tx.update(job, { selectedCount: 1, lastSelectedApplicantId: 'candidato' });
    tx.update(app, { status: 'selected', selectedAt: serverTimestamp() });
  }));
  await assertFails(updateDoc(ref, { status: 'withdrawn', withdrawnAt: serverTimestamp() }));
  await assertSucceeds(runTransaction(candidate, async tx => {
    const job = doc(candidate, 'opportunities', 'oferta');
    await tx.get(job); await tx.get(ref);
    tx.update(job, { selectedCount: 0, lastWithdrawnApplicantId: 'candidato' });
    tx.update(ref, { status: 'withdrawn', withdrawnAt: serverTimestamp() });
  }));
});

test('la postulación requiere habilidades y disponibilidad en el perfil', async () => {
  await env.withSecurityRulesDisabled(async context => {
    await updateDoc(doc(context.firestore(), 'users', 'candidato'), { skills: '', availability: '' });
  });
  await assertFails(setDoc(doc(user('candidato'), 'opportunities', 'oferta', 'applications', 'candidato'), application('candidato')));
});

test('una oportunidad retirada no permite seleccionar postulantes', async () => {
  const owner = user('cliente');
  await assertSucceeds(setDoc(doc(user('candidato'), 'opportunities', 'oferta', 'applications', 'candidato'), application('candidato')));
  await assertSucceeds(updateDoc(doc(owner, 'opportunities', 'oferta'), { status: 'removed', removedAt: serverTimestamp() }));
  await assertFails(runTransaction(owner, async tx => {
    const job = doc(owner, 'opportunities', 'oferta');
    const app = doc(owner, 'opportunities', 'oferta', 'applications', 'candidato');
    await tx.get(job); await tx.get(app);
    tx.update(job, { selectedCount: 1, lastSelectedApplicantId: 'candidato' });
    tx.update(app, { status: 'selected', selectedAt: serverTimestamp() });
  }));
});

test('los datos compartidos deben corresponder al perfil del postulante', async () => {
  const ref = doc(user('candidato'), 'opportunities', 'oferta', 'applications', 'candidato');
  await assertFails(setDoc(ref, { ...application('candidato'), applicantSkills: 'Datos de otra persona' }));
});

test('los mensajes solo son visibles y enviables por participantes de la postulación', async () => {
  const candidate = user('candidato');
  const owner = user('cliente');
  const stranger = user('otro');
  const app = doc(candidate, 'opportunities', 'oferta', 'applications', 'candidato');
  await assertSucceeds(setDoc(app, application('candidato')));
  const path = ['opportunities', 'oferta', 'applications', 'candidato', 'messages'];
  const message = (uid) => ({ senderId: uid, text: 'Hola, coordinemos', createdAt: serverTimestamp() });
  await assertSucceeds(setDoc(doc(candidate, ...path, 'a'), message('candidato')));
  await assertSucceeds(setDoc(doc(owner, ...path, 'b'), message('cliente')));
  await assertSucceeds(getDocs(collection(candidate, ...path)));
  await assertSucceeds(getDocs(collection(owner, ...path)));
  await assertFails(getDocs(collection(stranger, ...path)));
  await assertFails(setDoc(doc(stranger, ...path, 'c'), message('otro')));
  await assertFails(setDoc(doc(candidate, ...path, 'd'), message('cliente')));
  await assertFails(setDoc(doc(candidate, ...path, 'e'), { ...message('candidato'), text: '   ' }));
  await assertFails(updateDoc(doc(candidate, ...path, 'a'), { text: 'Alterado' }));
  await assertSucceeds(updateDoc(app, { status: 'withdrawn', withdrawnAt: serverTimestamp() }));
  await assertFails(setDoc(doc(candidate, ...path, 'f'), message('candidato')));
  await assertSucceeds(getDocs(collection(candidate, ...path)));
});
