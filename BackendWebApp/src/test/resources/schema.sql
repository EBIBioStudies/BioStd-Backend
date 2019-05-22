CREATE TABLE AccessTag (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    description   VARCHAR(255) NULL,
    name          VARCHAR(255) NULL,
    owner_id      BIGINT       NULL,
    parent_tag_id BIGINT       NULL,
    CONSTRAINT access_tag_name_idx UNIQUE (name),
    CONSTRAINT FK5q8ww38t2jsgfy73t0e12am52 FOREIGN KEY (parent_tag_id) REFERENCES AccessTag (id)
);

CREATE INDEX FK5q8ww38t2jsgfy73t0e12am52 ON AccessTag (parent_tag_id);
CREATE INDEX FKc4d73kr6bf92cyioruykxptr0 ON AccessTag (owner_id);

CREATE TABLE AttributeSubscription (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    attribute VARCHAR(255) NULL,
    pattern   VARCHAR(255) NULL,
    user_id   BIGINT       NULL
);

CREATE INDEX attribute_index ON AttributeSubscription (attribute);
CREATE INDEX FKh8qytl1fxkrml09gigq7wf3et ON AttributeSubscription (user_id);

CREATE TABLE AttributeSubscriptionMatchEvent (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id   BIGINT NULL,
    subscription_id BIGINT NULL,
    user_id         BIGINT NULL,
    CONSTRAINT FKsewo0cljsku9qup7v98uy5kbf
    FOREIGN KEY (subscription_id) REFERENCES AttributeSubscription (id)
);

CREATE INDEX FK7h9hi0w70d3hyuc9sqa7tya7o ON AttributeSubscriptionMatchEvent (user_id);
CREATE INDEX FKb13xop3ik6im4c9ibudfx7pu0 ON AttributeSubscriptionMatchEvent (submission_id);
CREATE INDEX FKsewo0cljsku9qup7v98uy5kbf ON AttributeSubscriptionMatchEvent (subscription_id);

CREATE TABLE AuthorizationTemplate (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    className VARCHAR(255) NULL,
    CONSTRAINT classname_index UNIQUE (className)
);

CREATE TABLE Classifier (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) NULL,
    name        VARCHAR(255) NULL,
    CONSTRAINT classifier_name_idx UNIQUE (name)
);

CREATE TABLE Counter (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    maxCount BIGINT       NOT NULL,
    name     VARCHAR(255) NULL,
    CONSTRAINT counter_name_idx UNIQUE (name)
);

CREATE TABLE CounterPermGrpACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    action     VARCHAR(255) NULL,
    allow      BIT          NOT NULL,
    subject_id BIGINT       NULL,
    host_id    BIGINT       NULL,
    CONSTRAINT FKk7q0jo3p8uhq3stg0ibgevsgm
    FOREIGN KEY (host_id) REFERENCES Counter (id)
);

CREATE INDEX FK5sf1bgxd91o0q80xdqqkecn8g ON CounterPermGrpACR (subject_id);

CREATE INDEX FKk7q0jo3p8uhq3stg0ibgevsgm ON CounterPermGrpACR (host_id);

CREATE TABLE CounterPermUsrACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    action     VARCHAR(255) NULL,
    allow      BIT          NOT NULL,
    subject_id BIGINT       NULL,
    host_id    BIGINT       NULL,
    CONSTRAINT FKn60xb9mdy39tehhbn32ajs7t4
    FOREIGN KEY (host_id) REFERENCES Counter (id)
);

CREATE INDEX FK3j5d4i1yqh2xunjg3el23mu9m ON CounterPermUsrACR (subject_id);
CREATE INDEX FKn60xb9mdy39tehhbn32ajs7t4 ON CounterPermUsrACR (host_id);

CREATE TABLE CounterProfGrpACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NULL,
    subject_id BIGINT NULL,
    host_id    BIGINT NULL,
    CONSTRAINT FKpmk9i7nh9cds4awehd972vx58
    FOREIGN KEY (host_id) REFERENCES Counter (id)
);

CREATE INDEX FKa17hrj8xmnbqvhi1p57jv1y1j ON CounterProfGrpACR (profile_id);
CREATE INDEX FKoultj0eiyvf2g3694eb8jftfl ON CounterProfGrpACR (subject_id);
CREATE INDEX FKpmk9i7nh9cds4awehd972vx58 ON CounterProfGrpACR (host_id);

CREATE TABLE CounterProfUsrACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NULL,
    subject_id BIGINT NULL,
    host_id    BIGINT NULL,
    CONSTRAINT FKe58tenbwnq28i4pqb2mg1xfs1
    FOREIGN KEY (host_id) REFERENCES Counter (id)
);

CREATE INDEX FKe58tenbwnq28i4pqb2mg1xfs1 ON CounterProfUsrACR (host_id);
CREATE INDEX FKeo5m46oqxt9xv3i8kglsdhumq ON CounterProfUsrACR (profile_id);
CREATE INDEX FKj7i4xtr0qh73offslvyatva76 ON CounterProfUsrACR (subject_id);

CREATE TABLE DelegatePermGrpACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    action     VARCHAR(255) NULL,
    allow      BIT          NOT NULL,
    subject_id BIGINT       NULL,
    host_id    BIGINT       NULL,
    CONSTRAINT FKeh1oakbfcvo6qx3wfhuqw8y05
    FOREIGN KEY (host_id) REFERENCES AccessTag (id)
);

CREATE INDEX FK6hqac560pggme0ujt5f33yi6 ON DelegatePermGrpACR (subject_id);
CREATE INDEX FKeh1oakbfcvo6qx3wfhuqw8y05 ON DelegatePermGrpACR (host_id);

CREATE TABLE DelegatePermUsrACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    action     VARCHAR(255) NULL,
    allow      BIT          NOT NULL,
    subject_id BIGINT       NULL,
    host_id    BIGINT       NULL,
    CONSTRAINT FKj7qwjvqh1lh0c94f2sgsgc9w8
    FOREIGN KEY (host_id) REFERENCES AccessTag (id)
);

CREATE INDEX FKg3m1a2bcx37aoycmrhclniejd ON DelegatePermUsrACR (subject_id);
CREATE INDEX FKj7qwjvqh1lh0c94f2sgsgc9w8 ON DelegatePermUsrACR (host_id);

CREATE TABLE DelegateProfGrpACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NULL,
    subject_id BIGINT NULL,
    host_id    BIGINT NULL,
    CONSTRAINT FKktl8jhvvjknf3weutbc7plvy1
    FOREIGN KEY (host_id) REFERENCES AccessTag (id)
);

CREATE INDEX FK4s1js9suxjavjjj613tdd6sda ON DelegateProfGrpACR (profile_id);
CREATE INDEX FKbr37ya3yk2ieq3tju9rudfkq9 ON DelegateProfGrpACR (subject_id);
CREATE INDEX FKktl8jhvvjknf3weutbc7plvy1 ON DelegateProfGrpACR (host_id);

CREATE TABLE DelegateProfUsrACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NULL,
    subject_id BIGINT NULL,
    host_id    BIGINT NULL,
    CONSTRAINT FKec87x7tsb0hdrs7p71y6oiyc6
    FOREIGN KEY (host_id) REFERENCES AccessTag (id)
);

