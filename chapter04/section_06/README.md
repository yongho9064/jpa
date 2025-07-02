## 4.6 기본 키 매핑

지금까지 예제에서는 `@Id` 어노테이션만 사용해서 회원의 기본 키를 애플리케이션에서 직접 할당했다. 기본 키를 직접 할당하는 대신, 데이터베이스가 생성해주는 값을 사용하려면 어떻게 매핑해야 하는지 알아보자.

데이터베이스마다 기본 키를 생성하는 방식이 서로 다르므로 이 문제를 해결하기는 쉽지 않다. JPA는 이런 문제들을 해결하기 위해 다음과 같은 데이터베이스 기본 키 생성 전략을 제공한다.

*   **직접 할당**: 기본 키를 애플리케이션에서 직접 할당한다.
*   **자동 생성**: 대리 키(Surrogate Key) 사용 방식
    *   `IDENTITY`: 기본 키 생성을 데이터베이스에 위임한다. (예: MySQL의 `AUTO_INCREMENT`)
    *   `SEQUENCE`: 데이터베이스 시퀀스를 사용하여 기본 키를 할당한다. (예: Oracle의 `SEQUENCE`)
    *   `TABLE`: 키 생성 전용 테이블을 만들어 시퀀스처럼 사용한다.

자동 생성 전략이 다양한 이유는 데이터베이스 벤더마다 지원하는 방식이 다르기 때문이다. 예를 들어 오라클 데이터베이스는 시퀀스를 제공하지만, MySQL은 시퀀스를 제공하지 않고 대신 `AUTO_INCREMENT` 기능을 제공한다.

따라서 `SEQUENCE`나 `IDENTITY` 전략은 사용하는 데이터베이스에 의존적이다. `TABLE` 전략은 특정 데이터베이스에 종속되지 않는다.

기본 키를 직접 할당하려면 `@Id`만 사용하면 되고, 자동 생성 전략을 사용하려면 `@Id`와 함께 `@GeneratedValue` 어노테이션을 추가하고 원하는 키 생성 전략을 선택하면 된다.

> #### **⚠️ 주의: `hibernate.id.new_generator_mappings`**
>
> 키 생성 전략을 사용하려면 `persistence.xml`에 `hibernate.id.new_generator_mappings=true` 속성을 반드시 추가해야 한다. 하이버네이트는 더 효과적이고 JPA 규격에 맞는 새로운 키 생성 전략을 개발했지만, 과거 버전과의 호환성을 위해 이 옵션의 기본값을 `false`로 유지하고 있다.
>
> 이 옵션을 `true`로 설정하면 키 생성 성능을 최적화하는 `allocationSize` 속성의 동작 방식이 달라진다. `allocationSize`는 뒤에서 자세히 설명한다.
>
> ```xml
> <property name="hibernate.id.new_generator_mappings" value="true"/>
> ```

---

### 4.6.1 기본 키 직접 할당 전략

기본 키를 직접 할당하려면 아래 코드와 같이 `@Id`로 매핑하면 된다.

```java
@Id
@Column(name = "id")
private String id;
```

`@Id` 적용이 가능한 자바 타입은 다음과 같다.

*   자바 기본형 (`int`, `long`, ...)
*   자바 래퍼형 (`Integer`, `Long`, ...)
*   `String`
*   `java.util.Date`
*   `java.sql.Date`
*   `java.math.BigDecimal`
*   `java.math.BigInteger`

이 전략은 `em.persist()`로 엔티티를 저장하기 전에 애플리케이션에서 기본 키를 직접 할당하는 방식이다.

```java
Board board = new Board();
board.setId("id1");  // 기본 키 직접 할당
em.persist(board);
```

> #### **💡 참고**
>
> 기본 키 직접 할당 전략에서 식별자 값 없이 저장하면 예외가 발생하는데, 어떤 예외가 발생하는지는 JPA 표준에 정의되어 있지 않다. 하이버네이트 구현체를 사용하면 JPA 최상위 예외인 `javax.persistence.PersistenceException`이 발생하며, 내부적으로는 하이버네이트의 `org.hibernate.id.IdentifierGenerationException` 예외를 포함하고 있다.

---

### 4.6.2 IDENTITY 전략

`IDENTITY`는 기본 키 생성을 데이터베이스에 위임하는 전략이다. 주로 **MySQL, PostgreSQL, SQL Server, DB2**에서 사용한다. 예를 들어 MySQL의 `AUTO_INCREMENT` 기능이 대표적이다.

