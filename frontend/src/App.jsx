import React, { useState, useEffect } from 'react';
import { api } from './api/client.js';

export default function App() {
  const [activeTab, setActiveTab] = useState('diar');
  const [doctors, setDoctors] = useState([]);
  const [selectedDoctorId, setSelectedDoctorId] = useState('');
  const [selectedDate, setSelectedDate] = useState(() => {
    const today = new Date();
    // Default na pondělí v týdnu pro ordinační hodiny nebo dnešek
    return today.toISOString().split('T')[0];
  });
  
  // Data
  const [slots, setSlots] = useState([]);
  const [patients, setPatients] = useState([]);
  const [patientSearch, setPatientSearch] = useState('');
  const [procedures, setProcedures] = useState([]);
  const [rescheduleQueue, setRescheduleQueue] = useState([]);
  const [monthlyReport, setMonthlyReport] = useState(null);
  
  // UI stavy
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState(null);

  // Modaly
  const [showNewReservationModal, setShowNewReservationModal] = useState(false);
  const [selectedSlotForReservation, setSelectedSlotForReservation] = useState(null);
  const [newResPatientId, setNewResPatientId] = useState('');
  const [newResDurationMinutes, setNewResDurationMinutes] = useState(15);
  const [newResNote, setNewResNote] = useState('');

  const [showReservationDetailModal, setShowReservationDetailModal] = useState(false);
  const [selectedReservation, setSelectedReservation] = useState(null);

  const [showNewPatientModal, setShowNewPatientModal] = useState(false);
  const [newPatientData, setNewPatientData] = useState({
    jmeno: '', prijmeni: '', rokNarozeni: 1985, telefon: '+420', cisloPojistence: '', kodPojistovny: '111'
  });

  const [showVisitModal, setShowVisitModal] = useState(false);
  const [visitData, setVisitData] = useState({
    pacientId: '', lekarId: '', rezervaceId: null, zpravaLekare: '',
    selectedProcedures: [] // { vykonId, pocet, kodVykonu, nazev, body }
  });

  const [showAbsenceModal, setShowAbsenceModal] = useState(false);
  const [absenceData, setAbsenceData] = useState({
    lekarId: '', datumOd: '', datumDo: '', duvod: 'Nemoc / Odborné školení'
  });

  const showToast = (message, type = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 4000);
  };

  // Načtení lékařů a číselníku výkonů při startu
  useEffect(() => {
    loadDoctors();
    loadProcedures();
    loadRescheduleQueue();
  }, []);

  const loadDoctors = async () => {
    try {
      const data = await api.getLekari();
      setDoctors(data);
      if (data.length > 0 && !selectedDoctorId) {
        setSelectedDoctorId(String(data[0].id));
      }
    } catch (err) {
      console.warn('Backend nepřipojen, načítám demo lékaře:', err);
      // Fiktivní data pro případ startu před DB
      const mockDocs = [
        { id: 1, titul: 'MUDr.', jmeno: 'Jana', prijmeni: 'Hrubá', celeJmeno: 'MUDr. Jana Hrubá', aktivni: true },
        { id: 2, titul: 'MUDr.', jmeno: 'Tomáš', prijmeni: 'Dvořák', celeJmeno: 'MUDr. Tomáš Dvořák', aktivni: true },
        { id: 3, titul: 'MUDr.', jmeno: 'Eva', prijmeni: 'Novotná', celeJmeno: 'MUDr. Eva Novotná', aktivni: true }
      ];
      setDoctors(mockDocs);
      setSelectedDoctorId('1');
    }
  };

  const loadProcedures = async () => {
    try {
      const data = await api.getVykony();
      setProcedures(data);
    } catch (err) {
      setProcedures([
        { id: 1, kodVykonu: '09511', nazev: 'Komplexní vyšetření praktickým lékařem', body: 250 },
        { id: 2, kodVykonu: '09513', nazev: 'Cílené vyšetření praktickým lékařem', body: 150 },
        { id: 3, kodVykonu: '09523', nazev: 'Kontrolní klinické vyšetření', body: 100 },
        { id: 4, kodVykonu: '09115', nazev: 'Odběr krve ze žíly', body: 45 },
        { id: 5, kodVykonu: '09127', nazev: 'Injekční aplikace do svalu', body: 35 },
        { id: 6, kodVykonu: '09220', nazev: 'EKG vyšetření včetně popisu', body: 180 }
      ]);
    }
  };

  const loadRescheduleQueue = async () => {
    try {
      const data = await api.getFrontaPreobjednani();
      setRescheduleQueue(data);
    } catch {
      setRescheduleQueue([]);
    }
  };

  // Načtení slotů při změně lékaře nebo data
  useEffect(() => {
    if (selectedDoctorId && selectedDate) {
      loadSlots(selectedDoctorId, selectedDate);
    }
  }, [selectedDoctorId, selectedDate]);

  const loadSlots = async (docId, date) => {
    setLoading(true);
    try {
      const data = await api.getSloty(docId, date);
      setSlots(data);
    } catch (err) {
      // Demo sloty pro zobrazení mřížky
      const demoSlots = [];
      for (let h = 8; h < 13; h++) {
        for (let m = 0; m < 60; m += 15) {
          const timeStr = `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
          const isReserved = (h === 8 && m === 30) || (h === 10 && m === 15);
          demoSlots.push({
            casOd: `${date}T${timeStr}:00+02:00`,
            casDo: `${date}T${timeStr}:00+02:00`,
            volno: !isReserved,
            duvodObsazeni: isReserved ? 'REZERVOVANO' : null,
            rezervaceId: isReserved ? (h * 10 + m) : null,
            pacientCeleJmeno: isReserved ? 'Jan Novák' : null,
            poznamka: isReserved ? 'Pravidelná kontrola tlaku' : null
          });
        }
      }
      setSlots(demoSlots);
    } finally {
      setLoading(false);
    }
  };

  // Načtení pacientů při přepnutí na kartotéku nebo hledání
  useEffect(() => {
    if (activeTab === 'pacienti' || showNewReservationModal) {
      searchPatients(patientSearch);
    }
  }, [activeTab, patientSearch, showNewReservationModal]);

  const searchPatients = async (query) => {
    try {
      const data = await api.getPacienti(query);
      setPatients(data);
    } catch {
      setPatients([
        { id: 1, jmeno: 'Jan', prijmeni: 'Novák', rokNarozeni: 1980, telefon: '+420777111222', cisloPojistence: '8005121234', kodPojistovny: '111', pocetZrusenychRezervaci: 0 },
        { id: 2, jmeno: 'Marie', prijmeni: 'Svobodová', rokNarozeni: 1975, telefon: '+420777333444', cisloPojistence: '7558204321', kodPojistovny: '205', pocetZrusenychRezervaci: 3 },
        { id: 3, jmeno: 'Petr', prijmeni: 'Černý', rokNarozeni: 1992, telefon: '+420777555666', cisloPojistence: '9203159876', kodPojistovny: '111', pocetZrusenychRezervaci: 1 }
      ]);
    }
  };

  // Kliknutí na slot
  const handleSlotClick = (slot) => {
    if (slot.volno) {
      setSelectedSlotForReservation(slot);
      setNewResDurationMinutes(15);
      setNewResNote('');
      setShowNewReservationModal(true);
    } else if (slot.rezervaceId) {
      setSelectedReservation(slot);
      setShowReservationDetailModal(true);
    }
  };

  // Vytvoření rezervace
  const handleCreateReservation = async (e) => {
    e.preventDefault();
    if (!newResPatientId) {
      showToast('Vyberte pacienta pro rezervaci', 'error');
      return;
    }
    try {
      const slotStart = new Date(selectedSlotForReservation.casOd);
      const slotEnd = new Date(slotStart.getTime() + newResDurationMinutes * 60000);

      await api.createRezervace({
        pacientId: Number(newResPatientId),
        lekarId: Number(selectedDoctorId),
        casOd: slotStart.toISOString(),
        casDo: slotEnd.toISOString(),
        poznamka: newResNote
      });

      showToast('Rezervace byla úspěšně vytvořena');
      setShowNewReservationModal(false);
      loadSlots(selectedDoctorId, selectedDate);
    } catch (err) {
      showToast(err.message || 'Chyba při zakládání rezervace', 'error');
    }
  };

  // Stornování rezervace
  const handleCancelReservation = async () => {
    if (!selectedReservation?.rezervaceId) return;
    if (!window.confirm('Opravdu si přejete rezervaci stornovat? Slot bude okamžitě uvolněn pro další pacienty.')) return;

    try {
      await api.cancelRezervace(selectedReservation.rezervaceId);
      showToast('Rezervace byla stornována a slot uvolněn.');
      setShowReservationDetailModal(false);
      loadSlots(selectedDoctorId, selectedDate);
    } catch (err) {
      showToast(err.message || 'Chyba při rušení rezervace', 'error');
    }
  };

  // Založení nového pacienta
  const handleCreatePatient = async (e) => {
    e.preventDefault();
    try {
      const novy = await api.createPacient(newPatientData);
      showToast(`Pacient ${novy.prijmeni} byl úspěšně zaevidován.`);
      setShowNewPatientModal(false);
      setNewResPatientId(String(novy.id));
      searchPatients('');
    } catch (err) {
      showToast(err.message || 'Chyba při ukládání pacienta', 'error');
    }
  };

  // Uložení návštěvy a vykázaných výkonů
  const handleSaveVisit = async (e) => {
    e.preventDefault();
    try {
      const vykonyReq = visitData.selectedProcedures.map(p => ({
        vykonId: p.vykonId,
        pocet: p.pocet
      }));

      await api.createNavsteva({
        pacientId: Number(visitData.pacientId),
        lekarId: Number(visitData.lekarId),
        rezervaceId: visitData.rezervaceId,
        zacatekNavstevy: new Date().toISOString(),
        konecNavstevy: new Date().toISOString(),
        zpravaLekare: visitData.zpravaLekare,
        vykony: vykonyReq
      });

      showToast('Návštěva a vykázané výkony byly uloženy.');
      setShowVisitModal(false);
      loadSlots(selectedDoctorId, selectedDate);
    } catch (err) {
      showToast(err.message || 'Chyba při ukládání návštěvy', 'error');
    }
  };

  // Zadání výluky lékaře
  const handleSaveAbsence = async (e) => {
    e.preventDefault();
    try {
      const res = await api.createVyluka({
        lekarId: Number(absenceData.lekarId || selectedDoctorId),
        datumOd: new Date(absenceData.datumOd).toISOString(),
        datumDo: new Date(absenceData.datumDo).toISOString(),
        duvod: absenceData.duvod
      });
      showToast(`Výluka zaevidována. Dotčeno ${res.pocetDotcenychRezervaci} rezervací k přeobjednání.`);
      setShowAbsenceModal(false);
      loadSlots(selectedDoctorId, selectedDate);
      loadRescheduleQueue();
    } catch (err) {
      showToast(err.message || 'Chyba při zadávání výluky', 'error');
    }
  };

  // Načtení měsíčního výkazu
  const loadReport = async () => {
    const now = new Date();
    try {
      const data = await api.getMesicniVykaz(now.getFullYear(), now.getMonth() + 1, selectedDoctorId || null);
      setMonthlyReport(data);
    } catch {
      setMonthlyReport({
        rok: now.getFullYear(),
        mesic: now.getMonth() + 1,
        lekarCeleJmeno: doctors.find(d => String(d.id) === selectedDoctorId)?.celeJmeno || 'MUDr. Jana Hrubá',
        celkemNavstev: 142,
        celkemBodu: 38450,
        vykony: [
          { kodVykonu: '09511', nazevVykonu: 'Komplexní vyšetření praktickým lékařem', celkemProvedeno: 65, bodyZaJednotku: 250, celkemBodu: 16250 },
          { kodVykonu: '09513', nazevVykonu: 'Cílené vyšetření praktickým lékařem', celkemProvedeno: 48, bodyZaJednotku: 150, celkemBodu: 7200 },
          { kodVykonu: '09115', nazevVykonu: 'Odběr krve ze žíly', celkemProvedeno: 50, bodyZaJednotku: 45, celkemBodu: 2250 },
          { kodVykonu: '09220', nazevVykonu: 'EKG vyšetření včetně popisu', celkemProvedeno: 30, bodyZaJednotku: 180, celkemBodu: 5400 }
        ]
      });
    }
  };

  useEffect(() => {
    if (activeTab === 'vykazy') {
      loadReport();
    }
  }, [activeTab, selectedDoctorId]);

  return (
    <div className="app-layout">
      {/* Postranní panel navigace recepce */}
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-icon">✚</div>
          <div className="brand-text">
            <h1>Ordinace Na Vyhlídce</h1>
            <p>Recepční systém</p>
          </div>
        </div>

        <nav className="nav-menu">
          <button
            className={`nav-item ${activeTab === 'diar' ? 'active' : ''}`}
            onClick={() => setActiveTab('diar')}
          >
            <span>📅</span> Diář ordinace
          </button>
          <button
            className={`nav-item ${activeTab === 'pacienti' ? 'active' : ''}`}
            onClick={() => setActiveTab('pacienti')}
          >
            <span>👥</span> Kartotéka pacientů
          </button>
          <button
            className={`nav-item ${activeTab === 'navsteva' ? 'active' : ''}`}
            onClick={() => {
              setActiveTab('navsteva');
              if (patients.length > 0) {
                setVisitData(prev => ({
                  ...prev,
                  pacientId: String(patients[0].id),
                  lekarId: selectedDoctorId || String(doctors[0]?.id)
                }));
              }
            }}
          >
            <span>📋</span> Záznam návštěvy & výkony
          </button>
          <button
            className={`nav-item ${activeTab === 'preobjednani' ? 'active' : ''}`}
            onClick={() => setActiveTab('preobjednani')}
          >
            <span>⚠️</span> Fronta k přeobjednání
            {rescheduleQueue.length > 0 && (
              <span className="badge-count">{rescheduleQueue.length}</span>
            )}
          </button>
          <button
            className={`nav-item ${activeTab === 'vykazy' ? 'active' : ''}`}
            onClick={() => setActiveTab('vykazy')}
          >
            <span>📊</span> Měsíční výkaz pojišťovny
          </button>
        </nav>

        <div className="sidebar-footer">
          <p><strong>Recepce:</strong> pí Věra</p>
          <p>Mřížka: 15 minut</p>
          <p style={{ marginTop: '0.5rem', color: '#64748b' }}>PPRO Semestrálka (Java/React)</p>
        </div>
      </aside>

      {/* Hlavní obsah */}
      <main className="main-wrapper">
        <header className="top-navbar">
          <h2 className="page-title">
            {activeTab === 'diar' && 'Diář lékaře – Týdenní a denní přehled'}
            {activeTab === 'pacienti' && 'Kartotéka a rychlé hledání pacienta'}
            {activeTab === 'navsteva' && 'Zaevidování návštěvy a vykázaných výkonů'}
            {activeTab === 'preobjednani' && 'Fronta pacientů k přeobjednání (výluky lékařů)'}
            {activeTab === 'vykazy' && 'Měsíční výkaz bodů pro zdravotní pojišťovny'}
          </h2>

          <div className="user-status">
            <span className="reception-badge">
              <span className="reception-indicator"></span>
              Ordinace otevřena
            </span>
          </div>
        </header>

        <div className="content-body">
          {/* ================= POHLED: DIÁŘ ================= */}
          {activeTab === 'diar' && (
            <div>
              <div className="calendar-controls">
                <div className="filter-group">
                  <span className="filter-label">Lékař:</span>
                  <select
                    className="select-input"
                    value={selectedDoctorId}
                    onChange={(e) => setSelectedDoctorId(e.target.value)}
                  >
                    {doctors.map(d => (
                      <option key={d.id} value={d.id}>{d.celeJmeno}</option>
                    ))}
                  </select>
                </div>

                <div className="filter-group">
                  <span className="filter-label">Datum:</span>
                  <input
                    type="date"
                    className="date-input"
                    value={selectedDate}
                    onChange={(e) => setSelectedDate(e.target.value)}
                  />
                </div>

                <div className="filter-group" style={{ marginLeft: 'auto', gap: '0.5rem' }}>
                  <button
                    className="btn btn-secondary btn-sm"
                    onClick={() => {
                      setAbsenceData({
                        lekarId: selectedDoctorId,
                        datumOd: `${selectedDate}T08:00`,
                        datumDo: `${selectedDate}T16:00`,
                        duvod: 'Nemoc lékaře'
                      });
                      setShowAbsenceModal(true);
                    }}
                  >
                    ⚡ Zadat výluku lékaře
                  </button>
                  <button
                    className="btn btn-primary btn-sm"
                    onClick={() => {
                      if (slots.length > 0) {
                        const firstAvail = slots.find(s => s.volno);
                        if (firstAvail) {
                          handleSlotClick(firstAvail);
                        } else {
                          showToast('V tomto dni není volný žádný slot.', 'error');
                        }
                      }
                    }}
                  >
                    + Rychlá rezervace
                  </button>
                </div>
              </div>

              <div className="card">
                <div className="card-header">
                  <h2>15minutová mřížka ordinačních slotů ({selectedDate})</h2>
                  <div style={{ display: 'flex', gap: '0.75rem', fontSize: '0.8rem' }}>
                    <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                      <span style={{ width: 10, height: 10, borderRadius: 2, background: 'var(--success)' }}></span> Volno
                    </span>
                    <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                      <span style={{ width: 10, height: 10, borderRadius: 2, background: 'var(--primary)' }}></span> Rezervováno
                    </span>
                    <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                      <span style={{ width: 10, height: 10, borderRadius: 2, background: 'var(--warning)' }}></span> Výluka / Akutní
                    </span>
                  </div>
                </div>

                {loading ? (
                  <p style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>Načítám sloty diáře...</p>
                ) : slots.length === 0 ? (
                  <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
                    <p style={{ fontSize: '1.2rem', fontWeight: 600 }}>Žádné ordinační hodiny pro tento den</p>
                    <p style={{ fontSize: '0.9rem', marginTop: '0.5rem' }}>Lékař v tento den neordinuje nebo má celodenní nepřítomnost.</p>
                  </div>
                ) : (
                  <div className="slot-grid">
                    {slots.map((slot, idx) => {
                      const timeFrom = slot.casOd.substring(11, 16);
                      const timeTo = slot.casDo.substring(11, 16);
                      let cardClass = 'available';
                      let badgeClass = 'badge-available';
                      let statusText = 'Volno';

                      if (!slot.volno) {
                        if (slot.duvodObsazeni === 'REZERVOVANO') {
                          cardClass = 'reserved';
                          badgeClass = 'badge-reserved';
                          statusText = 'Obsazeno';
                        } else if (slot.duvodObsazeni === 'VYLUKA') {
                          cardClass = 'absence';
                          badgeClass = 'badge-absence';
                          statusText = 'Výluka';
                        } else {
                          cardClass = 'blocked';
                          badgeClass = 'badge-blocked';
                          statusText = 'Nelze objednat';
                        }
                      }

                      return (
                        <div
                          key={idx}
                          className={`slot-card ${cardClass}`}
                          onClick={() => handleSlotClick(slot)}
                        >
                          <div className="slot-time">
                            <span>{timeFrom} - {timeTo}</span>
                            <span className={`slot-status-badge ${badgeClass}`}>{statusText}</span>
                          </div>
                          {slot.pacientCeleJmeno ? (
                            <div>
                              <div className="slot-patient">{slot.pacientCeleJmeno}</div>
                              {slot.poznamka && <div className="slot-note">{slot.poznamka}</div>}
                            </div>
                          ) : (
                            <div className="slot-note" style={{ color: 'var(--text-light)' }}>
                              {slot.volno ? 'Klikněte pro objednání' : (slot.poznamka || 'Mimo běžný režim')}
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            </div>
          )}

          {/* ================= POHLED: KARTOTÉKA PACIENTŮ ================= */}
          {activeTab === 'pacienti' && (
            <div className="card">
              <div className="card-header">
                <h2>Kartotéka pacientů</h2>
                <button className="btn btn-primary" onClick={() => setShowNewPatientModal(true)}>
                  + Registrovat pacienta
                </button>
              </div>

              <div className="search-bar">
                <input
                  type="text"
                  className="search-input"
                  placeholder="🔍 Bleskové hledání při hovoru: zadejte příjmení, telefon nebo číslo pojištěnce..."
                  value={patientSearch}
                  onChange={(e) => setPatientSearch(e.target.value)}
                  autoFocus
                />
              </div>

              <div className="table-container">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Příjmení a jméno</th>
                      <th>Telefon</th>
                      <th>Číslo pojištěnce (RČ)</th>
                      <th>Pojišťovna</th>
                      <th>Rok nar.</th>
                      <th>Spolehlivost</th>
                      <th>Akce</th>
                    </tr>
                  </thead>
                  <tbody>
                    {patients.map(p => (
                      <tr key={p.id}>
                        <td><strong>{p.prijmeni} {p.jmeno}</strong></td>
                        <td>{p.telefon}</td>
                        <td><code>{p.cisloPojistence}</code></td>
                        <td><span style={{ fontWeight: 600 }}>{p.kodPojistovny}</span></td>
                        <td>{p.rokNarozeni}</td>
                        <td>
                          {p.pocetZrusenychRezervaci > 0 ? (
                            <span className="badge-unreliable" title="Historie zrušených termínů">
                              ⚠️ {p.pocetZrusenychRezervaci}x storno
                            </span>
                          ) : (
                            <span className="badge-reliable">Spolehlivý</span>
                          )}
                        </td>
                        <td>
                          <button
                            className="btn btn-secondary btn-sm"
                            onClick={() => {
                              setNewResPatientId(String(p.id));
                              setActiveTab('diar');
                              showToast(`Vybrán pacient ${p.prijmeni}. Zvolte termín v diáři.`);
                            }}
                          >
                            Objednat
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* ================= POHLED: ZÁZNAM NÁVŠTĚVY A VÝKONŮ ================= */}
          {activeTab === 'navsteva' && (
            <div className="card">
              <div className="card-header">
                <h2>Zaevidování uskutečněné návštěvy a vykázaných výkonů</h2>
              </div>

              <form onSubmit={handleSaveVisit}>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem', marginBottom: '1.5rem' }}>
                  <div className="form-group">
                    <label className="form-label">Pacient:</label>
                    <select
                      className="select-input"
                      style={{ width: '100%' }}
                      value={visitData.pacientId}
                      onChange={(e) => setVisitData({ ...visitData, pacientId: e.target.value })}
                      required
                    >
                      <option value="">-- Vyberte pacienta --</option>
                      {patients.map(p => (
                        <option key={p.id} value={p.id}>{p.prijmeni} {p.jmeno} ({p.cisloPojistence})</option>
                      ))}
                    </select>
                  </div>

                  <div className="form-group">
                    <label className="form-label">Ošetřující lékař:</label>
                    <select
                      className="select-input"
                      style={{ width: '100%' }}
                      value={visitData.lekarId}
                      onChange={(e) => setVisitData({ ...visitData, lekarId: e.target.value })}
                      required
                    >
                      {doctors.map(d => (
                        <option key={d.id} value={d.id}>{d.celeJmeno}</option>
                      ))}
                    </select>
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label">Lékařská zpráva / Anamnéza / Závěr:</label>
                  <textarea
                    className="text-input"
                    style={{ width: '100%', minHeight: '90px' }}
                    placeholder="Popis vyšetření, naměřené hodnoty (tlak, teplota), medikace..."
                    value={visitData.zpravaLekare}
                    onChange={(e) => setVisitData({ ...visitData, zpravaLekare: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
                    <label className="form-label" style={{ margin: 0 }}>Vykázané lékařské výkony (M:N asociace):</label>
                    <div style={{ fontSize: '0.95rem', fontWeight: 700, color: 'var(--primary)' }}>
                      Celkem bodů za návštěvu: {
                        visitData.selectedProcedures.reduce((sum, item) => sum + (item.body * item.pocet), 0)
                      } bodů
                    </div>
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '0.75rem' }}>
                    {procedures.map(proc => {
                      const existing = visitData.selectedProcedures.find(p => p.vykonId === proc.id);
                      const isSelected = !!existing;
                      const count = existing ? existing.pocet : 0;

                      return (
                        <div key={proc.id} className={`procedure-item ${isSelected ? 'selected' : ''}`}>
                          <div>
                            <strong>{proc.kodVykonu}</strong> – {proc.nazev}
                            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{proc.body} bodů / ks</div>
                          </div>

                          <div className="stepper">
                            <button
                              type="button"
                              className="stepper-btn"
                              onClick={() => {
                                if (count <= 1) {
                                  setVisitData({
                                    ...visitData,
                                    selectedProcedures: visitData.selectedProcedures.filter(p => p.vykonId !== proc.id)
                                  });
                                } else {
                                  setVisitData({
                                    ...visitData,
                                    selectedProcedures: visitData.selectedProcedures.map(p =>
                                      p.vykonId === proc.id ? { ...p, pocet: p.pocet - 1 } : p
                                    )
                                  });
                                }
                              }}
                            >
                              -
                            </button>
                            <span style={{ minWidth: 20, textAlign: 'center', fontWeight: 700 }}>{count}</span>
                            <button
                              type="button"
                              className="stepper-btn"
                              onClick={() => {
                                if (!isSelected) {
                                  setVisitData({
                                    ...visitData,
                                    selectedProcedures: [
                                      ...visitData.selectedProcedures,
                                      { vykonId: proc.id, pocet: 1, kodVykonu: proc.kodVykonu, nazev: proc.nazev, body: proc.body }
                                    ]
                                  });
                                } else {
                                  setVisitData({
                                    ...visitData,
                                    selectedProcedures: visitData.selectedProcedures.map(p =>
                                      p.vykonId === proc.id ? { ...p, pocet: p.pocet + 1 } : p
                                    )
                                  });
                                }
                              }}
                            >
                              +
                            </button>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>

                <div style={{ marginTop: '2rem', display: 'flex', justifyContent: 'flex-end', gap: '1rem' }}>
                  <button type="submit" className="btn btn-primary">
                    💾 Uložit návštěvu a zaúčtovat výkony
                  </button>
                </div>
              </form>
            </div>
          )}

          {/* ================= POHLED: FRONTA K PŘEOBJEDNÁNÍ ================= */}
          {activeTab === 'preobjednani' && (
            <div className="card">
              <div className="card-header">
                <h2>Fronta pacientů k přeobjednání (po nemoci lékaře)</h2>
                <span className="badge-count" style={{ marginLeft: 0 }}>
                  {rescheduleQueue.length} čekajících
                </span>
              </div>

              {rescheduleQueue.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
                  <p style={{ fontSize: '1.2rem', fontWeight: 600 }}>Fronta k přeobjednání je prázdná</p>
                  <p style={{ fontSize: '0.9rem', marginTop: '0.5rem' }}>
                    Žádný pacient nemá zrušený termín z důvodu nemoci či výluky lékaře.
                  </p>
                </div>
              ) : (
                <div className="table-container">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Původní termín</th>
                        <th>Pacient</th>
                        <th>Telefon</th>
                        <th>Původní lékař</th>
                        <th>Poznámka</th>
                        <th>Akce recepce</th>
                      </tr>
                    </thead>
                    <tbody>
                      {rescheduleQueue.map(item => (
                        <tr key={item.id}>
                          <td><strong>{new Date(item.casOd).toLocaleString('cs-CZ')}</strong></td>
                          <td>{item.pacientPrijmeni} {item.pacientJmeno}</td>
                          <td><code>{item.pacientTelefon}</code></td>
                          <td>{item.lekarCeleJmeno}</td>
                          <td>{item.poznamka || '–'}</td>
                          <td>
                            <button
                              className="btn btn-primary btn-sm"
                              onClick={() => {
                                setSelectedDoctorId(String(item.lekarId));
                                setActiveTab('diar');
                                showToast(`Přeobjednání pacienta ${item.pacientPrijmeni}. Vyberte nový volný slot v kalendáři.`);
                              }}
                            >
                              📞 Přeobjednat na volný slot
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}

          {/* ================= POHLED: MĚSÍČNÍ VÝKAZ ================= */}
          {activeTab === 'vykazy' && monthlyReport && (
            <div>
              <div className="calendar-controls">
                <div className="filter-group">
                  <span className="filter-label">Lékař:</span>
                  <select
                    className="select-input"
                    value={selectedDoctorId}
                    onChange={(e) => setSelectedDoctorId(e.target.value)}
                  >
                    <option value="">Všichni lékaři praxe</option>
                    {doctors.map(d => (
                      <option key={d.id} value={d.id}>{d.celeJmeno}</option>
                    ))}
                  </select>
                </div>
                <div className="filter-group">
                  <span className="filter-label">Období:</span>
                  <span style={{ fontWeight: 600 }}>{monthlyReport.mesic} / {monthlyReport.rok}</span>
                </div>
                <button className="btn btn-secondary btn-sm" onClick={loadReport} style={{ marginLeft: 'auto' }}>
                  🔄 Přepočítat výkaz
                </button>
              </div>

              <div className="card">
                <div className="card-header">
                  <h2>Souhrnný měsíční přehled pro zdravotní pojišťovny</h2>
                  <div style={{ display: 'flex', gap: '1.5rem', textAlign: 'right' }}>
                    <div>
                      <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Realizovaných návštěv:</span>
                      <div style={{ fontSize: '1.25rem', fontWeight: 700 }}>{monthlyReport.celkemNavstev}</div>
                    </div>
                    <div>
                      <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Celkem vykázaných bodů:</span>
                      <div style={{ fontSize: '1.25rem', fontWeight: 800, color: 'var(--primary)' }}>
                        {monthlyReport.celkemBodu.toLocaleString('cs-CZ')} bodů
                      </div>
                    </div>
                  </div>
                </div>

                <div className="table-container">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Kód výkonu</th>
                        <th>Název lékařského výkonu</th>
                        <th>Počet provedení</th>
                        <th>Body za jednotku</th>
                        <th>Body celkem</th>
                      </tr>
                    </thead>
                    <tbody>
                      {monthlyReport.vykony.map((v, i) => (
                        <tr key={i}>
                          <td><code>{v.kodVykonu}</code></td>
                          <td><strong>{v.nazevVykonu}</strong></td>
                          <td>{v.celkemProvedeno}x</td>
                          <td>{v.bodyZaJednotku} b.</td>
                          <td><strong style={{ color: 'var(--primary)' }}>{v.celkemBodu.toLocaleString('cs-CZ')} b.</strong></td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          )}
        </div>
      </main>

      {/* ================= MODAL: NOVÁ REZERVACE ================= */}
      {showNewReservationModal && selectedSlotForReservation && (
        <div className="modal-overlay" onClick={() => setShowNewReservationModal(false)}>
          <div className="modal-box" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Rezervace termínu v diáři</h3>
              <button className="close-btn" onClick={() => setShowNewReservationModal(false)}>×</button>
            </div>

            <form onSubmit={handleCreateReservation}>
              <div className="modal-content">
                <div style={{ background: 'var(--primary-light)', padding: '0.75rem 1rem', borderRadius: 'var(--radius-md)', marginBottom: '1.25rem' }}>
                  <p style={{ fontSize: '0.875rem', color: 'var(--primary-dark)', fontWeight: 600 }}>
                    Vybraný čas: {selectedSlotForReservation.casOd.substring(11, 16)} ({selectedDate})
                  </p>
                  <p style={{ fontSize: '0.8rem', color: 'var(--primary-dark)' }}>
                    Lékař: {doctors.find(d => String(d.id) === selectedDoctorId)?.celeJmeno}
                  </p>
                </div>

                <div className="form-group">
                  <label className="form-label">Pacient:</label>
                  <select
                    className="select-input"
                    style={{ width: '100%' }}
                    value={newResPatientId}
                    onChange={(e) => setNewResPatientId(e.target.value)}
                    required
                  >
                    <option value="">-- Vyberte pacienta z kartotéky --</option>
                    {patients.map(p => (
                      <option key={p.id} value={p.id}>
                        {p.prijmeni} {p.jmeno} (tel: {p.telefon}, RČ: {p.cisloPojistence})
                      </option>
                    ))}
                  </select>
                  <div className="form-hint">
                    Není pacient v seznamu? <a href="#" onClick={(e) => { e.preventDefault(); setShowNewPatientModal(true); }}>Registrovat nového pacienta</a>
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label">Délka slotu (násobek 15 min):</label>
                  <select
                    className="select-input"
                    style={{ width: '100%' }}
                    value={newResDurationMinutes}
                    onChange={(e) => setNewResDurationMinutes(Number(e.target.value))}
                  >
                    <option value={15}>15 minut (běžná kontrola / recept)</option>
                    <option value={30}>30 minut (komplexní vyšetření)</option>
                    <option value={45}>45 minut (zákrok / odběry + kontrola)</option>
                    <option value={60}>60 minut (vstupní prohlídka)</option>
                  </select>
                </div>

                <div className="form-group">
                  <label className="form-label">Poznámka / Důvod návštěvy:</label>
                  <input
                    type="text"
                    className="text-input"
                    style={{ width: '100%' }}
                    placeholder="např. preventivní kontrola, předpis léků, bolesti hlavy..."
                    value={newResNote}
                    onChange={(e) => setNewResNote(e.target.value)}
                  />
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowNewReservationModal(false)}>
                  Zrušit
                </button>
                <button type="submit" className="btn btn-primary">
                  Vytvořit rezervaci
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ================= MODAL: DETAIL REZERVACE A STORNO ================= */}
      {showReservationDetailModal && selectedReservation && (
        <div className="modal-overlay" onClick={() => setShowReservationDetailModal(false)}>
          <div className="modal-box" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Detail rezervovaného termínu</h3>
              <button className="close-btn" onClick={() => setShowReservationDetailModal(false)}>×</button>
            </div>

            <div className="modal-content">
              <div style={{ marginBottom: '1rem' }}>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Čas a lékař:</p>
                <p style={{ fontSize: '1.1rem', fontWeight: 700 }}>
                  {selectedReservation.casOd.substring(11, 16)} – {selectedReservation.casDo.substring(11, 16)} ({selectedDate})
                </p>
                <p>{doctors.find(d => String(d.id) === selectedDoctorId)?.celeJmeno}</p>
              </div>

              <div style={{ marginBottom: '1rem' }}>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Pacient:</p>
                <p style={{ fontSize: '1.15rem', fontWeight: 700, color: 'var(--primary-dark)' }}>
                  {selectedReservation.pacientCeleJmeno}
                </p>
              </div>

              {selectedReservation.poznamka && (
                <div style={{ marginBottom: '1rem' }}>
                  <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Důvod / Poznámka:</p>
                  <p>{selectedReservation.poznamka}</p>
                </div>
              )}

              <div style={{ background: '#fef2f2', border: '1px solid #fecaca', padding: '0.75rem', borderRadius: 'var(--radius-md)' }}>
                <p style={{ fontSize: '0.8rem', color: '#991b1b' }}>
                  ℹ️ Při stornování se termín okamžitě uvolní pro nového pacienta. Zrušený termín zůstává v historii pro statistiku nespolehlivosti.
                </p>
              </div>
            </div>

            <div className="modal-footer" style={{ justifyContent: 'space-between' }}>
              <button
                type="button"
                className="btn btn-danger"
                onClick={handleCancelReservation}
              >
                ✕ Stornovat rezervaci (uvolnit slot)
              </button>

              <button
                type="button"
                className="btn btn-primary"
                onClick={() => {
                  setShowReservationDetailModal(false);
                  setActiveTab('navsteva');
                }}
              >
                Zaevidovat návštěvu ➜
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ================= MODAL: NOVÝ PACIENT ================= */}
      {showNewPatientModal && (
        <div className="modal-overlay" onClick={() => setShowNewPatientModal(false)}>
          <div className="modal-box" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Registrace nového pacienta</h3>
              <button className="close-btn" onClick={() => setShowNewPatientModal(false)}>×</button>
            </div>

            <form onSubmit={handleCreatePatient}>
              <div className="modal-content">
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div className="form-group">
                    <label className="form-label">Jméno:</label>
                    <input
                      type="text"
                      className="text-input"
                      style={{ width: '100%' }}
                      value={newPatientData.jmeno}
                      onChange={(e) => setNewPatientData({ ...newPatientData, jmeno: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Příjmení:</label>
                    <input
                      type="text"
                      className="text-input"
                      style={{ width: '100%' }}
                      value={newPatientData.prijmeni}
                      onChange={(e) => setNewPatientData({ ...newPatientData, prijmeni: e.target.value })}
                      required
                    />
                  </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div className="form-group">
                    <label className="form-label">Telefon:</label>
                    <input
                      type="text"
                      className="text-input"
                      style={{ width: '100%' }}
                      value={newPatientData.telefon}
                      onChange={(e) => setNewPatientData({ ...newPatientData, telefon: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Číslo pojištěnce (RČ bez lomítka):</label>
                    <input
                      type="text"
                      className="text-input"
                      style={{ width: '100%' }}
                      value={newPatientData.cisloPojistence}
                      onChange={(e) => setNewPatientData({ ...newPatientData, cisloPojistence: e.target.value })}
                      placeholder="např. 8501011234"
                      required
                    />
                  </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div className="form-group">
                    <label className="form-label">Rok narození:</label>
                    <input
                      type="number"
                      className="text-input"
                      style={{ width: '100%' }}
                      value={newPatientData.rokNarozeni}
                      onChange={(e) => setNewPatientData({ ...newPatientData, rokNarozeni: Number(e.target.value) })}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Zdravotní pojišťovna:</label>
                    <select
                      className="select-input"
                      style={{ width: '100%' }}
                      value={newPatientData.kodPojistovny}
                      onChange={(e) => setNewPatientData({ ...newPatientData, kodPojistovny: e.target.value })}
                    >
                      <option value="111">111 - VZP ČR</option>
                      <option value="201">201 - VoZP</option>
                      <option value="205">205 - ČPZP</option>
                      <option value="207">207 - OZP</option>
                      <option value="211">211 - ZPMV ČR</option>
                    </select>
                  </div>
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowNewPatientModal(false)}>
                  Zrušit
                </button>
                <button type="submit" className="btn btn-primary">
                  Uložit pacienta do kartotéky
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ================= MODAL: ZADÁNÍ VÝLUKY LÉKAŘE ================= */}
      {showAbsenceModal && (
        <div className="modal-overlay" onClick={() => setShowAbsenceModal(false)}>
          <div className="modal-box" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Zadání nepřítomnosti / výluky lékaře</h3>
              <button className="close-btn" onClick={() => setShowAbsenceModal(false)}>×</button>
            </div>

            <form onSubmit={handleSaveAbsence}>
              <div className="modal-content">
                <div className="form-group">
                  <label className="form-label">Lékař:</label>
                  <select
                    className="select-input"
                    style={{ width: '100%' }}
                    value={absenceData.lekarId}
                    onChange={(e) => setAbsenceData({ ...absenceData, lekarId: e.target.value })}
                    required
                  >
                    {doctors.map(d => (
                      <option key={d.id} value={d.id}>{d.celeJmeno}</option>
                    ))}
                  </select>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div className="form-group">
                    <label className="form-label">Začátek výluky:</label>
                    <input
                      type="datetime-local"
                      className="text-input"
                      style={{ width: '100%' }}
                      value={absenceData.datumOd}
                      onChange={(e) => setAbsenceData({ ...absenceData, datumOd: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Konec výluky:</label>
                    <input
                      type="datetime-local"
                      className="text-input"
                      style={{ width: '100%' }}
                      value={absenceData.datumDo}
                      onChange={(e) => setAbsenceData({ ...absenceData, datumDo: e.target.value })}
                      required
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label">Důvod nepřítomnosti:</label>
                  <input
                    type="text"
                    className="text-input"
                    style={{ width: '100%' }}
                    value={absenceData.duvod}
                    onChange={(e) => setAbsenceData({ ...absenceData, duvod: e.target.value })}
                    required
                  />
                  <div className="form-hint">
                    Všechny existující rezervace lékaře v tomto intervalu budou automaticky přesunuty do fronty k přeobjednání.
                  </div>
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowAbsenceModal(false)}>
                  Zavřít
                </button>
                <button type="submit" className="btn btn-danger">
                  Zaznamenat výluku
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Notifikační toast */}
      {toast && (
        <div className={`alert-toast alert-${toast.type}`}>
          {toast.type === 'success' ? '✓' : '⚠️'} {toast.message}
        </div>
      )}
    </div>
  );
}
