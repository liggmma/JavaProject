package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import java.util.Date;
import java.util.List;
import model.CartItems;
import model.Carts;
import model.Products;

public class CartItemDAO extends GenericDAO<CartItems> {

    private final EntityManagerFactory emf;

    public CartItemDAO() {
        super(CartItems.class);
        this.emf = EntityManagerFactoryInit.getEntityManagerFactory();
    }

    public CartItems findByCartAndProduct(Carts cart, Products product) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT ci FROM CartItems ci WHERE ci.cartId = :cart AND ci.productId = :product", CartItems.class)
                    .setParameter("cart", cart)
                    .setParameter("product", product)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public void addOrUpdateItemWithQuantity(Carts cart, Products product, int quantity) {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            // Tìm sản phẩm đã có trong giỏ hàng chưa
            String jpql = "SELECT ci FROM CartItems ci WHERE ci.cartId = :cart AND ci.productId = :product";
            List<CartItems> result = em.createQuery(jpql, CartItems.class)
                    .setParameter("cart", cart)
                    .setParameter("product", product)
                    .getResultList();

            CartItems item;
            if (!result.isEmpty()) {
                item = result.get(0);
                int newQuantity = item.getQuantity() + quantity;
                item.setQuantity(Math.min(newQuantity, product.getStockQuantity()));
                em.merge(item);
            } else {
                item = new CartItems();
                item.setCartId(cart);
                item.setProductId(product);
                item.setQuantity(Math.min(quantity, product.getStockQuantity()));
                item.setAddedAt(new Date()); // ✅ Gán ngày giờ thêm mới
                em.persist(item);
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
