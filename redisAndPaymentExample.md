# PayOS Payment System with Redis Queue Order Cancellation - Detailed Implementation Summary

## Executive Overview

The FCinema Spring project implements a sophisticated payment system using **PayOS** for payment processing and **Redis
with Redisson** for automatic order cancellation via delayed queues. The system ensures that unpaid bookings are
automatically cancelled after a configurable timeout (default: 2 minutes, but designed to be 15 minutes).

---

## 1. ARCHITECTURE OVERVIEW

### 1.1 Core Technologies

| Component              | Technology              | Purpose                                  |
|------------------------|-------------------------|------------------------------------------|
| **Payment Gateway**    | PayOS (vn.payos v1.0.1) | Vietnamese payment aggregator            |
| **Distributed Queue**  | Redisson v3.24.3        | Delayed queue for automatic cancellation |
| **Message Broker**     | Redis                   | Cache and queue storage                  |
| **Spring Integration** | Spring Boot Integration | Event-driven architecture                |
| **Async Processing**   | Spring's @Async         | Non-blocking operations                  |
| **Scheduling**         | Spring Scheduling       | Regular expiration checks                |

### 1.2 Key Design Pattern: Event-Driven Architecture

```
Booking Created → BookingCreatedEvent → PaymentEventListener 
  → PaymentExpirationService → Redis Delayed Queue 
  → PaymentExpirationService (scheduled checker) → Order Cancellation
```

---

## 2. PAYMENT FLOW - DETAILED WALKTHROUGH

### 2.1 Step 1: Booking Creation & Payment Link Generation

**File**: `BookingServiceImpl.java` (lines 75-227)

```java

@Override
@Transactional
public ApiResponse<BookingResponse> createBooking(BookingRequest request) {
  // 1. Validate user, showtime, and seats
  User user = validateUser(request.getUserId());
  Showtime showtime = validateShowtime(request.getShowtimeId());
  List<Seat> seats = validateSeats(request.getSeatIds(), showtime);

  // 2. Create and save initial booking
  Booking booking = createInitialBooking(request, user, showtime, seats);
  Booking savedBooking = bookingRepository.save(booking);

  // 3. Process combos and snacks
  processBookingCombos(savedBooking, request.getBookingCombos());
  processBookingSnacks(savedBooking, request.getBookingSnacks());

  // 4. Create payment link (only for ONLINE payments)
  if (booking.getPaymentMethod() == Booking.PaymentMethod.ONLINE) {
    createPaymentLink(savedBooking);
  }

  // 5. Return booking with payment link
  return ApiResponse.<BookingResponse>builder()
    .result(bookingMapper.toBookingResponse(savedBooking))
    .build();
}
```

### 2.2 Step 2: Payment Link Creation with PayOS

**File**: `BookingServiceImpl.java` (lines 189-226)

**Process**:

1. Extract payment details (amount, description, URLs)
2. Call `OrderService.createPaymentLink()`
3. Generate unique order code from timestamp
4. Create payment data object with return/cancel URLs
5. Call PayOS to create payment link
6. Save order code and checkout URL to booking entity
7. Publish `BookingCreatedEvent` for automatic cancellation scheduling

**Code Example**:

```java
private void createPaymentLink(Booking savedBooking) {
  try {
    CreatePaymentLinkRequest paymentRequest =
      new CreatePaymentLinkRequest(
        "FCinema order#" + savedBooking.getId(),           // Product name
        "FCinema order#" + savedBooking.getId(),           // Description
        savedBooking.getFeUrl() + payOsReturnUrl,          // Return URL
        savedBooking.getTotalPrice().intValue(),           // Price
        savedBooking.getFeUrl() + payOsReturnUrl           // Cancel URL
      );

    ObjectNode paymentResult = orderService.createPaymentLink(paymentRequest);

    if (paymentResult != null && paymentResult.get("error").asInt() == 0) {
      String orderCode = paymentResult.get("data").get("orderCode").asText();
      String checkoutUrl = paymentResult.get("data").get("checkoutUrl").asText();

      savedBooking.setPayOsCode(orderCode);      // Store order code
      savedBooking.setPayOsLink(checkoutUrl);    // Store payment URL
    }
  } catch (Exception e) {
    throw new AppException(ErrorCode.PAYMENT_LINK_CREATION_FAILED);
  }

  // *** CRITICAL: Publish event for automatic cancellation ***
  BookingCreatedEvent event = new BookingCreatedEvent(
    savedBooking.getId(),
    savedBooking.getUser().getId(),
    savedBooking.getCreatedAt(),
    savedBooking.getCreatedAt().plusMinutes(15)  // Expiration time
  );
  eventPublisher.publishEvent(event);
}
```

### 2.3 Order Code Generation

**File**: `OrderServiceImpl.java` (lines 41-49)

