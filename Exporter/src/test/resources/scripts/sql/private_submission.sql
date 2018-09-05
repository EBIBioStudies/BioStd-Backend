-- Submission
INSERT INTO Submission (CTime, MTime, RTime, accNo, relPath, released, rootPath, title, version, owner_id, secretKey) VALUES (1460402396, 1460402396, 1460402396, 'S-EPMC2873748', 'S-EPMC/S-EPMCxxx748/S-EPMC2873748', true, 'S-EPMC/S-EPMCxxx748/S-EPMC2873748', 'Cost-effectiveness of a potential vaccine for human papillomavirus.', 1, 1, null);

-- Sections
INSERT INTO Section (accNo, global, parentAccNo, tableIndex, type, parent_id, submission_id, ord) VALUES (null, false, null, -1, 'Study', null, 1, null);
INSERT INTO Section (accNo, global, parentAccNo, tableIndex, type, parent_id, submission_id, ord) VALUES ('PMC2873748', false, null, -1, 'Publication', 1, 1, 0);
INSERT INTO Section (accNo, global, parentAccNo, tableIndex, type, parent_id, submission_id, ord) VALUES ('a1', false, null, -1, 'Author', 1, 1, 1);
INSERT INTO Section (accNo, global, parentAccNo, tableIndex, type, parent_id, submission_id, ord) VALUES ('a2', false, null, -1, 'Author', 1, 1, 2);
INSERT INTO Section (accNo, global, parentAccNo, tableIndex, type, parent_id, submission_id, ord) VALUES ('o1', false, null, -1, 'Organization', 1, 1, 3);

-- Set RootSection
update Submission set rootSection_id = 1 where id = 1;

-- Sections files
INSERT INTO FileRef (directory, name, size, tableIndex, sectionId, ord, path) VALUES (false, '02-0168_appT-s1.pdf', 34984, -1, 1, 0, null);
INSERT INTO FileAttribute (name, nameQualifierString, numValue, reference, value, valueQualifierString, file_id, ord) VALUES ('Type', null, 0, false, 'application(pdf)', null, 1, 0);

-- Section attributes
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
