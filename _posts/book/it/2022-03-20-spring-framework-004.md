---
layout: post
title: "Spring 프로그래밍 (Chapter 8)[DB 연동]"
author: "Bys"
category: it_book
date: 2022-03-20 01:00:00
tags: book programming spring framework db @transactional 
---

## 8. DB 연동  
많은 웹 어플리케이션은 데이터를 보관하기 위해 MySQL이나 Oracle과 같은 DBMS를 사용한다. 
자바에서는 JDBC(Java Database Connectivity) API를 사용하거나 JPA(Java Persistence API), MyBatis와 같은 기술을 사용해서 DB연동을 처리한다. 
이 책에서는 JDBC를 위해 스프링이 제공하는 JdbcTemplate의 사용법을 설명한다.  

### 8.1 JDBC 프로그래밍의 단점을 보완하는 스프링  

![spring5_8_1](/assets/book/spring5/spring5_8_1.png){: width="60%" height="auto"}  

JDBC API를 이용하면 위의 코드처럼 DB연동에 필요한 Connection을 구한 다음 쿼리를 실행하기 위한 PreparedStatement를 생성한다. 
그리고 쿼리를 실행한 뒤에는 finally 블록에서 ResultSet, PreparedStatement, Connection을 닫는다.  

여기서 문제는 점선으로 표시한 부분이다. 점선으로 표시한 코드는 사실상 데이터 처리와는 상관없는 코드지만 JDBC 프로그래밍을 할 때 구조적으로 반복된다. 
실제 핵심은 점선으로 표시한 부분을 제외한 나머지 코드로 전체 코드의 절반도 되지 않는다.  

구조적인 반복을 줄이기 위한 방법은 템플릿 메서드 패턴과 전략 패턴을 함께 사용하는 것이다. 
스프링은 바로 이 두 패턴을 엮은 JdbcTemplate 클래스를 제공한다. 이 클래스를 사용하면 코드를 다음과 같이 변경할 수 있다.  

```Java
List<Member> results = jdbcTemplate.query(
    "select * from MEMBER where EMAIL = ?",
    new RowMapper<Member>(){
        @Override
        public Member mapRow(ResultSet rs, int rowNum) throws SQLException{
            Member member = new Member(rs.getString("Email"),
                                       rs.getString("PASSWORD"),
                                       rs.getString("NAME"),
                                       rs.getTimestamp("REGDATE")
                                      );
            member.setId(rs.getLong("ID"));
            return member;
        }
    }, email);
return results.isEmpty() ? null : results.get(0);
)
```
<br>

스프링이 제공하는 또 다른 장점은 트랜잭션 관리가 쉽다는 것이다. 
JDBC AP로 트랜잭션을 처리하려면  다음과 같이 Connection의 setAutoCommit(false)을 이용해서 자동커밋을 비활성화하고 commit()과 rollback() 메서드를 이용해서 트랜잭션을 커밋하거나 롤백해야 한다.  

```Java
public void insert(Member member){
    Connection conn = null
    PreparedStatement pstmt = null;
    try{
        conn = DirverManager.getConnection(~~);
        conn.setAutoCommit(false);

        //DB Query
        //~~

        conn.commit(); // Commit
    } catch(SQLException ex){
        if(conn != null){
            try{ conn. rollback(); } // Rollback
             catch (SQLException e){}
        }
    } finally {
        ~~
    }
    
}
```

스프링을 사용하면 트랜잭션을 적용하고 싶은 메서드에 @Transactional 어노테이션을 붙이기만 하면 된다. 
```Java
@Transactional
public void insert(Member member){
}
```
commit과 rollback 처리는 스프링이 알아서 처리하므로 코드를 작성하는 사람은 트랜잭션 처리를 제외한 핵심 코드만 집중해서 작성하면 된다.  


### 8.2 프로젝트 준비  

- spring-jdbc: JdbcTemplate 등 JDBC 연동에 필요한 기능을 제공
- tomcat-jdbc: DB Connection Pool 기능을 제공 (DB 커넥션 풀 기능을 제공하는 모듈로는 Tomcat JDBC, HikariCP, DBCP, c3p0 등이 존재)
- mysql-connection-java: MySQL 연결에 필요한 JDBC 드라이버를 제공 

