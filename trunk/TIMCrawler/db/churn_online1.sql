select *,timediff(now(),last_seen) from tim.sessions s,
(select peer_id,fk_test_id,peer_ip,max(session_num) s_max from tim.sessions
where completion_rate<=1 and timediff(now(),last_seen) > '00:30:00' and fk_test_id=95
group by peer_id,fk_test_id,peer_ip) tmp
where s.peer_id=tmp.peer_id and s.fk_test_id=tmp.fk_test_id
and s.peer_ip=tmp.peer_ip 
and s.session_num=tmp.s_max