package org.example.erp.modules.partners.domain;

import lombok.Getter;

@Getter
public final class Partner {
    private final Long id;
    private String name;
    private PartnerType type;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String taxId;
    private boolean active;

    private Partner(Long id, String name, PartnerType type, String contactPerson, String phone, String email,
                    String address, String taxId, boolean active) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.taxId = taxId;
        this.active = active;
    }

    public static Partner register(String name, PartnerType type, String contactPerson, String phone, String email,
                                   String address, String taxId) {
        return new Partner(null, name, type, contactPerson, phone, email, address, taxId, true);
    }

    public static Partner restore(Long id, String name, PartnerType type, String contactPerson, String phone,
                                  String email, String address, String taxId, boolean active) {
        return new Partner(id, name, type, contactPerson, phone, email, address, taxId, active);
    }

    public void update(String name, PartnerType type, String contactPerson, String phone, String email,
                       String address, String taxId, boolean active) {
        this.name = name;
        this.type = type;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.taxId = taxId;
        this.active = active;
    }

    public boolean isCustomer() {
        return type == PartnerType.CUSTOMER || type == PartnerType.BOTH;
    }

    public boolean isSupplier() {
        return type == PartnerType.SUPPLIER || type == PartnerType.BOTH;
    }
}
