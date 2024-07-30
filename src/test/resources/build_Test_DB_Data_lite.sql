INSERT INTO address_info (id,formated_address,postcode,town) VALUES
                                                                 ('10519','Bashley Common Road','BH25 5SF','New Milton'),
                                                                 ('10520','Lower Canterton','SO43 7HD','Brook'),
                                                                 ('11266','Windmill Hill Business Park','SN5 6PB','Swindon'),
                                                                 ('11712','The Granary, Phillips Lane','SP1 3YR','Salisbury'),
                                                                 ('11900','Portland Terrace','SO14 7EG','Southampton'),
                                                                 ('11901','A31 near Southampton','SO43 7PE','Southampton'),
                                                                 ('13352','Emery Down','SO43 7PT','Lyndhurst '),
                                                                 ('13361','69 High Street','SO43 7BE','Lyndhurst'),
                                                                 ('13602','1000 Lakeside North Harbour','PO6 3EN','Portsmouth'),
                                                                 ('15173','Fenn Close','SN5 5BL','Swindon');




INSERT INTO chargers (id,created_at,data_providerid,date_created,date_last_status_update,date_last_verified,latitude,longitude,is_recently_verified,number_of_points,operatorid,operators_reference,status_typeid,submission_status_typeid,updated_at,usage_cost,usage_typeid,uuid,address_info_id,location) VALUES
                                                                                                                                                                                                                                                                                                                 (10519,'2024-07-25 11:23:50.954188',1,'2012-06-01 10:57:00','2017-04-21 17:18:00','2017-04-21 17:18:00',50.781461,-1.657852,0,2,19,NULL,50,200,'2024-07-25 11:23:50.954188',NULL,1,'1F8D9BAF-AF89-46DD-B1BA-6A3A99655BBB','10519',ST_GeomFromText('POINT (-1.657852 50.781461)')),
                                                                                                                                                                                                                                                                                                                 (10520,'2024-07-25 11:23:50.966160',1,'2012-06-01 11:05:00','2017-04-21 17:17:00','2017-04-21 17:17:00',50.919523,-1.619995,0,2,19,NULL,50,200,'2024-07-25 11:23:50.966160',NULL,1,'0CEA9C9A-B1AC-4D3B-AE3F-CDDFD1AA05FF','10520',ST_GeomFromText('POINT (-1.619995 50.919523)')),
                                                                                                                                                                                                                                                                                                                 (11266,'2024-07-25 11:23:36.576011',1,'2012-06-06 14:31:00','2020-03-06 14:17:00','2020-03-06 14:17:00',51.549004,-1.846025,0,1,104,'EM-GB-00000005-01-00001',0,100,'2024-07-25 11:23:36.576012','Charges May Apply',2,'0F5A5B27-4EDC-4841-BAFF-4ACAA2388A10','11266',ST_GeomFromText('POINT (-1.846025 51.549004)')),
                                                                                                                                                                                                                                                                                                                 (11712,'2024-07-25 11:23:51.153105',1,'2012-06-18 14:09:00','2017-04-21 17:16:00','2017-04-21 17:16:00',51.095865,-1.815421,0,2,19,NULL,50,200,'2024-07-25 11:23:51.153105',NULL,1,'E06FDAED-EF31-4401-952D-44CC3B5C1100','11712',ST_GeomFromText('POINT (-1.815421 51.095865)')),
                                                                                                                                                                                                                                                                                                                 (11900,'2024-07-25 11:23:51.738103',1,'2012-09-02 11:41:00','2023-04-04 09:19:00','2023-04-04 09:19:00',50.9056985,-1.40643260000002,0,2,32,'870',50,200,'2024-07-25 11:23:51.738104','£0.59/kWh; other tariffs available',4,'7CE9C108-C328-490E-9675-7DD33E1E096C','11900',ST_GeomFromText('POINT (-1.40643260000002 50.9056985)')),
                                                                                                                                                                                                                                                                                                                 (11901,'2024-07-25 11:23:50.963052',1,'2012-09-02 11:44:00','2012-09-07 11:02:00','2012-09-07 11:02:00',50.8774746,-1.5593394999999646,0,1,8,NULL,50,200,'2024-07-25 11:23:50.963052','Free',4,'AE8F3103-2E11-4DCB-A7AC-9F15ED4F585D','11901',ST_GeomFromText('POINT (-1.5593394999999646 50.8774746)')),
                                                                                                                                                                                                                                                                                                                 (13352,'2024-07-25 11:23:50.967491',1,'2012-09-18 15:43:00','2017-04-21 17:10:00','2017-04-21 17:10:00',50.874479,-1.593356999999969,0,2,19,NULL,50,200,'2024-07-25 11:23:50.967492',NULL,1,'C1B9CDD1-7815-4CCF-BA82-0C7DB53B87C8','13352',ST_GeomFromText('POINT (-1.593356999999969 50.874479)')),
                                                                                                                                                                                                                                                                                                                 (13361,'2024-07-25 11:23:50.962218',1,'2012-09-24 10:26:00','2017-04-21 17:09:00','2017-04-21 17:09:00',50.872662,-1.575196,0,2,19,NULL,50,200,'2024-07-25 11:23:50.962219',NULL,1,'7E1F1E3C-27F2-47C9-96C7-B89C3121B5A4','13361',ST_GeomFromText('POINT (-1.575196 50.872662)')),
                                                                                                                                                                                                                                                                                                                 (13602,'2024-07-25 11:23:51.342123',1,'2012-11-20 09:19:00','2020-07-29 12:28:00','2020-07-29 12:28:00',50.841433,-1.0793,0,2,19,NULL,50,200,'2024-07-25 11:23:51.342124',NULL,1,'F6B02944-9CAC-4C01-ABFA-21C9D7529D52','13602',ST_GeomFromText('POINT (-1.0793 50.841433)')),
                                                                                                                                                                                                                                                                                                                 (15173,'2024-07-25 11:23:31.094142',1,'2012-12-17 08:19:00','2023-10-06 12:48:00','2023-10-06 12:48:00',51.574692,-1.83254050000005,0,2,7,'PG-80113',50,200,'2024-07-25 11:23:31.094142','Free',4,'4C625A07-1B33-47AC-9DBA-BACC37A88901','15173',ST_GeomFromText('POINT (-1.83254050000005 51.574692)'));
