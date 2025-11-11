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
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author ADMIN
 */
@Entity
@Table(name = "Wallet_Transactions")
@NamedQueries({
    @NamedQuery(name = "WalletTransactions.findAll", query = "SELECT w FROM WalletTransactions w"),
    @NamedQuery(name = "WalletTransactions.findByTransactionId", query = "SELECT w FROM WalletTransactions w WHERE w.transactionId = :transactionId"),
    @NamedQuery(name = "WalletTransactions.findByTransactionType", query = "SELECT w FROM WalletTransactions w WHERE w.transactionType = :transactionType"),
    @NamedQuery(name = "WalletTransactions.findByAmount", query = "SELECT w FROM WalletTransactions w WHERE w.amount = :amount"),
    @NamedQuery(name = "WalletTransactions.findByBalanceAfter", query = "SELECT w FROM WalletTransactions w WHERE w.balanceAfter = :balanceAfter"),
    @NamedQuery(name = "WalletTransactions.findByTransactionDate", query = "SELECT w FROM WalletTransactions w WHERE w.transactionDate = :transactionDate"),
    @NamedQuery(name = "WalletTransactions.findByDescription", query = "SELECT w FROM WalletTransactions w WHERE w.description = :description"),
    @NamedQuery(name = "WalletTransactions.findByStatus", query = "SELECT w FROM WalletTransactions w WHERE w.status = :status")})
public class WalletTransactions implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "transaction_id")
    private Integer transactionId;
    @Size(max = 50)
    @Column(name = "transaction_type")
    private String transactionType;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "amount")
    private BigDecimal amount;
    @Column(name = "balance_after")
    private BigDecimal balanceAfter;
    @Column(name = "transaction_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionDate;
    @Size(max = 2147483647)
    @Column(name = "description")
    private String description;
    @Size(max = 50)
    @Column(name = "status")
    private String status;
    @JoinColumn(name = "wallet_id", referencedColumnName = "wallet_id")
    @ManyToOne
    private Wallets walletId;

    public WalletTransactions() {
    }

    public WalletTransactions(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public WalletTransactions(Integer transactionId, BigDecimal amount) {
        this.transactionId = transactionId;
        this.amount = amount;
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Wallets getWalletId() {
        return walletId;
    }

    public void setWalletId(Wallets walletId) {
        this.walletId = walletId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (transactionId != null ? transactionId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof WalletTransactions)) {
            return false;
        }
        WalletTransactions other = (WalletTransactions) object;
        if ((this.transactionId == null && other.transactionId != null) || (this.transactionId != null && !this.transactionId.equals(other.transactionId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.WalletTransactions[ transactionId=" + transactionId + " ]";
    }

}
