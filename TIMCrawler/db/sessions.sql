SELECT *,timediff(last_seen,start_time) FROM tim.sessions
where timediff(last_seen,start_time) > '00:00:00' and fk_test_id=(select max(id) from tim.tests)