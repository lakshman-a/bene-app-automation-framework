@ui @bene @signup
Feature: Sign up UI
  As a new visitor
  I want to create an account and sign in
  So that I can reach my 401k dashboard home

  # Atomic: the user + auto-created 401k account are removed before (precondition)
  # so the same fixed data always re-runs cleanly.
  @smoke @qa
  Scenario Outline: A new end user can sign up, log in and log out
    Given no user exists with username "<username>" and email "<email>"
    When I sign up as a new end user:
      | fullName   | username   | email   | phone   | dob   | ssn   | balance   | password   |
      | <fullName> | <username> | <email> | <phone> | <dob> | <ssn> | <balance> | <password> |
    Then I should be redirected to the login page
    When I log in with username "<username>" and password "<password>"
    Then my 401k dashboard home should be visible
    When I log out
    Then I should be back on the login page

    Examples:
      | fullName       | username     | email                  | phone       | dob        | ssn  | balance | password |
      | QA Signup User | qa_signup_u1 | qa_signup_u1@email.com | +1-555-0150 | 1992-07-15 | 3344 | 30000   | Test@123 |

  # Negative: signing up with a username that already exists must be rejected by
  # the API and must NOT create any new data in the database. We deliberately do
  # NOT clear "johndoe" first - the scenario relies on it already existing.
  @regression @negative
  Scenario Outline: Sign up is rejected when the username already exists
    When I sign up as a new end user:
      | fullName   | username   | email   | phone   | dob   | ssn   | balance   | password   |
      | <fullName> | <username> | <email> | <phone> | <dob> | <ssn> | <balance> | <password> |
    Then I should see a signup error containing "<errorMessage>"
    And no account should exist in the database for email "<email>"

    Examples:
      | fullName  | username | email                     | phone       | dob        | ssn  | balance | password | errorMessage  |
      | Dupe User | johndoe  | neg_signup_dupe@email.com | +1-555-0199 | 1991-02-02 | 9999 | 1000    | Test@123 | already taken |
