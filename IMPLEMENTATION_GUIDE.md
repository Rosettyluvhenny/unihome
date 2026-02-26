# Transaction System Implementation Guide

## Summary

A complete payment transaction system has been implemented for the UniHome project with automatic expiration handling
using Redis. The system manages the full lifecycle of payment transactions and automatically synchronizes order
statuses.

## What Was Created

### 1. Database Migration (V21__fix_payment_and_transaction.sql)

- **Location**: `src/main/resources/db/migration/V21__fix_payment_and_transaction.sql`
- **Purpose**: Fixes and properly structures payment and transaction tables
- **Changes**:
  - Fixed transaction table ID type to VARCHAR(50)
  - Added proper constraints and indexes
  - Added expired_at column with proper NULL default
  - Added CREATE INDEX IF NOT EXISTS statements

### 2. Enums

#### TransactionStatus.java

- **Location**: `src/main/java/com/exe/unihome/persistence/enums/TransactionStatus.java`
- **Values**:
  - `PENDING`: Default status when transaction is created
  - `SUCCESS`: Payment successfully confirmed
  - `CANCEL`: Transaction manually cancelled
  - `EXPIRED`: Transaction automatically expired

### 3. Entity Classes

#### Payment.java

- **Location**: `src/main/java/com/exe/unihome/persistence/entity/payment/Payment.java`
- **Fields**:
  - `id` (String, PK): Payment method identifier
  - `name`: Payment method name
  - `isActive`: Active/inactive status
  - `createdAt`, `updatedAt`: Timestamps

#### Transaction.java

- **Location**: `src/main/java/com/exe/unihome/persistence/entity/payment/Transaction.java`
- **Fields**:
  - `id` (String, UUID): Transaction unique identifier
  - `order`: Many-to-One relationship with Order
  - `payment`: Many-to-One relationship with Payment
  - `status` (TransactionStatus): Current transaction status
  - `url`: Payment gateway URL
  - `paidAt`: Payment confirmation timestamp
  - `expiredAt`: Transaction expiration time
  - `createdAt`, `updatedAt`: Timestamps

### 4. Repositories

#### TransactionRepository.java

- **Location**: `src/main/java/com/exe/unihome/persistence/repository/TransactionRepository.java`
- **Key Methods**:
  - `findByOrder_OrderId(UUID)`: Find transaction by order
  - `findByStatus(TransactionStatus)`: Find all transactions with status
  - `findByStatusAndExpiredAtBefore()`: Find expired transactions
  - `countByOrder_OrderIdAndStatus()`: Count transactions

#### PaymentRepository.java

- **Location**: `src/main/java/com/exe/unihome/persistence/repository/PaymentRepository.java`
- **Key Methods**:
  - `findByName(String)`: Find payment by name
  - `findByIdAndIsActiveTrue()`: Find active payment

### 5. Event Classes

#### TransactionCreatedEvent.java

- Published when transaction is created
- Triggers automatic expiration scheduling

#### TransactionSuccessEvent.java

- Published when payment is confirmed
- Cancels scheduled expiration

#### TransactionCancelledEvent.java

- Published when transaction is cancelled
- Includes `automatic` flag for expiration-triggered cancellations

#### TransactionExpirationEvent.java

- Represents expiration event in Redis Delayed Queue
- Processed by scheduled task

### 6. Configuration

#### RedisConfig.java

- **Location**: `src/main/java/com/exe/unihome/config/RedisConfig.java`
- **Components**:
  - Redisson client configuration
  - RedisTemplate bean with Jackson2 serialization
  - Java time module support
  - Connection pooling settings

#### Updated JpaConfig.java

- Added Clock bean for time-based operations
- `@Bean public Clock clock()`

### 7. Services

#### TransactionService Interface

- **Location**: `src/main/java/com/exe/unihome/service/TransactionService.java`
- **Methods**:
  - `createTransaction()`: Create new transaction
  - `confirmTransaction()`: Confirm payment
  - `cancelTransaction()`: Cancel transaction
  - `getTransaction()`: Retrieve by ID
  - `getTransactionByOrderId()`: Retrieve by order
  - `updateTransactionStatus()`: Update status
  - `isTransactionActive()`: Check if active

#### TransactionServiceImpl

- **Location**: `src/main/java/com/exe/unihome/service/impl/TransactionServiceImpl.java`
- **Features**:
  - `@Transactional` for data consistency
  - Event publishing for async processing
  - Automatic order status updates
  - Comprehensive error handling

