package stepDefinitions;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.myntrascraper.tshirtscraper.Product;
import io.cucumber.java.en.*;
import org.junit.jupiter.api.Assertions;

import java.util.*;

public class ScrapeTShirtsSteps {

    private Playwright playwright;
    private Browser browser;
    private Page page;
    private List<Product> extractedData;

    // Step definition to navigate to the Myntra homepage
    @Given("I navigate to the Myntra homepage")
    public void iNavigateToTheMyntraHomepage() {
        // Initialize Playwright and launch a browser instance
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false)); // Headless=false for debugging
        page = browser.newPage();
        page.setDefaultTimeout(60000); // Set default timeout for page operations
        page.navigate("https://www.myntra.com/"); // Open Myntra homepage
    }

    // Step definition to navigate to a specific category and subcategory
    @When("I select {string} and navigate to {string}")
    public void iSelectAndNavigateTo(String category, String subcategory) {
        try {
            // Construct the URL based on the category and subcategory
            String categoryUrl = "https://www.myntra.com/" + category.toLowerCase().replace(" ", "-") +
                    "-" + subcategory.toLowerCase().replace(" ", "-");
            page.navigate(categoryUrl); // Navigate to the constructed URL
            page.waitForLoadState(LoadState.NETWORKIDLE); // Wait for page to fully load
        } catch (Exception e) {
            throw new RuntimeException("Failed to navigate to category: " + category + ", subcategory: " + subcategory);
        }
    }

    // Step definition to filter products by brand
    @When("I filter by the brand {string}")
    public void iFilterByTheBrand(String brand) {
        try {
            // Wait for the filter search box to appear
            page.waitForSelector("div.filter-search-filterSearchBox",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
            page.click("div.filter-search-filterSearchBox"); // Click on the search box
            page.fill("input.filter-search-inputBox[placeholder='Search for Brand']", brand); // Enter the brand name
            page.keyboard().press("Enter"); // Trigger the search

            // Selector for the checkbox of the specified brand
            String labelSelector = "label.vertical-filters-label.common-customCheckbox:has(input[type='checkbox'][value='" + brand + "'])";

            // Check if the brand exists in the filter options
            if (page.querySelector(labelSelector) == null) {
                System.out.println("The brand '" + brand + "' does not exist or is unavailable for filtering.");
                throw new RuntimeException("Brand not found: " + brand);
            }

            // Apply the filter by clicking the brand checkbox
            page.waitForSelector(labelSelector, new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));
            page.click(labelSelector);
            page.waitForLoadState(LoadState.NETWORKIDLE); // Wait for the page to reload with the applied filter
        } catch (Exception e) {
            throw new RuntimeException("Failed to filter by the brand: " + brand, e);
        }
    }

    // Step definition to extract product data
    @Then("I extract price, discount percentage, and product links")
    public void iExtractPriceDiscountPercentageAndProductLinks() {
        List<Product> extractedProducts = new ArrayList<>();
        int pageCount = 1; // Limit to 3 pages to avoid excessive scraping

        while (pageCount <= 3) {
            try {
                List<ElementHandle> items = page.querySelectorAll(".product-base"); // Select all product items

                for (ElementHandle item : items) {
                    try {
                        // Extract price, discount, and product link
                        String price = item.querySelector(".product-discountedPrice") != null
                                ? item.querySelector(".product-discountedPrice").innerText()
                                : "N/A";
                        String discount = item.querySelector(".product-discountPercentage") != null
                                ? item.querySelector(".product-discountPercentage").innerText()
                                : "0%";
                        String link = item.querySelector("a") != null
                                ? "https://www.myntra.com" + item.querySelector("a").getAttribute("href").replaceAll("//", "/")
                                : "N/A";

                        // Clean up link formatting if necessary
                        if (link != null && !link.startsWith("https://www.myntra.com/")) {
                            link = link.replace("https://www.myntra.com", "https://www.myntra.com/");
                        }

                        // Skip items with missing price or link
                        if (price.equals("N/A") || link.equals("N/A")) {
                            System.out.println("No Discount, Discount: " + discount + ", Link: " + link);
                            continue;
                        }

                        // Add extracted product to the list
                        Product product = new Product(price, discount, link);
                        extractedProducts.add(product);
                    } catch (Exception e) {
                        System.out.println("Error extracting item data. Skipping...");
                    }
                }

                // Check for the next button and navigate to the next page
                ElementHandle nextButton = page.querySelector(".pagination-next");
                if (nextButton != null && nextButton.isVisible()) {
                    nextButton.click();
                    page.waitForLoadState(LoadState.NETWORKIDLE);
                    pageCount++;
                } else {
                    break;
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to extract product data");
            }
        }

        this.extractedData = extractedProducts; // Store the extracted data for validation
    }

    // Step definition to validate and sort extracted data
    @Then("I validate and print the sorted data to the console")
    public void iValidateAndPrintTheSortedDataToTheConsole() {
        try {
            if (extractedData.isEmpty()) {
                System.out.println("No data extracted. This might be due to an invalid brand or no matching products.");
                return;
            }

            // Sort data by discount percentage in descending order
            extractedData.sort((a, b) -> {
                int discountA = extractNumericDiscount(a.getDiscount());
                int discountB = extractNumericDiscount(b.getDiscount());
                return Integer.compare(discountB, discountA);
            });

            // Validate and print each product
            for (Product product : extractedData) {
                Assertions.assertNotNull(product.getPrice(), "Price should not be null!");
                Assertions.assertTrue(product.getPrice().matches(".*\\d.*"), "Price should be a valid number!");
                System.out.println(product); // Print product details
            }
        } finally {
            browser.close(); // Close browser
            playwright.close(); // Close Playwright
        }
    }

    // Utility method to extract numeric discount percentage
    private int extractNumericDiscount(String discount) {
        try {
            return Integer.parseInt(discount.replaceAll("[^0-9]", "")); // Remove non-numeric characters
        } catch (NumberFormatException e) {
            return 0; // Return 0 if no numeric discount is found
        }
    }
}
