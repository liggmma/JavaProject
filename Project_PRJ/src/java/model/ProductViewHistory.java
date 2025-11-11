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
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author ADMIN
 */
@Entity
@Table(name = "Product_View_History")
@NamedQueries({
    @NamedQuery(name = "ProductViewHistory.findAll", query = "SELECT p FROM ProductViewHistory p"),
    @NamedQuery(name = "ProductViewHistory.findByViewId", query = "SELECT p FROM ProductViewHistory p WHERE p.viewId = :viewId"),
    @NamedQuery(name = "ProductViewHistory.findByViewedAt", query = "SELECT p FROM ProductViewHistory p WHERE p.viewedAt = :viewedAt")})
public class ProductViewHistory implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "view_id")
    private Integer viewId;
    @Column(name = "viewed_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date viewedAt;
    @JoinColumn(name = "product_id", referencedColumnName = "product_id")
    @ManyToOne(optional = false)
    private Products productId;
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ManyToOne(optional = false)
    private Users userId;

    public ProductViewHistory() {
    }

    public ProductViewHistory(Date viewedAt, Products p, Users u) {
        this.viewedAt = viewedAt;
        this.productId = p;
        this.userId = u;
    }

    public Integer getViewId() {
        return viewId;
    }

    public void setViewId(Integer viewId) {
        this.viewId = viewId;
    }

    public Date getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(Date viewedAt) {
        this.viewedAt = viewedAt;
    }

    public Products getProductId() {
        return productId;
    }

    public void setProductId(Products productId) {
        this.productId = productId;
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
        hash += (viewId != null ? viewId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ProductViewHistory)) {
            return false;
        }
        ProductViewHistory other = (ProductViewHistory) object;
        if ((this.viewId == null && other.viewId != null) || (this.viewId != null && !this.viewId.equals(other.viewId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.ProductViewHistory[ viewId=" + viewId + " ]";
    }

}
