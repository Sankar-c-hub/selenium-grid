package common;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

public class WebBrowserLauncher {

    private static WebDriver driver;
    private static final String url = CommonUtil.getXMLTagValue("URL");
    private static final String browserType = CommonUtil.getXMLTagValue("BrowserType").toUpperCase();
    private static final String pageLoadTimeout = CommonUtil.getXMLTagValue("PageLoadTime");
    private static final String dockerHost = CommonUtil.getXMLTagValue("Grid");

    public static WebDriver getDriver() {
        return driver;
    }

    public static void launchApplication() {
        boolean isGridExecution = dockerHost != null && !dockerHost.isEmpty();

        try {
            if (isGridExecution) {
                String seleniumURL = dockerHost;
                System.out.println("Running on Selenium Grid: " + seleniumURL);
                driver = createRemoteWebDriver(seleniumURL, browserType);
            } else {
                System.out.println("Running tests locally");
                driver = createLocalWebDriver(browserType);
            }

            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(Integer.parseInt(pageLoadTimeout)));
            driver.get(url);
        } catch (Exception e) {
            throw new RuntimeException("Failed to launch application: " + e.getMessage(), e);
        }
    }

    private static WebDriver createRemoteWebDriver(String gridUrl, String browserType) throws MalformedURLException {
        if (browserType.contains("CHROME")) {
            ChromeOptions options = new ChromeOptions();
            applyHeadlessIfNeeded(options, browserType);
            return new RemoteWebDriver(new URL(gridUrl), options);
        } else if (browserType.contains("FIREFOX")) {
            FirefoxOptions options = new FirefoxOptions();
            applyHeadlessIfNeeded(options, browserType);
            return new RemoteWebDriver(new URL(gridUrl), options);
        } else if (browserType.contains("EDGE")) {
            EdgeOptions options = new EdgeOptions();
            applyHeadlessIfNeeded(options, browserType);
            return new RemoteWebDriver(new URL(gridUrl), options);
        } else {
            throw new IllegalArgumentException("Unsupported browser type: " + browserType);
        }
    }

    private static WebDriver createLocalWebDriver(String browserType) {
        if (browserType.contains("CHROME")) {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            applyHeadlessIfNeeded(options, browserType);
            return new ChromeDriver(options);
        } else if (browserType.contains("FIREFOX")) {
            WebDriverManager.firefoxdriver().setup();
            FirefoxOptions options = new FirefoxOptions();
            applyHeadlessIfNeeded(options, browserType);
            return new FirefoxDriver(options);
        } else if (browserType.contains("EDGE")) {
            WebDriverManager.edgedriver().setup();
            EdgeOptions options = new EdgeOptions();
            applyHeadlessIfNeeded(options, browserType);
            return new EdgeDriver(options);
        } else {
            throw new IllegalArgumentException("Unsupported browser type: " + browserType);
        }
    }

    private static void applyHeadlessIfNeeded(ChromeOptions options, String browserType) {
        if (browserType.contains("HEADLESS")) {
            options.addArguments("--headless=new", "--window-size=1920,1080", "--disable-gpu");
        }
    }

    private static void applyHeadlessIfNeeded(FirefoxOptions options, String browserType) {
        if (browserType.contains("HEADLESS")) {
            options.addArguments("--headless", "--width=1920", "--height=1080");
        }
    }

    private static void applyHeadlessIfNeeded(EdgeOptions options, String browserType) {
        if (browserType.contains("HEADLESS")) {
            options.addArguments("--headless", "--window-size=1920,1080");
        }
    }

    public static void tabSwitch(String identifier) {
        List<String> tabs = new ArrayList<>(driver.getWindowHandles());
        try {
            int index = Integer.parseInt(identifier);
            if (index >= 0 && index < tabs.size()) {
                driver.switchTo().window(tabs.get(index));
            }
        } catch (NumberFormatException e) {
            for (String handle : tabs) {
                driver.switchTo().window(handle);
                if (driver.getTitle().contains(identifier)) {
                    break;
                }
            }
        }
    }

    public static void closeBrowserInstance() {
        try {
            if (driver != null) {
                driver.quit();
            } else {
                System.out.println("No browser instance to close.");
            }
        } catch (Exception ignored) {
        } finally {
            driver = null;
        }
    }
}
