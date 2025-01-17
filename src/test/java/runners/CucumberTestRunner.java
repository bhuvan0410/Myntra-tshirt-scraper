package runners;

import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/features",  // Path to your feature files
        glue = "stepDefinitions",       // Package containing step definitions
        plugin = {"pretty", "html:target/cucumber-reports.html"}  // Reports for results
)
public class CucumberTestRunner {
}
