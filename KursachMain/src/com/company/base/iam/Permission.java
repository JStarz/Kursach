package com.company.base.iam;

public class Permission {

    String permission;

    public Permission(String perm) {
        this.permission = perm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Permission that = (Permission) o;

        return permission.equals(that.permission);

    }

    @Override
    public int hashCode() {
        return permission.hashCode();
    }
}
