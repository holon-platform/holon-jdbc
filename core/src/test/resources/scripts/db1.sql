CREATE TABLE IF NOT EXISTS test1 (
	 id bigint primary key, 
	 str varchar(100)
);

delete from test1;
INSERT INTO test1 VALUES (1, 'One');

COMMIT;