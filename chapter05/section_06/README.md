# 5.6 양방향 연관관계의 주의점

양방향 연관관계를 설정할 때 가장 흔히 하는 실수는 연관관계의 주인에는 값을 입력하지 않고, 주인이 아닌 곳에만 값을 입력하는 것이다. 데이터베이스에 외래 키 값이 정상적으로 저장되지 않는다면 이 문제부터 의심해봐야 한다.

주인이 아닌 곳에만 값을 설정하면 어떻게 되는지 아래 예제를 통해 살펴본다.

### 주인이 아닌 곳에만 연관관계를 설정한 경우
```java
public void testSaveNonOwner() {
    
    // 회원1 저장
    Member member1 = new Member("member1", "회원1");
    em.persist(member1);
    
    // 회원2 저장
    Member member2 = new Member("member2", "회원2");
    em.persist(member2);
    
    Team team = new Team("team1", "팀1");
    // 주인이 아닌 Team.members에만 연관관계 설정
    team.getMembers().add(member1);
    team.getMembers().add(member2);
    
    em.persist(team);
}
```
위 코드를 실행한 후 회원을 조회하면 결과는 다음과 같다.

| MEMBER_ID | USERNAME | TEAM_ID |
|:----------|:---------|:--------|
| `member1` | 회원1    | null    |
| `member2` | 회원2    | null    |

**연관관계의 주인만이 외래 키의 값을 변경할 수 있다.** 따라서 위 예제에서 `team.getMembers().add(member1);`처럼 주인이 아닌 `Team.members`에만 값을 추가하면, 데이터베이스의 `MEMBER` 테이블 `TEAM_ID` 외래 키 컬럼에는 `null`이 저장된다.

---

## 5.6.1 순수한 객체까지 고려한 양방향 연관관계

> 🤔 **그렇다면 정말 연관관계의 주인에만 값을 저장하고, 주인이 아닌 곳에는 값을 저장하지 않아도 될까?**

**결론부터 말하면, 객체 관점에서 양쪽 방향에 모두 값을 입력해주는 것이 가장 안전하다.** 양쪽 모두에 값을 설정하지 않으면, JPA를 사용하지 않는 순수한 객체 상태에서 심각한 문제가 발생할 수 있다.

예를 들어, JPA의 도움 없이 엔티티만으로 테스트 코드를 작성한다고 가정해보자. ORM은 객체와 관계형 데이터베이스 양쪽의 패러다임을 모두 중요하게 다루므로, 데이터베이스뿐만 아니라 객체 관계도 신중하게 고려해야 한다.

### 순수한 객체 상태의 문제점
```java
public void testPureObject_Bidirectional() {
    
    // 팀과 회원 생성
    Team team1 = new Team("team1", "팀1");
    Member member1 = new Member("member1", "회원1");
    Member member2 = new Member("member2", "회원2");
    
    // 연관관계의 주인인 Member에만 값을 설정
    member1.setTeam(team1); // 연관관계 설정: member1 -> team1
    member2.setTeam(team1); // 연관관계 설정: member2 -> team1
    
    // 반대 방향인 Team에는 값을 설정하지 않았음!
    List<Member> members = team1.getMembers();
    System.out.println("members.size = " + members.size());
    
    // 결과: members.size = 0
}
```
위 코드는 JPA를 사용하지 않은 순수한 객체 코드다. `Member`의 `team` 필드에만 값을 설정하고, 반대 방향인 `Team`의 `members` 컬렉션에는 아무런 작업을 하지 않았다. 이 경우 `team1.getMembers()`를 호출해도 `List`가 비어있으므로 `members.size()`는 **0**이 된다. 이는 우리가 기대하는 양방향 연관관계가 아니다.

객체의 양방향 관계는 양쪽 모두에 참조를 설정해야 한다. `회원 -> 팀` 관계를 설정했다면, 반대 방향인 `팀 -> 회원` 관계도 설정해주어야 한다.