```sql
-- MySQL의 AUTO_INCREMENT 예제 DDL
CREATE TABLE BOARD (
    ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    DATA VARCHAR(255)
);

INSERT INTO BOARD(DATA) VALUES ('A');
INSERT INTO BOARD(DATA) VALUES ('B');
```

테이블 생성 시 기본 키 컬럼인 `ID`에 `AUTO_INCREMENT`를 추가하면, 데이터베이스에 값을 저장할 때 ID 컬럼을 비워두어도 데이터베이스가 순서대로 값을 채워준다. `IDENTITY` 전략은 이처럼 데이터베이스에 값을 저장하고 나서야 기본 키 값을 구할 수 있을 때 사용한다.

매핑은 `@GeneratedValue`의 `strategy` 속성 값을 `GenerationType.IDENTITY`로 지정하면 된다.

**예제 4.9: IDENTITY 매핑 코드**
```java
@Entity
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //...
}
```

**예제 4.10: IDENTITY 사용 코드**
```java
private static void logic(EntityManager em) {
    Board board = new Board();
    em.persist(board);
    // board.id = 1
    System.out.println("board.getId() = " + board.getId());
}
```

위 예제에서 `em.persist()`를 호출하여 엔티티를 저장한 직후, 할당된 식별자 값을 출력했다. 출력된 값 `1`은 저장 시점에 데이터베이스가 생성한 값을 JPA가 조회한 것이다.

> #### **⚠️ 주의: IDENTITY 전략과 쓰기 지연**
>
> 엔티티가 영속 상태가 되려면 식별자가 반드시 필요하다. 그런데 `IDENTITY` 전략은 엔티티를 데이터베이스에 **저장해야만** 식별자를 구할 수 있다. 이 때문에 `em.persist()`를 호출하는 즉시 `INSERT` SQL이 데이터베이스에 전달된다. 결과적으로, 이 전략은 **트랜잭션을 지원하는 쓰기 지연이 동작하지 않는다.**

> #### **💡 참고: IDENTITY 전략과 최적화**
>
> `IDENTITY` 전략은 데이터를 `INSERT`한 후에 기본 키 값을 조회해야 하므로, JPA는 추가로 데이터베이스를 조회해야 한다. 하지만 JDBC 3에 추가된 `Statement.getGeneratedKeys()`를 사용하면 데이터를 저장하면서 동시에 생성된 기본 키 값을 얻어올 수 있다. 하이버네이트는 이 메소드를 사용하여 데이터베이스와 한 번만 통신하도록 최적화한다.

---

### 4.6.3 SEQUENCE 전략

데이터베이스 시퀀스는 유일한 값을 순서대로 생성하는 특별한 데이터베이스 오브젝트다. `SEQUENCE` 전략은 이 시퀀스를 사용하여 기본 키를 생성한다. 이 전략은 시퀀스를 지원하는 **Oracle, PostgreSQL, DB2, H2** 데이터베이스에서 사용할 수 있다.

**예제 4.11: 시퀀스 DDL**
```sql
CREATE TABLE BOARD (
  ID BIGINT NOT NULL PRIMARY KEY,
  DATA VARCHAR(255)
);

-- 시퀀스 생성
CREATE SEQUENCE BOARD_SEQ START WITH 1 INCREMENT BY 1;
```

**예제 4.12: 시퀀스 매핑 코드**
```java
@Entity
@SequenceGenerator(
    name = "BOARD_SEQ_GENERATOR",
    sequenceName = "BOARD_SEQ", // 매핑할 데이터베이스 시퀀스 이름
    initialValue = 1,
    allocationSize = 1
)
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
                    generator = "BOARD_SEQ_GENERATOR")
    private Long id;
    //...
}
```

위 예제에서는 `@SequenceGenerator`를 사용하여 `BOARD_SEQ_GENERATOR`라는 이름의 시퀀스 생성기를 등록했다. `sequenceName` 속성으로 `BOARD_SEQ`를 지정하여, JPA가 이 시퀀스 생성기를 실제 데이터베이스의 `BOARD_SEQ` 시퀀스와 매핑하도록 했다.

그 후 `@GeneratedValue`의 `strategy`를 `GenerationType.SEQUENCE`로 설정하고, `generator` 속성으로 방금 등록한 시퀀스 생성기를 선택했다. 이제 `id` 식별자 값은 `BOARD_SEQ_GENERATOR`가 할당한다.

