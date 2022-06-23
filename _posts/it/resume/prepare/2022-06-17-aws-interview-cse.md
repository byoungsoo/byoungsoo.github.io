---
layout: resume
title: "AWS - Interview CSE"
author: "Bys"
category: resume
---


**채용과정**

1차 면접: Docker, PKI, Token, Oauth2.0, JWT, Kubernetes 인증, 배포전략, Affinity, Selector,  등등

2차 면접: Interview Tips

What to expect : This interview will cover your experience and how it relates to the team and position. You will be speaking directly with a manager or member of the team that is currently working in this space. Our goal during the session is to introduce you to our team and gauge your fit for Amazon Web Services.

What can help you before interviews : I encourage you to take a look at the attached links for information on our Amazon Leadership Principles, Interview Tips, and WORKING AT AMAZON, to help you prepare for the interview. We would look for strong examples/data points around our leadership principles during your interviews as well as passion/interest from candidates around our company/services during the interviews.

Click here to find out more information on interview preparations with AWS.


**LP**

1. Customer Obsession
   Leaders start with the customer and work backwards. They work vigorously to earn and keep customer trust. Although leaders pay attention to competitors, they obsess over customers.

   **고객 편의 제공**  
   1) LG화학 CDN서비스
   S: LG화학 홈페이지 및 기타 제품 광고 홈페이지등에 대해서 AWS Cloud로 전환하는 프로젝트에서 구성을 모두 마치고 테스트도 어느정도 마쳐가는 수순인 상황에서 서비스의 속도가 조금 느린 상황이었고
   레거시와 비교한 결과에서는 큰 차이가 없었음. 레거시와 비교한 결과에서는 큰 차이가 없었으나 대표홈페이지는 글로벌 고객들도 접속을 할 수 있는 페이지이고 회사를 대표하는 페이지 였기 때문에 성능 개선을 하면 좋겠다는 생각을 하였음 
   T: 그렇게 저는 cloudfront를 적용해서 성능개선을 하려고 작업을 시작했습니다.  
   A: AWS의 CloudFront CDN 서비스를 사용. cdn서비스에서는 origin을 지정하게 되어있는데 origin으로 서비스 alb를 등록했습니다.
   그런데 어플리케이션에서 리다이렉션이 있는 경우에는 리다이렉트가 되면서 브라우저상에 hostname이 alb dns의 주소로 변경되는 이슈들이 발생했습니다. 
   성능은 개선이 되었는데 리다이렉트후 클라이언트 브라우저에 호스트헤더가 변경이 되었고, 이 부분을 해결하기 위해서 또 많은 자료들을 찾아봤던 기억이 있습니다. 
   origin의 호스트 헤더를 전달하도록 whilelist header 부분에 저희 호스트 헤더를 넣어줘야 된다라고 하는 관련문서를 찾아봤고 이를 적용하면서
   R: 결과적으로는 이슈도 해결하고 고객들에게 빠른 서비스를 제공할 수 있게 되었습니다. 


   2) X-Ray 서비스
   S: 당시 상황은 처음에는 수집 어플리케이션, 제공 어플리케이션 두 종류의 어플리케이션만 Kubernetes환경에 배포될 예정이었는데 고객쪽에서 제공 어플리케이션을 기능별로 더 나누었으면 좋겠다는 변경사항이 있었습니다. 저희는 해당 요구사항에 맞춰 어플리케이션을 기능별로 나누어서 4개 정도의 어플리케이션으로 구성을 했었습니다.  
   로깅은 EFK를 통해 모두 통합된 한경으로 모이게 이미 구축이 되어있었고, 어플리케이션의 모니터링은 Jeniffer라고 하는 APM툴에 의해 분석이 되고 있었습니다. 
   다만 서비스가 나눠지고 고객들은 컨테이너 환경에 익숙하지 않자 오류가 발생했을 때 추적 및 대응을 하기에 어려워하는게 보였습니다. 
   마침 X-RAY VPC 엔드포인트가 작년에 나왔고하여 제가 데브온 프레임워크 담당자에게 X-Ray라는 서비스를 좀 설명드리고 이것을 적용하면 고객들이 좋아할 것 같다고 설득하여 적용을 시작했습니다. 
   T: 그렇게 저는 x-ray 서비스를 고객에게 제공하자는 업무를 가지고 시작을 했습니다. 
   A: 어플리케이션이 올라가는 파드에 사이드카패턴으로 x-ray daemon 컨테이너를 배포하고 iam role생성 및 service계정에 해당하는 role을 등록하여 정상적으로 x-ray서비스를 제공했습니다. 
   R: 고객분들은 이런 추적 서비스에 대해서 잘 모르고 계셨고, 저희가 다 구축한 후에 이런 서비스가 있는데 이용을 하시겠냐라고 물어봤을 때 너무 좋아하셔서 운영서버까지 적용했던 경험이 있습니다. 

   **신뢰를 얻어야함**
   S: 일정이 굉장히 촉박한 상황
   T: 가트너에서 이야기하는 MSA Outer 아키텍처에 대한 구성을 혼자서 진행했어야 하는 업무 
   A: 1) 고객으로 부터 신뢰를 얻기 위한 노력 고객을 리딩해야 한다!
      1) 일정에 대한 집착
      KB카드 마이데이터 프로젝트는 일정부터가 매우 촉박한 프로젝트 였습니다. 
      기존 AA분으로 투입되셨던 분이 허리디스크로 인하여 병가를 사용하게 되었고, 저는 투입된지 1주일 안에 개발 환경을 구축해주어야 하는 상황이었습니다. 그리고 그 때 까지 진행된 것은 거의 없었습니다. 
      진급하고 처음 혼자 투입된 프로젝트에서 저는 엄청난 부담감과 책임감 그리고 압박감을 받았습니다. 그리고 잘 해내고 싶었습니다. 

      KB카드 마이데이터 프로젝트에서 부하 테스트가 끝나가는 중 고객들이 Logging 환경 구축은 해주었지만, 로그를 통해서만 오류를 감지하는 것은 쉽지 않다고 이야기를 들었고, 최대한 고객 입장에서 어떻게 쉽게 오류를 찾아갈 수 있을지를 고민했습니다. 
      1차적으로는 오류가 발생하면 우선적으로 알람을 보내도록 하고, 2차적으로는 X-Ray를 통해 추적이 가능하도록 하는 것이 가장 좋은 선택지였습니다. 
      1차 목표를 달성하기 위해서 로그를 통해 알람을 보낼 수 있는 방법을 Kibana 에서 찾을 수 있었습니다. Elasticsearch에 저장된 Index들을 쿼리하여 특정 에러가 발생할 경우 담당자들에게 문자를 발송하는 것이었습니다. 
      또한 모든 어플리케이션 파드에 사이드카 형식으로 x-ray 데몬 컨테이너를 추가하여 어떤 어플리케이션에서 오류가 발생했는지를 쉽게 찾을 수 있도록 가이드하였습니다. 
   R: 개발/운영 환경을 모두 구축하고 부하테스트 까지 진행하면서 모든 일정안에 성공적으로 맞춤

   고객 집착의 경험 신뢰를 쌓기 위해 했던 것들.

