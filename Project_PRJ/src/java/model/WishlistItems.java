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
@Table(name = "Wishlist_Items")
@NamedQueries({
    @NamedQuery(name = "WishlistItems.findAll", query = "SELECT w FROM WishlistItems w"),
    @NamedQuery(name = "WishlistItems.findByWishlistItemId", query = "SELECT w FROM WishlistItems w WHERE w.wishlistItemId = :wishlistItemId"),
    @NamedQuery(name = "WishlistItems.findByAddedAt", query = "SELECT w FROM WishlistItems w WHERE w.addedAt = :addedAt")})
public class WishlistItems implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "wishlist_item_id")
    private Integer wishlistItemId;
    @Column(name = "added_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date addedAt;
    @JoinColumn(name = "product_id", referencedColumnName = "product_id")
    @ManyToOne(optional = false)
    private Products productId;
    @JoinColumn(name = "wishlist_id", referencedColumnName = "wishlist_id")
    @ManyToOne(optional = false)
    private Wishlists wishlistId;

    public WishlistItems() {
    }

    public WishlistItems(Date addedAt, Products p, Wishlists w) {
        this.addedAt = addedAt;
        this.productId = p;
        this.wishlistId = w;
    }

    public Integer getWishlistItemId() {
        return wishlistItemId;
    }

    public void setWishlistItemId(Integer wishlistItemId) {
        this.wishlistItemId = wishlistItemId;
    }

    public Date getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Date addedAt) {
        this.addedAt = addedAt;
    }

    public Products getProductId() {
        return productId;
    }

    public void setProductId(Products productId) {
        this.productId = productId;
    }

    public Wishlists getWishlistId() {
        return wishlistId;
    }

    public void setWishlistId(Wishlists wishlistId) {
        this.wishlistId = wishlistId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (wishlistItemId != null ? wishlistItemId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof WishlistItems)) {
            return false;
        }
        WishlistItems other = (WishlistItems) object;
        if ((this.wishlistItemId == null && other.wishlistItemId != null) || (this.wishlistItemId != null && !this.wishlistItemId.equals(other.wishlistItemId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.WishlistItems[ wishlistItemId=" + wishlistItemId + " ]";
    }

}
