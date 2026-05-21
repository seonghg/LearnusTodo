const userNameEl = document.querySelector("[data-user-id]");
const courseCountEl = document.querySelector("[data-course-count]");
const eventCountEl = document.querySelector("[data-event-count]");
const avgProgressEl = document.querySelector("[data-average-progress]");
const courseListEl = document.querySelector("[data-course-list]");
const eventListEl = document.querySelector("[data-event-list]");
const calendarBodyEl = document.querySelector("[data-calendar-body]");
const calendarMonthLabelEl = document.querySelector("[data-calendar-month-label]");
const calendarPrevButton = document.querySelector("[data-calendar-prev]");
const calendarTodayButton = document.querySelector("[data-calendar-today]");
const calendarNextButton = document.querySelector("[data-calendar-next]");
const syncForm = document.querySelector("[data-learnus-sync-form]");
const syncStatus = document.querySelector("[data-sync-status]");
let dashboardEvents = [];
let visibleCalendarDate = new Date();
let calendarEventsByDate = new Map();
let activeCalendarPopover = null;

loadDashboard();

document.addEventListener("click", (event) => {
    if (!activeCalendarPopover) return;
    if (
        activeCalendarPopover.contains(event.target)
        || event.target.closest("[data-calendar-more]")
    ) {
        return;
    }
    closeCalendarPopover();
});

document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") {
        closeCalendarPopover();
    }
});

calendarBodyEl?.addEventListener("click", (event) => {
    const moreButton = event.target.closest("[data-calendar-more]");
    if (!moreButton) return;

    event.stopPropagation();
    const dateKey = moreButton.dataset.calendarMore;
    const dayEvents = calendarEventsByDate.get(dateKey) || [];
    showCalendarPopover(moreButton, dateKey, dayEvents);
});

calendarPrevButton?.addEventListener("click", () => {
    visibleCalendarDate = new Date(
        visibleCalendarDate.getFullYear(),
        visibleCalendarDate.getMonth() - 1,
        1
    );
    renderCalendar(dashboardEvents);
});

calendarTodayButton?.addEventListener("click", () => {
    visibleCalendarDate = new Date();
    renderCalendar(dashboardEvents);
});

calendarNextButton?.addEventListener("click", () => {
    visibleCalendarDate = new Date(
        visibleCalendarDate.getFullYear(),
        visibleCalendarDate.getMonth() + 1,
        1
    );
    renderCalendar(dashboardEvents);
});

syncForm?.addEventListener("submit", async (event) => {
    event.preventDefault();

    const formData = new FormData(syncForm);
    const learnusId = String(formData.get("id") || "").trim();
    const password = String(formData.get("password") || "");

    if (!learnusId || !password) {
        syncStatus.textContent = "LearnUs 아이디와 비밀번호를 모두 입력해주세요.";
        return;
    }

    syncStatus.textContent = "LearnUs 정보를 동기화하는 중입니다.";

    try {
        const response = await fetch("/todo/api/learnus_con", {
            ...jsonRequestOptions("POST", { id: learnusId, password })
        });
        const result = await response.json();

        if (!response.ok || !result.success) {
            syncStatus.textContent = "동기화에 실패했습니다. LearnUs 로그인 정보나 파싱 결과를 확인해주세요.";
            return;
        }

        syncStatus.textContent = "동기화가 완료되었습니다.";
        syncForm.reset();
        await loadDashboard();
    } catch (error) {
        syncStatus.textContent = `동기화 중 오류가 발생했습니다: ${error.message}`;
    }
});

async function loadDashboard() {
    try {
        const data = await fetchJson("/api/dashboard");
        const userId = data.userId || getUserId() || "사용자";
        setUserId(userId);
        userNameEl.textContent = userId;
        renderSummary(data);
        renderCourses(data.courses || []);
        dashboardEvents = data.events || [];
        renderEvents(dashboardEvents);
        renderCalendar(dashboardEvents);
    } catch (error) {
        if (String(error.message).includes("401") || String(error.message).includes("403")) {
            location.href = "login.html";
            return;
        }
        courseListEl.innerHTML = `<li class="empty-state">데이터를 불러오지 못했습니다: ${escapeHtml(error.message)}</li>`;
    }
}

function renderSummary(data) {
    const courses = data.courses || [];
    const rates = courses
        .map((course) => Number(course.learning_rate))
        .filter((rate) => !Number.isNaN(rate));
    const average = rates.length
        ? Math.round(rates.reduce((sum, rate) => sum + rate, 0) / rates.length)
        : 0;

    courseCountEl.textContent = data.courseCount ?? courses.length;
    eventCountEl.textContent = data.eventCount ?? 0;
    avgProgressEl.textContent = `${average}%`;
}

function renderCourses(courses) {
    if (!courses.length) {
        courseListEl.innerHTML = `<li class="empty-state">저장된 강의가 없습니다. 위의 LearnUs 동기화를 먼저 실행해주세요.</li>`;
        return;
    }

    courseListEl.innerHTML = courses.map((course) => `
        <li class="data-item">
            <div>
                <strong>${escapeHtml(course.course_name)}</strong>
                <span>${escapeHtml(course.course_code || "코드 없음")} · ${escapeHtml(course.professor_name || "교수 정보 없음")}</span>
            </div>
            <span class="badge">${course.learning_rate ?? 0}%</span>
        </li>
    `).join("");
}

