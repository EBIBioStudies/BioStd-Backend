package uk.ac.ebi.biostd.authz;

public interface PermissionACR extends ACR {

    @Override
    Permission getPermissionUnit();
}
