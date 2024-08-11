insert into authors(full_name)
values ('author_db_1'), ('author_db_2'), ('author_db_3');

insert into genres(name)
values ('genre_db_1'), ('genre_db_2'), ('genre_db_3'),
       ('genre_db_4'), ('genre_db_5'), ('genre_db_6');

insert into books(title, author_id)
values ('bookTitle_db_1', 1), ('bookTitle_db_2', 2), ('bookTitle_db_3', 3);

insert into books_genres(book_id, genre_id)
values (1, 1),   (1, 2),
       (2, 3),   (2, 4),
       (3, 5),   (3, 6);

insert into comments(content, book_id)
values ('comment_db_1', 1), ('comment_db_2', 1), ('comment_db_3', 1),
       ('comment_db_4', 2), ('comment_db_5', 2), ('comment_db_6', 2);
