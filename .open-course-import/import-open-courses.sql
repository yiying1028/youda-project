SET NAMES utf8mb4;

UPDATE course
SET name = 'MIT OCW: Solid State Chemistry',
    description = 'Downloadable MIT OpenCourseWare lecture videos stored locally for the demo environment.',
    cover_image = '/uploads/course-cover/mit-chemistry.svg',
    teacher_name = 'Donald Sadoway',
    subject_id = 5,
    grade_id = 12,
    chapter_count = 2,
    student_count = 5
WHERE id = 1;

UPDATE course
SET name = 'MIT OCW: Nonlinear Programming',
    description = 'Downloadable MIT OpenCourseWare lecture videos stored locally for the demo environment.',
    cover_image = '/uploads/course-cover/mit-nonlinear.svg',
    teacher_name = 'Robert M. Freund',
    subject_id = 2,
    grade_id = 12,
    chapter_count = 2,
    student_count = 4
WHERE id = 2;

UPDATE course_chapter SET course_id = 1, title = 'Course Introduction', sort = 1 WHERE id = 1;
UPDATE course_chapter SET course_id = 1, title = 'Periodic Table', sort = 2 WHERE id = 2;
UPDATE course_chapter SET course_id = 2, title = 'Lecture 10 Feb 2004', sort = 1 WHERE id = 3;
UPDATE course_chapter SET course_id = 2, title = 'Lecture 03 May 2004', sort = 2 WHERE id = 4;

UPDATE course_video
SET course_id = 1,
    chapter_id = 1,
    title = 'Lecture 1: Introduction to Solid State Chemistry',
    video_url = '/uploads/open-course/mit-chemistry-intro.mp4',
    duration = 0,
    sort = 1
WHERE id = 1;

UPDATE course_video
SET course_id = 1,
    chapter_id = 2,
    title = 'Lecture 2: The Periodic Table',
    video_url = '/uploads/open-course/mit-chemistry-periodic-table.mp4',
    duration = 0,
    sort = 1
WHERE id = 2;

UPDATE course_video
SET course_id = 2,
    chapter_id = 3,
    title = 'MIT OCW Nonlinear Programming - 10 Feb 2004',
    video_url = '/uploads/open-course/mit-nonlinear-10feb2004.mp4',
    duration = 0,
    sort = 1
WHERE id = 3;

UPDATE course_video
SET course_id = 2,
    chapter_id = 4,
    title = 'MIT OCW Nonlinear Programming - 03 May 2004',
    video_url = '/uploads/open-course/mit-nonlinear-03may2004.mp4',
    duration = 0,
    sort = 1
WHERE id = 4;

DELETE FROM learning_progress WHERE video_id NOT IN (1,2,3,4);