CREATE INDEX FKec87x7tsb0hdrs7p71y6oiyc6 ON DelegateProfUsrACR (host_id);
CREATE INDEX FKkwqiloo5m1x620t9hs8brvrm4 ON DelegateProfUsrACR (profile_id);
CREATE INDEX FKtjpmgiy6509wrklnjq6xgvoo3 ON DelegateProfUsrACR (subject_id);

CREATE TABLE Domain (
    id BIGINT AUTO_INCREMENT PRIMARY KEY
);

CREATE TABLE DomainPermGrpACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    action     VARCHAR(255) NULL,
    allow      BIT          NOT NULL,
    subject_id BIGINT       NULL,
    host_id    BIGINT       NULL,
    CONSTRAINT FKji5qpcew5st4nauudn1y65djd
    FOREIGN KEY (host_id) REFERENCES Domain (id)
);

CREATE INDEX FK4phx5icammt848odixjhc7ba7 ON DomainPermGrpACR (subject_id);
CREATE INDEX FKji5qpcew5st4nauudn1y65djd ON DomainPermGrpACR (host_id);

CREATE TABLE DomainPermUsrACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    action     VARCHAR(255) NULL,
    allow      BIT          NOT NULL,
    subject_id BIGINT       NULL,
    host_id    BIGINT       NULL,
    CONSTRAINT FKkxnym6a8mf5y7dgqcjmwg7rwh
    FOREIGN KEY (host_id) REFERENCES Domain (id)
);

CREATE INDEX FKe7i854txd6jxpmlr4gv8tiec5 ON DomainPermUsrACR (subject_id);
CREATE INDEX FKkxnym6a8mf5y7dgqcjmwg7rwh ON DomainPermUsrACR (host_id);

CREATE TABLE DomainProfGrpACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NULL,
    subject_id BIGINT NULL,
    host_id    BIGINT NULL,
    CONSTRAINT FK1sx655p2gfq55lyf2u7sm6w2x
    FOREIGN KEY (host_id) REFERENCES Domain (id)
);

CREATE INDEX FK1sx655p2gfq55lyf2u7sm6w2x ON DomainProfGrpACR (host_id);
CREATE INDEX FKih13oyxqkeix9osakex0yl1v3 ON DomainProfGrpACR (profile_id);
CREATE INDEX FKjatkxbassa6xkt3k2i34hhvx1 ON DomainProfGrpACR (subject_id);

CREATE TABLE DomainProfUsrACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NULL,
    subject_id BIGINT NULL,
    host_id    BIGINT NULL,
    CONSTRAINT FK5ve2hjrk6a7g1js1mqrr01hwb
    FOREIGN KEY (host_id) REFERENCES Domain (id)
);

CREATE INDEX FK4qo9jt42qjg9tuun93yde795q ON DomainProfUsrACR (profile_id);
CREATE INDEX FK5ve2hjrk6a7g1js1mqrr01hwb ON DomainProfUsrACR (host_id);
CREATE INDEX FK6af0e2y9y0tbcxb7h1m7hm3am ON DomainProfUsrACR (subject_id);

CREATE TABLE FileAttribute (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                 LONGTEXT NULL,
    nameQualifierString  LONGTEXT NULL,
    reference            BIT      NOT NULL,
    value                LONGTEXT NULL,
    valueQualifierString LONGTEXT NULL,
    file_id              BIGINT   NULL,
    ord                  INT      NULL
);

CREATE INDEX FKek4om17ruuhrjo2gmirdxevay ON FileAttribute (file_id);

CREATE TABLE FileAttributeTagRef (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    parameter    VARCHAR(255) NULL,
    tag_id       BIGINT       NULL,
    attribute_id BIGINT       NULL,
    CONSTRAINT FK7ajmi2d4us29421nuiy0dlxib
    FOREIGN KEY (attribute_id) REFERENCES FileAttribute (id)
);

CREATE INDEX FK7ajmi2d4us29421nuiy0dlxib ON FileAttributeTagRef (attribute_id);
CREATE INDEX FKeplf4g0jggh2a5uvggocyeopq ON FileAttributeTagRef (tag_id);

CREATE TABLE FileRef (
    id         BIGINT AUTO_INCREMENT
        PRIMARY KEY,
    directory  BIT          NOT NULL,
    name       VARCHAR(255) NULL,
    size       BIGINT       NOT NULL,
    tableIndex INT          NOT NULL,
    sectionId  BIGINT       NULL,
    ord        INT          NULL,
    path       VARCHAR(255) NULL
);

CREATE INDEX FK464kkuexjpycuic1n33q0yhe2 ON FileRef (sectionId);

ALTER TABLE FileAttribute ADD CONSTRAINT FKek4om17ruuhrjo2gmirdxevay FOREIGN KEY (file_id) REFERENCES FileRef (id);

CREATE TABLE ReferencedFileAttribute (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                 LONGTEXT NULL,
    nameQualifierString  LONGTEXT NULL,
    reference            BIT      NOT NULL,
    value                LONGTEXT NULL,
    valueQualifierString LONGTEXT NULL,
    referenced_file_id   BIGINT   NULL,
    ord                  INT      NULL
);

CREATE INDEX ReferencedFileAttrFileId_IDX ON ReferencedFileAttribute (referenced_file_id);

CREATE TABLE ReferencedFile (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(255) NULL,
    size           BIGINT       NOT NULL,
    libraryFile    VARCHAR(100) NOT NULL,
    path           VARCHAR(255) NULL
);

CREATE INDEX ReferencedFile_LibraryFile_IDX ON ReferencedFile (libraryFile);

ALTER TABLE ReferencedFileAttribute
ADD CONSTRAINT ReferencedFile_ReferencedFileAttr_FRG_KEY FOREIGN KEY (referenced_file_id) REFERENCES ReferencedFile(id);

CREATE TABLE LibraryFile(
  name         VARCHAR(100) NOT NULL PRIMARY KEY
);

ALTER TABLE ReferencedFile
ADD CONSTRAINT ReferencedFile_LibraryFile_FRG_KEY FOREIGN KEY (libraryFile) REFERENCES LibraryFile(name);

CREATE TABLE FileRef_AccessTag (
    FileRef_id    BIGINT NOT NULL,
    accessTags_id BIGINT NOT NULL,
    CONSTRAINT FK9eo58a9b82bidhn1gitcu0lxq
    FOREIGN KEY (FileRef_id) REFERENCES FileRef (id),
    CONSTRAINT FKnbring73j11hr4lyv1189px2d
    FOREIGN KEY (accessTags_id) REFERENCES AccessTag (id)
);

CREATE INDEX FK9eo58a9b82bidhn1gitcu0lxq ON FileRef_AccessTag (FileRef_id);
CREATE INDEX FKnbring73j11hr4lyv1189px2d ON FileRef_AccessTag (accessTags_id);

CREATE TABLE FileTagRef (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    parameter VARCHAR(255) NULL,
    tag_id    BIGINT       NULL,
    file_id   BIGINT       NULL,
    CONSTRAINT FKjwy2goq7hgb8h6ha0cr4c7nc6
    FOREIGN KEY (file_id) REFERENCES FileRef (id)
);

CREATE INDEX FKc6qwpt1pjhskr2i0qut81jyes ON FileTagRef (tag_id);
CREATE INDEX FKjwy2goq7hgb8h6ha0cr4c7nc6 ON FileTagRef (file_id);

