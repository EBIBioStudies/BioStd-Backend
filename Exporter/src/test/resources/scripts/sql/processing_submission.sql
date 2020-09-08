-- Submission
INSERT INTO Submission (
    id, CTime, MTime, RTime, accNo, relPath, method, released, rootPath, title, version, owner_id, submitter_id, status)
VALUES (4,
        1460389106,
        1460389106,
        1460389106,
        'S-TEST123',
        'S-TEST/S-TESTxxx123/S-TEST123',
        'FILE',
        true,
        'S-TEST123',
        'Async Submission In Process',
        1,
        1,
        1,
        'PROCESSING');

-- Submission Access Tag
INSERT INTO Submission_AccessTag (Submission_id, accessTags_id) VALUES (4, 1);

-- Sections
INSERT INTO Section (id, accNo, parentAccNo, tableIndex, type, parent_id, submission_id, ord)
VALUES (16, null, null, -1, 'Study', null, 4, null);

-- Set RootSection
update Submission set rootSection_id = 16 where id = 4;
