## 3.4 영속성 컨텍스트의 특징
영속성 컨텍스트의 특징은 다음과 같다.

#### **1. 영속성 컨텍스트와 식별자 값**
영속성 컨텍스트는 엔티티를 식별자 값(@Id로 테이블의 기본 키와 매핑한 값)으로 구분한다.
따라서 **영속 상태는 식별자 값이 반드시 있어야 한다.** 식별자 값이 없으면 예외가 발생한다.

#### **2. 영속성 컨텍스트와 데이터베이스 저장**
JPA는 보통 트랜잭션을 커밋하는 순간 영속성 컨텍스트에 새로 저장된 엔티티를 데이터베이스에 반영하는데 이것을 `플러시`라 한다.

#### **3. 영속성 컨텍스트가 엔티티를 관리하면 다음과 같은 장점이 있다.**
* 1차 캐시
* 동일성 보장
* 트랜잭션을 지원하는 쓰기 지연
* 변경 감지
* 지연 로딩

지금부터 영속성 컨텍스트가 왜 필요하고 어떤 이점이 있는지 엔티티를 CRUD하면서 그 이유를 알아보자.

## 3.4.1 엔티티 조회
영속성 컨텍스트는 내부에 캐시를 가지고 있는데 이것을 1차 캐시라 한다. 영속 상태의 엔티티는 모두 이곳에 저장된다.
쉽게 이야기하면 영속성 컨텍스트 내부에 Map이 하나 있는데 키는 @Id로 매핑한 식별자고 값은 엔티티 인스턴스다.
```java
// 엔티티를 생성한 상태(비영속)
Member member = new Member();
member.setId("member1");
member.setUsername("회원1");

//엔티티를 영속
em.persist(member);
```
이 코드를 실행하면 아래 그림처럼 1차 캐시에 회원 엔티티를 저장한다. 회원 엔티티는 아직 데이터베이스에 저장되지 않았다.

