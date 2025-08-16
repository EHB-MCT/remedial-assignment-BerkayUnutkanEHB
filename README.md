# ⚽ TransferSim – Football Economy Simulation

Een backend-gedreven voetbal-economie simulatie gebouwd met **Spring Boot 3** en **MongoDB**.  
Er is **geen frontend** — interactie verloopt via **Postman** of **cURL**.

## 📌 Functies

- **Clubs**: budget, ticket- en sponsorinkomsten (persistent in MongoDB)
- **Spelers**: marktwaarde + club (persistent in MongoDB)
- **Simulatie-tick**:
  - Marktwaardes fluctueren (random binnen ingestelde bandbreedte)
  - Clubs ontvangen inkomsten per tick (ticket + sponsor)
  - Willekeurige transfers (0–N per tick) op basis van budget en marktwaarde
- **Transfers**: volledige geschiedenis/overzicht via API
- **Admin normalisatie**: snel budgetten/waardes clampen naar realistische ranges
- **(Optioneel) Auto-tick** via `@Scheduled` met instelbare delay

---

## 🚀 Installatie & Starten

### Vereisten
- **Java 21** (of 22, getest met JDK 22)
- **Maven 3.9+**
- **MongoDB** (lokaal of Atlas/extern)
- **Postman** (aanbevolen) of **cURL**


## Project clonen & runnen
git clone <JOUW_GITHUB_REPO_URL>
cd EconomySimulation
export MONGODB_URI="mongodb://localhost:27017/transfersim"
mvn clean spring-boot:run

## ⚙️ Configuratie (application.properties)
# --- simulatie toggles ---
app.sim.auto=false                 # auto-tick aan/uit
app.sim.fixedDelayMs=10000         # interval auto-tick (ms)
app.sim.applyIncome=true           # clubs krijgen inkomsten per tick
app.sim.transferEnabled=true       # transfers genereren per tick

# --- marktwaarde schommeling ---
app.sim.factor.min=0.99            # min multiplicator per tick
app.sim.factor.max=1.01            # max multiplicator per tick

# --- waardes onder/bovergrens ---
app.sim.minPlayerValue=500000      # hard minimum
app.sim.maxPlayerValue=200000000   # 'cap' (gebruik soft cap hieronder)

# --- zachte cap boven max (aanbevolen) ---
app.sim.softCap.enabled=true       # i.p.v. hard naar cap te 'teleporteren'
app.sim.softCap.k=0.5              # 0..1 deel van overshoot behouden per tick

# --- transfers ---
app.sim.maxTransfersPerTick=3
app.sim.transfer.mult.min=0.9      # fee ~ marketValue * random(min..max)
app.sim.transfer.mult.max=1.1


### 📡 Belangrijkste API Endpoints

## Simulatie
POST /api/tick/manual → voer 1 simulatie-tick uit
GET /api/players → lijst spelers
GET /api/clubs → lijst clubs

## Transfers
GET /api/transfers → alle transfers
GET /api/transfers/recent?limit=10 → laatste transfers
GET /api/transfers/byClub/{clubName} → transfers per club

## Admin normalisatie
POST /api/admin/players/normalize?min=1000000&max=200000000
POST /api/admin/clubs/normalize?min=50000000&max=800000000

### MongoDB lokaal starten
```bash
mongod --dbpath /pad/naar/jouw/datafolder
# (optioneel) eerste keer in mongo shell:
# use transfersim
