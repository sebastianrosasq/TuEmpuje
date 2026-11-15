import { readFile, writeFile } from 'node:fs/promises';
import { randomBytes } from 'node:crypto';
import { deleteApp, initializeApp } from 'firebase/app';
import { createUserWithEmailAndPassword, getAuth, signInWithEmailAndPassword, signOut } from 'firebase/auth';
import { collection, deleteField, doc, getDoc, getDocs, getFirestore, query, serverTimestamp, setDoc, terminate, updateDoc, where } from 'firebase/firestore';

const project = 'tuempuje-dev-e1835b';
const config = JSON.parse(await readFile(new URL('../android/app/google-services.json', import.meta.url), 'utf8'));
if (config.project_info.project_id !== project) throw new Error('La configuración no corresponde al proyecto de desarrollo');
const client = config.client.find(item => item.client_info.android_client_info.package_name === 'pe.tuempuje.app');
if (!client) throw new Error('No se encontró la aplicación Android en la configuración');
const app = initializeApp({ apiKey: client.api_key[0].current_key, appId: client.client_info.mobilesdk_app_id, projectId: project });
const auth = getAuth(app);
const db = getFirestore(app);
const credentialsFile = new URL('.env.demo.local', import.meta.url);
let credentials;
try { credentials = JSON.parse(await readFile(credentialsFile, 'utf8')); }
catch (error) {
  if (error.code !== 'ENOENT') throw error;
  const tag = randomBytes(6).toString('hex');
  credentials = {
    employer: { email: `empleador-${tag}@example.invalid`, password: randomBytes(18).toString('base64url') },
    worker: { email: `postulante-${tag}@example.invalid`, password: randomBytes(18).toString('base64url') },
  };
  await writeFile(credentialsFile, JSON.stringify(credentials, null, 2) + '\n', { flag: 'wx', mode: 0o600 });
}

async function signInOrCreate(account) {
  try { return (await signInWithEmailAndPassword(auth, account.email, account.password)).user; }
  catch (error) {
    if (!['auth/invalid-credential', 'auth/user-not-found'].includes(error.code)) throw error;
    return (await createUserWithEmailAndPassword(auth, account.email, account.password)).user;
  }
}

async function ensureProfile(user, name, fields) {
  const ref = doc(db, 'users', user.uid);
  if (!(await getDoc(ref)).exists()) {
    await setDoc(ref, { uid: user.uid, displayName: name, email: user.email, dni: '00000000', phone: '900000000', ...fields });
  }
}

