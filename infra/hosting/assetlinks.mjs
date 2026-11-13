import { mkdir, writeFile } from 'node:fs/promises';

const fingerprint = process.argv[2]?.trim().toUpperCase();
if (!fingerprint || !/^([0-9A-F]{2}:){31}[0-9A-F]{2}$/.test(fingerprint)) {
  console.error('Indica la huella SHA-256 del certificado Android, separada por dos puntos');
  process.exit(1);
}
const directory = new URL('./public/.well-known/', import.meta.url);
await mkdir(directory, { recursive: true });
const association = [{
  relation: ['delegate_permission/common.handle_all_urls'],
  target: { namespace: 'android_app', package_name: 'pe.tuempuje.app', sha256_cert_fingerprints: [fingerprint] },
}];
await writeFile(new URL('assetlinks.json', directory), JSON.stringify(association, null, 2) + '\n');
console.log('Asociación Android preparada');
