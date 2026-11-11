import { readFile, writeFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';

const androidConfig = new URL('../android/app/google-services.json', import.meta.url);
const output = new URL('../android/firebase.local.properties', import.meta.url);
const packageName = 'pe.tuempuje.app';

try {
  const config = JSON.parse(await readFile(androidConfig, 'utf8'));
  const clients = (config.client ?? []).filter(
    (client) => client.client_info?.android_client_info?.package_name === packageName,
  );
  if (clients.length !== 1) throw new Error('La configuración debe contener exactamente un cliente para pe.tuempuje.app');

  const values = {
    projectId: config.project_info?.project_id,
    applicationId: clients[0].client_info?.mobilesdk_app_id,
    apiKey: clients[0].api_key?.[0]?.current_key,
  };
  if (Object.values(values).some((value) => typeof value !== 'string' || !value || /[\r\n\\]/.test(value))) {
    throw new Error('La configuración de Firebase está incompleta o contiene valores inválidos');
  }
  const properties = Object.entries(values).map(([key, value]) => `${key}=${value}`).join('\n') + '\n';
  await writeFile(output, properties, { flag: 'wx' });
  console.log('Configuración local de Android creada');
} catch (error) {
  if (error.code === 'EEXIST') {
    console.error(`Ya existe ${fileURLToPath(output)}, revísalo antes de generar otra configuración`);
  } else {
    console.error(`No se pudo preparar la configuración: ${error.message}`);
  }
  process.exitCode = 1;
}
