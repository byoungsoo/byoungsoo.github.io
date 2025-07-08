---
layout: post
title: "머신러닝 지도학습 - DecisionTreeClassifier"
author: "Bys"
category: ml
date: 2025-06-17 01:00:00
tags: ml decisiontreeclassifier supervisedlearning
---

## 머신러닝

### 머신러닝
머신러닝의 지도학습에는 분류(Classifier)와 회귀(Regression)이 있다. 이 중 사이킷런(sklearn)의 DecisionTreeClassifier 를 통해 간다한게 분류가 무엇인지 살펴본다.  

### [DecisionTreeClassifier]()

```python
!pip install numpy
!pip install DecisionTreeClassifier
!pip install mlflow
!pip install boto3
```

```python
import numpy as np
from sklearn.tree import DecisionTreeClassifier
import mlflow
import os


# 1. 데이터 준비
X = np.array([
  [1, 1],
  [1, 2],
  [1, 3],
  [1, 4],
  [2, 1],
  [2, 2],
  [2, 3],
  [2, 4],
  [3, 1],
  [3, 2],
  [3, 3],
  [3, 4],
  [4, 1],
  [4, 2],
  [4, 3],
  [4, 4],
  [10,1],
  [10,15]
])
y = np.array([0, 0, 1, 1, 0, 0, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3])

# 2. MLflow 연동 (생략가능)
# os.environ['MLFLOW_TRACKING_USERNAME'] = ""
# os.environ['MLFLOW_TRACKING_PASSWORD'] = ""
# os.environ['MLFLOW_S3_ENDPOINT_URL']='http://minio.minio.svc.cluster.local:9000'
# os.environ['AWS_ACCESS_KEY_ID']=''
# os.environ['AWS_SECRET_ACCESS_KEY']=''

#HOST = "https://mlflow.bys.asia/"
#EXPERIMENT_NAME = "mlflow-test-experiment"

# Connect to local MLflow tracking server
# mlflow.set_tracking_uri(HOST)

# Set the experiment name through which you will label all your exerpiments runs
# mlflow.set_experiment(EXPERIMENT_NAME)

# enable autologging for scikit
# mlflow.sklearn.autolog()



# 3. 모델 학습
model = DecisionTreeClassifier(max_depth=3, criterion='gini',min_samples_leaf = 1 ,min_samples_split = 2)
#with mlflow.start_run() as run:
#    model.fit(X, y)
 model.fit(X, y)


# 3. 트리 시각화
plt.figure(figsize=(12, 8))
plot_tree(model, filled=True, feature_names=['X', 'Y'], class_names=['0', '1', '2', '3'])
plt.title("Decision Tree Structure")
plt.show()

# 4. 결정경계 시각화
x_min, x_max = X[:, 0].min() - 1, X[:, 0].max() + 1
y_min, y_max = X[:, 1].min() - 1, X[:, 1].max() + 1
xx, yy = np.meshgrid(np.arange(x_min, x_max, 0.1),
                     np.arange(y_min, y_max, 0.1))
Z = model.predict(np.c_[xx.ravel(), yy.ravel()])
Z = Z.reshape(xx.shape)

plt.figure(figsize=(10, 6))
plt.contourf(xx, yy, Z, cmap=plt.cm.Paired, alpha=0.4)
plt.scatter(X[:, 0], X[:, 1], c=y, cmap=plt.cm.Paired, s=100, edgecolors='k')
plt.xlabel('X')
plt.ylabel('Y')
plt.title("Decision Boundary of Decision Tree")
plt.show()
```

이 코드에서 mlflow를 제외하고 데이터 준비를 보면 X는 각 좌표를 의미하고, Y는 각 좌표에 대한 클래스(분류)를 한것으로 이해할 수 있다. 즉, 좌표 (1,1) 은 클래스 0이고, 좌표 (1,2) 도 클래스 0이다. 좌표 (10,15) 는  클래스 3이다. 
위 코드를 시각화 하면 아래와 같다.
