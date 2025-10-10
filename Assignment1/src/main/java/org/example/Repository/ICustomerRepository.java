package org.example.Repository;

import org.example.Model.Customer;
import java.util.List;

public interface ICustomerRepository {
    Customer addCustomer(Customer c);
    Customer updateCustomer(Customer c);
    Customer updateCustomerDetails(int customerId, Customer updatedData);
    Customer getCustomer(int id);
    List<Customer> getAllCustomers();
    void deleteCustomer(int id);
}


