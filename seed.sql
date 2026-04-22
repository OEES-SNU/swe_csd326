-- Reset sequences and insert with explicit IDs
ALTER SEQUENCE users_id_seq RESTART WITH 1;
ALTER SEQUENCE courses_id_seq RESTART WITH 1;
ALTER SEQUENCE enrollments_id_seq RESTART WITH 1;
ALTER SEQUENCE questions_id_seq RESTART WITH 1;
ALTER SEQUENCE exams_id_seq RESTART WITH 1;
ALTER SEQUENCE exam_questions_id_seq RESTART WITH 1;

-- USERS (password = "password123")
INSERT INTO users (id, name, email, password, role, active) VALUES
(1, 'Admin User',      'admin@oees.com',      '$2a$10$tv4mCCuTlmJJcb.zExYTt.wrhhOlQ1dk5M3.zYHyLWp5qGnq.OTuC', 'ADMIN',      true),
(2, 'Dr. Sarah Khan',  'instructor@oees.com', '$2a$10$tv4mCCuTlmJJcb.zExYTt.wrhhOlQ1dk5M3.zYHyLWp5qGnq.OTuC', 'INSTRUCTOR', true),
(3, 'Alice Student',   'alice@oees.com',      '$2a$10$tv4mCCuTlmJJcb.zExYTt.wrhhOlQ1dk5M3.zYHyLWp5qGnq.OTuC', 'STUDENT',    true),
(4, 'Bob Student',     'bob@oees.com',        '$2a$10$tv4mCCuTlmJJcb.zExYTt.wrhhOlQ1dk5M3.zYHyLWp5qGnq.OTuC', 'STUDENT',    true),
(5, 'Charlie Student', 'charlie@oees.com',    '$2a$10$tv4mCCuTlmJJcb.zExYTt.wrhhOlQ1dk5M3.zYHyLWp5qGnq.OTuC', 'STUDENT',    true);

-- COURSES
INSERT INTO courses (id, course_code, course_name, description, instructor_id, active) VALUES
(1, 'CS101', 'Introduction to Computer Science', 'Fundamentals of programming and computational thinking.', 2, true),
(2, 'CS201', 'Data Structures & Algorithms',     'Arrays, linked lists, trees, graphs, sorting and searching.', 2, true),
(3, 'CS301', 'Database Management Systems',      'Relational databases, SQL, normalization and transactions.', 2, true);

-- ENROLLMENTS
INSERT INTO enrollments (id, student_id, course_id, status) VALUES
(1, 3, 1, 'APPROVED'),
(2, 3, 2, 'APPROVED'),
(3, 3, 3, 'APPROVED'),
(4, 4, 1, 'APPROVED'),
(5, 4, 2, 'APPROVED'),
(6, 4, 3, 'APPROVED'),
(7, 5, 1, 'APPROVED');

-- QUESTIONS for CS101
INSERT INTO questions (id, content, type, difficulty_level, marks, unit, option_a, option_b, option_c, option_d, correct_answer, course_id, created_by, active) VALUES
(1, 'What does CPU stand for?', 'MULTIPLE_CHOICE', 'EASY', 2, 'Unit 1', 'Central Processing Unit', 'Central Program Unit', 'Computer Processing Unit', 'Core Processing Unit', 'A', 1, 2, true),
(2, 'Which data type stores a single character in Java?', 'MULTIPLE_CHOICE', 'EASY', 2, 'Unit 1', 'String', 'char', 'int', 'byte', 'B', 1, 2, true),
(3, 'What is the output of 5 % 2?', 'MULTIPLE_CHOICE', 'EASY', 2, 'Unit 1', '2', '2.5', '1', '0', 'C', 1, 2, true),
(4, 'A variable declared inside a function is called a _______ variable.', 'FILL_IN_THE_BLANK', 'MEDIUM', 3, 'Unit 2', NULL, NULL, NULL, NULL, NULL, 1, 2, true),
(5, 'Explain the difference between compiled and interpreted programming languages. Give one example of each.', 'DESCRIPTIVE', 'MEDIUM', 5, 'Unit 2', NULL, NULL, NULL, NULL, NULL, 1, 2, true),
(6, 'Which of the following is NOT a programming paradigm?', 'MULTIPLE_CHOICE', 'MEDIUM', 2, 'Unit 2', 'Object-Oriented', 'Functional', 'Sequential', 'Declarative', 'C', 1, 2, true),
(7, 'What is recursion in programming?', 'DESCRIPTIVE', 'HARD', 5, 'Unit 3', NULL, NULL, NULL, NULL, NULL, 1, 2, true),
(8, 'The time complexity of binary search is:', 'MULTIPLE_CHOICE', 'MEDIUM', 2, 'Unit 3', 'O(n)', 'O(n^2)', 'O(log n)', 'O(1)', 'C', 1, 2, true);

