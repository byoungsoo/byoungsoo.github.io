---
layout: post
title: "Java 의시간 계, UTC, LocalDateTime, LocalDate, LocalTime"
author: "Bys"
category: programming
date: 2022-04-19 01:00:00
tags: programming spring @transactional
---

1. UTC (Coordinated Universal Time)
영국을 기준 (UTC+0:00)으로 하여 각 지역의 시차를 규정한 것이다. 한국은 영구보다 9시간 빠르므로 UTC+9:00 이라고 표시한다. 

Java에서 UTC를 얻는 방법은 아래와 같다. 
```Java
OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
Instant.now()

```

현재 나의 문제


<br><br><br>

---

**Reference**  

---
