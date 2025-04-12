## AggregateStatsTest.md

### Overview

The `AggregateStats` directive, which aggregates byte sizes and time durations. This directive computes either the `total` or the `average` of specified columns using units like `kb`, `mb`, `ms`, `s`, etc., and stores the result in new columns.

---

### Test: `testAggregateStatsTotal`

**Purpose:** Validates the total aggregation logic for both byte sizes and time durations.

**Input Recipe:**

```java
String[] recipe = new String[] {
    "aggregate-stats :data_size :response_time :total_size :total_time 'total'",
};
```

**Input Rows:**

```java
rows.add(new Row("data_size", new ByteSize("10kb"))
        .add("response_time", new TimeDuration("1.5ms")));
rows.add(new Row("data_size", new ByteSize("20Kb"))
        .add("response_time", new TimeDuration("2.5Ms")));
rows.add(new Row("data_size", new ByteSize("35.6MB"))
        .add("response_time", new TimeDuration("35s")));
```

**Expected Outputs:**

```java
double expectedTotalBytes = (10 * 1024 + 20 * 1024 + 35.6 * 1024 * 1024);
double expectedTotalNanos = 1_500_000 + 2_500_000 + 35_000_000_000L;
```

**Assertions:**

```java
Assert.assertEquals(expectedTotalBytes, gotTotalBytes, 0.01);
Assert.assertEquals(expectedTotalNanos, gotTotalNanos, 0.01);
```

---

### Test: `testAggregateStatsAvg`

**Purpose:** Verifies that the average computation is correct for given inputs.

**Input Recipe:**

```java
String[] recipe = new String[] {
    "aggregate-stats :data_size :response_time :avg_size :avg_time 'average'",
};
```

**Input Rows:**

```java
rows.add(new Row("data_size", new ByteSize("10kb"))
        .add("response_time", new TimeDuration("1.5ms")));
rows.add(new Row("data_size", new ByteSize("20Kb"))
        .add("response_time", new TimeDuration("2.5Ms")));
rows.add(new Row("data_size", new ByteSize("35.6MB"))
        .add("response_time", new TimeDuration("35s")));
```

**Expected Outputs:**

```java
double expectedAvgBytes = (10 * 1024 + 20 * 1024 + 35.6 * 1024 * 1024) / 3.0;
double expectedAvgNanos = (1_500_000 + 2_500_000 + 35_000_000_000L) / 3.0;
```

**Assertions:**

```java
Assert.assertEquals(expectedAvgBytes, gotAvgBytes, 0.01);
Assert.assertEquals(expectedAvgNanos, gotAvgNanos, 0.01);
```

---

### Notes:

- Each test uses custom `ByteSize` and `TimeDuration` classes.
- Uses `TransientStore` to temporarily hold intermediate values.
- The result row is expected only when all rows are processed.
- The unit conversion logic is tested implicitly.


