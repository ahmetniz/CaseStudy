package org.example.web.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SearchPage extends BasePage {

    private static final By SEARCH_INPUT = By.cssSelector(
            "input#search-home-form-combo-input, input[type='search']");

    public SearchPage(WebDriver driver) {
        super(driver);
    }

    public SearchPage type(String keyword) {
        log.info("Typing search keyword: '{}'", keyword);
        retry(() -> {
            WebElement input = wait.clickable(SEARCH_INPUT);
            input.sendKeys(keyword);
        });
        return this;
    }

    public SearchPage clearSearch() {
        log.info("Clearing search box");
        retry(() -> {
            WebElement input = wait.clickable(SEARCH_INPUT);
            String currentValue = input.getAttribute("value");
            if (currentValue != null) {
                for (int i = 0; i < currentValue.length(); i++) {
                    input.sendKeys(Keys.BACK_SPACE);
                }
            }
        });
        return this;
    }

    public ProductListPage pressEnter() {
        log.info("Submitting search with ENTER");
        retry(() -> wait.clickable(SEARCH_INPUT).sendKeys(Keys.ENTER));
        return new ProductListPage(driver);
    }

    private void retry(Runnable action) {
        int attempts = 3;
        while (true) {
            try {
                action.run();
                return;
            } catch (StaleElementReferenceException e) {
                if (--attempts == 0) throw e;
                log.debug("Stale element, retrying");
            }
        }
    }
}
