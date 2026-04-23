package org.example.web.pages;

import org.example.web.utils.WaitHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.Duration;

public class LoginPage extends BasePage {

    private static final int LOGIN_RETRY_ATTEMPTS = 3;
    private static final long RETRY_BACKOFF_MS = 4000L;

    private static final By EMAIL_INPUT = By.cssSelector(
            "input[data-qa-input-qualifier='logonId'], " +
            "input[name='username'][type='email'], " +
            "input[type='email']");
    private static final By PASSWORD_INPUT = By.cssSelector(
            "input[data-qa-input-qualifier='password'], " +
            "input[name='password'], " +
            "input[type='password']");
    private static final By SUBMIT_BUTTON = By.cssSelector(
            "button[data-qa-id='logon-form-submit'], " +
            "button[type='submit']");
    private static final By PASSWORD_LINK = By.xpath(
            "//*[(@role='link' or self::a) and " +
            "(normalize-space(.)='Şifre ile giriş yapın' or normalize-space(.)='Şifre ile Giriş Yap')]");
    private static final By ERROR_BANNER = By.xpath(
            "//*[contains(translate(., 'ABCÇDEFGĞHIİJKLMNOÖPRSŞTUÜVYZ', 'abcçdefgğhıijklmnoöprsştuüvyz'), 'sorun ile karşılaştık')" +
            "    or contains(translate(., 'ABCÇDEFGĞHIİJKLMNOÖPRSŞTUÜVYZ', 'abcçdefgğhıijklmnoöprsştuüvyz'), 'beklenmeyen')" +
            "    or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'try again')]");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public HomePage login(String email, String password) {
        log.info("Attempting two-step login with user '{}'", email);
        waitForOAuthRedirectToSettle();

        enterEmailAndContinue(email);
        dismissOkDialogIfPresent();
        clickSwitchToPasswordLink();
        submitPasswordWithRetry(password);

        return new HomePage(driver);
    }

    private void enterEmailAndContinue(String email) {
        try {
            WebElement emailEl = new WaitHelper(driver, Duration.ofSeconds(45)).visible(EMAIL_INPUT);
            emailEl.clear();
            emailEl.sendKeys(email);
            log.info("Email entered; clicking DEVAM ET");
            WebElement devamEt = new WaitHelper(driver, Duration.ofSeconds(15)).clickable(SUBMIT_BUTTON);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", devamEt);
        } catch (TimeoutException e) {
            dumpLoginForm("step1-email-or-devam-not-found");
            throw e;
        }
    }

    private void dismissOkDialogIfPresent() {
        // After DEVAM ET, Zara sometimes shows an information dialog
        // ("Doğrulama kodu e-postanıza gönderildi") with a primary "OK" button.
        // Click it to advance to the two-step page that contains "Şifre ile giriş yapın".
        By okButton = By.xpath(
                "//button[normalize-space(.)='OK' or normalize-space(.)='Tamam']" +
                "[not(@data-qa-id='logon-form-submit')]");
        long deadline = System.currentTimeMillis() + 15_000L;
        while (System.currentTimeMillis() < deadline) {
            for (WebElement el : driver.findElements(okButton)) {
                try {
                    if (el.isDisplayed()) {
                        log.info("Dismissing OK dialog after DEVAM ET");
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                        sleep(800);
                        return;
                    }
                } catch (Exception ignored) {
                }
            }
            sleep(500);
        }
        log.debug("No OK dialog detected after DEVAM ET");
    }

    private void clickSwitchToPasswordLink() {
        log.info("Looking for 'Şifre ile giriş yapın' link");
        try {
            WebElement link = new WaitHelper(driver, Duration.ofSeconds(30)).visible(PASSWORD_LINK);
            log.info("Clicking 'Şifre ile giriş yapın' link");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
        } catch (TimeoutException e) {
            log.warn("'Şifre ile giriş yapın' link not found (URL={}). " +
                    "Either already on password form or account state is different.",
                    driver.getCurrentUrl());
        }
    }

