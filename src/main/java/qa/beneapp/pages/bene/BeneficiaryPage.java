package qa.beneapp.pages.bene;

import qa.beneapp.pages.BasePage;

/**
 * BeneficiaryPage - Page Object for adding and viewing beneficiaries of the
 * currently selected 401k account.
 */
public class BeneficiaryPage extends BasePage {

    private static final String TITLE   = "[data-testid='beneficiaries-title']";
    private static final String NAME    = "[data-testid='bene-name']";
    private static final String DOB     = "[data-testid='bene-dob']";
    private static final String PCT     = "[data-testid='bene-percentage']";
    private static final String SUBMIT  = "[data-testid='add-beneficiary-submit']";
    private static final String ACTIVE_TOTAL = "[data-testid='active-total']";
    private static final String ERROR_TOAST = "[data-testid='toast-error']";

    public boolean isLoaded() {
        waitForElement(TITLE);
        return isVisible(TITLE);
    }

    /**
     * Select a relationship radio by its accessible name (the wrapping label
     * text, e.g. "SPOUSE"). This is robust regardless of the per-radio
     * data-testid hooks and matches the radio reliably.
     */
    private void selectRelationship(String relationship) {
        log.debug("Selecting relationship: {}", relationship);
        clickRadioButtonByRole(relationship);
    }

    /** A table row carries data-bene-name="<name>" once it is rendered. */
    private String rowByName(String name) {
        return "tr[data-bene-name='" + name + "']";
    }

    private void fillAndSubmit(String name, String relationship, String dob, String percentage) {
        type(NAME, name);
        selectRelationship(relationship);
        type(PCT, percentage);
        type(DOB, dob);
        click(SUBMIT);
    }

    /** Happy path: fill, submit and wait for the new row to appear. */
    public void addBeneficiary(String name, String relationship, String dob, String percentage) {
        log.info("Adding beneficiary '{}' ({}, {}%)", name, relationship, percentage);
        fillAndSubmit(name, relationship, dob, percentage);
        waitForElement(rowByName(name));
    }

    /** Negative path: fill and submit but DON'T wait for a row (it should be rejected). */
    public void tryAddBeneficiary(String name, String relationship, String dob, String percentage) {
        log.info("Attempting invalid beneficiary '{}' ({}, {}%)", name, relationship, percentage);
        fillAndSubmit(name, relationship, dob, percentage);
    }

    /** Waits for the error toast the API failure raises, then reports it. */
    public boolean isAddErrorShown() {
        waitForElement(ERROR_TOAST);
        return isVisible(ERROR_TOAST);
    }

    public boolean isBeneficiaryListed(String name) {
        return getElementCount(rowByName(name)) > 0;
    }

    /** Active allocation total as a number, e.g. "60%" -> 60. */
    public double getActiveTotalPercent() {
        String text = getText(ACTIVE_TOTAL).replace("%", "").trim();
        return text.isEmpty() ? 0 : Double.parseDouble(text);
    }
}
