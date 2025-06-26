# 03. 영속성 관리

JPA가 제공하는 기능은 크게 **엔티티와 테이블을 매핑하는 설계 부분**과 **매핑한 엔티티를 실제 사용하는 부분**으로 나눌 수 있다. 이 장에서는 매핑한 엔티티를 엔티티 매니저를 통해 어떻게 사용하는지 알아본다.

엔티티 매니저는 엔티티를 저장, 수정, 삭제, 조회하는 등 엔티티와 관련된 모든 일을 처리한다. 이름 그대로 엔티티를 관리하는 관리자이며, 개발자 입장에서는 엔티티를 저장하는 가상의 데이터베이스로 생각할 수 있다.

---

## 3.1 엔티티 매니저 팩토리와 엔티티 매니저

데이터베이스를 하나만 사용하는 애플리케이션은 일반적으로 `EntityManagerFactory`를 하나만 생성한다.

```java
// 공장 만들기, 생성 비용이 아주 비싸다.
EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook");
```

`Persistence.createEntityManagerFactory("jpabook")`를 호출하면 `META-INF/persistence.xml`의 설정 정보를 바탕으로 `EntityManagerFactory`를 생성한다.

> **예제 3.1 persistence.xml**
> ```xml
> <persistence-unit name="jpabook">
>     <properties>
>         <!-- 필수 속성 -->
>         <property name="javax.persistence.jdbc.driver" value="org.h2.Driver"/>
>         <property name="javax.persistence.jdbc.user" value="sa"/>
>         <property name="javax.persistence.jdbc.password" value=""/>
>         <property name="javax.persistence.jdbc.url" value="jdbc:h2:tcp://localhost/~/test"/>
>         ...
>     </properties>
> </persistence-unit>
> ```

`EntityManagerFactory`는 이름 그대로 엔티티 매니저를 만드는 공장인데, 이 공장을 만드는 비용은 상당히 크므로 **한 개만 만들어서 애플리케이션 전체에서 공유하도록 설계되어 있다.**

반면에 공장에서 엔티티 매니저를 생성하는 비용은 거의 들지 않는다.

-   **`EntityManagerFactory`**: 여러 스레드가 동시에 접근해도 안전하므로(Thread-safe) 서로 다른 스레드 간에 공유해도 된다.
-   **`EntityManager`**: 여러 스레드가 동시에 접근하면 동시성 문제가 발생하므로(Not Thread-safe) **스레드 간에 절대 공유하면 안 된다.**

> **그림 3.1 일반적인 웹 애플리케이션 구조**
>
> ![일반적인 웹 애플리케이션 구조](https://velog.velcdn.com/images/tudiiii/post/386be415-8c74-4757-b406-e59c9340f18f/image.png)

위 그림처럼 하나의 `EntityManagerFactory`에서 다수의 `EntityManager`를 생성해서 사용한다. 엔티티 매니저는 데이터베이스 연결이 꼭 필요한 시점까지 커넥션을 얻지 않으며, 보통 트랜잭션을 시작할 때 커넥션을 획득한다.

> 하이버네이트를 포함한 JPA 구현체들은 `EntityManagerFactory`를 생성할 때 커넥션 풀도 만든다. 이는 J2SE 환경에서 사용하는 방법이며, J2EE 환경(스프링 프레임워크 포함)에서는 해당 컨테이너가 제공하는 데이터소스를 사용한다.

---

## 3.2 영속성 컨텍스트란?

JPA를 이해하는 데 가장 중요한 용어는 **영속성 컨텍스트(Persistence Context)**다. 이는 **"엔티티를 영구 저장하는 환경"**이라는 뜻으로, 논리적인 개념이다.

엔티티 매니저로 엔티티를 저장하거나 조회하면, 엔티티 매니저는 영속성 컨텍스트에 엔티티를 보관하고 관리한다.

```java
em.persist(member);
```

`persist()` 메소드는 엔티티 매니저를 사용하여 회원 엔티티를 **영속성 컨텍스트에 저장**하는 것이다.

엔티티 매니저를 생성할 때 영속성 컨텍스트가 하나 만들어지며, 엔티티 매니저를 통해 영속성 컨텍스트에 접근하고 관리할 수 있다.

> *참고: 여러 엔티티 매니저가 같은 영속성 컨텍스트에 접근할 수도 있다. 하지만 지금은 '하나의 엔티티 매니저에 하나의 영속성 컨텍스트가 만들어진다'고 단순하게 생각하는 것이 좋다.*

---

## 3.3 엔티티의 생명주기

엔티티에는 4가지 생명주기 상태가 존재한다.

-   **비영속 (New/Transient)**: 영속성 컨텍스트와 전혀 관계가 없는 새로운 상태
-   **영속 (Managed)**: 영속성 컨텍스트에 의해 관리되는 상태
-   **준영속 (Detached)**: 영속성 컨텍스트에 저장되었다가 분리된 상태
-   **삭제 (Removed)**: 데이터베이스에서 삭제된 상태

> **그림 3.2 엔티티 생명주기**
>
> ![엔티티 생명주기](https://ultrakain.gitbooks.io/jpa/content/chapter3/images/JPA_3_2.png)

### 1. 비영속 (New/Transient)

엔티티 객체를 생성한 순수한 객체 상태다. 아직 데이터베이스에 저장되지 않았으며, 영속성 컨텍스트와는 아무런 관련이 없다.

> **그림 3.3 `em.persist()` 호출 전, 비영속 상태**
>
> ![비영속 상태](https://velog.velcdn.com/images/dongvelop/post/b1f30d4e-7032-4788-8375-8488026f77c3/image.png)

### 2. 영속 (Managed)

`em.persist(entity)`를 통해 엔티티를 영속성 컨텍스트에 저장한 상태다. **영속 상태는 영속성 컨텍스트에 의해 관리된다는 뜻**이다.

*참고: `em.find()`나 JPQL을 사용해서 조회한 엔티티도 영속성 컨텍스트가 관리하는 영속 상태다.*

> **그림 3.4 `em.persist()` 호출 후, 영속 상태**
>
> ![영속 상태](https://velog.velcdn.com/images/dongvelop/post/4488949b-f0de-441a-b3d9-e76214e24c7e/image.png)

### 3. 준영속 (Detached)

영속성 컨텍스트가 관리하던 영속 상태의 엔티티를 더 이상 관리하지 않으면 준영속 상태가 된다.
`em.detach(entity)`를 호출하여 특정 엔티티를 준영속 상태로 만들 수 있다.

또한 `em.close()`로 영속성 컨텍스트를 닫거나, `em.clear()`로 영속성 컨텍스트를 초기화해도 관리되던 모든 엔티티는 준영속 상태가 된다.

```java
// 회원 엔티티를 영속성 컨텍스트에서 분리하여 준영속 상태로 만든다.
em.detach(member);
```

### 4. 삭제 (Removed)

`em.remove(entity)`를 통해 엔티티를 영속성 컨텍스트와 데이터베이스에서 모두 삭제한 상태다.

```java
// 객체를 삭제한 상태 (삭제)
em.remove(member);
```