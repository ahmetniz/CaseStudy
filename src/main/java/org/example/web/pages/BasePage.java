package org.example.web.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.web.utils.WaitHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public abstract class BasePage {

    protected static final By HEADER_SEARCH_ICON = By.cssSelector(
            "a[data-qa-id='header-search-text-link']");

    protected final Logger log = LogManager.getLogger(getClass());
    protected final WebDriver driver;
    protected final WaitHelper wait;
    protected final Actions actions;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WaitHelper(driver);
        this.actions = new Actions(driver);
    }

    public SearchPage openSearch() {
        log.info("Opening search page");
        try {
            WebElement searchLink = new WaitHelper(driver, java.time.Duration.ofSeconds(5))
                    .clickable(HEADER_SEARCH_ICON);
            String href = searchLink.getAttribute("href");
            if (href != null && !href.isBlank()) {
                driver.get(href);
            } else {
                ((org.openqa.selenium.JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", searchLink);
            }
        } catch (Exception e) {
            log.info("Header search link not clickable, navigating directly to search URL");
            driver.get("https://www.zara.com/tr/tr/search/home");
        }
        return new SearchPage(driver);
    }

    protected WebElement visible(By locator) {
        return wait.visible(locator);
    }

    protected WebElement clickable(By locator) {
        return wait.clickable(locator);
    }

    protected void click(By locator) {
        clickable(locator).click();
    }

    protected void type(By locator, String text) {
        WebElement element = visible(locator);
        element.clear();
        element.sendKeys(text);
    }

    protected void pressEnter(By locator) {
        visible(locator).sendKeys(Keys.ENTER);
    }

    protected String text(By locator) {
        return visible(locator).getText();
    }

    protected boolean isPresent(By locator) {
        try {
            return !driver.findElements(locator).isEmpty();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    protected void scrollIntoView(WebElement element) {
        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
    }
}
