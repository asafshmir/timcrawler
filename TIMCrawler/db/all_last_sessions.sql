select * from tim.tests t, tim.sessions s,
(select peer_id,fk_test_id,peer_ip,max(session_num) s_max
from tim.sessions where session_num>0 and fk_test_id=178
group by peer_id) tmp
where s.fk_test_id=t.id and s.peer_id=tmp.peer_id and s.fk_test_id=tmp.fk_test_id
and s.peer_ip=tmp.peer_ip and s.session_num=tmp.s_max
and (timediff(t.end_time,s.last_seen) > '00:30:00' or completion_rate>=0.95)
limit 10000