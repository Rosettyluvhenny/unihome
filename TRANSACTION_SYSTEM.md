# Transaction System Documentation

## Overview

The Transaction System manages payment transactions for orders using Redis for automatic expiration handling. It
implements a complete transaction lifecycle with automatic status updates and order status synchronization.

## Features

- **Transaction Creation**: Create payment transactions for orders with automatic expiration
- **Payment Confirmation**: Confirm transaction payments and update order status to SHIPPING
- **Transaction Cancellation**: Cancel transactions manually and update order status to CANCELLED
- **Automatic Expiration**: Automatic expiration of pending transactions after configured time (default 15 minutes)
- **Order Status Synchronization**: Automatic order status updates based on transaction status
- **Event-Driven Architecture**: Uses Spring Events for async processing and decoupling

## Transaction Status Flow

```
PENDING (Default)
├─ → SUCCESS (Payment confirmed)
│   └─ Order Status: PENDING → SHIPPING
├─ → CANCEL (User/Admin cancelled)
│   └─ Order Status: PENDING → CANCELLED
└─ → EXPIRED (Auto-expired after 15 minutes)
    └─ Order Status: PENDING → CANCELLED
```

## Entity Structure

### Transaction Entity

- **id** (String, UUID): Primary key for transaction
- **order** (FK): Reference to Order entity
- **payment** (FK): Reference to Payment method
- **status** (TransactionStatus): Current transaction status
- **url** (String): Payment gateway URL
- **paidAt** (LocalDateTime): Payment confirmation timestamp
- **expiredAt** (LocalDateTime): Expiration time
- **createdAt** (LocalDateTime): Creation timestamp
- **updatedAt** (LocalDateTime): Last update timestamp

### Payment Entity

- **id** (String): Primary key (e.g., "payos")
- **name** (String): Payment method name
- **isActive** (Boolean): Active status
- **createdAt** (LocalDateTime): Creation timestamp
- **updatedAt** (LocalDateTime): Last update timestamp

### TransactionStatus Enum

```java
-PENDING   // Default status
-SUCCESS   // Payment successful
-CANCEL    // Transaction cancelled
-EXPIRED   // Transaction expired
```

## API Endpoints

### Create Transaction

```
POST /api/transactions
Content-Type: application/json

{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "paymentMethodId": "payos",
  "paymentUrl": "https://payment-gateway.example.com/checkout?id=123"
}

Response:
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "paymentId": "payos",
  "status": "PENDING",
  "url": "https://payment-gateway.example.com/checkout?id=123",
  "paidAt": null,
  "expiredAt": "2025-02-26T15:30:00",
  "createdAt": "2025-02-26T15:15:00",
  "updatedAt": "2025-02-26T15:15:00"
}
```

### Get Transaction by ID

```
GET /api/transactions/{transactionId}

Response: TransactionResponse
```

### Get Transaction by Order ID

```
GET /api/transactions/order/{orderId}

Response: TransactionResponse
```

### Confirm Transaction

```
PUT /api/transactions/{transactionId}/confirm

Response:
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "status": "SUCCESS",
  "paidAt": "2025-02-26T15:20:00",
  ...
}

Order Status Changes: PENDING → SHIPPING
```

### Cancel Transaction

```
PUT /api/transactions/{transactionId}/cancel
Content-Type: application/json

{
  "reason": "User cancelled payment"
}

Response:
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "status": "CANCEL",
  ...
}

Order Status Changes: PENDING → CANCELLED
```

### Check Transaction Active Status

```
GET /api/transactions/{transactionId}/active

Response: boolean
true  // Transaction is PENDING and not expired
false // Transaction is not PENDING or has expired
```

## Service Layer

### TransactionService Interface

```java
// Create a new transaction
Transaction createTransaction(UUID orderId, String paymentMethodId,
                              String paymentUrl, LocalDateTime expirationTime);

// Confirm payment and update order status to SHIPPING
void confirmTransaction(String transactionId);

// Cancel transaction and update order status to CANCELLED
void cancelTransaction(String transactionId, String reason);

// Get transaction by ID
Optional<Transaction> getTransaction(String transactionId);

// Get transaction by order ID
Optional<Transaction> getTransactionByOrderId(UUID orderId);

// Update transaction status
void updateTransactionStatus(String transactionId, TransactionStatus status);

// Check if transaction is active (PENDING and not expired)
boolean isTransactionActive(String transactionId);
```

## Redis Configuration

### RedisConfig

- Configures Redisson client for distributed operations
- Uses Redis Delayed Queues for automatic expiration scheduling
- Configures RedisTemplate for caching and general operations

### Queue Management

- **QUEUE_NAME**: `transaction:expiration:queue`
- **CACHE_PREFIX**: `transaction:cache:`
- **DEFAULT_EXPIRATION**: 15 minutes (configurable)

### Expiration Processing

- Scheduled task runs every 5 seconds to process expired transactions
- Uses Redis Delayed Queues to trigger expiration automatically
- Caches transaction data in Redis for quick access

## Event Flow

### 1. Transaction Created

```
User creates order
    ↓
TransactionServiceImpl.createTransaction()
    ↓
TransactionCreatedEvent published
    ↓
TransactionEventListener.handleTransactionCreated()
    ↓
TransactionExpirationService.scheduleTransactionExpiration()
    ↓
Redisson Delayed Queue - add expiration event with 15-minute delay
```

### 2. Payment Confirmed

