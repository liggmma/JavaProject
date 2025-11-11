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
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ADMIN
 */
@Entity
@Table(name = "Membership_Tiers")
@NamedQueries({
    @NamedQuery(name = "MembershipTiers.findAll", query = "SELECT m FROM MembershipTiers m"),
    @NamedQuery(name = "MembershipTiers.findByTierId", query = "SELECT m FROM MembershipTiers m WHERE m.tierId = :tierId"),
    @NamedQuery(name = "MembershipTiers.findByTierName", query = "SELECT m FROM MembershipTiers m WHERE m.tierName = :tierName"),
    @NamedQuery(name = "MembershipTiers.findByMinSpendingRequired", query = "SELECT m FROM MembershipTiers m WHERE m.minSpendingRequired = :minSpendingRequired"),
    @NamedQuery(name = "MembershipTiers.findByTierColor", query = "SELECT m FROM MembershipTiers m WHERE m.tierColor = :tierColor"),
    @NamedQuery(name = "MembershipTiers.findByTierIconUrl", query = "SELECT m FROM MembershipTiers m WHERE m.tierIconUrl = :tierIconUrl"),
    @NamedQuery(name = "MembershipTiers.findByCashbackPercentage", query = "SELECT m FROM MembershipTiers m WHERE m.cashbackPercentage = :cashbackPercentage"),
    @NamedQuery(name = "MembershipTiers.findByPrioritySupport", query = "SELECT m FROM MembershipTiers m WHERE m.prioritySupport = :prioritySupport"),
    @NamedQuery(name = "MembershipTiers.findByBirthdayBonus", query = "SELECT m FROM MembershipTiers m WHERE m.birthdayBonus = :birthdayBonus"),
    @NamedQuery(name = "MembershipTiers.findByDescription", query = "SELECT m FROM MembershipTiers m WHERE m.description = :description"),
    @NamedQuery(name = "MembershipTiers.findByIsActive", query = "SELECT m FROM MembershipTiers m WHERE m.isActive = :isActive"),
    @NamedQuery(name = "MembershipTiers.findByCreatedAt", query = "SELECT m FROM MembershipTiers m WHERE m.createdAt = :createdAt")})
public class MembershipTiers implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "tier_id")
    private Integer tierId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "tier_name")
    private String tierName;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "min_spending_required")
    private BigDecimal minSpendingRequired;
    @Size(max = 7)
    @Column(name = "tier_color")
    private String tierColor;
    @Size(max = 255)
    @Column(name = "tier_icon_url")
    private String tierIconUrl;
    @Basic(optional = false)
    @NotNull
    @Column(name = "cashback_percentage")
    private BigDecimal cashbackPercentage;
    @Basic(optional = false)
    @NotNull
    @Column(name = "priority_support")
    private boolean prioritySupport;
    @Basic(optional = false)
    @NotNull
    @Column(name = "birthday_bonus")
    private BigDecimal birthdayBonus;
    @Size(max = 2147483647)
    @Column(name = "description")
    private String description;
    @Column(name = "is_active")
    private Boolean isActive;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "membershipTierId")
    private List<UserLoyalty> userLoyaltyList;

    public MembershipTiers() {
    }

    public MembershipTiers(Integer tierId) {
        this.tierId = tierId;
    }

    public MembershipTiers(Integer tierId, String tierName, BigDecimal minSpendingRequired, BigDecimal cashbackPercentage, boolean prioritySupport, BigDecimal birthdayBonus) {
        this.tierId = tierId;
        this.tierName = tierName;
        this.minSpendingRequired = minSpendingRequired;
        this.cashbackPercentage = cashbackPercentage;
        this.prioritySupport = prioritySupport;
        this.birthdayBonus = birthdayBonus;
    }

    public Integer getTierId() {
        return tierId;
    }

    public void setTierId(Integer tierId) {
        this.tierId = tierId;
    }

    public String getTierName() {
        return tierName;
    }

    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    public BigDecimal getMinSpendingRequired() {
        return minSpendingRequired;
    }

    public void setMinSpendingRequired(BigDecimal minSpendingRequired) {
        this.minSpendingRequired = minSpendingRequired;
    }

    public String getTierColor() {
        return tierColor;
    }

    public void setTierColor(String tierColor) {
        this.tierColor = tierColor;
    }

    public String getTierIconUrl() {
        return tierIconUrl;
    }

    public void setTierIconUrl(String tierIconUrl) {
        this.tierIconUrl = tierIconUrl;
    }

    public BigDecimal getCashbackPercentage() {
        return cashbackPercentage;
    }

    public void setCashbackPercentage(BigDecimal cashbackPercentage) {
        this.cashbackPercentage = cashbackPercentage;
    }

    public boolean getPrioritySupport() {
        return prioritySupport;
    }

    public void setPrioritySupport(boolean prioritySupport) {
        this.prioritySupport = prioritySupport;
    }

    public BigDecimal getBirthdayBonus() {
        return birthdayBonus;
    }

    public void setBirthdayBonus(BigDecimal birthdayBonus) {
        this.birthdayBonus = birthdayBonus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<UserLoyalty> getUserLoyaltyList() {
        return userLoyaltyList;
    }

    public void setUserLoyaltyList(List<UserLoyalty> userLoyaltyList) {
        this.userLoyaltyList = userLoyaltyList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (tierId != null ? tierId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MembershipTiers)) {
            return false;
        }
        MembershipTiers other = (MembershipTiers) object;
        if ((this.tierId == null && other.tierId != null) || (this.tierId != null && !this.tierId.equals(other.tierId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.MembershipTiers[ tierId=" + tierId + " ]";
    }

}
