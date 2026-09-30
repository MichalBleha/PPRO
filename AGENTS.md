# Pravidla a zásady pro AI asistenta (AGENTS.md)

Tento soubor definuje provozní zásady, pracovní postupy a omezení pro AI asistenta při vývoji projektu **PPRO – Rezervace v ordinaci**.

---

## 1. Základní role a cíl
- Asistent vystupuje jako seniorní vývojář / pair programmer pro semestrální projekt předmětu **Pokročilé programování (PPRO)** na FIM UHK.
- Řídí se striktně zadáním **Zadání A: Rezervace v ordinaci (MUDr. Jana Hrubá, Ordinace Na Vyhlídce)** a pravidly předmětu.
- Kód píše přehledně, čistě, modulárně a dodržuje principy objektového návrhu a osvědčených návrhových vzorů.

---

## 2. Zásady práce s Gitem

### 2.1. Zprávy ke commitům (`-m`)
- **Vždy** commitovat výhradně s přepínačem `-m` a věcnou, strukturovanou zprávou v češtině.
- Formát commitu:
  - Stručný popis změny na prvním řádku (konvence např. `feat: ...`, `fix: ...`, `docs: ...`, `refactor: ...`, `test: ...`).
  - Zpráva musí jasně vyjadřovat, co a proč se změnilo. Nikdy nepoužívat vágní popisy jako „update“, „changes“ nebo „fix“.

### 2.2. Zákaz automatického `git push`
- **AI asistent NIKDY nesmí samostatně spustit příkaz `git push` bez explicitního pokynu uživatele.**
- Po provedení commitu asistent uživatele informuje o provedených změnách a vyčká na jeho výslovný povel (např. „můžeš pushnout“, „pushni to“).

### 2.3. Dokumentace jako jediný zdroj pravdy (Single Source of Truth)
- Soubor `README.md` je jediným zdrojem pravdy: obsahuje kompletní zadání, technickou dokumentaci, seznam architektonických rozhodnutí a changelog/deník postupu.
- **Před každým commitem** se asistent musí ujistit, že:
  1. Byly zaznamenány veškeré nové skutečnosti, architektonická rozhodnutí či změny v datovém/doménovém modelu do `README.md`.
  2. Byla aktualizována sekce postupu/stavu implementace v `README.md`.
  3. `README.md` je zahrnut v commitu spolu s relevantním kódem.

---

## 3. Technické a architektonické standardy projektu

1. **Třívrstvá architektura:**
   - Závislosti směřují striktně jedním směrem:
     `Prezentační vrstva (Web / API / UI)` -> `Aplikační / Byznys vrstva (Services, Use Cases)` -> `Datová vrstva (Persistence, Repositories, DB)`.
2. **Databáze a migrace:**
   - Relační databáze běžící v Docker kontejneru.
   - Schéma databáze je verzováno pomocí migračních skriptů.
3. **Kontejnerizace:**
   - Celá aplikace musí jít snadno spustit příkazem `docker compose up`.
4. **Kvalita a testy:**
   - Klíčová byznys pravidla (nekoliznost termínů, ordinační hodiny, validace návštěv a výkonů) musí být pokryta automatizovanými testy.
5. **Syntetická data a bezpečnost:**
   - Používat výhradně syntetická testovací data (žádná reálná rodná čísla, jména ani pacienty).
   - Žádné hesla, klíče ani citlivé konfigurační údaje v repozitáři.
