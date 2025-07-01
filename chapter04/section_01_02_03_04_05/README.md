# 04. 엔티티 매핑

JPA를 사용하는 데 가장 중요한 일은 엔티티와 테이블을 정확히 매핑하는 것이다.
JPA는 다양한 매핑 어노테이션을 지원하는데 크게 4가지로 분류할 수 있다. 아래는 대표 어노테이션들이다.

*   **객체와 테이블 매핑**: `@Entity`, `@Table`
*   **기본 키 매핑**: `@Id`
*   **필드와 컬럼 매핑**: `@Column`
*   **연관관계 매핑**: `@ManyToOne`, `@JoinColumn`

> **💡 참고**
>
> 매핑 정보는 XML이나 어노테이션 중에 선택해서 기술하면 되는데 각각 장단점이 있지만, 어노테이션을 사용하는 쪽이 좀 더 쉽고 직관적이다.

## 4.1 @Entity

JPA를 사용해서 테이블과 매핑할 클래스는 `@Entity` 어노테이션을 필수로 붙여야 한다. `@Entity`가 붙은 클래스는 JPA가 관리하는 것으로, **엔티티**라 부른다.

| 속성 | 기능 | 기본값 |
| :--- | :--- | :--- |
| `name` | JPA에서 사용할 엔티티 이름을 지정한다. 보통 기본값인 클래스 이름을 사용한다. 만약 다른 패키지에 이름이 같은 엔티티 클래스가 있다면 이름을 지정해서 충돌하지 않도록 해야 한다. | 설정하지 않으면 클래스 이름을 그대로 사용한다. |

`@Entity` 적용 시 주의사항은 다음과 같다.

*   기본 생성자는 필수다 (파라미터가 없는 `public` 또는 `protected` 생성자).
*   `final` 클래스, `enum`, `interface`, `inner` 클래스에는 사용할 수 없다.
*   저장할 필드에 `final`을 사용하면 안 된다.

## 4.2 @Table

`@Table`은 엔티티와 매핑할 테이블을 지정한다. 생략하면 매핑한 엔티티 이름을 테이블 이름으로 사용한다.

| 속성 | 기능 | 기본값 |
| :--- | :--- | :--- |
| `name` | 매핑할 테이블 이름 | 엔티티 이름을 사용한다. |
| `catalog` | `catalog` 기능이 있는 데이터베이스에서 `catalog`를 매핑한다. | |
| `schema` | `schema` 기능이 있는 데이터베이스에서 `schema`를 매핑한다. | |
| `uniqueConstraints`(DDL) | DDL 생성 시에 유니크 제약조건을 만든다. 2개 이상의 복합 유니크 제약조건도 만들 수 있다. 참고로 이 기능은 스키마 자동 생성 기능을 사용해서 DDL을 만들 때만 사용된다. | |

## 4.3 다양한 매핑 사용

회원 관리 프로그램에 다음 요구사항이 추가되었다.

1.  회원은 일반 회원과 관리자로 구분해야 한다.
2.  회원 가입일과 수정일이 있어야 한다.
3.  회원을 설명할 수 있는 필드가 있어야 한다. 이 필드는 길이 제한이 없다.

**예제 4.1 회원 엔티티**

```java
@Entity
@Table(name = "MEMBER")
public class Member {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "NAME")
    private String username;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    @Lob
    private String description;
    
    // Getter, Setter
    // ...
}
```

```java
public enum RoleType {
    ADMIN, USER
}
```

1.  **`roleType`**: 자바의 `enum`을 사용해서 회원의 타입을 구분했다. 일반 회원은 `USER`, 관리자는 `ADMIN`이다. 이처럼 자바의 `enum`을 사용하려면 `@Enumerated` 어노테이션으로 매핑해야 한다.
2.  **`createdDate`, `lastModifiedDate`**: 자바의 날짜 타입은 `@Temporal`을 사용해서 매핑한다.
3.  **`description`**: 회원을 설명하는 필드는 길이 제한이 없다. 따라서 데이터베이스의 `VARCHAR` 타입 대신 `CLOB` 타입으로 저장해야 한다. `@Lob`을 사용하면 `CLOB`, `BLOB` 타입을 매핑할 수 있다.

