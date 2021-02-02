package tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;

public class OrderPaidByPayPalTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @Before
    public void setup() throws MalformedURLException {
        driver = new RemoteWebDriver(
                new URL("http://localhost:4444/wd/hub"),
                new FirefoxOptions()
        );
        wait = new WebDriverWait(driver, 10);
    }

    /*@After
    public void cleanup() {
        driver.quit();
    }*/

    @Test
    public void orderPaidByPayPal() {
        // 1.
        driver.get("https://www.deindeal.ch/fr/");

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.className("header_menu_account"))).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.className("jsregister"))).click();

        WebElement userElement = driver.findElement(By.className("ui-autocomplete-input"));
        userElement.sendKeys("name@provider.com");

        WebElement passwordElement = driver.findElement(By.name("password"));
        passwordElement.sendKeys("Qw!@1234");

        Select selectCity = new Select(driver.findElement(By.name("my_deals_default_region")));
        selectCity.selectByValue("geneve");

        JavascriptExecutor executor = (JavascriptExecutor) driver;
        WebElement gender = driver.findElement(By.xpath("//input[@value='21749']"));
        executor.executeScript("arguments[0].click();", gender);

        driver.findElements(By.cssSelector("button[class='button button-accent button-md']")).stream()
                .filter(we -> we.getText().equals("S'inscrire"))
                .findFirst().get().click();

        System.out.println("Tried to register");

        // 2.
        // 3.
        // 4.
        // 6.
        // 7.
    }
}