### 양쪽 모두 관계를 설정하는 올바른 코드
```java
public void testPureObject_Bidirectional_Correct() {
    
    // 팀과 회원 생성
    Team team1 = new Team("team1", "팀1");
    Member member1 = new Member("member1", "회원1");
    Member member2 = new Member("member2", "회원2");
    
    // 1. 주인에게 값 설정 (Member -> Team)
    member1.setTeam(team1);
    // 2. 주인이 아닌 곳에도 값 설정 (Team -> Member)
    team1.getMembers().add(member1);
    
    // 1. 주인에게 값 설정 (Member -> Team)
    member2.setTeam(team1);
    // 2. 주인이 아닌 곳에도 값 설정 (Team -> Member)
    team1.getMembers().add(member2);
    
    List<Member> members = team1.getMembers();
    System.out.println("members.size = " + members.size());
    
    // 결과: members.size = 2
}
```
객체지향적인 관점까지 고려한다면 이처럼 양쪽 모두 관계를 맺어주어야 한다. 이제 JPA를 사용하여 이 로직을 완성한 예를 본다.

### JPA를 적용한 양방향 관계 설정
```java
public void testORM_Bidirectional() {
    
    // 팀1 저장
    Team team1 = new Team("team1", "팀1");
    em.persist(team1);
    
    Member member1 = new Member("member1", "회원1");
    // 양방향 연관관계 설정
    member1.setTeam(team1);              // 연관관계 설정 (주인) member1 -> team1
    team1.getMembers().add(member1);     // 연관관계 설정 (주인X) team1 -> member1
    em.persist(member1);
    
    Member member2 = new Member("member2", "회원2");
    // 양방향 연관관계 설정
    member2.setTeam(team1);              // 연관관계 설정 (주인) member2 -> team1
    team1.getMembers().add(member2);     // 연관관계 설정 (주인X) team1 -> member2
    em.persist(member2);
}
```
이렇게 양쪽에 연관관계를 모두 설정하면, 순수한 객체 상태에서도 관계가 올바르게 동작하며 데이터베이스의 외래 키도 정상적으로 저장된다. 물론, 외래 키 값은 연관관계의 주인인 `Member.team`의 값을 사용한다.

-   `Member.team`: 연관관계의 주인. 이 값으로 외래 키를 관리한다.
-   `Team.members`: 연관관계의 주인이 아님. 저장 시 사용되지 않으며, 순수 객체 상태의 탐색을 위해 존재한다.

**결론은, 객체의 양방향 연관관계는 양쪽 모두 관계를 맺어주어야 한다.**

---

## 5.6.2 연관관계 편의 메소드

양방향 연관관계를 설정할 때는 결국 양쪽을 모두 신경 써야 한다. `member.setTeam(team)`과 `team.getMembers().add(member)`를 매번 함께 호출해야 하는데, 만약 실수로 둘 중 하나만 호출하면 객체 관계가 깨질 수 있다.

```java
member.setTeam(team);           // (1) Member -> Team 설정
team.getMembers().add(member);  // (2) Team -> Member 설정
```

이 두 코드는 사실상 하나의 논리적인 단위이므로, 하나처럼 사용하는 것이 안전하다. 이를 위해 `Member` 클래스의 `setTeam()` 메소드를 다음과 같이 리팩토링할 수 있다.

```java
// Member.java
public class Member {
    
    private Team team;
    
    public void setTeam(Team team) {
        this.team = team;
        team.getMembers().add(this); // team의 members 리스트에도 현재 Member 객체를 추가
    }
    // ...
}
```

이제 `setTeam()` 메소드 하나만 호출해도 양방향 관계가 모두 설정된다. 이렇게 하나의 메소드로 양방향 관계를 편리하게 설정하는 메소드를 **연관관계 편의 메소드**라고 부른다.

### 연관관계 편의 메소드를 사용한 리팩토링
```java
public void testORM_Bidirectional_Refactoring() {
    
    Team team1 = new Team("team1", "팀1");
    em.persist(team1);
    
    Member member1 = new Member("member1", "회원1");
    member1.setTeam(team1); // 편의 메소드 호출 한 번으로 양방향 설정 완료
    em.persist(member1);
    
    Member member2 = new Member("member2", "회원2");
    member2.setTeam(team1); // 편의 메소드 호출 한 번으로 양방향 설정 완료
    em.persist(member2);
}
```
리팩토링을 통해 실수를 줄이고, 코드가 훨씬 간결해졌다.

