# **01. JPA 소개**

> **JPA(Java Persistence API)**는 반복적인 `CRUD` `SQL`을 자동으로 처리할 뿐만 아니라, **객체 모델링**과 **관계형 데이터베이스** 사이의 패러다임 불일치 문제를 해결하는 자바 표준 기술이다.

JPA는 애플리케이션 실행 시점에 SQL을 동적으로 생성하여 실행한다. 이를 통해 개발자는 SQL을 직접 작성하는 대신, **어떤 `SQL`이 실행될지 예측**하며 객체 중심으로 개발에 집중할 수 있다.

> **참고**: JPA가 생성하는 `SQL`은 대부분 쉽게 예측 가능하며, 통계나 복잡한 분석 쿼리의 경우 직접 `SQL`을 작성하는 기능(네이티브 SQL)도 지원한다.

---

## **JPA 사용의 이점**

### **1. 생산성 향상**
- 기본적인 `CRUD` `SQL`(INSERT, SELECT, UPDATE, DELETE)을 작성할 필요가 없다.
- 조회된 결과를 객체에 매핑하는 작업도 JPA가 자동으로 처리하므로, 데이터 접근 계층(DAO)의 코드가 획기적으로 줄어든다.

### **2. 유지보수 용이**
- `SQL`이 아닌 **객체 중심**으로 개발하므로 비즈니스 로직을 파악하고 수정하기 쉽다.
- 엔티티에 필드가 추가되거나 삭제되어도 관련된 `SQL`과 매핑 코드를 일일이 수정할 필요가 없다. JPA가 이러한 변경을 감지하여 `SQL`을 자동으로 처리한다.

### **3. 패러다임 불일치 해결**
- 상속, 연관관계, 객체 그래프 탐색 등 객체지향적인 모델링을 데이터베이스에 반영할 수 있다.

### **4. 데이터베이스 독립성 확보**
- `JPA`는 특정 데이터베이스에 종속되지 않는 표준 인터페이스이다.
- 애플리케이션 코드를 수정하지 않고, 설정 파일만 변경하여 데이터베이스 벤더를 손쉽게 교체할 수 있다.

### **5. 테스트 용이성**
- 데이터베이스에 의존하지 않는 순수한 객체 상태의 단위 테스트 작성이 편리하다.
- 이는 버그 발생 가능성을 줄이고 애플리케이션의 안정성을 높인다.

---

### **핵심 요약**

> 개발자는 반복적인 `SQL` 처리와 같은 단순 작업을 JPA에 위임하고, **더 나은 객체 모델링**과 **충분한 테스트 작성**에 집중함으로써 애플리케이션의 전체적인 품질을 향상시킬 수 있다.

---

# **1.1 SQL을 직접 다룰 때의 문제점**

**관계형 데이터베이스**는 오늘날 가장 대중적이고 신뢰성 높은 데이터 저장소이다. 따라서 대부분의 자바 애플리케이션은 관계형 데이터베이스를 사용하며, `JDBC` API를 통해 `SQL`을 데이터베이스에 전달하는 방식으로 데이터를 관리한다.

### **1.1.1 반복적인 코드와 SQL**

`SQL`을 직접 다룰 때 발생하는 문제점을 회원 관리 기능 개발 예제를 통해 알아본다.

> **예제 1.1: 회원 객체**
> ```java
> public class Member {
>     private String memberId;
>     private String name;
>     // ... getter, setter
> }
> ```

이 객체를 데이터베이스에서 관리하기 위한 `MemberDAO`(Data Access Object)를 구현한다.

> **예제 1.2: 회원용 DAO**
> ```java
> public class MemberDAO {
>     public Member find(String memberId) { /*...*/ } // 조회
>     public void save(Member member) { /*...*/ }    // 저장
> }
> ```

**`find()` 메소드 구현 단계:**
1.  **회원 조회용 SQL 작성**
    ```sql
    SELECT MEMBER_ID, NAME FROM MEMBER M WHERE MEMBER_ID = ?
    ```
2.  **JDBC API로 SQL 실행**
    ```java
    ResultSet rs = stmt.executeQuery(sql);
    ```
3.  **조회 결과를 Member 객체로 매핑**
    ```java
    String memberId = rs.getString("MEMBER_ID");
    String name = rs.getString("NAME");
    
    Member member = new Member();
    member.setMemberId(memberId);
    member.setName(name);
    ```

**`save()` 메소드 구현 단계:**
1.  **회원 등록용 SQL 작성**
    ```sql
    INSERT INTO MEMBER (MEMBER_ID, NAME) VALUES(?,?)
    ```
2.  **Member 객체의 값을 SQL 파라미터에 바인딩**
    ```java
    pstmt.setString(1, member.getMemberId());
    pstmt.setString(2, member.getName());
    ```
3.  **JDBC API로 SQL 실행**
    ```java
    pstmt.executeUpdate();
    ```
수정, 삭제 기능 또한 이와 유사한 `SQL` 작성과 `JDBC` API 사용을 반복해야 한다. 만약 회원 객체를 자바 컬렉션에 보관했다면 `list.add(member);` 한 줄로 저장이 끝났을 것이다. 이처럼 객체 모델과 데이터 중심의 관계형 데이터베이스 사이에는 큰 간극이 존재한다.

> #### **근본적인 문제점**
> 데이터베이스 `CRUD`를 위해 너무 많은 `SQL`과 `JDBC` API 코드를 작성해야 한다. 모든 테이블마다 이와 유사한 작업을 반복해야 하므로, 테이블이 수십, 수백 개로 늘어나면 엄청난 양의 `SQL`과 반복 코드가 생성된다.

