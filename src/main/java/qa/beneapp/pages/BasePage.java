package qa.beneapp.pages;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;

import qa.beneapp.driver.PlaywrightDriver;

/**
 * BasePage - Common actions shared across all Page Objects.
 *
 * Every page object extends this class and gets access to the Playwright Page
 * instance plus wrapper methods for click, type, getText, waitFor, etc., with
 * built-in logging for every action.
 */
public abstract class BasePage {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    protected Page page;

    public BasePage() {
        this.page = PlaywrightDriver.getPage();
    }

    // ==================== Navigation ====================
    protected void navigateTo(String url) {
        log.info("Navigating to: {}", url);
        page.navigate(url);
    }

    protected String getCurrentUrl() {
        return page.url();
    }

    protected String getTitle() {
        return page.title();
    }

    // ==================== Element Actions ====================
    protected void click(String selector) {
        log.debug("Clicking element: {}", selector);
        page.locator(selector).click();
    }

    protected void clickLinkByRoleFirst(String name) {
        log.debug("Clicking link by role: {}", name);
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(name)).first().click();
    }

    protected void clickRadioButtonByRole(String name){
         log.debug("Clicking radio button by role: {}", name);
        page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions().setName(name)).check();
    }

    protected void type(String selector, String text) {
        log.debug("Typing '{}' into element: {}", text, selector);
        page.locator(selector).fill(text);
    }

    protected void check(String selector) {
        page.locator(selector).check();
    }

    protected void uncheck(String selector) {
        page.locator(selector).uncheck();
    }

    protected void pressEnter(String selector) {
        page.locator(selector).press("Enter");
    }

    protected String getText(String selector) {
        String text = page.locator(selector).textContent();
        log.debug("Got text '{}' from element: {}", text, selector);
        return text;
    }

    protected String getInnerText(String selector) {
        return page.locator(selector).innerText();
    }

    protected String getInputValue(String selector) {
        return page.locator(selector).inputValue();
    }

    protected boolean isVisible(String selector) {
        return page.locator(selector).isVisible();
    }

    protected boolean isEnabled(String selector) {
        return page.locator(selector).isEnabled();
    }

    // ==================== Wait Helpers ====================
    protected void waitForElement(String selector) {
        log.debug("Waiting for element: {}", selector);
        page.locator(selector).waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE));
    }

    protected void waitForElementToDisappear(String selector) {
        page.locator(selector).waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN));
    }

    // ==================== Dropdown / Select (was SelectUtil) ====================
    protected void selectOption(String selector, String value) {
        log.debug("Selecting option value '{}' in: {}", value, selector);
        page.locator(selector).selectOption(value);
    }

    protected void selectOptionByLabel(String selector, String label) {
        log.debug("Selecting option label '{}' in: {}", label, selector);
        page.locator(selector).selectOption(new SelectOption().setLabel(label));
    }

    protected void selectOptionByIndex(String selector, int index) {
        page.locator(selector).selectOption(new SelectOption().setIndex(index));
    }

    protected int getOptionCount(String selector) {
        return page.locator(selector).locator("option").count();
    }

    protected List<String> getOptionTexts(String selector) {
        return page.locator(selector).locator("option").allInnerTexts();
    }

    protected String getSelectedLabel(String selector) {
        Locator selected = page.locator(selector).locator("option:checked");
        return selected.count() > 0 ? selected.first().innerText().trim() : "";
    }

    protected boolean hasOption(String selector, String label) {
        return getOptionTexts(selector).stream().anyMatch(t -> t.trim().equals(label));
    }

    // ==================== Count ====================
    protected int getElementCount(String selector) {
        return page.locator(selector).count();
    }

    // ==================== Dialogs ====================
    protected void acceptDialog(String selector) {
        page.onDialog(dialog -> dialog.accept());
    }
}