---

## 5.6.3 연관관계 편의 메소드 작성 시 주의사항

하지만 위에서 작성한 `setTeam()` 편의 메소드에는 미묘한 버그가 숨어 있다. 회원의 팀을 변경하는 경우를 생각해보자.

```java
member1.setTeam(teamA); // 1. member1을 teamA에 소속시킴
member1.setTeam(teamB); // 2. member1의 팀을 teamB로 변경
// 이 때, teamA의 members 리스트에는 여전히 member1이 남아있다!
List<Member> teamAMembers = teamA.getMembers(); // 여기서 member1이 조회되는 문제 발생
```

먼저 `member1.setTeam(teamA)`를 호출하면 `member1 -> teamA`와 `teamA -> member1` 관계가 설정된다.

>**그림 5.9** `member1.setTeam(teamA)` 호출 후
>
> ![image](https://velog.velcdn.com/images%2Fshininghyunho%2Fpost%2F150e1146-12ef-4300-bcca-bf8365ead688%2Fimage.png)

다음으로 `member1.setTeam(teamB)`를 호출하면, `member1`의 `team` 필드는 `teamB`로 바뀌고, `teamB.members`에 `member1`이 추가된다. 하지만 **기존 `teamA.members`에서 `member1`을 제거하는 로직이 없으므로** `teamA`는 여전히 `member1`을 참조하는 문제가 발생한다.

>**그림 5.10** `member1.setTeam(teamB)` 호출 후 (잘못된 상태)
>
>![image](https://velog.velcdn.com/images%2Fshininghyunho%2Fpost%2Fd32161e7-ef63-4ba0-92af-16fff4def098%2Fimage.png)

연관관계를 변경할 때는 **기존 관계를 먼저 제거**한 후에 새로운 관계를 설정해야 한다. `setTeam()` 메소드를 다음과 같이 수정해야 안전하다.

```java
// Member.java
public void setTeam(Team team) {
    // 1. 기존 팀과의 관계를 제거
    if (this.team != null) {
        this.team.getMembers().remove(this);
    }
    
    // 2. 새로운 팀과의 관계를 설정
    this.team = team;
    team.getMembers().add(this);
}
```

이 코드는 객체 세계에서 서로 다른 단방향 연관관계 2개를 마치 하나의 양방향 관계처럼 보이게 만들기 위해 얼마나 세심한 로직이 필요한지 보여준다. 반면, 관계형 데이터베이스는 외래 키 하나로 이 문제를 아주 간단하게 해결한다.

결론적으로, 객체에서 양방향 연관관계를 사용하려면 이처럼 로직을 견고하게 작성해야 한다.

> **📝 참고**
>
> 그림 5.10처럼 `teamA`에서 `member1`을 제거하지 않아도 데이터베이스의 외래 키를 변경하는 데는 문제가 없다. 연관관계의 주인인 `Member.team`의 참조가 `teamB`로 변경되었으므로, 데이터베이스에는 `TEAM_ID`가 `teamB`의 ID로 정상적으로 반영된다.
>
> 또한, 트랜잭션이 커밋되고 새로운 영속성 컨텍스트에서 `teamA`를 조회하여 `teamA.getMembers()`를 호출하면, 데이터베이스에는 이미 관계가 끊어져 있으므로 `member1`이 조회되지 않는다.
>
> **진짜 문제는 연관관계를 변경한 직후, 아직 영속성 컨텍스트가 살아있는 상태에서 `teamA.getMembers()`를 호출할 때 발생한다.** 이때는 메모리에 남아있는 `teamA`의 `members` 컬렉션에 `member1`이 그대로 존재하므로, 의도치 않은 결과가 반환될 수 있다. 따라서 위에서 설명한 것처럼 기존 관계를 제거하는 로직을 추가하는 것이 가장 안전하고 예측 가능한 방법이다.