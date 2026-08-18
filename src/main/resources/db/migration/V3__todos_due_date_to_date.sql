-- Todo 의 마감일을 Task 와 동일하게 날짜 단위로 통일한다.
--
-- 기존에는 Todo.dueDate 만 LocalDateTime(datetime) 이었고, API 는 'yyyy-MM-dd' 만 받아
-- 매퍼가 atStartOfDay() 로 변환해 저장했다. 그 결과 "오늘 마감"인 Todo 가 00:00 기준이라
-- 하루 종일 이미 지난 것으로 취급됐고, Task(date) 와 비교할 때도 단위가 어긋났다.
--
-- 시각 정보는 처음부터 의미가 없었으므로(전부 00:00) 절삭 손실 없이 DATE 로 변경한다.
ALTER TABLE todos MODIFY COLUMN due_date DATE NOT NULL;
