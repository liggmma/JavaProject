/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author ADMIN
 */
@Entity
@Table(name = "User_Loyalty")
@NamedQueries({
    @NamedQuery(name = "UserLoyalty.findAll", query = "SELECT u FROM UserLoyalty u"),
    @NamedQuery(name = "UserLoyalty.findByLoyaltyId", query = "SELECT u FROM UserLoyalty u WHERE u.loyaltyId = :loyaltyId"),
    @NamedQuery(name = "UserLoyalty.findByTotalSpending", query = "SELECT u FROM UserLoyalty u WHERE u.totalSpending = :totalSpending"),
    @NamedQuery(name = "UserLoyalty.findByCurrentYearSpending", query = "SELECT u FROM UserLoyalty u WHERE u.currentYearSpending = :currentYearSpending"),
    @NamedQuery(name = "UserLoyalty.findByCurrentMonthSpending", query = "SELECT u FROM UserLoyalty u WHERE u.currentMonthSpending = :currentMonthSpending"),
    @NamedQuery(name = "UserLoyalty.findByCashbackEarned", query = "SELECT u FROM UserLoyalty u WHERE u.cashbackEarned = :cashbackEarned"),
    @NamedQuery(name = "UserLoyalty.findByTierProgressAmount", query = "SELECT u FROM UserLoyalty u WHERE u.tierProgressAmount = :tierProgressAmount"),
    @NamedQuery(name = "UserLoyalty.findByTierUpdatedAt", query = "SELECT u FROM UserLoyalty u WHERE u.tierUpdatedAt = :tierUpdatedAt"),
    @NamedQuery(name = "UserLoyalty.findByTotalOrders", query = "SELECT u FROM UserLoyalty u WHERE u.totalOrders = :totalOrders"),
    @NamedQuery(name = "UserLoyalty.findByFirstPurchaseDate", query = "SELECT u FROM UserLoyalty u WHERE u.firstPurchaseDate = :firstPurchaseDate"),
    @NamedQuery(name = "UserLoyalty.findByLastPurchaseDate", query = "SELECT u FROM UserLoyalty u WHERE u.lastPurchaseDate = :lastPurchaseDate"),
    @NamedQuery(name = "UserLoyalty.findByUpdatedAt", query = "SELECT u FROM UserLoyalty u WHERE u.updatedAt = :updatedAt")})
public class UserLoyalty implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "loyalty_id")
    private Integer loyaltyId;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "total_spending")
    private BigDecimal totalSpending;
    @Column(name = "current_year_spending")
    private BigDecimal currentYearSpending;
    @Column(name = "current_month_spending")
    private BigDecimal currentMonthSpending;
    @Column(name = "cashback_earned")
    private BigDecimal cashbackEarned;
    @Column(name = "tier_progress_amount")
    private BigDecimal tierProgressAmount;
    @Column(name = "tier_updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date tierUpdatedAt;
    @Column(name = "total_orders")
    private Integer totalOrders;
    @Column(name = "first_purchase_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date firstPurchaseDate;
    @Column(name = "last_purchase_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastPurchaseDate;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @JoinColumn(name = "membership_tier_id", referencedColumnName = "tier_id")
    @ManyToOne(optional = false)
    private MembershipTiers membershipTierId;
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @OneToOne
    private Users userId;

    public UserLoyalty() {
    }

    public UserLoyalty(Integer loyaltyId) {
        this.loyaltyId = loyaltyId;
    }

    public Integer getLoyaltyId() {
        return loyaltyId;
    }

    public void setLoyaltyId(Integer loyaltyId) {
        this.loyaltyId = loyaltyId;
    }

    public BigDecimal getTotalSpending() {
        return totalSpending;
    }

    public void setTotalSpending(BigDecimal totalSpending) {
        this.totalSpending = totalSpending;
    }

    public BigDecimal getCurrentYearSpending() {
        return currentYearSpending;
    }

    public void setCurrentYearSpending(BigDecimal currentYearSpending) {
        this.currentYearSpending = currentYearSpending;
    }

    public BigDecimal getCurrentMonthSpending() {
        return currentMonthSpending;
    }

    public void setCurrentMonthSpending(BigDecimal currentMonthSpending) {
        this.currentMonthSpending = currentMonthSpending;
    }

    public BigDecimal getCashbackEarned() {
        return cashbackEarned;
    }

    public void setCashbackEarned(BigDecimal cashbackEarned) {
        this.cashbackEarned = cashbackEarned;
    }

    public BigDecimal getTierProgressAmount() {
        return tierProgressAmount;
    }

    public void setTierProgressAmount(BigDecimal tierProgressAmount) {
        this.tierProgressAmount = tierProgressAmount;
    }

    public Date getTierUpdatedAt() {
        return tierUpdatedAt;
    }

    public void setTierUpdatedAt(Date tierUpdatedAt) {
        this.tierUpdatedAt = tierUpdatedAt;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Date getFirstPurchaseDate() {
        return firstPurchaseDate;
    }

    public void setFirstPurchaseDate(Date firstPurchaseDate) {
        this.firstPurchaseDate = firstPurchaseDate;
    }

    public Date getLastPurchaseDate() {
        return lastPurchaseDate;
    }

    public void setLastPurchaseDate(Date lastPurchaseDate) {
        this.lastPurchaseDate = lastPurchaseDate;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public MembershipTiers getMembershipTierId() {
        return membershipTierId;
    }

    public void setMembershipTierId(MembershipTiers membershipTierId) {
        this.membershipTierId = membershipTierId;
    }

    public Users getUserId() {
        return userId;
    }

    public void setUserId(Users userId) {
        this.userId = userId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (loyaltyId != null ? loyaltyId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserLoyalty)) {
            return false;
        }
        UserLoyalty other = (UserLoyalty) object;
        if ((this.loyaltyId == null && other.loyaltyId != null) || (this.loyaltyId != null && !this.loyaltyId.equals(other.loyaltyId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.UserLoyalty[ loyaltyId=" + loyaltyId + " ]";
    }

}
