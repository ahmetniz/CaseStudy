package org.example.web.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CartPage extends BasePage {

    private static final String CART_URL = "https://www.zara.com/tr/tr/shop/cart";

    private static final By CART_ITEM = By.cssSelector(
            "li.shop-cart-item, " +
            "li[data-qa-qualifier='shop-cart-item'], " +
            "[data-qa-id='shop-cart-item'], " +
            "li[class*='shop-cart-item']");
    private static final By ITEM_PRICE = By.cssSelector(".money-amount__main");
    private static final By INCREASE_QTY = By.cssSelector(
            "[data-qa-id='add-order-item-unit'], " +
            ".zds-quantity-selector__increase, " +
            "button[aria-label*='Bir birim daha ekle' i], " +
            "[role='button'][aria-label*='Bir birim daha ekle' i]");
    private static final By QUANTITY_TEXT = By.cssSelector(
            ".zds-quantity-selector__quantity, " +
            ".zds-quantity-selector__value, " +
            "[data-qa-id='order-item-units'], " +
            "[data-qa-qualifier='quantity-selector-value']");
    private static final By REMOVE_BUTTON = By.cssSelector(
            "button[data-qa-action='remove-order-item'], " +
            "button[aria-label='Ürünü sil'], " +
            "button[data-qa-action='item-remove'], " +
            "button[aria-label*='sil' i]");
    private static final By EMPTY_CART_MARKER = By.cssSelector(
            "[data-qa-qualifier='shop-cart-empty'], " +
            ".shop-cart-empty, " +
            "[class*='cart-empty']");
    private static final By CART_ICON = By.cssSelector("a[data-qa-id='layout-actions-cart']");

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public CartPage open() {
        log.info("Navigating to cart URL: {}", CART_URL);
        driver.get(CART_URL);
        return this;
    }

    public String getFirstItemPrice() {
        WebElement item = visible(CART_ITEM);
        String price = item.findElement(ITEM_PRICE).getText().trim();
        log.info("Cart item price: '{}'", price);
        return price;
    }

    public CartPage increaseQuantity() {
        log.info("Increasing product quantity by one");
        try {
            clickButtonInsideItem(INCREASE_QTY);
        } catch (TimeoutException | org.openqa.selenium.NoSuchElementException e) {
            dumpCartForDebugging("increase-quantity");
            throw e;
        }
        return this;
    }

    public int getQuantity() {
        WebElement qtyEl = visible(QUANTITY_TEXT);
        String text = qtyEl.getText();
        if (text == null || text.isBlank()) {
            text = qtyEl.getAttribute("value");
        }
        if (text == null) {
            text = "1";
        }
        int qty = Integer.parseInt(text.replaceAll("\\D", ""));
        log.info("Current cart quantity: {}", qty);
        return qty;
    }

    public CartPage removeItem() {
        log.info("Removing product from cart");
        try {
            clickButtonInsideItem(REMOVE_BUTTON);
        } catch (TimeoutException | org.openqa.selenium.NoSuchElementException e) {
            dumpCartForDebugging("remove-item");
            throw e;
        }
        return this;
    }

    public boolean isEmpty() {
        boolean emptyMarker = isPresent(EMPTY_CART_MARKER);
        boolean noItems = driver.findElements(CART_ITEM).isEmpty();
        boolean ariaEmpty = isCartIconAriaEmpty();
        boolean empty = emptyMarker || noItems || ariaEmpty;
        log.info("Cart empty check => marker={}, no items={}, aria={} -> {}",
                emptyMarker, noItems, ariaEmpty, empty);
        return empty;
    }

    private void clickButtonInsideItem(By buttonLocator) {
        WebElement item = visible(CART_ITEM);
        WebElement button = item.findElements(buttonLocator).stream()
                .findFirst()
                .orElseGet(() -> wait.clickable(buttonLocator));
        scrollIntoView(button);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
    }

    private boolean isCartIconAriaEmpty() {
        try {
            String aria = driver.findElement(CART_ICON).getAttribute("aria-label");
            return aria != null && aria.toLowerCase().contains("boş");
        } catch (Exception e) {
            return false;
        }
    }

    private void dumpCartForDebugging(String label) {
        try {
            String html = (String) ((JavascriptExecutor) driver).executeScript(
                    "const item = document.querySelector(\"li[data-qa-qualifier='shop-cart-item'], " +
                    "li.shop-cart-item, li[class*='shop-cart-item'], [class*='cart-item'][class*='grid']\");" +
                    "return item ? item.outerHTML : '<no cart item found>';");
            log.error("=== Cart item HTML dump ({}):\n{}\n=== end dump", label, html);
        } catch (Exception e) {
            log.error("Failed to dump cart item HTML: {}", e.getMessage());
        }
    }
}