CREATE TABLE GroupPermGrpACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    action     VARCHAR(255) NULL,
    allow      BIT          NOT NULL,
    subject_id BIGINT       NULL,
    host_id    BIGINT       NULL
);

CREATE INDEX FKb5uvg7lkkymi8cxuukfoq40jj ON GroupPermGrpACR (host_id);
CREATE INDEX FKfk0aeimcd84ghw6yqsvl4crmf ON GroupPermGrpACR (subject_id);

CREATE TABLE GroupPermUsrACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    action     VARCHAR(255) NULL,
    allow      BIT          NOT NULL,
    subject_id BIGINT       NULL,
    host_id    BIGINT       NULL
);

CREATE INDEX FKd5iloff3ys8vrdqfd0s2deuh ON GroupPermUsrACR (host_id);
CREATE INDEX FKgnlvyk3kl124e1sdx93kfrfe1 ON GroupPermUsrACR (subject_id);

CREATE TABLE GroupProfGrpACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NULL,
    subject_id BIGINT NULL,
    host_id    BIGINT NULL
);

CREATE INDEX FK2ugnf9iqbbmc4uola613hi1ba ON GroupProfGrpACR (subject_id);
CREATE INDEX FKexss0gjarhk3g640cd59bwiso ON GroupProfGrpACR (profile_id);

CREATE INDEX FKpkcs62l0kqa4p7i3jpgqibe1 ON GroupProfGrpACR (host_id);

CREATE TABLE GroupProfUsrACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NULL,
    subject_id BIGINT NULL,
    host_id    BIGINT NULL
);

CREATE INDEX FK48xq4my0foj1mdoxywmo8gndx ON GroupProfUsrACR (subject_id);
CREATE INDEX FKa5e9vbhh89jw9x0xi3s7j7kjd ON GroupProfUsrACR (profile_id);
CREATE INDEX FKo3kswy5ax5h6c6ao21ubdih8o ON GroupProfUsrACR (host_id);

CREATE TABLE IdGen (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    prefix     VARCHAR(255) NULL,
    suffix     VARCHAR(255) NULL,
    counter_id BIGINT       NULL,
    CONSTRAINT pfxsfx_idx
    UNIQUE (prefix, suffix),
    CONSTRAINT FKjndkokb5qh9p1af7cim617rvv
    FOREIGN KEY (counter_id) REFERENCES Counter (id)
);

CREATE INDEX FKjndkokb5qh9p1af7cim617rvv ON IdGen (counter_id);

CREATE TABLE IdGenPermGrpACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    action     VARCHAR(255) NULL,
    allow      BIT          NOT NULL,
    subject_id BIGINT       NULL,
    host_id    BIGINT       NULL,
    CONSTRAINT FKtnpsmccpwkr6l8vwphd8gll8r
    FOREIGN KEY (host_id) REFERENCES IdGen (id)
);

CREATE INDEX FKeai1jrb5f522qfq70p3oq5ph5 ON IdGenPermGrpACR (subject_id);
CREATE INDEX FKtnpsmccpwkr6l8vwphd8gll8r ON IdGenPermGrpACR (host_id);

CREATE TABLE IdGenPermUsrACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    action     VARCHAR(255) NULL,
    allow      BIT          NOT NULL,
    subject_id BIGINT       NULL,
    host_id    BIGINT       NULL,
    CONSTRAINT FKc318sg4w2yxt9yn5ousdruy25
    FOREIGN KEY (host_id) REFERENCES IdGen (id)
);

CREATE INDEX FKbr7bcaanyb55175xmh4kducuy ON IdGenPermUsrACR (subject_id);
CREATE INDEX FKc318sg4w2yxt9yn5ousdruy25 ON IdGenPermUsrACR (host_id);

CREATE TABLE IdGenProfGrpACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NULL,
    subject_id BIGINT NULL,
    host_id    BIGINT NULL,
    CONSTRAINT FK8if6oo32nulhyoyn08nc52135
    FOREIGN KEY (host_id) REFERENCES IdGen (id)
);

CREATE INDEX FK1soo14x8o767oboi3k4xq6a75 ON IdGenProfGrpACR (subject_id);
CREATE INDEX FK8if6oo32nulhyoyn08nc52135 ON IdGenProfGrpACR (host_id);
CREATE INDEX FK9elcun29j9cp8duy2dcapiyta ON IdGenProfGrpACR (profile_id);

CREATE TABLE IdGenProfUsrACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NULL,
    subject_id BIGINT NULL,
    host_id    BIGINT NULL,
    CONSTRAINT FK4p13ft5wvd0ysdlipfd5hx8ga
    FOREIGN KEY (host_id) REFERENCES IdGen (id)
);

CREATE INDEX FK4p13ft5wvd0ysdlipfd5hx8ga ON IdGenProfUsrACR (host_id);
CREATE INDEX FKq9319jend4uh3jsq7ku5n9twp ON IdGenProfUsrACR (profile_id);
CREATE INDEX FKqm34wgkkt2ocpfa5xs3nkfgu5 ON IdGenProfUsrACR (subject_id);

CREATE TABLE Link (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    tableIndex INT          NOT NULL,
    url        VARCHAR(255) NULL,
    section_id BIGINT       NULL,
    ord        INT          NULL
);

CREATE INDEX FKqhsnrwf0i6q08gt5l83fwchn8 ON Link (section_id);

CREATE TABLE LinkAttribute (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                 LONGTEXT NULL,
    nameQualifierString  LONGTEXT NULL,
    reference            BIT      NOT NULL,
    value                LONGTEXT NULL,
    valueQualifierString LONGTEXT NULL,
    link_id              BIGINT   NULL,
    ord                  INT      NULL,
    CONSTRAINT FKiy7ig2d3ubfsc921qrarw4x5n
    FOREIGN KEY (link_id) REFERENCES Link (id)
);

CREATE INDEX FKiy7ig2d3ubfsc921qrarw4x5n ON LinkAttribute (link_id);

CREATE TABLE LinkAttributeTagRef (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    parameter    VARCHAR(255) NULL,
    tag_id       BIGINT       NULL,
    attribute_id BIGINT       NULL,
    CONSTRAINT FKt2e1g7chbwfkbrsjvxtjfeidi
    FOREIGN KEY (attribute_id) REFERENCES LinkAttribute (id)
);

CREATE INDEX FKlti8k5ohpaxww51g7ujyu96f6 ON LinkAttributeTagRef (tag_id);
CREATE INDEX FKt2e1g7chbwfkbrsjvxtjfeidi ON LinkAttributeTagRef (attribute_id);

CREATE TABLE LinkTagRef (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    parameter VARCHAR(255) NULL,
    tag_id    BIGINT       NULL,
    link_id   BIGINT       NULL,
    CONSTRAINT FKt4yfajuy06mokg9r2vdkeni1e
    FOREIGN KEY (link_id) REFERENCES Link (id)
);

CREATE INDEX FKc0qu7988w42xxe7ypsmy5c242 ON LinkTagRef (tag_id);
CREATE INDEX FKt4yfajuy06mokg9r2vdkeni1e ON LinkTagRef (link_id);