2. Ownership
   Leaders are owners. They think long term and don’t sacrifice long-term value for short-term results. They act on behalf of the entire company, beyond just their own team. They never say “that’s not my job."

   1) 시스템 최적화 - 이 자원이 나의 돈이라면 나는 이렇게 할 것인가?
   S: 한화생명 MSP 프로젝트 당시 리소스 모니터링 & 자원 최적화


   S: 
   


3. Invent and Simplify
   Leaders expect and require innovation and invention from their teams and always find ways to simplify. They are externally aware, look for new ideas from everywhere, and are not limited by “not invented here." As we do new things, we accept that we may be misunderstood for long periods of time.

4. Are Right, A Lot
   Leaders are right a lot. They have strong judgment and good instincts. They seek diverse perspectives and work to disconfirm their beliefs.

5. Learn and Be Curious
   Leaders are never done learning and always seek to improve themselves. They are curious about new possibilities and act to explore them.

   1) 꾸준히 책이나 하고 개인적으로 제가 겪었던 사항들에 대해서 모두 기록을 좀 하는 편이며 문제의 원인 해결과정등을 좀 적어놓습니다. 
   업무 진행중에 대해서 잘 몰랐던 개념들 이런 것들을 일단 카테고리에 대한 메모를 해놓고 시간이 될 때마다 정리해 나가면서 학습을 좀 하고 있습니다.
   이 정리가 저한테는 예전 오답노트 와 같은 역할을해서 제가 부족한 부분들을 채워나가고 있습니다. 