### **1.1.2 SQL에 의존적인 개발**

비즈니스 요구사항 변경으로 `Member` 객체와 테이블에 연락처(`tel`) 필드가 추가되었다고 가정하자.

> **예제 1.3: 필드 추가된 Member 클래스**
> ```java
> public class Member {
>   private String memberId;
>   private String name;
>   private String tel; // 추가
>   // ...
> }
> ```

이 변경사항을 반영하려면 **모든 관련 코드**를 수정해야 한다.

-   **등록(INSERT):** `INSERT` SQL에 `TEL` 컬럼을 추가하고, `pstmt.setString(3, ...)` 코드를 추가해야 한다.
-   **조회(SELECT):** `SELECT` 절에 `TEL`을 추가하고, `ResultSet`에서 값을 읽어 `member.setTel()`을 호출하는 코드를 추가해야 한다.
-   **수정(UPDATE):** `UPDATE` SQL과 관련 `JDBC` 코드 역시 모두 변경해야 한다.

이제 회원이 특정 팀에 소속된다는 요구사항이 추가되었다. `Member` 객체는 `Team` 객체를 참조하게 된다.

> **예제 1.4: 연관관계가 추가된 Member 클래스**
> ```java
> class Member { 
>   private String memberId;
>   private Team team; // 연관된 팀 객체 추가
>   // ...
> }
> ```
개발자는 회원 정보와 함께 팀 이름도 조회하기 위해 `member.getTeam().getTeamName()` 코드를 사용했지만, `member.getTeam()`은 항상 `null`을 반환했다. 데이터베이스에는 모든 회원이 팀에 소속되어 있는데도 말이다.

원인은 `MemberDAO.find()` 메소드가 기존 SQL을 그대로 사용해 팀 정보를 조회하지 않았기 때문이다. 팀 정보까지 함께 조회하려면 `JOIN`이 포함된 새로운 `SQL`을 사용하는 `findWithTeam()` 메소드를 별도로 만들어 호출해야 했다.

> **예제 1.5: 새로운 조회 메소드 추가**
> ```java
> public class MemberDAO {
>   public Member find(String memberId) {
>     // SELECT MEMBER_ID, NAME, TEL FROM MEMBER M ...
>   }
>   public Member findWithTeam(String memberId) {
>     // SELECT M.*, T.* FROM MEMBER M JOIN TEAM T ON ...
>   }
> }
> ```

이러한 방식은 다음과 같은 심각한 문제를 야기한다.

> `Member` 객체가 연관된 `Team` 객체를 사용할 수 있는지는 전적으로 DAO가 실행하는 `SQL`에 달려 있다.

결국, 데이터 접근 계층(DAO)을 사용해 `SQL`을 숨겨도, 개발자는 어떤 `SQL`이 실행되는지 확인하기 위해 DAO 내부 코드를 항상 열어봐야 한다.

-   **진정한 계층 분할 실패:** 물리적으로는 `SQL`을 DAO에 숨겼지만, 논리적으로는 엔티티가 `SQL`에 **강하게 의존**하는 구조가 된다.
-   **신뢰할 수 없는 엔티티:** 개발자는 엔티티 객체만 보고는 그 상태를 완전히 신뢰할 수 없다.
-   **유지보수 비용 증가:** 엔티티에 필드 하나를 추가할 때마다 관련된 모든 `DAO`의 `CRUD` 코드와 `SQL`을 변경해야 한다.

### **1.1.3 JPA를 통한 문제 해결**

JPA는 이러한 문제들을 해결한다. 개발자는 직접 `SQL`을 작성하는 대신 JPA가 제공하는 API를 호출하고, JPA가 개발자 대신 적절한 `SQL`을 생성하여 데이터베이스에 전달한다.

#### **1. 저장**
```java
jpa.persist(member); // 저장
```
`persist()` 메소드를 호출하면, JPA가 `Member` 객체의 매핑 정보를 분석하여 적절한 `INSERT` SQL을 생성하고 실행한다.

#### **2. 조회**
```java
String memberId = "helloId";
Member member = jpa.find(Member.class, memberId); // 조회
```
`find()` 메소드는 객체 하나를 조회한다. JPA는 `SELECT` SQL을 생성하여 실행하고, 그 결과를 `Member` 객체로 만들어 반환한다.

#### **3. 수정**
```java
Member member = jpa.find(Member.class, memberId);
member.setName("이름변경");  // 객체의 상태만 변경
```
JPA는 별도의 수정 메소드를 제공하지 않는다. 대신, 트랜잭션 범위 안에서 조회한 객체의 상태를 변경하면, 트랜잭션이 커밋될 때 JPA가 변경을 감지하여 적절한 `UPDATE` SQL을 자동으로 생성하고 실행한다.

#### **4. 연관된 객체 조회**
```java
Member member = jpa.find(Member.class, memberId);
Team team = member.getTeam(); // 연관된 객체를 자유롭게 조회
```
JPA를 사용하면 객체 그래프를 자유롭게 탐색할 수 있다. `member.getTeam()`을 호출하는 시점에 JPA가 필요하다고 판단하면, 연관된 `Team` 객체를 조회하는 `SELECT` SQL을 실행한다. 개발자는 더 이상 `JOIN` SQL을 직접 다룰 필요가 없다.

이처럼 JPA를 사용하면 개발자는 `SQL`이 아닌 **객체 그 자체에 집중**하여 비즈니스 로직을 구현할 수 있다.