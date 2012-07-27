select timediff(s.last_seen, s.start_time) as "last session length" , bitfield_recv, completion_rate, floor(completion_rate*20)*5 as "completion rate group", total_size as "total size (bytes)", 
last_dl_rate1 as "last min dl rate (kB/s)", if (floor(last_dl_rate1/10)*10 > 300, 300, floor(last_dl_rate1/10)*10) as "last  min dl rate group" , 
total_download_rate as "session dl rate (kB/s)", IF(ceil(total_download_rate/10)*10 > 300, 300, ceil(total_download_rate/10)*10) as "session dl rate group", 
total_size*(1-completion_rate)/1024/total_download_rate as "ETA (seconds)",
sec_to_time(total_size*(1-completion_rate)/1024/total_download_rate) as "ETA",
ceil((total_size*(1-completion_rate)/1024/total_download_rate)/1800)*1800 as "ETA group (seconds)",
sec_to_time(IF(ceil((total_size*(1-completion_rate)/1024/total_download_rate)/1800)*1800 > 24*3600, 24*3600,
                ceil((total_size*(1-completion_rate)/1024/total_download_rate)/1800)*1800)) as "ETA group"
from tim.tests t, tim.sessions s,
(select peer_id,fk_test_id,peer_ip,max(session_num) s_max
from tim.sessions where session_num>0 and fk_test_id in(117, 114, 111, 108)
group by peer_id) tmp
where s.fk_test_id=t.id and s.peer_id=tmp.peer_id and s.fk_test_id=tmp.fk_test_id
and s.peer_ip=tmp.peer_ip and s.session_num=tmp.s_max
and (timediff(t.end_time,s.last_seen) > '00:30:00' or completion_rate>=0.95)
and completion_rate<=0.95
limit 10000