6. Hire and Develop the Best
   Leaders raise the performance bar with every hire and promotion. They recognize exceptional talent, and willingly move them throughout the organization. Leaders develop leaders and take seriously their role in coaching others. We work on behalf of our people to invent mechanisms for development like Career Choice.

7. Insist on the Highest Standards
   Leaders have relentlessly high standards — many people may think these standards are unreasonably high. Leaders are continually raising the bar and drive their teams to deliver high quality products, services, and processes. Leaders ensure that defects do not get sent down the line and that problems are fixed so they stay fixed.

   1) 최고의 기준을 고집하기 위해 노력했던 적
   
   S:마이데이터 프로젝트에서 성능테스트를 통해 시스템의 품질을 이끌어내기 위해 노력했던 경험을 이야기 드리겠습니다. 
   모든 환경 구축이 끝나고 오픈 전 성능테스트를 진행하였습니다. 목표부하테스트, 임계부하테스트, 장기부하테스트 3단계로 나누어서 1주일간 진행을 하는 것이었습니다. 
   T: 저의 업무는 시스템 모니터링을 하면서 모든 이슈상황들에 대해서 대응하는 것이었습니다. 
   A: 제가 했던 많은 대응중에 몇 가지 사항들에 대해서 설명 드리겠습니다. 
   파드가 부하를 받다 제가 설정한 hpa 설정값에 따라 파드가 scale-out이 되고나서 트래픽을 받기 시작하는 순간 응답시간에 지연이 쭉 생기기 시작했습니다. 
   해당 원인을 분석하기 위해 몇 가지 가설들을 세웠고 프레임워크 담당자와 낸 결론은 어플리케이션이 기동되고 나서 첫 요청들에 대해서는 캐싱을 위해 메모리로 올리는 작업을 하는데 그 부분에서 지연이 생기는 것 같다고 판단했고, 이를 해결하기 위해 빠르게 해결방안을 찾았습니다.일단 파드가 scale-out이 된 바로 직후에는 다른 파드들보다 가중치를 적게 받으면 좋겠다는 생각을 했고, 
   target 그룹 속성중에 slow duration 옵션을 찾아 적용하였습니다. 
   R: 적용 후 테스트에서는 scale-out시 눈에 띄게 안정적인 모습을 보였지만 duration time에 대한 세부적인 시간을 맞추고자 몇 번의 테스트를 더 요청하여 가장 이상적인 시간을 적용하여
   scale-out에 적합한 시스템을 구축했다고 생각하고 있습니다. 

   또한 
   S: 목표부하테스트, 임계부하테스트에 대한 목적을 모두 달성하고 장기부하테스트를 실시했을 때 입니다. 
   장기부하테스트는 저희가 밤에 퇴근할 때 테스트를 걸어놓고 갔는데 다음날 오전에 하나의 파드가 죽고 재 생성이 되어있었습니다. 
   처음에는 파드가 잘 유지되고 있었어서 이걸 인지하지 못하고 있었었는데 2번 정도의 장기부하테스트를 한 후 항상 1~2개의 파드 START시간이 달라져 있었습니다. 
   그리고 APM툴에 들어가보니 이것도 마찬가지로 지연이 좀 발생을 하고 있었습니다. 
   T: 저는 이 부분이 좀 찝찝하게 느껴졌고 분명히 어떤 문제가 있었을 것이라고 판단이 되어서 빠르게 원인을 찾고 문제를 해결하기로 생각 했습니다. 
   A: 실제 파드에 들어가서 jstat 커맨드로 gc를 분석했더니 full gc가 지속적으로 발생하고 있었지만 old영역의 메모리는 떨어지지 않고 있었습니다. 
     그래서 밤에 장기부하테스트를 한 번 더 요청했고 오전 일찍 출근해서 어플리케이션의 힙덤프를 떠본결과 메모리 누수가 있는 것을 확인했습니다. 
     해당 부분에 대해서 프레임워크 담당자분께 가이드를 드려서 문제를 해결 하였고 
   R: 결과적으로는 정말 시스템의 품질이 엄청 높아졌고 어떤 이슈사항에서도 잘 버틸 수 있는 시스템을 만들었다 라는 생각에 굉장히 뿌듯했고, 성능테스트가 끝나고 나서 고객분들에게도 '잘해주셨다' 칭찬도 많이 받았던 경험이었습니다.

