package org.example.web.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class MenPage extends BasePage {

    private static final By SEE_ALL_LINK = By.xpath(
            "//a[@data-qa-action='unfold-category'][normalize-space(.)='TÜMÜNÜ GÖR']");

    public MenPage(WebDriver driver) {
        super(driver);
    }

    public MenPage clickSeeAll() {
        log.info("Clicking 'TÜMÜNÜ GÖR' under ERKEK");
        click(SEE_ALL_LINK);
        return this;
    }
}
