
docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=1111 --name mysql_test mysql

CREATE DATABASE dev DEFAULT CHARACTER SET utf8mb3;

CREATE TABLE object_key_info (
  object_key varchar(100) NOT NULL,
  last_modified timestamp NOT NULL,
  PRIMARY KEY (object_key)
);