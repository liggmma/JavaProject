/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.GenericDAO;
import model.Addresses;

/**
 *
 * @author ADMIN
 */
public class AddressService {

    GenericDAO<Addresses> addressDAO;

    public AddressService() {
        this.addressDAO = new GenericDAO<>(Addresses.class);
    }

    public Addresses findById(int id) {
        return addressDAO.findById(id);
    }

    public void add(Addresses a) {
        addressDAO.add(a);
    }

    public void delete(int id) {
        addressDAO.delete(id);
    }

    public void update(int id, String fullName, String phoneNumber, String addressLine, boolean isDefault) {
        Addresses a = findById(id);

        a.setFullName(fullName);
        a.setPhoneNumber(phoneNumber);
        a.setAddressLine(addressLine);
        a.setIsDefault(isDefault);

        addressDAO.update(a);
    }

}