스프링이 제공하는 트랜잭션 기능을 사용하려면 spring-tx 모듈이 필요한데, spring-jdbc 모듈에 대한 의존을 추가하면 spring-tx모듈도 자동으로 포함된다.  


### 8.3 DataSource 설정 
JDBC API는 DriverManager 외에 DataSource를 이용해서 DB 연결을 구하는 방법을 정의하고 있다.  
DataSource를 사용하면 다음 방식으로 Connection을 구할 수 있다.
```Java
Connection conn = null;
try{
    conn = dataSource.getConnection();
}
```
스프링이 제공하는 DB 연동 기능은 DataSource를 사용해서 DB Connection을 구한다. 
DB 연동에 사용할 DataSource를 스프링 빈으로 등록하고 DB 연동 기능을 구현한 빈 객체는 DataSource를 주입받아 사용한다.  

Tomcat JDBC 모듈은 javax.sql.dataSource를 구현한 DataSource 클래스를 제공한다. 
이 클래스를 스프링 빈으로 등록해서 DataSource로 사용할 수 있다.  

`DBConfig`
```Java
package config;

@Configuration
public class DbConfig {

	@Bean(destroyMethod = "close")
	public DataSource dataSource() {
		DataSource ds = new DataSource();
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUrl("jdbc:mysql://localhost/spring5fs?characterEncoding=utf8");
		ds.setUsername("spring5");
		ds.setPassword("spring5");
		ds.setInitialSize(2);
		ds.setMaxActive(10);
		ds.setTestWhileIdle(true);
		ds.setMinEvictableIdleTimeMillis(60000 * 3);
		ds.setTimeBetweenEvictionRunsMillis(10 * 1000);
		return ds;
	}
}
```
<br>

### 8.7 트랜잭션 처리  
이메일이 유효한지 여부를 판단하기 위해 실제로 검증 목적의 메일을 발송하는 서비스를 사용한 경험이 있을 것이다. 
이들 서비스는 이메일에 함께 보낸 링크를 클릭하면 최종적으로 이메일이 유효하다고 판단하고 해당 이메일을 사용할 수 있도록 한다. 
이렇게 이메일 인증 시점에 테이블의 데이터를 변경하는 기능은 다음 코드처럼 회원 정보에서 이메일을 수정하고 인증 상태를 변경하는 두 쿼리를 실행할 것이다. 
```Java
jdbcTemplate.update("update MEMBER set EMAIL = ?", email);
jdbcTemplate.update("insert into EMAIL_AUTH values(?, 'T')", email);
```
그런데 만약 첫 번째 쿼리를 실행한 후 두 번째 쿼리를 실행하는 시점에 문제가 발생하면 어떻게 될까? 
두 번째 쿼리가 실패했음에도 불구하고 첫 번째 쿼리 실행 결과가 DB에 반영되면 이후 해당 사용자의 이메일 주소는 인증되지 않은 채로 계속 남아 있게 될 것이다. 
따라서 두 번째 쿼리 실행에 실패하면 첫 번째 쿼리 실행 결과도 취소해야 올바른 상태를 유지한다. 
*이렇게 두 개 이상의 쿼리를 한 작업으로 실행해야 할 때 사용하는 것이 트랜잭션(Transaction)이다.* 
트랜잭션은 여러 쿼리를 논리적으로 하나의 작업으로 묶어준다. 한 트랜잭션으로 묶인 쿼리 중 하나라도 실패하면 전체 쿼리를 실패로 간주하고 실패 이전에 실행한 쿼리를 취소한다. 
쿼리 실행 결과를 취소하고 DB를 기존 상태로 되돌리는 것을 롤백(rollback)이라고 부른다. 반면에 트랜잭션으로 묶이 모든 쿼리가 성공해서 쿼리 결과를 DB에 실제로 반영하는 것을 커밋(commit)이라고 한다. 

트랜잭션을 시작하면 트랜잭션을 커밋하거나 롤백할 때까지 실행한 쿼리들이 하나의 작업 단위가 된다. 
JDBC는 Connection의 setAutoCommit(false)를 이용해서 트랜잭션을 시작하고 commit()과 rollback()을 이용해서 트랜잭션을 커밋하거나 롤백한다. 

