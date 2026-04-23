package org.example.web.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.web.utils.ConfigReader;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public final class DriverManager {

    private static final Logger LOG = LogManager.getLogger(DriverManager.class);
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {
    }

    public static WebDriver initDriver() {
        if (DRIVER.get() != null) {
            return DRIVER.get();
        }
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.NONE);
        if (ConfigReader.getBoolean("browser.headless", false)) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--no-default-browser-check");
        options.addArguments("--no-first-run");
        options.addArguments("--start-maximized");
        options.addArguments("--lang=tr-TR");
        options.addArguments("--user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation", "enable-logging"});
        options.setExperimentalOption("useAutomationExtension", false);

        ChromeDriver chromeDriver = new ChromeDriver(options);
        applyStealthScript(chromeDriver);

        chromeDriver.manage().window().maximize();
        chromeDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        chromeDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        chromeDriver.manage().timeouts().scriptTimeout(Duration.ofSeconds(60));
        DRIVER.set(chromeDriver);
        LOG.info("Initialised ChromeDriver with stealth mode");
        return chromeDriver;
    }

    private static void applyStealthScript(ChromeDriver driver) {
        String stealthJs = String.join(" ",
                "Object.defineProperty(navigator, 'webdriver', { get: () => undefined });",
                "Object.defineProperty(navigator, 'languages', { get: () => ['tr-TR', 'tr', 'en-US', 'en'] });",
                "Object.defineProperty(navigator, 'plugins', { get: () => [1, 2, 3, 4, 5] });",
                "window.chrome = window.chrome || { runtime: {} };");
        Map<String, Object> params = new HashMap<>();
        params.put("source", stealthJs);
        driver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", params);
    }

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("Driver is not initialised. Call initDriver() first.");
        }
        return driver;
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            try {
                driver.quit();
            } finally {
                DRIVER.remove();
                LOG.info("Quit ChromeDriver");
            }
        }
    }
}
