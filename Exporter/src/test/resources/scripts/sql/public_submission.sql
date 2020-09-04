-- Submission
INSERT INTO Submission (
    CTime, MTime, RTime, accNo, relPath, method, released, rootPath, title, version, owner_id, submitter_id)
VALUES (1460389106,
        1460389106,
        1460389106,
        'S-EPMC3343633',
        'S-EPMC/S-EPMCxxx633/S-EPMC3343633',
        'PAGE_TAB',
        true,
        'S-EPMC/S-EPMCxxx633/S-EPMC3343633',
        'Transcription factor RORα is critical for nuocyte development.',
        1,
        1,
        1);

-- Submission Access Tag
INSERT INTO Submission_AccessTag (Submission_id, accessTags_id) VALUES (2, 1);

-- Sections
INSERT INTO Section (accNo, parentAccNo, tableIndex, type, parent_id, submission_id, ord)
VALUES (null, null, -1, 'Study', null, 2, null);

INSERT INTO Section (accNo,  parentAccNo, tableIndex, type, parent_id, submission_id, ord)
VALUES ('PMC3343633', null, -1, 'Publication', 6, 2, 0);

INSERT INTO Section (accNo,  parentAccNo, tableIndex, type, parent_id, submission_id, ord)
VALUES ('a1', null, -1, 'Author', 6, 2, 1);

INSERT INTO Section (accNo,  parentAccNo, tableIndex, type, parent_id, submission_id, ord)
VALUES ('a2', null, -1, 'Author', 6, 2, 2);

INSERT INTO Section (accNo, parentAccNo, tableIndex, type, parent_id, submission_id, ord)
VALUES ('o1', null, -1, 'Organization', 6, 2, 3);

-- Set RootSection
update Submission set rootSection_id = 6 where id = 2;

-- Sections files
INSERT INTO FileRef (directory, name, size, tableIndex, sectionId, ord, path)
VALUES (false, 'NIHMS40251-supplement-1.pdf', 9170139, -1, 6, 0, null);

INSERT INTO FileAttribute (name, nameQualifierString, reference, value, valueQualifierString, file_id, ord)
VALUES ('Type', null, false, 'application(pdf)', null, 2, 0);

-- Section attributes
INSERT INTO SectionAttribute (name, nameQualifierString, reference, value, valueQualifierString, section_id, ord)
VALUES ('Title', null, false, 'Transcription factor RORα is critical for nuocyte development.', null, 6, 0);

INSERT INTO SectionAttribute (name, nameQualifierString, reference, value, valueQualifierString, section_id, ord)
VALUES ('Abstract', null, false, 'Nuocytes are essential in innate type 2 immunity', null, 6, 1);

INSERT INTO SectionAttribute (name, nameQualifierString, reference, value, valueQualifierString, section_id, ord)
VALUES ('Journal', null, false, 'Nature immunology', null, 7, 0);

INSERT INTO SectionAttribute (name, nameQualifierString, reference, value, valueQualifierString, section_id, ord)
VALUES ('Volume', null, false, '13(3)', null, 7, 1);

INSERT INTO SectionAttribute (name, nameQualifierString, reference, value, valueQualifierString, section_id, ord)
VALUES ('Pages', null, false, '229-36', null, 7, 2);

INSERT INTO SectionAttribute (name, nameQualifierString, reference, value, valueQualifierString, section_id, ord)
VALUES ('Publication date', null, false, '2012 Mar', null, 7, 3);

INSERT INTO SectionAttribute (name, nameQualifierString, reference, value, valueQualifierString, section_id, ord)
VALUES ('Name', null, false, 'Wong SH', null, 8, 0);

INSERT INTO SectionAttribute (name, nameQualifierString, reference, value, valueQualifierString, section_id, ord)
VALUES ('affiliation', null, true, 'o1', null, 8, 1);

INSERT INTO SectionAttribute (name, nameQualifierString, reference, value, valueQualifierString, section_id, ord)
VALUES ('Name', null, false, 'Walker JA', null, 9, 0);

INSERT INTO SectionAttribute (name, nameQualifierString,  reference, value, valueQualifierString, section_id, ord)
VALUES ('affiliation', null, true, 'o1', null, 9, 1);

INSERT INTO SectionAttribute (name, nameQualifierString,  reference, value, valueQualifierString, section_id, ord)
VALUES ('Name', null, false, 'MRC Laboratory of Molecular Biology, Hills Road, Cambridge, CB0QH, UK', null, 10, 0);

INSERT INTO SubmissionStat(accNo, type, value) VALUES('S-EPMC3343633', 'VIEWS', 314);
