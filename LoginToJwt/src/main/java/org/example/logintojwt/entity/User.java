package org.example.logintojwt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Table(name = "users")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor // 없어도되긴함
@Getter
@ToString(exclude = "password")
@EqualsAndHashCode(of = "id")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password; //Bcrypt 암호화

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    private String address;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private List<Role> roles;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL)
    private List<Review> reviewList;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Cart cart;

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "user",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Order> orderList;

    @Builder
    public User(Long id, String username, String password, List<Role> roles, String email, String phoneNumber, String address, Cart cart,Order order) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.roles = roles != null ? new ArrayList<>(roles) : new ArrayList<>(List.of(Role.ROLE_USER));
        this.reviewList = new ArrayList<>();
        this.cart = cart;
        this.orderList = new ArrayList<>();
    }

    public void updateProfile(String email, String phoneNumber, String address){
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public void addReview(Review review) {
        reviewList.add(review);
        review.updateUser(this);
    }

    public void assignCart(Cart cart) {
        this.cart = cart;
        if (cart.getUser() != this) {
            cart.assignUser(this);
        }
    }

    public void addOrder(Order order) {
        orderList.add(order);
        order.assignUser(this);
    }

    public void removeOrder(Order order) {
        orderList.remove(order);
        order.assignUser(null);
    }

    public void changePassword(String password) {
        this.password = password;
    }
}