```java
// Generate unique order code from last 6 digits of current timestamp
String currentTimeString = String.valueOf(new Date().getTime());
long orderCode = Long.parseLong(currentTimeString.substring(currentTimeString.length() - 6));
```

**Example**:

- Current time: `1709014532123`
- Last 6 digits: `532123`
- Order Code: `532123`

### 2.4 PayOS Configuration

**File**: `PayOsConfig.java`

```java

@Configuration
public class PayOsConfig implements WebMvcConfigurer {
  @Value("${payos.clientId}")
  private String clientId;

  @Value("${payos.apiKey}")
  private String apiKey;

  @Value("${payos.checksumKey}")
  private String checksumKey;

  @Bean
  public PayOS payOS() {
    return new PayOS(clientId, apiKey, checksumKey);
  }
}
```

**Configuration (application.yaml)**:

```yaml
payos:
  clientId: ${PAYOS_CLIENT_ID:95c115a3-33ba-4cac-896d-b7ff826de2be}
  apiKey: ${PAYOS_API_KEY:b85a3670-922b-4cae-87eb-39db0841c375}
  checksumKey: ${PAYOS_CHECKSUM_KEY:636ac61625c8cd6b62904eb5cf8b6159147cdcf66129e107429fb3b1e22c1a61}
  returnUrl: ${VITE_PAYOS_FE_RETURN_URL:/return-url}

fcinema:
  payment:
    expiration:
      minutes: ${EXPIRED_PAYMENT:2}  # Configurable expiration time
```

### 2.5 PayOS API Calls

**File**: `OrderServiceImpl.java`

#### Creating Payment Link:

```java

@Override
public ObjectNode createPaymentLink(CreatePaymentLinkRequest request) {
  ObjectNode response = objectMapper.createObjectNode();
  try {
    ItemData item = ItemData.builder()
      .name(request.getProductName())
      .price(request.getPrice())
      .quantity(1)
      .build();

    PaymentData paymentData = PaymentData.builder()
      .orderCode(orderCode)
      .description(request.getDescription())
      .amount(request.getPrice())
      .item(item)
      .returnUrl(request.getReturnUrl())        // ← Return after payment
      .cancelUrl(request.getCancelUrl())        // ← User cancels payment
      .build();

    CheckoutResponseData data = payOS.createPaymentLink(paymentData);

    response.put("error", 0);
    response.put("message", "success");
    response.set("data", objectMapper.valueToTree(data));
    return response;
  } catch (Exception e) {
    response.put("error", -1);
    response.put("message", e.getMessage());
    return response;
  }
}
```

#### Getting Order Information:

```java

@Override
public ObjectNode getOrderById(long orderId) {
  ObjectNode response = objectMapper.createObjectNode();
  try {
    PaymentLinkData order = payOS.getPaymentLinkInformation(orderId);
    response.set("data", objectMapper.valueToTree(order));
    response.put("error", 0);
    response.put("message", "ok");
    return response;
  } catch (Exception e) {
    response.put("error", -1);
    response.put("message", e.getMessage());
    return response;
  }
}
```

#### Cancelling Order:

```java

@Override
public ObjectNode cancelOrder(int orderId) {
  ObjectNode response = objectMapper.createObjectNode();
  try {
    PaymentLinkData order = payOS.cancelPaymentLink(orderId, null);
    response.set("data", objectMapper.valueToTree(order));
    response.put("error", 0);
    response.put("message", "ok");
    return response;
  } catch (Exception e) {
    response.put("error", -1);
    response.put("message", e.getMessage());
    return response;
  }
}
```

---

## 3. REDIS QUEUE & AUTOMATIC CANCELLATION SYSTEM

### 3.1 Payment Expiration Service Flow

**File**: `PaymentExpirationService.java`

#### 3.1.1 Scheduling Payment Expiration

