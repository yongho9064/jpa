## 2.6 애플리케이션 개발

객체 매핑을 완료하고 `persistence.xml`로 JPA 설정도 완료했다고 가정한다. 이제 JPA 애플리케이션을 개발해보자.

```java
public class JpaMain {
    public static void main(String[] args) {

        // 엔티티 매니저 팩토리 - 생성
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook");
        // 엔티티 매니저 - 생성
        EntityManager em = emf.createEntityManager();
        // 트랜잭션 - 획득
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();             // 트랜잭션 - 시작
            logic(em);              // 비즈니스 로직 실행
            tx.commit();            // 트랜잭션 - 커밋
        } catch (Exception e) {
            tx.rollback();          // 트랜잭션 - 롤백
            e.printStackTrace();
        } finally {
            em.close();             // 엔티티 매니저 - 종료
        }
        emf.close();                // 엔티티 매니저 팩토리 - 종료
    }

    // 비즈니스 로직
    private static void logic(EntityManager em) {
        // ...
    }
}
```

코드는 크게 3부분으로 나뉘어 있다.

*   **엔티티 매니저 설정**
*   **트랜잭션 관리**
*   **비즈니스 로직**

엔티티 매니저 설정부터 살펴보자.

### 2.6.1 엔티티 매니저 설정

아래 그림을 보면서 엔티티 매니저의 생성 과정을 분석해보자.

