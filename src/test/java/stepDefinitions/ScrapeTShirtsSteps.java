package stepDefinitions;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.myntrascraper.tshirtscraper.Product;
import io.cucumber.java.en.*;
import org.junit.jupiter.api.Assertions;

import java.util.*;

public class ScrapeTShirtsSteps {

    // Playwright-related objects to interact with the browser
    private Playwright playwright;
    private Browser browser;
    private Page page;

    // List to hold the extracted product data
    private List<Product> extractedData;

    // Step definition to navigate to the Myntra homepage
    @Given("I navigate to the Myntra homepage")
    public void iNavigateToTheMyntraHomepage() {
        // Initialize Playwright and launch a Chromium browser instance
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false)); // Run in non-headless mode
        page = browser.newPage(); // Create a new browser page
        page.setDefaultTimeout(60000); // Set a default timeout of 60 seconds
        page.navigate("https://www.myntra.com/"); // Navigate to Myntra homepage
    }

    // Step definition to navigate to a specific category and subcategory
    @When("I select {string} and navigate to {string}")
    public void iSelectAndNavigateTo(String category, String subcategory) {
        try {
            // Construct the URL dynamically for the given category and subcategory
            String categoryUrl = "https://www.myntra.com/" + category.toLowerCase().replace(" ", "-") +
                    "-" + subcategory.toLowerCase().replace(" ", "-");
            page.navigate(categoryUrl); // Navigate to the constructed URL
            page.waitForLoadState(LoadState.NETWORKIDLE); // Wait for the network to be idle
        } catch (Exception e) {
            // Handle exceptions and provide meaningful error messages
            throw new RuntimeException("Failed to navigate to category: " + category + ", subcategory: " + subcategory);
        }
    }

    // Step definition to filter products by a specific brand
    @When("I filter by the brand {string}")
    public void iFilterByTheBrand(String brand) {
        try {
            // Construct the brand filter URL dynamically
            String brandFilterUrl = "https://www.myntra.com/men-tshirts?f=Brand%3A" + brand.replace(" ", "%20");
            page.navigate(brandFilterUrl); // Navigate to the brand filter URL
            page.waitForLoadState(LoadState.NETWORKIDLE); // Wait for the network to be idle
        } catch (Exception e) {
            // Handle exceptions and provide meaningful error messages
            throw new RuntimeException("Failed to filter by the brand: " + brand);
        }
    }

    // Step definition to extract product data
    @Then("I extract price, discount percentage, and product links")
    public void iExtractPriceDiscountPercentageAndProductLinks() {
        List<Product> extractedProducts = new ArrayList<>(); // Initialize a list to hold product data

        while (true) {
            try {
                // Select all product elements on the page
                List<ElementHandle> items = page.querySelectorAll(".product-base");

                for (ElementHandle item : items) {
                    try {
                        // Extract product details: price, discount, and link
                        String price = item.querySelector(".product-discountedPrice") != null
                                ? item.querySelector(".product-discountedPrice").innerText()
                                : null;
                        String discount = item.querySelector(".product-discountPercentage") != null
                                ? item.querySelector(".product-discountPercentage").innerText()
                                : null;
                        String link = item.querySelector("a") != null
                                ? "https://www.myntra.com" + item.querySelector("a").getAttribute("href")
                                : null;

                        // Skip items with missing data
                        if (price == null || discount == null || link == null) {
                            System.out.println("Skipping item due to missing data: Price: " + price +
                                    ", Discount: " + discount + ", Link: " + link);
                            continue;
                        }

                        // Create a Product object and add it to the list
                        Product product = new Product(price, discount, link);
                        extractedProducts.add(product);
                    } catch (Exception e) {
                        System.out.println("Error extracting item data. Skipping...");
                    }
                }

                // Check for the presence of a "Next" button and navigate if available
                ElementHandle nextButton = page.querySelector(".pagination-next");
                if (nextButton != null && nextButton.isVisible()) {
                    nextButton.click(); // Click the next button
                    page.waitForLoadState(LoadState.NETWORKIDLE); // Wait for the network to be idle
                } else {
                    break; // Exit the loop if no next button is found
                }
            } catch (Exception e) {
                // Handle exceptions and stop extraction
                throw new RuntimeException("Failed to extract product data");
            }
        }

        this.extractedData = extractedProducts; // Store the extracted data
    }

    // Step definition to validate and print the extracted data
    @Then("I validate and print the sorted data to the console")
    public void iValidateAndPrintTheSortedDataToTheConsole() {
        try {
            // Check if the extracted data is empty
            if (extractedData.isEmpty()) {
                System.out.println("No data extracted. This might be due to an invalid brand or no matching products.");
                return; // Exit if no data is available
            }

            // Sort data by discount percentage in descending order
            extractedData.sort((a, b) -> {
                int discountA = extractNumericDiscount(a.getDiscount());
                int discountB = extractNumericDiscount(b.getDiscount());
                return Integer.compare(discountB, discountA);
            });

            // Print sorted data to the console
            for (Product product : extractedData) {
                Assertions.assertNotNull(product.getPrice(), "Price should not be null!"); // Ensure price is not null
                Assertions.assertTrue(product.getPrice().matches(".*\\d.*"), "Price should be a valid number!"); // Validate price format
                System.out.println(product); // Print product details
            }
        } finally {
            // Ensure the browser is closed in any case
            browser.close();
            playwright.close();
        }
    }

    // Helper method to extract numeric values from discount strings
    private int extractNumericDiscount(String discount) {
        try {
            // Remove all non-numeric characters and parse the result as an integer
            return Integer.parseInt(discount.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0; // Return 0 if parsing fails
        }
    }
}
