select *, timediff(t.end_time,s.last_seen), t.end_time, s.last_seen, max(s.last_seen)
from tim.sessions s, tim.tests t 
where s.fk_test_id=t.id and completion_rate<1 and timediff(t.end_time,s.last_seen) > '00:20:00' and t.id=51
group by s.peer_id