---
layout: post
title: "Bedrock workshop"
author: "Bys"
category: ml
date: 2025-01-14 01:00:00
tags: genai prompt
---

{% comment %}
- Workshop 
  - https://studio.us-east-1.prod.workshops.aws/preview/26bcaabd-082a-4b2d-a5a0-63d1c1d61485/builds/91252534-ac26-4fa3-bf4b-a2a43a1d37ab/en-US/amazon-bedrock-beginner/foundation-models/getting-started
  - https://catalog.us-east-1.prod.workshops.aws/workshops/0b6e72fe-77ee-4777-98cc-237eec795fdb/en-US
    - aws cloudformation create-stack --stack-name bedrock-workshop --capabilities CAPABILITY_NAMED_IAM --template-body file:///Users/bys/workspace/bedrock/cf.yml --tags Key=Name,Value=BedrockWorkshop --region us-east-1
{% endcomment %}

```bash
!pip install boto3 --upgrade

!pip install Pillow --upgrade
!pip install numpy --upgrade
!pip install pandas --upgrade
!pip install scikit-learn --upgrade
!pip install psycopg2-binary --upgrade
!pip install requests --upgrade
!pip install requests_aws4auth --upgrade
!pip install pinecone-client --upgrade
!pip install threadpoolctl==3.1.0
!pip install numexpr==2.8.4
!pip install bottleneck==1.3.6
!pip install seaborn
```

```
region = 'us-east-1'
```


Token: 자연어를 분리하였을 때 단위를 토큰이라고 함. 보통 평균적으로 단어 2~3개 정도가 하나의 토큰
Tokenize 하는 방식은 각 회사마다 모델마다 다름. 

- 파라미터
    Parameter Tuning에 대해서 논의하기 전에, LLM에 대해서 조금 더 간단하게 설명을 하겠습니다. 수학적 증명을 제외하고 LLM을 요약하자면, 한번에 한 단어 (token)씩 생성합니다. 다음 단어를 생성할 때, 그 자리에 와야할 단어가 무엇인지 가능성 있는 모든 단어를 가지고 있습니다. 가능성은 확률로 표현이 되며 가능성 있는 모든 단어들의 확률을 더하면 100%가 됩니다.
    오늘 저녁에 어디서 무엇을 ___ ?  
    여기서 ___ 밑줄 친 부분에 들어올 수 있는 단어는 무엇이 있을까요?  
    [(먹지?, 70%) , (할까?,30%), (해볼까?, 9.7%), (점프할까?, 0.1%), (뛸까?, 0.1%), (게임할까?, 0.1%) ] LLM 은 위와 비슷한 리스트를 생성하고, 이중 높은 확률을 선택합니다.  
    Inference Parameter 값들을 조정하여 조금 더 위 리스트를 필터링 해볼 수 있습니다. 이 부분을 Parameter Tuning이라고 합니다.

- 파라미터 조정 (모델별로 지원가능한 파라미터가 다름)
  - https://docs.aws.amazon.com/ko_kr/bedrock/latest/userguide/inference-parameters.html
  - Temperature: 0 ~ 1 사이의 값으로, 0은 높은 확률의 값을 선택하도록 유도하고, 1은 낮은 확률의 단어를 선택하도록 유도할 수 있습니다. 적절한 값은 재밌고 창의로운 문장을 생성할 수 있습니다.
  - Top P: 0~1 사이의 값으로 LLM이 생성한 가능성이 있는 단어 리스트의 합을 설정합니다. 1.0 을 선택할 경우 100% 합이 나올때까지 단어 리스트를 생성하며, 0.5일 경우에는 그 합이 50%가 되면 이 후 단어들은 제외됩니다.
  - Top K: 확률 높은 단어들 위주로 정렬된 리스트에서 K개의 단어들까지만 남겨두고 나머지는 제외합니다. 여러 번 실행할 때 Top K를 1에 가깝게 설정하면 결정론적인 결과를 얻을 수 있고, 더 높은 값으로 설정하면 결과의 다양성이 더 커집니다.
  - Response Length - 생성된 답변의 길이를 제한하는 값 입니다.
  - Stop Sequences - 특정 키워드들로 구성된 Stop조건입니다. 이 키워드가 생성되면 모델은 바로 답변을 멈춥니다.

