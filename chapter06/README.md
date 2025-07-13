## JPA에서 복합 키를 사용하는 방법
* 복합 키는 별도의 식별자 클래스로 만들어야 한다.
* Serializable 인터페이스를 구현해야 한다.
* equals()와 hashCode() 메서드를 구현해야 한다.
* 기본 생성자가 필요하다.
* 식별자 클래스는 public이어야 한다.
* `@IdClss`를 사용하는 방법 외에 `@EmbeddedId`를 사용하는 방법도 있다.

> 식별 관계란?
>
> 부모 테이블의 기본 키를 받아서 자신의 기본 키 + 외래 키로 사용하는 것을 데이터베이스 용어로 `식별 관계`라고 한다.

### 예시) 회원-상품 주문(M:N) 관계

**회원상품 엔티티 코드**

```java
@Entity
@IdClass(MemberProductId.class)
public class MemberProudct {
    
    @Id
    @ManyToOne
    @JoinColumn(name = "MEMBER_ID")
    private Member member;     // MemberProductId.memberId와 연결
    
    @Id
    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID")
    private Product product;   // MemberProductId.productId와 연결
    
    private int orderAmount; // 주문 금액

    ...
}
```
**회원 상품 식별자 클래스 코드**

```java
public class MemberProductId implements Serializable {
    
    private Long memberId;    // Member의 id
    private Long productId;   // Product의 id

    public MemberProductId() {
    }

    public MemberProductId(Long memberId, Long productId) {
        this.memberId = memberId;
        this.productId = productId;
    }

    @Override
    public boolean equals(Object o) {
        ...
    }

    @Override
    public int hashCode() {
        ...
    }
}
```

## @ManyToMany 사용을 피해야 하는 이유
* 다대다 관계는 연결 테이블을 JPA가 알아서 처리해주므로 편리하지만 연결 테이블에 필드가 추가되면 더는 사용할 수 없으므로 실무에서 활용하기에는 무리가 있다.
  * 따라서 중간 테이블을 만들어서 1:N, N:1 관계로 풀어내는 것이 좋다.