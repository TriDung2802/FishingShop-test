package com.fishing.model;

import jakarta.persistence.*;

/** Địa chỉ giao hàng của khách (mỗi khách có nhiều địa chỉ, tối đa một địa chỉ mặc định). */
@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "receiver_name", nullable = false, length = 100)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 15)
    private String receiverPhone;

    @Column(nullable = false, length = 150)
    private String street;

    @Column(length = 80)
    private String ward;

    @Column(length = 80)
    private String district;

    @Column(nullable = false, length = 80)
    private String city;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    public Address() {}

    /** Địa chỉ đầy đủ một dòng, dùng điền sẵn vào trang đặt hàng. */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder(street);
        if (ward != null && !ward.isBlank()) {
            sb.append(", ").append(ward);
        }
        if (district != null && !district.isBlank()) {
            sb.append(", ").append(district);
        }
        sb.append(", ").append(city);
        return sb.toString();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
}