위 용어들을 종합하여 생각해보자면 아래와 같습니다.
  - LLM은 먼저 가능성 있는 단어들을 생성하고 확률 높은 순으로 정렬합니다.
  - 이 때, Top P 값에 따라서 위 리스트를 제한합니다.
  - 그리고, Temperature 값을 활용하여 위 리스트에서 단어를 선택합니다.
  - 만약 Response Length 값보다 커지면 답변 생성을 멈춥니다.
  - 혹은, Stop Sequence가 생성되면 답변 생성을 멈춥니다.



- invoke_model API 사용시 모델별로 파라미터 들이 달라서(max_tokens, maxTokens 등) 통일되지 않음. Bedrock에서는 ConverseAPI 를 통해 일관되게 사용하는 것을 권장.

`invoke_model 사용시`
```python
import json
import boto3

def invoke_model(client, prompt, model, 
    accept = 'application/json', content_type = 'application/json',
    max_tokens  = 512, temperature = 1.0, top_p = 1.0, top_k = 250, stop_sequences = [],
    count_penalty = 0, presence_penalty = 0, frequency_penalty = 0, return_likelihoods = 'NONE'):
    # default response
    output = ''
    # identify the model provider
    provider = model.split('.')[0] 
    # InvokeModel
    if (provider == 'anthropic'): 
        input = {
            'prompt': prompt,
            'max_tokens_to_sample': max_tokens, 
            'temperature': temperature,
            'top_k': top_k,
            'top_p': top_p,
            'stop_sequences': stop_sequences
        }
        body=json.dumps(input)
        response = bedrock.invoke_model(body=body, modelId=model, accept=accept,contentType=content_type)
        response_body = json.loads(response.get('body').read())
        output = response_body['completion']
    elif (provider == 'ai21'): 
        input = {
            'prompt': prompt, 
            'maxTokens': max_tokens,
            'temperature': temperature,
            'topP': top_p,
            'stopSequences': stop_sequences,
            'countPenalty': {'scale': count_penalty},
            'presencePenalty': {'scale': presence_penalty},
            'frequencyPenalty': {'scale': frequency_penalty}
        }
        body=json.dumps(input)
        response = bedrock.invoke_model(body=body, modelId=model, accept=accept,contentType=content_type)
        response_body = json.loads(response.get('body').read())
        completions = response_body['completions']
        for part in completions:
            output = output + part['data']['text']
    elif (provider == 'amazon'): 
        input = {
            'inputText': prompt,
            'textGenerationConfig': {
                  'maxTokenCount': max_tokens,
                  'stopSequences': stop_sequences,
                  'temperature': temperature,
                  'topP': top_p
            }
        }
        body=json.dumps(input)
        response = bedrock.invoke_model(body=body, modelId=model, accept=accept,contentType=content_type)
        response_body = json.loads(response.get('body').read())
        results = response_body['results']
        for result in results:
            output = output + result['outputText']
    elif (provider == 'cohere'): 
        input = {
            'prompt': prompt, 
            'max_tokens': max_tokens,
            'temperature': temperature,
            'k': top_k,
            'p': top_p,
            'stop_sequences': stop_sequences,
            'return_likelihoods': return_likelihoods
        }
        body=json.dumps(input)
        response = bedrock.invoke_model(body=body, modelId=model, accept=accept,contentType=content_type)
        response_body = json.loads(response.get('body').read())
        results = response_body['generations']
        for result in results:
            output = output + result['text']
    elif (provider == 'meta'): 
        input = {
            'prompt': prompt,
            'max_gen_len': max_tokens,
            'temperature': temperature,
            'top_p': top_p
        }
        body=json.dumps(input)
        response = bedrock.invoke_model(body=body, modelId=model, accept=accept,contentType=content_type)
        response_body = json.loads(response.get('body').read())
        output = response_body['generation']
    # return
    return output

# 전 단계에서 만든 Helper function
# {...}

# main function
bedrock = boto3.client(
    service_name='bedrock-runtime'
)
model  = 'ai21.j2-ultra'

prompt = """
Human: How are you?
AI: I am well, thank you for asking. How are you?
Human: I can't find my cat.
AI: I am sorry to hear that. I can assist you with finding your cat.
Human: Thank you! Where do you think it is hiding?
AI: Cats often hide in small spaces where they feel safe, such as under beds or in closets. I can also help you look in those areas.
Human: Alright, let's just check under the bed first.
AI: I can help you look under the bed as well. Let's move the bed and check thoroughly.
Human: It is definitely not under the bed.
AI: Let's check in the closet then. Cats often feel safe in small, enclosed spaces. I can help you search in the closet.
Human: It is not in the closet either.
"""
output = invoke_model(client=bedrock, prompt=prompt, model=model)
print(output)
```


