-- User
INSERT INTO User (activationKey, active, auxProfileInfo, email, fullName, keyTime, login, passwordDigest, secret, superuser, ssoSubject) VALUES (null, true, null, 'biostudies-dev@ebi.ac.uk', 'Biostudy manager', 0, 'manager', 0x9C22709B6EFF014C8D11A52E4EBA161CFC2D2DFC, '3bd53494-e672-41d4-b491-27f77bdb59b8', true, null);

-- AccessTags
INSERT INTO AccessTag (id, description, name, owner_id, parent_tag_id) VALUES (1, null, 'Public', 1, null);

-- Submission
INSERT INTO Submission (CTime, MTime, RTime, accNo, relPath, released, rootPath, title, version, owner_id, secretKey) VALUES (1460402396, 1460402396, 1460402396, 'S-EPMC2873748', 'S-EPMC/S-EPMCxxx748/S-EPMC2873748', true, 'S-EPMC/S-EPMCxxx748/S-EPMC2873748', 'Cost-effectiveness of a potential vaccine for human papillomavirus.', 1, 1, null);
INSERT INTO Submission (CTime, MTime, RTime, accNo, relPath, released, rootPath, title, version, owner_id, secretKey) VALUES (1460389106, 1460389106, 1460389106, 'S-EPMC3343633', 'S-EPMC/S-EPMCxxx633/S-EPMC3343633', true, 'S-EPMC/S-EPMCxxx633/S-EPMC3343633', 'Transcription factor RORα is critical for nuocyte development.', 1, 1, null);

-- Submission Access Tag
INSERT INTO Submission_AccessTag (Submission_id, accessTags_id) VALUES (2, 1);

-- Sections
-- Submission 1
INSERT INTO Section (accNo, global, parentAccNo, tableIndex, type, parent_id, submission_id, ord) VALUES (null, false, null, -1, 'Study', null, 1, null);
INSERT INTO Section (accNo, global, parentAccNo, tableIndex, type, parent_id, submission_id, ord) VALUES ('PMC2873748', false, null, -1, 'Publication', 1, 1, 0);
INSERT INTO Section (accNo, global, parentAccNo, tableIndex, type, parent_id, submission_id, ord) VALUES ('a1', false, null, -1, 'Author', 1, 1, 1);
INSERT INTO Section (accNo, global, parentAccNo, tableIndex, type, parent_id, submission_id, ord) VALUES ('a2', false, null, -1, 'Author', 1, 1, 2);
INSERT INTO Section (accNo, global, parentAccNo, tableIndex, type, parent_id, submission_id, ord) VALUES ('o1', false, null, -1, 'Organization', 1, 1, 3);
-- Submission 2
INSERT INTO Section (accNo, global, parentAccNo, tableIndex, type, parent_id, submission_id, ord) VALUES (null, false, null, -1, 'Study', null, 2, null);
INSERT INTO Section (accNo, global, parentAccNo, tableIndex, type, parent_id, submission_id, ord) VALUES ('PMC3343633', false, null, -1, 'Publication', 6, 2, 0);
INSERT INTO Section (accNo, global, parentAccNo, tableIndex, type, parent_id, submission_id, ord) VALUES ('a1', false, null, -1, 'Author', 6, 2, 1);
INSERT INTO Section (accNo, global, parentAccNo, tableIndex, type, parent_id, submission_id, ord) VALUES ('a2', false, null, -1, 'Author', 6, 2, 2);
INSERT INTO Section (accNo, global, parentAccNo, tableIndex, type, parent_id, submission_id, ord) VALUES ('o1', false, null, -1, 'Organization', 6, 2, 3);

-- Set RootSection
update Submission set rootSection_id = 1 where id = 1;
update Submission set rootSection_id = 6 where id = 2;

