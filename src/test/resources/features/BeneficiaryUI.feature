@ui @bene @beneficiary
Feature: Beneficiary management UI
  As an end user with an existing 401k account
  I want to add a beneficiary through the UI
  So that my nomination is captured and persisted in the system

  # This scenario uses an account that must ALREADY exist (johndoe / 401K-30001).
  # If the login or account is missing the scenario fails fast - we do not create
  # data here. The Background is a single cleanup-and-log step that makes the run
  # repeatable by clearing any beneficiaries left from a previous run.
  Background:
    Given existing beneficiaries are cleared for account "401K-30001"

  @smoke @qa
  Scenario Outline: Add a beneficiary, confirm it is saved, and log out
    Given I am on the bene-app login page
    And I log in with username "johndoe" and password "demo123"
    And my 401k dashboard shows account "401K-30001"
    When I open the Beneficiaries page
    And I add a beneficiary:
      | name   | relationship   | dob   | percentage   |
      | <name> | <relationship> | <dob> | <percentage> |
    Then the beneficiary "<name>" should be listed in the table
    And the active allocation total should be <percentage>%
    And the beneficiary "<name>" should be saved in the database with percentage <percentage>
    When I log out
    Then I should be back on the login page

    Examples:
      | name     | relationship | dob        | percentage |
      | Jane Doe | SPOUSE       | 1990-05-01 | 60         |

  # Negative: a single beneficiary cannot exceed 100%. The API must reject it and
  # nothing must be written to the database. We submit 150% and assert the error
  # plus the absence of the row in both the UI table and the DB.
  @regression @negative
  Scenario Outline: Adding a beneficiary over 100% is rejected and not saved
    Given I am on the bene-app login page
    And I log in with username "johndoe" and password "demo123"
    And my 401k dashboard shows account "401K-30001"
    When I open the Beneficiaries page
    And I try to add a beneficiary:
      | name   | relationship   | dob   | percentage   |
      | <name> | <relationship> | <dob> | <percentage> |
    Then I should see an add-beneficiary error
    And the beneficiary "<name>" should not be listed in the table
    And the beneficiary "<name>" should not be saved in the database

    Examples:
      | name        | relationship | dob        | percentage |
      | Greedy Heir | CHILD        | 2001-03-03 | 150        |