CREATE TABLE Link_AccessTag (
    Link_id       BIGINT NOT NULL,
    accessTags_id BIGINT NOT NULL,
    CONSTRAINT FKja1o6a442g9dmbouqehhuloxn
    FOREIGN KEY (Link_id) REFERENCES Link (id),
    CONSTRAINT FKsiq98dra76i2ucsi7h7rkd3js
    FOREIGN KEY (accessTags_id) REFERENCES AccessTag (id)
);

CREATE INDEX FKja1o6a442g9dmbouqehhuloxn ON Link_AccessTag (Link_id);
CREATE INDEX FKsiq98dra76i2ucsi7h7rkd3js ON Link_AccessTag (accessTags_id);

CREATE TABLE Permission (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    action      VARCHAR(255) NULL,
    allow       BIT          NOT NULL,
    description VARCHAR(255) NULL,
    DTYPE       VARCHAR(31)  NOT NULL,
    subject_id  BIGINT       NULL,
    host_id     BIGINT       NULL,
    CONSTRAINT FKe9ry9gancqbvo7vpua4hhnhps
    FOREIGN KEY (host_id) REFERENCES AccessTag (id)
);

CREATE INDEX FKe9ry9gancqbvo7vpua4hhnhps ON Permission (host_id);
CREATE INDEX FKmydq52kdr5u8mah355qdc00qf ON Permission (subject_id);

CREATE TABLE PermissionProfile (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) NULL,
    name        VARCHAR(255) NULL
);

ALTER TABLE CounterProfGrpACR ADD CONSTRAINT FKa17hrj8xmnbqvhi1p57jv1y1j
FOREIGN KEY (profile_id) REFERENCES PermissionProfile (id);

ALTER TABLE CounterProfUsrACR ADD CONSTRAINT FKeo5m46oqxt9xv3i8kglsdhumq
FOREIGN KEY (profile_id) REFERENCES PermissionProfile (id);

ALTER TABLE DelegateProfGrpACR ADD CONSTRAINT FK4s1js9suxjavjjj613tdd6sda
FOREIGN KEY (profile_id) REFERENCES PermissionProfile (id);

ALTER TABLE DelegateProfUsrACR ADD CONSTRAINT FKkwqiloo5m1x620t9hs8brvrm4
FOREIGN KEY (profile_id) REFERENCES PermissionProfile (id);

ALTER TABLE DomainProfGrpACR ADD CONSTRAINT FKih13oyxqkeix9osakex0yl1v3
FOREIGN KEY (profile_id) REFERENCES PermissionProfile (id);

ALTER TABLE DomainProfUsrACR ADD CONSTRAINT FK4qo9jt42qjg9tuun93yde795q
FOREIGN KEY (profile_id) REFERENCES PermissionProfile (id);

ALTER TABLE GroupProfGrpACR ADD CONSTRAINT FKexss0gjarhk3g640cd59bwiso
FOREIGN KEY (profile_id) REFERENCES PermissionProfile (id);

ALTER TABLE GroupProfUsrACR ADD CONSTRAINT FKa5e9vbhh89jw9x0xi3s7j7kjd
FOREIGN KEY (profile_id) REFERENCES PermissionProfile (id);

ALTER TABLE IdGenProfGrpACR ADD CONSTRAINT FK9elcun29j9cp8duy2dcapiyta
FOREIGN KEY (profile_id) REFERENCES PermissionProfile (id);

ALTER TABLE IdGenProfUsrACR ADD CONSTRAINT FKq9319jend4uh3jsq7ku5n9twp
FOREIGN KEY (profile_id) REFERENCES PermissionProfile (id);

CREATE TABLE PermissionProfile_Permission (
    PermissionProfile_id BIGINT NOT NULL,
    permissions_id       BIGINT NOT NULL,
    CONSTRAINT UK_nve4weya42ttltfihwaf2f4ww
    UNIQUE (permissions_id),
    CONSTRAINT FKsiylfh5b67lymyxieyp8vt5i3
    FOREIGN KEY (PermissionProfile_id) REFERENCES PermissionProfile (id),
    CONSTRAINT FKna3es1w529vt170rgalday23k
    FOREIGN KEY (permissions_id) REFERENCES Permission (id)
);

CREATE INDEX FKsiylfh5b67lymyxieyp8vt5i3 ON PermissionProfile_Permission (PermissionProfile_id);

CREATE TABLE PermissionProfile_PermissionProfile (
    PermissionProfile_id BIGINT NOT NULL,
    profiles_id          BIGINT NOT NULL,
    CONSTRAINT FKou02v5l7x6l67pl8l145qilhu
    FOREIGN KEY (PermissionProfile_id) REFERENCES PermissionProfile (id),
    CONSTRAINT FKmahhiqqgllt6ljl6di8wg0gq3
    FOREIGN KEY (profiles_id) REFERENCES PermissionProfile (id)
);

CREATE INDEX FKmahhiqqgllt6ljl6di8wg0gq3 ON PermissionProfile_PermissionProfile (profiles_id);
CREATE INDEX FKou02v5l7x6l67pl8l145qilhu ON PermissionProfile_PermissionProfile (PermissionProfile_id);

CREATE TABLE Section (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    accNo         VARCHAR(255) NULL,
    parentAccNo   VARCHAR(255) NULL,
    tableIndex    INT          NOT NULL,
    type          VARCHAR(255) NULL,
    parent_id     BIGINT       NULL,
    submission_id BIGINT       NULL,
    libraryFile   VARCHAR(100) NULL,
    ord           INT          NULL,
    CONSTRAINT FKba6xolosvegauoq8xs1kj17ch
    FOREIGN KEY (parent_id) REFERENCES Section (id),
    CONSTRAINT LibraryFile_Section_FRG_KEY FOREIGN KEY (libraryFile) REFERENCES LibraryFile(name) ON DELETE CASCADE
);

CREATE INDEX acc_idx ON Section (accNo);
CREATE INDEX FK4bi0ld27mvrinwk6gleu9phf4 ON Section (submission_id);
CREATE INDEX FKba6xolosvegauoq8xs1kj17ch ON Section (parent_id);

CREATE INDEX section_type_index ON Section (type);
ALTER TABLE FileRef ADD CONSTRAINT FK464kkuexjpycuic1n33q0yhe2
FOREIGN KEY (sectionId) REFERENCES Section (id);
ALTER TABLE Link ADD CONSTRAINT FKqhsnrwf0i6q08gt5l83fwchn8
FOREIGN KEY (section_id) REFERENCES Section (id);

CREATE TABLE SectionAttribute (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                 LONGTEXT NULL,
    nameQualifierString  LONGTEXT NULL,
    reference            BIT      NOT NULL,
    value                LONGTEXT NULL,
    valueQualifierString LONGTEXT NULL,
    section_id           BIGINT   NULL,
    ord                  INT      NULL,
    CONSTRAINT FK93fwpmt18ghb0hktnsljtlnhu
    FOREIGN KEY (section_id) REFERENCES Section (id)
);

CREATE INDEX FK93fwpmt18ghb0hktnsljtlnhu ON SectionAttribute (section_id);

CREATE TABLE SectionAttributeTagRef (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    parameter    VARCHAR(255) NULL,
    tag_id       BIGINT       NULL,
    attribute_id BIGINT       NULL,
    CONSTRAINT FK1cb21btg3gd8mkt44uuip4tw7
    FOREIGN KEY (attribute_id) REFERENCES SectionAttribute (id)
);

