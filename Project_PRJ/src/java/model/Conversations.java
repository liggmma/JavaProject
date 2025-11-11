/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import jakarta.persistence.Basic;
import jakarta.persistence.Cacheable;
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
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ADMIN
 */
@Entity
@Cacheable(false)
@Table(name = "Conversations")
@NamedQueries({
    @NamedQuery(name = "Conversations.findAll", query = "SELECT c FROM Conversations c"),
    @NamedQuery(name = "Conversations.findByConversationId", query = "SELECT c FROM Conversations c WHERE c.conversationId = :conversationId"),
    @NamedQuery(name = "Conversations.findByLastMessageAt", query = "SELECT c FROM Conversations c WHERE c.lastMessageAt = :lastMessageAt"),
    @NamedQuery(name = "Conversations.findByCreatedAt", query = "SELECT c FROM Conversations c WHERE c.createdAt = :createdAt")})
public class Conversations implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "conversation_id")
    private Integer conversationId;
    @Column(name = "last_message_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastMessageAt;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "conversationId")
    private List<Messages> messagesList;
    @JoinColumn(name = "shop_id", referencedColumnName = "shop_id")
    @ManyToOne(optional = false)
    private Shops shopId;
    @JoinColumn(name = "customer_id", referencedColumnName = "user_id")
    @ManyToOne(optional = false)
    private Users customerId;

    public Conversations() {
    }

    public Conversations(Integer conversationId) {
        this.conversationId = conversationId;
    }

    public Integer getConversationId() {
        return conversationId;
    }

    public void setConversationId(Integer conversationId) {
        this.conversationId = conversationId;
    }

    public Date getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(Date lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<Messages> getMessagesList() {
        return messagesList;
    }

    public void setMessagesList(List<Messages> messagesList) {
        this.messagesList = messagesList;
    }

    public Shops getShopId() {
        return shopId;
    }

    public void setShopId(Shops shopId) {
        this.shopId = shopId;
    }

    public Users getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Users customerId) {
        this.customerId = customerId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (conversationId != null ? conversationId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Conversations)) {
            return false;
        }
        Conversations other = (Conversations) object;
        if ((this.conversationId == null && other.conversationId != null) || (this.conversationId != null && !this.conversationId.equals(other.conversationId))) {
            return false;
        }
        return true;
    }

    public int getUserUnReadCount() {
        int unReadCount = 0;
        for (Messages m : messagesList) {
            if (m.getSenderType().equalsIgnoreCase("user") && !m.getIsRead()) {
                unReadCount++;
            }
        }
        return unReadCount;
    }

    public int getShopUnReadCount() {
        int unReadCount = 0;
        for (Messages m : messagesList) {
            if (m.getSenderType().equalsIgnoreCase("shop") && !m.getIsRead()) {
                unReadCount++;
            }
        }
        return unReadCount;
    }

    @Override
    public String toString() {
        return "model.Conversations[ conversationId=" + conversationId + " ]";
    }

}
