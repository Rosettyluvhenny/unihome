# Transaction System Quick Reference

## Key Directories

```
src/main/java/com/exe/unihome/
├── persistence/
│   ├── entity/payment/
│   │   ├── Payment.java
│   │   └── Transaction.java
│   ├── enums/
│   │   └── TransactionStatus.java
│   └── repository/
│       ├── TransactionRepository.java
│       └── PaymentRepository.java
├── service/
│   ├── TransactionService.java
│   ├── model/
│   │   ├── TransactionCreatedEvent.java
│   │   ├── TransactionCancelledEvent.java
│   │   ├── TransactionSuccessEvent.java
│   │   └── TransactionExpirationEvent.java
│   └── impl/
│       ├── TransactionServiceImpl.java
│       ├── TransactionExpirationService.java
│       └── TransactionEventListener.java
├── controller/
│   └── TransactionController.java
├── mapper/
│   └── TransactionMapper.java
├── dto/payment/
│   ├── request/
│   │   ├── CreateTransactionRequest.java
│   │   └── CancelTransactionRequest.java
│   └── response/
│       └── TransactionResponse.java
└── config/
    └── RedisConfig.java
```

## Quick API Examples

### Create Transaction

```bash
curl -X POST http://localhost:8080/unihome/api/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "orderId": "550e8400-e29b-41d4-a716-446655440000",
    "paymentMethodId": "payos",
    "paymentUrl": "https://payment-gateway.example.com/checkout"
  }'
```

### Get Transaction

```bash
curl -X GET http://localhost:8080/unihome/api/transactions/{transactionId} \
  -H "Authorization: Bearer {token}"
```

### Get Transaction by Order

```bash
curl -X GET http://localhost:8080/unihome/api/transactions/order/{orderId} \
  -H "Authorization: Bearer {token}"
```

### Confirm Transaction

```bash
curl -X PUT http://localhost:8080/unihome/api/transactions/{transactionId}/confirm \
  -H "Authorization: Bearer {token}"
```

### Cancel Transaction

```bash
curl -X PUT http://localhost:8080/unihome/api/transactions/{transactionId}/cancel \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "reason": "User cancelled payment"
  }'
```

### Check if Active

```bash
curl -X GET http://localhost:8080/unihome/api/transactions/{transactionId}/active \
  -H "Authorization: Bearer {token}"
```

## Transaction Status Transitions

```
PENDING ─┬─→ SUCCESS (confirmTransaction)
         ├─→ CANCEL (cancelTransaction)
         └─→ EXPIRED (automatic, after timeout)
```

## Environment Configuration

### application.yaml

```yaml
app:
  transaction:
    expiration:
      minutes: 15

spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
```

### Environment Variables

```bash
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=
export TRANSACTION_EXPIRATION_MINUTES=15
```

## Dependency Injection Reference

### TransactionServiceImpl

```java

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
  private final TransactionRepository transactionRepository;
  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;
  private final OrderService orderService;
  private final TransactionExpirationService transactionExpirationService;
  private final ApplicationEventPublisher eventPublisher;
  private final Clock clock;
  // ...
}
```

### TransactionExpirationService

```java

@Service
@RequiredArgsConstructor
public class TransactionExpirationService {
  private final RedissonClient redissonClient;
  private final RedisTemplate<String, Object> redisTemplate;
  private final TransactionRepository transactionRepository;
  private final OrderService orderService;
  private final Clock clock;
  // ...
}
```

## Common Debugging Commands

### Check Redis Connection

```bash
redis-cli ping
# Expected output: PONG
```

### List Expired Transactions Queue

```bash
redis-cli LRANGE transaction:expiration:queue 0 -1
```

### Clear Cache for Transaction

```bash
redis-cli DEL transaction:cache:{transactionId}
```

### Check Transaction in Cache

```bash
redis-cli GET transaction:cache:{transactionId}
```

## Log Patterns to Monitor

### Transaction Creation

```
INFO: Created transaction {id} for order {orderId} with payment method {paymentMethodId}
INFO: Published TransactionCreatedEvent for transaction {id}
INFO: Scheduled transaction {id} for expiration at {expirationTime}
```

### Payment Confirmation

```
INFO: Confirmed transaction {id} with status SUCCESS
INFO: Published TransactionSuccessEvent for transaction {id}
INFO: Successfully cancelled expiration for transaction {id}
```

### Manual Cancellation

```
INFO: Cancelled transaction {id} with reason: {reason}
INFO: Published TransactionCancelledEvent for transaction {id}
```

