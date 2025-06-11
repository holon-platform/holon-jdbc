CREATE TABLE IF NOT EXISTS test2 (
	 id bigint primary key, 
	 str varchar(100)
);

delete from test2;
INSERT INTO test2 VALUES (2, 'Two');

COMMIT;