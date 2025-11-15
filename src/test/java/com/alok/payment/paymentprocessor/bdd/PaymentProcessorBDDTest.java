package com.alok.payment.paymentprocessor.bdd;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * BDD Test Suite Runner for Cucumber tests.
 * This class uses JUnit Platform Suite to execute Cucumber BDD tests.
 * 
 * To run: mvn failsafe:integration-test -Dit.test=PaymentProcessorBDDTest
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.alok.payment.paymentprocessor.bdd")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-reports/cucumber.html, json:target/cucumber-reports/cucumber.json, junit:target/cucumber-reports/cucumber.xml")
public class PaymentProcessorBDDTest {
    // This class serves as the entry point for running BDD tests
    // The actual test scenarios are defined in .feature files
    // and step definitions are in PaymentProcessingSteps.java
}
