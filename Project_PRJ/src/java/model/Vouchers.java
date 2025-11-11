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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "Vouchers")
@NamedQueries({
    @NamedQuery(name = "Vouchers.findAll", query = "SELECT v FROM Vouchers v"),
    @NamedQuery(name = "Vouchers.findByVoucherId", query = "SELECT v FROM Vouchers v WHERE v.voucherId = :voucherId"),
    @NamedQuery(name = "Vouchers.findByVoucherCode", query = "SELECT v FROM Vouchers v WHERE v.voucherCode = :voucherCode"),
    @NamedQuery(name = "Vouchers.findByTitle", query = "SELECT v FROM Vouchers v WHERE v.title = :title"),
    @NamedQuery(name = "Vouchers.findByDescription", query = "SELECT v FROM Vouchers v WHERE v.description = :description"),
    @NamedQuery(name = "Vouchers.findByDiscountType", query = "SELECT v FROM Vouchers v WHERE v.discountType = :discountType"),
    @NamedQuery(name = "Vouchers.findByDiscountValue", query = "SELECT v FROM Vouchers v WHERE v.discountValue = :discountValue"),
    @NamedQuery(name = "Vouchers.findByMinOrderValue", query = "SELECT v FROM Vouchers v WHERE v.minOrderValue = :minOrderValue"),
    @NamedQuery(name = "Vouchers.findByMaxDiscountAmount", query = "SELECT v FROM Vouchers v WHERE v.maxDiscountAmount = :maxDiscountAmount"),
    @NamedQuery(name = "Vouchers.findByUsageLimit", query = "SELECT v FROM Vouchers v WHERE v.usageLimit = :usageLimit"),
    @NamedQuery(name = "Vouchers.findByUsedCount", query = "SELECT v FROM Vouchers v WHERE v.usedCount = :usedCount"),
    @NamedQuery(name = "Vouchers.findByStartDate", query = "SELECT v FROM Vouchers v WHERE v.startDate = :startDate"),
    @NamedQuery(name = "Vouchers.findByEndDate", query = "SELECT v FROM Vouchers v WHERE v.endDate = :endDate"),
    @NamedQuery(name = "Vouchers.findByIsActive", query = "SELECT v FROM Vouchers v WHERE v.isActive = :isActive"),
    @NamedQuery(name = "Vouchers.findByCreatedAt", query = "SELECT v FROM Vouchers v WHERE v.createdAt = :createdAt")})
public class Vouchers implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "voucher_id")
    private Integer voucherId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "voucher_code")
    private String voucherCode;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "title")
    private String title;
    @Size(max = 2147483647)
    @Column(name = "description")
    private String description;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "discount_type")
    private String discountType;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "discount_value")
    private BigDecimal discountValue;
    @Column(name = "min_order_value")
    private BigDecimal minOrderValue;
    @Column(name = "max_discount_amount")
    private BigDecimal maxDiscountAmount;
    @Column(name = "usage_limit")
    private Integer usageLimit;
    @Column(name = "used_count")
    private Integer usedCount;
    @Basic(optional = false)
    @NotNull
    @Column(name = "start_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "end_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;
    @Column(name = "is_active")
    private Boolean isActive;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @JoinColumn(name = "shop_id", referencedColumnName = "shop_id")
    @ManyToOne
    private Shops shopId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "voucherId")
    private List<VoucherUsage> voucherUsageList;

    public Vouchers() {
    }

    public Vouchers(Integer voucherId) {
        this.voucherId = voucherId;
    }

    public Vouchers(Integer voucherId, String voucherCode, String title, String discountType, BigDecimal discountValue, Date startDate, Date endDate) {
        this.voucherId = voucherId;
        this.voucherCode = voucherCode;
        this.title = title;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Integer getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(Integer voucherId) {
        this.voucherId = voucherId;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public BigDecimal getMinOrderValue() {
        return minOrderValue;
    }

    public void setMinOrderValue(BigDecimal minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public BigDecimal getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
        this.maxDiscountAmount = maxDiscountAmount;
    }

    public Integer getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(Integer usageLimit) {
        this.usageLimit = usageLimit;
    }

    public Integer getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(Integer usedCount) {
        this.usedCount = usedCount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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

    public Shops getShopId() {
        return shopId;
    }

    public void setShopId(Shops shopId) {
        this.shopId = shopId;
    }

    public List<VoucherUsage> getVoucherUsageList() {
        return voucherUsageList;
    }

    public void setVoucherUsageList(List<VoucherUsage> voucherUsageList) {
        this.voucherUsageList = voucherUsageList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (voucherId != null ? voucherId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Vouchers)) {
            return false;
        }
        Vouchers other = (Vouchers) object;
        if ((this.voucherId == null && other.voucherId != null) || (this.voucherId != null && !this.voucherId.equals(other.voucherId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.Vouchers[ voucherId=" + voucherId + " ]";
    }

}
