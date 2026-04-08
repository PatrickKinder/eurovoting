document.addEventListener("DOMContentLoaded", () => {
    loadResults();
});

async function loadResults() {
    const tableBody = document.getElementById("resultsTableBody");
    const messageBox = document.getElementById("resultsMessage");

    tableBody.innerHTML = "";
    hideMessage(messageBox);

    try {
        const results = await getResults();

        if (!Array.isArray(results) || results.length === 0) {
            showMessage(messageBox, "No results available yet.", "info");
            return;
        }

        // WICHTIG:
        // Nicht sortieren, nicht totalPoints neu berechnen.
        // Backend garantiert die Reihenfolge.
        results.forEach((entry) => {
            const row = document.createElement("tr");

            if (entry.rank === 1) {
                row.classList.add("winner");
            }

            row.innerHTML = `
                <td class="rank-cell">#${entry.rank}</td>
                <td>
                    <div class="country-cell">
                        <span class="country-code">${escapeHtml(entry.countryCode)}</span>
                        <span class="country-name">${escapeHtml(entry.countryName)}</span>
                    </div>
                </td>
                <td>${escapeHtml(entry.artistName)}</td>
                <td>${escapeHtml(entry.songTitle)}</td>
                <td>${entry.juryPoints}</td>
                <td>${entry.audiencePoints}</td>
                <td class="total-cell">${entry.totalPoints}</td>
            `;

            tableBody.appendChild(row);
        });
    } catch (error) {
        showMessage(
            messageBox,
            error.message || "Failed to load results.",
            "error"
        );
    }
}

function showMessage(element, text, type) {
    element.textContent = text;
    element.className = `message ${type}`;
}

function hideMessage(element) {
    element.textContent = "";
    element.className = "message hidden";
}

function escapeHtml(value) {
    if (value === null || value === undefined) return "";
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}