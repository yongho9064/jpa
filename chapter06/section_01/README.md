# 6. 다양한 연관관계 매핑

엔티티의 연관관계를 매핑할 때는 다음 3가지를 고려해야 한다.

-   **다중성**
-   **단방향, 양방향**
-   **연관관계의 주인**

먼저 연관관계가 있는 두 엔티티가 일대일 관계인지 일대다 관계인지 `다중성`을 고려해야 한다. 다음으로 두 엔티티 중 한쪽만 참조하는 단방향 관계인지 서로 참조하는 양방향 관계인지 고려해야 한다. 마지막으로 양방향 관계면 연관관계의 주인을 정해야 한다.

### 1. 다중성

연관관계에는 다음과 같은 다중성이 있다.

-   **다대일(`@ManyToOne`)**
-   **일대다(`@OneToMany`)**
-   **일대일(`@OneToOne`)**
-   **다대다(`@ManyToMany`)**

다중성을 판단하기 어려울 때는 **반대 방향을 생각해 보면 된다.** 일대다의 반대 방향은 항상 다대일이고, 일대일의 반대 방향은 항상 일대일이다.

보통 다대일과 일대다 관계를 가장 많이 사용하고, 다대다 관계는 실무에서 거의 사용하지 않는다.

### 2. 단방향, 양방향

테이블은 외래 키 하나로 조인을 사용해서 양방향으로 쿼리가 가능하므로 사실상 방향이라는 개념이 없다. 반면에 객체는 참조용 필드를 가지고 있는 객체만 연관된 객체를 조회할 수 있다. 객체 관계에서 한쪽만 참조하는 것을 **단방향 관계**라 하고, 양쪽이 서로 참조하는 것을 **양방향 관계**라 한다.

### 3. 연관관계의 주인

데이터베이스는 외래 키 하나로 두 테이블이 연관관계를 맺는다. 따라서 테이블의 연관관계를 관리하는 포인트는 외래 키 하나다. 반면에 엔티티를 양방향으로 매핑하면 A -> B, B -> A처럼 2곳에서 서로를 참조한다. 따라서 객체의 연관관계를 관리하는 포인트는 2곳이다.

JPA는 두 객체 연관관계 중 하나를 정해서 데이터베이스 외래 키를 관리하는데, 이것을 **연관관계의 주인(Owner)**이라 한다. 따라서 A -> B, B -> A 둘 중 하나를 정해서 외래 키를 관리해야 한다. 외래 키를 가진 테이블과 매핑한 엔티티가 외래 키를 관리하는 게 효율적이므로 보통 이곳을 연관관계의 주인으로 선택한다. 주인이 아닌 방향은 외래 키를 변경할 수 없고 읽기만 가능하다.

연관관계의 주인은 `mappedBy` 속성을 사용하지 않는다. 연관관계의 주인이 아니면 `mappedBy` 속성을 사용하고, 연관관계의 주인 필드 이름을 값으로 입력해야 한다.

---

지금부터 다중성과 단방향, 양방향을 고려한 가능한 모든 연관관계를 하나씩 알아본다.

> **참고**: 다중성은 왼쪽을 연관관계의 주인으로 정했다. 예를 들어 '다대일 양방향'이라 하면 다(N) 쪽이 연관관계의 주인이다.

*   다대일: 단방향, 양방향
*   일대다: 단방향, 양방향
*   일대일: 주 테이블 단방향, 양방향
*   일대일: 대상 테이블 단방향, 양방향
*   다대다: 단방향, 양방향

## 6.1 다대일(N:1)

다대일 관계의 반대 방향은 항상 일대다 관계고, 일대다 관계의 반대 방향은 항상 다대일 관계다. 데이터베이스 테이블의 일(1), 다(N) 관계에서 외래 키는 항상 **다(N) 쪽**에 있다. 따라서 객체 양방향 관계에서 연관관계의 주인은 항상 **다(N) 쪽**이다. 예를 들어 회원(N)과 팀(1)이 있으면 회원 쪽이 연관관계의 주인이다.

### 6.1.1 다대일 단방향 [N:1]