function renderEvents(events) {
    const now = new Date();
    const upcoming = events
        .filter((event) => new Date(event.start_time) >= now)
        .slice(0, 8);

    if (!upcoming.length) {
        eventListEl.innerHTML = `<li class="empty-state">저장된 일정이 없습니다.</li>`;
        return;
    }

    eventListEl.innerHTML = upcoming.map((event) => `
        <li class="data-item">
            <div>
                <strong>${escapeHtml(event.title)}</strong>
                <span>${formatDateTime(event.start_time)} · ${escapeHtml(event.course_name || "개인 일정")}</span>
            </div>
        </li>
    `).join("");
}

function renderCalendar(events) {
    closeCalendarPopover();
    const year = visibleCalendarDate.getFullYear();
    const month = visibleCalendarDate.getMonth();
    const firstDay = new Date(year, month, 1);
    const start = new Date(year, month, 1 - firstDay.getDay());

    if (calendarMonthLabelEl) {
        calendarMonthLabelEl.textContent = `${year}. ${String(month + 1).padStart(2, "0")}`;
    }

    calendarEventsByDate = new Map();
    for (const event of events) {
        const date = new Date(event.start_time);
        if (Number.isNaN(date.getTime())) continue;
        const key = toDateKey(date);
        const bucket = calendarEventsByDate.get(key) || [];
        bucket.push(event);
        calendarEventsByDate.set(key, bucket);
    }

    const rows = [];
    for (let week = 0; week < 6; week++) {
        const cells = [];
        for (let day = 0; day < 7; day++) {
            const date = new Date(start);
            date.setDate(start.getDate() + week * 7 + day);
            const key = toDateKey(date);
            const dayEvents = calendarEventsByDate.get(key) || [];
            const muted = date.getMonth() !== month ? " muted" : "";
            cells.push(`
                <td class="${muted}">
                    <span class="day-number">${date.getDate()}</span>
                    ${renderCalendarDayEvents(dayEvents, key)}
                </td>
            `);
        }
        rows.push(`<tr>${cells.join("")}</tr>`);
    }

    calendarBodyEl.innerHTML = rows.join("");
}

function renderCalendarDayEvents(dayEvents, dateKey) {
    const visibleEvents = dayEvents.slice(0, 2);
    const hiddenCount = dayEvents.length - visibleEvents.length;
    const eventItems = visibleEvents.map((event) => {
        const fullText = formatCalendarEventDetail(event);
        return `<div class="event" title="${escapeHtml(fullText)}" data-full-title="${escapeHtml(fullText)}">${escapeHtml(event.title)}</div>`;
    });

    if (hiddenCount > 0) {
        eventItems.push(`
            <button class="event event-more" type="button" data-calendar-more="${escapeHtml(dateKey)}" aria-expanded="false">
                +${hiddenCount} more
            </button>
        `);
    }

    return eventItems.join("");
}

function showCalendarPopover(anchor, dateKey, dayEvents) {
    closeCalendarPopover();

    anchor.setAttribute("aria-expanded", "true");
    const popover = document.createElement("div");
    popover.className = "calendar-popover";
    popover.setAttribute("role", "dialog");
    popover.innerHTML = `
        <div class="calendar-popover-header">
            <strong>${escapeHtml(dateKey)}</strong>
            <button class="calendar-popover-close" type="button" aria-label="Close">x</button>
        </div>
        <div class="calendar-popover-list">
            ${dayEvents.map((event) => `
                <article class="calendar-popover-item">
                    <strong>${escapeHtml(event.title)}</strong>
                    <span>${formatDateTime(event.start_time)} · ${escapeHtml(event.course_name || "개인 일정")}</span>
                </article>
            `).join("")}
        </div>
    `;

    document.body.appendChild(popover);
    activeCalendarPopover = popover;

    const anchorRect = anchor.getBoundingClientRect();
    const popoverRect = popover.getBoundingClientRect();
    const gap = 8;
    const left = Math.min(
        Math.max(anchorRect.left, gap),
        window.innerWidth - popoverRect.width - gap
    );
    const top = Math.min(
        Math.max(anchorRect.bottom + gap, gap),
        window.innerHeight - popoverRect.height - gap
    );

    popover.style.left = `${left}px`;
    popover.style.top = `${top}px`;

    popover.querySelector(".calendar-popover-close")?.addEventListener("click", () => {
        anchor.setAttribute("aria-expanded", "false");
        closeCalendarPopover();
    });
}

function closeCalendarPopover() {
    document
        .querySelectorAll("[data-calendar-more][aria-expanded='true']")
        .forEach((button) => button.setAttribute("aria-expanded", "false"));
    activeCalendarPopover?.remove();
    activeCalendarPopover = null;
}

function formatCalendarEventDetail(event) {
    return `${event.title}\n${formatDateTime(event.start_time)} · ${event.course_name || "개인 일정"}`;
}

function toDateKey(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
}
