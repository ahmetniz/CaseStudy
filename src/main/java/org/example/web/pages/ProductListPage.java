package org.example.web.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ProductListPage extends BasePage {

    private static final By PRODUCT_LINK_ANY = By.cssSelector("a[data-qa-action='product-click']");

    public ProductListPage(WebDriver driver) {
        super(driver);
    }

    public ProductDetailPage pickRandomProduct() {
        log.info("Waiting for search result grid and picking a random product");
        wait.visible(PRODUCT_LINK_ANY);
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 800);");
        try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        List<WebElement> links = driver.findElements(PRODUCT_LINK_ANY).stream()
                .filter(el -> el.getText() != null && !el.getText().isBlank())
                .toList();
        if (links.isEmpty()) {
            throw new IllegalStateException(
                    "No named product links rendered on the search grid. URL=" + driver.getCurrentUrl());
        }
        int index = ThreadLocalRandom.current().nextInt(links.size());
        WebElement nameLink = links.get(index);
        WebElement card = nameLink.findElement(By.xpath("ancestor::li[1]"));
        scrollIntoView(card);
        log.info("Selected product #{} of {}: '{}'", index + 1, links.size(), nameLink.getText());
        return new ProductDetailPage(driver, card);
    }
}
