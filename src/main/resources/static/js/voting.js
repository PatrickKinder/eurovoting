// ============================================================
// voting.js  –  Person 4 (Voting Page Logic)
// Ort: src/main/resources/static/js/voting.js
//
// Regeln laut Dokument:
// - buildVoteRequest() IMMER verwenden beim Stimmabgeben
// - Eigenes Land (DEU) aus Dropdown ausschließen (client-seitig)
// - Jury-Mitglied-Auswahl nur sichtbar, wenn voterRole === "JURY"
// - castVote() mit try/catch; bei Fehler: error.message anzeigen
// - Bei Erfolg: Bestätigung zeigen, Formular zurücksetzen
// - getVotingStatus() beim Laden UND nach jeder erfolgreichen Stimme
// ============================================================

// ---- DOM-Elemente holen ----
const contestantSelect  = document.getElementById("contestantSelect");
const voterRoleSelect   = document.getElementById("voterRoleSelect");
const jurySection       = document.getElementById("jurySection");
const juryMemberSelect  = document.getElementById("juryMemberSelect");
const pointsSelect      = document.getElementById("pointsSelect");
const voteForm          = document.getElementById("voteForm");
const messageBox        = document.getElementById("messageBox");
const statusBox         = document.getElementById("statusBox");

// ---- Hilfsfunktionen für Nachrichten ----
function showSuccess(text) {
    messageBox.textContent = text;
    messageBox.className = "message success";
}

function showError(text) {
    messageBox.textContent = text;
    messageBox.className = "message error";
}

function clearMessage() {
    messageBox.textContent = "";
    messageBox.className = "message";
}

// ---- Voting-Status anzeigen ----
async function updateVotingStatus() {
    try {
        const status = await getVotingStatus();

        const progress = (voter, count, complete, total) =>
            `<div class="status-row ${complete ? 'done' : ''}">
        <span>${voter}</span>
        <span>${count} / ${total} Stimmen ${complete ? "✓ Fertig" : ""}</span>
      </div>`;

        statusBox.innerHTML = `
      <h3>Abstimmungsstatus – ${status.ourCountryName}</h3>
      ${progress("Jury-Mitglied 1", status.juryMember1VoteCount, status.juryMember1Complete, status.totalRequiredPerSet)}
      ${progress("Jury-Mitglied 2", status.juryMember2VoteCount, status.juryMember2Complete, status.totalRequiredPerSet)}
      ${progress("Jury-Mitglied 3", status.juryMember3VoteCount, status.juryMember3Complete, status.totalRequiredPerSet)}
      ${progress("Publikum",        status.audienceVoteCount,    status.audienceComplete,    status.totalRequiredPerSet)}
    `;
    } catch (err) {
        statusBox.innerHTML = `<p class="error">Status konnte nicht geladen werden: ${err.message}</p>`;
    }
}

// ---- Kandidaten laden und Dropdown befüllen ----
async function loadContestants() {
    try {
        const contestants = await getContestants();

        // Eigenes Land (DEU) client-seitig herausfiltern (Regel aus Dokument)
        const filtered = contestants.filter(c => c.countryCode !== "DEU");

        contestantSelect.innerHTML = '<option value="">– Kandidat/in wählen –</option>';
        filtered.forEach(c => {
            const option = document.createElement("option");
            option.value       = c.id;
            option.textContent = `${c.countryName} – ${c.artistName} – „${c.songTitle}"`;
            contestantSelect.appendChild(option);
        });
    } catch (err) {
        showError("Kandidaten konnten nicht geladen werden: " + err.message);
    }
}

// ---- Jury-Bereich ein-/ausblenden ----
voterRoleSelect.addEventListener("change", () => {
    if (voterRoleSelect.value === "JURY") {
        jurySection.style.display = "block";
    } else {
        jurySection.style.display = "none";
        juryMemberSelect.value = "";
    }
    clearMessage();
});

// ---- Formular absenden (Stimme abgeben) ----
voteForm.addEventListener("submit", async (event) => {
    event.preventDefault();  // Seite nicht neu laden
    clearMessage();

    const toContestantId = parseInt(contestantSelect.value, 10);
    const voterRole      = voterRoleSelect.value;
    const points         = parseInt(pointsSelect.value, 10);
    const juryMemberId   = voterRole === "JURY" ? parseInt(juryMemberSelect.value, 10) : null;

    // Einfache Pflichtfeld-Prüfung im Frontend
    if (!toContestantId) { showError("Bitte wähle einen Kandidaten aus."); return; }
    if (!voterRole)       { showError("Bitte wähle Jury oder Publikum.");  return; }
    if (!points)          { showError("Bitte wähle eine Punktzahl.");       return; }
    if (voterRole === "JURY" && !juryMemberId) {
        showError("Bitte wähle ein Jury-Mitglied (1, 2 oder 3).");
        return;
    }

    try {
        // IMMER buildVoteRequest() verwenden (Regel aus Dokument)
        await castVote(toContestantId, voterRole, points, juryMemberId);

        showSuccess("✓ Stimme erfolgreich abgegeben!");
        voteForm.reset();
        jurySection.style.display = "none";

        // Status nach erfolgreicher Stimme aktualisieren (Regel aus Dokument)
        await updateVotingStatus();

    } catch (err) {
        // Fehlermeldung vom Server (error.message) anzeigen
        showError("Fehler: " + err.message);
    }
});

// ---- Seite initialisieren ----
(async function init() {
    await loadContestants();      // Kandidaten-Dropdown befüllen
    await updateVotingStatus();   // Status beim Laden anzeigen (Regel aus Dokument)
})();