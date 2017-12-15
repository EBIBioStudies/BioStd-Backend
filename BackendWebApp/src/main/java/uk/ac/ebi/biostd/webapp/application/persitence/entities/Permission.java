package uk.ac.ebi.biostd.webapp.application.persitence.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "PermissionProfile")
public class Permission {

    public static final String SUBMIT_PERMISSION = "Edit Submission";
    public static final String READ_PERMISSION = "View Submission";

    @Id
    @GeneratedValue
    private long id;
    private String description;
    private String name;
}
