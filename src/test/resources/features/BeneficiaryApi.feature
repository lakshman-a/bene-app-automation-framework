@api @bene @beneficiaryapi
Feature: Beneficiaries REST API
  Positive and negative API tests for /api/beneficiaries. Beneficiaries belong to
  an account, so each scenario uses the existing account 401K-30001 and sets up
  its own beneficiary data first. Every scenario is atomic and self-contained
  (no Background, no hooks): arrange the data, call the API, then validate the
  HTTP status, the response body AND the database. All data lives in the feature.

  # ---- POST ----
  @smoke
  Scenario: Add a beneficiary (POST) is stored in the DB and echoed in the response
    Given the account "401K-30001" has no beneficiaries
    When I add a beneficiary via the API with:
      | beneficiaryName | relationship | dateOfBirth | percentage | status |
      | Ada Lovelace    | CHILD        | 2005-12-10  | 40         | ACTIVE |
    Then the beneficiary API response status should be 201
    And the beneficiary API response should match the submitted data
    And the beneficiary stored in the database should match the submitted data

  # ---- GET (also proves API response == DB record via a reusable comparison) ----
  @smoke
  Scenario: Fetch a beneficiary by id (GET) returns exactly what is in the database
    Given the account "401K-30001" has a beneficiary:
      | beneficiaryName | relationship | dateOfBirth | percentage | status |
      | Katherine John  | PARENT       | 1960-08-26  | 55         | ACTIVE |
    When I get the beneficiary by its id via the API
    Then the beneficiary API response status should be 200
    And the beneficiary API response should match the submitted data
    And the beneficiary API response should equal the database record

  # ---- PUT ----
  @regression
  Scenario: Update a beneficiary (PUT) returns and persists the new values
    Given the account "401K-30001" has a beneficiary:
      | beneficiaryName | relationship | dateOfBirth | percentage | status |
      | Before Update   | SIBLING      | 1979-04-04  | 20         | ACTIVE |
    When I update the beneficiary via the API with:
      | beneficiaryName | relationship | dateOfBirth | percentage | status |
      | After Update    | SIBLING      | 1979-04-04  | 35         | ACTIVE |
    Then the beneficiary API response status should be 200
    And the beneficiary API response should match the submitted data
    And the beneficiary stored in the database should match the submitted data

  # ---- DELETE (hard delete: the row is removed) ----
  @regression
  Scenario: Delete a beneficiary (DELETE) permanently removes it from the database
    Given the account "401K-30001" has a beneficiary:
      | beneficiaryName | relationship | dateOfBirth | percentage | status |
      | Temp Heir       | OTHER        | 2000-02-02  | 10         | ACTIVE |
    When I delete the beneficiary via the API
    Then the beneficiary API response status should be 204
    And the beneficiary should not exist in the database

  # ---- Negative: percentage over 100 ----
  @negative
  Scenario: Adding a beneficiary over 100 percent is rejected (400) and not stored
    Given the account "401K-30001" has no beneficiaries
    When I add a beneficiary via the API with:
      | beneficiaryName | relationship | dateOfBirth | percentage | status |
      | Greedy Heir     | CHILD        | 2001-03-03  | 150        | ACTIVE |
    Then the beneficiary API response status should be 400
    And the beneficiary "Greedy Heir" should not be stored in the database

  # ---- Negative: fetch a non-existent beneficiary ----
  @negative
  Scenario: Fetching a non-existent beneficiary returns 404
    When I get the beneficiary by id "99999999" via the API
    Then the beneficiary API response status should be 404
