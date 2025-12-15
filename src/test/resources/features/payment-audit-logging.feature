Feature: Payment Audit Logging
  As a payment processor system
  I want to maintain comprehensive audit trails for all payment transactions
  So that all payment activities can be tracked and reviewed for compliance

  Background:
    Given the payment processor system is running
    And test accounts are available

  Scenario: Audit trail created for successful payment
    Given a valid source account "ACC001" with sufficient balance
    And a valid destination account "ACC002"
    When I submit a payment of 500.00 USD from "ACC001" to "ACC002"
    Then the payment should be completed successfully
    And an audit trail should exist for the payment
    And the audit trail should contain "PAYMENT_CREATED" action
    And the audit trail should contain "PAYMENT_COMPLETED" action
    And the audit should record "ACC001" as from account
    And the audit should record "ACC002" as to account

  Scenario: Audit trail created for failed payment due to fraud
    When I submit a payment of 75000.00 USD from "ACC001" to "ACC002"
    Then the payment should fail
    And an audit trail should exist for the payment
    And the audit trail should contain "PAYMENT_CREATED" action
    And the audit trail should contain "PAYMENT_FAILED" action
    And the audit should record fraud failure reason

  Scenario: Audit trail created for insufficient balance failure
    Given a source account "ACC005" with balance 1000.00
    When I submit a payment of 10000.00 USD from "ACC005" to "ACC002"
    Then the payment should fail
    And an audit trail should exist for the payment
    And the audit trail should contain "PAYMENT_FAILED" action
    And the audit should record insufficient balance failure

  Scenario: Audit trail records status changes
    Given a valid source account "ACC001" with sufficient balance
    And a valid destination account "ACC002"
    When I submit a payment of 300.00 USD from "ACC001" to "ACC002"
    Then the payment should be completed successfully
    And the audit trail should contain "STATUS_CHANGE" action
    And the status change audit should record old and new status

  Scenario: Audit trail contains correct payment details
    Given a valid source account "ACC002" with sufficient balance
    And a valid destination account "ACC003"
    When I submit a payment of 750.00 USD from "ACC002" to "ACC003"
    Then the payment should be completed successfully
    And the audit should record amount as 750.00
    And the audit should record currency as "USD"
    And the audit should record performer as "SYSTEM"

  Scenario: Query audit trail by transaction ID
    Given a valid source account "ACC001" with sufficient balance
    And a valid destination account "ACC002"
    When I submit a payment of 200.00 USD from "ACC001" to "ACC002"
    Then the payment should be completed successfully
    When I query audit trail by transaction ID
    Then I should receive multiple audit records
    And all audit records should have the same transaction ID

  Scenario: Query audit records by account number
    Given a valid source account "ACC001" with sufficient balance
    And a valid destination account "ACC002"
    When I submit a payment of 100.00 USD from "ACC001" to "ACC002"
    And I submit a payment of 150.00 USD from "ACC001" to "ACC003"
    When I query audit records for account "ACC001"
    Then I should receive audit records involving "ACC001"
    And all audit records should involve account "ACC001" as from or to account

  Scenario: Query audit records by action type
    Given a valid source account "ACC001" with sufficient balance
    And a valid destination account "ACC002"
    When I submit a payment of 250.00 USD from "ACC001" to "ACC002"
    When I query audit records by action "PAYMENT_CREATED"
    Then I should receive audit records with action "PAYMENT_CREATED"

  Scenario: Audit records have timestamps
    Given a valid source account "ACC001" with sufficient balance
    And a valid destination account "ACC002"
    When I submit a payment of 400.00 USD from "ACC001" to "ACC002"
    Then the payment should be completed successfully
    And all audit records should have valid timestamps
    And audit timestamps should be in chronological order

  Scenario: Multiple payments create separate audit trails
    Given a valid source account "ACC001" with sufficient balance
    And a valid destination account "ACC002"
    When I submit a payment of 50.00 USD from "ACC001" to "ACC002"
    And I submit a payment of 60.00 USD from "ACC001" to "ACC002"
    And I submit a payment of 70.00 USD from "ACC001" to "ACC002"
    Then each payment should have its own audit trail
    And the total number of audit records should be at least 6

  Scenario: Audit trail captures failure details
    When I submit a payment of 100.00 USD from "INVALID999" to "ACC002"
    Then the payment should fail
    And an audit trail should exist for the payment
    And the audit should capture detailed failure reason
    And the failure reason should contain account validation information

  Scenario: Query recent audit records with limit
    Given a valid source account "ACC001" with sufficient balance
    And a valid destination account "ACC002"
    When I submit 5 consecutive payments of 10.00 USD from "ACC001" to "ACC002"
    When I query recent audit records with limit 3
    Then I should receive at most 3 audit records
    And the audit records should be the most recent ones

  Scenario: Audit maintains data integrity for concurrent payments
    Given a valid source account "ACC001" with sufficient balance
    And a valid destination account "ACC002"
    When I submit 10 concurrent payments of 5.00 USD from "ACC001" to "ACC002"
    Then all 10 payments should be completed successfully
    And audit records should exist for all 10 payments
    And each payment should have complete audit trail