#### 8.7.1 @Transactional을 이용한 트랜잭션 처리  
스프링이 제공하는 @Transactional 어노테이션을 사용하면 트랜잭션 범위를 매우 쉽게 지정할 수 있다. 
다음과 같이 트랜잭션 범위에서 실행하고 싶은 메서드에 @Transactional 어노테이션만 붙이면 된다.  

```Java
package spring;

public class ChangePasswordService {

	private MemberDao memberDao;

	@Transactional
	public void changePassword(String email, String oldPwd, String newPwd) {
		Member member = memberDao.selectByEmail(email);
		if (member == null)
			throw new MemberNotFoundException();

		member.changePassword(oldPwd, newPwd);

		memberDao.update(member);
	}

	public void setMemberDao(MemberDao memberDao) {
		this.memberDao = memberDao;
	}

}
```
스프링은 @Transactional 어노테이션이 붙은 changePassword() 메서드를 동일한 트랜잭션 범위에서 실행한다. 
따라서 memberDao.selectByEmail()에서 실행하는 쿼리와 member.changePassword()에서 실행하는 쿼리는 한 트랜잭션에 묶인다.  

@Transactional 어노테이션이 제대로 동작하려면 다음의 두 가지 내용을 스프링 설정에 추가해야 한다.  
- 플랫폼 트랜잭션 매니저(PlatformTransactionManager) 빈 설정
- @Transactional 어노테이션 활성화 설정 

<br>

```Java
package config;

@Configuration
@EnableTransactionManagement 
public class AppCtx {

	@Bean(destroyMethod = "close")
	public DataSource dataSource() {
		DataSource ds = new DataSource();
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUrl("jdbc:mysql://localhost/spring5fs?characterEncoding=utf8");
		ds.setUsername("spring5");
		ds.setPassword("spring5");
		ds.setInitialSize(2);
		ds.setMaxActive(10);
		ds.setTestWhileIdle(true);
		ds.setMinEvictableIdleTimeMillis(60000 * 3);
		ds.setTimeBetweenEvictionRunsMillis(10 * 1000);
		return ds;
	}

	// PlatformTransactionManager 빈 설정 
	@Bean
	public PlatformTransactionManager transactionManager() {
		DataSourceTransactionManager tm = new DataSourceTransactionManager();
		tm.setDataSource(dataSource());
		return tm;
	}
}
```
PlatformTransactionManager는 스프링이 제공하는 트랜잭션 매니저 인터페이스이다. 스프링은 구현기술에 상관없이 동일한 방식으로 트랜잭션을 처리하기 위해 이 인터페이스를 제공한다. 
JDBC는 DataSourceTransactionManager 클래스를 PlatformTransactionManager로 사용한다.  

@EnableTransactionManagement 어노테이션은 @Transactional 어노테이션이 붙은 메서드를 트랜잭션 범위에서 실행하는 기능을 활성화한다. 
등록된 PlatformTransactionManager 빈을 사용해서 트랜잭션을 적용한다.  

트랜잭션 처리를 위한 설정을 완료하면 트랜잭션 범위에서 실행하고 싶은 스프링 빈 객체의 메서드에 @Transactional 어노테이션을 붙이면 된다.  




#### 8.7.2 @Transactional과 프록시  
트랜잭션을 시작하고, 커밋하고, 롤백하는 것은 누가 대체 어떻게 처리하는 걸까? 이에 관한 내용을 이해하려면 프록시를 알아야 한다.  

앞서 7장에서 여러 빈 객체에 공통으로 적용되는 기능을 구현하는 방법으로 AOP를 설명했는데 트랜잭션도 공통 기능 중 하나이다. 
스프링은 @Transactional 어노테이션을 이용해서 트랜잭션을 처리하기 위해 내부적으로 AOP를 사용한다. 
스프링에서 AOP는 프록시를 통해서 구현된다는 것을 기억한다면 트랜잭션 처리도 프록시를 통해서 이루어진다고 유추할 수 있을 것이다.  

