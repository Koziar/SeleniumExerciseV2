package test;

import org.junit.*;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CarsTest1 {

    private static final int WAIT_MAX = 4;
    static WebDriver driver;

    @BeforeClass
    public static void setup() {
        System.setProperty("webdriver.chrome.driver","drivers/chromedriver");
        //Reset Database
        com.jayway.restassured.RestAssured.given().get("http://localhost:3000/reset");
        // Create a new instance of the Chrome driver
        driver = new ChromeDriver();
        // And now use this to visit http://localhost:3000/
        driver.get("http://localhost:3000");
    }

    @AfterClass
    public static void tearDown() {
        driver.quit();
        //Reset Database
        com.jayway.restassured.RestAssured.given().get("http://localhost:3000/reset");
    }

    @Test
    // 1. Verify that data is loaded, and the DOM is constructed (5 rows in the table)
    public void test1() throws Exception {
        (new WebDriverWait(driver, WAIT_MAX)).until((ExpectedCondition<Boolean>) (WebDriver d) -> {
            WebElement e = d.findElement(By.tagName("tbody"));
            List<WebElement> rows = e.findElements(By.tagName("tr"));
            Assert.assertThat(rows.size(), is(5));
            return true;
        });
    }

    @Test
    // 2. Write 2002 in the filter text and verify that we only see two rows
    public void test2() throws Exception {
        //No need to WAIT, since we are running test in a fixed order, we know the DOM is ready (because of the wait in test1)
        WebElement element = driver.findElement(By.id("filter"));
        element.sendKeys("2002");
        List<WebElement> rows = driver.findElements(By.xpath("//table[@class='table']/tbody/tr"));
        Assert.assertThat(rows.size(), is(2));
    }

    @Test
    // 3. Clear the text in the filter text and verify that we have the original five rows
    public void test3() throws Exception {
        WebElement filterField = driver.findElement(By.id("filter"));
        filterField.sendKeys(Keys.BACK_SPACE);
        List<WebElement> rows = driver.findElements(By.xpath("//table[@class='table']/tbody/tr"));
        Assert.assertThat(rows.size(), is(5));
    }

    @Test
    // 4. Click the sort “button” for Year, and verify that the top row contains the car with id 938 and
    // the last row the car with id = 940.
    public void test4() throws Exception {
        WebElement sortButton = driver.findElement(By.id("h_year"));
        sortButton.click();
        // Get the whole string of data, e.g: 940 2005 1/6/2005 Volvo V70 Super cool car 34.000,00 kr. Edit | Delete
        String row938 = driver.findElement(By.xpath("//tbody/tr[td='938']")).getText();
        String row940 = driver.findElement(By.xpath("//tbody/tr[td='940']")).getText();
        List<WebElement> rows = driver.findElements(By.xpath("//tbody/tr"));
        // Same here
        String topRow = rows.get(0).getText();
        String lastRow = rows.get(rows.size() - 1).getText();

        // Compare them
        Assert.assertThat(topRow, is(row938));
        Assert.assertThat(lastRow, is(row940));
    }

    @Test
    // 5. Press the edit button for the car with the id 938. Change the Description to "Cool car", and save changes.
    // Verify that the row for car with id 938 now contains "Cool car" in the Description column.
    public void test5() throws Exception {
        String newDescription = "Cool car";
        WebElement row = driver.findElement(By.xpath("//tbody/tr[td='938']"));
        WebElement edit = row.findElements(By.tagName("a")).get(0);
        edit.click();

        WebElement descriptionField = driver.findElement(By.id("description"));
        descriptionField.clear();
        descriptionField.sendKeys(newDescription);

        WebElement saveButton = driver.findElement(By.id("save"));
        saveButton.click();

        List<WebElement> row938 = driver.findElements(By.xpath("//tbody/tr[td='938']/td"));
        String description = row938.get(5).getText();

        Assert.assertThat(description, is(newDescription));
    }

    @Test
    // Click the new “Car Button”, and click the “Save Car” button.
    // Verify that we have an error message with the text “All fields are required”
    // and we still only have five rows in the all cars table.
    public void test6() throws Exception {
        String expectedMessage = "All fields are required";
        WebElement newCarButton = driver.findElement(By.id("new"));
        newCarButton.click();
        WebElement saveButton = driver.findElement(By.id("save"));
        saveButton.click();

        WebElement err = driver.findElement(By.id("submiterr"));
        String errorMessage = err.getText();
        Assert.assertThat(errorMessage, is(expectedMessage));

        List<WebElement> rows = driver.findElements(By.xpath("//table[@class='table']/tbody/tr"));
        Assert.assertThat(rows.size(), is(5));
    }

    @Test
    /*
     Click the new Car Button, and add the following values for a new car
     Year: 		2008
     Registered: 	2002-5-5
     Make: 		Kia
     Model: 		Rio
     Description: 	As new
     Price:		 31000

     Click “Save car”, and verify that the new car was added to the table with all cars .

     */
    public void test7() {
        // New Car Button
        driver.findElement(By.id("new")).click();
        // Adding values for a new car
        driver.findElement(By.id("year")).sendKeys("2008");
        driver.findElement(By.id("registered")).sendKeys("2002-5-5");
        driver.findElement(By.id("make")).sendKeys("Kia");
        driver.findElement(By.id("model")).sendKeys("Rio");
        driver.findElement(By.id("description")).sendKeys("As new");
        driver.findElement(By.id("price")).sendKeys("31000");
        // Save Car Button
        driver.findElement(By.id("save")).click();
        // Check if 6th car was added to the table
        List<WebElement> rows = driver.findElements(By.xpath("//table[@class='table']/tbody/tr"));
        Assert.assertThat(rows.size(), is(6));
    }
}
