@ui @bene @login
Feature: Login UI
  As a registered bene-app user
  I want to log in and log out of the end-user portal
  So that I can securely access my 401k dashboard

  @smoke @qa
  Scenario Outline: Successful login and logout
    Given I am on the bene-app login page
    When I log in with username "<username>" and password "<password>"
    Then I should land on my dashboard
    When I log out
    Then I should be back on the login page

    Examples:
      | username | password |
      | johndoe  | demo123  |

  @regression
  Scenario Outline: Login fails and shows the expected error
    Given I am on the bene-app login page
    When I log in with username "<username>" and password "<password>"
    Then I should see a login error containing "<errorMessage>"

    Examples:
      | username | password  | errorMessage                       |
      | johndoe  | wrongpass | Invalid password for this username |
