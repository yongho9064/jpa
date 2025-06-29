## 3.4 영속성 컨텍스트의 특징

영속성 컨텍스트의 특징은 다음과 같다.

1.  **영속성 컨텍스트와 식별자 값**
    - 영속성 컨텍스트는 엔티티를 식별자 값(`@Id`로 테이블의 기본 키와 매핑한 값)으로 구분한다.
    - 따라서 **영속 상태는 식별자 값이 반드시 있어야 한다.** 식별자 값이 없으면 예외가 발생한다.

2.  **영속성 컨텍스트와 데이터베이스 저장**
    - JPA는 보통 트랜잭션을 커밋하는 순간 영속성 컨텍스트에 새로 저장된 엔티티를 데이터베이스에 반영하는데 이것을 `플러시(flush)`라 한다.

3.  **영속성 컨텍스트가 엔티티를 관리할 때의 장점**
    - 1차 캐시 (1st Level Cache)
    - 동일성(Identity) 보장
    - 트랜잭션을 지원하는 쓰기 지연 (Transactional Write-Behind)
    - 변경 감지 (Dirty Checking)
    - 지연 로딩 (Lazy Loading)

지금부터 영속성 컨텍스트가 왜 필요하고 어떤 이점이 있는지 엔티티를 CRUD하면서 그 이유를 알아보자.

### 3.4.1 엔티티 조회

영속성 컨텍스트는 내부에 캐시를 가지고 있는데 이것을 **1차 캐시**라 한다. 영속 상태의 엔티티는 모두 이곳에 저장된다. 쉽게 이야기하면 영속성 컨텍스트 내부에 `Map`이 하나 있는데, 키는 `@Id`로 매핑한 식별자이고 값은 엔티티 인스턴스다.

```java
// 엔티티를 생성한 상태(비영속)
Member member = new Member();
member.setId("member1");
member.setUsername("회원1");

// 엔티티를 영속
em.persist(member);
```
이 코드를 실행하면 1차 캐시에 회원 엔티티를 저장한다. 이때 회원 엔티티는 아직 데이터베이스에 저장되지 않았다.

