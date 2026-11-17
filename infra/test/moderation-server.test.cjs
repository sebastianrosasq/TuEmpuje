const { test } = require('node:test');
const assert = require('node:assert/strict');
const { createModerationServer } = require('../moderation-server.cjs');

test('el panel exige sesión, origen local y registra decisiones permitidas', async () => {
  const token = 'a'.repeat(43); let resolution;
  const actions = {
    listOpenReports: async () => [{ id: 'caso', opportunityId: 'oportunidad', details: '<script>alert(1)</script>' }],
    listOpenDisputes: async () => [],
    resolveReport: async (_db, id, action, operator) => { resolution = { id, action, operator }; return 'kept'; },
  };
  const db = {
    collection: () => ({ doc: id => ({ id }) }),
    getAll: async (...refs) => refs.map(ref => ({ id: ref.id, exists: true, data: () => ({ title: 'Trabajo', description: 'Detalle' }) })),
  };
  const server = createModerationServer({ db, token, actions });
  await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
  const base = `http://127.0.0.1:${server.address().port}`;
  try {
    assert.equal((await fetch(`${base}/api/reports`)).status, 401);
    assert.equal((await fetch(`${base}/api/reports`, { headers: { Authorization: `Bearer ${token}`, Origin: 'https://ejemplo.invalid' } })).status, 403);
    const headers = { Authorization: `Bearer ${token}`, Origin: base, 'Content-Type': 'application/json' };
    const reports = await fetch(`${base}/api/reports` , { headers });
    assert.equal(reports.status, 200);
    assert.equal((await reports.json())[0].context.title, 'Trabajo');
    assert.equal((await fetch(`${base}/api/reports/caso/resolve`, { method: 'POST', headers: { ...headers, Origin: 'https://ejemplo.invalid' }, body: '{}' })).status, 403);
    assert.equal((await fetch(`${base}/api/reports/caso/resolve`, { method: 'POST', headers, body: JSON.stringify({ action: 'keep', operator: 'Equipo' }) })).status, 200);
    assert.deepEqual(resolution, { id: 'caso', action: 'keep', operator: 'Equipo' });
    const page = await fetch(base); assert.match(page.headers.get('content-security-policy'), /frame-ancestors 'none'/);
  } finally { await new Promise(resolve => server.close(resolve)); }
});