```java

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentExpirationService {

  private static final String PAYMENT_EXPIRATION_QUEUE = "payment:expiration:queue";
  private static final String PAYMENT_CACHE_PREFIX = "payment:cache:";

  @Value("${fcinema.payment.expiration.minutes}")
  private long EXPIRATION_MINUTES;  // 2 minutes (configurable)

  private final RedissonClient redissonClient;
  private final RedisTemplate<String, Object> redisTemplate;

  @Async
  public void schedulePaymentExpiration(Integer bookingId) {
    try {
      // 1. Get or create Redisson queue
      RQueue<BookingExpirationEvent> queue =
        redissonClient.getQueue(PAYMENT_EXPIRATION_QUEUE);

      // 2. Get delayed queue wrapper
      RDelayedQueue<BookingExpirationEvent> delayedQueue =
        redissonClient.getDelayedQueue(queue);

      // 3. Calculate expiration time
      LocalDateTime expirationTime = LocalDateTime.now(clock)
        .plusMinutes(EXPIRATION_MINUTES);

      // 4. Create expiration event
      BookingExpirationEvent event = new BookingExpirationEvent(
        bookingId,
        null,  // Will be populated when processing
        expirationTime,
        "Payment expiration after " + EXPIRATION_MINUTES + " minutes"
      );

      // 5. Offer event to delayed queue (it will be auto-triggered after delay)
      delayedQueue.offer(event, EXPIRATION_MINUTES, TimeUnit.MINUTES);

      // 6. Cache booking for quick access
      cachePaymentForExpiration(bookingId);

      log.info("Scheduled payment {} for expiration at {}", bookingId, expirationTime);

    } catch (Exception e) {
      log.error("Failed to schedule payment expiration for booking {}: {}",
        bookingId, e.getMessage(), e);
    }
  }

  private void cachePaymentForExpiration(Integer bookingId) {
    try {
      Booking booking = bookingService.getBooking(bookingId);
      if (booking != null) {
        redisTemplate.opsForValue().set(
          PAYMENT_CACHE_PREFIX + bookingId,
          booking,
          EXPIRATION_MINUTES,
          TimeUnit.MINUTES
        );
      }
    } catch (Exception e) {
      log.error("Failed to cache payment for booking {}: {}", bookingId, e.getMessage(), e);
    }
  }
}
```

#### 3.1.2 Processing Expired Payments (Scheduled Task)

```java

@Scheduled(fixedDelay = 5000)  // Check every 5 seconds
public void processExpiredPayments() {
  try {
    // 1. Get the Redisson queue
    RQueue<BookingExpirationEvent> queue =
      redissonClient.getQueue(PAYMENT_EXPIRATION_QUEUE);

    // 2. Poll events (returns null if queue is empty)
    BookingExpirationEvent event;
    while ((event = queue.poll()) != null) {
      processPaymentExpiration(event);
    }

  } catch (Exception e) {
    log.error("Error processing expired payments: {}", e.getMessage(), e);
  }
}

@Transactional
protected void processPaymentExpiration(BookingExpirationEvent event) {
  try {
    Integer bookingId = event.getBookingId();

    // 1. Get cached booking (or fallback to DB)
    Booking booking = getCachedPayment(bookingId);

    if (booking == null) {
      log.warn("Booking {} not found for payment expiration processing", bookingId);
      return;
    }

    // 2. Only expire if payment is still PENDING
    if (booking.getPaymentStatus() == Booking.PaymentStatus.PENDING) {
      expirePayment(booking);
    }

    // 3. Clean up cache
    redisTemplate.delete(PAYMENT_CACHE_PREFIX + bookingId);

  } catch (Exception e) {
    log.error("Error processing payment expiration for booking {}: {}",
      event.getBookingId(), e.getMessage(), e);
  }
}

@Transactional
protected void expirePayment(Booking booking) {
  try {
    bookingService.cancelBooking(booking);
    log.info("Payment expired for booking {}", booking.getId());
  } catch (Exception e) {
    log.error("Failed to expire payment for booking {}: {}",
      booking.getId(), e.getMessage(), e);
  }
}
```

### 3.2 Redis Configuration

**File**: `RedisConfig.java`

```java

@Configuration
@EnableAsync
@EnableScheduling
public class RedisConfig {

  @Value("${spring.data.redis.host:localhost}")
  private String redisHost;

  @Value("${spring.data.redis.port:6379}")
  private int redisPort;

  @Value("${spring.data.redis.password:}")
  private String redisPassword;

  // Redisson Client for distributed data structures
  @Bean
  public RedissonClient redissonClient() {
    Config config = new Config();
    String redisUrl = "redis://" + redisHost + ":" + redisPort;

    config
      .useSingleServer()
      .setAddress(redisUrl)
      .setPassword(redisPassword.isEmpty() ? null : redisPassword)
      .setConnectionMinimumIdleSize(1)
      .setConnectionPoolSize(10)
      .setRetryAttempts(3)
      .setRetryInterval(1500);

    return Redisson.create(config);
  }

  // RedisTemplate for cache operations
  @Bean
  public RedisTemplate<String, Object> redisTemplate(
    RedisConnectionFactory connectionFactory) {

    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    // Configure serialization
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    GenericJackson2JsonRedisSerializer serializer =
      new GenericJackson2JsonRedisSerializer(objectMapper);

    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(serializer);
    template.setHashKeySerializer(new StringRedisSerializer());

    template.afterPropertiesSet();
    return template;
  }
}
```

---

## 4. EVENT-DRIVEN ARCHITECTURE

### 4.1 Event Classes

#### 4.1.1 BookingCreatedEvent

- **Triggered**: When booking is created with online payment
- **Data**: `bookingId`, `userId`, `createdAt`, `expirationTime`
- **Handler**: PaymentEventListener

#### 4.1.2 BookingExpirationEvent

