package qa.beneapp.runners;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

/**
 * TestRunner - Main Cucumber-JUnit test runner.
 *
 * This is what Maven Surefire picks up and executes.
 *
 * Key annotations:
 *   @RunWith(Cucumber.class) - Tells JUnit to use Cucumber
 *   @CucumberOptions - Configures feature paths, glue code, plugins, tags
 *
 * RUN COMMANDS:
 *   mvn test                                    → Runs all tests tagged @all
 *   mvn test -Dcucumber.filter.tags="@smoke"    → Runs only @smoke tests
 *   mvn test -Dcucumber.filter.tags="@qa"       → Runs only @qa tests
 *   mvn test -Dcucumber.filter.tags="not @regression" → Excludes @regression
 *   mvn test -P smoke                           → Uses Maven smoke profile
 *   mvn test -Dbrowser=firefox -Dheadless=false → Firefox, visible browser
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"qa.beneapp.stepdefinitions", "qa.beneapp.hooks"},
        plugin = {
                "pretty",                                           // Console output
                "html:target/cucumber-reports/cucumber.html",       // HTML report
                "json:target/cucumber-reports/cucumber.json",       // JSON report
                "qa.beneapp.hooks.ScreenshotStepListener",          // per-step screenshots (must be BEFORE allure)
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"  // Allure report
        },
        monochrome = true
        //tags = "@all"
)
public class TestRunner {
    // This class is intentionally empty.       
    // The @CucumberOptions annotation does all the configuration.
}

