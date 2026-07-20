@public
Feature: Public demo smoke tests (no bene-app or DB required)
  Scenarios against stable public practice sites - used to prove the
  Playwright / Cucumber / RestAssured / Jenkins pipeline works end-to-end
  without needing the bene-app UI, API or database running locally.

  @ui @smoke
  Scenario: Log into a public practice site and see the secure area
    Given I am on the public practice login page
    When I log in to the practice site with username "tomsmith" and password "SuperSecretPassword!"
    Then I should see the secure area message

  @public @api @smoke
  Scenario: Call a public REST API and get a valid post back
    When I call the public posts API for post id 1
    Then the public API response status should be 200
    And the public API response should contain a non-empty title
