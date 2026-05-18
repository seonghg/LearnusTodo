const profileForm = document.querySelector("[data-profile-form]");
const profileStatus = document.querySelector("[data-profile-status]");
const deleteButton = document.querySelector("[data-delete-account]");
const deleteStatus = document.querySelector("[data-delete-status]");

loadProfile();
loadScheduleSummary();

profileForm?.addEventListener("submit", async (event) => {
    event.preventDefault();

    profileStatus.textContent = "사용자 정보를 수정하는 중입니다.";

    try {
        const response = await fetch("/api/mypage/profile", {
            method: "PUT",
            body: new URLSearchParams(new FormData(profileForm))
        });
        const result = await response.json();

        profileStatus.textContent = result.message || "수정이 완료되었습니다.";

        if (response.ok && result.success) {
            profileForm.newPassword.value = "";
            await loadProfile();
        }
    } catch (error) {
        profileStatus.textContent = `수정 중 오류가 발생했습니다: ${error.message}`;
    }
});

deleteButton?.addEventListener("click", async () => {
    const confirmed = confirm("정말 계정을 삭제할까요? 이 작업은 되돌릴 수 없습니다.");
    if (!confirmed) return;

    deleteStatus.textContent = "계정을 삭제하는 중입니다.";

    try {
        const response = await fetch("/api/mypage/profile", {
            method: "DELETE"
        });
        const result = await response.json();

        if (!response.ok || !result.success) {
            deleteStatus.textContent = result.message || "계정 삭제에 실패했습니다.";
            return;
        }

        localStorage.removeItem("learntodo.userId");
        location.href = "login.html";
    } catch (error) {
        deleteStatus.textContent = `계정 삭제 중 오류가 발생했습니다: ${error.message}`;
    }
});

async function loadProfile() {
    try {
        const data = await fetchJson("/api/mypage/profile");
        const profile = data.profile || {};

        setText("[data-profile-user-id]", profile.user_id);
        setText("[data-profile-user-name]", profile.user_name);
        setText("[data-profile-department]", profile.department);
        setText("[data-profile-id-number]", profile.id_number);
        setText("[data-profile-grade]", profile.grade);
        setText("[data-profile-email]", profile.email_address || "-");
        setText("[data-profile-created-at]", formatDateTime(profile.create_time));

        profileForm.userName.value = profile.user_name || "";
        profileForm.department.value = profile.department || "";
        profileForm.idNumber.value = profile.id_number || "";
        profileForm.grade.value = profile.grade || "";
        profileForm.emailAddress.value = profile.email_address || "";
    } catch (error) {
        profileStatus.textContent = `내 정보를 불러오지 못했습니다: ${error.message}`;
    }
}

async function loadScheduleSummary() {
    const list = document.querySelector("[data-summary-events]");

    try {
        const summary = await fetchJson("/api/mypage/schedule-summary");

        setText("[data-summary-total]", summary.totalEvents ?? 0);
        setText("[data-summary-today]", summary.todayEvents ?? 0);
        setText("[data-summary-upcoming]", summary.upcomingEvents ?? 0);

        const events = summary.nextEvents || [];
        if (!events.length) {
            list.innerHTML = `<li class="empty-state">다가오는 일정이 없습니다.</li>`;
            return;
        }

        list.innerHTML = events.map((event) => `
            <li class="data-item">
                <div>
                    <strong>${escapeHtml(event.title)}</strong>
                    <span>${formatDateTime(event.start_time)} · ${escapeHtml(event.course_name || "LearnUs")}</span>
                </div>
            </li>
        `).join("");
    } catch (error) {
        list.innerHTML = `<li class="empty-state">일정 요약을 불러오지 못했습니다: ${escapeHtml(error.message)}</li>`;
    }
}

function setText(selector, value) {
    const element = document.querySelector(selector);
    if (element) {
        element.textContent = value ?? "-";
    }
}