- **Triggered**: Programmatically when scheduled time arrives
- **Data**: `bookingId`, `expirationTime`, `reason`
- **Handler**: PaymentExpirationService

#### 4.1.3 BookingCancelledEvent

- **Triggered**: When booking is cancelled (manual or automatic)
- **Data**: `bookingId`, `userId`, `timestamp`, `reason`, `automatic` flag
- **Handler**: PaymentEventListener (to clean up Redis)

#### 4.1.4 BookingSuccessEvent

- **Triggered**: When payment is confirmed
- **Data**: `bookingId`
- **Handlers**: PaymentEventListener (cancel expiration), ReceiptEventListener (create receipt)

### 4.2 Payment Event Listener

**File**: `PaymentEventListener.java`

```java

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

  private final PaymentExpirationService paymentExpirationService;

  // When booking is created, schedule automatic cancellation
  @EventListener
  @Async
  public void handleBookingCreated(BookingCreatedEvent event) {
    log.info("Processing booking created event for booking: {}", event.getBookingId());
    try {
      paymentExpirationService.schedulePaymentExpiration(event.getBookingId());
      log.info("Successfully scheduled expiration for booking: {}", event.getBookingId());
    } catch (Exception e) {
      log.error("Failed to process booking created event for booking {}: {}",
        event.getBookingId(), e.getMessage(), e);
    }
  }

  // When booking is cancelled, clean up scheduled expiration
  @EventListener
  @Async
  public void handleBookingCancelled(BookingCancelledEvent event) {
    log.info("Processing booking cancelled event for booking: {} (automatic: {})",
      event.getBookingId(), event.isAutomatic());
    try {
      // Only cancel scheduled expiration if this is NOT the automatic expiration itself
      if (!event.isAutomatic()) {
        paymentExpirationService.cancelPaymentExpiration(event.getBookingId());
      }
      log.info("Successfully processed booking cancellation for booking: {}",
        event.getBookingId());
    } catch (Exception e) {
      log.error("Failed to process booking cancelled event for booking {}: {}",
        event.getBookingId(), e.getMessage(), e);
    }
  }
}
```

---

## 5. PAYMENT CONFIRMATION & WEBHOOK HANDLING

### 5.1 PayOS Webhook Endpoint

**File**: `PaymentController.java`

**Endpoint**: `POST /payment/payos_transfer_handler`

```java

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

  private final PayOS payOS;
  private final BookingService bookingService;

  @PostMapping(path = "/payos_transfer_handler")
  public ObjectNode payosTransferHandler(@RequestBody ObjectNode body)
    throws JsonProcessingException {

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode response = objectMapper.createObjectNode();

    try {
      log.info("Received PayOS webhook: {}", body);

      // 1. Parse webhook body
      Webhook webhookBody = objectMapper.treeToValue(body, Webhook.class);

      // 2. Verify webhook with PayOS
      WebhookData data = payOS.verifyPaymentWebhookData(webhookBody);
      log.info("Payment webhook verified successfully: {}", data);

      // 3. Process based on payment status
      if (data.getDesc().equals("success")) {
        handleSuccessfulPayment(data);
      } else {
        handleCancelledPayment(data);
      }

      response.put("error", 0);
      response.put("message", "Webhook delivered");
      response.set("data", objectMapper.valueToTree(data));

    } catch (Exception e) {
      log.error("Error processing PayOS webhook", e);
      response.put("error", -1);
      response.put("message", e.getMessage());
    }

    return response;
  }

  private void handleSuccessfulPayment(WebhookData data) {
    try {
      Long orderCode = data.getOrderCode();
      log.info("Processing successful payment for order: {}", orderCode);

      // Update booking with payment confirmation
      bookingService.confirmWebhookPayment(orderCode);

      log.info("Payment successful - Order: {}, Amount: {}, Description: {}",
        orderCode, data.getAmount(), data.getDescription());

    } catch (Exception e) {
      log.error("Error handling successful payment", e);
    }
  }

  private void handleCancelledPayment(WebhookData data) {
    try {
      Long orderCode = data.getOrderCode();
      log.info("Processing cancelled payment for order: {}", orderCode);

      // Cancel booking and PayOS order
      bookingService.cancelWebhookPayment(orderCode);

    } catch (Exception e) {
      log.error("Error handling cancelled payment", e);
    }
  }
}
```

### 5.2 Webhook Payment Confirmation Flow

**File**: `BookingServiceImpl.java`