**그림 6.1 다대일 단방향**
![img](https://lar542.github.io/img/post_img/JPA-2019-08-08-2.png)

회원은 `Member.team`으로 팀 엔티티를 참조할 수 있지만, 반대로 팀에는 회원을 참조하는 필드가 없다. 따라서 회원과 팀은 다대일 단방향 관계다.

**예제 6.1 회원 엔티티 (`Member.java`)**
```java
import javax.persistence.*;

@Entity
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    private String username;

    @ManyToOne
    @JoinColumn(name = "TEAM_ID")
    private Team team;

    // Getter, Setter ...
}
```

**예제 6.2 팀 엔티티 (`Team.java`)**
```java
import javax.persistence.*;

@Entity
public class Team {

    @Id
    @GeneratedValue
    @Column(name = "TEAM_ID")
    private Long id;

    private String name;

    // Getter, Setter ...
}
```

`@JoinColumn(name = "TEAM_ID")`를 사용해서 `Member.team` 필드를 `TEAM_ID` 외래 키와 매핑했다. 따라서 `Member.team` 필드로 `MEMBER` 테이블의 `TEAM_ID` 외래 키를 관리한다.

### 6.1.2 다대일 양방향 [N:1, 1:N]

아래 그림에서 실선이 연관관계의 주인(`Member.team`)이고, 점선(`Team.members`)은 연관관계의 주인이 아니다.

**그림 6.2 다대일 양방향**

![img](https://oopy.lazyrockets.com/api/v2/notion/image?src=https%3A%2F%2Fs3-us-west-2.amazonaws.com%2Fsecure.notion-static.com%2F30ed9314-1269-4854-afeb-5f4f6c1c4dcf%2FUntitled.png&blockId=697375cb-d8c8-4d70-85d4-74374eccd405)

**예제 6.3 회원 엔티티 (`Member.java`)**
```java
@Entity
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    private String username;

    @ManyToOne
    @JoinColumn(name = "TEAM_ID")
    private Team team;

    //==연관관계 편의 메소드==//
    public void setTeam(Team team) {
        this.team = team;

        // 무한 루프에 빠지지 않도록 체크
        if (!team.getMembers().contains(this)) {
            team.getMembers().add(this);
        }
    }
}
```

**예제 6.4 팀 엔티티 (`Team.java`)**
```java
import java.util.ArrayList;
import java.util.List;

@Entity
public class Team {

    @Id
    @GeneratedValue
    @Column(name = "TEAM_ID")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();

    //==연관관계 편의 메소드==//
    public void addMember(Member member) {
        this.members.add(member);
        // 무한 루프에 빠지지 않도록 체크
        if (member.getTeam() != this) {
            member.setTeam(this);
        }
    }
    ...
}
```

#### 양방향은 외래 키가 있는 쪽이 연관관계의 주인이다
일대다와 다대일 연관관계는 항상 다(N)에 외래 키가 있다. 여기서는 다(N) 쪽인 `MEMBER` 테이블이 외래 키를 가지고 있으므로 `Member.team`이 연관관계의 주인이다. JPA는 외래 키를 관리할 때 연관관계의 주인만 사용한다. 주인이 아닌 `Team.members`는 조회를 위한 JPQL이나 객체 그래프를 탐색할 때 사용한다.

#### 양방향 연관관계는 항상 서로를 참조해야 한다
양방향 연관관계는 항상 서로 참조해야 한다. 어느 한쪽만 참조하면 양방향 연관관계가 성립하지 않는다. 항상 서로 참조하게 하려면 **연관관계 편의 메소드**를 작성하는 것이 좋다. 회원의 `setTeam()`, 팀의 `addMember()` 메소드가 이런 편의 메소드들이다. 편의 메소드는 한 곳에만 작성하거나 양쪽 다 작성할 수 있는데, 양쪽에 다 작성하면 무한 루프에 빠질 수 있으므로 주의해야 한다. 예제 코드는 편의 메소드를 양쪽에 다 작성해서 둘 중 하나만 호출하면 되도록 했으며, 무한 루프에 빠지지 않도록 검사하는 로직도 포함되어 있다.