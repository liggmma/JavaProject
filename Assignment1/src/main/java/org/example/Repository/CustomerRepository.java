package org.example.Repository;

import org.example.DAO.CustomerDao;
import org.example.Model.Customer;
import jakarta.persistence.EntityManager;

import java.util.List;

public class CustomerRepository implements ICustomerRepository {
    private final CustomerDao dao;

    public CustomerRepository(EntityManager em) {
        this.dao = new CustomerDao(em);
    }

    @Override
    public Customer addCustomer(Customer c) {
        return dao.addCustomer(c);
    }

    @Override
    public Customer updateCustomer(Customer c) {
        return dao.updateCustomer(c);
    }

    @Override
    public Customer updateCustomerDetails(int customerId, Customer updatedData) {
        return dao.updateCustomerDetails(customerId, updatedData);
    }


    @Override
    public Customer getCustomer(int id) {
        return dao.getCustomer(id);
    }

    @Override
    public List<Customer> getAllCustomers() {
        return dao.getAllCustomers();
    }

    @Override
    public void deleteCustomer(int id) {
        dao.deleteCustomer(id);
    }
}