CREATE INDEX FK1cb21btg3gd8mkt44uuip4tw7 ON SectionAttributeTagRef (attribute_id);
CREATE INDEX FKdemukuohsa90k3txsfvqq59f9 ON SectionAttributeTagRef (tag_id);

CREATE TABLE SectionTagRef (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    parameter  VARCHAR(255) NULL,
    tag_id     BIGINT       NULL,
    section_id BIGINT       NULL,
    CONSTRAINT FK9o566l9pdh34it8jchomb1s5d
    FOREIGN KEY (section_id) REFERENCES Section (id)
);

CREATE INDEX FK9o566l9pdh34it8jchomb1s5d ON SectionTagRef (section_id);
CREATE INDEX FKi9bxyq49anoelbynx544y9dy ON SectionTagRef (tag_id);

CREATE TABLE Section_AccessTag (
    Section_id    BIGINT NOT NULL,
    accessTags_id BIGINT NOT NULL,
    CONSTRAINT FK8naal5ijvf4yiqsm61r4o0t38
    FOREIGN KEY (Section_id) REFERENCES Section (id),
    CONSTRAINT FKdbupse53uebxuuafb22ti6b9d
    FOREIGN KEY (accessTags_id) REFERENCES AccessTag (id)
);

CREATE INDEX FK8naal5ijvf4yiqsm61r4o0t38 ON Section_AccessTag (Section_id);
CREATE INDEX FKdbupse53uebxuuafb22ti6b9d ON Section_AccessTag (accessTags_id);

CREATE TABLE Submission (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    CTime          BIGINT       NOT NULL,
    MTime          BIGINT       NOT NULL,
    RTime          BIGINT       NOT NULL,
    accNo          VARCHAR(255) NULL,
    relPath        LONGTEXT     NULL,
    released       BIT          NOT NULL,
    rootPath       LONGTEXT     NULL,
    title          LONGTEXT     NULL,
    version        INT          NOT NULL,
    owner_id       BIGINT       NULL,
    rootSection_id BIGINT       NULL,
    secretKey      VARCHAR(255) NULL,
    CONSTRAINT UKalkiyx9bg56ika8jw65r99fll
    UNIQUE (accNo, version),
    CONSTRAINT FKhsm5gtat31dkrft0was3a7gr7
    FOREIGN KEY (rootSection_id) REFERENCES Section (id)
);

CREATE INDEX FKhsm5gtat31dkrft0was3a7gr7 ON Submission (rootSection_id);
CREATE INDEX FKidqs3m2ntuqyuiophfwikw81a ON Submission (owner_id);
CREATE INDEX released_idx ON Submission (released);
CREATE INDEX rtime_idx ON Submission (RTime);

ALTER TABLE AttributeSubscriptionMatchEvent ADD CONSTRAINT FKb13xop3ik6im4c9ibudfx7pu0
FOREIGN KEY (submission_id) REFERENCES Submission (id);

ALTER TABLE Section ADD CONSTRAINT FK4bi0ld27mvrinwk6gleu9phf4
FOREIGN KEY (submission_id) REFERENCES Submission (id);

CREATE TABLE SubmissionAttribute (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                 LONGTEXT NULL,
    nameQualifierString  LONGTEXT NULL,
    reference            BIT      NOT NULL,
    value                LONGTEXT NULL,
    valueQualifierString LONGTEXT NULL,
    submission_id        BIGINT   NULL,
    ord                  INT      NULL,
    CONSTRAINT FKstek2rbmsk052iydxt2eamv15
    FOREIGN KEY (submission_id) REFERENCES Submission (id)
);

CREATE INDEX FKstek2rbmsk052iydxt2eamv15 ON SubmissionAttribute (submission_id);

CREATE TABLE SubmissionAttributeTagRef (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    parameter    VARCHAR(255) NULL,
    tag_id       BIGINT       NULL,
    attribute_id BIGINT       NULL,
    CONSTRAINT FKp4d99wrvt56pcmh232bcqb39q
    FOREIGN KEY (attribute_id) REFERENCES SubmissionAttribute (id)
);

CREATE INDEX FKp4d99wrvt56pcmh232bcqb39q ON SubmissionAttributeTagRef (attribute_id);
CREATE INDEX FKr6su7l8bywogputykgveyulpd ON SubmissionAttributeTagRef (tag_id);

CREATE TABLE SubmissionTagRef (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    parameter     VARCHAR(255) NULL,
    tag_id        BIGINT       NULL,
    submission_id BIGINT       NULL,
    CONSTRAINT FK8hm6cswe1g8yln27i7io54q0q
    FOREIGN KEY (submission_id) REFERENCES Submission (id)
);

CREATE INDEX FK4fb5yjdagkfeagoebhiloqqrp ON SubmissionTagRef (tag_id);
CREATE INDEX FK8hm6cswe1g8yln27i7io54q0q ON SubmissionTagRef (submission_id);

CREATE TABLE Submission_AccessTag (
    Submission_id BIGINT NOT NULL,
    accessTags_id BIGINT NOT NULL,
    CONSTRAINT FK6kvcm7vgoutbie7um590vt5ev
    FOREIGN KEY (Submission_id) REFERENCES Submission (id),
    CONSTRAINT FKgsgxljia12i17av51pl5el3c0
    FOREIGN KEY (accessTags_id) REFERENCES AccessTag (id)
);

CREATE INDEX FK6kvcm7vgoutbie7um590vt5ev ON Submission_AccessTag (Submission_id);
CREATE INDEX FKgsgxljia12i17av51pl5el3c0 ON Submission_AccessTag (accessTags_id);

CREATE TABLE SystemPermGrpACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    action     VARCHAR(255) NULL,
    allow      BIT          NOT NULL,
    subject_id BIGINT       NULL
);

CREATE INDEX FK3jv5jn809obpxl8owmq3cs7o5 ON SystemPermGrpACR (subject_id);

CREATE TABLE SystemPermUsrACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    action     VARCHAR(255) NULL,
    allow      BIT          NOT NULL,
    subject_id BIGINT       NULL
);

CREATE INDEX FKtfti9r75r0drcn4gykuapf341 ON SystemPermUsrACR (subject_id);

CREATE TABLE SystemProfGrpACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NULL,
    subject_id BIGINT NULL,
    CONSTRAINT FK3davvbn9jm62fen9j2884iisw
    FOREIGN KEY (profile_id) REFERENCES PermissionProfile (id)
);

CREATE INDEX FK3davvbn9jm62fen9j2884iisw ON SystemProfGrpACR (profile_id);
CREATE INDEX FK8wbpwp547sycebpc4xqc0ikrg ON SystemProfGrpACR (subject_id);

CREATE TABLE SystemProfUsrACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NULL,
    subject_id BIGINT NULL,
    CONSTRAINT FK1uk7dhb8ct4llymadgktb3hsu
    FOREIGN KEY (profile_id) REFERENCES PermissionProfile (id)
);

CREATE INDEX FK1uk7dhb8ct4llymadgktb3hsu ON SystemProfUsrACR (profile_id);
CREATE INDEX FKfbt2bpon4gx5qbo7j4q5km7bu ON SystemProfUsrACR (subject_id);

