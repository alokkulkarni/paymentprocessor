-- Payment Processor Database Schema

-- Drop table if exists
DROP TABLE IF EXISTS payments;

-- Create payments table
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL UNIQUE,
    from_account VARCHAR(100) NOT NULL,
    to_account VARCHAR(100) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    payment_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    description TEXT,
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_payments_transaction_id ON payments(transaction_id);
CREATE INDEX idx_payments_from_account ON payments(from_account);
CREATE INDEX idx_payments_to_account ON payments(to_account);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_created_at ON payments(created_at);

-- Comments for documentation
COMMENT ON TABLE payments IS 'Stores payment transaction records';
COMMENT ON COLUMN payments.transaction_id IS 'Unique transaction identifier (UUID)';
COMMENT ON COLUMN payments.from_account IS 'Source account number';
COMMENT ON COLUMN payments.to_account IS 'Destination account number';
COMMENT ON COLUMN payments.amount IS 'Payment amount';
COMMENT ON COLUMN payments.currency IS 'Currency code (e.g., USD, EUR)';
COMMENT ON COLUMN payments.payment_type IS 'Type of payment (DOMESTIC_PAYMENT, DOMESTIC_TRANSFER, etc.)';
COMMENT ON COLUMN payments.status IS 'Current payment status (PENDING, COMPLETED, FAILED, etc.)';
COMMENT ON COLUMN payments.description IS 'Payment description or memo';
COMMENT ON COLUMN payments.failure_reason IS 'Reason for payment failure if applicable';
COMMENT ON COLUMN payments.created_at IS 'Timestamp when payment was initiated';
COMMENT ON COLUMN payments.updated_at IS 'Timestamp of last status update';

-- Drop payment_audit table if exists
DROP TABLE IF EXISTS payment_audit;

-- Create payment_audit table
CREATE TABLE payment_audit (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    from_account VARCHAR(100),
    to_account VARCHAR(100),
    amount DECIMAL(19, 2),
    currency VARCHAR(3),
    performed_by VARCHAR(100),
    details TEXT,
    audit_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for payment_audit table
CREATE INDEX idx_payment_audit_transaction_id ON payment_audit(transaction_id);
CREATE INDEX idx_payment_audit_action ON payment_audit(action);
CREATE INDEX idx_payment_audit_timestamp ON payment_audit(audit_timestamp);
CREATE INDEX idx_payment_audit_from_account ON payment_audit(from_account);
CREATE INDEX idx_payment_audit_to_account ON payment_audit(to_account);

-- Comments for payment_audit table
COMMENT ON TABLE payment_audit IS 'Stores audit trail for all payment transactions';
COMMENT ON COLUMN payment_audit.transaction_id IS 'Payment transaction identifier';
COMMENT ON COLUMN payment_audit.action IS 'Audit action type (PAYMENT_CREATED, STATUS_CHANGE, etc.)';
COMMENT ON COLUMN payment_audit.old_status IS 'Previous payment status';
COMMENT ON COLUMN payment_audit.new_status IS 'New payment status';
COMMENT ON COLUMN payment_audit.from_account IS 'Source account number';
COMMENT ON COLUMN payment_audit.to_account IS 'Destination account number';
COMMENT ON COLUMN payment_audit.amount IS 'Payment amount';
COMMENT ON COLUMN payment_audit.currency IS 'Currency code';
COMMENT ON COLUMN payment_audit.performed_by IS 'User or system that performed the action';
COMMENT ON COLUMN payment_audit.details IS 'Additional audit details';
COMMENT ON COLUMN payment_audit.audit_timestamp IS 'Timestamp when audit record was created';

