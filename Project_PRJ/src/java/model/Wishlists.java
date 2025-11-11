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
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ADMIN
 */
@Entity
@Table(name = "Wishlists")
@NamedQueries({
    @NamedQuery(name = "Wishlists.findAll", query = "SELECT w FROM Wishlists w"),
    @NamedQuery(name = "Wishlists.findByWishlistId", query = "SELECT w FROM Wishlists w WHERE w.wishlistId = :wishlistId"),
    @NamedQuery(name = "Wishlists.findByCreatedAt", query = "SELECT w FROM Wishlists w WHERE w.createdAt = :createdAt")})
public class Wishlists implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "wishlist_id")
    private Integer wishlistId;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @JoinColumn(name = "customer_id", referencedColumnName = "user_id")
    @OneToOne(optional = false)
    private Users customerId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "wishlistId")
    private List<WishlistItems> wishlistItemsList;

    public Wishlists() {
    }

    public Wishlists(Integer wishlistId) {
        this.wishlistId = wishlistId;
    }

    public Integer getWishlistId() {
        return wishlistId;
    }

    public void setWishlistId(Integer wishlistId) {
        this.wishlistId = wishlistId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Users getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Users customerId) {
        this.customerId = customerId;
    }

    public List<WishlistItems> getWishlistItemsList() {
        return wishlistItemsList;
    }

    public void setWishlistItemsList(List<WishlistItems> wishlistItemsList) {
        this.wishlistItemsList = wishlistItemsList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (wishlistId != null ? wishlistId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Wishlists)) {
            return false;
        }
        Wishlists other = (Wishlists) object;
        if ((this.wishlistId == null && other.wishlistId != null) || (this.wishlistId != null && !this.wishlistId.equals(other.wishlistId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.Wishlists[ wishlistId=" + wishlistId + " ]";
    }

}
