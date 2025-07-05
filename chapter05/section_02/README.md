# 5.2 연관관계 사용

연관관계를 등록, 수정, 삭제, 조회하는 예제를 통해 연관관계를 어떻게 사용하는지 알아본다.

## 5.2.1 저장

연관관계를 매핑한 엔티티를 저장하는 방법은 다음과 같다.

```java
public void testSave() {
    
    // 팀1 저장
    Team team1 = new Team("team1", "팀1");
    em.persist(team1);
    
    // 회원1 저장
    Member member1 = new Member("member1", "회원1");
    member1.setTeam(team1); // 연관관계 설정 member1 -> team1
    em.persist(member1);
    
    // 회원2 저장
    Member member2 = new Member("member2", "회원2");
    member2.setTeam(team1); // 연관관계 설정 member2 -> team1
    em.persist(member2);
}
```

> **주의**
> JPA에서 엔티티를 저장할 때 연관된 모든 엔티티는 영속 상태여야 한다.

`member1.setTeam(team1);` 코드를 통해 회원이 팀을 참조하도록 설정한 후 `em.persist(member1);`로 저장하면, JPA는 참조한 팀의 식별자(`Team.id`)를 외래 키로 사용하여 적절한 등록 쿼리를 실행한다.

실행되는 SQL은 다음과 같으며, `MEMBER` 테이블의 외래 키(`TEAM_ID`) 값으로 참조한 팀의 식별자 값인 `team1`이 입력된 것을 확인할 수 있다.

```sql
INSERT INTO TEAM (TEAM_ID, NAME) VALUES ('team1', '팀1');
INSERT INTO MEMBER (MEMBER_ID, NAME, TEAM_ID) VALUES ('member1', '회원1', 'team1');
INSERT INTO MEMBER (MEMBER_ID, NAME, TEAM_ID) VALUES ('member2', '회원2', 'team1');
```

## 5.2.2 조회

연관관계가 있는 엔티티를 조회하는 방법은 크게 2가지다.

1.  **객체 그래프 탐색** (객체 연관관계를 사용한 조회)
2.  **객체지향 쿼리 사용** (JPQL)

### 1. 객체 그래프 탐색

`member.getTeam()`을 사용하여 `Member` 엔티티와 연관된 `Team` 엔티티를 조회할 수 있다. 이처럼 객체를 통해 연관된 엔티티를 찾아가는 것을 **객체 그래프 탐색**이라 한다.

```java
Member member = em.find(Member.class, "member1");
Team team = member.getTeam(); // 객체 그래프 탐색
System.out.println("팀 이름: " + team.getName());

// 출력 결과: 팀 이름: 팀1
```

### 2. 객체지향 쿼리 사용 (JPQL)

JPQL을 사용하면 특정 조건에 맞는 엔티티를 SQL과 유사한 문법으로 조회할 수 있다. 예를 들어, `팀1`에 소속된 모든 회원을 조회하려면 `JOIN`을 사용해야 한다.

> **JPQL 조인 검색**

```java
private static void queryLogicJoin(EntityManager em) {
    
    String jpql = "SELECT m FROM Member m JOIN m.team t WHERE t.name = :teamName";
    
    List<Member> resultList = em.createQuery(jpql, Member.class)
            .setParameter("teamName", "팀1")
            .getResultList();
    
    for (Member member : resultList) {
        System.out.println("[query] member.username = " + member.getUsername());
    }
    // 출력 결과: [query] member.username=회원1
    // 출력 결과: [query] member.username=회원2
}
```

JPQL의 `FROM Member m JOIN m.team t` 부분을 보면, `Member` 엔티티가 `Team` 엔티티와 관계를 맺고 있는 필드(`m.team`)를 통해 조인했다. 그리고 `WHERE` 절에서 조인한 팀의 이름(`t.name`)을 검색 조건으로 사용했다.
참고로 **`:teamName`과 같이 `:`로 시작하는 것은 파라미터를 바인딩받는 문법이다.**


이 JPQL은 다음과 같은 SQL로 변환되어 실행된다.

```sql
SELECT M.* 
FROM MEMBER M
INNER JOIN TEAM T ON M.TEAM_ID = T.TEAM_ID
WHERE T.NAME = '팀1';
```

JPQL은 SQL과 달리 테이블이 아닌 엔티티 객체를 대상으로 쿼리하므로 더 객체지향적이고 간결하다.

## 5.2.3 수정

`팀1` 소속이던 회원을 새로운 `팀2`에 소속되도록 연관관계를 수정해본다.

```java
private static void updateRelation(EntityManager em) {
    
    // 새로운 팀2 생성 및 저장
    Team team2 = new Team("team2", "팀2");
    em.persist(team2);
    
    // 회원1을 찾아 새로운 팀2로 설정
    Member member = em.find(Member.class, "member1");
    member.setTeam(team2); // 연관관계 수정
}
```

엔티티의 참조 대상만 변경하면, 트랜잭션 커밋 시 JPA의 **변경 감지(Dirty Checking)** 기능이 작동하여 `UPDATE` SQL을 자동으로 생성하고 실행한다.

```sql
UPDATE MEMBER 
SET 
    TEAM_ID = 'team2', ... 
WHERE 
    MEMBER_ID = 'member1';
```

## 5.2.4 연관관계 제거

회원의 소속 팀을 없애려면 참조를 `null`로 설정하면 된다.

```java
private static void deleteRelation(EntityManager em) {
    
    Member member1 = em.find(Member.class, "member1");
    member1.setTeam(null); // 연관관계 제거
}
```

이 코드는 `MEMBER` 테이블의 외래 키 `TEAM_ID`를 `null`로 설정하는 `UPDATE` 쿼리를 실행한다.

```sql
UPDATE MEMBER
SET 
    TEAM_ID = null, ... 
WHERE 
    MEMBER_ID = 'member1';
```

## 5.2.5 연관된 엔티티 삭제

연관된 엔티티를 삭제하려면, **기존 연관관계를 먼저 제거해야 한다.** 그렇지 않으면 외래 키 제약조건 위배 오류가 발생할 수 있다.

예를 들어, `회원1`과 `회원2`가 소속된 `팀1`을 삭제하려면 다음과 같이 연관관계를 먼저 끊어야 한다.

```java
// 팀1에 소속된 회원들의 연관관계를 먼저 제거
member1.setTeam(null);
member2.setTeam(null);

// 그 후에 팀을 삭제
em.remove(team1);
```