CREATE TABLE Tag (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    description   VARCHAR(255) NULL,
    name          VARCHAR(255) NULL,
    classifier_id BIGINT       NULL,
    parent_tag_id BIGINT       NULL,
    CONSTRAINT name_idx
    UNIQUE (name),
    CONSTRAINT classifier_fk
    FOREIGN KEY (classifier_id) REFERENCES Classifier (id),
    CONSTRAINT parent_tag_fk
    FOREIGN KEY (parent_tag_id) REFERENCES Tag (id)
);

CREATE INDEX classifier_fk ON Tag (classifier_id);
CREATE INDEX parent_tag_fk ON Tag (parent_tag_id);

ALTER TABLE FileAttributeTagRef ADD CONSTRAINT FKeplf4g0jggh2a5uvggocyeopq FOREIGN KEY (tag_id) REFERENCES Tag (id);
ALTER TABLE FileTagRef ADD CONSTRAINT FKc6qwpt1pjhskr2i0qut81jyes FOREIGN KEY (tag_id) REFERENCES Tag (id);
ALTER TABLE LinkAttributeTagRef ADD CONSTRAINT FKlti8k5ohpaxww51g7ujyu96f6 FOREIGN KEY (tag_id) REFERENCES Tag (id);
ALTER TABLE LinkTagRef ADD CONSTRAINT FKc0qu7988w42xxe7ypsmy5c242 FOREIGN KEY (tag_id) REFERENCES Tag (id);
ALTER TABLE SectionAttributeTagRef ADD CONSTRAINT FKdemukuohsa90k3txsfvqq59f9 FOREIGN KEY (tag_id) REFERENCES Tag (id);
ALTER TABLE SectionTagRef ADD CONSTRAINT FKi9bxyq49anoelbynx544y9dy FOREIGN KEY (tag_id) REFERENCES Tag (id);
ALTER TABLE SubmissionAttributeTagRef ADD CONSTRAINT FKr6su7l8bywogputykgveyulpd FOREIGN KEY (tag_id) REFERENCES Tag (id);
ALTER TABLE SubmissionTagRef ADD CONSTRAINT FK4fb5yjdagkfeagoebhiloqqrp FOREIGN KEY (tag_id) REFERENCES Tag (id);

CREATE TABLE TagPermGrpACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    action     VARCHAR(255) NULL,
    allow      BIT          NOT NULL,
    subject_id BIGINT       NULL,
    host_id    BIGINT       NULL,
    CONSTRAINT FKh47life067b6795pfrm1uoia
    FOREIGN KEY (host_id) REFERENCES AccessTag (id)
);

CREATE INDEX FKh47life067b6795pfrm1uoia ON TagPermGrpACR (host_id);
CREATE INDEX FKmr5f3x121ndijqgwsayxklbtn ON TagPermGrpACR (subject_id);

CREATE TABLE TagPermUsrACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    action     VARCHAR(255) NULL,
    allow      BIT          NOT NULL,
    subject_id BIGINT       NULL,
    host_id    BIGINT       NULL,
    CONSTRAINT FK6xcybyp33nxu74byne2krk6q8
    FOREIGN KEY (host_id) REFERENCES AccessTag (id)
);

CREATE INDEX FK2djtblr07v4l3kkp1jnhl5g1w ON TagPermUsrACR (subject_id);
CREATE INDEX FK6xcybyp33nxu74byne2krk6q8 ON TagPermUsrACR (host_id);

CREATE TABLE TagProfGrpACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NULL,
    subject_id BIGINT NULL,
    host_id    BIGINT NULL,
    CONSTRAINT FK8ukcmycyyw00rwjwqxqf1oya8
    FOREIGN KEY (profile_id) REFERENCES PermissionProfile (id),
    CONSTRAINT FK8y3p3eqb28j5kehff9bwswsta
    FOREIGN KEY (host_id) REFERENCES AccessTag (id)
);

CREATE INDEX FK8ukcmycyyw00rwjwqxqf1oya8 ON TagProfGrpACR (profile_id);
CREATE INDEX FK8y3p3eqb28j5kehff9bwswsta ON TagProfGrpACR (host_id);
CREATE INDEX FKey1lxe9pspnvkajsd9ebntje4 ON TagProfGrpACR (subject_id);

CREATE TABLE TagProfUsrACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NULL,
    subject_id BIGINT NULL,
    host_id    BIGINT NULL,
    CONSTRAINT FKtbpn3n25lmy7tigtb9s3n8h18
    FOREIGN KEY (profile_id) REFERENCES PermissionProfile (id),
    CONSTRAINT FK34re2b0uivh1xxrqp02gkhm2q
    FOREIGN KEY (host_id) REFERENCES AccessTag (id)
);

CREATE INDEX FK34re2b0uivh1xxrqp02gkhm2q ON TagProfUsrACR (host_id);
CREATE INDEX FKhswhutge4n1vvqf078nabcrrk ON TagProfUsrACR (subject_id);
CREATE INDEX FKtbpn3n25lmy7tigtb9s3n8h18 ON TagProfUsrACR (profile_id);

CREATE TABLE TagSubscription (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    tag_id  BIGINT NULL,
    user_id BIGINT NULL,
    CONSTRAINT tag_fk
    FOREIGN KEY (tag_id) REFERENCES Tag (id)
);

CREATE INDEX tag_fk ON TagSubscription (tag_id);
CREATE INDEX user_fk ON TagSubscription (user_id);

CREATE TABLE TagSubscriptionMatchEvent (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id   BIGINT NULL,
    subscription_id BIGINT NULL,
    user_id         BIGINT NULL,
    CONSTRAINT FKkhr4b0kpjuoa2m9u791efrrhp
    FOREIGN KEY (submission_id) REFERENCES Submission (id),
    CONSTRAINT FKjpyitq19xx20cu269fxwa1x2m
    FOREIGN KEY (subscription_id) REFERENCES TagSubscription (id)
);

CREATE INDEX FKiqlvauowjbb9na4lpcijjwx1r ON TagSubscriptionMatchEvent (user_id);
CREATE INDEX FKjpyitq19xx20cu269fxwa1x2m ON TagSubscriptionMatchEvent (subscription_id);
CREATE INDEX FKkhr4b0kpjuoa2m9u791efrrhp ON TagSubscriptionMatchEvent (submission_id);

CREATE TABLE TemplatePermGrpACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    action     VARCHAR(255) NULL,
    allow      BIT          NOT NULL,
    subject_id BIGINT       NULL,
    host_id    BIGINT       NULL,
    CONSTRAINT FKng3hqrb8h40l2pcxrh46tuee3
    FOREIGN KEY (host_id) REFERENCES AuthorizationTemplate (id)
);

CREATE INDEX FK72sqcoxks9xp8lv8aw03kf8o3 ON TemplatePermGrpACR (subject_id);
CREATE INDEX FKng3hqrb8h40l2pcxrh46tuee3 ON TemplatePermGrpACR (host_id);

CREATE TABLE TemplatePermUsrACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    action     VARCHAR(255) NULL,
    allow      BIT          NOT NULL,
    subject_id BIGINT       NULL,
    host_id    BIGINT       NULL,
    CONSTRAINT FKpjowt28229n8tojml209ayje3
    FOREIGN KEY (host_id) REFERENCES AuthorizationTemplate (id)
);

