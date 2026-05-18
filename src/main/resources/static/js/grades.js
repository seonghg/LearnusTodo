const gradeForm = document.querySelector("[data-grade-form]");
const gradeResult = document.querySelector("[data-grade-result]");

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

        const score = await response.json();
        gradeResult.textContent = `예상 최종 점수: ${Number(score).toFixed(2)}점`;
    } catch (error) {
        gradeResult.textContent = `계산 실패: ${error.message}`;
    }
});

function nullableNumber(value) {
    if (value === null || value === undefined || value === "") {
        return null;
    }
    return Number(value);
}
