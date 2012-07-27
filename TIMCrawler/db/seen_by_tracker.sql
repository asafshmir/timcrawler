select * from tim.tests t, tim.sessions s,
(select peer_id,fk_test_id,peer_ip,max(session_num) s_max
from tim.sessions where session_num>0 and fk_test_id=95
group by peer_id) tmp,
(select fk_test_id, peer_ip, peer_port, last_seen_by_tracker from tim.sessions where session_num = 0) zeros
where s.fk_test_id=t.id and s.peer_id=tmp.peer_id and s.fk_test_id=tmp.fk_test_id
and s.peer_ip=tmp.peer_ip and s.session_num=tmp.s_max
and timediff(t.end_time,s.last_seen) > '00:30:00' and completion_rate<=0.95
and zeros.fk_test_id = t.id and zeros.peer_ip = s.peer_ip and zeros.peer_port = s.peer_port and timediff(zeros.last_seen_by_tracker, s.last_seen) > '01:00:00'
limit 10000