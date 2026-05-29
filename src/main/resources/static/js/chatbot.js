(function () {
    if (!document.querySelector(".app-container")) {
        return;
    }

    const systemPrompt = `당신은 연세대학교 소프트웨어학부 22학번 학생을 위한 학사 및 졸업요건 안내 챗봇입니다.
아래 졸업요건 정보를 기반으로만 정확하게 답변하세요.
관련 없는 질문에는 "학사 관련 질문만 답변 가능합니다"라고 안내하세요.

[졸업요건 핵심 정보]
1. 졸업 총 이수학점: 135학점 이상
2. 전공 최소 이수학점: 기본전공 36학점, 심화전공 36학점
3. 전공필수 과목: 데이터구조론(SWE2001), 알고리즘기초(SWE2007), 운영체제(SWE3001), 소프트웨어공학(SWE3013)
4. 교양기초: 22학점
5. 전공탐색: 21학점
6. 대학교양: 8개 영역 이상 이수
7. 3000단위 이상 과목: 45학점 이상
8. 인증요건: 영어인증 필수 + 정보인증 또는 산업실무역량인증 중 하나
9. 2개 이상의 전공 이수 필수
10. 트랙: AI빅데이터, AI미디어, AI계산과학, 스마트IoT, 정보보호 중 선택 가능
11. SWE2006 기초데이터구조, SWE2014 기초알고리즘, SWE2015 기초프로그래밍은 기본전공 학점으로 불인정
12. 영어인증은 교양영어 4개 학기 이수 후 아래 조건 중 하나를 만족해야 합니다.
- 교양영어 4개 학기 평량평균 A- 이상
- 입학 후 응시한 공인어학시험에서 전공별 기준 점수 통과
- 영어 교양선택 2과목 이상 이수 및 성적 B- 이상
13. 정보인증 또는 산업실무역량인증은 아래 조건 중 하나를 만족해야 합니다.
- 학과 지정 등급 자격증 1개 이상 취득
- 미래평생교육원 블록체인 강좌 수료
- 산업실무역량 인증 교과목 이수 또는 프로그램 참여로 200포인트 이상 취득
14. 1전공 소프트웨어학부 + 2전공 소프트웨어학부 심화 중복 인정:
- 공통 교과목은 최대 7학점까지만 중복 인정
- 졸업이수학점에는 중복 인정 불가
- 7학점을 채우려면 SW튜터, SW인턴십 같은 1학점 과목만 인정
- 1학점 과목 미이수 시 최대 중복 인정은 6학점
- 전공은 자유롭게 이수 권장`;

    const messages = [{ role: "system", content: systemPrompt }];

    const panel = document.createElement("section");
    panel.className = "chatbot-panel";
    panel.setAttribute("aria-label", "학사 챗봇");
    panel.hidden = true;
    panel.innerHTML = `
        <div class="chatbot-panel-header">
            <div>
                <strong>학사 챗봇</strong>
                <span>졸업요건을 물어보세요</span>
            </div>
            <button class="chatbot-close" type="button" aria-label="챗봇 닫기">x</button>
        </div>
        <div class="chatbot-messages" aria-live="polite">
            <div class="chatbot-message bot">안녕하세요. 졸업요건, 전공필수, 인증요건을 질문해 주세요.</div>
        </div>
        <form class="chatbot-form">
            <input class="chatbot-input" type="text" placeholder="질문 입력" autocomplete="off">
            <button class="chatbot-send" type="submit">전송</button>
        </form>
    `;

    const launcher = document.createElement("button");
    launcher.className = "chatbot-launcher";
    launcher.type = "button";
    launcher.setAttribute("aria-label", "챗봇 열기");
    launcher.setAttribute("aria-expanded", "false");
    launcher.title = "챗봇 열기";

    const normalImage = document.createElement("img");
    normalImage.className = "chatbot-image chatbot-image-normal";
    normalImage.src = "images/yon_chat.png";
    normalImage.alt = "";

    const hoverImage = document.createElement("img");
    hoverImage.className = "chatbot-image chatbot-image-hover";
    hoverImage.src = "images/yon_chat_hover.png";
    hoverImage.alt = "";

    launcher.append(normalImage, hoverImage);
    document.body.append(panel, launcher);

    const closeButton = panel.querySelector(".chatbot-close");
    const messagesEl = panel.querySelector(".chatbot-messages");
    const form = panel.querySelector(".chatbot-form");
    const input = panel.querySelector(".chatbot-input");
    const sendButton = panel.querySelector(".chatbot-send");

    function setOpen(isOpen) {
        panel.hidden = !isOpen;
        launcher.setAttribute("aria-expanded", String(isOpen));
        launcher.setAttribute("aria-label", isOpen ? "챗봇 닫기" : "챗봇 열기");

        if (isOpen) {
            setTimeout(() => input.focus(), 0);
        }
    }

    function appendMessage(role, content) {
        const message = document.createElement("div");
        message.className = `chatbot-message ${role}`;
        message.textContent = content;
        messagesEl.append(message);
        messagesEl.scrollTop = messagesEl.scrollHeight;
        return message;
    }

    launcher.addEventListener("click", () => {
        setOpen(panel.hidden);
    });

    closeButton.addEventListener("click", () => {
        setOpen(false);
    });

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const userMessage = input.value.trim();

        if (!userMessage) {
            input.focus();
            return;
        }

        input.value = "";
        appendMessage("user", userMessage);
        messages.push({ role: "user", content: userMessage });

        sendButton.disabled = true;
        input.disabled = true;
        const pendingMessage = appendMessage("bot", "응답 중...");

        try {
            const response = await fetch("/api/chatbot/message", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ messages })
            });

            if (!response.ok) {
                const data = await response.json().catch(() => ({}));
                throw new Error(data.message || `HTTP ${response.status}`);
            }

            const data = await response.json();
            const answer = data.message || "응답을 불러오지 못했습니다.";
            pendingMessage.textContent = answer;
            messages.push({ role: "assistant", content: answer });
        } catch (error) {
            pendingMessage.textContent = error.message || "챗봇 응답 중 오류가 발생했습니다.";
            messages.pop();
        } finally {
            sendButton.disabled = false;
            input.disabled = false;
            input.focus();
        }
    });
})();
