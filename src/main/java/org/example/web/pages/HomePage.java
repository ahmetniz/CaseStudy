package org.example.web.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

public class HomePage extends BasePage {

    private static final String BASE_URL = "https://www.zara.com/tr/";

    private static final By COOKIE_ACCEPT = By.id("onetrust-accept-btn-handler");
    private static final By LOGIN_ICON = By.cssSelector("a[data-qa-id='layout-desktop-layout-logon-action']");
    private static final By MENU_BUTTON = By.cssSelector("button[data-qa-id='layout-desktop-open-menu-trigger']");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public HomePage open() {
        log.info("Opening Zara homepage: {}", BASE_URL);
        try {
            driver.get(BASE_URL);
        } catch (org.openqa.selenium.ScriptTimeoutException | org.openqa.selenium.TimeoutException e) {
            log.warn("driver.get() hit a timeout ({}); DOM is likely ready — continuing", e.getClass().getSimpleName());
        }
        visible(By.tagName("header"));
        acceptCookiesIfPresent();
        return this;
    }

    public HomePage acceptCookiesIfPresent() {
        try {
            new org.example.web.utils.WaitHelper(driver, java.time.Duration.ofSeconds(15))
                    .clickable(COOKIE_ACCEPT).click();
            log.info("Accepted cookie banner");
        } catch (TimeoutException | NoSuchElementException ignored) {
            log.debug("No cookie banner displayed");
        }
        return this;
    }

    public HomePage login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Zara credentials are required. Set zara.email and zara.password in config.properties.");
        }
        try {
            log.info("Clicking GİRİŞ YAP link");
            org.openqa.selenium.WebElement loginLink =
                    new org.example.web.utils.WaitHelper(driver, java.time.Duration.ofSeconds(15))
                            .visible(LOGIN_ICON);
            ((org.openqa.selenium.JavascriptExecutor) driver)
                    .executeScript("arguments[0].click();", loginLink);
        } catch (Exception e) {
            log.info("GİRİŞ YAP link not visible, navigating directly to /tr/tr/logon");
            driver.get("https://www.zara.com/tr/tr/logon");
        }
        new LoginPage(driver).login(email, password);
        log.info("Login step completed — returning to homepage");
        try {
            driver.get(BASE_URL);
        } catch (org.openqa.selenium.ScriptTimeoutException | org.openqa.selenium.TimeoutException ignored) {
        }
        visible(By.tagName("header"));
        acceptCookiesIfPresent();
        return this;
    }

    public HomePage openMenu() {
        log.info("Opening main menu");
        click(MENU_BUTTON);
        visible(By.cssSelector("a[data-qa-qualifier='category-level-1']"));
        return this;
    }

    public MenPage selectMen() {
        log.info("Selecting 'ERKEK' (Men) top-level category");
        By menTab = By.xpath(
                "//a[@data-qa-qualifier='category-level-1'][normalize-space(.)='ERKEK']");
        click(menTab);
        return new MenPage(driver);
    }
}