Embedding

```python
import json
import boto3
import math

from numpy import dot
from numpy.linalg import norm

bedrock = boto3.client(
    service_name='bedrock-runtime'
)

def get_embedding(bedrock, text):
    modelId = 'amazon.titan-embed-text-v1'
    accept = 'application/json'
    contentType = 'application/json'
    input = {
            'inputText': text
        }
    body=json.dumps(input)
    response = bedrock.invoke_model(
        body=body, modelId=modelId, accept=accept,contentType=contentType)
    response_body = json.loads(response.get('body').read())
    embedding = response_body['embedding']
    return embedding

def calculate_distance(v1, v2):
    distance = math.dist(v1, v2)
    return distance

def calculate_dot_product_similarity(v1, v2):
    similarity = dot(v1, v2)
    return similarity

def calculate_cousin_similarity(v1, v2):
    similarity = dot(v1, v2)/(norm(v1)*norm(v2))
    return similarity


names = ['Albert Einstein', 'Bob Dylan', 'Elvis Presley', 
         'Isaac Newton', 'Michael Jackson', 'Niels Bohr', 
         'Taylor Swift', 'Hank Williams', 'Werner Heisenberg', 
         'Stevie Wonder', 'Marie Curie', 'Ernest Rutherford', 'Byoungsoo Ko']
embeddings = []
for name in names:
    embeddings.append(get_embedding(bedrock, name))
# clustering
df = pd.DataFrame(data={'names': names, 'embeddings': embeddings})
matrix = np.vstack(df.embeddings.values)
n_clusters = 2
kmeans = KMeans(n_clusters = n_clusters, init='k-means++', random_state=42)
kmeans.fit(matrix)
df['cluster'] = kmeans.labels_
# result
print(df[['cluster', 'names']])

```




`ConverseAPI`
```python
import json
import boto3

prompt = """
Explain what general relativity is to an 8 year old.
"""

## Model 
# model_id = 'anthropic.claude-3-sonnet-20240229-v1:0'
# model_id = 'meta.llama3-8b-instruct-v1:0'
model_id = 'amazon.titan-text-lite-v1'

bedrock = boto3.client(service_name='bedrock-runtime')
message = {"role": "user", "content": [{"text": prompt}]}
messages = [message]
inference_config = {"temperature": 0.5, "topP": 0.9, "maxTokens": 500}
response = bedrock.converse(
    modelId=model_id,
    messages=messages,
    inferenceConfig=inference_config
)
content = response['output']['message']['content']
for item in content:
    print(item['text'])
```


`ConverseStream API`
```python
import json
import boto3

prompt = """
Explain what general relativity is to an 8 year old.
"""

# model_id = 'anthropic.claude-3-sonnet-20240229-v1:0'
# model_id = 'meta.llama3-8b-instruct-v1:0'
model_id = 'amazon.titan-text-lite-v1'

bedrock = boto3.client(service_name='bedrock-runtime')
message = {"role": "user", "content": [{"text": prompt}]}
messages = [message]
inference_config = {"temperature": 0.5, "topP": 0.9, "maxTokens": 500}
response = bedrock.converse_stream(
    modelId=model_id,
    messages=messages,
    inferenceConfig=inference_config
)
stream = response.get('stream')
if stream:
    for event in stream:
        if 'messageStart' in event:
            print(f"\nRole: {event['messageStart']['role']}")
        if 'contentBlockDelta' in event:
            print(event['contentBlockDelta']['delta']['text'], end="")
        if 'messageStop' in event:
            print(f"\n\nStop reason: {event['messageStop']['stopReason']}")
        if 'metadata' in event:
            metadata = event['metadata']
            if 'usage' in metadata:
                print("\nToken usage")
                print(f"Input tokens: {metadata['usage']['inputTokens']}")
                print(f"Output tokens: {metadata['usage']['outputTokens']}")
                print(f"Total tokens: {metadata['usage']['totalTokens']}")
            if 'metrics' in event['metadata']:
                print(f"Latency: {metadata['metrics']['latencyMs']} milliseconds")
```






