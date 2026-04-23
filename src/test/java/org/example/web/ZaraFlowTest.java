package org.example.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.web.driver.DriverManager;
import org.example.web.pages.CartPage;
import org.example.web.pages.HomePage;
import org.example.web.pages.ProductDetailPage;
import org.example.web.pages.ProductListPage;
import org.example.web.pages.SearchPage;
import org.example.web.utils.ExcelReader;
import org.example.web.utils.ProductInfoWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ZaraFlowTest {

    private static final Logger LOG = LogManager.getLogger(ZaraFlowTest.class);

    @BeforeEach
    void setUp() {
        DriverManager.initDriver();
    }

    @AfterEach
    void tearDown() {
        DriverManager.quitDriver();
    }

    @Test
    @DisplayName("Zara: men -> search -> pick product -> cart -> qty -> remove")
    void fullZaraFlow() throws Exception {
        String keyword1;
        String keyword2;
        try (ExcelReader excel = new ExcelReader("testdata.xlsx")) {
            keyword1 = excel.read(0, 0);
            keyword2 = excel.read(0, 1);
        }
        LOG.info("Excel keywords => [1]='{}' [2]='{}'", keyword1, keyword2);

        HomePage home = new HomePage(DriverManager.getDriver()).open();

        SearchPage search = home.openMenu()
                .selectMen()
                .clickSeeAll()
                .openSearch()
                .type(keyword1)
                .clearSearch()
                .type(keyword2);

        ProductListPage listPage = search.pressEnter();

        ProductDetailPage selected = listPage.pickRandomProduct();
        String productName = selected.getProductName();
        String productPrice = selected.getProductPrice();

        new ProductInfoWriter("selected-product.txt").write(productName, productPrice);

        CartPage cart = selected.addToCart();
        assertEquals(productPrice, cart.getFirstItemPrice(),
                "Product grid price should match cart price");

        // Quantity step can fail (Zara may not render the +/- control); we still
        // want the remove + empty-cart verification to run afterwards.
        String quantityFailure = null;
        try {
            cart.increaseQuantity();
            int qty = cart.getQuantity();
            if (qty != 2) {
                quantityFailure = "After increasing quantity once, cart quantity should be 2 but was " + qty;
            }
        } catch (Exception e) {
            quantityFailure = "Quantity increase step could not complete: " + e.getMessage();
            LOG.warn("Quantity increase failed — proceeding with removal. Details: {}", e.getMessage());
        }

        cart.removeItem();
        assertTrue(cart.isEmpty(),
                "Cart should be empty after removing the only item");

        if (quantityFailure != null) {
            fail(quantityFailure);
        }
    }
}
