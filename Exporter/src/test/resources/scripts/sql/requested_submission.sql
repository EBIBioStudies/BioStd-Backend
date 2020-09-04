-- Submission
INSERT INTO Submission (
    id, CTime, MTime, RTime, accNo, relPath, method, released, rootPath, title, version, owner_id, submitter_id, status)
VALUES (5,
        1460389106,
        1460389106,
        1460389106,
        'S-TEST124',
        'S-TEST/S-TESTxxx124/S-TEST124',
        'FILE',
        true,
        'S-TEST124',
        'Async Submission Requested',
        1,
        1,
        1,
        'REQUESTED');

-- Submission Access Tag
INSERT INTO Submission_AccessTag (Submission_id, accessTags_id) VALUES (5, 1);

-- Sections
INSERT INTO Section (id, accNo, parentAccNo, tableIndex, type, parent_id, submission_id, ord)
VALUES (17, null, null, -1, 'Study', null, 5, null);

-- Set RootSection
update Submission set rootSection_id = 17 where id = 5;
