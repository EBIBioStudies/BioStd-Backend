-- User
INSERT INTO User (activationKey, active, auxProfileInfo, email, fullName, keyTime, login, passwordDigest, secret, superuser, ssoSubject) VALUES (null, true, null, 'biostudies-dev@ebi.ac.uk', 'Biostudy manager', 0, 'manager', 0x9C22709B6EFF014C8D11A52E4EBA161CFC2D2DFC, '3bd53494-e672-41d4-b491-27f77bdb59b8', true, null);

-- AccessTags
INSERT INTO AccessTag (id, description, name, owner_id, parent_tag_id) VALUES (1, null, 'Public', 1, null);