`Conversational Chatbot`
For foundation models to remember what has been asked and answered, it requires something similar to the concept of a session on the server side. Considering the size of the conversation data, this is something very expensive to offer, especially in a multi-tenant environment.
A workaround is, instead of asking the service to keep the chat history, the customer keeps chat history on the client side. When asking a follow up question, the client sends the chat history as part of the prompt so that the LLM models know what has been talked about.

```json
import json
import boto3

model_id = 'amazon.titan-text-lite-v1'
bedrock = boto3.client(service_name='bedrock-runtime')
messages = [
    {"role": "user",      "content": [{"text": "How are you?"}]},
    {"role": "assistant", "content": [{"text": "I am well, thank you for asking! I hope you are well, too."}]},
    {"role": "user",      "content": [{"text": "I can't find my cat."}]},
    {"role": "assistant", "content": [{"text": "Did the cat go missing recently?"}]},
    {"role": "user",      "content": [{"text": "I saw it in the garden yesterday."}]}
]
inference_config = {"temperature": 1.0, "topP": 1.0, "maxTokens": 200, "stopSequences": []}
response = bedrock.converse(
    modelId=model_id,
    messages=messages,
    inferenceConfig=inference_config
)
content = response['output']['message']['content']
for item in content:
    print(item['text'])
```

![bedrock_chatbot.png](/assets/it/cloud/bedrock/bedrock_chatbot.png){: width="60%" height="auto"}  





---
### Embedding
The academic definition of embedding is translating high-dimensional vectors into a relatively low-dimensional space. You might know each and every word in this sentence but still have no idea about the whole sentence. We can think of embedding as converting natural language into a sequence of numbers, with the input being a piece of text and the output being a vector. In other words, the vector is a numerical representation of the text, making it easy to perform all kinds of complex computations in AI/ML.

Embedding의 학술적 정의는 고차원의 벡터를 상대적으로 저차원의 벡터로 변환하는 것입니다. 이번 워크샵에서는 고차원의 벡터는 자연어, 저차원의 벡터는 수백 차원 정도의 벡터라고 생각하시면 됩니다.
이 문장의 모든 단어를 알고 있지만 문장 전체에 대해서는 전혀 모를 수도 있습니다. 임베딩은 자연어를 일련의 숫자로 변환하는 것으로, 입력은 텍스트이고 출력은 벡터라고 생각할 수 있습니다. 즉, 벡터는 텍스트를 숫자로 표현한 것이므로 AI/ML에서 모든 종류의 복잡한 연산을 쉽게 수행할 수 있습니다.

```python
import json
import boto3
import math

def get_embedding(bedrock, text):
    modelId = 'amazon.titan-embed-text-v1'
    accept = 'application/json'
    contentType = 'application/json'
    input = {
            'inputText': text
        }
    body=json.dumps(input)
    response = bedrock.invoke_model(
        body=body, modelId=modelId, accept=accept,contentType=contentType)
    response_body = json.loads(response.get('body').read())
    embedding = response_body['embedding']
    return embedding


## Euclidean distance
def calculate_distance(v1, v2):
    distance = math.dist(v1, v2)
    return distance

## Dot Product Similarity
def calculate_dot_product_similarity(v1, v2):
    similarity = dot(v1, v2)
    return similarity

## Cosine Similarity
def calculate_cousin_similarity(v1, v2):
    similarity = dot(v1, v2)/(norm(v1)*norm(v2))
    return similarity

## Search and recommendation
def search(dataset, v):
    for item in dataset:
        item['distance'] = calculate_distance(item['embedding'], v)
    dataset.sort(key=lambda x: x['distance'])
    return dataset[0]['text']

## Classification
def classify(classes, v):
    for item in classes:
        item['distance'] = calculate_distance(item['embedding'], v)
    classes.sort(key=lambda x: x['distance'])
    return classes[0]['name']

```