-- QUESTIONS for CS201
INSERT INTO questions (id, content, type, difficulty_level, marks, unit, option_a, option_b, option_c, option_d, correct_answer, course_id, created_by, active) VALUES
(9,  'Which data structure uses LIFO order?', 'MULTIPLE_CHOICE', 'EASY', 2, 'Unit 1', 'Queue', 'Stack', 'Array', 'Linked List', 'B', 2, 2, true),
(10, 'Worst-case time complexity of bubble sort?', 'MULTIPLE_CHOICE', 'MEDIUM', 2, 'Unit 2', 'O(n)', 'O(n log n)', 'O(n^2)', 'O(log n)', 'C', 2, 2, true),
(11, 'In a BST, the left child is always _______ than the parent.', 'FILL_IN_THE_BLANK', 'EASY', 2, 'Unit 3', NULL, NULL, NULL, NULL, NULL, 2, 2, true),
(12, 'Explain the difference between a stack and a queue with real-world examples.', 'DESCRIPTIVE', 'MEDIUM', 5, 'Unit 1', NULL, NULL, NULL, NULL, NULL, 2, 2, true),
(13, 'Which traversal visits root, left, then right?', 'MULTIPLE_CHOICE', 'MEDIUM', 2, 'Unit 3', 'Inorder', 'Postorder', 'Preorder', 'Level order', 'C', 2, 2, true);

-- QUESTIONS for CS301
INSERT INTO questions (id, content, type, difficulty_level, marks, unit, option_a, option_b, option_c, option_d, correct_answer, course_id, created_by, active) VALUES
(14, 'What does SQL stand for?', 'MULTIPLE_CHOICE', 'EASY', 2, 'Unit 1', 'Structured Query Language', 'Simple Query Language', 'Standard Query Logic', 'Structured Question Language', 'A', 3, 2, true),
(15, 'Which SQL keyword retrieves data?', 'MULTIPLE_CHOICE', 'EASY', 2, 'Unit 1', 'INSERT', 'UPDATE', 'SELECT', 'DELETE', 'C', 3, 2, true),
(16, 'A primary key must be _______.', 'FILL_IN_THE_BLANK', 'EASY', 2, 'Unit 2', NULL, NULL, NULL, NULL, NULL, 3, 2, true),
(17, 'Explain database normalization and why it is important.', 'DESCRIPTIVE', 'HARD', 5, 'Unit 3', NULL, NULL, NULL, NULL, NULL, 3, 2, true),
(18, 'Which normal form eliminates transitive dependencies?', 'MULTIPLE_CHOICE', 'HARD', 3, 'Unit 3', '1NF', '2NF', '3NF', 'BCNF', 'C', 3, 2, true);

-- EXAMS
INSERT INTO exams (id, title, course_id, instructor_id, duration_minutes, total_marks, max_attempts, pass_mark, start_time, end_time, status, created_at) VALUES
(1, 'CS101 Midterm Exam',   1, 2, 30, 16, 2, 10, NOW() - INTERVAL '1 day',   NOW() + INTERVAL '7 days',  'ACTIVE',    NOW()),
(2, 'CS101 Final Exam',     1, 2, 45, 21, 1, 13, NOW() + INTERVAL '14 days', NOW() + INTERVAL '21 days', 'SCHEDULED', NOW()),
(3, 'CS201 Quiz 1',         2, 2, 20, 13, 2,  8, NOW() - INTERVAL '2 days',  NOW() + INTERVAL '5 days',  'ACTIVE',    NOW()),
(4, 'CS301 SQL Assessment', 3, 2, 25, 12, 1,  8, NOW() - INTERVAL '3 days',  NOW() - INTERVAL '1 day',   'EXPIRED',   NOW());

-- EXAM QUESTIONS
INSERT INTO exam_questions (exam_id, question_id, marks, order_index) VALUES
(1, 1, 2, 1), (1, 2, 2, 2), (1, 3, 2, 3), (1, 4, 3, 4), (1, 5, 5, 5), (1, 6, 2, 6),
(2, 1, 2, 1), (2, 2, 2, 2), (2, 3, 2, 3), (2, 4, 3, 4), (2, 5, 5, 5), (2, 6, 2, 6), (2, 7, 5, 7), (2, 8, 2, 8),
(3, 9, 2, 1), (3, 10, 2, 2), (3, 11, 2, 3), (3, 12, 5, 4), (3, 13, 2, 5),
(4, 14, 2, 1), (4, 15, 2, 2), (4, 16, 2, 3), (4, 17, 5, 4), (4, 18, 3, 5);

-- Update sequences to avoid conflicts on future inserts
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('courses_id_seq', (SELECT MAX(id) FROM courses));
SELECT setval('enrollments_id_seq', (SELECT MAX(id) FROM enrollments));
SELECT setval('questions_id_seq', (SELECT MAX(id) FROM questions));
SELECT setval('exams_id_seq', (SELECT MAX(id) FROM exams));
SELECT setval('exam_questions_id_seq', (SELECT MAX(id) FROM exam_questions));
