-- Submission
INSERT INTO Submission (CTime, MTime, RTime, accNo, relPath, released, rootPath, title, version, owner_id, secretKey)
VALUES (1460389106, 1460389106, 1460389106, 'S-EPMC3343634', 'S-EPMC/S-EPMCxxx634/S-EPMC3343634', true, 'S-EPMC/S-EPMCxxx634/S-EPMC3343634', 'Transcription factor RORα is critical for nuocyte development.', 1, 1, null);

-- Submission Access Tag
INSERT INTO Submission_AccessTag (Submission_id, accessTags_id) VALUES (3, 1);

-- Sections
INSERT INTO Section (accNo, parentAccNo, tableIndex, type, parent_id, submission_id, ord)
VALUES ('SECT-001', null, -1, 'Study', null, 3, null);

INSERT INTO Section (accNo,  parentAccNo, tableIndex, type, parent_id, submission_id, ord)
VALUES ('PMC3343635', null, -1, 'Publication', 11, 3, 0);

INSERT INTO Section (accNo,  parentAccNo, tableIndex, type, parent_id, submission_id, ord)
VALUES ('a1', null, -1, 'Author', 11, 3, 1);

INSERT INTO Section (accNo,  parentAccNo, tableIndex, type, parent_id, submission_id, ord)
VALUES ('a2', null, -1, 'Author', 11, 3, 3);

INSERT INTO Section (accNo, parentAccNo, tableIndex, type, parent_id, submission_id, ord)
VALUES ('o1', null, -1, 'Organization', 11, 3, 3);

-- Set RootSection
update Submission set rootSection_id = 11 where id = 3;

-- File List
INSERT INTO FileList(name) VALUES('S-EPMC3343634.SECT-001.files');

INSERT INTO ReferencedFile (name, size, fileListId, path) VALUES ('NIHMS40251-supplement-1.pdf', 9170140, 1, null);

INSERT INTO ReferencedFileAttribute (name, reference, value, referenced_file_id, ord)
VALUES ('Type', false, 'application(pdf)', 1, 0);

UPDATE Section SET fileListId = 1 WHERE id = 11;

-- Section attributes
INSERT INTO SectionAttribute (name, nameQualifierString, reference, value, valueQualifierString, section_id, ord)
VALUES ('Title', null, false, 'Transcription factor RORα is critical for nuocyte development.', null, 11, 0);

INSERT INTO SectionAttribute (name, nameQualifierString, reference, value, valueQualifierString, section_id, ord)
VALUES ('Abstract', null, false, 'Nuocytes are essential in innate type 3 immunity', null, 11, 1);

INSERT INTO SectionAttribute (name, nameQualifierString, reference, value, valueQualifierString, section_id, ord)
VALUES ('File List', null, false, 'S-EPMC3343634.SECT-001.files', null, 11, 1);

INSERT INTO SectionAttribute (name, nameQualifierString, reference, value, valueQualifierString, section_id, ord)
VALUES ('Journal', null, false, 'Nature immunology', null, 12, 0);

INSERT INTO SectionAttribute (name, nameQualifierString, reference, value, valueQualifierString, section_id, ord)
VALUES ('Volume', null, false, '13(3)', null, 12, 1);

INSERT INTO SectionAttribute (name, nameQualifierString, reference, value, valueQualifierString, section_id, ord)
VALUES ('Pages', null, false, '229-36', null, 12, 2);

INSERT INTO SectionAttribute (name, nameQualifierString, reference, value, valueQualifierString, section_id, ord)
VALUES ('Publication date', null, false, '2012 Mar', null, 12, 3);

INSERT INTO SectionAttribute (name, nameQualifierString, reference, value, valueQualifierString, section_id, ord)
VALUES ('Name', null, false, 'Wong SH', null, 13, 0);

INSERT INTO SectionAttribute (name, nameQualifierString, reference, value, valueQualifierString, section_id, ord)
VALUES ('affiliation', null, true, 'o1', null, 13, 1);

INSERT INTO SectionAttribute (name, nameQualifierString, reference, value, valueQualifierString, section_id, ord)
VALUES ('Name', null, false, 'Walker JA', null, 14, 0);

INSERT INTO SectionAttribute (name, nameQualifierString, reference, value, valueQualifierString, section_id, ord)
VALUES ('affiliation', null, true, 'o1', null, 14, 1);

INSERT INTO SectionAttribute (name, nameQualifierString, reference, value, valueQualifierString, section_id, ord)
VALUES ('Name', null, false, 'MRC Laboratory of Molecular Biology, Hills Road, Cambridge, CB0QH, UK', null, 15, 0);