```python
## Search and recommendation
# main function
bedrock = boto3.client(
    service_name='bedrock-runtime'
)
# the data set
t1 = """
The theory of general relativity says that the observed gravitational effect between masses results from their warping of spacetime. 
"""
t2 = """
Quantum mechanics allows the calculation of properties and behaviour of physical systems. It is typically applied to microscopic systems: molecules, atoms and sub-atomic particles. 
"""
t3 = """
Wavelet theory is essentially the continuous-time theory that corresponds to dyadic subband transforms — i.e., those where the L (LL) subband is recursively split over and over.
"""
t4 = """
Every particle attracts every other particle in the universe with a force that is proportional to the product of their masses and inversely proportional to the square of the distance between their centers.
"""
t5 = """
The electromagnetic spectrum is the range of frequencies (the spectrum) of electromagnetic radiation and their respective wavelengths and photon energies. 
"""
dataset = [
    {'text': t1, 'embedding': get_embedding(bedrock, t1)}, 
    {'text': t2, 'embedding': get_embedding(bedrock, t2)}, 
    {'text': t3, 'embedding': get_embedding(bedrock, t3)}, 
    {'text': t4, 'embedding': get_embedding(bedrock, t4)}, 
    {'text': t5, 'embedding': get_embedding(bedrock, t5)}
]
query = 'Isaac Newton'
v = get_embedding(bedrock, query)              
result = search(dataset, v)
print(result)
```
Isaac Newton returns t4 on universal gravitation



```python
## Classification
# main function1
bedrock = boto3.client(
    service_name='bedrock-runtime'
)
# the data set
classes = [    
    {'name': 'athletics', 'description': 'all students with a talent in sports'}, 
    {'name': 'musician', 'description': 'all students with a talent in music'}, 
    {'name': 'magician', 'description': 'all students with a talent in witch craft'}
]
for item in classes:
    item['embedding'] = get_embedding(bedrock, item['description'])
# perform a classification
query = 'Ellison sends a spell to prevent Professor Wang from entering the classroom'
v = get_embedding(bedrock, query)              
result = classify(classes, v)
print(result)


# main function2
bedrock = boto3.client(
    service_name='bedrock-runtime'
)
# the data set
classes = [    
    {'name': 'positive', 'description': 'customer demonstrated positive sentiment in the response.'}, 
    {'name': 'negative', 'description': 'customer demonstrated negative sentiment in the response.'}
]
for item in classes:
    item['embedding'] = get_embedding(bedrock, item['description'])
# perform a classification
query = 'Steve helped me solve the problem in just a few minutes. Thank you for the great work!'
#query = 'It took too long to get a response from your support engineer!'
v = get_embedding(bedrock, query)              
result = classify(classes, v)
print(result)
```




`Clustering`
```python
import json
import boto3
import numpy as np
import pandas as pd
from sklearn.cluster import KMeans

def get_embedding(bedrock, text):
    modelId = 'amazon.titan-embed-text-v1'
    accept = 'application/json'
    contentType = 'application/json'
    input = {
            'inputText': text
        }
    body=json.dumps(input)
    response = bedrock.invoke_model(
        body=body, modelId=modelId, accept=accept,contentType=contentType)
    response_body = json.loads(response.get('body').read())
    embedding = response_body['embedding']
    return embedding

# main function
bedrock = boto3.client(
    service_name='bedrock-runtime'
)
names = ['Albert Einstein', 'Bob Dylan', 'Elvis Presley', 
         'Isaac Newton', 'Michael Jackson', 'Niels Bohr', 
         'Taylor Swift', 'Hank Williams', 'Werner Heisenberg', 
         'Stevie Wonder', 'Marie Curie', 'Ernest Rutherford']
embeddings = []
for name in names:
    embeddings.append(get_embedding(bedrock, name))
# clustering
df = pd.DataFrame(data={'names': names, 'embeddings': embeddings})
matrix = np.vstack(df.embeddings.values)
n_clusters = 2
kmeans = KMeans(n_clusters = n_clusters, init='k-means++', random_state=42)
kmeans.fit(matrix)
df['cluster'] = kmeans.labels_
# result
print(df[['cluster', 'names']])
```