실제로 @Transactional 어노테이션을 적용하기 위해 @EnableTransactionManagement 태그를 사용하면 스프링은 @Transactional 어노테이션이 적용된 빈 객체를 찾아서 알맞은 프록시 객체를 생성한다. 

다음의 MainForCPS 예제의 경우 그림과 같은 구조로 프록시를 사용하게 된다.  
`MainForCPS`  
```Java
public class MainForCPS {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = 
				new AnnotationConfigApplicationContext(AppCtx.class);

        ChangePasswordService cps = 
                ctx.getBean("changePwdSvc", ChangePasswordService.class);
        try {
            cps.changePassword("madvirus@madvirus.net", "1111", "1234");
            System.out.println("암호를 변경했습니다.");
        } catch (MemberNotFoundException e) {
            System.out.println("회원 데이터가 존재하지 않습니다.");
        } catch (WrongIdPasswordException e) {
            System.out.println("암호가 올바르지 않습니다.");
        }

		ctx.close();

	}
}


package spring;
public class ChangePasswordService {

	private MemberDao memberDao;

	@Transactional
	public void changePassword(String email, String oldPwd, String newPwd) {
		Member member = memberDao.selectByEmail(email);
		if (member == null)
			throw new MemberNotFoundException();

		member.changePassword(oldPwd, newPwd);

		memberDao.update(member);
	}

	public void setMemberDao(MemberDao memberDao) {
		this.memberDao = memberDao;
	}

}
```

![spring5_8_4](/assets/book/spring5/spring5_8_4.png){: width="50%" height="auto"}  

ChangePasswordService 클래스의 메서드에 @Transactional 어노테이션이 적용되어 있으므로 스프링은 트랜잭션 기능을 적용한 프록시 객체를 생성한다. 
ctx.getBean("changePwdSvc", ChangePasswordService.class); 코드를 실행하면 ChangePasswordService 객체 대신에 트랜잭션 처리를 위해 생성한 프록시 객체를 리턴한다.  

이 프록시 객체는 @Transactional 어노테이션이 붙은 메서드를 호출하면 그림의 과정처럼 PlatformTransactionManager를 사용해서 트랜잭션을 시작한다. 
트랜잭션을 시작한 후 실제 객체의 메서드를 호출하고, 성공적으로 실행되면 트랜잭션을 커밋한다.  

#### 8.7.3 @Transactional 적용 메서드의 롤백 처리  

![spring5_8_5](/assets/book/spring5/spring5_8_5.png){: width="60%" height="auto"}  

@Transactional을 처리하기 위한 프록시 객체는 원본 객체의 메서드를 실행하는 과정에서 RuntimeException이 발생하면 트랜잭션을 롤백한다. 
별도 설정을 추가하지 않으면 발생한 exception이 RuntimeException일 때 트랜잭션을 롤백한다. 
WrongIdPasswordException 클래스를 구현할 때 RuntimeException을 상속한 이유는 바로 트랜잭션 롤백을 염두해 두었기 때문이다. 

JdbcTemplate은 DB 연동 과정에서 문제가 있으면 DataAccessException을 발생한다고 했는데 DataAccessException 역시 RuntimeException을 상속받고 있다. 
따라서 JdbcTemplate의 기능을 실행하는 도중 exception이 발생해도 프록시는 트랜잭션을 롤백한다.  

SQLException은 RuntimeException을 상속하고 있지 않으므로 SQLException이 발생하면 트랜잭션을 롤백하지 않는다. 
RuntimeException 뿐만 아니라 SQLException이 발생하는 경우에도 트랜잭션을 롤백하고 싶다면 @Transactional의 rollbackFor 속성을 사용해야 한다. 

```Java
@Transactional(rollbackFor = {SQLException.class})
public void someMethod(){
}
```

<br>

#### 8.7.4 @Transactional의 주요 속성

`@Transactional 어노테이션의 주요 속성`  