**예제 4.13: SEQUENCE 사용 코드**
```java
private static void logic(EntityManager em) {
    Board board = new Board();
    em.persist(board);
    // board.id = 1
    System.out.println("board.getId() = " + board.getId());
}
```

위 코드는 `IDENTITY` 전략과 같아 보이지만, 내부 동작 방식은 다르다.

*   **`SEQUENCE` 전략**: `em.persist()` 호출 시, 먼저 데이터베이스 시퀀스를 사용해 식별자를 조회하고, 엔티티에 할당한 후 영속성 컨텍스트에 저장한다. `INSERT` SQL은 트랜잭션 커밋 시점에 실행된다. (쓰기 지연이 가능하다)
*   **`IDENTITY` 전략**: `em.persist()` 호출 시, 먼저 엔티티를 데이터베이스에 저장한 후, 생성된 식별자를 조회하여 엔티티에 할당한다. (쓰기 지연이 불가능하다)

#### @SequenceGenerator 속성

| 속성 | 설명 | 기본값 |
| :--- | :--- | :--- |
| `name` | 식별자 생성기 이름 (필수) | **필수** |
| `sequenceName` | 데이터베이스에 등록된 시퀀스 이름 | `hibernate_sequence` |
| `initialValue` | DDL 생성 시에만 사용됨. 시퀀스 시작 값. | 1 |
| `allocationSize`| 시퀀스 한 번 호출에 증가하는 수 (성능 최적화용) | 50 |
| `catalog`, `schema`| 데이터베이스 catalog, schema 이름 | |

매핑에 따른 DDL 생성 예시는 다음과 같다.
```sql
create sequence [sequenceName] start with [initialValue] increment by [allocationSize];
```

> #### **⚠️ 주의: `allocationSize`의 기본값**
>
> `SequenceGenerator.allocationSize`의 기본값은 `50`이다. 이 때문에 JPA가 자동으로 생성하는 데이터베이스 시퀀스는 `increment by 50`으로 설정된다. 즉, 시퀀스를 호출할 때마다 값이 50씩 증가한다. 데이터베이스 시퀀스의 증가 값이 `1`이라면, **반드시 `allocationSize`를 `1`로 설정해야 한다.**

> #### **💡 참고: SEQUENCE 전략과 최적화 (`allocationSize`)**
>
> `SEQUENCE` 전략은 식별자를 얻기 위해 데이터베이스와 추가 통신이 필요하다.
>
> 1.  식별자를 구하기 위해 데이터베이스 시퀀스 조회 (예: `SELECT BOARD_SEQ.NEXTVAL FROM DUAL`)
> 2.  조회한 시퀀스 값을 기본 키로 사용하여 데이터 `INSERT`
>
> JPA는 `@SequenceGenerator.allocationSize`를 사용해 시퀀스 접근 횟수를 줄여 성능을 최적화한다. 예를 들어 `allocationSize`가 `50`이면, JPA는 시퀀스를 한 번 호출하여 값을 50만큼 증가시키고(예: 1 → 51), `1`부터 `50`까지의 값을 메모리에 할당해 순차적으로 사용한다. 51번째 ID가 필요하면 다시 시퀀스를 호출하여 값을 101로 증가시키고, `51`부터 `100`까지 메모리에서 사용한다.
>
> 이 최적화 방법은 시퀀스 값을 미리 선점하므로 여러 JVM이 동시에 동작해도 기본 키 값이 충돌하지 않는 장점이 있다. 반면, 데이터베이스에 직접 접근해서 데이터를 등록할 때는 시퀀스 값이 한 번에 많이 증가한 것처럼 보일 수 있다. 만약 INSERT 성능이 크게 중요하지 않다면 `allocationSize`를 `1`로 설정하면 된다.
>
> 이 최적화 방법은 앞서 설명한 `hibernate.id.new_generator_mappings` 속성을 `true`로 설정해야 제대로 동작한다.
>
> `false`일 경우(과거 방식): `allocationSize`가 50이면, 시퀀스에서 `1`을 받아오면 애플리케이션에서 `1`부터`50`까지 사용하고, 다음으로 시퀀스에서 `2`를 받아오면 `51`부터`100`까지 사용하는 방식이었다.

> #### **💡 참고**
>
> `@SequenceGenerator`는 다음과 같이 `@GeneratedValue` 바로 옆에 함께 선언할 수도 있다.
> ```java
> @Entity
> public class Board {
>     @Id
>     @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "...")
>     @SequenceGenerator(name = "...", sequenceName = "...", ...)
>     private Long id;
> }
> ```

### 4.6.4 TABLE 전략
