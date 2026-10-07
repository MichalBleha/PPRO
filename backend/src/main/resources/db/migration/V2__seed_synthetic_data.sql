-- V2__seed_synthetic_data.sql
-- Fiktivní / syntetická testovací data pro Ordinaci Na Vyhlídce

-- 1. Lékaři (3 lékaři soukromé praxe)
INSERT INTO lekar (id, titul, jmeno, prijmeni, aktivni) VALUES
(1, 'MUDr.', 'Jana', 'Hrubá', true),
(2, 'MUDr.', 'Tomáš', 'Dvořák', true),
(3, 'MUDr.', 'Eva', 'Novotná', true);
SELECT setval('lekar_id_seq', (SELECT MAX(id) FROM lekar));

-- 2. Ordinační hodiny (1 = Pondělí ... 5 = Pátek)
INSERT INTO ordinacni_hodina (lekar_id, den_v_tydnu, cas_od, cas_do, pracoviste_adresa, lze_objednat) VALUES
-- MUDr. Jana Hrubá: Po, St, Pá dopoledne
(1, 1, '08:00:00', '13:00:00', 'Ordinace Na Vyhlídce, Hradec Králové', true),
(1, 3, '08:00:00', '13:00:00', 'Ordinace Na Vyhlídce, Hradec Králové', true),
(1, 5, '08:00:00', '12:00:00', 'Ordinace Na Vyhlídce, Hradec Králové', true),

-- MUDr. Tomáš Dvořák: Út, Čt
(2, 2, '09:00:00', '15:00:00', 'Ordinace Na Vyhlídce, Hradec Králové', true),
(2, 4, '09:00:00', '15:00:00', 'Ordinace Na Vyhlídce, Hradec Králové', true),

-- MUDr. Eva Novotná: Po odpoledne, St odpoledne
(3, 1, '13:00:00', '17:00:00', 'Ordinace Na Vyhlídce, Hradec Králové', true),
(3, 3, '13:00:00', '17:00:00', 'Ordinace Na Vyhlídce, Hradec Králové', true);
SELECT setval('ordinacni_hodina_id_seq', (SELECT MAX(id) FROM ordinacni_hodina));

-- 3. Číselník lékařských výkonů
INSERT INTO vykon (id, kod_vykonu, nazev, body, platnost_od) VALUES
(1, '09511', 'Komplexní vyšetření praktickým lékařem', 250, '2026-01-01'),
(2, '09513', 'Cílené vyšetření praktickým lékařem', 150, '2026-01-01'),
(3, '09523', 'Kontrolní klinické vyšetření', 100, '2026-01-01'),
(4, '09115', 'Odběr krve ze žíly pro laboratorní vyšetření', 45, '2026-01-01'),
(5, '09127', 'Injekční aplikace do svalu nebo podkoží', 35, '2026-01-01'),
(6, '09220', 'EKG vyšetření včetně popisu křivky', 180, '2026-01-01');
SELECT setval('vykon_id_seq', (SELECT MAX(id) FROM vykon));

-- 4. Pacienti (striktně syntetické identitní údaje)
INSERT INTO pacient (id, jmeno, prijmeni, rok_narozeni, telefon, cislo_pojistence, kod_pojistovny) VALUES
(1, 'Jan', 'Novák', 1980, '+420777111222', '8005121234', '111'),
(2, 'Marie', 'Svobodová', 1975, '+420777333444', '7558204321', '205'),
(3, 'Petr', 'Černý', 1992, '+420777555666', '9203159876', '111'),
(4, 'Alena', 'Procházková', 1968, '+420777777888', '6859101122', '201'),
(5, 'Tomáš', 'Kučera', 2001, '+420777999000', '0101015566', '207');
SELECT setval('pacient_id_seq', (SELECT MAX(id) FROM pacient));

-- 5. Ukázková výluka lékaře (dovolená MUDr. Hrubé)
INSERT INTO vyluka_lekare (id, lekar_id, datum_od, datum_do, duvod) VALUES
(1, 1, '2026-10-20 00:00:00+02', '2026-10-22 23:59:59+02', 'Účast na odborné konferenci');
SELECT setval('vyluka_lekare_id_seq', (SELECT MAX(id) FROM vyluka_lekare));

-- 6. Příklad dokončené návštěvy a vykázaných výkonů
INSERT INTO navsteva (id, pacient_id, lekar_id, zacatek_navstevy, konec_navstevy, zprava_lekare) VALUES
(1, 1, 1, '2026-10-05 08:30:00+02', '2026-10-05 08:50:00+02', 'Preventivní prohlídka, tlak 125/80, bez patologického nálezu.');
SELECT setval('navsteva_id_seq', (SELECT MAX(id) FROM navsteva));

INSERT INTO navsteva_vykon (navsteva_id, vykon_id, pocet, priznane_body) VALUES
(1, 1, 1, 250),
(1, 4, 1, 45);
