/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.GenericDAO;
import model.Wallets;

/**
 *
 * @author ADMIN
 */
public class WalletService {

    GenericDAO<Wallets> walletDAO;

    public WalletService() {
        this.walletDAO = new GenericDAO<>(Wallets.class);
    }

    public Wallets findById(int id) {
        return walletDAO.findById(id);
    }

    public void add(Wallets a) {
        walletDAO.add(a);
    }

    public void delete(int id) {
        walletDAO.delete(id);
    }

}
