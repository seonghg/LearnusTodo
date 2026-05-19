const gradeForm = document.querySelector("[data-grade-form]");
const gradeCard = document.querySelector("[data-grade-card]");
const gradeLetter = document.querySelector("[data-grade-letter]");
const gradeScore = document.querySelector("[data-grade-score]");
const gradeChart = document.querySelector("[data-grade-chart]");

const gradeItems = [
    { label: "중간고사", weight: "midtermWeight", score: "midtermScore" },
    { label: "기말고사", weight: "finalWeight", score: "finalScore" },
    { label: "과제", weight: "homeworkWeight", score: "homeworkScore" },
    { label: "발표", weight: "presentationWeight", score: "presentationScore" },
    { label: "출석", weight: "attendanceWeight", score: "attendanceScore" }
];

gradeForm?.addEventListener("submit", async (event) => {
    event.preventDefault();

    const data = Object.fromEntries(new FormData(gradeForm).entries());
    const payload = {
        midtermWeight: Number(data.midtermWeight),
        finalWeight: Number(data.finalWeight),
        homeworkWeight: Number(data.homeworkWeight),
        presentationWeight: Number(data.presentationWeight),
        attendanceWeight: Number(data.attendanceWeight),
        midtermScore: nullableNumber(data.midtermScore),
        finalScore: nullableNumber(data.finalScore),
        homeworkScore: nullableNumber(data.homeworkScore),
        presentationScore: nullableNumber(data.presentationScore),
        attendanceScore: nullableNumber(data.attendanceScore)
    };

    try {
        const response = await fetch("/api/grade/predict", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            throw new Error(await response.text());
        }

        const score = Number(await response.json());
        renderGradeResult(score);
        renderGradeChart(data);
    } catch (error) {
        gradeCard.className = "grade-score-card grade-f";
        gradeLetter.textContent = "!";
        gradeScore.textContent = `계산 실패: ${error.message}`;
        gradeChart.innerHTML = `<p class="empty-state">입력값을 확인한 뒤 다시 계산해주세요.</p>`;
    }
});

function renderGradeResult(score) {
    const grade = getLetterGrade(score);

    gradeCard.className = `grade-score-card grade-${grade.toLowerCase()}`;
    gradeLetter.textContent = grade;
    gradeScore.textContent = `(${formatScore(score)}/100)`;
}

function renderGradeChart(data) {
    const rows = gradeItems.map((item) => {
        const weight = Number(data[item.weight]);
        const score = nullableNumber(data[item.score]) ?? 0;
        const contribution = score * weight / 100;

        return {
            label: item.label,
            weight,
            score,
            contribution
        };
    });

    gradeChart.innerHTML = `
        ${renderTrendChart(rows)}
        <div class="grade-contribution-list">
            ${renderContributionRows(rows)}
        </div>
    `;
}

function renderTrendChart(rows) {
    const width = 620;
    const height = 190;
    const padding = { top: 28, right: 18, bottom: 42, left: 34 };
    const chartWidth = width - padding.left - padding.right;
    const chartHeight = height - padding.top - padding.bottom;
    const points = rows.map((row, index) => {
        const x = rows.length === 1
            ? padding.left + chartWidth / 2
            : padding.left + (chartWidth * index / (rows.length - 1));
        const y = padding.top + chartHeight - (clamp(row.score, 0, 100) / 100 * chartHeight);

        return { ...row, x, y };
    });
    const linePoints = points.map((point) => `${point.x},${point.y}`).join(" ");
    const areaPoints = [
        `${padding.left},${padding.top + chartHeight}`,
        linePoints,
        `${padding.left + chartWidth},${padding.top + chartHeight}`
    ].join(" ");
    const trend = getTrendSummary(rows);

    return `
        <div class="grade-trend">
            <div class="grade-trend-summary">
                <strong>${trend.title}</strong>
                <span>${trend.detail}</span>
            </div>
            <svg class="grade-line-chart" viewBox="0 0 ${width} ${height}" role="img" aria-label="항목별 성적 선 그래프">
                <line class="grade-line-grid" x1="${padding.left}" y1="${padding.top}" x2="${padding.left + chartWidth}" y2="${padding.top}"></line>
                <line class="grade-line-grid" x1="${padding.left}" y1="${padding.top + chartHeight / 2}" x2="${padding.left + chartWidth}" y2="${padding.top + chartHeight / 2}"></line>
                <line class="grade-line-grid" x1="${padding.left}" y1="${padding.top + chartHeight}" x2="${padding.left + chartWidth}" y2="${padding.top + chartHeight}"></line>
                <polygon class="grade-line-area" points="${areaPoints}"></polygon>
                <polyline class="grade-line-path" points="${linePoints}"></polyline>
                ${points.map((point) => `
                    <circle class="grade-line-point" cx="${point.x}" cy="${point.y}" r="5"></circle>
                    <text class="grade-line-score" x="${point.x}" y="${Math.max(14, point.y - 10)}" text-anchor="middle">${formatScore(point.score)}</text>
                    <text class="grade-line-label" x="${point.x}" y="${height - 14}" text-anchor="middle">${point.label}</text>
                `).join("")}
            </svg>
        </div>
    `;
}

function getTrendSummary(rows) {
    const first = rows[0]?.score ?? 0;
    const last = rows[rows.length - 1]?.score ?? 0;
    const diff = last - first;

    if (Math.abs(diff) < 3) {
        return {
            title: "유지세",
            detail: `첫 항목 대비 ${formatScore(Math.abs(diff))}점 차이로 안정적인 흐름입니다.`
        };
    }

    if (diff > 0) {
        return {
            title: "성장세",
            detail: `첫 항목 대비 ${formatScore(diff)}점 상승했습니다.`
        };
    }

    return {
        title: "하향세",
        detail: `첫 항목 대비 ${formatScore(Math.abs(diff))}점 낮아졌습니다.`
    };
}

function renderContributionRows(rows) {
    return rows.map((row) => `
        <div class="grade-chart-row">
            <div class="grade-chart-meta">
                <strong>${row.label}</strong>
                <span>${formatScore(row.score)}점 · ${formatScore(row.weight)}% · +${formatScore(row.contribution)}</span>
            </div>
            <div class="grade-bar-track" aria-label="${row.label} ${formatScore(row.score)}점">
                <span class="grade-bar" style="width: ${clamp(row.score, 0, 100)}%"></span>
            </div>
        </div>
    `).join("");
}

function getLetterGrade(score) {
    if (score >= 90) return "A";
    if (score >= 80) return "B";
    if (score >= 70) return "C";
    if (score >= 60) return "D";
    return "F";
}

function formatScore(value) {
    const number = Number(value);
    if (!Number.isFinite(number)) {
        return "0.0";
    }
    return number.toFixed(1);
}

function clamp(value, min, max) {
    return Math.min(Math.max(Number(value) || 0, min), max);
}

function nullableNumber(value) {
    if (value === null || value === undefined || value === "") {
        return null;
    }
    return Number(value);
}
