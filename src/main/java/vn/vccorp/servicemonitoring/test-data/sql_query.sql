--!WIP!
--!NOT ALL FIELDS ARE INSERTED!

--insert new server records
INSERT INTO server (ip, description, name, status) VALUES ('127.0.0.1', 'Test server', 'local', 'Running');
INSERT INTO server (ip, description, name, status) VALUES ('192.168.1.1', 'Test server 2', 'local', 'Running');

--insert new service records
INSERT INTO service (pid, server_id, start_time) VALUES ('100', 1, NOW());
INSERT INTO service (pid, server_id, start_time) VALUES ('200', 2, NOW());
INSERT INTO service (pid, server_id, start_time) VALUES ('300', 1, NOW());

--insert user_service records (user & service many-to-many)
INSERT INTO user_service (service_id, user_id, role) VALUES (1, 1, 'OWNER');
INSERT INTO user_service (service_id, user_id, role) VALUES (2, 1, 'MAINTAINER');
INSERT INTO user_service (service_id, user_id, role) VALUES (3, 1, 'OWNER');

--insert user_server records (user & server many-to-many)
INSERT INTO user_server (server_id, user_id, groups, username) VALUES (1, 1, 'sudoer', 'root');
INSERT INTO user_server (server_id, user_id, groups, username) VALUES (2, 1, 'ssh', 'root');