| 속성         | 타입         | 설명  |
| :---        | :---        | :--- |
| value       | String      | 트랜잭션을 관리할 때 사용할 PlatformTransactionManager 빈의 이름을 지정한다. 기본값은 " " 이다. |
| propagation | Propagation | 트랜잭션 전파 타입을 지정한다. 기본값으 Propagation.REQUIRED이다. | 
| isolation   | Isolation   | 트랜잭션 격리 레벨을 지정한다. 기본값은 Isolation.DEFAULT 이다.  |
| timeout     | int         | 트랜잭션 제한 시간을 지정한다. 기본 값은 -1로 이 경우 데이터베이스의 타임아웃 시간을 사용한다. 초 단위로 지정한다. |

> Propagation.REQUIRED: 메서드를 수행하는 데 트랜잭션이 필요하다는 것을 의미한다. 현재 진행 중인 트랜잭션이 존재하면 해당 트랜잭션을 사용한다. 존재하지 않으면 새로운 트랜잭션을 생성한다. 

`Isolation 열거 타입에 정의된 값`  

| 속성              | 타입  |
| :---             | :--- |
| DEFAULT          | 기본 설정을 사용한다. |
| READ_UNCOMMITTED | 다른 트랜잭션이 커밋하지 않은 데이터를 읽을 수 있다.|
| READ_COMMITTED   | 다른 트랜잭션이 커밋한 데이터를 읽을 수 있다. |
| REPEATABLE_READ  | 처음에 읽어 온 데이터와 두 번째 읽어 온 데이터가 동일한 값을 갖는다. |
| SERIALIZABLE     | 동일한 데이터에 대해서 동시에 두 개 이상의 트랜잭션을 수행할 수 없다. |

> 트랜잭션 격리 레벨은 동시에 DB에 접근할 때 그 접근을 어떻게 제어할지에 대한 설정을 다룬다. 
> 트랜잭션 격리 레벨을 SERIALIZABLE로 설정하면 동일 데이터에 100개 연결이 접근하면 한 번에 한 개의 연결만 처리한다. 
> 이는 마치 100명이 줄을 서서 차례대로 처리되는 것과 비슷하기 때문에 전반적인 응답 속도가 느려지는 문제가 발생할 수 있다. 

<br>

#### 8.7.6 트랜잭션 전파  

`Propagation 열거 타입의 주요 값`  

| 속성           | 타입  |
| :---          | :--- |
| REQUIRED      | 메서드를 수행하는 데 트랜잭션이 필요하다는 것을 의미한다. 현재 진행 중인 트랜잭션이 존재하면 해당 트랜잭션을 사용한다. 존재하지 않으면 새로운 트랜잭션을 생성한다. |
| MANDATORY     | 메서드를 수행하는 데 트랜잭션이 필요하다는 것을 의미한다. 하지만 REQUIRED와 달리 진행 중인 트랜잭션이 존재하지 않을 경우 exception이 발생한다. |
| REQUIRES_NEW  | 항상 새로운 트랜잭션을 시작한다. 진행 중인 트랜잭션이 존재하면 기존 트랜잭션을 일시 중지하고 새로운 트랜잭션을 시작한다. 새로 시작된 트랜잭션이 종료된 뒤에 기존 트랜잭션이 계속된다. |
| SUPPORTS      | 메서드가 트랜잭션을 필요로 하지는 않지만, 진행 중인 트랜잭션이 존재하면 트랜잭션을 사용한다는 것을 의미한다. 진행 중인 트랜잭션이 존재하지 않더라도 메서드는 정상적으로 동작한다. |
| NOT_SUPPORTED | 메서드가 트랜잭션을 필요로 하지 않음을 의미한다. SUPPORTS와 달리 진행 중인 트랜잭션이 존재할 경우 메서드가 실행되는 동안 트랜잭션은 일시 중지되고 메서드 실행이 종료된 후에 트랜잭션을 계속 진행한다. |
| NEVER         | 메서드가 트랜잭션을 필요로 하지 않는다. 만약 진행 중인 트랜잭션이 존재하면 exception이 발생한다. |
| NESTED        | 진행 중인 트랜잭션이 존재하면 기존 트랜잭션에 중첩된 트랜잭션에서 메서드를 실행한다. 진행 중인 트랜잭션이 존재하지 않으면 REQUIRED와 동일하게 동작한다. 메인 트랜잭션이 롤백되면 중첩된 로그 트랜잭션도 같이 롤백되지만, 반대로 중첩된 로그 트랜잭션이 롤백되어도 메인 작업에 이상이 없다면 메인 트랜잭션은 정상적으로 커밋된다.