### Automatic Expiration

```
INFO: Transaction {id} expired and order {orderId} cancelled
ERROR: Failed to expire transaction {id}: {error}
```

## Scheduled Task Details

### ProcessExpiredTransactions

- **Location**: `TransactionExpirationService.processExpiredTransactions()`
- **Schedule**: Every 5 seconds (fixed delay)
- **Purpose**: Poll Redis Delayed Queue for expired transactions
- **Annotation**: `@Scheduled(fixedDelay = 5000)`

## Database Queries Reference

### Find Transaction by Order

```java
transactionRepository.findByOrder_OrderId(orderId)
```

### Find All Pending Transactions

```java
transactionRepository.findByStatus(TransactionStatus.PENDING)
```

### Find Expired Transactions (for cleanup)

```java
transactionRepository.findByStatusAndExpiredAtBefore(
  TransactionStatus.PENDING,
  LocalDateTime.now()
)
```

## Order Status Mapping

| Transaction Status | Order Status        |
|--------------------|---------------------|
| PENDING            | PENDING (unchanged) |
| SUCCESS            | SHIPPING            |
| CANCEL             | CANCELLED           |
| EXPIRED            | CANCELLED           |

## Cache Strategy

- **Prefix**: `transaction:cache:`
- **TTL**: Same as `TRANSACTION_EXPIRATION_MINUTES`
- **Serializer**: Jackson2JsonRedisSerializer
- **Fallback**: Database query if cache miss

## Event Flow Summary

```
1. Create Transaction
   └─→ TransactionCreatedEvent
       └─→ Schedule Expiration in Redis

2. Confirm Payment (before expiration)
   └─→ Update: PENDING → SUCCESS
   └─→ Update Order: PENDING → SHIPPING
   └─→ TransactionSuccessEvent
       └─→ Cancel Scheduled Expiration

3. Cancel Transaction (before expiration)
   └─→ Update: PENDING → CANCEL
   └─→ Update Order: PENDING → CANCELLED
   └─→ TransactionCancelledEvent
       └─→ Cancel Scheduled Expiration

4. Transaction Expires (after timeout)
   └─→ Scheduled Task Triggers
   └─→ Update: PENDING → EXPIRED
   └─→ Update Order: PENDING → CANCELLED
```

## Error Handling

### Common Exceptions

- `IllegalArgumentException`: Order/Payment not found
- `IllegalStateException`: Invalid status transition
- `RuntimeException`: Database or Redis errors

### Response Codes

- **201 Created**: Transaction created
- **200 OK**: Successful operation
- **400 Bad Request**: Invalid request or state
- **404 Not Found**: Transaction/Order not found
- **500 Internal Server Error**: Server error

## Testing Checklist

- [ ] Create transaction successfully
- [ ] Confirm transaction before expiration
- [ ] Verify order status changed to SHIPPING
- [ ] Cancel transaction manually
- [ ] Verify order status changed to CANCELLED
- [ ] Create transaction and wait for expiration
- [ ] Verify transaction expired after 15 minutes
- [ ] Verify order status changed to CANCELLED
- [ ] Check Redis cache operations
- [ ] Check scheduled task logs

## Performance Tips

1. **Caching**: Transaction data cached in Redis for 15 minutes
2. **Async Events**: Use Spring `@Async` for non-blocking operations
3. **Indexing**: Database indexes on order_id, status, created_at
4. **Connection Pool**: Redis connection pool size: 10
5. **Scheduled Task**: Run every 5 seconds for efficient queue processing

## Troubleshooting Guide

### Transactions Not Expiring

**Problem**: Transactions remain PENDING after 15 minutes
**Solution**:

- Check Redis server is running
- Check scheduled task logs
- Verify TRANSACTION_EXPIRATION_MINUTES is set correctly

### Order Status Not Updating

**Problem**: Order status doesn't change with transaction status
**Solution**:

- Verify OrderService bean is created
- Check transaction repository query results
- Ensure @Transactional annotations present

### Redis Connection Error

**Problem**: Cannot connect to Redis
**Solution**:

- Verify Redis server address and port
- Check firewall settings
- Verify password if required

## Migration Notes

**Original V20__payment.sql Issues**:

- Inconsistent formatting and indentation
- Missing status default value
- Missing indexes for performance
- Transaction ID type inconsistency

**Fixed in V21__fix_payment_and_transaction.sql**:

- Standardized table definitions
- Added proper constraints
- Added performance indexes
- Added CREATE IF NOT EXISTS for safety

