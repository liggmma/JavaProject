package org.example.View;

import jakarta.persistence.*;
import org.example.Model.Customer;
import org.example.Model.Invoice;
import org.example.Model.Promotion;
import org.example.Repository.CustomerRepository;
import org.example.Repository.ICustomerRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("customerPU");
        EntityManager em = emf.createEntityManager();
        ICustomerRepository repo = new CustomerRepository(em);

        Scanner sc = new Scanner(System.in);
        int choice;

        try {
            do {
                System.out.println("\n===== MENU QUẢN LÝ KHÁCH HÀNG =====");
                System.out.println("1. Thêm khách hàng");
                System.out.println("2. Cập nhật khách hàng");
                System.out.println("3. Xóa khách hàng");
                System.out.println("4. Hiển thị tất cả khách hàng");
                System.out.println("5. Thêm khuyến mãi mẫu");
                System.out.println("6. Cập nhật chi tiết khách hàng (Invoices & Promotions)");
                System.out.println("0. Thoát");
                System.out.print("Chọn: ");
                choice = Integer.parseInt(sc.nextLine());

                switch (choice) {
                    case 1 -> {
                        System.out.print("Tên: ");
                        String name = sc.nextLine();
                        System.out.print("Email: ");
                        String email = sc.nextLine();
                        System.out.print("SĐT: ");
                        String phone = sc.nextLine();
                        System.out.print("Địa chỉ: ");
                        String address = sc.nextLine();

                        Customer c = new Customer(name, email, phone, address);

                        c.addInvoice(new Invoice(100.0, LocalDate.now(), "Invoice 1"));
                        c.addInvoice(new Invoice(200.0, LocalDate.now(), "Invoice 2"));

                        List<Promotion> promos = em.createQuery("SELECT p FROM Promotion p", Promotion.class).getResultList();
                        if (!promos.isEmpty()) {
                            c.addPromotion(promos.get(0));
                        }

                        repo.addCustomer(c);
                        System.out.println("Đã thêm khách hàng.");
                    }
                    case 2 -> {
                        System.out.print("Nhập ID khách hàng cần cập nhật: ");
                        int id = Integer.parseInt(sc.nextLine());
                        Customer existing = repo.getCustomer(id);
                        if (existing != null) {
                            System.out.print("Tên mới: ");
                            existing.setName(sc.nextLine());
                            System.out.print("Email mới: ");
                            existing.setEmail(sc.nextLine());
                            System.out.print("SĐT mới: ");
                            existing.setPhone(sc.nextLine());
                            System.out.print("Địa chỉ mới: ");
                            existing.setAddress(sc.nextLine());

                            existing.addInvoice(new Invoice(75.0, LocalDate.now(), "Invoice Update"));

                            repo.updateCustomer(existing);
                            System.out.println("Đã cập nhật khách hàng.");
                        } else {
                            System.out.println("Không tìm thấy khách hàng.");
                        }
                    }
                    case 3 -> {
                        System.out.print("Nhập ID khách hàng cần xóa: ");
                        int id = Integer.parseInt(sc.nextLine());
                        repo.deleteCustomer(id);
                        System.out.println("Đã xóa khách hàng.");
                    }
                    case 4 -> {
                        printAllCustomers(repo);
                    }
                    case 5 -> {
                        EntityTransaction tx = em.getTransaction();
                        tx.begin();
                        Promotion promo1 = new Promotion("Summer Sale", "10% off", LocalDate.now(), LocalDate.now().plusDays(30), 10.0);
                        Promotion promo2 = new Promotion("VIP Deal", "20% off for VIPs", LocalDate.now(), LocalDate.now().plusDays(15), 20.0);
                        Promotion promo3 = new Promotion("Black Friday", "50% off", LocalDate.now(), LocalDate.now().plusDays(3), 50.0);
                        em.persist(promo1);
                        em.persist(promo2);
                        em.persist(promo3);
                        tx.commit();
                        System.out.println("Đã thêm 3 khuyến mãi mẫu.");
                    }
                    case 6 -> {
                        System.out.print("Nhập ID khách hàng cần cập nhật chi tiết: ");
                        int id = Integer.parseInt(sc.nextLine());
                        Customer existing = repo.getCustomer(id);

                        if (existing != null) {
                            Customer updatedData = new Customer();
                            updatedData.setId(id);

                            // ====== Invoices ======
                            System.out.println("\n--- Danh sách Invoice hiện có ---");
                            existing.getInvoices().forEach(inv ->
                                    System.out.println(inv.getId() + " | " + inv.getAmount() + " | " + inv.getDescription()));

                            System.out.print("Bạn muốn thêm bao nhiêu Invoice mới? ");
                            int newInvCount = Integer.parseInt(sc.nextLine());
                            for (int i = 0; i < newInvCount; i++) {
                                System.out.print("Số tiền: ");
                                double amount = Double.parseDouble(sc.nextLine());
                                System.out.print("Mô tả: ");
                                String desc = sc.nextLine();
                                updatedData.addInvoice(new Invoice(amount, LocalDate.now(), desc));
                            }

                            System.out.print("Bạn có muốn chỉnh sửa Invoice cũ không? (y/n): ");
                            if (sc.nextLine().equalsIgnoreCase("y")) {
                                System.out.print("Nhập ID invoice cần sửa: ");
                                int invId = Integer.parseInt(sc.nextLine());
                                Invoice oldInv = existing.getInvoices().stream()
                                        .filter(inv -> inv.getId() == invId)
                                        .findFirst()
                                        .orElse(null);
                                if (oldInv != null) {
                                    System.out.print("Số tiền mới: ");
                                    double amount = Double.parseDouble(sc.nextLine());
                                    System.out.print("Mô tả mới: ");
                                    String desc = sc.nextLine();

                                    Invoice updatedInv = new Invoice();
                                    updatedInv.setId(oldInv.getId());
                                    updatedInv.setAmount(amount);
                                    updatedInv.setDate(LocalDate.now());
                                    updatedInv.setDescription(desc);
                                    updatedData.addInvoice(updatedInv);
                                }
                            }

                            // ====== Promotions ======
                            System.out.println("\n--- Danh sách Promotion hiện có ---");
                            List<Promotion> promos = em.createQuery("SELECT p FROM Promotion p", Promotion.class).getResultList();
                            promos.forEach(p -> System.out.println(p.getId() + ": " + p.getTitle()));

                            System.out.print("Nhập danh sách ID promotion muốn gán (phân cách bằng dấu phẩy, hoặc để trống): ");
                            String promoInput = sc.nextLine();
                            if (!promoInput.isBlank()) {
                                for (String pid : promoInput.split(",")) {
                                    int promoId = Integer.parseInt(pid.trim());
                                    Promotion pro = em.find(Promotion.class, promoId);
                                    if (pro != null) {
                                        updatedData.addPromotion(pro);
                                    }
                                }
                            }

                            // ====== Gọi repo cập nhật ======
                            repo.updateCustomerDetails(id, updatedData);
                            System.out.println("✅ Đã cập nhật chi tiết khách hàng.");
                        } else {
                            System.out.println("❌ Không tìm thấy khách hàng.");
                        }
                    }


                    case 0 -> System.out.println(" Thoát chương trình.");
                    default -> System.out.println("Lựa chọn không hợp lệ.");
                }
            } while (choice != 0);
        } finally {
            em.close();
            emf.close();
            sc.close();
        }
    }

    private static void printAllCustomers(ICustomerRepository repo) {
        List<Customer> list = repo.getAllCustomers();
        for (Customer c : list) {
            System.out.println(c);
            c.getInvoices().forEach(inv -> System.out.println("   " + inv));
            c.getPromotions().forEach(p -> System.out.println("   " + p));
        }
    }
}

