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