INSERT INTO charger_connections (charger_id,amps,connection_typeid,created_at,current_typeid,id,levelid,powerkw,quantity,reference,status_type,status_typeid,updated_at,voltage) VALUES
                                                                                                                                                                                            (10519,13,3,NULL,10,1,2,3,1,NULL,NULL,50,NULL,230),
                                                                                                                                                                                            (10519,32,4,NULL,20,2,2,7,1,NULL,NULL,50,NULL,230),
                                                                                                                                                                                            (10520,13,3,NULL,10,3,2,3,1,NULL,NULL,50,NULL,230),
                                                                                                                                                                                            (10520,32,4,NULL,20,4,2,7,1,NULL,NULL,50,NULL,230),
                                                                                                                                                                                            (11266,13,3,NULL,10,5,2,3,1,NULL,NULL,50,NULL,230),
                                                                                                                                                                                            (11266,32,4,NULL,20,6,2,7,1,NULL,NULL,50,NULL,230),
                                                                                                                                                                                            (11712,13,3,NULL,10,7,2,3,1,NULL,NULL,50,NULL,230),
                                                                                                                                                                                            (11712,32,4,NULL,20,8,2,7,1,NULL,NULL,50,NULL,230),
                                                                                                                                                                                            (11900,13,3,NULL,10,9,2,3,1,NULL,NULL,50,NULL,230),
                                                                                                                                                                                            (11900,32,4,NULL,20,10,2,7,1,NULL,NULL,50,NULL,230),
                                                                                                                                                                                            (11901,13,3,NULL,10,11,2,3,1,NULL,NULL,50,NULL,230),
                                                                                                                                                                                            (11901,32,4,NULL,20,12,2,7,1,NULL,NULL,50,NULL,230),
                                                                                                                                                                                            (13352,13,3,NULL,10,13,2,3,1,NULL,NULL,50,NULL,230),
                                                                                                                                                                                            (13352,32,4,NULL,20,14,2,7,1,NULL,NULL,50,NULL,230),
                                                                                                                                                                                            (13361,13,3,NULL,10,15,2,3,1,NULL,NULL,50,NULL,230),
                                                                                                                                                                                            (13361,32,4,NULL,20,16,2,7,1,NULL,NULL,50,NULL,230),
                                                                                                                                                                                            (13602,13,3,NULL,10,17,2,3,1,NULL,NULL,50,NULL,230),
                                                                                                                                                                                            (13602,32,4,NULL,20,18,2,7,1,NULL,NULL,50,NULL,230),
                                                                                                                                                                                            (13602,13,3,NULL,10,19,2,3,1,NULL,NULL,50,NULL,230),
                                                                                                                                                                                            (13602,32,4,NULL,20,20,2,7,1,NULL,NULL,50,NULL,230),
                                                                                                                                                                                            (15173,13,3,NULL,10,30,2,3,1,NULL,NULL,50,NULL,230),
                                                                                                                                                                                            (15173,32,4,NULL,20,31,2,7,1,NULL,NULL,50,NULL,230),
                                                                                                                                                                                            (15173,13,3,NULL,10,32,2,3,1,NULL,NULL,50,NULL,230),
                                                                                                                                                                                            (15173,32,4,NULL,20,33,2,7,1,NULL,NULL,50,NULL,230);