CREATE INDEX FKj6s1ceas8cxveq6i24lcfmj13 ON TemplatePermUsrACR (subject_id);
CREATE INDEX FKpjowt28229n8tojml209ayje3 ON TemplatePermUsrACR (host_id);

CREATE TABLE TemplateProfGrpACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NULL,
    subject_id BIGINT NULL,
    host_id    BIGINT NULL,
    CONSTRAINT FKcfy2byvhw4ubyt4q9i66dhdgi
    FOREIGN KEY (profile_id) REFERENCES PermissionProfile (id),
    CONSTRAINT FK23wwmxdclv2p9fo9gjxb98ydq
    FOREIGN KEY (host_id) REFERENCES AuthorizationTemplate (id)
);

CREATE INDEX FK23wwmxdclv2p9fo9gjxb98ydq ON TemplateProfGrpACR (host_id);
CREATE INDEX FK68m9cp82i31alwb0mcqiajluo ON TemplateProfGrpACR (subject_id);
CREATE INDEX FKcfy2byvhw4ubyt4q9i66dhdgi ON TemplateProfGrpACR (profile_id);

CREATE TABLE TemplateProfUsrACR (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NULL,
    subject_id BIGINT NULL,
    host_id    BIGINT NULL,
    CONSTRAINT FK4ama6dl6dc9at8peysfdeo4o3
    FOREIGN KEY (profile_id) REFERENCES PermissionProfile (id),
    CONSTRAINT FK18ai9ee3c4lv0x4o9gctpripo
    FOREIGN KEY (host_id) REFERENCES AuthorizationTemplate (id)
);

CREATE INDEX FK18ai9ee3c4lv0x4o9gctpripo ON TemplateProfUsrACR (host_id);
CREATE INDEX FK4ama6dl6dc9at8peysfdeo4o3 ON TemplateProfUsrACR (profile_id);
CREATE INDEX FKflte3awcj10olkyjo7hlewiib ON TemplateProfUsrACR (subject_id);

CREATE TABLE User (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    activationKey  VARCHAR(255) NULL,
    active         BIT          NOT NULL,
    auxProfileInfo LONGTEXT     NULL,
    email          VARCHAR(255) NULL,
    fullName       VARCHAR(255) NULL,
    keyTime        BIGINT       NOT NULL,
    login          VARCHAR(255) NULL,
    passwordDigest LONGBLOB     NULL,
    secret         VARCHAR(255) NULL,
    superuser      BIT          NOT NULL,
    ssoSubject     VARCHAR(255) NULL,
    CONSTRAINT email_index
    UNIQUE (email),
    CONSTRAINT login_index
    UNIQUE (login)
);

ALTER TABLE AccessTag ADD CONSTRAINT FKc4d73kr6bf92cyioruykxptr0
FOREIGN KEY (owner_id) REFERENCES User (id);

ALTER TABLE AttributeSubscription ADD CONSTRAINT FKh8qytl1fxkrml09gigq7wf3et
FOREIGN KEY (user_id) REFERENCES User (id);

ALTER TABLE AttributeSubscriptionMatchEvent ADD CONSTRAINT FK7h9hi0w70d3hyuc9sqa7tya7o
FOREIGN KEY (user_id) REFERENCES User (id);

ALTER TABLE CounterPermUsrACR ADD CONSTRAINT FK3j5d4i1yqh2xunjg3el23mu9m
FOREIGN KEY (subject_id) REFERENCES User (id);

ALTER TABLE CounterProfUsrACR ADD CONSTRAINT FKj7i4xtr0qh73offslvyatva76
FOREIGN KEY (subject_id) REFERENCES User (id);

ALTER TABLE DelegatePermUsrACR ADD CONSTRAINT FKg3m1a2bcx37aoycmrhclniejd
FOREIGN KEY (subject_id) REFERENCES User (id);

ALTER TABLE DelegateProfUsrACR ADD CONSTRAINT FKtjpmgiy6509wrklnjq6xgvoo3
FOREIGN KEY (subject_id) REFERENCES User (id);

ALTER TABLE DomainPermUsrACR ADD CONSTRAINT FKe7i854txd6jxpmlr4gv8tiec5
FOREIGN KEY (subject_id) REFERENCES User (id);

ALTER TABLE DomainProfUsrACR ADD CONSTRAINT FK6af0e2y9y0tbcxb7h1m7hm3am
FOREIGN KEY (subject_id) REFERENCES User (id);

ALTER TABLE GroupPermUsrACR ADD CONSTRAINT FKgnlvyk3kl124e1sdx93kfrfe1
FOREIGN KEY (subject_id) REFERENCES User (id);

ALTER TABLE GroupProfUsrACR ADD CONSTRAINT FK48xq4my0foj1mdoxywmo8gndx
FOREIGN KEY (subject_id) REFERENCES User (id);

ALTER TABLE IdGenPermUsrACR ADD CONSTRAINT FKbr7bcaanyb55175xmh4kducuy
FOREIGN KEY (subject_id) REFERENCES User (id);

ALTER TABLE IdGenProfUsrACR ADD CONSTRAINT FKqm34wgkkt2ocpfa5xs3nkfgu5
FOREIGN KEY (subject_id) REFERENCES User (id);

ALTER TABLE Permission ADD CONSTRAINT FKcvw4cufrnbhv27xk1y9etsvdw
FOREIGN KEY (subject_id) REFERENCES User (id);

ALTER TABLE Submission ADD CONSTRAINT FKidqs3m2ntuqyuiophfwikw81a
FOREIGN KEY (owner_id) REFERENCES User (id);

ALTER TABLE SystemPermUsrACR ADD CONSTRAINT FKtfti9r75r0drcn4gykuapf341
FOREIGN KEY (subject_id) REFERENCES User (id);

ALTER TABLE SystemProfUsrACR ADD CONSTRAINT FKfbt2bpon4gx5qbo7j4q5km7bu
FOREIGN KEY (subject_id) REFERENCES User (id);

ALTER TABLE TagPermUsrACR ADD CONSTRAINT FK2djtblr07v4l3kkp1jnhl5g1w
FOREIGN KEY (subject_id) REFERENCES User (id);

ALTER TABLE TagProfUsrACR ADD CONSTRAINT FKhswhutge4n1vvqf078nabcrrk
FOREIGN KEY (subject_id) REFERENCES User (id);

ALTER TABLE TagSubscription ADD CONSTRAINT user_fk
FOREIGN KEY (user_id) REFERENCES User (id);

ALTER TABLE TagSubscriptionMatchEvent ADD CONSTRAINT FKiqlvauowjbb9na4lpcijjwx1r
FOREIGN KEY (user_id) REFERENCES User (id);

ALTER TABLE TemplatePermUsrACR ADD CONSTRAINT FKj6s1ceas8cxveq6i24lcfmj13
FOREIGN KEY (subject_id) REFERENCES User (id);

ALTER TABLE TemplateProfUsrACR ADD CONSTRAINT FKflte3awcj10olkyjo7hlewiib
FOREIGN KEY (subject_id) REFERENCES User (id);

CREATE TABLE UserData (
    dataKey     VARCHAR(255) NOT NULL,
    userId      BIGINT       NOT NULL,
    data        LONGTEXT     NULL,
    contentType VARCHAR(255) NULL,
    topic       VARCHAR(255) NULL,
    PRIMARY KEY (dataKey, userId)
);

