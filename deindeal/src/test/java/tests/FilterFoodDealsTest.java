package tests;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import requests.GETReply;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class FilterFoodDealsTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private boolean popupOpened;

    private static String getLastWordFromString(String src, String regex) {
        String array[] = src.split(regex);
        return array[array.length - 1];
    }

    @Before
    public void setup() throws MalformedURLException {
        driver = new RemoteWebDriver(
                new URL("http://localhost:4444/wd/hub"),
                new FirefoxOptions()
        );
        wait = new WebDriverWait(driver, 10);
        popupOpened = false;
    }

    @After
    public void cleanup() {
        driver.quit();
    }

    void managePopup(String moment) {
        if (!popupOpened) {
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.id("ju_iframe_335872")));
                popupOpened = true;
                System.out.println("\tPop-up appears " + moment + ".");
                driver.switchTo().frame(driver.findElement(By.name("ju_iframe_335872")));

                Optional<WebElement> we = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                        By.className("layer-wiziwig"))).stream()
                        .filter(w -> w.getText().equals("x"))
                        .findAny();
                if (we.isPresent()) {
                    we.get().click();
                }

                driver.switchTo().parentFrame();
            } catch (TimeoutException e) {
                System.out.println("\tPop-up does not appear " + moment + ".");
            }
        } else {
            System.out.println("\t(Pop-up does not appear " + moment + ".)");
        }
    }

    @Test
    public void filterFoodDeals() throws MalformedURLException {
        // 1.
        driver.get("https://www.deindeal.ch/fr/");

        // 2.
        driver.findElement(By.className("withIcon")).click();

        // 3.
        assertTrue(driver.getCurrentUrl().endsWith("restaurant/"));

        // 4.
        WebElement cityElement = driver.findElement(By.className("Food__cities"))
                .findElement(By.className("Food__cities-list"))
                .findElement(By.className("Food__city-wrapper"))
                .findElement(By.className("Link"));
        cityElement.click();

        // 5.
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.className("FoodFilters__wrapper")));
        String foodCity = getLastWordFromString(driver.getCurrentUrl(), "/");
        System.out.println("Selected food city: " + foodCity);
        WebElement addressFilteringElement = driver.findElement(By.className("Button__by-address-filtering"));
        assertFalse(addressFilteringElement.isEnabled());

        // 6.
        WebElement searchElement = driver.findElement(By.className("InputLocation"));
        searchElement.sendKeys("Rue Emma-Kammacher 9");

        // 7.
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.className("Button__prediction-item")));
        driver.findElement(By.className("Button__prediction-item")).click();

        // 8.
        wait.until(ExpectedConditions.elementToBeClickable(addressFilteringElement));
        System.out.println("Selected address: " + searchElement.getAttribute("value"));
        assertTrue(addressFilteringElement.isEnabled());

        // 9.
        addressFilteringElement.click();

        // 11-12. pop-up may appear before displaying filters (10.)
        managePopup("before displaying filters");

        // 10.
        wait.until(ExpectedConditions
                .elementToBeClickable(driver.findElement(By.className("Button__food-filters"))))
                .click();

        // 13.
        String popularFilter = "";
        List<WebElement> foodFiltersPopularItemElements = driver.findElements(By.className("FoodFilters__popular-item"));
        for (WebElement element : foodFiltersPopularItemElements) {
            List<WebElement> subElements = element.findElements(By.id("popular-food-filter-fd_healthy"));
            if (!subElements.isEmpty()){
                WebElement inputLabelRadioElement = element.findElement(By.className("Input__label--radio"));
                inputLabelRadioElement.click();
                popularFilter = inputLabelRadioElement.findElement(By.className("Input__label-content")).getText();
            }
        }

        // 11-12. pop-up may appear after selecting filter (11.)
        managePopup("after selecting filter");

        // 14.
        String allFilter = "";
        List<WebElement> foodFiltersAllItemElements = driver.findElements(By.className("FoodFilters__all-item"));
        for (WebElement element : foodFiltersAllItemElements) {
            List<WebElement> subElements = element.findElements(By.id("food-filter-fd_healthy"));
            if (!subElements.isEmpty()){
                WebElement inputLabelRadioElement = element.findElement(By.className("Input__label--radio"));
                allFilter = inputLabelRadioElement.findElement(By.className("Input__label-content")).getText();
            }
        }
        assertEquals(popularFilter, allFilter);

        // 15.
        URL urlWithFilter = new URL(driver.getCurrentUrl());
        String filter = getLastWordFromString(
                getLastWordFromString(urlWithFilter.getQuery(), "&"),
                "=");
        System.out.println("Selected filter: " + filter);

        // 16.
        List<WebElement> oneSaleWrapperList = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("OneSale__wrapper")));
        List<String> oneSaleWrapperIdNumberList = oneSaleWrapperList.stream()
                .map(element
                        -> getLastWordFromString(element.getAttribute("id"), "-"))
                .collect(Collectors.toList());
        System.out.println("Filtered sales: " + oneSaleWrapperIdNumberList);

        // 17.
        URL testfoodiosURL = new URL("https://testfoodios.herokuapp.com/food_city/" + foodCity);
        GETReply reply = null;
        try {
            reply = GETReply.sendGET(testfoodiosURL);
        } catch (IOException e) {
            System.out.println("Exception caught:" + e + e.toString());
        }

        // 18.
        String response = "";
        if(null != reply) {
            assertEquals(HttpURLConnection.HTTP_OK, reply.getStatusCode());
            response = reply.getResponse();
            System.out.println("GET status code (food city = " + foodCity + "): " + reply.getStatusCode());
        }

        // 19.
        JSONObject obj = new JSONObject(response);
        JSONArray items = obj.getJSONArray("items");
        List<String> GETids = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject json = items.getJSONObject(i);

            JSONArray jsonArray = json.getJSONArray("myThemes");
            List<String> themesList = new ArrayList<>();
            for(int j = 0; j < jsonArray.length(); j++) {
                themesList.add(jsonArray.getString(j));
            }

            if (themesList.contains(filter)){
                GETids.add(String.valueOf(json.getInt("id")));
            }
        }
        System.out.println("GET ids with " + filter + " filter: " + GETids);

        // 20.
        Collections.sort(oneSaleWrapperIdNumberList);
        Collections.sort(GETids);
        assertEquals(oneSaleWrapperIdNumberList, GETids);
    }
}