## 4.4 데이터베이스 스키마 자동 생성

JPA는 데이터베이스 스키마를 자동으로 생성하는 기능을 지원한다. JPA는 매핑 정보와 데이터베이스 방언을 사용해서 데이터베이스 스키마를 생성한다.

스키마 자동 생성 기능을 사용해보려면, `persistence.xml`에 다음 속성을 추가한다.

```xml
<property name="hibernate.hbm2ddl.auto" value="create"/>
```

이 속성을 추가하면 **애플리케이션 실행 시점에 데이터베이스 테이블을 자동으로 생성**한다.

> **💡 참고**
>
> `hibernate.show_sql` 속성을 `true`로 설정하면 콘솔에 실행되는 테이블 생성 DDL을 출력할 수 있다.
>
> ```xml
> <property name="hibernate.show_sql" value="true"/>
> ```

콘솔에 다음 DDL이 출력되면서 실제 테이블이 생성된다.

**예제 4.2 DDL 콘솔 출력**

```sql
Hibernate:
    drop table Member if exists
Hibernate:
    create table Member (
       ID varchar(255) not null,
        NAME varchar(255),
        AGE integer,
        ROLE_TYPE varchar(255),
        CREATED_DATE timestamp,
        LAST_MODIFIED_DATE timestamp,
        DESCRIPTION clob,
        primary key (ID)
    )
```

실행된 결과를 보면 기존 테이블을 삭제하고 다시 생성한 것을 알 수 있다. 스키마 자동 생성 기능을 사용하면 애플리케이션 실행 시점에 데이터베이스 테이블이 자동으로 생성되므로 개발자가 테이블을 직접 생성하는 수고를 덜 수 있다.

스키마 자동 생성 기능이 만든 DDL은 운영 환경에서 사용할 만큼 완벽하지는 않으므로, **개발 환경에서 사용하거나 매핑을 어떻게 해야 하는지 참고하는 정도로만 사용하는 것이 좋다.**

**표 4.3 `hibernate.hbm2ddl.auto` 속성**

| 옵션 | 설명 |
| :--- | :--- |
| `create` | 기존 테이블을 삭제하고 새로 생성한다. (`DROP` + `CREATE`) |
| `create-drop` | `create` 속성에 추가로 애플리케이션을 종료할 때 생성한 DDL을 제거한다. (`DROP` + `CREATE` + `DROP`) |
| `update` | 데이터베이스 테이블과 엔티티 매핑정보를 비교해서 변경 사항만 수정한다. |
| `validate` | 데이터베이스 테이블과 엔티티 매핑정보를 비교해서 차이가 있으면 경고를 남기고 애플리케이션을 실행하지 않는다. 이 설정은 DDL을 수정하지 않는다. |
| `none` | 자동 생성 기능을 사용하지 않으려면 `hibernate.hbm2ddl.auto` 속성 자체를 삭제하거나 유효하지 않은 옵션 값(`none` 등)을 주면 된다. |

> **⚠️ 주의: HBM2DDL 주의사항**
>
> 운영 서버에서 `create`, `create-drop`, `update`처럼 DDL을 수정하는 옵션은 **절대 사용하면 안 된다.** 오직 개발 서버나 개발 단계에서만 사용해야 한다. 이 옵션들은 운영 중인 데이터베이스의 테이블이나 컬럼을 삭제할 수 있다.
>
> **개발 환경에 따른 추천 전략**은 다음과 같다.
> *   **개발 초기 단계**: `create` 또는 `update`
> *   **초기화 상태로 자동화된 테스트를 진행하는 개발자 환경과 CI 서버**: `create` 또는 `create-drop`
> *   **테스트 서버**: `update` 또는 `validate`
> *   **스테이징과 운영 서버**: `validate` 또는 `none`

