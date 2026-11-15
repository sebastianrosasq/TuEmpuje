import { after, before, beforeEach, test } from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { assertFails, assertSucceeds, initializeTestEnvironment } from '@firebase/rules-unit-testing';
import {
  collection, collectionGroup, deleteField, doc, getDoc, getDocs, query, runTransaction, serverTimestamp, setDoc, updateDoc, where, writeBatch,
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

test('los datos del negocio son privados y admiten solo valores válidos', async () => {
  const own = doc(user('cliente'), 'users', 'cliente');
  await assertSucceeds(updateDoc(own, { businessType: 'NEGOCIO', organization: 'Taller Central', businessSector: 'Fotografía', businessDescription: 'Servicios para eventos' }));
  await assertFails(getDoc(doc(user('candidato'), 'users', 'cliente')));
  await assertFails(updateDoc(doc(user('otro'), 'users', 'cliente'), { businessSector: 'Cambio ajeno' }));
  await assertFails(updateDoc(own, { businessType: 'EMPRESA_VERIFICADA' }));
  await assertFails(updateDoc(own, { organization: '' }));
  await assertFails(updateDoc(own, { businessDescription: 'x'.repeat(501) }));
});

test('la experiencia previa acepta un indicador booleano y conserva compatibilidad', async () => {
  const db = user('cliente');
  await assertSucceeds(setDoc(doc(db, 'opportunities', 'antigua'), opportunity('cliente')));
  await assertSucceeds(setDoc(doc(db, 'opportunities', 'primera'), { ...opportunity('cliente'), noExperienceRequired: true }));
  await assertSucceeds(setDoc(doc(db, 'opportunities', 'hibrida'), { ...opportunity('cliente'), workMode: 'Híbrido' }));
  await assertFails(setDoc(doc(db, 'opportunities', 'modalidad-invalida'), { ...opportunity('cliente'), workMode: 'Inventada' }));
  await assertFails(setDoc(doc(db, 'opportunities', 'invalida'), { ...opportunity('cliente'), noExperienceRequired: 'sí' }));
  await assertSucceeds(updateDoc(doc(db, 'opportunities', 'primera'), { noExperienceRequired: false, updatedAt: serverTimestamp() }));
  await assertFails(updateDoc(doc(db, 'opportunities', 'primera'), { noExperienceRequired: 'no', updatedAt: serverTimestamp() }));
});

test('el negocio solo se comparte cuando coincide con el perfil y puede retirarse de una oferta', async () => {
  const owner = user('cliente');
  await assertSucceeds(updateDoc(doc(owner, 'users', 'cliente'), { organization: 'Taller Central', businessSector: 'Fotografía', businessDescription: 'Servicios para eventos' }));
  const fields = { publicBusinessName: 'Taller Central', publicBusinessSector: 'Fotografía', publicBusinessDescription: 'Servicios para eventos' };
  await assertFails(setDoc(doc(owner, 'opportunities', 'nombre-falso'), { ...opportunity('cliente'), ...fields, publicBusinessName: 'Otro negocio' }));
  await assertFails(setDoc(doc(owner, 'opportunities', 'parcial'), { ...opportunity('cliente'), publicBusinessName: 'Taller Central' }));
  const job = doc(owner, 'opportunities', 'con-negocio');
  await assertSucceeds(setDoc(job, { ...opportunity('cliente'), ...fields }));
  const read = await assertSucceeds(getDoc(doc(user('candidato'), 'opportunities', 'con-negocio')));
  assert.equal(read.data().publicBusinessName, 'Taller Central');
  await assertFails(getDoc(doc(user('candidato'), 'users', 'cliente')));
  await assertFails(updateDoc(job, { publicBusinessDescription: 'Información falsa', updatedAt: serverTimestamp() }));
  await env.withSecurityRulesDisabled(async context => {
    await updateDoc(doc(context.firestore(), 'opportunities', 'con-negocio'), { selectedCount: 1 });
  });
  await assertFails(updateDoc(doc(user('otro'), 'opportunities', 'con-negocio'), { publicBusinessName: deleteField(), publicBusinessSector: deleteField(), publicBusinessDescription: deleteField(), updatedAt: serverTimestamp() }));
  await assertFails(updateDoc(job, { publicBusinessName: deleteField(), title: 'Cambio no permitido', updatedAt: serverTimestamp() }));
  await assertSucceeds(updateDoc(job, { publicBusinessName: deleteField(), publicBusinessSector: deleteField(), publicBusinessDescription: deleteField(), updatedAt: serverTimestamp() }));
});

test('la experiencia y la referencia requieren la confirmación de ambas partes', async () => {
  await env.withSecurityRulesDisabled(async context => {
    await setDoc(doc(context.firestore(), 'opportunities', 'oferta', 'applications', 'candidato'), { ...application('candidato'), status: 'selected' });
  });
  const applicant = user('candidato');
  const owner = user('cliente');
  const other = user('otro');
  const app = doc(applicant, 'opportunities', 'oferta', 'applications', 'candidato');
  const data = { opportunityId: 'oferta', workerId: 'candidato', authorId: 'cliente', authorName: 'Perfil cliente', title: 'Ayudante de fotografía', text: 'Colaboró con responsabilidad', visible: false, createdAt: serverTimestamp() };
  const ref = doc(owner, 'workReferences', 'oferta_candidato');
  await assertFails(updateDoc(app, { workerConfirmedAt: serverTimestamp() }));
  await env.withSecurityRulesDisabled(async context => {
    await updateDoc(doc(context.firestore(), 'opportunities', 'oferta', 'applications', 'candidato'), { status: 'completed' });
  });
  await assertFails(setDoc(ref, data));
  await assertFails(updateDoc(doc(other, 'opportunities', 'oferta', 'applications', 'candidato'), { workerConfirmedAt: serverTimestamp() }));
  await assertFails(updateDoc(app, { workerConfirmedAt: serverTimestamp(), status: 'pending' }));
  await assertSucceeds(updateDoc(app, { workerConfirmedAt: serverTimestamp() }));
  await assertFails(updateDoc(app, { workerConfirmedAt: serverTimestamp() }));
  await assertFails(setDoc(doc(other, 'workReferences', 'oferta_candidato'), { ...data, authorId: 'otro' }));
  await assertFails(setDoc(ref, { ...data, visible: true }));
  await assertSucceeds(setDoc(ref, data));
  await assertFails(getDoc(doc(other, 'workReferences', 'oferta_candidato')));
  await assertSucceeds(getDoc(doc(applicant, 'workReferences', 'oferta_candidato')));
  await assertFails(updateDoc(ref, { text: 'Texto modificado sin autorización' }));
  await assertSucceeds(updateDoc(doc(applicant, 'workReferences', 'oferta_candidato'), { visible: true }));
  await assertSucceeds(getDoc(doc(other, 'workReferences', 'oferta_candidato')));
  await assertSucceeds(getDocs(query(collection(other, 'workReferences'), where('workerId', '==', 'candidato'), where('visible', '==', true))));
  await assertSucceeds(updateDoc(doc(applicant, 'workReferences', 'oferta_candidato'), { visible: false }));
  const dispute = { referenceId: 'oferta_candidato', workerId: 'candidato', details: 'La descripción no coincide con lo ocurrido', status: 'open', createdAt: serverTimestamp() };
  await assertFails(setDoc(doc(other, 'referenceDisputes', 'oferta_candidato'), { ...dispute, workerId: 'otro' }));
  await assertSucceeds(setDoc(doc(applicant, 'referenceDisputes', 'oferta_candidato'), dispute));
  await assertFails(updateDoc(doc(applicant, 'referenceDisputes', 'oferta_candidato'), { details: 'cambiado' }));
  await env.withSecurityRulesDisabled(async context => {
    await updateDoc(doc(context.firestore(), 'referenceDisputes', 'oferta_candidato'), { status: 'resolved', resolution: 'hide' });
  });
  await assertFails(updateDoc(doc(applicant, 'workReferences', 'oferta_candidato'), { visible: true }));
});

test('recorrido completo de postulación, confirmación y referencia compartida', async () => {
  const worker = user('candidato');
  const owner = user('cliente');
  const job = doc(owner, 'opportunities', 'oferta');
  const appOwner = doc(owner, 'opportunities', 'oferta', 'applications', 'candidato');
  const appWorker = doc(worker, 'opportunities', 'oferta', 'applications', 'candidato');
  await assertSucceeds(setDoc(appWorker, application('candidato')));
  await assertSucceeds(runTransaction(owner, async tx => {
    await tx.get(job);
    await tx.get(appOwner);
    tx.update(job, { selectedCount: 1, lastSelectedApplicantId: 'candidato' });
    tx.update(appOwner, { status: 'selected', selectedAt: serverTimestamp() });
  }));
  await assertSucceeds(updateDoc(appOwner, { status: 'completed', completedAt: serverTimestamp() }));
  await assertSucceeds(updateDoc(appWorker, { workerConfirmedAt: serverTimestamp() }));
  await assertSucceeds(setDoc(doc(owner, 'workReferences', 'oferta_candidato'), {
    opportunityId: 'oferta', workerId: 'candidato', authorId: 'cliente', authorName: 'Perfil cliente',
    title: 'Ayudante de fotografía', text: 'Cumplió con las tareas acordadas', visible: false, createdAt: serverTimestamp(),
  }));
  await assertSucceeds(updateDoc(doc(worker, 'workReferences', 'oferta_candidato'), { visible: true }));
  await assertSucceeds(getDoc(doc(user('otro'), 'workReferences', 'oferta_candidato')));
});

test('cerrar, revisar y cancelar selección conserva el conteo de vacantes', async () => {
  const worker = user('candidato'), owner = user('cliente'), other = user('otro');
  const job = doc(owner, 'opportunities', 'oferta');
  const appOwner = doc(owner, 'opportunities', 'oferta', 'applications', 'candidato');
  const appWorker = doc(worker, 'opportunities', 'oferta', 'applications', 'candidato');
  await assertSucceeds(setDoc(appWorker, application('candidato')));
  await assertFails(updateDoc(doc(other, 'opportunities', 'oferta'), { status: 'closed', statusChangedAt: serverTimestamp() }));
  await assertSucceeds(updateDoc(job, { status: 'closed', statusChangedAt: serverTimestamp() }));
  await assertFails(setDoc(doc(other, 'opportunities', 'oferta', 'applications', 'otro'), application('otro')));
  await assertFails(updateDoc(appWorker, { status: 'reviewing', reviewedAt: serverTimestamp() }));
  await assertSucceeds(updateDoc(appOwner, { status: 'reviewing', reviewedAt: serverTimestamp() }));
  await assertSucceeds(runTransaction(owner, async tx => {
    await tx.get(job); await tx.get(appOwner);
    tx.update(job, { selectedCount: 1, lastSelectedApplicantId: 'candidato' });
    tx.update(appOwner, { status: 'selected', selectedAt: serverTimestamp() });
  }));
  await assertFails(updateDoc(job, { status: 'published', statusChangedAt: serverTimestamp() }));
  await assertFails(updateDoc(appOwner, { status: 'cancelled', decisionAt: serverTimestamp() }));
  await assertFails(updateDoc(job, { selectedCount: 0, lastCancelledApplicantId: 'candidato' }));
  await assertSucceeds(runTransaction(owner, async tx => {
    await tx.get(job); await tx.get(appOwner);
    tx.update(job, { selectedCount: 0, lastCancelledApplicantId: 'candidato' });
    tx.update(appOwner, { status: 'cancelled', decisionAt: serverTimestamp() });
  }));
  await assertFails(updateDoc(appWorker, { status: 'pending', createdAt: serverTimestamp(), selectedAt: deleteField(), reviewedAt: deleteField(), decisionAt: deleteField() }));
  await assertSucceeds(updateDoc(job, { status: 'published', statusChangedAt: serverTimestamp() }));
  await assertSucceeds(updateDoc(appWorker, { status: 'pending', createdAt: serverTimestamp(), selectedAt: deleteField(), reviewedAt: deleteField(), decisionAt: deleteField() }));
});

test('solo el empleador decide revisar o no seleccionar una postulación', async () => {
  const worker = user('candidato'), owner = user('cliente');
  const appWorker = doc(worker, 'opportunities', 'oferta', 'applications', 'candidato');
  const appOwner = doc(owner, 'opportunities', 'oferta', 'applications', 'candidato');
  await assertSucceeds(setDoc(appWorker, application('candidato')));
  await assertFails(updateDoc(appWorker, { status: 'rejected', decisionAt: serverTimestamp() }));
  await assertSucceeds(updateDoc(appOwner, { status: 'rejected', decisionAt: serverTimestamp() }));
  await assertFails(updateDoc(appWorker, { status: 'pending', createdAt: serverTimestamp(), decisionAt: deleteField() }));
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
