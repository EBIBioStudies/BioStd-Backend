INSERT INTO User (activationKey, active, auxProfileInfo, email, fullName, keyTime, login, passwordDigest, secret, superuser, ssoSubject) VALUES (null, false, null, null, 'Anonymous user', 0, '@Guest', null, null, false, null);
INSERT INTO User (activationKey, active, auxProfileInfo, email, fullName, keyTime, login, passwordDigest, secret, superuser, ssoSubject) VALUES (null, false, null, null, 'Represents system owned objects', 0, '@System', null, null, false, null);
INSERT INTO User (activationKey, active, auxProfileInfo, email, fullName, keyTime, login, passwordDigest, secret, superuser, ssoSubject) VALUES (null, true, null, 'admin_user@ebi.ac.uk', 'admin_user', 0, 'admin_user@ebi.ac.uk', X'7C4A8D09CA3762AF61E59520943DC26494F8941B', '69214a2f-f80b-4f33-86b7-26d3bd0453aa', true, null);
INSERT INTO User (activationKey, active, auxProfileInfo, email, fullName, keyTime, login, passwordDigest, secret, superuser, ssoSubject) VALUES (null, true, null, 'change_password@ebi.ac.uk', 'change_password', 0, 'change_password@ebi.ac.uk', X'7C4A8D09CA3762AF61E59520943DC26494F8941B', '69214a2f-f80b-4f33-86b7-26d3bd0453aa', true, null);