```java

@Override
@Transactional
public void confirmWebhookPayment(Long orderCode) {
  Booking booking = bookingRepository
    .findByPayOsCode(orderCode.toString())
    .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

  log.info("Confirming payment for booking: {}", booking.getId());

  // 1. Update booking status to SUCCESS
  booking.setStatus(Booking.Status.SUCCESS);
  booking.setPaymentStatus(Booking.PaymentStatus.SUCCESS);
  booking.setPayOsLink(null);
  booking.setUpdatedAt(LocalDateTime.now(clock));
  bookingRepository.save(booking);

  // 2. Publish success event (triggers receipt creation)
  eventPublisher.publishEvent(new BookingSuccessEvent(booking.getId()));
}

@Override
@Transactional
public void cancelWebhookPayment(Long orderCode) {
  Booking booking = bookingRepository
    .findByPayOsCode(orderCode.toString())
    .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

  // 1. Update booking status to CANCELLED
  booking.setStatus(Booking.Status.CANCELLED);
  booking.setPaymentStatus(Booking.PaymentStatus.FAILED);
  booking.setPayOsLink(null);

  // 2. Cancel PayOS order
  orderService.cancelOrder(Integer.parseInt(booking.getPayOsCode()));

  // 3. Clear seats and update
  booking.getSeats().clear();
  booking.setUpdatedAt(LocalDateTime.now(clock));
  bookingRepository.save(booking);
}
```

### 5.3 Manual Payment Confirmation Endpoint

**File**: `BookingServiceImpl.java`

```java

@Override
@Transactional
public ApiResponse<Void> confirmBookingPayment(Integer id) {
  Booking booking = bookingRepository
    .findById(id)
    .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

  // Only allow confirmation if booking is pending
  if (booking.getStatus() != Booking.Status.PENDING) {
    throw new AppException(ErrorCode.BOOKING_ALREADY_PROCESSED);
  }

  // 1. Update booking and payment status
  booking.setStatus(Booking.Status.SUCCESS);
  booking.setPaymentStatus(Booking.PaymentStatus.SUCCESS);
  booking.setPayOsLink(null);
  booking.setUpdatedAt(LocalDateTime.now(clock));
  bookingRepository.save(booking);

  // 2. Publish success event (triggers receipt creation)
  eventPublisher.publishEvent(new BookingSuccessEvent(booking.getId()));

  // 3. Publish cancellation event to cancel scheduled expiration
  BookingCancelledEvent cancelExpirationEvent =
    new BookingCancelledEvent(
      booking.getId(),
      booking.getUser().getId(),
      LocalDateTime.now(clock),
      "Payment confirmed - cancelling automatic expiration",
      false  // Not automatic
    );
  eventPublisher.publishEvent(cancelExpirationEvent);

  return ApiResponse.<Void>builder().build();
}
```

---

## 6. REDIS DATA STRUCTURE & KEYS

### 6.1 Redis Keys Used

| Key                                                 | Type           | TTL                | Purpose                              |
|-----------------------------------------------------|----------------|--------------------|--------------------------------------|
| `payment:expiration:queue`                          | Redisson Queue | -                  | Main queue for expiration events     |
| `payment:cache:{bookingId}`                         | Hash           | EXPIRATION_MINUTES | Cached booking data for quick lookup |
| `redisson_queue:{payment:expiration:queue}`         | Sorted Set     | -                  | Redisson internal delayed queue      |
| `redisson_queue_timeout:{payment:expiration:queue}` | Hash           | -                  | Redisson timeout tracking            |

### 6.2 Cache Serialization Strategy

**Object Mapper Configuration**:

```java
ObjectMapper objectMapper = new ObjectMapper();
objectMapper.

registerModule(new JavaTimeModule());
  objectMapper.

disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

GenericJackson2JsonRedisSerializer serializer =
  new GenericJackson2JsonRedisSerializer(objectMapper);
```

This ensures:

- Java Time objects (LocalDateTime) are serialized as ISO format strings
- Proper deserialization of complex objects like `Booking`, `User`, `Seat`
- Compatibility with Redis data format

---

## 7. COMPLETE PAYMENT FLOW DIAGRAM

