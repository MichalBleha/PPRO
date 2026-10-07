// API klient pro komunikaci se Spring Boot REST API
const BASE_URL = '/api';

async function request(endpoint, options = {}) {
  const config = {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  };

  const response = await fetch(`${BASE_URL}${endpoint}`, config);

  if (!response.ok) {
    let errorData;
    try {
      errorData = await response.json();
    } catch {
      errorData = { message: `Chyba serveru (${response.status} ${response.statusText})` };
    }
    const err = new Error(errorData.message || 'Nastala neočekávaná chyba.');
    err.status = response.status;
    err.details = errorData;
    throw err;
  }

  // Pro 204 No Content nebo prázdné tělo
  if (response.status === 204) return null;
  return response.json();
}

export const api = {
  // Pacienti
  getPacienti: (dotaz) => request(`/pacienti${dotaz ? `?dotaz=${encodeURIComponent(dotaz)}` : ''}`),
  getPacient: (id) => request(`/pacienti/${id}`),
  createPacient: (data) => request('/pacienti', { method: 'POST', body: JSON.stringify(data) }),
  updatePacient: (id, data) => request(`/pacienti/${id}`, { method: 'PUT', body: JSON.stringify(data) }),

  // Lékaři
  getLekari: () => request('/lekari'),
  getLekar: (id) => request(`/lekari/${id}`),

  // Sloty diáře (15minutová mřížka)
  getSloty: (lekarId, datum) => request(`/sloty?lekarId=${lekarId}&datum=${datum}`),

  // Rezervace
  getRezervaceLekare: (lekarId, od, doCas) => request(`/rezervace?lekarId=${lekarId}&od=${od}&doCas=${doCas}`),
  getHistoriePacienta: (pacientId) => request(`/rezervace/pacient/${pacientId}`),
  getFrontaPreobjednani: () => request('/rezervace/fronta-preobjednani'),
  createRezervace: (data) => request('/rezervace', { method: 'POST', body: JSON.stringify(data) }),
  cancelRezervace: (id) => request(`/rezervace/${id}`, { method: 'DELETE' }),
  preobjednejRezervaci: (id, data) => request(`/rezervace/${id}/preobjednat`, { method: 'POST', body: JSON.stringify(data) }),

  // Výluky lékaře
  createVyluka: (data) => request('/vyluky', { method: 'POST', body: JSON.stringify(data) }),

  // Návštěvy a výkony
  createNavsteva: (data) => request('/navstevy', { method: 'POST', body: JSON.stringify(data) }),
  getNavstevyPacienta: (pacientId) => request(`/navstevy/pacient/${pacientId}`),
  getVykony: () => request('/vykony'),

  // Měsíční výkazy
  getMesicniVykaz: (rok, mesic, lekarId) => {
    let url = `/vykazy/mesicni?rok=${rok}&mesic=${mesic}`;
    if (lekarId) url += `&lekarId=${lekarId}`;
    return request(url);
  }
};
