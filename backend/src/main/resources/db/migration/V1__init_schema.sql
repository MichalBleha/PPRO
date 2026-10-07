-- V1__init_schema.sql
-- Inicializace schématu pro Ordinaci Na Vyhlídce (8 entit)

CREATE TABLE IF NOT EXISTS pacient (
    id BIGSERIAL PRIMARY KEY,
    jmeno VARCHAR(100) NOT NULL,
    prijmeni VARCHAR(100) NOT NULL,
    rok_narozeni INT NOT NULL,
    telefon VARCHAR(20) NOT NULL,
    cislo_pojistence VARCHAR(20) NOT NULL UNIQUE,
    kod_pojistovny VARCHAR(10) NOT NULL,
    vytvoreno TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pacient_prijmeni ON pacient(prijmeni);
CREATE INDEX IF NOT EXISTS idx_pacient_telefon ON pacient(telefon);
CREATE INDEX IF NOT EXISTS idx_pacient_search ON pacient(prijmeni, telefon);

CREATE TABLE IF NOT EXISTS lekar (
    id BIGSERIAL PRIMARY KEY,
    titul VARCHAR(20),
    jmeno VARCHAR(100) NOT NULL,
    prijmeni VARCHAR(100) NOT NULL,
    aktivni BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS ordinacni_hodina (
    id BIGSERIAL PRIMARY KEY,
    lekar_id BIGINT NOT NULL REFERENCES lekar(id) ON DELETE CASCADE,
    den_v_tydnu INT NOT NULL CHECK (den_v_tydnu BETWEEN 1 AND 7),
    cas_od TIME NOT NULL,
    cas_do TIME NOT NULL,
    pracoviste_adresa VARCHAR(255) NOT NULL,
    lze_objednat BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_ordinacni_hodina_lekar ON ordinacni_hodina(lekar_id, den_v_tydnu);

CREATE TABLE IF NOT EXISTS vyluka_lekare (
    id BIGSERIAL PRIMARY KEY,
    lekar_id BIGINT NOT NULL REFERENCES lekar(id) ON DELETE CASCADE,
    datum_od TIMESTAMP WITH TIME ZONE NOT NULL,
    datum_do TIMESTAMP WITH TIME ZONE NOT NULL,
    duvod VARCHAR(255) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_vyluka_lekare_lekar_interval ON vyluka_lekare(lekar_id, datum_od, datum_do);

CREATE TABLE IF NOT EXISTS rezervace (
    id BIGSERIAL PRIMARY KEY,
    pacient_id BIGINT NOT NULL REFERENCES pacient(id) ON DELETE RESTRICT,
    lekar_id BIGINT NOT NULL REFERENCES lekar(id) ON DELETE RESTRICT,
    cas_od TIMESTAMP WITH TIME ZONE NOT NULL,
    cas_do TIMESTAMP WITH TIME ZONE NOT NULL,
    stav VARCHAR(30) NOT NULL DEFAULT 'PLANOVANA',
    poznamka TEXT,
    datum_zruseni TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_rezervace_lekar_termin ON rezervace(lekar_id, cas_od, cas_do, stav);
CREATE INDEX IF NOT EXISTS idx_rezervace_pacient ON rezervace(pacient_id);

CREATE TABLE IF NOT EXISTS navsteva (
    id BIGSERIAL PRIMARY KEY,
    rezervace_id BIGINT REFERENCES rezervace(id) ON DELETE SET NULL,
    pacient_id BIGINT NOT NULL REFERENCES pacient(id) ON DELETE RESTRICT,
    lekar_id BIGINT NOT NULL REFERENCES lekar(id) ON DELETE RESTRICT,
    zacatek_navstevy TIMESTAMP WITH TIME ZONE NOT NULL,
    konec_navstevy TIMESTAMP WITH TIME ZONE,
    zprava_lekare TEXT
);

CREATE INDEX IF NOT EXISTS idx_navsteva_pacient ON navsteva(pacient_id);
CREATE INDEX IF NOT EXISTS idx_navsteva_lekar_datum ON navsteva(lekar_id, zacatek_navstevy);

CREATE TABLE IF NOT EXISTS vykon (
    id BIGSERIAL PRIMARY KEY,
    kod_vykonu VARCHAR(20) NOT NULL UNIQUE,
    nazev VARCHAR(255) NOT NULL,
    body INT NOT NULL CHECK (body >= 0),
    platnost_od DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS navsteva_vykon (
    id BIGSERIAL PRIMARY KEY,
    navsteva_id BIGINT NOT NULL REFERENCES navsteva(id) ON DELETE CASCADE,
    vykon_id BIGINT NOT NULL REFERENCES vykon(id) ON DELETE RESTRICT,
    pocet INT NOT NULL DEFAULT 1 CHECK (pocet > 0),
    priznane_body INT NOT NULL CHECK (priznane_body >= 0),
    CONSTRAINT uq_navsteva_vykon UNIQUE (navsteva_id, vykon_id)
);

CREATE INDEX IF NOT EXISTS idx_navsteva_vykon_navsteva ON navsteva_vykon(navsteva_id);