> **💡 참고: JPA 표준 속성**
>
> JPA 2.1부터 스키마 자동 생성 기능을 표준으로 지원한다. 하지만 하이버네이트의 `hibernate.hbm2ddl.auto` 속성이 지원하는 `update`, `validate` 옵션은 지원하지 않는다.
>
> ```xml
> <property name="javax.persistence.schema-generation.database.action" value="drop-and-create"/>
> ```
>
> **지원 옵션**: `none`, `create`, `drop-and-create`, `drop`

> **💡 참고: 이름 매핑 전략 변경하기**
>
> 자바 언어는 관례상 `roleType`과 같이 카멜 표기법(Camel Case)을, 데이터베이스는 `role_type`과 같이 언더스코어 표기법(Snake Case)을 주로 사용한다.
>
> ```java
> @Column(name = "role_type")   // 언더스코어로 구분
> private RoleType roleType;   // 카멜 표기법으로 구분
> ```
>
> `hibernate.ejb.naming_strategy` 속성을 사용하면 이름 매핑 전략을 변경할 수 있다. 이 클래스는 테이블명이나 컬럼명이 생략되면 자바의 카멜 표기법을 테이블의 언더스코어 표기법으로 매핑한다.
>
> ```xml
> <property name="hibernate.ejb.naming_strategy"
>           value="org.hibernate.cfg.ImprovedNamingStrategy"/>
> ```

## 4.5 DDL 생성 기능

회원 이름은 필수로 입력되어야 하고 10자를 초과하면 안 된다는 제약조건이 추가되었다. 스키마 자동 생성하기를 통해 만들어지는 DDL에 이 제약조건을 추가해 보자.

**예제 4.4 추가 코드**

```java
@Entity
@Table(name = "MEMBER")
public class Member {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "NAME", nullable = false, length = 10) // 추가
    private String username;
    // ...
}
```

`@Column` 매핑정보의 `nullable` 속성 값을 `false`로 지정하면 자동 생성되는 DDL에 `not null` 제약조건을 추가할 수 있다. 그리고 `length` 속성 값을 사용하면 자동 생성되는 DDL에 문자의 크기를 지정할 수 있다.

**예제 4.5 생성된 DDL**

```sql
create table MEMBER (
    ID varchar(255) not null,
    NAME varchar(10) not null,
    ...
    primary key (ID)
)
```

이번에는 유니크 제약조건을 만들어 주는 `@Table`의 `uniqueConstraints` 속성을 알아본다.

**예제 4.6 유니크 제약조건**

```java
@Entity
@Table(name = "MEMBER", 
       uniqueConstraints = {@UniqueConstraint(
           name = "NAME_AGE_UNIQUE", 
           columnNames = {"NAME", "AGE"}
       )})
public class Member {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "NAME", nullable = false, length = 10)
    private String username;

    private Integer age;
    // ...
}
```

**예제 4.7 생성된 DDL**

```sql
ALTER TABLE MEMBER 
ADD CONSTRAINT NAME_AGE_UNIQUE UNIQUE (NAME, AGE)
```

예제 4.7의 생성된 DDL을 보면 유니크 제약조건이 추가되었다. 앞서 본 `@Column`의 `length`와 `nullable` 속성을 포함해서 **이런 기능들은 단지 DDL을 자동 생성할 때만 사용되고 JPA의 실행 로직에는 영향을 주지 않는다.** 따라서 스키마 자동 생성 기능을 사용하지 않고 직접 DDL을 만든다면 사용할 이유가 없다.

그럼에도 이 기능을 사용하면 애플리케이션 개발자가 엔티티만 보고도 손쉽게 다양한 제약 조건을 파악할 수 있는 장점이 있다.