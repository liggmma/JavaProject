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
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author ADMIN
 */
@Entity
@Table(name = "Voucher_Usage")
@NamedQueries({
    @NamedQuery(name = "VoucherUsage.findAll", query = "SELECT v FROM VoucherUsage v"),
    @NamedQuery(name = "VoucherUsage.findByUsageId", query = "SELECT v FROM VoucherUsage v WHERE v.usageId = :usageId"),
    @NamedQuery(name = "VoucherUsage.findByDiscountAmount", query = "SELECT v FROM VoucherUsage v WHERE v.discountAmount = :discountAmount"),
    @NamedQuery(name = "VoucherUsage.findByUsedAt", query = "SELECT v FROM VoucherUsage v WHERE v.usedAt = :usedAt")})
public class VoucherUsage implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "usage_id")
    private Integer usageId;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "discount_amount")
    private BigDecimal discountAmount;
    @Column(name = "used_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date usedAt;
    @JoinColumn(name = "order_id", referencedColumnName = "order_id")
    @ManyToOne(optional = false)
    private Orders orderId;
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ManyToOne(optional = false)
    private Users userId;
    @JoinColumn(name = "voucher_id", referencedColumnName = "voucher_id")
    @ManyToOne(optional = false)
    private Vouchers voucherId;

    public VoucherUsage() {
    }

    public VoucherUsage(Integer usageId) {
        this.usageId = usageId;
    }

    public VoucherUsage(Integer usageId, BigDecimal discountAmount) {
        this.usageId = usageId;
        this.discountAmount = discountAmount;
    }

    public Integer getUsageId() {
        return usageId;
    }

    public void setUsageId(Integer usageId) {
        this.usageId = usageId;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Date getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(Date usedAt) {
        this.usedAt = usedAt;
    }

    public Orders getOrderId() {
        return orderId;
    }

    public void setOrderId(Orders orderId) {
        this.orderId = orderId;
    }

    public Users getUserId() {
        return userId;
    }

    public void setUserId(Users userId) {
        this.userId = userId;
    }

    public Vouchers getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(Vouchers voucherId) {
        this.voucherId = voucherId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (usageId != null ? usageId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof VoucherUsage)) {
            return false;
        }
        VoucherUsage other = (VoucherUsage) object;
        if ((this.usageId == null && other.usageId != null) || (this.usageId != null && !this.usageId.equals(other.usageId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.VoucherUsage[ usageId=" + usageId + " ]";
    }

}