```
Payment gateway confirms payment
    ↓
TransactionController.confirmTransaction()
    ↓
TransactionServiceImpl.confirmTransaction()
    ├─ Update Transaction Status: PENDING → SUCCESS
    ├─ Update Order Status: PENDING → SHIPPING
    └─ Publish TransactionSuccessEvent
        ↓
        TransactionEventListener.handleTransactionSuccess()
        ↓
        TransactionExpirationService.cancelTransactionExpiration()
        ↓
        Remove from Redis cache and expiration queue
```

### 3. Transaction Cancelled (Manual)

```
User cancels transaction
    ↓
TransactionController.cancelTransaction()
    ↓
TransactionServiceImpl.cancelTransaction()
    ├─ Update Transaction Status: PENDING → CANCEL
    ├─ Update Order Status: PENDING → CANCELLED
    └─ Publish TransactionCancelledEvent (automatic=false)
        ↓
        TransactionEventListener.handleTransactionCancelled()
        ↓
        TransactionExpirationService.cancelTransactionExpiration()
        ↓
        Remove from Redis cache and expiration queue
```

### 4. Transaction Expired (Automatic)

```
Delayed Queue triggers after 15 minutes
    ↓
TransactionExpirationService.processExpiredTransactions()
    ↓
Poll expired events from queue
    ↓
TransactionExpirationService.processTransactionExpiration()
    ├─ Check if transaction is still PENDING
    ├─ Update Transaction Status: PENDING → EXPIRED
    └─ Update Order Status: PENDING → CANCELLED
        (Note: No event published for automatic expiration)
```

## Configuration

### Application Properties

```yaml
app:
  transaction:
    expiration:
      minutes: 15  # Default expiration time in minutes
```

### Environment Variables

```
TRANSACTION_EXPIRATION_MINUTES=15
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=optional
```

## Database Schema

### Transaction Table

```sql
CREATE TABLE transaction
(
  id         VARCHAR(36) PRIMARY KEY,
  order_id   UUID        NOT NULL,
  payment_id VARCHAR(50) NOT NULL,
  status     VARCHAR(30) NOT NULL DEFAULT 'PENDING',
  url        TEXT,
  paid_at    TIMESTAMP,
  expired_at TIMESTAMP,
  created_at TIMESTAMP   NOT NULL,
  updated_at TIMESTAMP   NOT NULL,
  CONSTRAINT fk_transaction_order FOREIGN KEY (order_id) REFERENCES orders (id),
  CONSTRAINT fk_transaction_payment FOREIGN KEY (payment_id) REFERENCES payment (id)
);

CREATE INDEX idx_transaction_order_id ON transaction (order_id);
CREATE INDEX idx_transaction_payment_id ON transaction (payment_id);
CREATE INDEX idx_transaction_status ON transaction (status);
CREATE INDEX idx_transaction_created_at ON transaction (created_at);
```

### Payment Table

```sql
CREATE TABLE payment
(
  id         VARCHAR(50) PRIMARY KEY,
  name       VARCHAR(100) NOT NULL,
  is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP    NOT NULL,
  updated_at TIMESTAMP    NOT NULL
);
```

## Error Handling

### Common Exceptions

- **IllegalArgumentException**: Order not found, Payment method not found
- **IllegalStateException**: Transaction not pending, Invalid status transition
- **RuntimeException**: Database errors, Redis connection errors

### Response Status Codes

- **201 Created**: Transaction created successfully
- **200 OK**: Successful GET/PUT operations
- **400 Bad Request**: Invalid request or state transition
- **404 Not Found**: Transaction or order not found
- **500 Internal Server Error**: Server errors

## Integration Points

### Order Service Integration

- Listens for transaction status changes
- Updates order status accordingly
- Handles order cancellation when transaction expires

### Payment Gateway Integration

- Receives payment confirmation from PayOS
- Calls `confirmTransaction()` API
- Handles payment failure by cancelling transaction

### Notification Service Integration

- (Future enhancement) Send notifications on transaction status changes
- Notify user of payment confirmation/cancellation/expiration

## Thread Safety

- **TransactionServiceImpl**: Uses `@Transactional` for database operations
- **TransactionExpirationService**: Async methods with proper locking
- **TransactionEventListener**: Async event handling with `@Async`
- **Redis Operations**: Atomic operations using Redisson

## Performance Considerations

1. **Caching**: Transaction data cached in Redis for quick access
2. **Async Processing**: Event listeners run asynchronously
3. **Indexing**: Database indexes on commonly queried fields
4. **Delayed Queues**: Efficient expiration handling with Redis Delayed Queues
5. **Scheduled Tasks**: 5-second interval for processing expired transactions

## Testing

### Unit Tests

- Transaction creation with valid/invalid data
- Status transition validation
- Order status synchronization
- Exception handling

### Integration Tests

- End-to-end transaction flow
- Redis integration
- Database persistence
- Event publishing and handling

### Load Tests

- Multiple concurrent transactions
- Bulk expiration processing
- Redis memory usage

## Future Enhancements

1. **Payment Gateway Webhooks**: Direct integration with PayOS webhooks
2. **Notification Service**: Send email/SMS on transaction status changes
3. **Refund Management**: Handle refunds and partial payments
4. **Retry Logic**: Automatic retry for failed transactions
5. **Audit Logging**: Track all transaction changes for compliance
6. **Dashboard**: Admin dashboard for transaction monitoring
7. **Payment History**: Detailed payment history per user/order

