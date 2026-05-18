const form = document.querySelector("[data-app-login-form]");
const statusBox = document.querySelector("[data-login-status]");

const params = new URLSearchParams(location.search);
if (params.has("error")) {
    statusBox.textContent = "아이디 또는 비밀번호를 확인해주세요.";
}

form?.addEventListener("submit", () => {
    const formData = new FormData(form);
    const userId = String(formData.get("username") || "").trim();

    if (userId) {
        setUserId(userId);
    }
});
