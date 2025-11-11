/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ADMIN
 */
@Entity
@Table(name = "Users")
@NamedQueries({
    @NamedQuery(name = "Users.findAll", query = "SELECT u FROM Users u"),
    @NamedQuery(name = "Users.findByUserId", query = "SELECT u FROM Users u WHERE u.userId = :userId"),
    @NamedQuery(name = "Users.findByUsername", query = "SELECT u FROM Users u WHERE u.username = :username"),
    @NamedQuery(name = "Users.findByPasswordHash", query = "SELECT u FROM Users u WHERE u.passwordHash = :passwordHash"),
    @NamedQuery(name = "Users.findByEmail", query = "SELECT u FROM Users u WHERE u.email = :email"),
    @NamedQuery(name = "Users.findByPhone", query = "SELECT u FROM Users u WHERE u.phone = :phone"),
    @NamedQuery(name = "Users.findByDateOfBirth", query = "SELECT u FROM Users u WHERE u.dateOfBirth = :dateOfBirth"),
    @NamedQuery(name = "Users.findByAvatarUrl", query = "SELECT u FROM Users u WHERE u.avatarUrl = :avatarUrl"),
    @NamedQuery(name = "Users.findByRole", query = "SELECT u FROM Users u WHERE u.role = :role"),
    @NamedQuery(name = "Users.findByStatus", query = "SELECT u FROM Users u WHERE u.status = :status"),
    @NamedQuery(name = "Users.findByCreatedAt", query = "SELECT u FROM Users u WHERE u.createdAt = :createdAt"),
    @NamedQuery(name = "Users.findByUpdatedAt", query = "SELECT u FROM Users u WHERE u.updatedAt = :updatedAt")})
public class Users implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "user_id")
    private Integer userId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "username")
    private String username;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "password_hash")
    private String passwordHash;
    // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Invalid email")//if the field contains email address consider using this annotation to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "email")
    private String email;
    // @Pattern(regexp="^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$", message="Invalid phone/fax format, should be as xxx-xxx-xxxx")//if the field contains phone or fax number consider using this annotation to enforce field validation
    @Size(max = 15)
    @Column(name = "phone")
    private String phone;
    @Column(name = "date_of_birth")
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;
    @Size(max = 255)
    @Column(name = "avatar_url")
    private String avatarUrl;
    @Size(max = 50)
    @Column(name = "role")
    private String role;
    @Size(max = 50)
    @Column(name = "status")
    private String status;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "customerId")
    private List<Orders> ordersList;
    @OneToOne(mappedBy = "userId")
    private UserLoyalty userLoyalty;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "customerId")
    private Wishlists wishlists;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "customerId")
    private List<Conversations> conversationsList;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "customerId")
    private Carts carts;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    private List<Addresses> addressesList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    private List<VoucherUsage> voucherUsageList;
    @OneToOne(mappedBy = "userId")
    private Wallets wallets;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "customerId")
    private List<Reviews> reviewsList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    private List<ProductViewHistory> productViewHistoryList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    private List<Notifications> notificationsList;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "sellerId")
    private Shops shops;

    public Users() {
    }

    public Users(Integer userId) {
        this.userId = userId;
    }

    public Users(String username, String passwordHash, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Orders> getOrdersList() {
        return ordersList;
    }

    public void setOrdersList(List<Orders> ordersList) {
        this.ordersList = ordersList;
    }

    public UserLoyalty getUserLoyalty() {
        return userLoyalty;
    }

    public void setUserLoyalty(UserLoyalty userLoyalty) {
        this.userLoyalty = userLoyalty;
    }

    public Wishlists getWishlists() {
        return wishlists;
    }

    public void setWishlists(Wishlists wishlists) {
        this.wishlists = wishlists;
    }

    public List<Conversations> getConversationsList() {
        return conversationsList;
    }

    public void setConversationsList(List<Conversations> conversationsList) {
        this.conversationsList = conversationsList;
    }

    public Carts getCarts() {
        return carts;
    }

    public void setCarts(Carts carts) {
        this.carts = carts;
    }

    public List<Addresses> getAddressesList() {
        return addressesList;
    }

    public void setAddressesList(List<Addresses> addressesList) {
        this.addressesList = addressesList;
    }

    public List<VoucherUsage> getVoucherUsageList() {
        return voucherUsageList;
    }

    public void setVoucherUsageList(List<VoucherUsage> voucherUsageList) {
        this.voucherUsageList = voucherUsageList;
    }

    public Wallets getWallets() {
        return wallets;
    }

    public void setWallets(Wallets wallets) {
        this.wallets = wallets;
    }

    public List<Reviews> getReviewsList() {
        return reviewsList;
    }

    public void setReviewsList(List<Reviews> reviewsList) {
        this.reviewsList = reviewsList;
    }

    public List<ProductViewHistory> getProductViewHistoryList() {
        return productViewHistoryList;
    }

    public void setProductViewHistoryList(List<ProductViewHistory> productViewHistoryList) {
        this.productViewHistoryList = productViewHistoryList;
    }

    public List<Notifications> getNotificationsList() {
        return notificationsList;
    }

    public void setNotificationsList(List<Notifications> notificationsList) {
        this.notificationsList = notificationsList;
    }

    public Shops getShops() {
        return shops;
    }

    public void setShops(Shops shops) {
        this.shops = shops;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userId != null ? userId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Users)) {
            return false;
        }
        Users other = (Users) object;
        if ((this.userId == null && other.userId != null) || (this.userId != null && !this.userId.equals(other.userId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Users{" + "userId=" + userId + ", username=" + username + ", passwordHash=" + passwordHash + ", email=" + email + ", phone=" + phone + ", dateOfBirth=" + dateOfBirth + ", avatarUrl=" + avatarUrl + ", role=" + role + ", status=" + status + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + ", ordersList=" + ordersList + ", userLoyalty=" + userLoyalty + ", wishlists=" + wishlists + ", conversationsList=" + conversationsList + ", carts=" + carts + ", addressesList=" + addressesList + ", voucherUsageList=" + voucherUsageList + ", wallets=" + wallets + ", reviewsList=" + reviewsList + ", productViewHistoryList=" + productViewHistoryList + ", notificationsList=" + notificationsList + ", shops=" + shops + '}';
    }

}
