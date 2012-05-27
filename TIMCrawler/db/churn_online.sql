select *, timediff(now(),last_seen), now(), last_seen, max(last_seen), max(session_num)
from tim.sessions 
where completion_rate<1 and timediff(now(),last_seen) > '00:15:00' and fk_test_id=53
group by peer_id