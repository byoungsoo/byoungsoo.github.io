OU - 글로벌 

Control Tower는 홈 리전을 선택할 수 있음

OU는 기본적으로 CT에 통합됨. 만약 Control Tower를 활성화화면 OU가 활성화됨 



아무것도 설정하지 않은 계정의 Control Tower 에 가면 '랜딩존 설정'을 해야함. 랜딩존이 설정되지 않은 상태에서는 OU, Controls를 등의 메뉴가 보이지 않음. 




### OU
1. Control Tower와 Organization Unit 서비스 연관관계
2. OU는 CT에 의존성이 없지만 CT는 OU를 사용한다. 
3. CT에서는 기본적으로 등록되지 않은 계정이 Root OU 밑에 있다. 이런 부분을 OU를 통해서는 모두 조작가능하나 하지 말아야 한다. (예외 상황이 하나 있음 - AWSControlTowerExecution Role이 )
4. 모든 OU는 기본적으로 15개의 필수 예방 제어가 설정됨. (이것은 등록되지 않은 계정에도 적용된다.) (예방 제어는 SCP를 통해 구현된다) (추가적인 제어는 등록되지 않은 계정에는 적용되지 않음)
5. AWSControlTowerExecution 로만 CT 리소스를 제어할 수 있음. 따라서 모든 계정에는 AWSControlTowerExecution 계정이 미리 생성이 되어 있어야 CT리소스가 가능함. -> AWSControlTowerExecution는 조직 등록을 하기 위한 전제조건이다. 
6. SCP 중 하나는 AWSControlTowerExecution에 대한 생성/변경을 하지 못하도록 하는 제어가 있음 따라서 AWSControlTowerExecution를 생성해야 하는 경우에는 RootOU로 잠시 옮겨서 AWSControlTowerExecution를 생성하고 OU를 옮겨서 SCP적용이되게끔 해야 함 (RootOU는 15개의 필수 예방 제어가 등록되지 않음)
7. 