const STORAGE_USER_ID = "learntodo.userId";

function getUserId() {
    return localStorage.getItem(STORAGE_USER_ID) || "";
}

function setUserId(userId) {
    localStorage.setItem(STORAGE_USER_ID, userId);
}

function requireUserId() {
    const userId = getUserId();
    if (!userId) {
        location.href = "login.html";
    }
    return userId;
}

async function fetchJson(url) {
    const response = await fetch(url);
    if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
    }
    return response.json();
}

function formToJson(form) {
    return Object.fromEntries(new FormData(form).entries());
}

function jsonRequestOptions(method, body) {
    return {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
    };
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

function formatDateTime(value) {
    if (!value) return "-";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return String(value);
    return new Intl.DateTimeFormat("ko-KR", {
        month: "short",
        day: "numeric",
        weekday: "short",
        hour: "2-digit",
        minute: "2-digit"
    }).format(date);
}
