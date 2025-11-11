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
@Table(name = "Shops")
@NamedQueries({
    @NamedQuery(name = "Shops.findAll", query = "SELECT s FROM Shops s"),
    @NamedQuery(name = "Shops.findByShopId", query = "SELECT s FROM Shops s WHERE s.shopId = :shopId"),
    @NamedQuery(name = "Shops.findByShopName", query = "SELECT s FROM Shops s WHERE s.shopName = :shopName"),
    @NamedQuery(name = "Shops.findByShopBannerUrl", query = "SELECT s FROM Shops s WHERE s.shopBannerUrl = :shopBannerUrl"),
    @NamedQuery(name = "Shops.findByShopLogoUrl", query = "SELECT s FROM Shops s WHERE s.shopLogoUrl = :shopLogoUrl"),
    @NamedQuery(name = "Shops.findByContactPhone", query = "SELECT s FROM Shops s WHERE s.contactPhone = :contactPhone"),
    @NamedQuery(name = "Shops.findByContactEmail", query = "SELECT s FROM Shops s WHERE s.contactEmail = :contactEmail"),
    @NamedQuery(name = "Shops.findByShopAddress", query = "SELECT s FROM Shops s WHERE s.shopAddress = :shopAddress"),
    @NamedQuery(name = "Shops.findByShopDescription", query = "SELECT s FROM Shops s WHERE s.shopDescription = :shopDescription"),
    @NamedQuery(name = "Shops.findByShopType", query = "SELECT s FROM Shops s WHERE s.shopType = :shopType"),
    @NamedQuery(name = "Shops.findByStatus", query = "SELECT s FROM Shops s WHERE s.status = :status"),
    @NamedQuery(name = "Shops.findByCreatedAt", query = "SELECT s FROM Shops s WHERE s.createdAt = :createdAt"),
    @NamedQuery(name = "Shops.findByUpdatedAt", query = "SELECT s FROM Shops s WHERE s.updatedAt = :updatedAt")})
public class Shops implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "shop_id")
    private Integer shopId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "shop_name")
    private String shopName;
    @Size(max = 500)
    @Column(name = "shop_banner_url")
    private String shopBannerUrl;
    @Size(max = 500)
    @Column(name = "shop_logo_url")
    private String shopLogoUrl;
    @Size(max = 20)
    @Column(name = "contact_phone")
    private String contactPhone;
    @Size(max = 255)
    @Column(name = "contact_email")
    private String contactEmail;
    @Size(max = 500)
    @Column(name = "shop_address")
    private String shopAddress;
    @Size(max = 2147483647)
    @Column(name = "shop_description")
    private String shopDescription;
    @Size(max = 50)
    @Column(name = "shop_type")
    private String shopType;
    @Size(max = 50)
    @Column(name = "status")
    private String status;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shopId")
    private List<Products> productsList;
    @OneToMany(mappedBy = "shopId")
    private List<Vouchers> vouchersList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shopId")
    private List<Conversations> conversationsList;
    @JoinColumn(name = "seller_id", referencedColumnName = "user_id")
    @OneToOne(optional = false)
    private Users sellerId;

    public Shops() {
    }

    public Shops(Integer shopId) {
        this.shopId = shopId;
    }

    public Shops(Integer shopId, String shopName) {
        this.shopId = shopId;
        this.shopName = shopName;
    }

    public Integer getShopId() {
        return shopId;
    }

    public void setShopId(Integer shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopBannerUrl() {
        return shopBannerUrl;
    }

    public void setShopBannerUrl(String shopBannerUrl) {
        this.shopBannerUrl = shopBannerUrl;
    }

    public String getShopLogoUrl() {
        return shopLogoUrl;
    }

    public void setShopLogoUrl(String shopLogoUrl) {
        this.shopLogoUrl = shopLogoUrl;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public void setShopAddress(String shopAddress) {
        this.shopAddress = shopAddress;
    }

    public String getShopDescription() {
        return shopDescription;
    }

    public void setShopDescription(String shopDescription) {
        this.shopDescription = shopDescription;
    }

    public String getShopType() {
        return shopType;
    }

    public void setShopType(String shopType) {
        this.shopType = shopType;
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

    public List<Products> getProductsList() {
        return productsList;
    }

    public void setProductsList(List<Products> productsList) {
        this.productsList = productsList;
    }

    public List<Vouchers> getVouchersList() {
        return vouchersList;
    }

    public void setVouchersList(List<Vouchers> vouchersList) {
        this.vouchersList = vouchersList;
    }

    public List<Conversations> getConversationsList() {
        return conversationsList;
    }

    public void setConversationsList(List<Conversations> conversationsList) {
        this.conversationsList = conversationsList;
    }

    public Users getSellerId() {
        return sellerId;
    }

    public void setSellerId(Users sellerId) {
        this.sellerId = sellerId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (shopId != null ? shopId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Shops)) {
            return false;
        }
        Shops other = (Shops) object;
        if ((this.shopId == null && other.shopId != null) || (this.shopId != null && !this.shopId.equals(other.shopId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.Shops[ shopId=" + shopId + " ]";
    }

}
