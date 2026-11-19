package org.route;

public record Customer(String fullName, String phone, String email) {
    public Customer {
        if (fullName == null || fullName.isBlank()) {
            throw new StoreException("Customer Full Name is required.");
        }
        fullName = fullName.trim();
        phone = phone.trim();
        email = email == null || email.isBlank() ? null : email.trim();
    }
}