<br>


이 설명을 이해하려면 트랜잭션 전파가 무엇인지 알아야 한다. 이해를 돕기 위해 아래의 자바 코드와 스프링 설정을 보자. 

```Java
public class SomeService {
    private AnyService anyService;

    @Transactional
    public void some(){
        anyService.any();
    }
    public void setAnyService(AnyService as){
        this.anyService = as;
    }
}

public class AnyService {

    @Transactional
    public void any(){
        ......
    }
}
```

```Java
@Configuration
@EnableTransactionManagement
public class Config {
    @Bean
    public SomeService some(){
        SomeService some = new SomeService();
        some.setAnyService(any());
        return some;
    }
    @Bean
    public AnyService any(){
        return new AnyService();
    }
}
```
SomeService 클래스와 AnyService 클래스는 둘 다 @Transactional 어노테이션을 적용하고 있다. 위의 설정에 따르면 두 클래스에 대해 프록시가 생성된다. 
즉 SomeService의 some() 메서드를 호출하면 트랜잭션이 시작되고 AnyService의 any()메서드를 호출해도 트랜잭션이 시작된다. 
그런데 some() 메서드는 내부에서 다시 any() 메서드를 호출하고 있다. 이 경우 트랜잭션 처리는 어떻게 될 까?

@Transactional의 propagation 속성은 기본 값이 Propagation.REQUIRED이다. 
REQUIRED는 현재 진행 중인 트랜잭션이 존재하면 해당 트랜잭션을 사용하고 존재하지 않으면 새로운 트랜잭션을 생성한다고 했다.  

처음 some() 메서드를 호출하면 트랜잭션을 새로 시작한다. 
하지만 some() 메서드 내부에서 any() 메서드를 호출하면 이미 some() 메서드에 의해 시작된 트랜잭션이 존재하므로 any() 메서드를 호출하는 시점에는 트랜잭션을 새로 생성하지 않는다. 
대신 존재하는 트랜잭션을 그대로 사용한다. 즉 some() 메서드와 any() 메서드를 한 트랜잭션으로 묶어서 실행하는 것이다.  

만약 any() 메서드에 적용한 @Transactional의 propagation 속성값이 REQUIRES_NEW라면 기존 트랜잭션이 존재하는지 여부에 상관없이 항상 새로운 트랜잭션을 시작한다.
따라서 이 경우에는 some() 메서드에 의해 트랜잭션이 생성되고 다시 any() 메서드에 의해 트랜잭션이 생성된다.  

<br>

다음 코드를 보자.  
```Java
public class ChangePasswordService {

	private MemberDao memberDao;

	// Transactional 있음
	@Transactional
	public void changePassword(String email, String oldPwd, String newPwd) {
		Member member = memberDao.selectByEmail(email);
		if (member == null)
			throw new MemberNotFoundException();

		member.changePassword(oldPwd, newPwd);

		memberDao.update(member);
	}
}


public class MemberDao {

	private JdbcTemplate jdbcTemplate;

	// Transactional 없음	
	public void update(Member member) {
		jdbcTemplate.update(
				"update MEMBER set NAME = ?, PASSWORD = ? where EMAIL = ?",
				member.getName(), member.getPassword(), member.getEmail());
	}
}

```
changePassword() 메서드는 MemberDao의 update() 메서들르 호출하고 있다. 
그런데 MemberDao.update() 메서드는 @Transactional 어노테이션이 적용되어 있지 않다. 이런 경우 트랜잭션 처리는 어떻게 될까?

비록 update() 메서드에 @Transactional이 붙어 있지 않지만 JdbcTemplate 클래스 덕에 트랜잭션 범위에서 쿼리를 실행할 수 있게 된다. 
JdbcTemplate은 진행 중인 트랜잭션이 존재하면 해당 트랜잭션 범위에서 쿼리를 실행한다. 




<br><br><br>

---

**Reference**  
- 초보 웹 개발자를 위한 스프링 5 (최범균)

---
