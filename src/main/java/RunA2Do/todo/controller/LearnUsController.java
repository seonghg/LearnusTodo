package RunA2Do.todo.controller;

import RunA2Do.todo.repository.LearnUsRepository;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class LearnUsController {

    private static final String SOURCE_TYPE = "LEARNUS";
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private static final Pattern COURSE_ID_PATTERN =
            Pattern.compile("(?:courseid|course_id|id)=([0-9A-Za-z_-]+)");

    private static final Pattern RATE_PATTERN =
            Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*%");

    private final LearnUsRepository learnUsRepository;

    public LearnUsController(LearnUsRepository learnUsRepository) {
        this.learnUsRepository = learnUsRepository;
    }

    @GetMapping("/todo/api/learnus_con")
    public String learnusConnect(
            @RequestParam("id") String id,
            @RequestParam("password") String password,
            Authentication authentication
    ) {
        WebDriver driver = null;
        String userId = authentication != null ? authentication.getName() : id;

        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1280,900");

            driver = new ChromeDriver(options);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            System.out.println("==== LearnUs Controller Called ====");
            System.out.println("id = " + id);

            driver.get("https://ys.learnus.org/");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            String initialUrl = driver.getCurrentUrl();
            System.out.println("Initial URL = " + initialUrl);

            if (!exists(driver, By.cssSelector("input[type='password']"))) {
                WebElement loginEntry = findFirst(driver,
                        By.xpath("//a[contains(text(), '로그인')]"),
                        By.xpath("//button[contains(text(), '로그인')]"),
                        By.xpath("//*[contains(text(), '로그인')]"),
                        By.xpath("//a[contains(text(), 'Login')]"),
                        By.xpath("//button[contains(text(), 'Login')]"),
                        By.cssSelector("a[href*='login']"),
                        By.cssSelector("a[href*='sso']")
                );

                if (loginEntry != null) {
                    System.out.println("Click login entry");
                    loginEntry.click();
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                    Thread.sleep(2000);
                }
            }

            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            Thread.sleep(2000);

            System.out.println("Login Page URL = " + driver.getCurrentUrl());

            WebElement idInput = findFirst(driver,
                    By.name("id"),
                    By.name("userid"),
                    By.name("user_id"),
                    By.name("userId"),
                    By.name("loginId"),
                    By.name("j_username"),
                    By.id("id"),
                    By.id("userid"),
                    By.id("userId"),
                    By.id("loginId"),
                    By.cssSelector("input[type='text']")
            );

            WebElement pwInput = findFirst(driver,
                    By.name("password"),
                    By.name("pw"),
                    By.name("passwd"),
                    By.name("userPw"),
                    By.name("loginPw"),
                    By.name("j_password"),
                    By.id("password"),
                    By.id("pw"),
                    By.id("passwd"),
                    By.id("loginPw"),
                    By.cssSelector("input[type='password']")
            );

            if (idInput == null || pwInput == null) {
                return html(
                        "FAIL",
                        "Could not find SSO login input fields.",
                        "Current URL: " + driver.getCurrentUrl(),
                        extractInputDebug(driver),
                        "",
                        ""
                );
            }

            idInput.clear();
            idInput.sendKeys(id);

            pwInput.clear();
            pwInput.sendKeys(password);

            System.out.println("ID/PW filled");

            WebElement loginButton = findFirst(driver,
                    By.cssSelector("button[type='submit']"),
                    By.cssSelector("input[type='submit']"),
                    By.xpath("//button[contains(text(), '로그인')]"),
                    By.xpath("//input[contains(@value, '로그인')]"),
                    By.xpath("//*[contains(text(), '로그인')]"),
                    By.xpath("//button[contains(text(), 'Login')]"),
                    By.xpath("//input[contains(@value, 'Login')]")
            );

            if (loginButton != null) {
                System.out.println("Click submit button");
                loginButton.click();
            } else {
                System.out.println("Submit by ENTER");
                pwInput.sendKeys(Keys.ENTER);
            }

            Thread.sleep(6000);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            String afterLoginUrl = driver.getCurrentUrl();
            System.out.println("After Login URL = " + afterLoginUrl);

            driver.get("https://ys.learnus.org/?lang=ko");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            Thread.sleep(3000);

            String finalUrl = driver.getCurrentUrl();
            String title = driver.getTitle();
            String bodyText = driver.findElement(By.tagName("body")).getText();
            String lowerBody = bodyText.toLowerCase();

            boolean success =
                    lowerBody.contains("logout")
                            || lowerBody.contains("my page")
                            || bodyText.contains("로그아웃")
                            || bodyText.contains("마이페이지")
                            || exists(driver, By.cssSelector(".front-box-body.course_lists"))
                            || exists(driver, By.cssSelector("td.day[data-day-timestamp]"));

            String calendarText = "";
            String calendarHtml = "";

            WebElement calendarBox = findFirstAny(driver,
                    By.cssSelector(".front-box-body:has([id^='calendar-month-'])"),
                    By.xpath("//div[contains(concat(' ', normalize-space(@class), ' '), ' front-box-body ')][.//*[starts-with(@id, 'calendar-month-')]]"),
                    By.cssSelector(".calendarwrapper"),
                    By.cssSelector("table.minicalendar")
            );

            if (calendarBox != null) {
                calendarText = preview(calendarBox.getText(), 4000);
                calendarHtml = preview(calendarBox.getAttribute("outerHTML"), 20000);
            }

            String courseText = "";
            String courseHtml = "";

            WebElement courseBox = findFirstAny(driver,
                    By.cssSelector("div.front-box-body.course_lists")
            );

            if (courseBox != null) {
                courseText = preview(courseBox.getText(), 4000);
                courseHtml = preview(courseBox.getAttribute("outerHTML"), 12000);
            }

            SyncResult syncResult = new SyncResult();

            if (success) {
                syncResult.courseCount = saveCourses(userId, courseBox);
                syncResult.eventCount = saveCalendarEvents(userId, calendarBox);
            }

            return html(
                    success ? "SUCCESS" : "FAIL",
                    success ? "Login checked. Extracted LearnUs target sections below." : "Login failed or target sections were not found.",
                    "Initial URL: " + initialUrl
                            + "\nAfter Login URL: " + afterLoginUrl
                            + "\nFinal URL: " + finalUrl
                            + "\nTitle: " + title,
                    section("DB Sync Result", syncResult.message())
                            + section("Calendar Text", calendarText)
                            + section("Calendar HTML", calendarHtml)
                            + section("Course List Text", courseText)
                            + section("Course List HTML", courseHtml),
                    calendarHtml,
                    courseHtml
            );

        } catch (Exception e) {
            e.printStackTrace();
            return html("ERROR", e.getClass().getName() + "\n" + e.getMessage(), "", "", "", "");
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private int saveCourses(String userId, WebElement courseBox) {
        if (courseBox == null) {
            return 0;
        }

        int savedCount = 0;
        List<WebElement> courseItems = courseBox.findElements(By.cssSelector("li.course-label-r"));
        Set<String> savedExternalIds = new HashSet<>();

        for (WebElement item : courseItems) {
            try {
                WebElement courseLink = findFirstInAny(item, By.cssSelector("a.course-link[href]"));
                String courseUrl = attr(courseLink, "href");

                String externalCourseId = firstNonBlank(
                        extractByPattern(courseUrl, COURSE_ID_PATTERN),
                        attr(item, "data-course-id"),
                        attr(item, "data-id"),
                        attr(item, "id")
                );

                if (isBlank(externalCourseId)) {
                    continue;
                }

                if (!savedExternalIds.add(externalCourseId)) {
                    continue;
                }

                WebElement h3Element = findFirstInAny(item, By.cssSelector(".course-title h3"));
                WebElement profElement = findFirstInAny(item, By.cssSelector(".course-title .prof"));

                String h3Text = textOf(h3Element);
                String profText = textOf(profElement);

                String courseName = extractCourseNameFromH3(h3Text);

                String courseCode = firstNonBlank(
                        extractCourseCodeFromProf(profText),
                        extractCourseCode(h3Text),
                        extractCourseCode(item.getText())
                );

                String professorName = extractProfessorName(profText);
                String semesterName = extractSemesterName(h3Text);

                String courseType = textOf(findFirstInAny(
                        item,
                        By.cssSelector(".course-label .badge-course:not(.badge-under)")
                ));

                String courseLevel = textOf(findFirstInAny(
                        item,
                        By.cssSelector(".course-label .badge-under")
                ));

                if (isBlank(courseName)) {
                    continue;
                }

                String itemText = item.getText();

                Long courseId = upsertCourse(
                        externalCourseId,
                        courseCode,
                        courseName,
                        professorName,
                        semesterName,
                        courseType,
                        courseLevel,
                        courseUrl
                );

                if (courseId == null) {
                    continue;
                }

                upsertUserCourse(userId, courseId, item, itemText);

                System.out.println("[COURSE SAVED] "
                        + "externalCourseId=" + externalCourseId
                        + ", courseCode=" + courseCode
                        + ", courseName=" + courseName
                        + ", professor=" + professorName
                        + ", semester=" + semesterName
                        + ", learningRate=" + extractLearningRate(itemText)
                        + ", completed=" + extractCompletedCount(itemText)
                        + ", total=" + extractTotalCount(itemText)
                );

                savedCount++;

            } catch (Exception e) {
                System.out.println("Course parse/save skipped: " + e.getMessage());
            }
        }

        return savedCount;
    }

    private Long upsertCourse(
            String externalCourseId,
            String courseCode,
            String courseName,
            String professorName,
            String semesterName,
            String courseType,
            String courseLevel,
            String courseUrl
    ) {
        return learnUsRepository.upsertCourse(
                SOURCE_TYPE,
                externalCourseId,
                courseCode,
                courseName,
                professorName,
                semesterName,
                courseType,
                courseLevel,
                courseUrl
        );
    }

    private void upsertUserCourse(String userId, Long courseId, WebElement item, String itemText) {
        learnUsRepository.upsertUserCourse(
                SOURCE_TYPE,
                userId,
                courseId,
                extractLearningRate(itemText),
                extractCompletedCount(itemText),
                extractTotalCount(itemText),
                findAttendanceUrl(item),
                itemText.toLowerCase().contains("new")
        );
    }

    private int saveCalendarEvents(String userId, WebElement calendarBox) {
        if (calendarBox == null) {
            System.out.println("[CALENDAR] calendarBox is null");
            return 0;
        }

        int savedCount = 0;
        Set<String> savedExternalIds = new HashSet<>();

        List<WebElement> dayCells = calendarBox.findElements(By.cssSelector("td.day[data-day-timestamp]"));
        System.out.println("[CALENDAR] dayCells count = " + dayCells.size());

        for (WebElement dayCell : dayCells) {
            try {
                String timestampText = attr(dayCell, "data-day-timestamp");

                if (isBlank(timestampText)) {
                    continue;
                }

                OffsetDateTime dayStart = parseTime(timestampText);

                if (dayStart == null) {
                    continue;
                }

                String dayClass = attr(dayCell, "class");
                boolean hasEventClass = dayClass != null && (
                        dayClass.contains("hasevent")
                                || dayClass.contains("calendar_event")
                                || dayClass.contains("duration_finish")
                );

                WebElement hiddenDiv = findFirstInAny(dayCell, By.cssSelector("div.hidden"));

                if (hiddenDiv == null) {
                    if (hasEventClass) {
                        System.out.println("[CALENDAR] hasevent day but hiddenDiv is null. date=" + dayStart);
                    }
                    continue;
                }

                List<WebElement> eventDivs = hiddenDiv.findElements(By.cssSelector("div[data-popover-eventtype-course]"));
                System.out.println("[CALENDAR] date=" + dayStart + ", eventDivs=" + eventDivs.size());

                if (eventDivs.isEmpty()) {
                    String hiddenText = firstNonBlank(
                            getDomText(hiddenDiv),
                            htmlToPlainText(attr(hiddenDiv, "innerHTML"))
                    );

                    List<String> fallbackLines = splitEventLines(hiddenText);

                    for (String line : fallbackLines) {
                        String title = cleanCalendarEventTitle(line);

                        if (isBlank(title) || isNonEventCalendarText(title)) {
                            continue;
                        }

                        String externalEventId = stableId(
                                userId + "|" + SOURCE_TYPE + "|" + timestampText + "|" + title
                        );

                        if (!savedExternalIds.add(externalEventId)) {
                            continue;
                        }

                        upsertCalendarEvent(
                                userId,
                                title,
                                hiddenText,
                                dayStart,
                                null,
                                true,
                                null,
                                null,
                                externalEventId
                        );

                        System.out.println("[CALENDAR SAVED - FALLBACK] date=" + dayStart + ", title=" + title);
                        savedCount++;
                    }

                    continue;
                }

                for (WebElement eventDiv : eventDivs) {
                    String rawTitle = firstNonBlank(
                            getDomText(eventDiv),
                            htmlToPlainText(attr(eventDiv, "innerHTML")),
                            eventDiv.getAttribute("textContent")
                    );

                    String title = cleanCalendarEventTitle(rawTitle);

                    if (isBlank(title)) {
                        continue;
                    }

                    if (isNonEventCalendarText(title)) {
                        continue;
                    }

                    String externalEventId = stableId(
                            userId + "|" + SOURCE_TYPE + "|" + timestampText + "|" + title
                    );

                    if (!savedExternalIds.add(externalEventId)) {
                        continue;
                    }

                    Long matchedCourseId = findMatchedCourseId(userId, title);

                    upsertCalendarEvent(
                            userId,
                            title,
                            title,
                            dayStart,
                            null,
                            true,
                            null,
                            matchedCourseId,
                            externalEventId
                    );

                    System.out.println("[CALENDAR SAVED] date=" + dayStart + ", title=" + title + ", courseId=" + matchedCourseId);
                    savedCount++;
                }

            } catch (Exception e) {
                System.out.println("Calendar day parse/save skipped: " + e.getMessage());
            }
        }

        return savedCount;
    }

    private Long findMatchedCourseId(String userId, String eventTitle) {
        if (isBlank(eventTitle)) {
            return null;
        }

        String courseCode = extractCourseCode(eventTitle);

        if (isBlank(courseCode)) {
            return null;
        }

        try {
            return learnUsRepository.findCourseIdByCode(SOURCE_TYPE, userId, courseCode);

        } catch (Exception e) {
            return null;
        }
    }

    private void upsertCalendarEvent(
            String userId,
            String title,
            String description,
            OffsetDateTime startTime,
            OffsetDateTime endTime,
            boolean isAllDay,
            String location,
            Long courseId,
            String externalEventId
    ) {
        learnUsRepository.upsertCalendarEvent(
                SOURCE_TYPE,
                userId,
                normalizeText(title),
                normalizeText(description),
                startTime,
                endTime,
                isAllDay,
                location,
                courseId,
                externalEventId
        );
    }

    private String cleanCalendarEventTitle(String value) {
        String text = normalizeText(value);

        if (text == null) {
            return null;
        }

        text = text.replaceAll("\\s+", " ").trim();

        text = text.replace("Loading", "").trim();

        return normalizeBlank(text);
    }

    private String extractCourseNameFromH3(String h3Text) {
        String text = normalizeBlank(h3Text);

        if (text == null) {
            return null;
        }

        text = text.replace("NEW", "").trim();

        text = text.replaceAll("\\s*\\([A-Z]{2,}[A-Z0-9]*[0-9]{3,}[^)]*\\).*", "").trim();

        return normalizeBlank(text);
    }

    private String extractProfessorName(String profText) {
        String text = normalizeBlank(profText);

        if (text == null) {
            return null;
        }

        String[] parts = text.split("/");

        if (parts.length >= 2) {
            return normalizeBlank(parts[1]);
        }

        return null;
    }

    private String extractCourseCodeFromProf(String profText) {
        String text = normalizeBlank(profText);

        if (text == null) {
            return null;
        }

        String[] parts = text.split("/");

        if (parts.length >= 1) {
            return normalizeBlank(parts[0]);
        }

        return null;
    }

    private String extractSemesterName(String h3Text) {
        String text = normalizeBlank(h3Text);

        if (text == null) {
            return null;
        }

        Matcher matcher = Pattern.compile("\\((\\d+학기)\\)").matcher(text);

        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractCourseCode(String text) {
        if (text == null) {
            return null;
        }

        Matcher matcher = Pattern
                .compile("([A-Z]{2,}[A-Z0-9]*[0-9]{3,}(?:\\.[0-9]{2}-[0-9]{2})?)")
                .matcher(text);

        return matcher.find() ? matcher.group(1) : null;
    }

    private Double extractLearningRate(String text) {
        if (text == null) {
            return null;
        }

        Matcher matcher = RATE_PATTERN.matcher(text);

        return matcher.find() ? Double.valueOf(matcher.group(1)) : null;
    }

    private Integer extractCompletedCount(String text) {
        if (text == null) {
            return null;
        }

        Matcher matcher = Pattern.compile("이수\\s*완료\\s*:\\s*(\\d+)개").matcher(text);

        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    private Integer extractTotalCount(String text) {
        if (text == null) {
            return null;
        }

        Matcher matcher = Pattern.compile("이수\\s*대상\\s*:\\s*(\\d+)개").matcher(text);

        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    private String findAttendanceUrl(WebElement root) {
        WebElement link = findFirstInAny(
                root,
                By.cssSelector("a[href*='ubcompletion']"),
                By.cssSelector("a[href*='progress.php']"),
                By.xpath(".//a[contains(text(), '출석현황')]")
        );

        return attr(link, "href");
    }

    private OffsetDateTime parseTime(String value) {
        String normalized = normalizeBlank(value);

        if (normalized == null) {
            return null;
        }

        try {
            if (normalized.matches("\\d{10,13}")) {
                long epoch = Long.parseLong(normalized);

                if (normalized.length() == 13) {
                    epoch = epoch / 1000;
                }

                return OffsetDateTime.ofInstant(Instant.ofEpochSecond(epoch), KOREA_ZONE);
            }
        } catch (Exception ignored) {
        }

        try {
            return OffsetDateTime.parse(normalized);
        } catch (Exception ignored) {
        }

        Matcher dateMatcher = Pattern
                .compile("(\\d{4})[-./](\\d{1,2})[-./](\\d{1,2})")
                .matcher(normalized);

        if (dateMatcher.find()) {
            try {
                LocalDate date = LocalDate.of(
                        Integer.parseInt(dateMatcher.group(1)),
                        Integer.parseInt(dateMatcher.group(2)),
                        Integer.parseInt(dateMatcher.group(3))
                );

                return date.atStartOfDay(KOREA_ZONE).toOffsetDateTime();

            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private String getDomText(WebElement element) {
        if (element == null) {
            return null;
        }

        try {
            return normalizeBlank(element.getAttribute("textContent"));
        } catch (Exception e) {
            return null;
        }
    }

    private String htmlToPlainText(String html) {
        if (html == null) {
            return null;
        }

        String text = html;

        text = text.replaceAll("(?i)<br\\s*/?>", "\n");
        text = text.replaceAll("(?i)</li>", "\n");
        text = text.replaceAll("(?i)</p>", "\n");
        text = text.replaceAll("(?i)</div>", "\n");
        text = text.replaceAll("<[^>]+>", " ");

        text = text.replace("&nbsp;", " ");
        text = text.replace("&amp;", "&");
        text = text.replace("&lt;", "<");
        text = text.replace("&gt;", ">");
        text = text.replace("&#39;", "'");
        text = text.replace("&quot;", "\"");

        return normalizeText(text);
    }

    private List<String> splitEventLines(String text) {
        if (text == null) {
            return List.of();
        }

        return text.lines()
                .map(this::normalizeText)
                .filter(line -> line != null && !line.isBlank())
                .filter(line -> !line.equals("일정 없음"))
                .filter(line -> !line.matches("\\d+"))
                .filter(line -> !isNonEventCalendarText(line))
                .toList();
    }

    private boolean isNonEventCalendarText(String text) {
        if (text == null) {
            return true;
        }

        String normalized = normalizeText(text);

        if (normalized == null) {
            return true;
        }

        return normalized.equals("일정 없음")
                || normalized.equals("Previous month")
                || normalized.equals("다음 달")
                || normalized.equals("이번 달")
                || normalized.startsWith("Today ")
                || normalized.matches("\\d+")
                || normalized.matches("\\d{4}년\\s*\\d{1,2}월")
                || normalized.contains("Loading");
    }

    private WebElement findFirstIn(WebElement root, By... byList) {
        if (root == null) {
            return null;
        }

        for (By by : byList) {
            try {
                List<WebElement> elements = root.findElements(by);

                if (!elements.isEmpty() && elements.get(0).isDisplayed()) {
                    return elements.get(0);
                }

            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private WebElement findFirstInAny(WebElement root, By... byList) {
        if (root == null) {
            return null;
        }

        for (By by : byList) {
            try {
                List<WebElement> elements = root.findElements(by);

                if (!elements.isEmpty()) {
                    return elements.get(0);
                }

            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private WebElement findFirst(WebDriver driver, By... byList) {
        for (By by : byList) {
            try {
                List<WebElement> elements = driver.findElements(by);

                if (!elements.isEmpty() && elements.get(0).isDisplayed()) {
                    return elements.get(0);
                }

            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private WebElement findFirstAny(WebDriver driver, By... byList) {
        for (By by : byList) {
            try {
                List<WebElement> elements = driver.findElements(by);

                if (!elements.isEmpty()) {
                    return elements.get(0);
                }

            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private boolean exists(WebDriver driver, By by) {
        try {
            return !driver.findElements(by).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private String attr(WebElement element, String name) {
        if (element == null) {
            return null;
        }

        try {
            return normalizeBlank(element.getAttribute(name));
        } catch (Exception e) {
            return null;
        }
    }

    private String textOf(WebElement element) {
        if (element == null) {
            return null;
        }

        try {
            return normalizeBlank(element.getText());
        } catch (Exception e) {
            return null;
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            String normalized = normalizeBlank(value);

            if (normalized != null) {
                return normalized;
            }
        }

        return null;
    }

    private boolean isBlank(String value) {
        return normalizeBlank(value) == null;
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeText(String value) {
        String normalized = normalizeBlank(value);

        if (normalized == null) {
            return null;
        }

        return normalized
                .replaceAll("[\\t\\x0B\\f\\r ]+", " ")
                .replaceAll("\\n\\s*\\n+", "\n")
                .trim();
    }

    private String extractByPattern(String value, Pattern pattern) {
        if (value == null) {
            return null;
        }

        Matcher matcher = pattern.matcher(value);

        return matcher.find() ? matcher.group(1) : null;
    }

    private String stableId(String value) {
        return Integer.toUnsignedString(String.valueOf(value).hashCode(), 36);
    }

    private String extractInputDebug(WebDriver driver) {
        StringBuilder sb = new StringBuilder();

        try {
            List<WebElement> inputs = driver.findElements(By.tagName("input"));

            sb.append("Current URL: ").append(driver.getCurrentUrl()).append("\n\n");
            sb.append("Detected input tags:\n");

            for (WebElement input : inputs) {
                sb.append("type=")
                        .append(input.getAttribute("type"))
                        .append(", name=")
                        .append(input.getAttribute("name"))
                        .append(", id=")
                        .append(input.getAttribute("id"))
                        .append(", placeholder=")
                        .append(input.getAttribute("placeholder"))
                        .append("\n");
            }

        } catch (Exception e) {
            sb.append("input debug failed: ").append(e.getMessage());
        }

        return sb.toString();
    }

    private String preview(String text, int maxLength) {
        if (text == null) {
            return "";
        }

        if (text.length() > maxLength) {
            return text.substring(0, maxLength) + "\n... truncated ...";
        }

        return text;
    }

    private String section(String title, String value) {
        String body = value == null || value.isBlank() ? "(not found)" : value;

        return "\n\n[" + title + "]\n" + body;
    }

    private String html(
            String status,
            String message,
            String meta,
            String debug,
            String calendarHtml,
            String courseHtml
    ) {
        String color = status.equals("SUCCESS") ? "green" : "red";

        return """
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>LearnUs Login Test</title>
                </head>
                <body>
                    <h2 style="color:%s;">%s</h2>
                    <h3>Message</h3>
                    <pre>%s</pre>
                    <h3>Meta</h3>
                    <pre>%s</pre>
                    <h3>Extracted Data / Debug</h3>
                    <pre style="white-space:pre-wrap; border:1px solid #ccc; padding:10px;">%s</pre>
                    <h3>Raw Calendar HTML</h3>
                    <pre style="white-space:pre-wrap; border:1px solid #ccc; padding:10px;">%s</pre>
                    <h3>Raw Course List HTML</h3>
                    <pre style="white-space:pre-wrap; border:1px solid #ccc; padding:10px;">%s</pre>
                </body>
                </html>
                """.formatted(
                color,
                escape(status),
                escape(message),
                escape(meta),
                escape(debug),
                escape(calendarHtml),
                escape(courseHtml)
        );
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static class SyncResult {
        private int courseCount;
        private int eventCount;

        private String message() {
            return "courses saved: " + courseCount + "\ncalendar events saved: " + eventCount;
        }
    }
}
