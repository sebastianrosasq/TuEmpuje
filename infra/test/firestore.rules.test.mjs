import { after, before, beforeEach, test } from 'node:test';
import { readFile } from 'node:fs/promises';
import { assertFails, assertSucceeds, initializeTestEnvironment } from '@firebase/rules-unit-testing';
import {
  collection, doc, getDoc, getDocs, query, serverTimestamp, setDoc, updateDoc, where,
} from 'firebase/firestore';

const projectId = 'demo-tuempuje';
let env;

const user = (uid) => env.authenticatedContext(uid, { email: `${uid}@example.invalid` }).firestore();
const profile = (uid) => ({ uid, displayName: `Perfil ${uid}`, email: `${uid}@example.invalid` });
const opportunity = (uid) => ({
  ownerId: uid,
  title: 'Ayudante de fotografía',
  description: 'Apoyo con equipo durante una sesión',
  location: 'Arequipa',
  pay: 'S/ 60 por jornada',
  duration: '4 horas',
  vacancies: 1,
  status: 'published',
  createdAt: serverTimestamp(),
});
const application = (uid) => ({ applicantId: uid, status: 'pending', createdAt: serverTimestamp() });

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
  await assertFails(getDoc(doc(user('otro'), 'users', 'nueva')));
  await assertFails(getDocs(collection(user('otro'), 'users')));
  await assertFails(updateDoc(doc(user('otro'), 'users', 'nueva'), { displayName: 'Cambio ajeno' }));
});

test('impide suplantación de correo, identidad y privilegios en perfiles', async () => {
  const db = user('nueva');
  await assertFails(setDoc(doc(db, 'users', 'nueva'), { ...profile('nueva'), email: 'otro@example.invalid' }));
  await assertFails(setDoc(doc(db, 'users', 'otro'), profile('otro')));
  await assertFails(setDoc(doc(db, 'users', 'nueva'), { ...profile('nueva'), role: 'admin' }));
  await assertFails(setDoc(doc(db, 'users', 'nueva'), { ...profile('nueva'), displayName: 'A' }));
  await assertFails(setDoc(doc(db, 'users', 'nueva'), { ...profile('nueva'), displayName: '   ' }));
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
  await assertFails(setDoc(doc(db, 'opportunities', 'incompleta'), { ...opportunity('cliente'), title: '' }));
  await assertFails(setDoc(doc(db, 'opportunities', 'vacantes'), { ...opportunity('cliente'), vacancies: 0 }));
  await assertFails(setDoc(doc(db, 'opportunities', 'estado'), { ...opportunity('cliente'), status: 'closed' }));
  await assertFails(setDoc(doc(db, 'opportunities', 'extra'), { ...opportunity('cliente'), verified: true }));
  await assertFails(setDoc(doc(user('sinperfil'), 'opportunities', 'sin-perfil'), opportunity('sinperfil')));
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
  await assertFails(setDoc(doc(user('cliente'), 'opportunities', 'oferta', 'applications', 'cliente'), application('cliente')));
  await assertFails(setDoc(doc(user('otro'), 'opportunities', 'inexistente', 'applications', 'otro'), application('otro')));
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
});

test('no se pueden seleccionar candidatos ni falsificar estados desde el cliente', async () => {
  const path = ['opportunities', 'oferta', 'applications', 'candidato'];
  await assertSucceeds(setDoc(doc(user('candidato'), ...path), application('candidato')));
  await assertFails(updateDoc(doc(user('cliente'), ...path), { status: 'selected' }));
  await assertFails(updateDoc(doc(user('candidato'), ...path), { status: 'selected' }));
  await assertFails(setDoc(doc(user('otro'), 'opportunities', 'oferta', 'applications', 'otro'), { ...application('otro'), status: 'selected' }));
});
