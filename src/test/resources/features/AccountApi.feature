@api @bene @accounts
Feature: Accounts REST API
  Positive and negative API tests for /api/accounts. Every scenario is atomic and
  self-contained (no Background, no hooks): it first sets up its own data, calls
  the API, then validates the HTTP status, the response body AND the database.
  All test data lives in the feature; the base URL, headers and endpoint path
  come from config (used inside the step definitions).

  # ---- POST ----
  @smoke
  Scenario: Create an account (POST) is stored in the DB and echoed in the response
    Given no account exists with number "401K-70001"
    When I create an account via the API with:
      | accountNumber | accountHolderName | email                | dateOfBirth | ssnLastFour | totalBalance | status |
      | 401K-70001    | Grace Hopper      | grace_70001@bene.com | 1975-06-20  | 7001        | 15000.00     | ACTIVE |
    Then the account API response status should be 201
    And the account API response should match the submitted data
    And the account stored in the database should match the submitted data

  # ---- GET (also proves API response == DB record via a reusable comparison) ----
  @smoke
  Scenario: Fetch an account by id (GET) returns exactly what is in the database
    Given an account exists in the database with:
      | accountNumber | accountHolderName | email               | dateOfBirth | ssnLastFour | totalBalance | status |
      | 401K-70002    | Alan Turing       | alan_70002@bene.com | 1970-03-15  | 7002        | 28000.00     | ACTIVE |
    When I get the account by its id via the API
    Then the account API response status should be 200
    And the account API response should match the submitted data
    And the account API response should equal the database record

  # ---- PUT ----
  @regression
  Scenario: Update an account (PUT) returns and persists the new values
    Given an account exists in the database with:
      | accountNumber | accountHolderName | email              | dateOfBirth | ssnLastFour | totalBalance | status |
      | 401K-70003    | Old Name          | old_70003@bene.com | 1980-01-01  | 7003        | 9000.00      | ACTIVE |
    When I update the account via the API with:
      | accountNumber | accountHolderName | email              | dateOfBirth | ssnLastFour | totalBalance | status    |
      | 401K-70003    | New Name          | new_70003@bene.com | 1980-01-01  | 7003        | 9000.00      | SUSPENDED |
    Then the account API response status should be 200
    And the account API response should match the submitted data
    And the account stored in the database should match the submitted data

  # ---- DELETE (soft delete: status becomes CLOSED, the row stays) ----
  @regression
  Scenario: Delete an account (DELETE) soft-closes it in the database
    Given an account exists in the database with:
      | accountNumber | accountHolderName | email                | dateOfBirth | ssnLastFour | totalBalance | status |
      | 401K-70004    | To Be Closed      | close_70004@bene.com | 1985-05-05  | 7004        | 4000.00      | ACTIVE |
    When I delete the account via the API
    Then the account API response status should be 204
    And the account in the database should have status "CLOSED"

  # ---- Negative: duplicate account number ----
  @negative
  Scenario: Creating an account with a duplicate number is rejected (400) and not duplicated
    Given an account exists in the database with:
      | accountNumber | accountHolderName | email               | dateOfBirth | ssnLastFour | totalBalance | status |
      | 401K-70005    | Original Holder   | orig_70005@bene.com | 1990-09-09  | 7005        | 3000.00      | ACTIVE |
    When I create an account via the API with:
      | accountNumber | accountHolderName | email               | dateOfBirth | ssnLastFour | totalBalance | status |
      | 401K-70005    | Duplicate Holder  | dupe_70005@bene.com | 1991-10-10  | 7006        | 500.00       | ACTIVE |
    Then the account API response status should be 400
    And only one account should exist in the database with number "401K-70005"

  # ---- Negative: fetch a non-existent account ----
  @negative
  Scenario: Fetching a non-existent account returns 404
    When I get the account by id "99999999" via the API
    Then the account API response status should be 404
