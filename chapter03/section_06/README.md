## 3.6 준영속

영속성 컨텍스트가 관리하는 영속 상태의 엔티티가 영속성 컨텍스트에서 분리된 것을 **준영속(detached)** 상태라 한다. 따라서 **준영속 상태의 엔티티는 영속성 컨텍스트가 제공하는 기능을 사용할 수 없다.**

영속 상태의 엔티티를 준영속 상태로 만드는 방법은 크게 3가지다.

1.  `em.detach(entity)`: 특정 엔티티만 준영속 상태로 전환한다.
2.  `em.clear()`: 영속성 컨텍스트를 완전히 초기화한다.
3.  `em.close()`: 영속성 컨텍스트를 종료한다.

### 3.6.1 엔티티를 준영속 상태로 전환: detach()

`em.detach()` 메소드는 특정 엔티티를 준영속 상태로 만든다.

**예제 3.8 detach() 테스트 코드**
```java
public void testDetached() {
    // ...
    // 회원 엔티티 생성, 비영속 상태
    Member member = new Member();
    member.setId("memberA");
    member.setUsername("회원A");
    
    // 회원 엔티티 영속 상태
    em.persist(member);
    
    // 회원 엔티티를 영속성 컨텍스트에서 분리, 준영속 상태
    em.detach(member);
    
    transaction.commit();   // 트랜잭션 커밋
}
```

`em.detach(member)`를 호출하면 영속성 컨텍스트에게 더는 해당 엔티티를 관리하지 말라고 지시하는 것이다. 이 메소드를 호출하는 순간 1차 캐시부터 쓰기 지연 SQL 저장소까지, 해당 엔티티를 관리하기 위한 모든 정보가 제거된다.

