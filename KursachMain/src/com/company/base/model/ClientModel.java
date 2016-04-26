package com.company.base.model;

public class ClientModel implements Comparable<ClientModel> {

    protected String name;
    protected String password = "";

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public int compareTo(ClientModel o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientModel that = (ClientModel) o;

        if (!name.equals(that.name)) return false;
        return password.equals(that.password);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + password.hashCode();
        return result;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