#### TransactionExpirationService

- **Location**: `src/main/java/com/exe/unihome/service/impl/TransactionExpirationService.java`
- **Key Methods**:
  - `scheduleTransactionExpiration()`: Schedule auto-expiration in Redis
  - `cancelTransactionExpiration()`: Remove from expiration queue
  - `processExpiredTransactions()`: Scheduled task (runs every 5 seconds)
  - `processTransactionExpiration()`: Process individual expiration
  - `expireTransaction()`: Update transaction and order status to CANCELLED

### 8. Event Listener

#### TransactionEventListener

- **Location**: `src/main/java/com/exe/unihome/service/impl/TransactionEventListener.java`
- **Event Handlers**:
  - `handleTransactionCreated()`: Schedules expiration
  - `handleTransactionSuccess()`: Cancels scheduled expiration
  - `handleTransactionCancelled()`: Cleans up expiration (if manual)

### 9. DTOs

#### CreateTransactionRequest.java

- **Location**: `src/main/java/com/exe/unihome/dto/payment/request/CreateTransactionRequest.java`
- **Fields**: `orderId`, `paymentMethodId`, `paymentUrl`

#### CancelTransactionRequest.java

- **Location**: `src/main/java/com/exe/unihome/dto/payment/request/CancelTransactionRequest.java`
- **Fields**: `reason`

#### TransactionResponse.java

- **Location**: `src/main/java/com/exe/unihome/dto/payment/response/TransactionResponse.java`
- **Fields**: All transaction details including timestamps

### 10. Mapper

#### TransactionMapper.java

- **Location**: `src/main/java/com/exe/unihome/mapper/TransactionMapper.java`
- Uses MapStruct for entity-to-DTO conversion

### 11. Controller

#### TransactionController

- **Location**: `src/main/java/com/exe/unihome/controller/TransactionController.java`
- **Endpoints**:
  - `POST /api/transactions`: Create transaction
  - `GET /api/transactions/{id}`: Get by transaction ID
  - `GET /api/transactions/order/{orderId}`: Get by order ID
  - `PUT /api/transactions/{id}/confirm`: Confirm payment
  - `PUT /api/transactions/{id}/cancel`: Cancel transaction
  - `GET /api/transactions/{id}/active`: Check if active

### 12. Configuration Updates

#### application.yaml

- Added transaction expiration configuration:
  ```yaml
  app:
    transaction:
      expiration:
        minutes: 15  # Configurable via TRANSACTION_EXPIRATION_MINUTES env var
  ```

## Order Status Update Flow

The system automatically updates order status based on transaction status:

```
Transaction Creation:
  - Order Status: PENDING (unchanged)
  - Transaction Status: PENDING

Payment Confirmed:
  - Order Status: PENDING → SHIPPING
  - Transaction Status: PENDING → SUCCESS

Manual Cancellation:
  - Order Status: PENDING → CANCELLED
  - Transaction Status: PENDING → CANCEL

Automatic Expiration (after 15 minutes):
  - Order Status: PENDING → CANCELLED
  - Transaction Status: PENDING → EXPIRED
```

## Redis Integration

### Delayed Queue Processing

- Uses Redisson's `RDelayedQueue` for scheduling expiration
- Events automatically triggered after EXPIRATION_MINUTES
- Scheduled task processes queue every 5 seconds

### Caching Strategy

- Cache key pattern: `transaction:cache:{transactionId}`
- Cached for same duration as expiration time
- Fallback to database if cache miss

### Queue Management

- Queue name: `transaction:expiration:queue`
- Async processing with `@Async` annotation
- Thread-safe operations

## Implementation Highlights

### 1. Event-Driven Architecture

- Loose coupling between services
- Async processing with Spring Events
- Easy to add new listeners without modifying existing code

### 2. Automatic Expiration

- No polling from database
- Uses Redis Delayed Queues for efficiency
- Scheduled task for reliable processing

### 3. Order Status Synchronization

- Automatic order status updates
- Synchronization with transaction status changes
- Supports status transitions: PENDING → SHIPPING, CANCELLED, EXPIRED

### 4. Transaction Safety

- `@Transactional` annotations for database consistency
- Proper error handling and logging
- UUID-based transaction IDs for uniqueness

### 5. Extensibility

- DTOs separate API from domain model
- Mapper for loose coupling
- Service interfaces for easy testing

## Database Schema