> **그림 3.5 영속성 컨텍스트 1차 캐시**
> 
> ![영속성 컨텍스트 1차 캐시](https://velog.velcdn.com/images%2Fseho100%2Fpost%2F422af98f-9d49-4d28-b06f-0624f9d12560%2Fimage.png)

1차 캐시의 키는 식별자 값이다. 그리고 식별자 값은 데이터베이스 기본 키와 매핑되어 있다. 따라서 영속성 컨텍스트에 데이터를 저장하고 조회하는 모든 기준은 데이터베이스 기본 키 값이다.

이번에는 엔티티를 조회해보자.
```java
Member member = em.find(Member.class, "member1");
```
find() 메소드를 보면 첫 번쨰 파라미터는 엔티티 클래스의 타입이고, 두 번쨰는 조회할 엔티티의 식별자 값이다.
```java
//EntityManager.find() 메소드 정의
public <T> T find(Class<T> entityClass, Object primaryKey);
```
em.find()를 호출하면 먼저 1차 캐시에서 엔티티를 찾고 만약 찾는 엔티티가 1차 캐시에 없으면 데이터베이스에서 조회한다.

### 1차 캐시에서 조회
아래 그림 처럼 em.find()를 호출하면 우선 1차 캐시에서 식별자 값으로 엔티티를 찾는다.
만약 찾는 엔티티가 있으면 데이터베이스를 조회하지 않고 메모리에 있는 1차 캐시에서 엔티티를 조회한다.

>**그림 3.6 1차 캐시에서 조회**
> 
> ![1차 캐시에서 조회](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdna%2FcxCtEP%2FbtrsJM118zT%2FAAAAAAAAAAAAAAAAAAAAALsdiAG140Q4XMjuP6g11gFPRhz2dALFcgx8PZ1OvPby%2Fimg.png%3Fcredential%3DyqXZFxpELC7KVnFOS48ylbz2pIh7yKj8%26expires%3D1751295599%26allow_ip%3D%26allow_referer%3D%26signature%3Dd7QXOX3ZF1bo9kC4NTOd6LnRb6s%253D)

다음 코드는 1차 캐시에 있는 엔티티를 조회한다.
```java
Member member = new Member();
member.setId("member1");
member.setUsername("회원1");

//1차 캐시에 저장됨
em.persist(member);

//1차 캐시에서 조회
Member findMember = em.find(Member.class, "member1");
```
### 데이터베이스에서 조회
만약 em.find()를 호출했는데 엔티티가 1차 캐시에 없으면 엔티티 매니저는 데이터베이스에서 엔티티를 조회해서 엔티티를 생성한다.
그리고 1차 캐시에 저장한 후에 영속 상태의 엔티티를 반환한다.
```java
Member findMember2 = em.find(Member.class, "member2");
```
>**그림 3.7 1차 캐시에 없어 데이터베이스 조회**
> 
> ![1차 캐시에 없어 데이터베이스 조회](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdna%2FdHEMbF%2FbtryP16ysdR%2FAAAAAAAAAAAAAAAAAAAAAJUB3moXct1XT3UK5HJnE0f58MkXkXe52qBlrUr4L5aU%2Fimg.png%3Fcredential%3DyqXZFxpELC7KVnFOS48ylbz2pIh7yKj8%26expires%3D1751295599%26allow_ip%3D%26allow_referer%3D%26signature%3D2nAtnuFJtYRW31kSNXgmG2kp%252Bis%253D)

1. em.find(Member.class, "member2")를 실행한다.
2. member2가 1차 캐시에 없으므로 데이터베이스에서 조회한다.
3. 조회한 데이터로 member2 엔티티를 생성해서 1차 캐시에 저장한다.(영속 상태)
4. 조회한 엔티티를 반환한다.

member1, member2 엔티티 인스턴스는 1차 캐시에 있다. 따라서 이 엔티티들을 조회하면 메모리에 있는 1차 캐시에서 바로 불러온다. 따라서 성능상 이점을 누릴 수 있다.

### 영속 엔티티 동일성 보장
```java
Member a = em.find(Member.class, "member1");
Member b = em.find(Member.class, "member1");

System.out.println(a == b);  // 동일성 비교
```
영속성 컨텍스트는 1차 캐시에 있는 같은 엔티티 인스턴스를 반환한다. 따라서 둘은 같은 인스턴스고 결과는 true가 된다. 따라서
**영속성 컨텍스트는 성능상 이점과 엔티티의 동일성을 보장한다.**

> **참고**
>
> * 동일성: 실제 인스턴스가 같다. 따라서 참조 값을 비교하는 == 비교의 값이 같다.
> * 동등성: 실제 인스턴스는 다를 수 있지만 인스턴스가 가지고 있는 값이 같다. 자바에서 동등성 비교는 equals() 메소드를 사용한다.

> **참고**
> 
> JPA는 1차 캐시를 통해 반복 가능한 읽기 등급의 트랜잭션 격리 수준을 데이터베이스가 아닌 애플리케이션 차원에서 제공한다는 장점이 있다.
> 트랜잭션 격리 수준은 16.1절에 알아본다.

## 3.4.2 엔티티 등록
엔티티 매니저를 사용해서 엔티티를 영속성 컨텍스트에 등록해보자.
```java
EntityManger em = emf.createEntityManager();
EntityTransaction transaction = em.getTransaction();
// 엔티티 매니저는 데이터 변경 시 트랜잭션을 시작해야 한다.
transaction.begin();    // 트랜잭션 시작

em.persist(memberA);
em.persist(memberB);

// 커밋하는 순간 데이터베이스에 INSERT SQL을 보낸다.
transaction.commit();   // 트랜잭션 커밋
```
엔티티 매니저는 트랜잭션을 커밋하기 직전까지 데이터베이스에 엔티티를 저장하지 않고 내부 쿼리 저장소에 INSERT SQL을 차곡차곡 모아둔다.
그리고 트랜잭션을 커밋할 떄 모아둔 쿼리를 데이터베이스에 보내는데 이것을 트랜잭션을 지원하는 `쓰기 지연` 이라 한다.
> 그림 3.8 쓰기 지연, 회원 A 영속
> 
> ![쓰기 지연, 회원 A 영속](https://velog.velcdn.com/post-images%2Fconatuseus%2Fd4a2fb30-d09b-11e9-a657-a958e5af4073%2Fimage.png)

위 그림을 보면 먼저 회원 A를 영속화했다. 영속성 컨텍스트는 1차 캐시에 회원 엔티티를 저장하면서 동시에 회원 엔티티 정보로 등록 쿼리를 만든다.
그리고 만들어진 등록 쿼리를 쓰기 지연 SQL 저장소에 보관한다.

> 그림 3.9 쓰기 지연, 회원 B 영속
> 
> ![쓰기 지연, 회원 B 영속](https://velog.velcdn.com/post-images%2Fconatuseus%2F51c8cae0-d09c-11e9-b275-49c1db32880d%2Fimage.png)

위 그림을 보면 다음으로 회원 B를 영속화했다. 마찬가지로 회원 엔티티 정보로 등록 쿼리를 생성해서 쓰기 지연 SQL 저장소에 보관한다.
현재 쓰기 지연 SQL 저장소에는 회원 A, B에 대한 등록 쿼리가 있다.

> 그림 3.10 쓰기 지연, 커밋
> 
> ![쓰기 지연, 커밋](https://velog.velcdn.com/post-images%2Fconatuseus%2Feb6c9c30-d09c-11e9-b0db-1597a34a142f%2Fimage.png)

위 그림을 보면 마지막으로 트랜잭션을 커밋했다. 트랜잭션을 커밋하면 엔티티 매니저는 우선 영속성 컨텍스트를 플러시한다. 플러시는 영속성 컨텍스트의 변경 내용을
데이터베이스에 동기화 하는 작업인데 이떄 등록, 수정, 삭제한 엔티티를 데이터베이스에 반영한다.