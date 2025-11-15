Feature: Payment Processing
  As a payment processor system
  I want to process domestic and interbank payments
  So that users can transfer money securely

  Background:
    Given the payment processor system is running
    And test accounts are available

  Scenario: Successful domestic payment
    Given a valid source account "ACC001" with sufficient balance
    And a valid destination account "ACC002"
    When I submit a payment of 1000.00 USD from "ACC001" to "ACC002"
    Then the payment should be completed successfully
    And the payment status should be "COMPLETED"
    And the payment should be persisted in the database

  Scenario: Payment with insufficient balance
    Given a source account "ACC005" with balance 1000.00
    When I submit a payment of 5000.00 USD from "ACC005" to "ACC002"
    Then the payment should fail
    And the payment status should be "INSUFFICIENT_BALANCE"
    And the failure reason should contain "Insufficient balance"

  Scenario: Payment with invalid source account
    When I submit a payment of 500.00 USD from "INVALID999" to "ACC002"
    Then the payment should fail
    And the payment status should be "ACCOUNT_VALIDATION_FAILED"
    And the failure reason should contain "Source account validation failed"

  Scenario: Payment flagged for fraud
    When I submit a payment of 100.00 USD from "ACC001" to "ACC001"
    Then the payment should fail
    And the payment status should be "FRAUD_CHECK_FAILED"
    And the failure reason should contain "fraud"

  Scenario: Successful interbank transfer
    Given a valid source account "ACC002" with sufficient balance
    And a valid destination account "ACC003"
    When I submit an interbank transfer of 750.00 USD from "ACC002" to "ACC003"
    Then the payment should be completed successfully
    And the payment status should be "COMPLETED"
    And the payment type should be "INTERBANK_TRANSFER"

  Scenario: Retrieve payment by transaction ID
    Given a payment exists with transaction ID "TEST-TXN-001"
    When I retrieve the payment by transaction ID "TEST-TXN-001"
    Then the payment should be found
    And the payment status should be "COMPLETED"
    And the from account should be "ACC001"
    And the to account should be "ACC002"

  Scenario: Retrieve payments by account
    When I retrieve all payments for account "ACC001"
    Then I should receive at least 1 payment
    And all payments should involve account "ACC001"

  Scenario Outline: Process payments with different types
    Given a valid source account "<fromAccount>" with sufficient balance
    And a valid destination account "<toAccount>"
    When I submit a <paymentType> payment of <amount> USD from "<fromAccount>" to "<toAccount>"
    Then the payment should be completed successfully
    And the payment status should be "COMPLETED"
    And the payment type should be "<paymentType>"

    Examples:
      | fromAccount | toAccount | amount  | paymentType          |
      | ACC001      | ACC002    | 500.00  | DOMESTIC_PAYMENT     |
      | ACC002      | ACC003    | 250.00  | DOMESTIC_TRANSFER    |
      | ACC003      | ACC004    | 100.00  | INTRABANK_TRANSFER   |
      | ACC001      | ACC003    | 1000.00 | INTERBANK_TRANSFER   |

  Scenario: Sequential payment processing
    Given a valid source account "ACC002" with sufficient balance
    And a valid destination account "ACC004"
    When I submit 3 consecutive payments of 10.00 USD from "ACC002" to "ACC004"
    Then all 3 payments should be completed successfully
    And all payments should have status "COMPLETED"

  Scenario: Health check endpoint
    When I check the health endpoint
    Then the health status should be "Payment Processor is running"