### Knowledge Base
- Knowledge Base는 문제 해결에 필요한 사실과 규칙 등이 저장되어 있는 데이터베이스입니다. LLM을 통해 Customized된 답변을 얻기 위해서 여러분만의 Knowledge Base를 구축하는 것이 하나의 방법이 될 수 있습니다.
- 우리가 앞선 과정에서 진행하였던 OpenSearch Index에 문서들을 저장하는 과정이 Knowledge Base를 구축하는 과정이라고 볼 수 있습니다.
- 상대성 이론에 관현 문서들을 Knowledge Base에 추가해봅시다.




### RAG(Retrieval Augmented Generation)
검색 증강 생성 
- Convert the prompt (question text) into embedding.
- (R) Retrieve N most relevant entries from the knowledge base. This is treated as the context of the conversation.
  - (R) Knowledge Base에서 연관있는 N개의 문서를 반환받습니다.
- (A) Augment the prompt with the context by prepending the context to the question text. This end result is the context aware prompt.
  - (A) 반환된 문서를 이용하여 프롬프트에 Context 부분을 질문 텍스트 앞에 포함시킵니다.(이를 프롬프트를 "증강"하였다고 표현합니다.)
- (G) Generate the answer by feeding the context aware prompt to the foundation model.
  - (G) Foundation Model에 "증강"된 프롬프트를 전송하여 답변을 생성합니다.

즉, 이를 이용하면 우리가 프롬프트를 작성할 때 미리 Context를 작성하지 않아도 Knowledge Base에서 검색된 결과가 프롬프트에 포함되어 Foundation Model에 전송됩니다.



```python
import boto3
import json
import requests
from requests_aws4auth import AWS4Auth

def get_embedding(bedrock, text):
    modelId = 'amazon.titan-embed-text-v1'
    accept = 'application/json'
    contentType = 'application/json'
    input = {
            'inputText': text
        }
    body=json.dumps(input)
    response = bedrock.invoke_model(
        body=body, modelId=modelId, accept=accept,contentType=contentType)
    response_body = json.loads(response.get('body').read())
    embedding = response_body['embedding']
    return embedding
    
def search(embedding, limit=1):
    # prepare for OpenSearch Serverless
    service = 'aoss'
    credentials = boto3.Session().get_credentials()
    awsauth = AWS4Auth(
        credentials.access_key, 
        credentials.secret_key, 
        region, 
        service, 
        session_token=credentials.token
    )
    # search
    index = 'demo-index'
    datatype = '_search'
    url = host + '/' + index + '/' + datatype
    headers = {'Content-Type': 'application/json'}
    document = {
        'size': limit,
        'query': {
            'knn': {
                'embedding': {
                    'vector': embedding,
                    'k': limit
                }
            }
        }
    }
    # response
    response = requests.get(url, auth=awsauth, json=document, headers=headers)
    response.raise_for_status()
    data = response.json()
    output = ''
    for item in data['hits']['hits']:
        output += item['_source']['content'] + '\n'
    return output

# main function
bedrock = boto3.client(service_name='bedrock-runtime')
# this is the original prompt (query text)
prompt = 'What does Albert Einstein do?'
# convet the query text into embedding
embedding = get_embedding(bedrock, prompt)
# retrieve 5 most relevant entries from the knowledge base
info = search(embedding, limit=5)
# augment the prompt with the context
prompt = 'Use the context below to answer the question:\n\n=== Context ===\n{0}\n\n=== Question ===\n{1}'.format(info, prompt)
# ask the foundation model
model_id = 'mistral.mistral-large-2402-v1:0'
message = {"role": "user", "content": [{"text": prompt}]}
messages = [message]
inference_config = {"temperature": 0.5, "topP": 0.9, "maxTokens": 500}
response = bedrock.converse(
    modelId=model_id,
    messages=messages,
    inferenceConfig=inference_config
)
content = response['output']['message']['content']
for item in content:
    print(item['text'])
```