8. Think Big
   Thinking small is a self-fulfilling prophecy. Leaders create and communicate a bold direction that inspires results. They think differently and look around corners for ways to serve customers.

   1) 단기적인 성과보다 장기적인 관점에서 생각한 점


   2) 



9.  Bias for Action
   Speed matters in business. Many decisions and actions are reversible and do not need extensive study. We value calculated risk taking. 

   1) 정보가 불충분한 상황에서 빠르게 의사결정을 내려본 경험
   S: 솔루션에 대한 선택이었는데요. LG
   
   2) 




10. Frugality
    Accomplish more with less. Constraints breed resourcefulness, self-sufficiency, and invention. There are no extra points for growing headcount, budget size, or fixed expense.

11. Earn Trust
    Leaders listen attentively, speak candidly, and treat others respectfully. They are vocally self-critical, even when doing so is awkward or embarrassing. Leaders do not believe their or their team’s body odor smells of perfume. They benchmark themselves and their teams against the best.

12. Dive Deep
    Leaders operate at all levels, stay connected to the details, audit frequently, and are skeptical when metrics and anecdote differ. No task is beneath them.

    트러블 슈팅할 때 최대한 DeepDive 하게 -> lg화학에서 사용하던 사내 프레임워크 코어 소스를 달라고 하여 설정이 잘 못된 부분 확인 ! 
    기존에 잘 운영되던 어플리케이션에서 컨테이너로 소스를 전환하자 문제가 발생 os가 windows -> linux로 전환되면서 설정 파일을 읽을 때 순서가 잘 못되었던 것! 
    최종적으로는 설정의 문제도 있었지만, 소스의 문제가 있을 수 있었고 해당 내용을 내부 팀에게 전달하여 원인 파악과 해결방법을 같이 전달해준 적 !


13. Have Backbone; Disagree and Commit
    Leaders are obligated to respectfully challenge decisions when they disagree, even when doing so is uncomfortable or exhausting. Leaders have conviction and are tenacious. They do not compromise for the sake of social cohesion. Once a decision is determined, they commit wholly.

14. Deliver Results
    Leaders focus on the key inputs for their business and deliver them with the right quality and in a timely fashion. Despite setbacks, they rise to the occasion and never settle.

15. Strive to be Earth's Best Employer
    Leaders work every day to create a safer, more productive, higher performing, more diverse, and more just work environment. They lead with empathy, have fun at work, and make it easy for others to have fun. Leaders ask themselves: Are my fellow employees growing? Are they empowered? Are they ready for what's next? Leaders have a vision for and commitment to their employees' personal success, whether that be at Amazon or elsewhere.

16. Success and Scale Bring Broad Responsibility
    We started in a garage, but we're not there anymore. We are big, we impact the world, and we are far from perfect. We must be humble and thoughtful about even the secondary effects of our actions. Our local communities, planet, and future generations need us to be better every day. We must begin each day with a determination to make better, do better, and be better for our customers, our employees, our partners, and the world at large. And we must end every day knowing we can do even more tomorrow. Leaders create more than they consume and always leave things better than how they found them.

