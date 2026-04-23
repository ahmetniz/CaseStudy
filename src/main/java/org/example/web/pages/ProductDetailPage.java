package org.example.web.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Represents the selected product inside the search-results grid.
 * Zara's product-detail pages are blocked by anti-bot so we operate on
 * the grid card directly: read name + price, then trigger the grid's
 * in-line "Sepete ekle" which opens a size-selector overlay.
 */
public class ProductDetailPage extends BasePage {

    private static final By NAME_IN_CARD = By.cssSelector("a.product-grid-product-info__name");
    private static final By PRICE_IN_CARD = By.cssSelector(
            ".money-amount__main, [data-qa-qualifier='price-amount-current'] .money-amount__main, " +
            ".product-grid-product-info__price .money-amount__main");
    private static final By ADD_TO_CART_IN_CARD = By.cssSelector(
            "button[data-qa-action='product-grid-open-size-selector']");
    private static final By SIZE_SELECTOR_OVERLAY = By.cssSelector(
            ".size-selector-sizes, [class*='size-selector']");
    private static final By SIZE_OPTION_IN_STOCK = By.cssSelector(
            "button[data-qa-action='size-in-stock'], button[data-qa-action='size-low-on-stock']");

    private final WebElement card;

    public ProductDetailPage(WebDriver driver, WebElement card) {
        super(driver);
        this.card = card;
    }

    public String getProductName() {
        String name = card.findElement(NAME_IN_CARD).getText().trim();
        log.info("Product name: '{}'", name);
        return name;
    }

    public String getProductPrice() {
        WebElement priceEl = card.findElements(PRICE_IN_CARD).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Price not found within the selected product card"));
        String price = priceEl.getText().trim();
        log.info("Product price: '{}'", price);
        return price;
    }

    public CartPage addToCart() {
        log.info("Clicking grid-level 'Sepete ekle' on the selected card");
        clickWithStaleRetry(() -> card.findElement(ADD_TO_CART_IN_CARD));

        log.info("Waiting for size selector overlay and picking first in-stock size");
        wait.visible(SIZE_SELECTOR_OVERLAY);
        clickWithStaleRetry(() -> wait.clickable(SIZE_OPTION_IN_STOCK));

        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(15))
                .until(ExpectedConditions.invisibilityOfElementLocated(SIZE_SELECTOR_OVERLAY));

        return new CartPage(driver).open();
    }

    private void clickWithStaleRetry(java.util.function.Supplier<WebElement> finder) {
        int attempts = 3;
        while (true) {
            try {
                WebElement el = finder.get();
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block: 'center'}); arguments[0].click();", el);
                return;
            } catch (org.openqa.selenium.StaleElementReferenceException e) {
                if (--attempts == 0) throw e;
                log.debug("Stale element on click, retrying ({} left)", attempts);
                try { Thread.sleep(400); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
