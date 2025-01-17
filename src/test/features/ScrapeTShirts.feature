#Feature: Scrape Discounted T-Shirts from Myntra
#
#  Scenario: Extract discounted Van Heusen T-shirts
#    Given I navigate to the Myntra homepage
#    When I select "Men" and navigate to "T-Shirts"
#    When I filter by the brand "Van Heusen"
#    Then I extract price, discount percentage, and product links
#    And I print the sorted data to the console
#
#  Scenario: Extract discounted Roadster T-shirts
#    Given I navigate to the Myntra homepage
#    When I select "Men" and navigate to "T-Shirts"
#    When I filter by the brand "Roadster"
#    Then I extract price, discount percentage, and product links
#    And I print the sorted data to the console
Feature: Scrape Discounted T-Shirts

  Scenario Outline: Extract discounted T-shirts for a specific brand
    Given I navigate to the Myntra homepage
    When I select "<Category>" and navigate to "<Subcategory>"
    And I filter by the brand "<Brand>"
    Then I extract price, discount percentage, and product links
    And I validate and print the sorted data to the console

    Examples:
      | Category | Subcategory | Brand        |
      | Men      | T-Shirts    | Van Heusen   |
      | Men      | T-Shirts    | Roadster     |

  Scenario: Extract discounted T-shirts for an invalid brand
    Given I navigate to the Myntra homepage
    When I select "Men" and navigate to "T-Shirts"
    And I filter by the brand "InvalidBrand"
    Then I extract price, discount percentage, and product links
    And I validate and print the sorted data to the console

  Scenario: Extract discounted T-shirts with no discounts
    Given I navigate to the Myntra homepage
    When I select "Men" and navigate to "T-Shirts"
    And I filter by the brand "Arrow"
    Then I extract price, discount percentage, and product links
    And I validate and print the sorted data to the console