<br>

---

<br>

**예상질문**

0. 자기소개
   자기소개 하도록 하겠습니다. 저는 2015년도에 LG CNS에 처음 입사하여 4년간 LG전자 GERP생산팀에서 INV/IWMS 모듈을 맡아 ERP개발 및 유지/보수등의 운영 업무를 수행했습니다.
   이 후에는 클라우드 아키텍처팀으로 이동을 하였고 주로 AWS를 사용해서 대한항공 마이그레이션 프로젝트, LG 화학 Public Cloud전환 프로젝트, 한화생명 M-SFA 운영업무, KB카드 마이데이터 프로젝트 등을 수행하였습니다. 대한항공 프로젝트에서는 ERP/Middleware/HR 시스템을 담당하여 web/was 및 어플리케이션에 대한 이관을 진행하였고,
   LG 화학 Public cloud 전환 프로젝트에서는 
   이 후 작년 10월에는 카카오엔터프라이즈라는 회사로 이동하여 Openstack 기반의 공공클라우드 서비스 개발을 진행하고 있습니다. 
   주로 Pure오픈스택을 기반으로 컴포넌트 검증 및 콘솔 개발 업무를 진행하고 있습니다. 

2. 왜 아마존에 입사하고 싶은가요?
   1) AWS 서비스 품질과 완성도, 다양한 서비스에 대해서 매료되었다. 고객친화적 프로세스와 페이지를 경험했고 해당 서비스들을 이용하면서 
   AWS를 좋아하게 되었고 언제가는 꼭 AWS에서 일하리라는 목표가 생겼습니다. 
   아마도 지금은 저도 AWS에 기여를하고 AWS 성장해나가면서 상호 이익을 얻을 수 있지 않을까 생각합니다. 

   2) AWS 에서 만난 띄어난 직원들 내가 아는 실력이 좋은 동료들! 꼭 같이 일하고 싶다. 주변의 동료의 중요성!

3. 왜 이 부서로 지원을 하게 되었나요?
   1) 트러블 슈팅 자체는 스트레스를 받는 일이기도 하지만 반대로 문제 해결 과정에서 사람은 성장합니다. 
   문제를 해결하기 위해서는 문제의 원인이 무엇인지 파악하고 그 문제를 해결하기 위한 기술적인 지식을 습득하게 되는데 
   저 스스로도 많은 프로젝트에서 문제해결 과정을 거치면서 성장을 했다고 생각합니다. 
   
   2) 또한 문제 해결은 고객의 긴급한 이슈를 해결 해주는 것이기 때문에 해결과정이 끝나고 나면 얻게되는 보람이 있습니다. 
   
4. 어떤 문제해결과정들을 해보셨나요?
   1) LG화학 Public Cloud 전환 프로젝트는 기존 lg화학 대표홈페이지, 광고성 홈페이지 등을 AWS ECS를 사용하여 전환하는 프로젝트였는데요.
   어플리케이션은 DevonFrame이라는 프레임워크를 사용하고 있었고, OS는 W -> L , WAS는 J -> LENA 
   저는 거기서 기존 어플리케이션들을 분석하고 ECS환경에 맞게 전환하는 작업을 하고 있었습니다. 
   그런데 문제는 기존에 잘 동작하던 어플리케이션이 Container환경에서 기동될 때 정상기동이 되지 않는 현상이 있었습니다. 
   이 문제를 해결하기 위해서 첫 번째로는 Linux 서버에 LENA환경을 구축하여 기동했는데 또 잘 기동이 되었습니다. 

   1) 마이데이터 
   장기부하테스트시 지속적으로 파드가 스케일 아웃 되는 현상 

   1) 
   