```
┌─────────────────────────────────────────────────────────────────┐
│                      BOOKING CREATION                           │
├─────────────────────────────────────────────────────────────────┤
│ POST /bookings                                                  │
│   ├─ Validate user, showtime, seats                           │
│   ├─ Save booking to database (Status: PENDING)               │
│   ├─ Process combos and snacks                                │
│   └─ Booking saved with payment method = ONLINE               │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   PAYMENT LINK CREATION                         │
├─────────────────────────────────────────────────────────────────┤
│ OrderService.createPaymentLink()                                │
│   ├─ Generate order code (last 6 digits of timestamp)          │
│   ├─ Create PaymentData with:                                  │
│   │   ├─ orderCode: {generated}                               │
│   │   ├─ amount: {booking.totalPrice}                         │
│   │   ├─ description: "FCinema order#{bookingId}"             │
│   │   ├─ returnUrl: {fe_url}/return-url                       │
│   │   └─ cancelUrl: {fe_url}/return-url                       │
│   ├─ Call: payOS.createPaymentLink(paymentData)               │
│   ├─ Extract checkoutUrl and orderCode from response          │
│   └─ Save PayOsCode and PayOsLink to booking entity           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                 EVENT-DRIVEN SCHEDULING                         │
├─────────────────────────────────────────────────────────────────┤
│ Publish: BookingCreatedEvent                                    │
│   ├─ eventPublisher.publishEvent(BookingCreatedEvent)         │
│   └─ Event contains: {bookingId, userId, createdAt,           │
│                       expirationTime}                           │
│                                                                 │
│ PaymentEventListener.handleBookingCreated()                     │
│   └─ paymentExpirationService.schedulePaymentExpiration()     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│            REDIS DELAYED QUEUE SCHEDULING                       │
├─────────────────────────────────────────────────────────────────┤
│ PaymentExpirationService.schedulePaymentExpiration()            │
│   ├─ Create: BookingExpirationEvent                            │
│   ├─ Get Redisson Queue: "payment:expiration:queue"           │
│   ├─ Get Delayed Queue wrapper: delayedQueue                  │
│   ├─ Call: delayedQueue.offer(event, 2, TimeUnit.MINUTES)    │
│   │         ← Event will auto-trigger after 2 minutes         │
│   ├─ Cache booking: key="payment:cache:{bookingId}"           │
│   │                 ttl=2 minutes                              │
│   └─ Log: "Scheduled payment {bookingId} for expiration"      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
                     ┌────────────────┐
                     │  CUSTOMER FLOW │
                     │    (2 options) │
                     └────────────────┘
                              │
              ┌───────────────┴──────────────┐
              │                              │
              ▼                              ▼
    ┌────────────────────┐       ┌─────────────────────┐
    │   PAYMENT SUCCESS  │       │   PAYMENT EXPIRED   │
    └────────────────────┘       │   (no payment made) │
              │                  └─────────────────────┘
              │                             │
              ▼                             ▼
    ┌────────────────────────┐  ┌─────────────────────────┐
    │  PayOS Webhook Called  │  │ @Scheduled Task Every   │
    │  /payos_transfer_      │  │ 5 seconds Polls Queue   │
    │   handler              │  │                         │
    │                        │  │ processExpiredPayments()│
    │ Webhook Data:          │  │                         │
    │ - desc: "success"      │  │ - Gets queue: "payment:│
    │ - orderCode: {orderID} │  │   expiration:queue"    │
    │ - amount: {amount}     │  │ - queue.poll()         │
    │                        │  │ - Processes event when │
    │                        │  │   delay elapsed        │
    └────────────────────────┘  └─────────────────────────┘
              │                             │
              ▼                             ▼
    ┌────────────────────────┐  ┌─────────────────────────┐
    │ handleSuccessful       │  │ processPaymentExpiration│
    │ Payment()              │  │ ()                      │
    │                        │  │                         │
    │ bookingService.confirm │  │ 1. Get cached booking  │
    │ WebhookPayment()       │  │ 2. Check if still      │
    │                        │  │    PENDING             │
    │                        │  │ 3. Call expirePayment()│
    │                        │  │                        │
    └────────────────────────┘  └─────────────────────────┘
              │                             │
              ▼                             ▼
    ┌────────────────────────┐  ┌─────────────────────────┐
    │ bookingService.confirm │  │ bookingService.cancel  │
    │ WebhookPayment()       │  │ Booking()              │
    │                        │  │                        │
    │ 1. Find booking by     │  │ 1. Set status =        │
    │    PayOsCode           │  │    CANCELLED           │
    │ 2. status = SUCCESS    │  │ 2. paymentStatus =     │
    │ 3. paymentStatus =     │  │    FAILED              │
    │    SUCCESS             │  │ 3. Call orderService.  │
    │ 4. Clear PayOsLink     │  │    cancelOrder()       │
    │ 5. Save booking        │  │ 4. Clear seats         │
    │ 6. Publish:            │  │ 5. Save booking        │
    │    BookingSuccessEvent │  │                        │
    │                        │  │ (Sends to PayOS API)   │
    │                        │  │ payOS.cancelPayment    │
    │                        │  │ Link()                 │
    └────────────────────────┘  └─────────────────────────┘
              │                             │
              ▼                             ▼
    ┌────────────────────────┐  ┌─────────────────────────┐
    │ BookingSuccessEvent    │  │ Redis cleanup:          │
    │ Published              │  │ Delete booking from     │
    │                        │  │ cache                   │
    │ Listeners:             │  │                         │
    │ 1. Receipt creation    │  │ Event done.             │
    │ 2. Payment expiration  │  │ Booking cancelled       │
    │    cancellation        │  │                         │
    │                        │  │ Status = CANCELLED      │
    │ Redis cleanup:         │  │ PaymentStatus = FAILED  │
    │ Delete cached booking  │  │                         │
    │                        │  │                         │
    └────────────────────────┘  └─────────────────────────┘
              │
              ▼
    ┌────────────────────────┐
    │ Final State:           │
    │ - Booking: SUCCESS     │
    │ - Payment: SUCCESS     │
    │ - Receipt: CREATED     │
    │ - Seats: RESERVED      │
    └────────────────────────┘
```

