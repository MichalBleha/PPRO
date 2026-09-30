# PPRO – Rezervace v ordinaci (Zadání A)

Projekt do předmětu **Pokročilé programování (PPRO)**, zimní semestr 2026/2027, FIM UHK.

Tento dokument slouží jako **jediný zdroj pravdy (Single Source of Truth)**: obsahuje kompletní specifikaci zadání klienta, technickou dokumentaci, evidenci architektonických rozhodnutí, popis datového a vrstvového modelu a deník postupu vývoje.

---

## 📋 Obsah
1. [Zadání klienta a kontext](#-zadání-klienta-a-kontext)
2. [Požadavky klienta](#-požadavky-klienta)
3. [Seznam architektonických rozhodnutí (Otevřené body)](#-seznam-architektonických-rozhodnutí-otevřené-body)
4. [Povinné minimum projektu](#-povinné-minimum-projektu)
5. [Architektura a vrstvy](#-architektura-a-vrstvy)
6. [Doménový a datový model](#-doménový-a-datový-model)
7. [Instalace a spuštění](#-instalace-a-spuštění)
8. [Testování](#-testování)
9. [Deník vývoje a změn (Changelog)](#-deník-vývoje-a-změn-changelog)

---

## 🏥 Zadání klienta a kontext

- **Klient:** MUDr. Jana Hrubá, *Ordinace Na Vyhlídce*
- **Charakteristika praxe:** Soukromá praxe, tři lékaři na částečné úvazky, paní Věra na recepci. Kmen čítá zhruba 900 pacientů, polovina dochází pravidelně.
- **Současný stav (problémy k řešení):**
  - Vedení agendy v papírovém diáři s tužkou.
  - Hledání volných termínů vyžaduje zdlouhavé listování při telefonátech.
  - Při nemoci lékaře musí recepční ručně dohledávat a obvolávat pacienty ze seznamu.
  - Na konci měsíce recepční celý podvečer ručně přepisuje výkony do tabulky pro pojišťovnu.
  - Riziko duplicitního objednání na stejný čas (kolize termínů v čekárně).

---

## 📌 Požadavky klienta

### Funkční požadavky
1. **Evidence pacientů:** Jméno, příjmení, rok narození, telefon, identifikátor pojištěnce (číslo pojištěnce), zdravotní pojišťovna. Rychlé vyhledávání podle příjmení nebo telefonního čísla přímo při hovoru.
2. **Lékaři a ordinační hodiny:** Každý lékař má specifické ordinační dny a časy. Možnost označit hodiny/místo, kde se nelze objednávat (např. ordinování na externí adrese).
3. **Rezervace termínu:** Rezervace na konkrétní termín u zvoleného lékaře vytvářená recepcí. Možnost zapsat poznámku s důvodem návštěvy.
4. **Záznam o návštěvě a výkonech:** Po příchodu pacienta se zaeviduje uskutečněná návštěva a provedené výkony z číselníku (kód, název, body). Možnost zadat více různých výkonů i násobný počet téhož výkonu (např. 3 aplikace).
5. **Zrušení rezervace a statistika nespolehlivosti:** Možnost stornovat termín s evidencí historie rušení pro sledování pacientů, kteří ruší opakovaně.
6. **Přehled volných termínů (klíčová funkce):** Týdenní matice volných slotů pro vybraného lékaře pro bleskovou reakci recepční při telefonátu.
7. **Měsíční výkaz pro pojišťovny:** Agregovaný přehled vykázaných výkonů a součet bodů za daný měsíc a lékaře.

### Obchodní pravidla (vynucená aplikací)
- **Zákaz kolize termínů:** U jednoho lékaře nesmí existovat dvě překrývající se aktivní rezervace.
- **Validace ordinačních hodin:** Rezervaci lze umístit výhradně do platných ordinačních hodin daného lékaře na dané pracoviště.
- **Návaznost výkonů:** Lékařský výkon lze přiřadit pouze k návštěvě, která již reálně proběhla (nikoliv k budoucí rezervaci).
- **Integrita a audit historie:** Historie pacienta a provedené výkony se fyzicky nemažou (právní a doložitelná odpovědnost péče).

### Ergonomie a provozní požadavky
- Intuitivní a svižné UI zvládnutelné i pro méně technickou recepční.
- Okamžitá odezva při vyhledávání v průběhu telefonního hovoru.

---

## ⚖️ Seznam architektonických rozhodnutí (Otevřené body)

V souladu s metodikou předmětu jsou zde evidována jednoznačná rozhodnutí k bodům, které klient nechal otevřené:

### 1. Délka termínu a struktura diáře
- **Rozhodnutí:** Diář je postaven na pevné základní mřížce po **15 minutách** (časový slot).
- **Zdůvodnění:** Pevná 15minutová mřížka umožňuje rychlé a deterministické vyhledávání volných oken a zabraňuje fragmentaci kalendáře. Běžná kontrola zabírá 1 slot (15 min), náročnější zákroky mohou obsadit násobek (např. 2 sloty = 30 min).

### 2. Oprávnění a zakládání rezervací
- **Rozhodnutí:** V 1. fázi je zakládání rezervací vyhrazeno **výhradně recepci (paní Věra)**. Architektura aplikační a servisní vrstvy je však navržena s rolemi (`Role-Based Access Control`), což umožní v budoucnu bezpečně dopojit samoobslužné klientské webové rozhraní bez zásahu do byznys jádra.
- **Zdůvodnění:** Klientka má z internetového objednávání obavy a personál potřebuje mít plnou kontrolu nad skladbou pacientů.

### 3. Zpracování zrušených rezervací (Historie vs. Diář)
- **Rozhodnutí:** Rezervace se fyzicky z databáze nemažou. Používá se stavový automat: `Planovana` (Scheduled), `Dokoncena` (Completed), `Zrusena` (Cancelled).
- **Zdůvodnění:** Při přechodu do stavu `Zrusena` se daný časový slot okamžitě uvolní pro novou rezervaci v přehledu volných termínů (kolizní pravidlo kontroluje pouze stavy `Planovana`). Původní rezervace však zůstává uložena s časovým razítkem storna, což umožňuje generovat statistiky nespolehlivých pacientů.

### 4. Jednoznačná totožnost pacienta a eliminace duplicit
- **Rozhodnutí:** Jednoznačným přirozeným identifikátorem pacienta je **číslo pojištěnce (rodné číslo bez lomítka)** opatřené unikátním indexem v databázi.
- **Zdůvodnění:** Jméno ani telefon nejsou unikátní. Pokud se recepční pokusí založit pacienta se shodným číslem pojištěnce, systém operaci odmítne a nabídne otevření existující karty pacienta. Vyhledávání je optimalizováno pro fulltext/prefix nad příjmením a telefonním číslem.

### 5. Postup při nemoci lékaře
- **Rozhodnutí:** Systém implementuje funkci **„Zaznamenat nepřítomnost lékaře“** (vyloučení z ordinace na zadaný interval).
- **Zdůvodnění:** Systém automaticky uzamkne dotčené období pro nové rezervace a existující rezervace přepne do stavu `VyzadujePreobjednani`. Recepční se vygeneruje přehledná fronta pacientů k obvolání s kontaktními údaji a možností přeobjednat pacienta k jinému dostupnému lékaři nebo na náhradní termín na jedno kliknutí.

---

## 🎯 Povinné minimum projektu

| Požadavek předmětu | Způsob splnění v projektu |
| :--- | :--- |
| **3 vrstvy se závislostmi jedním směrem** | Web/API (Prezentační) ➔ Application/Domain (Byznys) ➔ Infrastructure/Persistence (Datová) |
| **Relační databáze v Dockeru** | PostgreSQL běžící v dedikovaném kontejneru |
| **Verzované databázové migrace** | Automatické migrace aplikované při startu aplikace |
| **Nejméně 5 entit** | Pacient, Lekar, OrdinacniHodina, Rezervace, Navsteva, Vykon, NavstevaVykon, VylukaLekare (8 entit) |
| **Alespoň jedna vazba M:N** | Vazba mezi `Navsteva` a `Vykon` přes asociační entitu `NavstevaVykon` (s atributem `Pocet` a `Body`) |
| **Automatizované testy** | Jednotkové a integrační testy pro klíčová pravidla (kolize, ordinační hodiny, kalkulace bodů) |
| **Spuštění příkazem `docker compose up`** | Kompletní orchestrace aplikace a databáze v `docker-compose.yml` |
| **Technická dokumentace** | Udržována v tomto `README.md` jako jediném zdroji pravdy |
| **Syntetická data** | Seeder generuje realistická, avšak striktně fiktivní testovací data |

---

## 🏗️ Architektura a vrstvy

Aplikace striktně dodržuje jednosměrný tok závislostí:

```text
┌────────────────────────────────────────────────────────┐
│             Prezentační vrstva (Presentation)          │
│   - REST API kontrolery / Razor Pages / Web UI        │
│   - Validace vstupních DTO, serializace                │
└───────────────────────────┬────────────────────────────┘
                            │ závisí na
                            ▼
┌────────────────────────────────────────────────────────┐
│             Aplikační a doménová vrstva (Core)         │
│   - Doménové entity a byznys invarianty               │
│   - Služby: ReservationService, ScheduleService        │
│   - Kontrola kolizí, výpočty bodů pojišťovny           │
│   - Rozhraní repozitářů (IRepository)                  │
└───────────────────────────┬────────────────────────────┘
                            │ implementováno v
                            ▼
┌────────────────────────────────────────────────────────┐
│             Infrastrukturní vrstva (Infrastructure)    │
│   - Relační databáze (Entity Framework Core / SQL)     │
│   - Konkrétní repozitáře, databázový kontext, migrace  │
└────────────────────────────────────────────────────────┘
```

---

## 💾 Doménový a datový model

Aplikace pracuje s následujícími klíčovými entitami:

1. **`Pacient`:** `Id`, `Jmeno`, `Prijmeni`, `RokNarozeni`, `Telefon`, `CisloPojistence` (UNIQUE), `KodPojistovny`, `Vytvoreno`
2. **`Lekar`:** `Id`, `Titul`, `Jmeno`, `Prijmeni`, `Aktivni`
3. **`OrdinacniHodina`:** `Id`, `LekarId`, `DenVTydnu`, `CasOd`, `CasDo`, `PracovisteAdresa`, `LzeObjednat`
4. **`VylukaLekare`:** `Id`, `LekarId`, `DatumOd`, `DatumDo`, `Duvod`
5. **`Rezervace`:** `Id`, `PacientId`, `LekarId`, `CasOd`, `CasDo`, `Stav` (`Planovana`, `Dokoncena`, `Zrusena`, `VyzadujePreobjednani`), `Poznamka`, `DatumZruseni`
6. **`Navsteva`:** `Id`, `RezervaceId` (volitelné pro neplánované), `PacientId`, `LekarId`, `ZacatekNavstevy`, `KonecNavstevy`, `ZpravaLekare`
7. **`Vykon`:** `Id`, `KodVykonu` (UNIQUE), `Nazev`, `Body`, `PlatnostOd`
8. **`NavstevaVykon` (vazba M:N):** `Id`, `NavstevaId`, `VykonId`, `Pocet`, `PriznaneBody`

---

## 🚀 Instalace a spuštění

Celé prostředí lze zprovoznit pomocí Dockeru:

```bash
# 1. Klonování repozitáře
git clone https://github.com/MichalBleha/PPRO.git
cd PPRO

# 2. Spuštění kontejnerů (databáze + aplikace)
docker compose up -d --build
```

---

## 🧪 Testování

Spuštění sady automatizovaných testů ověřujících byznys pravidla:
```bash
# Spuštění testů
dotnet test
```

Testované scénáře zahrnují zejména:
- Pokus o překryv rezervací u stejného lékaře (očekáváno zamítnutí).
- Rezervace mimo ordinační dobu (očekáváno zamítnutí).
- Přiřazení výkonu k neproběhlé návštěvě (očekáváno zamítnutí).
- Správný výpočet součtu bodů pro měsíční výkaz pojišťovny.

---

## 📝 Deník vývoje a změn (Changelog)

- **2026-09-30:**
  - Výběr a analýza **Zadání A: Rezervace v ordinaci** (MUDr. Jana Hrubá, Ordinace Na Vyhlídce).
  - Vytvoření [AGENTS.md](file:///c:/Users/bleha/Desktop/PPRO/AGENTS.md) s pravidly vývoje, git politikou a ochranou proti neautorizovanému pushování.
  - Zpracování kompletní technické dokumentace a architektonických rozhodnutí do [README.md](file:///c:/Users/bleha/Desktop/PPRO/README.md) jako jediného zdroje pravdy.
  - Nastavení git pre-commit hooku pro kontrolu aktuálnosti dokumentace.