    private void submitPasswordWithRetry(String password) {
        // Wait for password input once, then retry the submit loop while Zara
        // shows the generic "Bir sorun ile karşılaştık" bot-detection banner.
        WebElement passwordEl;
        try {
            passwordEl = new WaitHelper(driver, Duration.ofSeconds(45)).visible(PASSWORD_INPUT);
        } catch (TimeoutException e) {
            dumpLoginForm("step2-password-input-not-found");
            throw e;
        }

        for (int attempt = 1; attempt <= LOGIN_RETRY_ATTEMPTS; attempt++) {
            log.info("Login attempt {}/{}: typing password and submitting", attempt, LOGIN_RETRY_ATTEMPTS);
            try {
                passwordEl.clear();
                passwordEl.sendKeys(password);
            } catch (org.openqa.selenium.StaleElementReferenceException e) {
                passwordEl = new WaitHelper(driver, Duration.ofSeconds(15)).visible(PASSWORD_INPUT);
                passwordEl.clear();
                passwordEl.sendKeys(password);
            }

            WebElement submit = new WaitHelper(driver, Duration.ofSeconds(15)).clickable(SUBMIT_BUTTON);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submit);

            sleep(RETRY_BACKOFF_MS);

            if (loginSucceeded()) {
                log.info("Login succeeded on attempt {} (URL={})", attempt, driver.getCurrentUrl());
                return;
            }

            if (botErrorDetected()) {
                log.warn("Zara bot-detection banner appeared on attempt {}; retrying", attempt);
                continue;
            }

            // Neither succeeded nor visible error → wait a bit longer in case navigation is pending
            long extraDeadline = System.currentTimeMillis() + 15_000L;
            while (System.currentTimeMillis() < extraDeadline) {
                if (loginSucceeded()) {
                    log.info("Login succeeded after extended wait (attempt {})", attempt);
                    return;
                }
                if (botErrorDetected()) {
                    log.warn("Bot banner surfaced during extended wait on attempt {}", attempt);
                    break;
                }
                sleep(1000);
            }
        }

        dumpLoginForm("login-retries-exhausted");
        throw new IllegalStateException(
                "Zara bot protection blocked login after " + LOGIN_RETRY_ATTEMPTS + " attempts. "
                + "URL=" + driver.getCurrentUrl());
    }

    private boolean loginSucceeded() {
        String url = driver.getCurrentUrl();
        if (url == null) return false;
        // Success = redirected back to zara.com (not account.zara.com) AND not on a login path.
        boolean onAccountDomain = url.contains("account.zara.com");
        boolean onLoginPath = url.contains("/login") || url.contains("/logon");
        return !onAccountDomain && !onLoginPath;
    }

    private boolean botErrorDetected() {
        try {
            return !driver.findElements(ERROR_BANNER).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private void waitForOAuthRedirectToSettle() {
        log.info("Waiting for OAuth redirect chain to resolve (current URL: {})", driver.getCurrentUrl());
        long deadline = System.currentTimeMillis() + 60_000L;
        while (System.currentTimeMillis() < deadline) {
            String url = driver.getCurrentUrl();
            boolean stillRedirecting = url == null || url.contains("init-authorize")
                    || url.contains("integration/oauth2/callback");
            boolean hasInput = !driver.findElements(By.tagName("input")).isEmpty();
            if (!stillRedirecting && hasInput) {
                log.info("OAuth settled on URL: {}", url);
                return;
            }
            sleep(1000);
        }
        log.warn("OAuth redirect did not settle within 60s; current URL: {}", driver.getCurrentUrl());
    }

    private void dumpLoginForm(String label) {
        try {
            String html = (String) ((JavascriptExecutor) driver).executeScript(
                    "const inputs = Array.from(document.querySelectorAll('input, button'))" +
                    "  .map(el => ({" +
                    "    tag: el.tagName.toLowerCase(), id: el.id, name: el.name, type: el.type," +
                    "    placeholder: el.placeholder, ariaLabel: el.getAttribute('aria-label')," +
                    "    dqa: Array.from(el.attributes).filter(a=>a.name.startsWith('data-qa-'))" +
                    "              .map(a=>a.name+'='+a.value).join('|')," +
                    "    text: (el.innerText||'').trim().slice(0,60)," +
                    "    cls: (el.className||'').toString().slice(0,80)" +
                    "  }));" +
                    "return JSON.stringify(inputs, null, 2);");
            log.error("=== Login page form dump ({}), URL={}\n{}\n=== end login dump",
                    label, driver.getCurrentUrl(), html);
        } catch (Exception e) {
            log.error("Failed to dump login form: {}", e.getMessage());
        }
    }
}