---

## 8. URL CONFIGURATIONS IN ORDER

### 8.1 Return URL After Payment

**Configuration**: `application.yaml`

```yaml
payos:
  returnUrl: ${VITE_PAYOS_FE_RETURN_URL:/return-url}
```

**Used In**: `BookingServiceImpl.createPaymentLink()`

```java
CreatePaymentLinkRequest paymentRequest =
  new CreatePaymentLinkRequest(
    "FCinema order#" + savedBooking.getId(),
    "FCinema order#" + savedBooking.getId(),
    savedBooking.getFeUrl() + payOsReturnUrl,    // ← Both return AND cancel URLs
    savedBooking.getTotalPrice().intValue(),
    savedBooking.getFeUrl() + payOsReturnUrl
  );
```

### 8.2 URL Flow

1. **Payment Link Generation**:
   ```
   returnUrl: {booking.feUrl}/return-url
   cancelUrl: {booking.feUrl}/return-url
   ```

2. **PayOS Checkout**:

- User is redirected to PayOS checkout page with this link
- After payment success: user redirected to `returnUrl`
- If user cancels: user redirected to `cancelUrl`

3. **Webhook Callback**:

- PayOS sends webhook to: `POST /payment/payos_transfer_handler`
- Frontend polls/checks payment status using booking ID or order code

### 8.3 Stored URLs in Booking Entity

```java
public class Booking {
  private String payOsCode;      // Order code from PayOS
  private String payOsLink;      // Checkout URL from PayOS
  private String feUrl;          // Frontend base URL
}
```

---

## 9. CONFIGURATION & ENVIRONMENT VARIABLES

### 9.1 Application Configuration

**File**: `application.yaml`

```yaml
server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: /movie_theater

spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/movie_theater}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:root}

  data:
    redis:
      host: ${REDIS_HOST:redis-14011.crce178.ap-east-1-1.ec2.redns.redis-cloud.com}
      port: ${REDIS_PORT:14011}
      password: ${REDIS_PASSWORD:Q5JVVypSNYHrcgfgqsLnsY7abMNecGNB}
      timeout: 2000ms
      connect-timeout: 2000ms
      lettuce:
        pool:
          max-active: 10
          max-idle: 10
          min-idle: 1

payos:
  clientId: ${PAYOS_CLIENT_ID:95c115a3-33ba-4cac-896d-b7ff826de2be}
  apiKey: ${PAYOS_API_KEY:b85a3670-922b-4cae-87eb-39db0841c375}
  checksumKey: ${PAYOS_CHECKSUM_KEY:636ac61625c8cd6b62904eb5cf8b6159147cdcf66129e107429fb3b1e22c1a61}
  returnUrl: ${VITE_PAYOS_FE_RETURN_URL:/return-url}

fcinema:
  payment:
    expiration:
      minutes: ${EXPIRED_PAYMENT:2}
```

### 9.2 Key Environment Variables

| Variable                   | Default            | Purpose                           |
|----------------------------|--------------------|-----------------------------------|
| `PAYOS_CLIENT_ID`          | (demo)             | PayOS merchant client ID          |
| `PAYOS_API_KEY`            | (demo)             | PayOS API authentication key      |
| `PAYOS_CHECKSUM_KEY`       | (demo)             | PayOS webhook verification key    |
| `VITE_PAYOS_FE_RETURN_URL` | `/return-url`      | Frontend return URL after payment |
| `REDIS_HOST`               | Remote Redis Cloud | Redis connection host             |
| `REDIS_PORT`               | 14011              | Redis connection port             |
| `REDIS_PASSWORD`           | (provided)         | Redis authentication password     |
| `EXPIRED_PAYMENT`          | 2                  | Payment expiration in minutes     |

---

## 10. ERROR HANDLING & EDGE CASES

### 10.1 Payment Link Creation Failures

```java
catch(Exception e){
  System.err.

println("Error creating payment link: "+e.getMessage());
  response.

put("error",-1);
    response.

put("message","fail: "+e.getMessage());
  response.

set("data",null);
    return response;
}
```

**Handled by**: `OrderServiceImpl.createPaymentLink()`

### 10.2 Booking Not Found During Confirmation

```java

@Override
public void confirmWebhookPayment(Long orderCode) {
  Booking booking = bookingRepository
    .findByPayOsCode(orderCode.toString())
    .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
  // ...
}
```

**Handled by**: Custom `AppException` with error code

### 10.3 Payment Already Processed

