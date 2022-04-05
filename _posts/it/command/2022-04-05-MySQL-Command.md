---
layout: post
title: "MySQL Command"
author: "Bys"
category: command
tags: mysql command
---

`CREATE DATABASES`  
```sql
CREATE DATABASE quartz CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Output
Query OK, 1 row affected (0.02 sec)
```

`SHOW DATABASES`  
```sql
mysql> SHOW DATABASES;

--Output
+--------------------+
| Database           |
+--------------------+
| information_schema |
| mysql              |
| performance_schema |
| quartz             |
| spring5fs          |
| sys                |
+--------------------+
```

`DROP DATABASE`  
```sql
DROP DATABASE quartz;

-- Output
Query OK, 11 rows affected (0.17 sec)
```


`USE DATABASE`  
```sql
USE quartz;

-- Output
Database changed
```