5. 경험해본 어려운 일들?
   1) 인도 출장 GST 프로젝트 -> 
   S: 3년차 사원 때 선배들은 모두 출장을 반대하는 상황에서 저는 출장경험을 해보고자 손들고 지원. 정확한 프로세스를 다 인지하지 못하는 상황이었음
      힘든 이유는 정확한 프로세스를 잘 모르는 상황에서 혼자 해내야 한다는 부담감
   T: GST프로젝트 관련 자재 입/출고시 관련 Tax를 계산하고 그것에 대한 Invoice를 출력하는 프로그램이 있었음 IGST, CGST, SGST
   A: 혼자 잘 해내야 한다는 압박감속에 부지런히 공부하고 프로세스를 익혔고 , 인도 법인의 현지 직원들에게도 많이 물어보며 도움을 얻었음
      현지직원들의 지속적인 시스템 개선사항들에 대해서는 
   R: 결과적으로는 현지에서 지속적인 시스템 개선사항들에 대해 반영하며 수정해나갔고, 현지직원들과의 좋은 관계를 유지하며 성공적으로 프로젝트를 맞칠수 있었다. 
   일정 내 생산시스템에 GST관련된 프로그램들을 모두 개발하여 반영하였고 인도법인장으로부터 출장인원들에 대한 환대를 받을 수 있었다. 


   1) 마이데이터 프로젝트
   S: 동료선배의 병가로 긴급투입이 되었음 이미 프로젝트의 일정이 불가능한 상태였음 
   T: 가트너에서 
   A:
   R: 
   고객은 진행된 것이 없어 신뢰가 없는 상황이었음, 저도 Outer 아키텍처를 혼자 구축해내는 것은 처음이었기에 항상 퇴근하면 부족했던 부분들을 강의, 책, 구글링을 통해 검색하고 
   아침일찍 눈이떠지면 부지런히 7시 출근해서 항상 자리에 앉아 많은 고민과 결정을 진행했음. 결론적으로는 
   책임이라는 진급 후 처음 선배도 없는 상황에서 투입된 프로젝트였기 때문에 꼭 잘 해내고 싶었음 

6. 고객에게 만족스러운 경험을 제공 한 적 

7) LG화학 - CDN서비스 제공 (스스로)

8) 마이데이터 - X-Ray 서비스 제공 (스스로)

9) 한 번은 KB카드 마이데이터 프로젝트 진행 중 어플리케이션에서 마이데이터 제공시 오류가 발생한 적이 있었습니다. 
미이데이터는 크게 수집과 제공으로 나뉠 수 있고 제공은 kb카드에서 타금융사에 마이데이터 정보를 제공하는 것이고, 수집은 kb카드가 타금융사의 마이데이터 정보를 수집하는 것인데요 
kb카드 입장에서는 마이데이터 제공을 하는 중간에 오류가 발생했던 적이 있습니다. 그런데 해당 오류를 고객사에서 먼저 인지한 것이 아닌 금융사를 통해서 전달을 받게 된 것이죠 

금융프로젝트 이다보니 분위기도 무거웠고 담당 팀장님은 굉장히 화가 나있었습니다. 
오류가 발생할 수는 있지만 우리는 인지하지 못한 것에 대해서 화가 나계셨고, 이 문제를 해결 할 수 있도록 하라는 것이었습니다. 
물론 오류가 발생한 부분을 수정하는 것도 당연했지만 추후에는 이렇게 오류가 발생했을 때 담당자에게 오류 발생에 대한 인지를 할 수 있게 해주도록 하는 것이 필요해보였습니다. 

저는 데브온 프레임워크 담당자와 둘이 의논을 했습니다. 오류 발생시 특정 로그패턴으로 로그만 찍어달라! 
그러면 저는 엘라스틱서치에 저장된 로그를 기반으로 쿼리를 통해 특정 패턴이 추출될 경우 알람을 보낼 수 있도록 하겠다!

그래서 Kibana의 Alert 기능과 AWS의 SNS서비스를 다시 연동하고 문자발송을 위해 외부 베스핀글로벌의 OpsNow라고 하는 플랫폼을 통해 어플리케이션에서 오류가 발생할 경우
알람을 전달 할 수 있는 환경을 구축하였습니다. 

