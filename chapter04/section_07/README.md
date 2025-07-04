## 4.7 필드와 컬럼 매핑: 레퍼런스

### 필드와 컬럼 매핑

| 매핑 어노테이션 | 설명 |
|:---:|:---|
| `@Column` | 컬럼을 매핑한다. |
| `@Enumerated` | 자바의 enum 타입을 매핑한다. |
| `@Temporal` | 날짜 타입을 매핑한다. |
| `@Lob` | BLOB, CLOB 타입을 매핑한다. |
| `@Transient` | 특정 필드를 데이터베이스에 매핑하지 않는다. |

### 기타

| 매핑 어노테이션 | 설명 |
|:---:|:---|
| `@Access` | JPA가 엔티티에 접근하는 방식을 지정한다. |

---

### 4.7.1 @Column

`@Column`은 객체 필드를 테이블 컬럼에 매핑한다. 가장 많이 사용되며 기능도 많다. 속성 중에는 `name`, `nullable`이 주로 사용되고 나머지는 잘 사용되지 않는 편이다.

`insertable`, `updatable` 속성은 데이터베이스에 저장된 정보를 읽기만 하고 실수로 변경하는 것을 방지하고 싶을 때 사용한다.

> **표 4.10 @Column 속성 정리**

| 속성 | 기능 | 기본값 |
|:---:|:---|:---|
| `name` | 필드와 매핑할 테이블의 컬럼 이름 | 객체의 필드 이름 |
| `insertable`<br/>(거의 사용하지 않음) | 엔티티 저장 시 이 필드도 같이 저장한다. `false`로 설정하면 이 필드는 데이터베이스에 저장하지 않는다. `false` 옵션은 읽기 전용일 때 사용한다. | `true` |
| `updatable`<br/>(거의 사용하지 않음) | 엔티티 수정 시 이 필드도 같이 수정한다. `false`로 설정하면 데이터베이스에 수정하지 않는다. `false` 옵션은 읽기 전용일 때 사용한다. | `true` |
| `table`<br/>(거의 사용하지 않음) | 하나의 엔티티를 두 개 이상의 테이블에 매핑할 때 사용한다. 지정한 필드를 다른 테이블에 매핑할 수 있다. | 현재 클래스가 매핑된 테이블 |
| `nullable` (DDL) | null 값의 허용 여부를 설정한다. `false`로 설정하면 DDL 생성 시 `not null` 제약조건이 붙는다. | `true` |
| `unique` (DDL) | `@Table`의 `uniqueConstraints`와 같지만, 한 컬럼에 간단히 유니크 제약조건을 걸 때 사용한다. 두 컬럼 이상을 사용해서 유니크 제약조건을 적용하려면 클래스 레벨에서 `@Table.uniqueConstraints`를 사용해야 한다. | |
| `columnDefinition` (DDL) | 데이터베이스 컬럼 정보를 직접 지정할 수 있다. | 필드의 자바 타입과 방언 정보를 사용해서 적절한 컬럼 타입을 생성한다. |
| `length` (DDL) | 문자 길이 제약조건, String 타입에만 사용한다. | 255 |
| `precision`, `scale` (DDL) | `BigDecimal` 타입에서 사용한다(BigInteger도 사용 가능). `precision`은 소수점을 포함한 전체 자릿수, `scale`은 소수의 자릿수다. `double`, `float` 타입에는 적용되지 않는다. 아주 큰 숫자나 정밀한 소수를 다룰 때만 사용한다. | `precision=19`, `scale=2` |

DDL 생성 속성에 따라 생성되는 DDL은 다음과 같다.

#### 1. nullable (DDL 생성 기능)

```java
@Column(nullable = false)
private String data;
```
**생성된 DDL**
```sql
data VARCHAR(255) NOT NULL
```

#### 2. unique (DDL 생성 기능)

