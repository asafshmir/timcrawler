select *
from tim.sessions s, tim.tests t 
where s.fk_test_id=t.id and session_num>0 and
timediff(t.end_time,s.last_seen) > '00:30:00' and t.id=96
group by s.peer_id
limit 10000