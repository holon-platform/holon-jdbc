CREATE TABLE IF NOT EXISTS test3 (
	 id bigint primary key, 
	 str varchar(100)
);

delete from test3;
INSERT INTO test3 VALUES (3, 'Three');

COMMIT;