---
layout: post
title: "Algorithm 기본"
author: "Bys"
category: algorithm
date: 2025-11-08 01:00:00
tags: algorithm bfs dfs hash
---

# Algorithm

---

## 1. BFS (Breadth-First Search)
BFS(너비 우선 탐색)는 그래프나 트리에서 시작 노드부터 인접한 노드들을 먼저 탐색하는 알고리즘이다. 큐(Queue) 자료구조를 사용하여 FIFO(First In First Out) 방식으로 동작하며, 최단 경로를 찾는 문제에 주로 활용된다. 레벨 순서대로 탐색하기 때문에 가중치가 없는 그래프에서 최단 거리를 보장한다.

- **자료구조**: Queue
- **시간 복잡도**: O(V + E) (V: 정점 수, E: 간선 수)
- **사용예시**: 최단경로, 미로 탐색
- **전제조건**: 가중치가 없는 그래프 (모든 간선의 가중치가 동일)

```python
import collections from deque

def bfs(graph, start_node):
    visited = set()
    queue = deque([start_node])
    visited.add(start_node)
    for neighbor in graph[current_node]:
        if neighbor not in visited:
            visited.add(neighbor)
            queue.append(neighbor)
```

---

## 2. DFS (Depth-First Search)
DFS(깊이 우선 탐색)는 그래프나 트리에서 한 방향으로 끝까지 탐색한 후 다시 돌아와서 다른 방향을 탐색하는 알고리즘이다. 스택(Stack) 자료구조나 재귀 함수를 사용하여 LIFO(Last In First Out) 방식으로 동작한다. 모든 경로를 탐색해야 하는 문제나 백트래킹 문제에 주로 사용된다.

- **자료구조**: Stack or Recursive
- **시간 복잡도**: O(V + E) (V: 정점 수, E: 간선 수)
- **사용예시**: 백트래킹, 사이클 탐지, 경로 탐색
- **전제조건**: 방문 체크 필요, 무한 루프 방지

```python
import collections from deque

def dfs(graph, start_node, visited):
    queue = deque([start_node])
    visited.add(start_node)

    while queue:
        current_node = queue.pop()
        print(f"Visit Node: {current_node})

        for neighbor in graph[current_node]:
            if neighbor not in visited:
                dfs(graph, current_node, visited)
```

---

## 3. Binary Search
이진 탐색은 정렬된 배열에서 특정 값을 찾는 효율적인 알고리즘이다. 배열의 중간값과 찾는 값을 비교하여 탐색 범위를 절반씩 줄여나가며, O(log n)의 시간 복잡도를 가진다. 배열이 정렬되어 있어야 한다는 전제 조건이 있다.

- **자료구조**: 정렬된 배열
- **시간 복잡도**: O(log n)
- **사용예시**: 값 검색, 경계값 찾기, 최적화 문제
- **전제조건**: 배열이 정렬되어 있어야 함

```python
def binary_search(arr: list[int], target: int):
    low = 0
    high = len(arr)-1
    while low <= high:
        mid = int((low+high)/2)
        if arr[mid] == target:
            return arr[mid]
        elif arr[mid] < target:
            low = mid + 1
        elif arr[mid] > target:
            high = mid -1
```

---

## 4. Two Pointer
투 포인터는 배열이나 리스트에서 두 개의 포인터를 사용하여 문제를 해결하는 기법이다. 보통 배열의 양 끝에서 시작하거나 같은 위치에서 시작하여 조건에 따라 포인터를 이동시킨다. 정렬된 배열에서 특히 유용하며 O(n) 시간 복잡도로 해결할 수 있다.

- **자료구조**: 배열, 리스트
- **시간 복잡도**: O(n)
- **사용예시**: 두 수의 합, 부분 배열 합, 팰린드롬 검사
- **전제조건**: 대부분 정렬된 배열 필요 (Two Sum 등)

---

## 5. Sorting Algorithms
정렬 알고리즘은 데이터를 특정 순서로 배열하는 알고리즘이다. 퀵 정렬, 머지 정렬, 힙 정렬 등 다양한 방법이 있으며, 각각 다른 시간/공간 복잡도를 가진다. 대부분의 프로그래밍 언어에서 내장 정렬 함수를 제공하지만, 알고리즘의 특성을 이해하는 것이 중요하다.

- **자료구조**: 배열, 리스트
- **시간 복잡도**: O(n log n) ~ O(n²)
- **사용예시**: 데이터 정렬, 전처리, 순위 매기기
- **전제조건**: 비교 가능한 데이터 타입

---

## 6. Greedy Algorithm
탐욕 알고리즘은 각 단계에서 가장 좋아 보이는 선택을 하는 알고리즘이다. 전체적으로 최적해를 보장하지는 않지만, 특정 조건을 만족하는 문제에서는 최적해를 구할 수 있다. 구현이 간단하고 효율적이지만, 문제의 특성을 정확히 파악해야 한다.

- **자료구조**: 다양함 (우선순위 큐 등)
- **시간 복잡도**: 문제에 따라 다름 (보통 O(n log n))
- **사용예시**: 최소 신장 트리, 활동 선택 문제, 거스름돈 문제
- **전제조건**: 탐욕 선택 속성과 최적 부분 구조를 만족해야 함



---

## 📚 References

[1] GPT