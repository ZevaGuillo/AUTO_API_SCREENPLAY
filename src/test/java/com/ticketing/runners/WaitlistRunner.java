package com.ticketing.runners;

import io.cucumber.junit.CucumberOptions;
import io.cucumber.junit.CucumberSerenityRunner;
import org.junit.runner.RunWith;

@RunWith(CucumberSerenityRunner.class)
@CucumberOptions(
        features = "src/test/resources/features/waitlist.feature",
        glue = {"com.ticketing.stepdefinitions", "com.ticketing.hooks"},
        plugin = {"pretty"},
        tags = "@waitlist"
)
public class WaitlistRunner {
}
