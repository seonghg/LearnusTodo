const todoForm = document.querySelector("[data-todo-form]");
const courseSelect = document.querySelector("[data-course-select]");
const todoStatus = document.querySelector("[data-todo-status]");
const todoEventList = document.querySelector("[data-todo-event-list]");

loadCourses();
loadEvents();

todoEventList?.addEventListener("click", async (event) => {
    const deleteButton = event.target.closest("[data-delete-todo-id]");
    if (!deleteButton) return;

    const eventId = deleteButton.dataset.deleteTodoId;
    if (!eventId) return;

    deleteButton.disabled = true;
    todoStatus.textContent = "일정을 삭제하는 중입니다.";

    try {
        const response = await fetch(`/api/todos/${encodeURIComponent(eventId)}`, {
            method: "DELETE"
        });
        const result = await response.json();

        todoStatus.textContent = result.message || "처리가 완료되었습니다.";

        if (response.ok && result.success) {
            await loadEvents();
        } else {
            deleteButton.disabled = false;
        }
    } catch (error) {
        deleteButton.disabled = false;
        todoStatus.textContent = `일정 삭제 중 오류가 발생했습니다: ${error.message}`;
    }
});

todoForm?.addEventListener("submit", async (event) => {
    event.preventDefault();

    todoStatus.textContent = "일정을 추가하는 중입니다.";

    try {
        const response = await fetch("/api/todos", {
            ...jsonRequestOptions("POST", formToJson(todoForm))
        });
        const result = await response.json();

        todoStatus.textContent = result.message || "처리가 완료되었습니다.";

        if (response.ok && result.success) {
            todoForm.reset();
            setDefaultDueDate();
            await loadEvents();
        }
    } catch (error) {
        todoStatus.textContent = `일정 추가 중 오류가 발생했습니다: ${error.message}`;
    }
});

async function loadCourses() {
    try {
        const courses = await fetchJson("/api/courses");
        const personalOption = `<option value="PERSONAL">개인 일정</option>`;

        if (!courses.length) {
            courseSelect.innerHTML = personalOption;
            setDefaultDueDate();
            return;
        }

        courseSelect.innerHTML = personalOption + courses.map((course) => `
            <option value="${course.course_id}">
                ${escapeHtml(course.course_name)}${course.course_code ? ` (${escapeHtml(course.course_code)})` : ""}
            </option>
        `).join("");

        setDefaultDueDate();
    } catch (error) {
        courseSelect.innerHTML = `<option value="">과목을 불러오지 못했습니다.</option>`;
        todoStatus.textContent = `과목 조회 실패: ${error.message}`;
    }
}

async function loadEvents() {
    try {
        const events = await fetchJson("/api/calendar");
        const now = new Date();
        const upcoming = events
            .filter((event) => new Date(event.start_time) >= now || event.source_type === "USER_TODO")
            .sort((a, b) => {
                const aTodo = a.source_type === "USER_TODO" ? 0 : 1;
                const bTodo = b.source_type === "USER_TODO" ? 0 : 1;
                if (aTodo !== bTodo) return aTodo - bTodo;
                return new Date(a.start_time) - new Date(b.start_time);
            })
            .slice(0, 8);

        if (!upcoming.length) {
            todoEventList.innerHTML = `<li class="empty-state">등록된 일정이 없습니다.</li>`;
            return;
        }

        todoEventList.innerHTML = upcoming.map((event) => `
            <li class="data-item todo-event-item">
                <div>
                    <strong>${escapeHtml(event.title)}</strong>
                    <span>${formatDateTime(event.start_time)} · ${escapeHtml(event.course_name || "개인 일정")}</span>
                </div>
                ${event.source_type === "USER_TODO" ? `
                    <button class="btn btn-danger btn-compact" type="button" data-delete-todo-id="${escapeHtml(event.event_id)}">
                        삭제
                    </button>
                ` : ""}
            </li>
        `).join("");
    } catch (error) {
        todoEventList.innerHTML = `<li class="empty-state">일정을 불러오지 못했습니다: ${escapeHtml(error.message)}</li>`;
    }
}

function setDefaultDueDate() {
    const input = todoForm?.elements.dueDate;
    if (!input || input.value) return;

    const date = new Date();
    date.setHours(date.getHours() + 1, 0, 0, 0);
    input.value = toDateTimeLocal(date);
}

function toDateTimeLocal(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const hour = String(date.getHours()).padStart(2, "0");
    const minute = String(date.getMinutes()).padStart(2, "0");
    return `${year}-${month}-${day}T${hour}:${minute}`;
}
