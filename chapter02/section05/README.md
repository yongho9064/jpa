## persistence.xml 설정

JPA는 `persistence.xml`을 사용해서 필요한 설정 정보를 관리한다. 이 설정 파일이 `META-INF/persistence.xml` 클래스 패스 경로에 있으면 별도의 설정 없이 JPA가 인식할 수 있다.

### 예제: JPA 환경설정 파일 (persistence.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="http://xmlns.jcp.org/xml/ns/persistence" version="2.1">

    <persistence-unit name="jpabook">
        <properties>
            <!-- 필수 속성 -->
            <property name="javax.persistence.jdbc.driver" value="org.h2.Driver"/>
            <property name="javax.persistence.jdbc.user" value="sa"/>
            <property name="javax.persistence.jdbc.password" value=""/>
            <property name="javax.persistence.jdbc.url" value="jdbc:h2:tcp://localhost/~/test"/>
            <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>

            <!-- 옵션 -->
            <property name="hibernate.show_sql" value="true"/>
            <property name="hibernate.format_sql" value="true"/>
            <property name="hibernate.use_sql_comments" value="true"/>
            <property name="hibernate.id.new_generator_mappings" value="true"/>
        </properties>
    </persistence-unit>

</persistence>
```

### persistence.xml 설정 분석

#### `<persistence>` 루트 태그
```xml
<persistence xmlns="http://xmlns.jcp.org/xml/ns/persistence" version="2.1">
```
설정 파일은 `<persistence>` 태그로 시작하며, 이곳에 XML 네임스페이스(`xmlns`)와 사용할 JPA 버전(`version`)을 지정한다.

#### `<persistence-unit>` 영속성 유닛
```xml
<persistence-unit name="jpabook" >
```
JPA 설정은 **영속성 유닛(Persistence Unit)** 단위로 이루어진다. 일반적으로 연결할 데이터베이스당 하나의 영속성 유닛을 등록하며, `name` 속성을 사용해 고유한 이름을 부여해야 한다. (예: `jpabook`)

#### `<properties>` 속성 설정
```xml
<properties>
    <!-- 필수 속성 -->
    <property name="javax.persistence.jdbc.driver" value="org.h2.Driver"/>
    ...
</properties>
```
데이터베이스 접속 정보와 JPA 및 구현체에 필요한 여러 옵션을 설정한다.

**1. JPA 표준 속성**
`javax.persistence`로 시작하는 속성은 특정 구현체에 종속되지 않는 JPA 표준 명세다.
*   **`javax.persistence.jdbc.driver`**: JDBC 드라이버
*   **`javax.persistence.jdbc.user`**: 데이터베이스 접속 아이디
*   **`javax.persistence.jdbc.password`**: 데이터베이스 접속 비밀번호
*   **`javax.persistence.jdbc.url`**: 데이터베이스 접속 URL

**2. 하이버네이트 전용 속성**
`hibernate`로 시작하는 속성은 하이버네이트에서만 사용할 수 있는 전용 속성이다.
*   **`hibernate.dialect`**: 데이터베이스 방언(Dialect) 설정

데이터베이스 연결 설정에서 가장 중요한 속성 중 하나는 바로 데이터베이스 방언을 설정하는 `hibernate.dialect`다.

---

## 데이터베이스 방언 (Database Dialect)

JPA는 특정 데이터베이스에 종속되지 않는 기술이므로, 다른 데이터베이스로 손쉽게 교체할 수 있다. 하지만 각 데이터베이스가 제공하는 SQL 문법과 함수는 조금씩 다르다.

예를 들어 데이터베이스마다 다음과 같은 차이가 있다.
*   **데이터 타입**: 가변 문자 타입으로 MySQL은 `VARCHAR`, 오라클은 `VARCHAR2`를 사용한다.
*   **다른 함수명**: 문자열을 자르는 함수로 SQL 표준은 `SUBSTRING()`이지만, 오라클은 `SUBSTR()`을 사용한다.
*   **페이징 처리**: MySQL은 `LIMIT`를 사용하지만, 오라클은 `ROWNUM`을 사용한다.

이처럼 SQL 표준을 지키지 않거나 특정 데이터베이스만의 고유한 기능을 JPA에서는 **방언(Dialect)**이라 부른다.

하이버네이트를 포함한 대부분의 JPA 구현체는 이런 문제를 해결하기 위해 다양한 데이터베이스 방언 클래스를 제공한다. 개발자는 JPA 표준 문법에 맞춰 코드를 작성하면, 특정 데이터베이스에 의존적인 SQL 처리는 데이터베이스 방언이 대신해준다.

따라서 데이터베이스가 변경되어도 애플리케이션 코드를 수정할 필요 없이, 아래 그림과 같이 **데이터베이스 방언만 교체**하면 된다.

> **그림 2.11 방언**
>
> ![데이터베이스 방언의 역할](https://blog.kakaocdn.net/dn/yG7yK/btsxkEL8YMu/SwFrxSmQyid1yynSRlHzH1/img.png)