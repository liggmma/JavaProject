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
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ADMIN
 */
@Entity
@Table(name = "Wallets")
@NamedQueries({
    @NamedQuery(name = "Wallets.findAll", query = "SELECT w FROM Wallets w"),
    @NamedQuery(name = "Wallets.findByWalletId", query = "SELECT w FROM Wallets w WHERE w.walletId = :walletId"),
    @NamedQuery(name = "Wallets.findByBalance", query = "SELECT w FROM Wallets w WHERE w.balance = :balance"),
    @NamedQuery(name = "Wallets.findByCreatedAt", query = "SELECT w FROM Wallets w WHERE w.createdAt = :createdAt"),
    @NamedQuery(name = "Wallets.findByUpdatedAt", query = "SELECT w FROM Wallets w WHERE w.updatedAt = :updatedAt")})
public class Wallets implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "wallet_id")
    private Integer walletId;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "balance")
    private BigDecimal balance;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @OneToMany(mappedBy = "walletId")
    private List<WalletTransactions> walletTransactionsList;
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @OneToOne
    private Users userId;

    public Wallets() {
    }

    public Wallets(Integer walletId) {
        this.walletId = walletId;
    }

    public Integer getWalletId() {
        return walletId;
    }

    public void setWalletId(Integer walletId) {
        this.walletId = walletId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
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

    public List<WalletTransactions> getWalletTransactionsList() {
        return walletTransactionsList;
    }

    public void setWalletTransactionsList(List<WalletTransactions> walletTransactionsList) {
        this.walletTransactionsList = walletTransactionsList;
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
        hash += (walletId != null ? walletId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Wallets)) {
            return false;
        }
        Wallets other = (Wallets) object;
        if ((this.walletId == null && other.walletId != null) || (this.walletId != null && !this.walletId.equals(other.walletId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.Wallets[ walletId=" + walletId + " ]";
    }

}