const employer = await signInOrCreate(credentials.employer);
await ensureProfile(employer, 'TuEmpuje · Ejemplo', { role: 'employer', interests: [], district: 'Cercado', organization: 'TuEmpuje · Ejemplo' });
const existing = await getDocs(query(collection(db, 'opportunities'), where('ownerId', '==', employer.uid)));
const known = new Set(existing.docs.map(item => item.id));
const examples = [
  ['ejemplo-edicion', 'Edición de videos para una campaña', 'Creativo', 'Cercado, Arequipa', 'S/ 80.00 · Por tarea', 'Por coordinar · 2 días', 'Editar dos videos cortos con material proporcionado y entregar las versiones finales', 'Manejo básico de edición y organización de archivos'],
  ['ejemplo-inventario', 'Apoyo con inventario en una tienda', 'Ayudantía', 'Yanahuara, Arequipa', 'S/ 65.00 · Por día', 'Por coordinar · 6 horas', 'Contar productos, ordenar el almacén y registrar cantidades junto a la persona responsable', 'Orden, puntualidad y manejo básico de hojas de cálculo'],
  ['ejemplo-diseno', 'Diseño de piezas para redes', 'Diseño y edición', 'Remoto', 'S/ 90.00 · Por tarea', 'Por coordinar · 3 días', 'Preparar cuatro piezas para redes sociales a partir de un brief y material proporcionado', 'Conocimientos de diseño y herramientas de edición'],
  ['ejemplo-web', 'Apoyo con una página web sencilla', 'Programación', 'Cayma, Arequipa', 'S/ 120.00 · Por tarea', 'Por coordinar · 1 semana', 'Actualizar textos y estilos de una página existente con apoyo de una persona responsable', 'HTML y CSS básicos, lectura de instrucciones'],
  ['ejemplo-atencion', 'Apoyo en atención durante campaña', 'Ventas y atención', 'José Luis Bustamante y Rivero, Arequipa', 'S/ 70.00 · Por día', 'Por coordinar · 8 horas', 'Orientar a visitantes y apoyar en la organización de una campaña local', 'Comunicación clara y disponibilidad durante la jornada'],
  ['ejemplo-evento', 'Asistencia en montaje de un evento', 'Eventos', 'Cerro Colorado, Arequipa', 'S/ 75.00 · Por día', 'Por coordinar · 6 horas', 'Apoyar con la preparación de materiales y la organización del espacio antes de un evento', 'Puntualidad y capacidad para trabajar en equipo'],
  ['ejemplo-administracion', 'Registro de información para negocio', 'Administración', 'Paucarpata, Arequipa', 'S/ 60.00 · Por día', 'Por coordinar · 5 horas', 'Organizar comprobantes y registrar información proporcionada en una hoja de cálculo', 'Manejo básico de hojas de cálculo y atención al detalle'],
  ['ejemplo-tutoria', 'Apoyo en taller de herramientas digitales', 'Educación', 'Cercado, Arequipa', 'S/ 45.00 · Por hora', 'Por coordinar · 2 horas', 'Asistir a la persona responsable de un taller durante ejercicios de herramientas digitales', 'Conocimiento básico de herramientas ofimáticas y paciencia'],
  ['ejemplo-almacen', 'Auxiliar de almacén para organizar pedidos', 'Logística', 'Cerro Colorado, Arequipa', 'S/ 70.00 · Por día', 'Por coordinar · 6 horas', 'Apoyar en el conteo y orden de productos con la supervisión de la persona responsable', 'Orden, puntualidad y capacidad para seguir indicaciones'],
  ['ejemplo-reparto', 'Apoyo en preparación de pedidos', 'Logística', 'Paucarpata, Arequipa', 'S/ 65.00 · Por día', 'Por coordinar · 6 horas', 'Preparar y revisar pedidos junto al equipo, sin conducir ni realizar entregas', 'Atención al detalle y disposición para trabajar en equipo'],
  ['ejemplo-reclutamiento', 'Apoyo administrativo en selección de personal', 'Administración', 'Cercado, Arequipa', 'S/ 75.00 · Por día', 'Por coordinar · 6 horas', 'Coordinar horarios de entrevistas y organizar información siguiendo las indicaciones del responsable', 'Comunicación clara, manejo básico de hojas de cálculo y confidencialidad'],
  ['ejemplo-ventas', 'Asistente de ventas en tienda', 'Ventas y atención', 'Cayma, Arequipa', 'S/ 70.00 · Por día', 'Por coordinar · 8 horas', 'Orientar a clientes, ordenar productos y apoyar al personal de la tienda', 'Trato cordial y disponibilidad para la jornada'],
];
for (const [id, title, category, location, pay, duration, description, requirements] of examples) {
  if (known.has(id)) continue;
  await setDoc(doc(db, 'opportunities', id), { ownerId: employer.uid, ownerName: 'TuEmpuje · Ejemplo', title: `[Ejemplo] ${title}`, category, location, pay, duration, description, requirements, vacancies: 1, selectedCount: 0, status: 'published', createdAt: serverTimestamp() });
}
await signOut(auth);
const worker = await signInOrCreate(credentials.worker);
await ensureProfile(worker, 'María Torres · Ejemplo', { role: 'worker', interests: ['Creativo', 'Diseño y edición'], district: 'Cercado', skills: 'Edición básica de video, organización de archivos y diseño de piezas sencillas', experience: 'Proyectos personales y trabajos de curso con edición de clips cortos', availability: 'Tardes y fines de semana', education: 'Estudios en curso', portfolio: '' });
const application = doc(db, 'opportunities', 'ejemplo-edicion', 'applications', worker.uid);
const profile = (await getDoc(doc(db, 'users', worker.uid))).data();
const snapshot = { applicantName: profile.displayName, applicantSkills: profile.skills, applicantExperience: profile.experience || '', applicantAvailability: profile.availability, applicantDistrict: profile.district || '', applicantEducation: profile.education || '', applicantPortfolio: profile.portfolio || '' };
const currentApplication = await getDoc(application);
if (!currentApplication.exists()) {
  await setDoc(application, { applicantId: worker.uid, ...snapshot, status: 'pending', createdAt: serverTimestamp() });
} else if (currentApplication.data().status === 'pending' && !('applicantEducation' in currentApplication.data())) {
  await updateDoc(application, { status: 'withdrawn', withdrawnAt: serverTimestamp() });
  await updateDoc(application, { ...snapshot, status: 'pending', createdAt: serverTimestamp(), withdrawnAt: deleteField(), selectedAt: deleteField(), completedAt: deleteField() });
}
const message = doc(db, 'opportunities', 'ejemplo-edicion', 'applications', worker.uid, 'messages', 'presentacion');
if (!(await getDoc(message)).exists()) {
  await setDoc(message, { senderId: worker.uid, text: '[Ejemplo] Hola, tengo experiencia en proyectos de edición y puedo coordinar los detalles', createdAt: serverTimestamp() });
}
await signOut(auth);
await terminate(db);
await deleteApp(app);
console.log(`Se prepararon ${examples.length} oportunidades y una postulación de ejemplo en ${project}`);
console.log('Las cuentas de demostración se guardaron en infra/.env.demo.local, fuera de Git');
process.exit(0);