```java
@Column(unique = true)
private String username;
```
**생성된 DDL**
```sql
alter table Tablename add constraint UK_Xxx unique (username)
```

#### 3. columnDefinition (DDL 생성 기능)

```java
@Column(columnDefinition = "varchar(100) default 'EMPTY'")
private String data;
```
**생성된 DDL**
```sql
data VARCHAR(100) DEFAULT 'EMPTY'
```

#### 4. length (DDL 생성 기능)

```java
@Column(length = 100)
private String data;
```
**생성된 DDL**
```sql
data VARCHAR(100)
```

#### 5. precision, scale (DDL 생성 기능)

```java
@Column(precision = 10, scale = 2)
private BigDecimal price;
```
**생성된 DDL**
```sql
cal numeric(10, 2) -- H2, PostgreSQL
cal number(10, 2)  -- Oracle
cal decimal(10, 2) -- MySQL
```

> **💡 참고: @Column 생략**
>
> `@Column`을 생략하면 속성의 기본값이 적용된다. 자바 기본 타입(primitive type)일 때는 `nullable` 속성에 예외가 있다.
>
> ```java
> int data1;    // @Column 생략, 자바 기본 타입
>
> Integer data2; // @Column 생략, 객체 타입
>
> @Column
> int data3; // @Column 사용, 자바 기본 타입
> ```
>
> **생성된 DDL**
> ```sql
> data1 integer not null
> data2 integer
> data3 integer
> ```
>
> 자바 기본 타입에 `@Column`을 사용하지 않으면 `not null`이 되므로, 의도를 명확히 하려면 `nullable = false`를 지정하는 것이 안전하다.

### 4.7.2 @Enumerated

> **표 4.11 @Enumerated 속성 정리**

| 속성 | 기능 | 기본값 |
|:---|:---|:---|
| `value` | `EnumType.ORDINAL`: enum 순서를 데이터베이스에 저장<br/>`EnumType.STRING`: enum 이름을 데이터베이스에 저장 | `EnumType.ORDINAL` |

#### @Enumerated 사용 예

```java
public enum RoleType {
    USER, ADMIN
}
```

다음은 enum 이름을 데이터베이스에 저장하는 예제다.

```java
@Enumerated(EnumType.STRING)
private RoleType roleType;
```

enum은 다음과 같이 사용한다.
```java
member.setRoleType(RoleType.ADMIN);
```

- **`EnumType.ORDINAL`**: enum에 정의된 순서대로 `USER`는 0, `ADMIN`은 1 값이 데이터베이스에 저장된다.
    - **장점**: 데이터베이스에 저장되는 데이터 크기가 작다.
    - **단점**: 이미 저장된 enum의 순서를 변경할 수 없다.
- **`EnumType.STRING`**: enum 이름 그대로 `USER`는 "USER", `ADMIN`은 "ADMIN"이라는 문자로 데이터베이스에 저장된다.
    - **장점**: 저장된 enum의 순서가 바뀌거나 enum이 추가되어도 안전하다.
    - **단점**: 데이터베이스에 저장되는 데이터 크기가 ORDINAL에 비해 크다.

> **⚠️ 주의: 기본값인 ORDINAL 사용**
>
> `ADMIN`(0번), `USER`(1번) 사이에 새로운 역할 `GUEST`가 추가되어 `ADMIN`(0번), `GUEST`(1번), `USER`(2번)으로 순서가 변경되면, 새로 저장되는 `USER`는 2로 저장되지만 기존에 1로 저장된 `USER` 데이터는 `GUEST`로 인식되는 심각한 문제가 발생한다. **따라서 이런 문제가 없는 `EnumType.STRING`을 사용해야 한다.**

### 4.7.3 @Temporal

> **표 4.12 @Temporal 속성 정리**