> **그림 3.5 영속성 컨텍스트 1차 캐시**
>
> ![영속성 컨텍스트 1차 캐시](https://velog.velcdn.com/images%2Fseho100%2Fpost%2F422af98f-9d49-4d28-b06f-0624f9d12560%2Fimage.png)

1차 캐시의 키는 식별자 값이며, 이 식별자 값은 데이터베이스 기본 키와 매핑되어 있다. 따라서 영속성 컨텍스트에 데이터를 저장하고 조회하는 모든 기준은 데이터베이스 기본 키 값이다.

이번에는 엔티티를 조회해보자.

```java
Member member = em.find(Member.class, "member1");
```

`find()` 메소드의 첫 번째 파라미터는 엔티티 클래스 타입이고, 두 번째는 조회할 엔티티의 식별자 값이다. `em.find()`를 호출하면 먼저 1차 캐시에서 엔티티를 찾고, 만약 찾는 엔티티가 1차 캐시에 없으면 데이터베이스에서 조회한다.

#### 1차 캐시에서 조회

`em.find()`를 호출하면 우선 1차 캐시에서 식별자 값으로 엔티티를 찾는다. 만약 찾는 엔티티가 있으면 데이터베이스를 조회하지 않고 메모리에 있는 1차 캐시에서 엔티티를 바로 반환한다.

> **그림 3.6 1차 캐시에서 조회**
>
> ![1차 캐시에서 조회](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdna%2FcxCtEP%2FbtrsJM118zT%2FAAAAAAAAAAAAAAAAAAAAALsdiAG140Q4XMjuP6g11gFPRhz2dALFcgx8PZ1OvPby%2Fimg.png%3Fcredential%3DyqXZFxpELC7KVnFOS48ylbz2pIh7yKj8%26expires%3D1751295599%26allow_ip%3D%26allow_referer%3D%26signature%3Dd7QXOX3ZF1bo9kC4NTOd6LnRb6s%253D)

다음 코드는 1차 캐시에 있는 엔티티를 조회하므로 `SELECT` SQL이 실행되지 않는다.
```java
Member member = new Member();
member.setId("member1");
member.setUsername("회원1");

// 1차 캐시에 저장됨
em.persist(member);

// 1차 캐시에서 조회 (SQL 실행 X)
Member findMember = em.find(Member.class, "member1");
```

#### 데이터베이스에서 조회

만약 `em.find()`를 호출했는데 엔티티가 1차 캐시에 없으면, 엔티티 매니저는 데이터베이스에서 엔티티를 조회하여 엔티티를 생성한다. 그리고 1차 캐시에 저장한 후에 영속 상태의 엔티티를 반환한다.

```java
// "member2"는 1차 캐시에 없으므로 DB에서 조회
Member findMember2 = em.find(Member.class, "member2");
```

> **그림 3.7 1차 캐시에 없어 데이터베이스 조회**
>
> ![1차 캐시에 없어 데이터베이스 조회](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdna%2FdHEMbF%2FbtryP16ysdR%2FAAAAAAAAAAAAAAAAAAAAAJUB3moXct1XT3UK5HJnE0f58MkXkXe52qBlrUr4L5aU%2Fimg.png%3Fcredential%3DyqXZFxpELC7KVnFOS48ylbz2pIh7yKj8%26expires%3D1751295599%26allow_ip%3D%26allow_referer%3D%26signature%3D2nAtnuFJtYRW31kSNXgmG2kp%252Bis%253D)

1. `em.find(Member.class, "member2")`를 실행한다.
2. `member2`가 1차 캐시에 없으므로 데이터베이스에서 조회한다.
3. 조회한 데이터로 `member2` 엔티티를 생성해서 1차 캐시에 저장한다. (영속 상태)
4. 조회한 엔티티를 반환한다.

이제 `member1`과 `member2` 엔티티 인스턴스는 모두 1차 캐시에 있다. 따라서 이후에 이 엔티티들을 다시 조회하면 메모리에 있는 1차 캐시에서 바로 불러오므로 성능상 이점을 누릴 수 있다.

#### 영속 엔티티의 동일성 보장

영속성 컨텍스트는 1차 캐시에 있는 같은 엔티티 인스턴스를 반환하므로 동일성(`==` 비교)을 보장한다.

```java
Member a = em.find(Member.class, "member1");
Member b = em.find(Member.class, "member1");

System.out.println(a == b); // 동일성 비교, 결과: true
```
**영속성 컨텍스트는 성능상 이점과 엔티티의 동일성을 보장한다.**

> **참고: 동일성(Identity)과 동등성(Equality)**
>
> *   **동일성**: 실제 인스턴스가 같다. 참조 값을 비교하는 `==` 비교의 값이 `true`이다.
> *   **동등성**: 실제 인스턴스는 다를 수 있지만, 인스턴스가 가지고 있는 값이 같다. 자바에서 동등성 비교는 `equals()` 메소드를 오버라이드하여 구현한다.

> **참고: 트랜잭션 격리 수준**
>
> JPA는 1차 캐시를 통해 **반복 가능한 읽기(REPEATABLE READ)** 등급의 트랜잭션 격리 수준을 데이터베이스가 아닌 애플리케이션 차원에서 제공한다는 장점이 있다.

---

### 3.4.2 엔티티 등록

엔티티 매니저는 트랜잭션을 커밋하기 직전까지 데이터베이스에 엔티티를 저장하지 않고 내부 쿼리 저장소에 `INSERT` SQL을 차곡차곡 모아둔다. 그리고 트랜잭션을 커밋할 때 모아둔 쿼리를 데이터베이스에 보내는데, 이것을 **트랜잭션을 지원하는 쓰기 지연(Transactional Write-Behind)**이라 한다.

```java
EntityManager em = emf.createEntityManager();
EntityTransaction transaction = em.getTransaction();
// 엔티티 매니저는 데이터 변경 시 트랜잭션을 시작해야 한다.
transaction.begin(); // 트랜잭션 시작

em.persist(memberA);
em.persist(memberB);
// 여기까지 INSERT SQL을 DB에 보내지 않는다.

// 커밋하는 순간 데이터베이스에 INSERT SQL을 보낸다.
transaction.commit(); // 트랜잭션 커밋
```
1.  `em.persist(memberA)`: `memberA`를 1차 캐시에 저장하고, `INSERT` SQL을 생성하여 쓰기 지연 SQL 저장소에 보관한다.
    > **그림 3.8 쓰기 지연, 회원 A 영속**
    >
    > ![쓰기 지연, 회원 A 영속](https://velog.velcdn.com/post-images%2Fconatuseus%2Fd4a2fb30-d09b-11e9-a657-a958e5af4073%2Fimage.png)

2.  `em.persist(memberB)`: `memberB`도 1차 캐시에 저장하고, `INSERT` SQL을 생성하여 쓰기 지연 SQL 저장소에 보관한다.
    > **그림 3.9 쓰기 지연, 회원 B 영속**
    >
    > ![쓰기 지연, 회원 B 영속](https://velog.velcdn.com/post-images%2Fconatuseus%2F51c8cae0-d09c-11e9-b275-49c1db32880d%2Fimage.png)

3.  `transaction.commit()`: 트랜잭션을 커밋하면 엔티티 매니저가 영속성 컨텍스트를 `플러시(flush)`한다. 플러시는 쓰기 지연 SQL 저장소에 모인 쿼리들을 데이터베이스에 보낸 후, 실제 데이터베이스 트랜잭션을 커밋한다.
    > **그림 3.10 쓰기 지연, 커밋**
    >
    > ![쓰기 지연, 커밋](https://velog.velcdn.com/post-images%2Fconatuseus%2Feb6c9c30-d09c-11e9-b0db-1597a34a142f%2Fimage.png)

#### 트랜잭션을 지원하는 쓰기 지연이 가능한 이유

데이터를 저장하는 로직은 아래 두 가지 경우 모두 동일한 결과를 낳는다.
1.  `save()`를 호출할 때마다 즉시 DB에 `INSERT` 쿼리를 보내고, 마지막에 트랜잭션을 커밋한다.
2.  `save()`를 호출할 때는 쿼리를 메모리에 모아두고, 트랜잭션을 커밋할 때 모아둔 쿼리를 한 번에 DB로 보낸다.

두 방법 모두 하나의 트랜잭션 범위 안에서 실행되므로, 커밋하면 모든 변경사항이 함께 저장되고 롤백하면 함께 취소된다. `INSERT` 쿼리를 바로 보내도 트랜잭션이 커밋되지 않으면 의미가 없기 때문에, 커밋 직전에 SQL을 전달해도 괜찮다. 이것이 트랜잭션을 지원하는 쓰기 지연이 가능한 이유이며, 이 기능을 활용해 모아둔 쿼리를 한 번에 전달하여 **성능을 최적화**할 수 있다.

---

### 3.4.3 엔티티 수정

#### SQL 수정 쿼리의 문제점

만약 비즈니스 로직에서 직접 `UPDATE` SQL을 다룬다면, 필드 일부만 수정하는 경우에도 모든 필드를 신경 써야 하는 등 번거로움이 많다. 이는 비즈니스 로직이 SQL에 의존하게 되는 문제점을 낳는다.

```sql
-- 이름과 나이만 바꾸고 싶어도 다른 필드까지 신경 써야 한다.
UPDATE MEMBER SET NAME = ?, AGE = ?, GRADE = ? WHERE id = ?
```

#### 변경 감지 (Dirty Checking)

JPA는 **변경 감지(Dirty Checking)** 기능을 통해 엔티티의 수정을 관리한다. 단순히 엔티티를 조회해서 데이터만 변경하면, JPA가 변경사항을 감지하여 데이터베이스에 자동으로 반영해준다.

```java
EntityManager em = emf.createEntityManager();
EntityTransaction transaction = em.getTransaction();
transaction.begin(); // 트랜잭션 시작

// 영속 엔티티 조회
Member memberA = em.find(Member.class, "memberA");

// 영속 엔티티 데이터 수정 (update() 메소드 같은 것이 없다)
memberA.setUsername("hi");
memberA.setAge(10);

transaction.commit(); // 트랜잭션 커밋
```

**변경 감지 동작 원리**
JPA는 엔티티를 영속성 컨텍스트에 보관할 때, 최초 상태를 복사해서 저장해두는데 이것을 **스냅샷**이라 한다.

> **그림 3.11 변경 감지**
>
> ![변경 감지](https://user-images.githubusercontent.com/87989933/197335056-12530693-d980-4ce0-8c88-e3ca0d3129fa.png)

1.  트랜잭션을 커밋하면 엔티티 매니저 내부에서 먼저 `플러시(flush())`가 호출된다.
2.  엔티티와 스냅샷을 비교해서 변경된 엔티티를 찾는다.
3.  변경된 엔티티가 있으면 수정 쿼리를 생성해서 쓰기 지연 SQL 저장소에 보낸다.
4.  쓰기 지연 저장소의 SQL을 데이터베이스에 보낸다.
5.  데이터베이스 트랜잭션을 커밋한다.

**변경 감지는 영속 상태의 엔티티에만 적용된다.** 비영속, 준영속 상태의 엔티티는 값을 변경해도 데이터베이스에 반영되지 않는다.

**UPDATE SQL**
JPA의 기본 전략은 엔티티의 **모든 필드를 업데이트**하는 것이다.

```sql
UPDATE MEMBER
SET
    NAME=?,
    AGE=?,
    GRADE=?,
    ...
WHERE
    ID=?
```
데이터 전송량이 증가하는 단점이 있지만, 다음과 같은 장점으로 인해 이 전략이 기본값이다.
*   수정 쿼리가 항상 같으므로, 애플리케이션 로딩 시점에 미리 생성해두고 재사용할 수 있다.
*   데이터베이스는 이전에 한 번 파싱된 쿼리를 재사용할 수 있다.

만약 수정된 데이터만 사용하여 동적으로 `UPDATE` SQL을 생성하고 싶다면, `@DynamicUpdate` 어노테이션을 사용하면 된다.

```java
@Entity
@org.hibernate.annotations.DynamicUpdate
@Table(name = "MEMBER")
public class Member { ... }
```
> **참고**
>
> 컬럼이 약 30개 이상으로 매우 많지 않다면 기본 전략의 성능이 더 좋을 수 있다. 일반적으로는 기본 전략을 사용하고, 최적화가 필요할 때 `@DynamicUpdate`를 고려하는 것이 좋다. 참고로 `INSERT` 시 `null`이 아닌 필드만으로 SQL을 생성하는 `@DynamicInsert` 어노테이션도 있다.

---

### 3.4.4 엔티티 삭제

엔티티를 삭제하려면 먼저 삭제 대상 엔티티를 조회해야 한다.

```java
// 삭제 대상 엔티티 조회
Member memberA = em.find(Member.class, "memberA");
// 엔티티 삭제
em.remove(memberA);
```
`em.remove()`를 호출하면 엔티티가 즉시 삭제되는 것이 아니라, 삭제 쿼리가 쓰기 지연 SQL 저장소에 등록된다. 이후 트랜잭션을 커밋하여 플러시를 호출하면 실제 데이터베이스에 삭제 쿼리가 전달된다.

> **참고**
>
> `em.remove(memberA)`를 호출하는 순간 `memberA`는 영속성 컨텍스트에서 제거된다. 이렇게 삭제된 엔티티는 재사용하지 말고 자연스럽게 가비지 컬렉션(GC)의 대상이 되도록 두는 것이 좋다.