*그림 3.12 detach 실행 전*
![detach 실행 전](https://blog.kakaocdn.net/dna/bNWKSf/btsdHQcrztt/AAAAAAAAAAAAAAAAAAAAABqE8rbosdxctU0Y9eSrLBSNgKJYUNSUeJWaWatwxzYn/img.png?credential=yqXZFxpELC7KVnFOS48ylbz2pIh7yKj8&expires=1751295599&allow_ip=&allow_referer=&signature=%2F%2BIXidrDuixPbadiu0rNUo%2BkCIw%3D)

*그림 3.13 detach 실행 후*
![detach 실행 후](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdna%2FbF5jRY%2FbtsdGkL8t76%2FAAAAAAAAAAAAAAAAAAAAABiIkHFk8nM6h5JhLoiuXMA5dl2m4JXzFXIo_7I9ve09%2Fimg.png%3Fcredential%3DyqXZFxpELC7KVnFOS48ylbz2pIh7yKj8%26expires%3D1751295599%26allow_ip%3D%26allow_referer%3D%26signature%3DXZh7ZM%252BUNYlQTnRcJ3sHnCQzn1c%253D)

이처럼 **영속 상태였다가 더는 영속성 컨텍스트가 관리하지 않는 상태를 준영속 상태**라 한다. 준영속 상태이므로 영속성 컨텍스트가 지원하는 어떤 기능도 동작하지 않는다. 심지어 쓰기 지연 SQL 저장소의 `INSERT` SQL도 제거되어 데이터베이스에 저장되지도 않는다.

### 3.6.2 영속성 컨텍스트 초기화: clear()

`em.clear()`는 영속성 컨텍스트를 초기화해서 해당 영속성 컨텍스트의 모든 엔티티를 준영속 상태로 만든다.

**예제 3.9 영속성 컨텍스트 초기화**
```java
// 엔티티 조회, 영속 상태
Member member = em.find(Member.class, "memberA");

em.clear(); // 영속성 컨텍스트 초기화

// 준영속 상태
member.setUsername("changeName");
```

*그림 3.14 영속성 컨텍스트 초기화 전*

![영속성 컨텍스트 초기화 전](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdna%2FbemQQF%2FbtsdHQp3OtV%2FAAAAAAAAAAAAAAAAAAAAADrzRvwpObZV_IoeOZE4eQq4a7fjaondXwrKw3fgIQa2%2Fimg.png%3Fcredential%3DyqXZFxpELC7KVnFOS48ylbz2pIh7yKj8%26expires%3D1751295599%26allow_ip%3D%26allow_referer%3D%26signature%3DSa4OxuaLYoHbxr2P1kbka4j5vP4%253D)

*그림 3.15 영속성 컨텍스트 초기화 후*

![영속성 컨텍스트 초기화 후](https://velog.velcdn.com/images/dongvelop/post/86e36ee4-273e-4853-8a46-64b444b69ce1/image.png)

위 그림을 비교해서 보면, 영속성 컨텍스트에 있는 모든 것이 초기화되었다. 이는 영속성 컨텍스트를 제거하고 새로 만든 것과 같다. 이제 `memberA`, `memberB`는 영속성 컨텍스트가 관리하지 않으므로 준영속 상태다.

```java
member.setUsername("changeName"); // 준영속 상태이므로 영속성 컨텍스트가 관리하지 않음
```
준영속 상태이므로 영속성 컨텍스트가 지원하는 변경 감지는 동작하지 않는다. 따라서 회원의 이름을 변경해도 데이터베이스에 반영되지 않는다.

### 3.6.3 영속성 컨텍스트 종료: close()

영속성 컨텍스트를 종료하면 해당 영속성 컨텍스트가 관리하던 영속 상태의 엔티티가 모두 준영속 상태가 된다.

**예제 3.10 영속성 컨텍스트 닫기**
```java
public void closeEntityManager() {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook");
    
    EntityManager em = emf.createEntityManager();
    EntityTransaction transaction = em.getTransaction();
    
    transaction.begin(); // 트랜잭션 시작
    
    Member memberA = em.find(Member.class, "memberA");
    Member memberB = em.find(Member.class, "memberB");
    
    transaction.commit(); // 트랜잭션 커밋
    
    em.close(); // 영속성 컨텍스트 종료
}
```

*그림 3.16 영속성 컨텍스트 제거 전*

![영속성 컨텍스트 제거 전](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdna%2FbemQQF%2FbtsdHQp3OtV%2FAAAAAAAAAAAAAAAAAAAAADrzRvwpObZV_IoeOZE4eQq4a7fjaondXwrKw3fgIQa2%2Fimg.png%3Fcredential%3DyqXZFxpELC7KVnFOS48ylbz2pIh7yKj8%26expires%3D1751295599%26allow_ip%3D%26allow_referer%3D%26signature%3DSa4OxuaLYoHbxr2P1kbka4j5vP4%253D)

*그림 3.17 영속성 컨텍스트 제거 후*

![영속성 컨텍스트 제거 후](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdna%2FuWtd7%2FbtsdFf5quoj%2FAAAAAAAAAAAAAAAAAAAAACpui3SAW1yiG4BOt-RhmK7LFCeuQwAPuUovjgiiBTFZ%2Fimg.png%3Fcredential%3DyqXZFxpELC7KVnFOS48ylbz2pIh7yKj8%26expires%3D1751295599%26allow_ip%3D%26allow_referer%3D%26signature%3Ds2TBQp4MrVjW%252FDUS0RLrQ8I%252BVGM%253D)

영속성 컨텍스트가 종료되어 `memberA`, `memberB`는 더는 관리되지 않는다.

> **참고**
>
> 영속 상태의 엔티티는 주로 영속성 컨텍스트가 종료되면서 준영속 상태가 된다. 개발자가 직접 준영속 상태로 만드는 일은 드물다.

### 3.6.4 준영속 상태의 특징

준영속 상태인 회원 엔티티는 다음과 같은 특징을 가진다.

1.  **거의 비영속 상태에 가깝다.**
    영속성 컨텍스트가 관리하지 않으므로 1차 캐시, 쓰기 지연, 변경 감지, 지연 로딩을 포함한 영속성 컨텍스트가 제공하는 어떠한 기능도 동작하지 않는다.

2.  **식별자 값을 가지고 있다.**
    비영속 상태는 식별자 값이 없을 수도 있지만, 준영속 상태는 이미 한 번 영속 상태였으므로 반드시 식별자 값을 가지고 있다.

3.  **지연 로딩을 할 수 없다.**
    지연 로딩은 실제 객체 대신 프록시 객체를 로딩해두고 해당 객체를 실제 사용할 때 영속성 컨텍스트를 통해 데이터를 불러오는 방법이다. 하지만 준영속 상태는 영속성 컨텍스트가 더는 관리하지 않으므로 지연 로딩 시 문제가 발생한다.

### 3.6.5 병합: merge()

준영속 상태의 엔티티를 다시 영속 상태로 변경하려면 병합(merge)을 사용하면 된다. `merge()` 메소드는 준영속 상태의 엔티티를 받아서 그 정보로 **새로운 영속 상태의 엔티티를 반환**한다.

**예제 3.12 merge() 사용 예**
```java
Member mergeMember = em.merge(member);
```

#### 준영속 병합

준영속 상태의 엔티티를 영속 상태로 변경하는 예제를 살펴보자.

```java
public class ExamMergeMain {

    static EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook");

    public static void main(String[] args) {
        Member member = createMember("memberA", "회원1");
        
        member.setUsername("회원명 변경"); // 준영속 상태에서 변경

        mergeMember(member);
    }
    
    static Member createMember(String id, String username) {
        //== 영속성 컨텍스트1 시작 ==//
        EntityManager em1 = emf.createEntityManager();
        EntityTransaction tx1 = em1.getTransaction();
        tx1.begin();

        Member member = new Member();
        member.setId(id);
        member.setUsername(username);

        em1.persist(member);
        tx1.commit();

        em1.close();    // 영속성 컨텍스트1 종료, member 엔티티는 준영속 상태가 된다.
        //== 영속성 컨텍스트1 종료 ==//

        return member;
    }

    static void mergeMember(Member member) {
        //== 영속성 컨텍스트2 시작 ==//
        EntityManager em2 = emf.createEntityManager();
        EntityTransaction tx2 = em2.getTransaction();

        tx2.begin();
        Member mergeMember = em2.merge(member);
        tx2.commit();

        // 준영속 상태
        System.out.println("member = " + member.getUsername());

        // 영속 상태
        System.out.println("mergeMember = " + mergeMember.getUsername());

        System.out.println("em2 contains member = " + em2.contains(member));
        System.out.println("em2 contains mergeMember = " + em2.contains(mergeMember));

        em2.close();
        //== 영속성 컨텍스트2 종료 ==//
    }
}
```

**출력 결과**
```text
member = 회원명 변경
mergeMember = 회원명 변경
em2 contains member = false
em2 contains mergeMember = true
```

1.  `member` 엔티티는 `createMember()` 메소드의 영속성 컨텍스트1에서 영속 상태였다가, 영속성 컨텍스트1이 종료되면서 준영속 상태가 되었다. 따라서 `createMember()` 메소드는 준영속 상태의 `member` 엔티티를 반환한다.
2.  `main()` 메소드에서 `member.setUsername("회원명 변경")`을 호출해서 회원 이름을 변경했지만, 준영속 상태인 `member` 엔티티를 관리하는 영속성 컨텍스트가 더는 존재하지 않으므로 수정 사항이 데이터베이스에 반영되지 않는다.
3.  `mergeMember()` 메소드에서 새로운 영속성 컨텍스트2를 시작하고 `em2.merge(member)`를 호출하여 준영속 상태의 `member` 엔티티를 영속 상태로 변경했다. 정확히는 `member` 엔티티가 영속 상태로 바뀌는 것이 아니라, `mergeMember`라는 새로운 영속 상태의 엔티티가 반환된다. 이 `mergeMember`가 영속성 컨텍스트의 관리를 받으므로, 트랜잭션을 커밋할 때 수정했던 회원명이 데이터베이스에 반영된다.

`merge()`의 동작 방식은 다음과 같다.

*그림 3.18 준영속 병합 - 수정*
![준영속 병합 - 수정](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdna%2FbHWOD7%2FbtsdHPxQIXc%2FAAAAAAAAAAAAAAAAAAAAAOUAbnKpsdRYLfu_A3mvYDQeWM92ipKY5fxK6VV4HJvb%2Fimg.png%3Fcredential%3DyqXZFxpELC7KVnFOS48ylbz2pIh7yKj8%26expires%3D1751295599%26allow_ip%3D%26allow_referer%3D%26signature%3Dj62lUSE%252BgdnxTQdqs4wPjn8ph0Q%253D)

1.  `merge()`를 실행한다.
2.  파라미터로 넘어온 준영속 엔티티의 식별자 값으로 1차 캐시에서 엔티티를 조회한다.
    - 만약 1차 캐시에 엔티티가 없으면 데이터베이스에서 엔티티를 조회하고 1차 캐시에 저장한다.
3.  조회한 영속 엔티티(`mergeMember`)에 파라미터로 넘어온 `member` 엔티티의 값을 채워 넣는다. (이때 `mergeMember`의 이름이 "회원1"에서 "회원명 변경"으로 바뀐다.)
4.  새로운 영속 엔티티인 `mergeMember`를 반환한다.

준영속 상태인 `member` 엔티티와 영속 상태인 `mergeMember` 엔티티는 서로 다른 인스턴스다. 병합 후에는 준영속 엔티티를 계속 사용하기보다, 반환된 영속 엔티티를 사용하는 것이 안전하다.

```java
// Member mergeMember = em2.merge(member); // 기존 코드
Member member = em2.merge(member); // 반환된 영속 엔티티를 사용하도록 변경
```

#### 비영속 병합

병합은 비영속 엔티티도 영속 상태로 만들 수 있다.

```java
Member member = new Member();
Member newMember = em.merge(member); // 비영속 병합
tx.commit();
```
병합은 파라미터로 넘어온 엔티티의 식별자 값으로 영속성 컨텍스트를 조회하고, 찾는 엔티티가 없으면 데이터베이스에서 조회한다. 만약 데이터베이스에서도 발견하지 못하면 새로운 엔티티를 생성해서 병합한다.

결론적으로 병합은 준영속, 비영속 상태를 신경 쓰지 않는다. 식별자 값으로 엔티티를 조회할 수 있으면 불러서 병합하고, 조회할 수 없으면 새로 생성해서 병합한다. 따라서 병합은 `save or update` 기능을 수행한다.