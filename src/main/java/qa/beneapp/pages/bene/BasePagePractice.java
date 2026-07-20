package qa.beneapp.pages.bene;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Locator.WaitForOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.GetByRoleOptions;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;

import qa.beneapp.driver.PlaywrightDriver;

public abstract class BasePagePractice {

   protected final Logger log = LoggerFactory.getLogger(this.getClass());
   protected Page page;

    public BasePagePractice() {
    this.page = PlaywrightDriver.getPage();
   }

   protected void navigateTo(String url){
    log.info("Navigating to : {} ", url);
    page.navigate(url);
   }

   protected String getCurrentUrl(){
    return page.url();
   }

   protected String getTitle(){
    return page.title();
   }

   protected void click(String selector){
    log.debug("Clicking element :{} ", selector);
    page.locator(selector).click();
   }
   protected void clickLinkByRoleFirst(String name){
    log.debug("clicking link by role : {}", name);
    page.getByRole(AriaRole.LINK, new GetByRoleOptions().setName(name)).first().click();

   }

   protected void type(String selector, String text){
    log.debug("clicking link by role: {}", text, selector);
    page.locator(selector).fill(text);

   }

   protected void check(String selector){
    page.locator(selector).check();
   }

   protected String getText(String selector){
    String text = page.locator(selector).textContent();
    log.info("got text from '{}' an element '{}'", text, selector);
    return text;
   }

   protected boolean isEnabled(String selector){
    return page.locator(selector).isEnabled();
   }

   protected void waitForElement(String selector){
    log.debug("waiting for an element : {}", selector);
    page.locator(selector).waitFor(new Locator.WaitForOptions()
.setState(WaitForSelectorState.VISIBLE));
   }

   protected void waitForElementToDisappear(String selector){
    log.debug("waiting for an element to disappear : {}", selector);
    page.locator(selector).waitFor(new Locator.WaitForOptions()
         .setState(WaitForSelectorState.HIDDEN));
   }

   protected void selectOptionByValue(String selector, String value ){
      log.debug("selecting option value '{}' in {} ", selector, value);
      page.locator(selector).selectOption(value);
   }
   protected void selectOptionByLabel(String selector, String label){
      log.debug("Selecting option label '{}' in '{}' ", label, selector);
      page.locator(selector).selectOption(new SelectOption().setLabel(label));

   }

   protected void selectOptionByIndex(String selector, int index){
      page.locator(selector).selectOption(new SelectOption().setIndex(index));
}
protected int getOptionCount(String selector){
   return page.locator(selector).locator("option").count();
}

protected int getElementCount(String selector){
   return page.locator(selector).count();
}

protected void acceptDialog(String selector){
   page.onDialog(dialog -> dialog.accept());
}

}


