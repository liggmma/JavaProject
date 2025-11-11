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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ADMIN
 */
@Entity
@Table(name = "Products")
@NamedQueries({
    @NamedQuery(name = "Products.findAll", query = "SELECT p FROM Products p"),
    @NamedQuery(name = "Products.findByProductId", query = "SELECT p FROM Products p WHERE p.productId = :productId"),
    @NamedQuery(name = "Products.findBySku", query = "SELECT p FROM Products p WHERE p.sku = :sku"),
    @NamedQuery(name = "Products.findByName", query = "SELECT p FROM Products p WHERE p.name = :name"),
    @NamedQuery(name = "Products.findByDescription", query = "SELECT p FROM Products p WHERE p.description = :description"),
    @NamedQuery(name = "Products.findByBasePrice", query = "SELECT p FROM Products p WHERE p.basePrice = :basePrice"),
    @NamedQuery(name = "Products.findByTags", query = "SELECT p FROM Products p WHERE p.tags = :tags"),
    @NamedQuery(name = "Products.findByWeight", query = "SELECT p FROM Products p WHERE p.weight = :weight"),
    @NamedQuery(name = "Products.findByStockQuantity", query = "SELECT p FROM Products p WHERE p.stockQuantity = :stockQuantity"),
    @NamedQuery(name = "Products.findBySoldQuantity", query = "SELECT p FROM Products p WHERE p.soldQuantity = :soldQuantity"),
    @NamedQuery(name = "Products.findByStatus", query = "SELECT p FROM Products p WHERE p.status = :status"),
    @NamedQuery(name = "Products.findByCreatedAt", query = "SELECT p FROM Products p WHERE p.createdAt = :createdAt"),
    @NamedQuery(name = "Products.findByUpdatedAt", query = "SELECT p FROM Products p WHERE p.updatedAt = :updatedAt")})
public class Products implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "product_id")
    private Integer productId;
    @Size(max = 100)
    @Column(name = "sku")
    private String sku;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "name")
    private String name;
    @Size(max = 2147483647)
    @Column(name = "description")
    private String description;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "base_price")
    private BigDecimal basePrice;
    @Size(max = 2147483647)
    @Column(name = "tags")
    private String tags;
    @Column(name = "weight")
    private BigDecimal weight;
    @Column(name = "stock_quantity")
    private Integer stockQuantity;
    @Column(name = "sold_quantity")
    private Integer soldQuantity;
    @Size(max = 50)
    @Column(name = "status")
    private String status;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @JoinColumn(name = "category_id", referencedColumnName = "category_id")
    @ManyToOne(optional = false)
    private Categories categoryId;
    @JoinColumn(name = "shop_id", referencedColumnName = "shop_id")
    @ManyToOne(optional = false)
    private Shops shopId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "productId")
    private List<ProductImages> productImagesList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "productId")
    private List<Reviews> reviewsList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "productId")
    private List<CartItems> cartItemsList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "productId")
    private List<ProductViewHistory> productViewHistoryList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "productId")
    private List<WishlistItems> wishlistItemsList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "productId")
    private List<OrderItems> orderItemsList;

    public Products() {
    }

    public Products(Integer productId) {
        this.productId = productId;
    }

    public Products(Integer productId, String name) {
        this.productId = productId;
        this.name = name;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Integer getSoldQuantity() {
        return soldQuantity;
    }

    public void setSoldQuantity(Integer soldQuantity) {
        this.soldQuantity = soldQuantity;
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

    public Categories getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Categories categoryId) {
        this.categoryId = categoryId;
    }

    public Shops getShopId() {
        return shopId;
    }

    public void setShopId(Shops shopId) {
        this.shopId = shopId;
    }

    public List<ProductImages> getProductImagesList() {
        Collections.sort(productImagesList, new Comparator<ProductImages>() {
            @Override
            public int compare(ProductImages o1, ProductImages o2) {
                return Integer.compare(o1.getDisplayOrder(), o2.getDisplayOrder());
            }
        });
        return productImagesList;
    }

    public void setProductImagesList(List<ProductImages> productImagesList) {
        this.productImagesList = productImagesList;
    }

    public List<Reviews> getReviewsList() {
        return reviewsList;
    }

    public void setReviewsList(List<Reviews> reviewsList) {
        this.reviewsList = reviewsList;
    }

    public List<CartItems> getCartItemsList() {
        return cartItemsList;
    }

    public void setCartItemsList(List<CartItems> cartItemsList) {
        this.cartItemsList = cartItemsList;
    }

    public List<ProductViewHistory> getProductViewHistoryList() {
        return productViewHistoryList;
    }

    public void setProductViewHistoryList(List<ProductViewHistory> productViewHistoryList) {
        this.productViewHistoryList = productViewHistoryList;
    }

    public List<WishlistItems> getWishlistItemsList() {
        return wishlistItemsList;
    }

    public void setWishlistItemsList(List<WishlistItems> wishlistItemsList) {
        this.wishlistItemsList = wishlistItemsList;
    }

    public List<OrderItems> getOrderItemsList() {
        return orderItemsList;
    }

    public void setOrderItemsList(List<OrderItems> orderItemsList) {
        this.orderItemsList = orderItemsList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (productId != null ? productId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Products)) {
            return false;
        }
        Products other = (Products) object;
        if ((this.productId == null && other.productId != null) || (this.productId != null && !this.productId.equals(other.productId))) {
            return false;
        }
        return true;
    }

    public double getAverageRating() {
        if (reviewsList == null || reviewsList.isEmpty()) {
            return 0.0;
        }
        int total = 0;
        for (Reviews review : reviewsList) {
            total += review.getRating();
        }
        return (double) total / reviewsList.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Products{");
        sb.append("productId=").append(productId);
        sb.append(", sku=").append(sku);
        sb.append(", name=").append(name);
        sb.append(", description=").append(description);
        sb.append(", basePrice=").append(basePrice);
        sb.append(", tags=").append(tags);
        sb.append(", weight=").append(weight);
        sb.append(", stockQuantity=").append(stockQuantity);
        sb.append(", soldQuantity=").append(soldQuantity);
        sb.append(", status=").append(status);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", categoryId=").append(categoryId);
        sb.append(", shopId=").append(shopId);
        sb.append(", productImagesList=").append(productImagesList);
        sb.append(", reviewsList=").append(reviewsList);
        sb.append(", cartItemsList=").append(cartItemsList);
        sb.append(", productViewHistoryList=").append(productViewHistoryList);
        sb.append(", wishlistItemsList=").append(wishlistItemsList);
        sb.append(", orderItemsList=").append(orderItemsList);
        sb.append('}');
        return sb.toString();
    }

}