> **그림 2.12 엔티티 매니저 생성 과정**
>
> ![엔티티 매니저 생성 과정](https://velog.velcdn.com/images/amoeba25/post/d69020f8-d619-43d9-aee0-2bdf1ad2205e/image.png)

#### 1. 엔티티 매니저 팩토리 생성

`persistence.xml`의 설정 정보를 사용해서 엔티티 매니저 팩토리를 생성해야 한다. 이때 `Persistence` 클래스를 사용하는데, 이 클래스는 엔티티 매니저 팩토리를 생성해서 JPA를 사용할 수 있게 준비한다.

```java
EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook");
```

이렇게 하면 `META-INF/persistence.xml`에서 이름이 `jpabook`인 영속성 유닛(persistence-unit)을 찾아 엔티티 매니저 팩토리를 생성한다.

이때 `persistence.xml`의 설정 정보를 읽어 JPA를 동작시키기 위한 기반 객체를 만들고, JPA 구현체에 따라 데이터베이스 커넥션 풀도 생성하므로 **엔티티 매니저 팩토리를 생성하는 비용은 아주 크다.**

따라서 **엔티티 매니저 팩토리는 애플리케이션 전체에서 딱 한 번만 생성하고 공유해서 사용해야 한다.**

#### 2. 엔티티 매니저 생성

```java
EntityManager em = emf.createEntityManager();
```

엔티티 매니저 팩토리에서 엔티티 매니저를 생성한다. JPA의 기능 대부분은 이 엔티티 매니저가 제공한다. 대표적으로 **엔티티 매니저를 사용해서 엔티티를 데이터베이스에 등록/수정/삭제/조회할 수 있다.**

엔티티 매니저는 내부에 데이터 소스(데이터베이스 커넥션)를 유지하면서 데이터베이스와 통신한다.

> **참고**
>
> **엔티티 매니저는 데이터베이스 커넥션과 밀접한 관계가 있으므로 스레드 간에 공유하거나 재사용하면 안 된다.**

#### 3. 종료

마지막으로 사용이 끝난 엔티티 매니저는 다음처럼 반드시 종료해야 한다.

```java
em.close(); // 엔티티 매니저 종료
```

애플리케이션을 종료할 때 엔티티 매니저 팩토리도 다음처럼 종료해야 한다.

```java
emf.close(); // 엔티티 매니저 팩토리 종료
```

### 2.6.2 트랜잭션 관리

JPA를 사용하면 항상 트랜잭션 안에서 데이터를 변경해야 한다. 트랜잭션 없이 데이터를 변경하면 예외가 발생한다. 트랜잭션을 시작하려면 엔티티 매니저(`em`)에서 트랜잭션 API를 받아와야 한다.

```java
EntityTransaction tx = em.getTransaction(); // 트랜잭션 API

try {
    tx.begin();     // 트랜잭션 - 시작
    logic(em);      // 비즈니스 로직 실행
    tx.commit();    // 트랜잭션 - 커밋
} catch (Exception e) {
    tx.rollback();  // 트랜잭션 - 롤백
}
```

트랜잭션 API를 사용해서 비즈니스 로직이 정상 동작하면 트랜잭션을 커밋하고, 예외가 발생하면 트랜잭션을 롤백한다.

### 2.6.3 비즈니스 로직

회원 엔티티를 하나 생성한 다음 엔티티 매니저를 통해 데이터베이스에 등록, 수정, 삭제, 조회한다.

> **예제 2.10 비즈니스 로직 코드**
>
> ```java
> private static void logic(EntityManager em) {
>
>     String id = "id1";
>     Member member = new Member();
>     member.setId(id);
>     member.setUsername("용호");
>     member.setAge(2);
>
>     // 등록
>     em.persist(member);
>
>     // 수정
>     member.setAge(28);
>
>     // 한 건 조회
>     Member findMember = em.find(Member.class, id);
>     System.out.println("findMember = " + findMember.getUsername() + ", age = " + findMember.getAge());
>
>     // 목록 조회
>     List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();
>     System.out.println("members.size = " + members.size());
>
>     // 삭제
>     em.remove(member);
> }
> ```

**출력 결과**
```console
findMember = 용호, age = 28
members.size = 1
```

비즈니스 로직을 보면 등록, 수정, 삭제, 조회 작업이 엔티티 매니저(`em`)를 통해서 수행되는 것을 알 수 있다.

#### 1. 등록

```java
String id = "id1";
Member member = new Member();
member.setId(id);
member.setUsername("용호");
member.setAge(2);

em.persist(member);  // 등록
```

엔티티를 저장하려면 엔티티 매니저의 `persist()` 메소드에 저장할 엔티티를 넘겨주면 된다. JPA는 회원 엔티티의 매핑 정보를 분석해서 다음과 같은 SQL을 만들어 데이터베이스에 전달한다.

```sql
INSERT INTO MEMBER (ID, NAME, AGE) VALUES ('id1', '용호', 2)
```

#### 2. 수정

```java
// 수정
member.setAge(28);
```

수정 부분을 보면 이상하다. 엔티티를 수정한 후에 `em.update()` 같은 메소드를 호출해야 할 것 같지만, 엔티티의 값만 변경했다. **JPA는 어떤 엔티티가 변경되었는지 추적하는 기능을 갖추고 있다.** 따라서 `member.setAge(28)`처럼 엔티티의 값만 변경하면 다음과 같은 `UPDATE` SQL을 생성해서 데이터베이스 값을 변경한다.

```sql
UPDATE MEMBER SET AGE=28 WHERE ID='id1'
```
#### 3. 삭제

```java
em.remove(member);
```

엔티티를 삭제하려면 엔티티 매니저의 `remove()` 메소드에 삭제하려는 엔티티를 넘겨준다. JPA는 다음 `DELETE` SQL을 생성해서 실행한다.

```sql
DELETE FROM MEMBER WHERE ID = 'id1'
```

#### 4. 한 건 조회

```java
Member findMember = em.find(Member.class, id);
```

`find()` 메소드는 조회할 엔티티 타입과 `@Id`로 매핑한 식별자 값으로 엔티티 하나를 조회하는 가장 단순한 메소드다. 이 메소드를 호출하면 다음 `SELECT` SQL을 생성해서 데이터베이스를 조회하고, 결과 값으로 엔티티를 생성해서 반환한다.

```sql
SELECT * FROM MEMBER WHERE ID='id1'
```

### 2.6.4 JPQL

```java
List<Member> members = em.createQuery("select m from Member m", Member.class)
                         .getResultList();
```

애플리케이션이 필요한 데이터만 데이터베이스에서 불러오려면 결국 검색 조건이 포함된 SQL을 사용해야 한다. JPA는 **JPQL(Java Persistence Query Language)** 이라는 쿼리 언어로 이런 문제를 해결한다.

JPA는 SQL을 추상화한 JPQL이라는 객체지향 쿼리 언어를 제공한다. JPQL은 SQL과 문법이 거의 유사해서 `SELECT`, `FROM`, `WHERE`, `GROUP BY`, `HAVING`, `JOIN` 등을 사용할 수 있다. 둘의 가장 큰 차이점은 다음과 같다.

*   **JPQL**은 **엔티티 객체**를 대상으로 쿼리한다. (클래스와 필드를 대상으로 쿼리)
*   **SQL**은 **데이터베이스 테이블**을 대상으로 쿼리한다.

위 예제 코드에서 `select m from Member m`이 바로 JPQL이다. 여기서 `from Member`는 회원 엔티티 객체를 말하는 것이지 `MEMBER` 테이블이 아니다. **JPQL은 데이터베이스 테이블을 전혀 알지 못한다.**

JPQL을 사용하려면 `em.createQuery(JPQL, 반환_타입)` 메소드를 실행해서 쿼리 객체를 생성한 후, `getResultList()` 같은 메소드를 호출하면 된다.

JPA는 JPQL을 분석해서 다음과 같이 적절한 SQL을 만들어 데이터베이스에서 데이터를 조회한다.

```sql
SELECT M.ID, M.NAME, M.AGE FROM MEMBER M
```

> **참고**
>
> JPQL은 엔티티와 속성의 대소문자를 명확하게 구분하지만, `SELECT`, `FROM`, `AS`와 같은 JPQL 키워드는 대소문자를 구분하지 않는다. SQL은 관례상 대소문자를 구분하지 않는 경우가 많다.