-- Sections files
-- Submission 1
INSERT INTO FileRef (directory, name, size, tableIndex, sectionId, ord, path) VALUES (false, '02-0168_appT-s1.pdf', 34984, -1, 1, 0, null);
INSERT INTO FileAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, file_id, ord) VALUES ('Type', null, 0, false, 'application(pdf)', null, 1, 0);
-- Submission 2
INSERT INTO FileRef (directory, name, size, tableIndex, sectionId, ord, path) VALUES (false, 'NIHMS40251-supplement-1.pdf', 9170139, -1, 6, 0, null);
INSERT INTO FileAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, file_id, ord) VALUES ('Type', null, 0, false, 'application(pdf)', null, 2, 0);

-- Section attributes
-- Submission 1
INSERT INTO SectionAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, section_id, ord) VALUES ('Title', null, 0, false, 'Cost-effectiveness of a potential vaccine for human papillomavirus.', null, 1, 0);
INSERT INTO SectionAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, section_id, ord) VALUES ('Abstract', null, 0, false, 'Human papillomavirus (HPV) ', null, 1, 1);
INSERT INTO SectionAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, section_id, ord) VALUES ('Journal', null, 0, false, 'Emerging infectious diseases', null, 2, 0);
INSERT INTO SectionAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, section_id, ord) VALUES ('Volume', null, 0, false, '9(1)', null, 2, 1);
INSERT INTO SectionAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, section_id, ord) VALUES ('Pages', null, 0, false, '37-48', null, 2, 2);
INSERT INTO SectionAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, section_id, ord) VALUES ('Publication date', null, 0, false, '2003 Jan', null, 2, 3);
INSERT INTO SectionAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, section_id, ord) VALUES ('Name', null, 0, false, 'Sanders GD', null, 3, 0);
INSERT INTO SectionAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, section_id, ord) VALUES ('affiliation', null, 0, true, 'o1', null, 3, 1);
INSERT INTO SectionAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, section_id, ord) VALUES ('Name', null, 0, false, 'Taira AV', null, 4, 0);
INSERT INTO SectionAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, section_id, ord) VALUES ('affiliation', null, 0, true, 'o1', null, 4, 1);
INSERT INTO SectionAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, section_id, ord) VALUES ('Name', null, 0, false, '* Stanford University, Stanford, California, USA', null, 5, 0);
-- Submission 2
INSERT INTO SectionAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, section_id, ord) VALUES ('Title', null, 0, false, 'Transcription factor RORα is critical for nuocyte development.', null, 6, 0);
INSERT INTO SectionAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, section_id, ord) VALUES ('Abstract', null, 0, false, 'Nuocytes are essential in innate type 2 immunity and contribute to the exacerbation of asthma responses.', null, 6, 1);
INSERT INTO SectionAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, section_id, ord) VALUES ('Journal', null, 0, false, 'Nature immunology', null, 7, 0);
INSERT INTO SectionAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, section_id, ord) VALUES ('Volume', null, 0, false, '13(3)', null, 7, 1);
INSERT INTO SectionAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, section_id, ord) VALUES ('Pages', null, 0, false, '229-36', null, 7, 2);
INSERT INTO SectionAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, section_id, ord) VALUES ('Publication date', null, 0, false, '2012 Mar', null, 7, 3);
INSERT INTO SectionAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, section_id, ord) VALUES ('Name', null, 0, false, 'Wong SH', null, 8, 0);
INSERT INTO SectionAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, section_id, ord) VALUES ('affiliation', null, 0, true, 'o1', null, 8, 1);
INSERT INTO SectionAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, section_id, ord) VALUES ('Name', null, 0, false, 'Walker JA', null, 9, 0);
INSERT INTO SectionAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, section_id, ord) VALUES ('affiliation', null, 0, true, 'o1', null, 9, 1);
INSERT INTO SectionAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, section_id, ord) VALUES ('Name', null, 0, false, 'MRC Laboratory of Molecular Biology, Hills Road, Cambridge, CB0QH, UK', null, 10, 0);