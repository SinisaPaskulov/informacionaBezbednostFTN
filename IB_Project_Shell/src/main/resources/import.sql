set foreign_key_checks = 0;

truncate table authority;
truncate table users;
truncate table user_authority;

set foreign_key_checks = 1;

insert into authority (name) values ('ADMIN');
insert into authority (name) values ('REGULAR');

INSERT INTO USERS (email, password, certificate, active) VALUES ('admin@gmail.com','$2a$04$SwzgBrIJZhfnzOw7KFcdzOTiY6EFVwIpG7fkF/D1w26G1.fWsi.aK', './data/admin.jks' , 1);
INSERT INTO USERS (email, password, certificate, active) VALUES ('user@gmail.com','$2a$04$Amda.Gm4Q.ZbXz9wcohDHOhOBaNQAkSS1QO26Eh8Hovu3uzEpQvcq', '' , 1);

insert into user_authority (user_id, authority_id) values (1, 1); -- admin has ROLE_ADMIN
insert into user_authority (user_id, authority_id) values (2, 2); -- user has ROLE_USER