'use strict';
const token = location.hash.slice(1);
history.replaceState(null, '', '/');
const message = document.querySelector('#message');
const refresh = document.querySelector('#refresh');
const renderText = (tag, text) => { const node = document.createElement(tag); node.textContent = text || ''; return node; };
async function api(url, options = {}) {
  const response = await fetch(url, { ...options, headers: { Authorization: `Bearer ${token}`, ...(options.headers || {}) } });
  const body = await response.json();
  if (!response.ok) throw new Error(body.error || 'No se pudo completar la solicitud');
  return body;
}
function card(item, type) {
  const box = document.createElement('article'); box.className = 'card';
  box.append(renderText('h3', type === 'reports' ? (item.reason || 'Reporte de oportunidad') : 'Reporte de referencia'));
  box.append(renderText('p', item.details || 'Sin detalles'));
  if (item.context) {
    box.append(renderText('h4', item.context.title || (type === 'reports' ? 'Oportunidad' : 'Referencia')));
    if (item.context.description) box.append(renderText('p', item.context.description));
    if (item.context.location) box.append(renderText('p', `Ubicación: ${item.context.location}`));
    if (item.context.status) box.append(renderText('p', `Estado de la publicación: ${item.context.status}`));
  } else box.append(renderText('p', 'El contenido reportado ya no está disponible'));
  const inputId = `operator-${type}-${item.id}`;
  const label = renderText('label', 'Nombre de quien revisa'); label.htmlFor = inputId;
  const operator = document.createElement('input'); operator.id = inputId; operator.maxLength = 100; operator.autocomplete = 'off';
  box.append(label, operator);
  const row = document.createElement('div'); row.className = 'actions';
  for (const [action, title] of (type === 'reports' ? [['keep','Mantener oportunidad'],['remove','Retirar oportunidad']] : [['keep','Mantener referencia'],['hide','Ocultar referencia']])) {
    const button = renderText('button', title); button.className = action === 'keep' ? 'secondary' : '';
    button.addEventListener('click', async () => {
      if (!operator.value.trim()) { message.textContent = 'Escribe el nombre de quien revisa'; operator.focus(); return; }
      if (!window.confirm(`¿Confirmas la decisión: ${title.toLowerCase()}?`)) return;
      button.disabled = true;
      try {
        await api(`/api/${type}/${encodeURIComponent(item.id)}/resolve`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ action, operator: operator.value.trim() }) });
        message.textContent = 'Decisión registrada'; await load();
      } catch (error) { message.textContent = error.message; button.disabled = false; }
    });
    row.append(button);
  }
  box.append(row); return box;
}
async function load() {
  refresh.disabled = true;
  try {
    const [reports, disputes] = await Promise.all([api('/api/reports'), api('/api/disputes')]);
    for (const [type, items] of [['reports', reports], ['disputes', disputes]]) {
      const root = document.querySelector(`#${type}`); root.replaceChildren();
      if (!items.length) { const empty = renderText('p', 'No hay casos pendientes'); empty.className = 'empty'; root.append(empty); }
      else items.forEach(item => root.append(card(item, type)));
    }
  } catch (error) { message.textContent = error.message; }
  finally { refresh.disabled = false; }
}
refresh.addEventListener('click', load);
if (token) load(); else message.textContent = 'Abre el enlace de sesión mostrado en la consola';
