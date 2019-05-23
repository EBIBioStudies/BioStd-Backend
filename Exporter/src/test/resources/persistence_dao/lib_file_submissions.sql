-- Library file submission by AccNo configuration
INSERT INTO Submission (CTime, MTime, RTime, accNo, relPath, released, rootPath, title, VERSION, secretKey, owner_id)
VALUES (
  1514764800,
  1514764800,
  1514764800,
  'S-BIAD2',
  'S-BIAD/SBIAD-2',
  FALSE,
  'Test-Imaging-Submission-relPath-rootPath',
  'Test-Imaging-Submission-Title',
  1,
  'abc-123',
  1);

-- Library file submission by using library and referenced files
INSERT INTO Submission (CTime, MTime, RTime, accNo, relPath, released, rootPath, title, VERSION, secretKey, owner_id)
VALUES (1514764800, 1514764800, 1514764800, 'S-BIAD3', 'S-BIAD/SBIAD-3', FALSE, 'rootPath', 'Title', 1, 'abc-123', 1);

INSERT INTO Section (accNo, parentAccNo, tableIndex, type, parent_id, submission_id, ord)
VALUES ('SECT-001', null, -1, 'Study', null, 3, null);

INSERT INTO LibraryFile(name) VALUES('S-EPMC3343634.SECT-001.files');

INSERT INTO ReferencedFile (name, size, libraryFileId, path)
VALUES ('NIHMS40251-supplement-1.pdf', 9170139, 1, null);

INSERT INTO ReferencedFileAttribute (name, reference, value, referenced_file_id, ord)
VALUES ('Type', false, 'application(pdf)', 1, 0);

UPDATE Section SET libraryFileId = 1 WHERE id = 1;
