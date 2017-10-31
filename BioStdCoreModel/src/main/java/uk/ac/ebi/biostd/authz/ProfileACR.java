package uk.ac.ebi.biostd.authz;

public interface ProfileACR extends ACR {

    @Override
    PermissionProfile getPermissionUnit();
}
