const registerForm = document.querySelector("[data-register-form]");
const registerStatus = document.querySelector("[data-register-status]");

registerForm?.addEventListener("submit", async (event) => {
    event.preventDefault();

    const formData = new FormData(registerForm);
    const password = String(formData.get("password") || "");
    const passwordConfirm = String(formData.get("passwordConfirm") || "");
    const userId = String(formData.get("userId") || "").trim();

    if (password !== passwordConfirm) {
        registerStatus.textContent = "비밀번호가 일치하지 않습니다.";
        return;
    }

    formData.delete("passwordConfirm");
    registerStatus.textContent = "회원가입을 처리하는 중입니다.";

    try {
        const response = await fetch("/api/auth/register", {
            ...jsonRequestOptions("POST", Object.fromEntries(formData.entries()))
        });

        const result = await response.json();

        if (!response.ok || !result.success) {
            registerStatus.textContent = result.message || "회원가입에 실패했습니다.";
            return;
        }

        setUserId(userId);
        registerStatus.textContent = "회원가입이 완료되었습니다. 로그인 화면으로 이동합니다.";

        setTimeout(() => {
            location.href = "login.html";
        }, 900);
    } catch (error) {
        registerStatus.textContent = `회원가입 중 오류가 발생했습니다: ${error.message}`;
    }
});
