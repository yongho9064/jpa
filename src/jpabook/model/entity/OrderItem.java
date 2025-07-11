package jpabook.model.entity;

import javax.persistence.*;

@Entity
@Table(name = "ORDER_ITEM")
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "ORDER_ITEM_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ITEM_ID")
    private OrderItem item;

    @ManyToOne
    @JoinColumn(name = "ORDER_ID")
    private Order orderId;

    private int orderPrice;   // 주문 가격
    private int count;        // 주문 수량

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrderItem getItem() {
        return item;
    }

    public void setItem(OrderItem item) {
        this.item = item;
    }

    public Order getOrderId() {
        return orderId;
    }

    public void setOrderId(Order orderId) {
        this.orderId = orderId;
    }

    public int getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(int orderPrice) {
        this.orderPrice = orderPrice;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
