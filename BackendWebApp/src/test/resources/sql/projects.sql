INSERT INTO Submission (id, CTime, MTime, RTime, accNo, relPath, released, rootPath, title, version, owner_id, rootSection_id, secretKey) VALUES (1, 1460387622, 1460387622, -1, 'Test-Project', 'Test-Project', false, 'Test-Project', 'Test-Project', 1, 1, null, 'c64d22e1-cce1-4a79-ba13-3da130f40051');

INSERT INTO Section (id, accNo, global, parentAccNo, tableIndex, type, parent_id, submission_id, ord) VALUES (1, null, false, null, -1, 'Project', null, 1, null);

UPDATE Submission set rootSection_id = 1 where id = 1;

INSERT INTO AccessTag (id, description, name, owner_id, parent_tag_id) VALUES (2, null, 'Test-Project', 2, null);

INSERT INTO Submission_AccessTag (Submission_id, accessTags_id) VALUES (1, 2);
