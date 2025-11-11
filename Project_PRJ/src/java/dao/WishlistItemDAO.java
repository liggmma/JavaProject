package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.Date;
import model.Products;
import model.WishlistItems;
import model.Wishlists;

public class WishlistItemDAO extends GenericDAO<WishlistItems> {

    private final EntityManagerFactory emf;

    public WishlistItemDAO() {
        super(WishlistItems.class);
        this.emf = EntityManagerFactoryInit.getEntityManagerFactory();
    }

}
