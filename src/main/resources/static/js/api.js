// ============================================================
// api.js  –  Person 4 (Shared API Layer)
// Ort: src/main/resources/static/js/api.js
//
// WICHTIG: Diese Datei wird auch von Person 5 (getResults)
// genutzt. Funktionsnamen und Parameter NIEMALS ändern!
// ============================================================

// Schalter: true = Mock-Daten, false = echtes Backend
// Auf false setzen, sobald Person 1 ihren Checklist bestanden hat.
const USE_MOCK = true;

const BASE_URL = "http://localhost:8080/api";

// ----------------------------------------------------------
// buildVoteRequest
// Baut das JSON-Objekt für POST /api/votes.
// MUSS immer verwendet werden – niemals manuell bauen!
// Regel: bei AUDIENCE wird juryMemberId IMMER auf null gesetzt.
// ----------------------------------------------------------
function buildVoteRequest(toContestantId, voterRole, points, juryMemberId) {
    return {
        toContestantId: toContestantId,
        voterRole:      voterRole,
        points:         points,
        juryMemberId:   voterRole === "AUDIENCE" ? null : juryMemberId
    };
}

// ----------------------------------------------------------
// getContestants
// Lädt alle Kandidaten (ContestantResponse[]).
// USE_MOCK=true  → liest mock-contestants.json
// USE_MOCK=false → GET /api/contestants
// ----------------------------------------------------------
async function getContestants() {
    if (USE_MOCK) {
        const response = await fetch("./js/mock-contestants.json");
        return response.json();
    }
    const response = await fetch(`${BASE_URL}/contestants`);
    return response.json();
}

// ----------------------------------------------------------
// castVote
// Gibt eine Stimme ab (VoteResponse).
// USE_MOCK=true  → gibt gefälschte Antwort zurück, loggt in Konsole
// USE_MOCK=false → POST /api/votes
// Bei HTTP-Fehler: wirft Error mit serverResponse.message
// ----------------------------------------------------------
async function castVote(toContestantId, voterRole, points, juryMemberId) {
    if (USE_MOCK) {
        const fakeResponse = {
            id:              Math.floor(Math.random() * 1000),
            fromCountryName: "Germany",
            toArtistName:    "Mock Artist",
            toSongTitle:     "Mock Song",
            toCountryName:   "Mock Country",
            voterRole:       voterRole,
            points:          points,
            juryMemberId:    voterRole === "AUDIENCE" ? null : juryMemberId,
            createdAt:       new Date().toISOString()
        };
        console.log("[MOCK] castVote aufgerufen:", buildVoteRequest(toContestantId, voterRole, points, juryMemberId));
        console.log("[MOCK] Antwort:", fakeResponse);
        return fakeResponse;
    }

    const body = buildVoteRequest(toContestantId, voterRole, points, juryMemberId);
    const response = await fetch(`${BASE_URL}/votes`, {
        method:  "POST",
        headers: { "Content-Type": "application/json" },
        body:    JSON.stringify(body)
    });

    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message);
    }
    return response.json();
}

// ----------------------------------------------------------
// getVotingStatus
// Gibt den aktuellen Abstimmungsstatus zurück (VotingStatusResponse).
// USE_MOCK=true  → gibt fest codierten Status zurück
// USE_MOCK=false → GET /api/votes/status
// ----------------------------------------------------------
async function getVotingStatus() {
    if (USE_MOCK) {
        return {
            ourCountryId:        11,
            ourCountryName:      "Germany",
            totalRequiredPerSet: 10,
            juryMember1VoteCount: 0,
            juryMember2VoteCount: 0,
            juryMember3VoteCount: 0,
            audienceVoteCount:    0,
            juryMember1Complete:  false,
            juryMember2Complete:  false,
            juryMember3Complete:  false,
            audienceComplete:     false
        };
    }
    const response = await fetch(`${BASE_URL}/votes/status`);
    return response.json();
}

// ----------------------------------------------------------
// getResults
// Lädt das aktuelle Ranking (RankingEntry[]).
// HINWEIS: Diese Funktion wird von Person 5 (results.js) genutzt!
// Signatur und Name dürfen NIEMALS geändert werden.
// USE_MOCK=true  → liest mock-results.json (erstellt von Person 5)
// USE_MOCK=false → GET /api/results
// ----------------------------------------------------------
async function getResults() {
    if (USE_MOCK) {
        const response = await fetch("./js/mock-results.json");
        return response.json();
    }
    const response = await fetch(`${BASE_URL}/results`);
    return response.json();
}