1) 고객사 파견이 되어 신뢰를 얻기 위해서도 많이 노력을 했던 것 같습니다. 
나가면 항상 고객들의 기술리딩은 할 수 있어야 된다고 생각했고 그래서 정말 급박했을 때는 오전 일찍 출근해서 문제를 좀 정의하고 


7. 

8. 주인의식을 가지고 일한적
   모든일에 있어 주인의식을 가지고 일하려 하고 있으며 특히나 제가 구축한 시스템의 경우에는 더 그렇습니다. 
   제가 구축한 시스템에서 장애가 발생하거나 혹은 제가 수정/배포 한 이 후에 장애가 발생하면 더 신경이쓰이고 얼른 확인 해봐야 한다고 생각을 많이 합니다. 
   마이데이터에서도 제가 목요일에 CI/CD 스크립트를 배포 한 이 후에 토요일 오전에 갑자기 장애가 발생했다는 메세지를 받았습니다. 



9.  단기적인성과보다 장기적인 가치를 둔 경험


10. 창의적인

11. 근검절약
   1) 시스템 최적화 
   LG CNS에서는 전문위원이라고 하는 직책이 있었습니다. 기술적으로 정점에 있는 분들이 가질 수 있는 직책이었고, 대한항공 프로젝트에서 한 위원님과 대화를 한 적이 있었습니다.
   클라우드 시스템이 도입이 되면서 시스템의 최적화의 영역이 좀 가치가 떨어진 것 같다. 
   최근에는 리소스가 부족한 경우에도 오토스케일링이나 클라우드 환경에서 쉽게 자원을 투입하여 리소스를 늘릴 수 있게 되었습니다.

   하지만 그럼에도 저는 이 모든 것들은 비용과 관련이 되어있다고 생각했습니다. 내 개인 프로젝트였어도 과연 회사 돈이 아닌 내 돈이 나가도 이렇게 할 것인가? 를 반문한다면 그렇지 않았습니다. 
   따라서 저는 최대한 시스템은 최적화가 되어 있고 그 상황에서 리소스가 부족할 때 스케일 업, 또는 스케일 아웃이 진행되는 것이 맞지 않나 라는 생각을 합니다. 

   kb카드 마이데이터 프로젝트에서도 부하테스트를 2주간에 걸쳐 진행한 적이 있습니다.
   이 때 마이크로서비스 들이 파드로 올라갔는데 장기부하테스트 마다 특정시간이 지니면 파드가 늘어나있었습니다. 
   저의 업무가 그랬기 때문에 더 신경쓴것도 있었지만 대부분의 사람들은 서비스가 정상이었기에 파드가 늘어났다는 것에 대해 별로 신경쓰지 않았던것 같습니다.
   

12. 내 장점? 단점?
    1) 책임감과 성실함 
    어떤 문제든 하나의 문제를 해결해야 하는 상황이오면 그 문제를 풀기전까지는 계속 고민하고 집착한다고 생각합니다. 
    그런 과정속에서 A를 해결하기 위해서 B라는 개념이 
    LG화학에서 -> 데브온 프레임이라고 하는 사내 프레임워크 

    1) 단점
    고집이 센편, 하고자 하는 일이 딱 생기면 그날 혹은 최대한 빠른 시일 내에 일을 해야 하는 편. 

    저는 스스로를 슬로우 스타터라고 생각하는데 이 부분이 단점인 경우들이 있습니다.
    취미도 그렇고 어떤 일을 시작할 때 열정적으로 시작하는 사람들이 있습니다. 무언가를 하기로 시작하면 학원도 다니고 오롯이 그들의 시간을 거기에 쏟아부기 시작합니다. 
    이런 사람들은 단기간내에 급속도로 성장하며 많은 결과를 창출하는 것 처럼 제 눈에 보입니다. 
    
    하지만 저는 어떤 일을 하거나 취미를 가지게 되면 처음부터 그렇게 모든 시간을 쏟아붑는 편은 아닌 것 같습니다. 
    하지만 그럼에도 저는 시작하게 되면 꾸준히 하는 편입니다. 

    
    