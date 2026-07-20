@ui @bene @advanced
Feature: Advanced browser & network testing
  These scenarios demonstrate three Playwright capabilities that go beyond
  clicking and typing. Each one is a real, explainable QA use case on the
  bene-app. They all reuse the normal login steps.

  # ===================================================================
  # 1) NETWORK INTERCEPTION (observe traffic, don't change it)
  # -------------------------------------------------------------------
  # WHAT: We watch the real network call the UI makes to the backend.
  # WHY : It lets us assert the frontend's API contract - the right
  #       endpoint is called with the right headers (here, the Bearer
  #       auth token) - WITHOUT trusting the backend. If a developer
  #       forgot to attach the token, this fails even though the page
  #       might still look fine. The request still goes to the server
  #       (we only observe it).
  # ===================================================================
  @network
  Scenario: The UI calls the accounts API with a bearer token
    Given I am on the bene-app login page
    And I start observing requests to the accounts API
    When I log in with username "johndoe" and password "demo123"
    Then the observed accounts request should include a bearer token

  # ===================================================================
  # 2) API MOCKING (replace the response with our own)
  # -------------------------------------------------------------------
  # WHAT: We intercept the accounts API and return our OWN response
  #       (an empty list) instead of letting it reach the backend.
  # WHY : It lets us test UI states that are hard to create with real
  #       data - here, "a user with no accounts" - deterministically
  #       and without touching the database. Same trick is used to test
  #       error states (500), slow responses, huge datasets, etc., and
  #       to run UI tests with no live backend at all.
  # ===================================================================
  @mock
  Scenario: The dashboard shows the empty state when the API returns no accounts
    Given I am on the bene-app login page
    And the accounts API is mocked to return no accounts
    When I log in with username "johndoe" and password "demo123"
    Then my dashboard should show the no-accounts empty state

  # ===================================================================
  # 3) COOKIES & STORAGE STATE (session persistence)
  # -------------------------------------------------------------------
  # WHAT: After login we inspect where the session lives (a cookie and
  #       localStorage) and prove the session survives a page reload.
  # WHY : This is the foundation of "storage state" reuse: once the
  #       auth lives in cookies/localStorage, it can be saved and
  #       injected into other tests so they skip the login UI entirely
  #       - a big speed-up for large suites.
  # ===================================================================
  @session
  Scenario: The session is stored in a cookie and localStorage and survives a reload
    Given I am on the bene-app login page
    When I log in with username "johndoe" and password "demo123"
    Then a session cookie "bene_session" should be present
    And local storage should contain the logged-in user "johndoe"
    And my session should survive a page reload
