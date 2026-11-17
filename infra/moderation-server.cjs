const http = require('node:http');
const crypto = require('node:crypto');
const fs = require('node:fs');
const path = require('node:path');
const moderation = require('./functions/moderation.cjs');

const assets = {
  '/': ['index.html', 'text/html; charset=utf-8'],
  '/app.js': ['app.js', 'text/javascript; charset=utf-8'],
  '/style.css': ['style.css', 'text/css; charset=utf-8'],
};

async function addCaseContext(db, items, type) {
  const collection = type === 'reports' ? 'opportunities' : 'workReferences';
  const field = type === 'reports' ? 'opportunityId' : 'referenceId';
  const ids = [...new Set(items.map(item => item[field]).filter(id => typeof id === 'string' && id.length > 0 && !id.includes('/')))];
  if (!ids.length) return items;
  const snapshots = await db.getAll(...ids.map(id => db.collection(collection).doc(id)));
  const context = new Map(snapshots.filter(doc => doc.exists).map(doc => {
    const data = doc.data();
    const clean = key => typeof data[key] === 'string' ? data[key].slice(0, 3000) : '';
    return [doc.id, type === 'reports'
      ? { title: clean('title'), description: clean('description'), location: clean('location'), status: clean('status') }
      : { title: clean('title'), description: clean('text') }];
  }));
  return items.map(item => ({ ...item, context: context.get(item[field]) || null }));
}

function createModerationServer({ db, token, actions = moderation }) {
  if (!db || typeof token !== 'string' || token.length < 32) throw new Error('Configuración incompleta');
  const server = http.createServer(async (req, res) => {
    const port = server.address()?.port;
    const host = `127.0.0.1:${port}`;
    const common = {
      'Cache-Control': 'no-store',
      'X-Content-Type-Options': 'nosniff',
      'X-Frame-Options': 'DENY',
      'Referrer-Policy': 'no-referrer',
      'Content-Security-Policy': "default-src 'none'; script-src 'self'; style-src 'self'; connect-src 'self'; base-uri 'none'; frame-ancestors 'none'; form-action 'none'",
    };
    function reply(code, body, type = 'application/json; charset=utf-8') {
      res.writeHead(code, { ...common, 'Content-Type': type });
      res.end(typeof body === 'string' ? body : JSON.stringify(body));
    }
    if (req.headers.host !== host || (req.headers.origin && req.headers.origin !== `http://${host}`)) {
      return reply(403, { error: 'Acceso no permitido' });
    }
    const url = new URL(req.url, `http://${host}`);
    if (req.method === 'GET' && assets[url.pathname]) {
      const [file, type] = assets[url.pathname];
      return reply(200, fs.readFileSync(path.join(__dirname, 'moderation-ui', file), 'utf8'), type);
    }
    const supplied = req.headers.authorization || '';
    const expected = `Bearer ${token}`;
    if (Buffer.byteLength(supplied) !== Buffer.byteLength(expected) ||
        !crypto.timingSafeEqual(Buffer.from(supplied), Buffer.from(expected))) {
      return reply(401, { error: 'Sesión no autorizada' });
    }
    try {
      if (req.method === 'GET' && url.pathname === '/api/reports') return reply(200, await addCaseContext(db, await actions.listOpenReports(db), 'reports'));
      if (req.method === 'GET' && url.pathname === '/api/disputes') return reply(200, await addCaseContext(db, await actions.listOpenDisputes(db), 'disputes'));
      const match = url.pathname.match(/^\/api\/(reports|disputes)\/([^/]+)\/resolve$/);
      if (req.method === 'POST' && match) {
        if (req.headers.origin !== `http://${host}` || !String(req.headers['content-type'] || '').startsWith('application/json')) {
          return reply(403, { error: 'Solicitud no permitida' });
        }
        let raw = '';
        for await (const chunk of req) {
          raw += chunk;
          if (Buffer.byteLength(raw) > 4096) return reply(413, { error: 'Solicitud demasiado grande' });
        }
        let body;
        try { body = JSON.parse(raw); } catch { return reply(400, { error: 'Solicitud inválida' }); }
        const id = decodeURIComponent(match[2]);
        if (!id || id.includes('/') || !body || typeof body.operator !== 'string' || typeof body.action !== 'string') {
          return reply(400, { error: 'Datos incompletos' });
        }
        const result = match[1] === 'reports'
          ? await actions.resolveReport(db, id, body.action, body.operator)
          : await actions.resolveDispute(db, id, body.action, body.operator);
        return reply(200, { result });
      }
      return reply(404, { error: 'No encontrado' });
    } catch (error) {
      console.error('No se pudo completar la operación de moderación:', error.message);
      return reply(400, { error: 'No se pudo completar la operación. Verifica el estado y vuelve a intentar' });
    }
  });
  return server;
}

async function main() {
  const args = process.argv.slice(2);
  const index = args.indexOf('--project');
  const projectId = index >= 0 ? args[index + 1] : '';
  if (!projectId || projectId.startsWith('-')) throw new Error('Indica el proyecto con --project');
  const fromFunctions = require('node:module').createRequire(path.join(__dirname, 'functions', 'package.json'));
  const { initializeApp, applicationDefault } = fromFunctions('firebase-admin/app');
  const { getFirestore } = fromFunctions('firebase-admin/firestore');
  const options = { projectId };
  if (!process.env.FIRESTORE_EMULATOR_HOST) options.credential = applicationDefault();
  initializeApp(options);
  const token = crypto.randomBytes(32).toString('base64url');
  const server = createModerationServer({ db: getFirestore(), token });
  server.listen(0, '127.0.0.1', () => {
    console.log(`Panel local: http://127.0.0.1:${server.address().port}/#${token}`);
    console.log('El enlace concede acceso a esta sesión. No lo compartas. Cierra la consola para terminar.');
  });
}
if (require.main === module) main().catch(error => { console.error(error.message); process.exitCode = 1; });
module.exports = { createModerationServer };
