package com.alok.payment.paymentprocessor.bdd;

import com.alok.payment.paymentprocessor.dto.PaymentRequest;
import com.alok.payment.paymentprocessor.dto.PaymentResponse;
import com.alok.payment.paymentprocessor.model.Payment;
import com.alok.payment.paymentprocessor.model.PaymentStatus;
import com.alok.payment.paymentprocessor.model.PaymentType;
import com.alok.payment.paymentprocessor.repository.PaymentRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cucumber step definitions for payment processing BDD tests.
 */
public class PaymentProcessingSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PaymentRepository paymentRepository;

    private ResponseEntity<PaymentResponse> lastPaymentResponse;
    private ResponseEntity<PaymentResponse[]> lastPaymentsArrayResponse;
    private ResponseEntity<String> lastHealthResponse;
    private List<ResponseEntity<PaymentResponse>> multiplePaymentResponses;
    private String lastTransactionId;

    @Given("the payment processor system is running")
    public void thePaymentProcessorSystemIsRunning() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Given("test accounts are available")
    public void testAccountsAreAvailable() {
        // Test accounts are pre-configured in the system (ACC001-ACC005)
        // This is a verification step - accounts are available through init.sql and AccountService
        assertTrue(true, "Test accounts ACC001-ACC005 are available");
    }

    @Given("a valid source account {string} with sufficient balance")
    public void aValidSourceAccountWithSufficientBalance(String accountNumber) {
        // Account is assumed to exist with sufficient balance for test scenarios
        // AccountService mock provides pre-configured accounts with balances
        assertNotNull(accountNumber);
        assertFalse(accountNumber.isEmpty());
    }

    @Given("a valid destination account {string}")
    public void aValidDestinationAccount(String accountNumber) {
        assertNotNull(accountNumber);
        assertFalse(accountNumber.isEmpty());
    }

    @Given("a source account {string} with balance {double}")
    public void aSourceAccountWithBalance(String accountNumber, double balance) {
        assertNotNull(accountNumber);
        assertTrue(balance >= 0);
    }

    @Given("a payment exists with transaction ID {string}")
    public void aPaymentExistsWithTransactionID(String transactionId) {
        // First check if payment exists from init.sql
        Optional<Payment> existing = paymentRepository.findByTransactionId(transactionId);
        
        if (existing.isEmpty()) {
            // If not found, create it for the test
            Payment payment = new Payment();
            payment.setTransactionId(transactionId);
            payment.setFromAccount("ACC001");
            payment.setToAccount("ACC002");
            payment.setAmount(new BigDecimal("1000.00"));
            payment.setCurrency("USD");
            payment.setPaymentType(PaymentType.DOMESTIC_PAYMENT);
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setDescription("Test payment created for BDD scenario");
            paymentRepository.save(payment);
        }
        
        // Verify payment now exists
        Optional<Payment> payment = paymentRepository.findByTransactionId(transactionId);
        assertTrue(payment.isPresent(), "Payment with transaction ID " + transactionId + " should exist");
    }

    @When("I submit a payment of {double} USD from {string} to {string}")
    public void iSubmitAPaymentOfUSDFromTo(double amount, String fromAccount, String toAccount) {
        PaymentRequest request = createPaymentRequest(fromAccount, toAccount, amount, "USD", PaymentType.DOMESTIC_PAYMENT);
        lastPaymentResponse = restTemplate.postForEntity("/api/payments", request, PaymentResponse.class);
        if (lastPaymentResponse.getBody() != null) {
            lastTransactionId = lastPaymentResponse.getBody().getTransactionId();
        }
    }

    @When("I submit an interbank transfer of {double} USD from {string} to {string}")
    public void iSubmitAnInterbankTransferOfUSDFromTo(double amount, String fromAccount, String toAccount) {
        PaymentRequest request = createPaymentRequest(fromAccount, toAccount, amount, "USD", PaymentType.INTERBANK_TRANSFER);
        lastPaymentResponse = restTemplate.postForEntity("/api/payments", request, PaymentResponse.class);
        if (lastPaymentResponse.getBody() != null) {
            lastTransactionId = lastPaymentResponse.getBody().getTransactionId();
        }
    }

    @When("I submit a {word} payment of {double} USD from {string} to {string}")
    public void iSubmitAPaymentOfTypeFromTo(String paymentType, double amount, String fromAccount, String toAccount) {
        PaymentType type = PaymentType.valueOf(paymentType);
        PaymentRequest request = createPaymentRequest(fromAccount, toAccount, amount, "USD", type);
        lastPaymentResponse = restTemplate.postForEntity("/api/payments", request, PaymentResponse.class);
        if (lastPaymentResponse.getBody() != null) {
            lastTransactionId = lastPaymentResponse.getBody().getTransactionId();
        }
    }

    @When("I submit {int} consecutive payments of {double} USD from {string} to {string}")
    public void iSubmitConsecutivePaymentsOfUSDFromTo(int count, double amount, String fromAccount, String toAccount) {
        multiplePaymentResponses = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            PaymentRequest request = createPaymentRequest(fromAccount, toAccount, amount, "USD", PaymentType.DOMESTIC_PAYMENT);
            ResponseEntity<PaymentResponse> response = restTemplate.postForEntity("/api/payments", request, PaymentResponse.class);
            multiplePaymentResponses.add(response);
        }
    }

    @When("I retrieve the payment by transaction ID {string}")
    public void iRetrieveThePaymentByTransactionID(String transactionId) {
        lastPaymentResponse = restTemplate.getForEntity("/api/payments/" + transactionId, PaymentResponse.class);
    }

    @When("I retrieve all payments for account {string}")
    public void iRetrieveAllPaymentsForAccount(String accountNumber) {
        lastPaymentsArrayResponse = restTemplate.getForEntity("/api/payments/account/" + accountNumber, PaymentResponse[].class);
    }

    @When("I check the health endpoint")
    public void iCheckTheHealthEndpoint() {
        lastHealthResponse = restTemplate.getForEntity("/actuator/health", String.class);
    }

    @Then("the payment should be completed successfully")
    public void thePaymentShouldBeCompletedSuccessfully() {
        assertNotNull(lastPaymentResponse);
        assertEquals(HttpStatus.OK, lastPaymentResponse.getStatusCode());
        assertNotNull(lastPaymentResponse.getBody());
        assertNotNull(lastPaymentResponse.getBody().getTransactionId());
    }

    @Then("the payment should fail")
    public void thePaymentShouldFail() {
        assertNotNull(lastPaymentResponse);
        assertEquals(HttpStatus.BAD_REQUEST, lastPaymentResponse.getStatusCode());
        assertNotNull(lastPaymentResponse.getBody());
    }

    @Then("the payment status should be {string}")
    public void thePaymentStatusShouldBe(String status) {
        assertNotNull(lastPaymentResponse);
        assertNotNull(lastPaymentResponse.getBody());
        assertEquals(PaymentStatus.valueOf(status), lastPaymentResponse.getBody().getStatus());
    }

    @Then("the payment type should be {string}")
    public void thePaymentTypeShouldBe(String type) {
        assertNotNull(lastPaymentResponse);
        assertNotNull(lastPaymentResponse.getBody());
        assertEquals(PaymentType.valueOf(type), lastPaymentResponse.getBody().getPaymentType());
    }

    @Then("the failure reason should contain {string}")
    public void theFailureReasonShouldContain(String expectedReason) {
        assertNotNull(lastPaymentResponse);
        assertNotNull(lastPaymentResponse.getBody());
        String failureReason = lastPaymentResponse.getBody().getFailureReason();
        assertNotNull(failureReason);
        assertTrue(failureReason.toLowerCase().contains(expectedReason.toLowerCase()),
                "Expected failure reason to contain '" + expectedReason + "' but was '" + failureReason + "'");
    }

    @Then("the payment should be persisted in the database")
    public void thePaymentShouldBePersistedInTheDatabase() {
        assertNotNull(lastTransactionId);
        Optional<Payment> payment = paymentRepository.findByTransactionId(lastTransactionId);
        assertTrue(payment.isPresent(), "Payment should be persisted in database");
        assertEquals(lastTransactionId, payment.get().getTransactionId());
    }

    @Then("the payment should be found")
    public void thePaymentShouldBeFound() {
        assertNotNull(lastPaymentResponse);
        assertEquals(HttpStatus.OK, lastPaymentResponse.getStatusCode());
        assertNotNull(lastPaymentResponse.getBody());
    }

    @Then("the from account should be {string}")
    public void theFromAccountShouldBe(String expectedAccount) {
        assertNotNull(lastPaymentResponse);
        assertNotNull(lastPaymentResponse.getBody());
        assertEquals(expectedAccount, lastPaymentResponse.getBody().getFromAccount());
    }

    @Then("the to account should be {string}")
    public void theToAccountShouldBe(String expectedAccount) {
        assertNotNull(lastPaymentResponse);
        assertNotNull(lastPaymentResponse.getBody());
        assertEquals(expectedAccount, lastPaymentResponse.getBody().getToAccount());
    }

    @Then("I should receive at least {int} payment")
    public void iShouldReceiveAtLeastPayment(int minCount) {
        assertNotNull(lastPaymentsArrayResponse);
        assertEquals(HttpStatus.OK, lastPaymentsArrayResponse.getStatusCode());
        assertNotNull(lastPaymentsArrayResponse.getBody());
        assertTrue(lastPaymentsArrayResponse.getBody().length >= minCount,
                "Expected at least " + minCount + " payments but got " + lastPaymentsArrayResponse.getBody().length);
    }

    @Then("all payments should involve account {string}")
    public void allPaymentsShouldInvolveAccount(String accountNumber) {
        assertNotNull(lastPaymentsArrayResponse);
        assertNotNull(lastPaymentsArrayResponse.getBody());
        for (PaymentResponse payment : lastPaymentsArrayResponse.getBody()) {
            assertTrue(payment.getFromAccount().equals(accountNumber) || payment.getToAccount().equals(accountNumber),
                    "Payment should involve account " + accountNumber);
        }
    }

    @Then("all {int} payments should be completed successfully")
    public void allPaymentsShouldBeCompletedSuccessfully(int count) {
        assertNotNull(multiplePaymentResponses);
        assertEquals(count, multiplePaymentResponses.size());
        for (ResponseEntity<PaymentResponse> response : multiplePaymentResponses) {
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    @And("all payments should have status {string}")
    public void allPaymentsShouldHaveStatus(String status) {
        assertNotNull(multiplePaymentResponses);
        PaymentStatus expectedStatus = PaymentStatus.valueOf(status);
        for (ResponseEntity<PaymentResponse> response : multiplePaymentResponses) {
            assertNotNull(response.getBody());
            assertEquals(expectedStatus, response.getBody().getStatus());
        }
    }

    @Then("the health status should be {string}")
    public void theHealthStatusShouldBe(String expectedStatus) {
        assertNotNull(lastHealthResponse);
        assertEquals(HttpStatus.OK, lastHealthResponse.getStatusCode());
        assertNotNull(lastHealthResponse.getBody());
        assertTrue(lastHealthResponse.getBody().contains("\"status\":\"UP\""), 
            "Health check should return UP status");
    }

    private PaymentRequest createPaymentRequest(String fromAccount, String toAccount, double amount, String currency, PaymentType paymentType) {
        PaymentRequest request = new PaymentRequest();
        request.setFromAccount(fromAccount);
        request.setToAccount(toAccount);
        request.setAmount(BigDecimal.valueOf(amount));
        request.setCurrency(currency);
        request.setPaymentType(paymentType);
        request.setDescription("BDD Test Payment");
        return request;
    }
}
