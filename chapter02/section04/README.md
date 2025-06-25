## 2.4 객체 매핑 시작

먼저 예제 2.4의 SQL을 실행해서 예제에서 사용할 회원 테이블을 만든다.

> **예제 2.4 회원 테이블 생성 SQL**
>
> ```sql
> CREATE TABLE MEMBER (
>     ID VARCHAR(255) NOT NULL,    -- 아이디(기본 키)
>     NAME VARCHAR(255),           -- 이름
>     AGE INTEGER,                 -- 나이
>     PRIMARY KEY (ID)
> );
> ```

다음으로 예제 2.5와 같이 애플리케이션에서 사용할 회원 클래스를 만든다.

> **예제 2.5 회원 클래스(Member.java)**
>
> ```java
> public class Member {
>     private String id;
>     private String username;
>     private Integer age;
>
>     // Getters and Setters
>     public String getId() {
>         return id;
>     }
>
>     public void setId(String id) {
>         this.id = id;
>     }
>
>     public String getUsername() {
>         return username;
>     }
>
>     public void setUsername(String username) {
>         this.username = username;
>     }
>
>     public Integer getAge() {
>         return age;
>     }
>
>     public void setAge(Integer age) {
>         this.age = age;
>     }
> }
> ```

JPA를 사용하려면 가장 먼저 회원 클래스와 회원 테이블을 매핑해야 한다. 다음 표의 매핑 정보를 참고하여 둘을 비교하며 실제 매핑을 시작한다.

| 매핑 정보 | 회원 객체 (`Member`) | 회원 테이블 (`MEMBER`) |
| :-------- | :------------------- | :--------------------- |
| 클래스와 테이블 | `Member` | `MEMBER` |
| 기본 키 | `id` | `ID` |
| 필드와 컬럼 | `username` | `NAME` |
| 필드와 컬럼 | `age` | `AGE` |

예제 2.6과 같이 회원 클래스에 JPA가 제공하는 매핑 어노테이션을 추가한다.

> **예제 2.6 매핑 정보가 포함된 회원 클래스**
>
> ```java
> @Entity
> @Table(name="MEMBER")
> public class Member {
>
>     @Id
>     @Column(name = "ID")
>     private String id;
>
>     @Column(name = "NAME")
>     private String username;
>
>     // 매핑 정보가 없는 필드
>     private Integer age;
>
>     // ... Getters and Setters ...
> }
> ```

> **그림 2.10 클래스와 테이블 매핑**
>
> ![클래스와 테이블 매핑](https://lh3.googleusercontent.com/pw/ACtC-3dR9wnUXYz7fApGFwk79lfrcIlAwFvraFhEmDrNzTNZi3hvyrp2J0xzNnhUO3Aqn2QTXXB7Ftgl4ebAW1eL2FayGP2UBKxWsjRt80jji1CwHaLuRbmDw_6GMAUQwKy2cCY0oFkF6cAdyrKQ6YWZthPQMg=w1053-h243-no?authuser=0)

JPA는 매핑 어노테이션을 분석해서 어떤 객체가 어떤 테이블과 관계가 있는지 알아낸다.

#### **1. `@Entity`**

이 클래스를 테이블과 매핑한다고 JPA에게 알려주는 역할을 한다. 이렇게 `@Entity`가 사용된 클래스를 **엔티티 클래스(Entity Class)**라 한다.

#### **2. `@Table`**

엔티티 클래스에 매핑할 테이블 정보를 알려준다. 여기서는 `name` 속성을 사용해서 `Member` 엔티티를 `MEMBER` 테이블에 매핑했다. 이 어노테이션을 생략하면 클래스 이름(정확히는 엔티티 이름)을 테이블 이름으로 매핑한다.

#### **3. `@Id`**

엔티티 클래스의 필드를 테이블의 기본 키(Primary Key)에 매핑한다. 여기서는 `Member` 엔티티의 `id` 필드를 `MEMBER` 테이블의 `ID` 기본 키 컬럼에 매핑했다. 이렇게 `@Id`가 사용된 필드를 **식별자 필드(Identifier Field)**라 한다.

#### **4. `@Column`**

필드를 컬럼에 매핑한다. 여기서는 `name` 속성을 사용해서 `Member` 엔티티의 `username` 필드를 `MEMBER` 테이블의 `NAME` 컬럼에 매핑했다.

#### **5. 매핑 정보가 없는 필드**

`age` 필드에는 매핑 어노테이션이 없다. 이렇게 매핑 어노테이션을 생략하면 필드명을 사용해서 컬럼명으로 매핑한다. (예: `age` -> `AGE`) 대소문자를 구분하는 데이터베이스를 사용한다면 `@Column(name="AGE")`처럼 명시적으로 매핑하는 것이 안전하다.

---

매핑 정보 덕분에 JPA는 어떤 엔티티를 어떤 테이블에 저장해야 하는지 알 수 있다.

다음으로 JPA를 실행하기 위한 기본 설정 파일인 `persistence.xml`을 알아본다.

> **참고**: JPA 어노테이션의 패키지는 `javax.persistence`이다.

[다음: 2.5 persistence.xml 설정](chapter02/section05/README.md)