CREATE TABLE UserGroup (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) NULL,
    name        VARCHAR(255) NULL,
    project     BIT          NOT NULL,
    owner_id    BIGINT       NULL,
    secret      VARCHAR(255) NULL,
    CONSTRAINT name_index
    UNIQUE (name),
    CONSTRAINT FKt6580c8mqsfigvlgbtepcdnnk
    FOREIGN KEY (owner_id) REFERENCES User (id)
);

CREATE INDEX FKt6580c8mqsfigvlgbtepcdnnk ON UserGroup (owner_id);

ALTER TABLE CounterPermGrpACR ADD CONSTRAINT FK5sf1bgxd91o0q80xdqqkecn8g
FOREIGN KEY (subject_id) REFERENCES UserGroup (id);

ALTER TABLE CounterProfGrpACR ADD CONSTRAINT FKoultj0eiyvf2g3694eb8jftfl
FOREIGN KEY (subject_id) REFERENCES UserGroup (id);

ALTER TABLE DelegatePermGrpACR ADD CONSTRAINT FK6hqac560pggme0ujt5f33yi6
FOREIGN KEY (subject_id) REFERENCES UserGroup (id);

ALTER TABLE DelegateProfGrpACR ADD CONSTRAINT FKbr37ya3yk2ieq3tju9rudfkq9
FOREIGN KEY (subject_id) REFERENCES UserGroup (id);

ALTER TABLE DomainPermGrpACR ADD CONSTRAINT FK4phx5icammt848odixjhc7ba7
FOREIGN KEY (subject_id) REFERENCES UserGroup (id);

ALTER TABLE DomainProfGrpACR ADD CONSTRAINT FKjatkxbassa6xkt3k2i34hhvx1
FOREIGN KEY (subject_id) REFERENCES UserGroup (id);

ALTER TABLE GroupPermGrpACR ADD CONSTRAINT FKfk0aeimcd84ghw6yqsvl4crmf
FOREIGN KEY (subject_id) REFERENCES UserGroup (id);

ALTER TABLE GroupPermGrpACR ADD CONSTRAINT FKb5uvg7lkkymi8cxuukfoq40jj
FOREIGN KEY (host_id) REFERENCES UserGroup (id);

ALTER TABLE GroupPermUsrACR ADD CONSTRAINT FKd5iloff3ys8vrdqfd0s2deuh
FOREIGN KEY (host_id) REFERENCES UserGroup (id);

ALTER TABLE GroupProfGrpACR ADD CONSTRAINT FK2ugnf9iqbbmc4uola613hi1ba
FOREIGN KEY (subject_id) REFERENCES UserGroup (id);

ALTER TABLE GroupProfGrpACR ADD CONSTRAINT FKpkcs62l0kqa4p7i3jpgqibe1
FOREIGN KEY (host_id) REFERENCES UserGroup (id);

ALTER TABLE GroupProfUsrACR ADD CONSTRAINT FKo3kswy5ax5h6c6ao21ubdih8o
FOREIGN KEY (host_id) REFERENCES UserGroup (id);

ALTER TABLE IdGenPermGrpACR ADD CONSTRAINT FKeai1jrb5f522qfq70p3oq5ph5
FOREIGN KEY (subject_id) REFERENCES UserGroup (id);

ALTER TABLE IdGenProfGrpACR ADD CONSTRAINT FK1soo14x8o767oboi3k4xq6a75
FOREIGN KEY (subject_id) REFERENCES UserGroup (id);

ALTER TABLE Permission ADD CONSTRAINT FKmydq52kdr5u8mah355qdc00qf
FOREIGN KEY (subject_id) REFERENCES UserGroup (id);

ALTER TABLE SystemPermGrpACR ADD CONSTRAINT FK3jv5jn809obpxl8owmq3cs7o5
FOREIGN KEY (subject_id) REFERENCES UserGroup (id);

ALTER TABLE SystemProfGrpACR ADD CONSTRAINT FK8wbpwp547sycebpc4xqc0ikrg
FOREIGN KEY (subject_id) REFERENCES UserGroup (id);

ALTER TABLE TagPermGrpACR ADD CONSTRAINT FKmr5f3x121ndijqgwsayxklbtn
FOREIGN KEY (subject_id) REFERENCES UserGroup (id);

ALTER TABLE TagProfGrpACR ADD CONSTRAINT FKey1lxe9pspnvkajsd9ebntje4
FOREIGN KEY (subject_id) REFERENCES UserGroup (id);

ALTER TABLE TemplatePermGrpACR ADD CONSTRAINT FK72sqcoxks9xp8lv8aw03kf8o3 FOREIGN KEY (subject_id) REFERENCES UserGroup (id);
ALTER TABLE TemplateProfGrpACR ADD CONSTRAINT FK68m9cp82i31alwb0mcqiajluo FOREIGN KEY (subject_id) REFERENCES UserGroup (id);

CREATE TABLE UserGroup_User (
    groups_id BIGINT NOT NULL,
    users_id  BIGINT NOT NULL,
    CONSTRAINT FK7t0wbkhu02mbvoxwt7np5h0xv
    FOREIGN KEY (groups_id) REFERENCES UserGroup (id),
    CONSTRAINT FK77fyj1avmh71l1dgqu5rl516l
    FOREIGN KEY (users_id) REFERENCES User (id)
);

CREATE INDEX FK77fyj1avmh71l1dgqu5rl516l ON UserGroup_User (users_id);
CREATE INDEX FK7t0wbkhu02mbvoxwt7np5h0xv ON UserGroup_User (groups_id);

CREATE TABLE UserGroup_UserGroup (
    UserGroup_id BIGINT NOT NULL,
    groups_id    BIGINT NOT NULL,
    CONSTRAINT FK2eixf3lpm38fj2ffey19uqnqg
    FOREIGN KEY (UserGroup_id) REFERENCES UserGroup (id),
    CONSTRAINT FKdm8ojg4ou9wj3j5r9s9mhp6me
    FOREIGN KEY (groups_id) REFERENCES UserGroup (id)
);

CREATE INDEX FK2eixf3lpm38fj2ffey19uqnqg ON UserGroup_UserGroup (UserGroup_id);
CREATE INDEX FKdm8ojg4ou9wj3j5r9s9mhp6me ON UserGroup_UserGroup (groups_id);


-- New Security tables
CREATE TABLE AccessPermission(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    access_type VARCHAR(255),
    user_id BIGINT NOT NULL,
    access_tag_id BIGINT NOT NULL
);

ALTER TABLE AccessPermission ADD CONSTRAINT access_permission_user_fk FOREIGN KEY (user_id) REFERENCES User (id);
ALTER TABLE AccessPermission ADD CONSTRAINT access_permission_access_tag_fk FOREIGN KEY (access_tag_id) REFERENCES AccessTag (id);
CREATE UNIQUE INDEX access_permission_id_index ON AccessPermission (id);

CREATE TABLE SecurityToken
(
    id VARCHAR(500) PRIMARY KEY NOT NULL,
    invalidation_date DATETIME NOT NULL
);
CREATE UNIQUE INDEX SecurityToken_id_uindex ON SecurityToken (id);