```sql
-- Payment Table
CREATE TABLE payment
(
  id         VARCHAR(50) PRIMARY KEY,
  name       VARCHAR(100) NOT NULL,
  is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP    NOT NULL,
  updated_at TIMESTAMP    NOT NULL
);

-- Transaction Table
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

-- Indexes
CREATE INDEX idx_transaction_order_id ON transaction (order_id);
CREATE INDEX idx_transaction_payment_id ON transaction (payment_id);
CREATE INDEX idx_transaction_status ON transaction (status);
CREATE INDEX idx_transaction_created_at ON transaction (created_at);
```

## How to Use

### 1. Create a Payment Method

```java
Payment payos = Payment.builder()
  .id("payos")
  .name("PayOS")
  .isActive(true)
  .build();
paymentRepository.

save(payos);
```

### 2. Create a Transaction

```bash
POST /api/transactions
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "paymentMethodId": "payos",
  "paymentUrl": "https://payment-gateway.example.com/checkout"
}
```

### 3. Confirm Payment

```bash
PUT /api/transactions/{transactionId}/confirm
```

### 4. Cancel Transaction

```bash
PUT /api/transactions/{transactionId}/cancel
{
  "reason": "User requested cancellation"
}
```

## Environment Variables

```env
# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=optional

# Transaction Configuration
TRANSACTION_EXPIRATION_MINUTES=15
```

## Dependencies

- **Spring Data JPA**: Entity management and repositories
- **Spring Data Redis**: Redis template and operations
- **Redisson**: Distributed data structures and Delayed Queues
- **Jackson**: JSON serialization with Java Time support
- **Lombok**: Reduced boilerplate code
- **MapStruct**: Entity-to-DTO mapping

## Testing Scenarios

### 1. Transaction Creation and Expiration

- Create transaction
- Wait 15 minutes
- Verify status → EXPIRED
- Verify order status → CANCELLED

### 2. Payment Confirmation Before Expiration

- Create transaction
- Confirm within 15 minutes
- Verify status → SUCCESS
- Verify order status → SHIPPING
- Verify expiration cancelled

### 3. Manual Cancellation

- Create transaction
- Cancel before expiration
- Verify status → CANCEL
- Verify order status → CANCELLED
- Verify expiration cancelled

## Future Enhancements

1. **Webhook Integration**: Direct PayOS webhook handling
2. **Email Notifications**: Send status update emails
3. **Refund Management**: Handle partial refunds
4. **Payment History**: Detailed transaction audit log
5. **Admin Dashboard**: Monitor transactions
6. **Retry Logic**: Automatic retry for failed payments

## Troubleshooting

### Transaction Not Expiring

- Check Redis connection
- Verify `TRANSACTION_EXPIRATION_MINUTES` configuration
- Check scheduled task logs

### Order Status Not Updating

- Verify `OrderService` injection
- Check transaction repository queries
- Verify `@Transactional` annotations

### Redis Connection Issues

- Verify Redis server is running
- Check `REDIS_HOST` and `REDIS_PORT`
- Verify password if set

## Files Created Summary

| File                                 | Purpose                 |
|--------------------------------------|-------------------------|
| V21__fix_payment_and_transaction.sql | Database migration      |
| TransactionStatus.java               | Status enum             |
| Payment.java                         | Payment entity          |
| Transaction.java                     | Transaction entity      |
| TransactionRepository.java           | Transaction data access |
| PaymentRepository.java               | Payment data access     |
| TransactionService.java              | Service interface       |
| TransactionServiceImpl.java          | Service implementation  |
| TransactionExpirationService.java    | Expiration management   |
| TransactionEventListener.java        | Event handling          |
| TransactionCreatedEvent.java         | Event class             |
| TransactionCancelledEvent.java       | Event class             |
| TransactionSuccessEvent.java         | Event class             |
| TransactionExpirationEvent.java      | Event class             |
| RedisConfig.java                     | Redis configuration     |
| TransactionController.java           | REST endpoints          |
| CreateTransactionRequest.java        | Request DTO             |
| CancelTransactionRequest.java        | Request DTO             |
| TransactionResponse.java             | Response DTO            |
| TransactionMapper.java               | Entity mapper           |
| application.yaml                     | Configuration update    |
| TRANSACTION_SYSTEM.md                | System documentation    |

## Next Steps

1. Insert test data for Payment methods
2. Test transaction lifecycle through API
3. Monitor scheduled task logs
4. Integrate with PayOS webhook
5. Set up alerts for failed transactions