```java
if(booking.getStatus() !=Booking.Status.PENDING){
  throw new

AppException(ErrorCode.BOOKING_ALREADY_PROCESSED);
}
```

**Prevents**: Double-charging or duplicate confirmations

### 10.4 Redis Connection Loss

```java

@Async
public void schedulePaymentExpiration(Integer bookingId) {
  try {
    // Redis operations
  } catch (Exception e) {
    log.error("Failed to schedule payment expiration for booking {}: {}",
      bookingId, e.getMessage(), e);
  }
}
```

**Fallback**: Booking is still stored in database; manual cancellation can be triggered

---

## 11. PERFORMANCE & SCALABILITY CONSIDERATIONS

### 11.1 Async Processing

- **@Async** on event handlers and scheduling methods
- **@Scheduled** tasks run independently
- Non-blocking payment link creation

### 11.2 Caching Strategy

- Booking data cached in Redis during expiration window
- Reduces database queries during peak expiration times
- TTL matches expiration time for automatic cleanup

### 11.3 Connection Pooling

```yaml
lettuce:
  pool:
    max-active: 10      # Maximum active connections
    max-idle: 10        # Maximum idle connections
    min-idle: 1         # Minimum idle connections
    time-between-eviction-runs: 30s
```

### 11.4 Scheduled Task Optimization

- Checks every 5 seconds (configurable)
- Polls queue efficiently (returns null immediately if empty)
- Batch processes multiple expired events in one cycle

---

## 12. MONITORING & DEBUGGING

### 12.1 Key Logs to Monitor

```
✓ "Scheduled payment {bookingId} for expiration at {time}"
✓ "Received PayOS webhook: {body}"
✓ "Payment webhook verified successfully: {data}"
✓ "Processing successful payment for order: {orderCode}"
✓ "Confirming payment for booking: {bookingId}"
✓ "Payment expired for booking {bookingId}"
✓ "Failed to schedule payment expiration for booking {bookingId}: {error}"
```

### 12.2 Redis Debugging Commands

```bash
# Monitor all keys
KEYS payment:*

# Check queue status
LLEN payment:expiration:queue

# Get cached booking
GET payment:cache:{bookingId}

# Check Redis info
INFO stats
```

### 12.3 Database Queries to Monitor

```sql
-- Check pending bookings
SELECT *
FROM booking
WHERE payment_status = 'PENDING'
  AND created_at < NOW() - INTERVAL 2 MINUTE;

-- Check booking with PayOS code
SELECT *
FROM booking
WHERE payos_code = '{orderCode}';

-- Cancelled bookings
SELECT *
FROM booking
WHERE status = 'CANCELLED'
ORDER BY updated_at DESC LIMIT 10;
```

---

## 13. SUMMARY TABLE

| Component                  | Technology             | Purpose                                | Configuration                     |
|----------------------------|------------------------|----------------------------------------|-----------------------------------|
| **Payment Gateway**        | PayOS SDK              | Process online payments                | `PayOsConfig.java`                |
| **Order Creation**         | OrderService           | Create PayOS payment link              | `OrderServiceImpl.java`           |
| **Automatic Cancellation** | Redisson Delayed Queue | Expire pending payments                | `PaymentExpirationService.java`   |
| **Webhook Handling**       | PaymentController      | Receive payment confirmations          | `/payment/payos_transfer_handler` |
| **Event Publishing**       | Spring Events          | Async event-driven flow                | `BookingServiceImpl.java`         |
| **Event Listening**        | EventListener          | Respond to booking events              | `PaymentEventListener.java`       |
| **Caching**                | Redis + RedisTemplate  | Cache booking during expiration window | `RedisConfig.java`                |
| **Scheduling**             | @Scheduled             | Process expired payments               | `PaymentExpirationService.java`   |
| **Async Processing**       | @Async                 | Non-blocking operations                | All service methods               |

---

## 14. KEY TAKEAWAYS

1. **PayOS Integration**: Uses PayOS SDK to create payment links and receive webhooks
2. **Order Code**: Generated from last 6 digits of current timestamp for uniqueness
3. **URL Storage**: `payOsCode` (order ID) and `payOsLink` (checkout URL) stored in Booking entity
4. **Automatic Cancellation**: Redis delayed queues automatically process expired payments
5. **Event-Driven**: Uses Spring Events for decoupled, asynchronous handling
6. **Webhook Verification**: PayOS verifies webhook data before processing
7. **Dual Path**: Supports both webhook-based and manual payment confirmation
8. **Redis Cleanup**: Automatic cache expiration and event processing
9. **Fallback Mechanism**: Database serves as fallback if Redis fails
10. **Configurable Timeout**: Payment expiration time can be adjusted via `EXPIRED_PAYMENT` env var

This implementation provides a robust, scalable payment system with automatic order cancellation using industry-standard
tools and best practices.
