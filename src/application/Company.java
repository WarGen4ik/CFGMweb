package application;

/**
 * Created by Admin on 04.07.2017.
 */
public class Company {
    private String name;
    private String address;
    private String phone_number;
    private String form_phone_number;
    private String website;
    private String country;
    private String query;

    public Company(String[] info){
        name =                  info[0];
        address =               info[1];
        phone_number =          info[2];
        form_phone_number =     info[3];
        website =               info[4];
        country =               info[5];
        query =                 info[6];
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getForm_phone_number() {
        return form_phone_number;
    }

    public void setForm_phone_number(String form_phone_number) {
        this.form_phone_number = form_phone_number;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