| 속성 | 기능 | 기본값 |
|:---|:---|:---|
| `value` | `TemporalType.DATE`: 날짜, 데이터베이스 `date` 타입과 매핑 (예: 2023-10-27)<br/>`TemporalType.TIME`: 시간, 데이터베이스 `time` 타입과 매핑 (예: 11:22:33)<br/>`TemporalType.TIMESTAMP`: 날짜와 시간, 데이터베이스 `timestamp` 타입과 매핑 (예: 2023-10-27 11:22:33) | `TemporalType`은 필수로 지정해야 한다. |

자바의 `Date` 타입에는 년월일 시분초가 있지만, 데이터베이스에는 `date`(날짜), `time`(시간), `timestamp`(날짜와 시간) 세 가지 타입이 별도로 존재한다.

`@Temporal`을 생략하면 자바의 `Date`와 가장 유사한 `timestamp`로 정의된다. 데이터베이스에 따라 `timestamp` 대신 `datetime`을 예약어로 사용하기도 하지만, 데이터베이스 방언(Dialect) 덕분에 애플리케이션 코드는 변경하지 않아도 된다.

- **datetime**: MySQL
- **timestamp**: H2, PostgreSQL, Oracle

### 4.7.4 @Lob

데이터베이스의 BLOB, CLOB 타입과 매핑한다. `@Lob`에는 별도 속성이 없으며, 필드 타입에 따라 매핑 타입이 결정된다.

- **CLOB**: `String`, `char[]`, `java.sql.Clob`
- **BLOB**: `byte[]`, `java.sql.Blob`

### 4.7.5 @Transient

이 어노테이션이 붙은 필드는 데이터베이스에 저장하거나 조회하지 않는다. 객체에 임시로 값을 보관할 때 사용한다.

```java
@Transient
private Integer temp;
```

### 4.7.6 @Access

JPA가 엔티티 데이터에 접근하는 방식을 지정한다.

- **필드 접근(`AccessType.FIELD`)**: 필드에 직접 접근한다. 필드 접근 권한이 `private`이어도 접근 가능하다.
- **프로퍼티 접근(`AccessType.PROPERTY`)**: 접근자(Getter)를 사용한다.

`@Access`를 설정하지 않으면 `@Id`의 위치를 기준으로 접근 방식이 자동으로 설정된다.

**필드 접근 방식**

`@Id`가 필드에 있으므로 기본적으로 필드 접근 방식을 사용한다. `@Access(AccessType.FIELD)`를 명시한 것과 같으므로 생략 가능하다.

```java
@Entity
public class Member {
    
    @Id
    private String id;
    
    private String data1;
    private String data2;
    // ...
}
```

**프로퍼티 접근 방식**

`@Id`가 Getter 메서드에 있으므로 기본적으로 프로퍼티 접근 방식을 사용한다. 따라서 `@Access(AccessType.PROPERTY)`는 생략할 수 있다.

```java
@Entity
public class Member {
     
    private String id;
    private String data1;
    private String data2;
    
    @Id
    public String getId() {
        return id;
    }

    // @Column 등 다른 매핑 어노테이션도 Getter에 설정
    @Column 
    public String getData1() {
        return data1;
    }
    
    // 매핑 어노테이션이 없으면 매핑되지 않음
    public String getData2() {
        return data2;
    }
}
```

**필드와 프로퍼티 접근 함께 사용**

`@Id`가 필드에 있으므로 기본은 필드 접근 방식이지만, `getFullName()` 메서드에 `@Access(AccessType.PROPERTY)`를 지정하여 이 메서드만 프로퍼티 접근 방식을 사용하도록 할 수 있다. 이 경우, 엔티티를 저장하면 회원 테이블의 `FULLNAME` 컬럼에 `firstName`과 `lastName`을 조합한 결과가 저장된다.

```java
@Entity
public class Member {
    
    @Id
    private String id; 
    
    @Transient
    private String firstName;
    
    @Transient
    private String lastName;
    
    @Access(AccessType.PROPERTY)
    public String getFullName() {
        return firstName + " " + lastName;
    }
    // ...
}
```