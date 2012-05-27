SELECT *,timediff(last_seen,start_time) FROM tim.sessions
where timediff(last_seen,start_time) > '00:01:00' and fk_test_id=53;